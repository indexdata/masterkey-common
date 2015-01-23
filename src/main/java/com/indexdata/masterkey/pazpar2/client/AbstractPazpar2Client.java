/*
 * Copyright (c) 1995-2008, Index Data
 * All rights reserved.
 * See the file LICENSE for details.
 */
package com.indexdata.masterkey.pazpar2.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.indexdata.masterkey.pazpar2.client.exceptions.Pazpar2ErrorException;
import com.indexdata.masterkey.pazpar2.client.exceptions.Pazpar2IOException;
import com.indexdata.masterkey.pazpar2.client.exceptions.Pazpar2InitException;
import com.indexdata.masterkey.pazpar2.client.exceptions.Pazpar2MalformedOutputException;
import com.indexdata.masterkey.pazpar2.client.exceptions.Pazpar2MissingRecordException;
import com.indexdata.masterkey.pazpar2.client.exceptions.ProxyErrorException;
import com.indexdata.utils.PerformanceLogger;
import com.indexdata.utils.XmlUtils;

/**
 * @author jakub, nielserik
 *
 */
public abstract class AbstractPazpar2Client implements Pazpar2Client, Serializable {
  private static final long serialVersionUID = -5281057457054297741L;
  private static int MAX_URL_LENGTH = 2048;
  private static Logger logger = Logger.getLogger(AbstractPazpar2Client.class);
  protected Pazpar2ClientConfiguration cfg = null;
  protected Pazpar2ServiceDefinition serviceDefinition;
  protected Pazpar2Session pazpar2Session = new Pazpar2Session();
  private ConcurrentHashMap<String, Document> results =
    new ConcurrentHashMap<String, Document>();
  private Map<String, long[]> commandTimeStamps = new ConcurrentHashMap<String, long[]>();
  private int searchCount = 0;
  public final String XML_CT = "text/xml;charset=UTF-8";

  public class HttpResponse implements Pazpar2HttpResponse {
    final public int statusCode;
    final public InputStream body;
    final public String contentType;

    HttpResponse(int sc, InputStream is, String ct) {
      statusCode = sc;
      body = is;
      contentType = ct;
    }

    @Override
    public String getContentType() {
      return contentType;
    }

    @Override
    public int getStatusCode() {
      return statusCode;
    }
  }

  protected AbstractPazpar2Client(Pazpar2ClientConfiguration proxyCfg) throws
    ProxyErrorException {
    this.cfg = proxyCfg;
    this.serviceDefinition = new Pazpar2ServiceDefinition(proxyCfg);
    logger.log(Level.DEBUG, "Creating MODE " + proxyCfg.PROXY_MODE + " client.");
  }

  protected abstract boolean requiresForcedInit();

  private void setSessionId(String sessionId) {
    pazpar2Session.setSessionId(sessionId);
  }

  /**
   * Returns current Pazpar2 session ID
   */
  @Override
  public String getSessionId() {
    return pazpar2Session.getSessionId();
  }

  @Override
  public Pazpar2ClientConfiguration getConfiguration() {
    return cfg;
  }

  /**
   * Stores the current search command on the proxy client
   */
  @Override
  public void setSearchCommand(ClientCommand command) {
    searchCount++;
    setTimeStamp(command.getCommand(), searchCount);
    pazpar2Session.setSearchCommand(command);

  }

  @Override
  public int getSearchCount() {
    return searchCount;
  }

  /**
   * Returns the latest search command executed through this proxy client
   * 
   * @return
   */
  @Override
  public ClientCommand getSearchCommand() {
    return pazpar2Session.getSearchCommand();
  }

  private boolean hasSearchCommand() {
    return pazpar2Session.getSearchCommand() != null;
  }

  /**
   * Performs a Pazpar2 search command process
   * 
   * @param os
   *          OutputStream to pipe results to
   * @return 200 if successful
   * @throws IOException
   * @throws Pazpar2ErrorException
   */
  protected HttpResponse executeSearch(OutputStream os) throws IOException,
    Pazpar2ErrorException {
    try {
      if (!pazpar2Session.searchChanged() && sessionIsAlive()) {
        logger.info("The same search command [" + pazpar2Session.
          getSearchQueryString()
          + "] was just issued on session [" + pazpar2Session.getSessionId()
          + "]. Omitting Pazpar2 request.");
        os.write("<search><status>OK</status></search>".getBytes("UTF-8"));
        return new HttpResponse(200, null, XML_CT);
      }
      try {
        if (pazpar2Session.getSessionId() == null) {
          bootstrapSession(this.getSearchCommand());
        }
        return doSearch(os);
      } catch (Pazpar2InitException pz2ie) {
        bootstrapSession(this.getSearchCommand());
        logger.info("Final attempt at [" + pazpar2Session.getSearchCommand().
          getPz2queryString() + "] on session ["
          + getSessionId() + "]");
        return doSearch(os);
      }
    } catch (Pazpar2ErrorException erre) {
      pazpar2Session.resetQuery();
      logger.warn("Pazpar2 application error (" + erre.getAddInfo()
        + "), passed to the client.");
      logger.debug(erre);
      os.write(erre.toXML().getBytes("UTF-8"));
      return new HttpResponse(417, null, XML_CT);
    } catch (Pazpar2IOException pio) {
      pazpar2Session.resetQuery();
      logger.error("Pazpar2 IO error, passed to the client.");
      throw new ProxyErrorException(pio.getMessage(),
        ProxyErrorException.ErrorCode.PAZPAR2_IO_ERROR);
    }
  }

  /**
   * Executes the Pazpar2 search request, including possible changes to target
   * settings, and pipes the results
   * 
   * @param os
   *          OutputStream to pipe results to
   * @return 200 if successful
   * @throws Pazpar2InitException
   * @throws Pazpar2IOException
   * @throws Pazpar2ErrorException
   * @throws IOException
   */
  private HttpResponse doSearch(OutputStream os) throws Pazpar2InitException,
    Pazpar2IOException, Pazpar2ErrorException,
    IOException {
    if (requiresForcedInit())
      init();
    logger.info("Relaying request  [" + pazpar2Session.getSearchCommand().
      getPz2queryString() + "] on session ["
      + getSessionId() + "] to Pazpar2");
    HttpResponse response = request(pazpar2Session.getSearchCommand().
      getPz2queryString());
    pipeStream(response.body, os);
    return response;
  }

  @Override
  public final Pazpar2HttpResponse executeCommand(ClientCommand command,
    OutputStream os) throws
    IOException, Pazpar2ErrorException {
    if (command.search()) {
      return executeSearch(os);
    } else {
      try {
        try {
          logger.debug("Command [" + command.getCommand() + "]. Last search was: [" + pazpar2Session.getSearchQueryString() + "]");
          if (command.record() && !this.hasSearchCommand()) {
            logger.info("Encountered record request on session without a current search. Will attempt to bootstrap a search.");
            bootstrapRecord(command);
          }
          return doCommand(command, os);
        } catch (Pazpar2InitException pz2ie) {
          logger.info("Session is dead. Reinitializing before command " + command.getCommand());
          pazpar2Session.setSessionId(null);
          logger.debug(pz2ie);
          bootstrapSession(command);
          logger.info("Relaying request (final): " + command.getPz2queryString() + " on session [" + pazpar2Session.getSessionId() + "]");
          try {
            return doCommand(command, os);
          } catch (Pazpar2MissingRecordException pz2mre) {
            logger.info("Reinitialized session has a previous search that is missing the requested record. Will attempt to bootstrap a search.");
            logger.debug(pz2mre);
            bootstrapRecord(command);
            return doCommand(command, os);
          }
        } catch (Pazpar2MissingRecordException pz2mre) {
          if (!command.hasRecordQuery()) { 
            logger.error("Record is missing on current session and no recordquery provided to bootstrap another " + pz2mre);
            throw pz2mre;
          } 
          logger.info("Record is missing on current session. Will retry once and then while there are active clients.");
          logger.debug(pz2mre);
          bootstrapRecord(command);
          boolean recordMissing=true;
          boolean activeClients = true;
          HttpResponse recordResponse = null;
          while (recordMissing && activeClients) {
            try {
              logger.debug("Requesting record again.");
              recordResponse = doCommand(command, os);
              recordMissing=false;
            } catch (Pazpar2MissingRecordException e) {
              logger.warn("The requested record still not found in pazpar2 result set.");
              activeClients = Integer.parseInt(getResults("show").getElementsByTagName("activeclients").item(0).getTextContent())>0;
              if (activeClients) {
                logger.warn("There are still active clients. Trying to request the record again.");
                doShow();
              } else {
                logger.error("No more active clients. Giving up on finding the requested record.");
                throw e;
              }
            }
          }
          return recordResponse;
        }
      } catch (Pazpar2ErrorException erre) {
        logger.warn("Pazpar2 application error (" + erre.getAddInfo()
          + "), passed to the client.");
        logger.debug(erre);
        os.write(erre.toXML().getBytes("UTF-8"));
        return new HttpResponse(417, null, XML_CT);
      } catch (Pazpar2IOException pio) {
        logger.error("Pazpar2 IO error, passed to the client.");
        throw new ProxyErrorException(pio.getMessage(),
          ProxyErrorException.ErrorCode.PAZPAR2_IO_ERROR);
      }
    }
  }

  /**
   * Runs command against Pazpar2, streams the results out and puts the document
   * on the client for later use
   * 
   * @param command
   *          Command to run against Pazpar2
   * @param os
   *          OutputStream to pipe to
   * @return 200 if successful
   * @throws Pazpar2IOException
   * @throws IOException
   * @throws Pazpar2ErrorException
   */
  private HttpResponse doCommand(ClientCommand command, OutputStream os)
    throws
    Pazpar2IOException, IOException,
    Pazpar2ErrorException {
    HttpResponse response = request(command.getPz2queryString());
    logger.debug("Completed Pazpar2 request on session ["+ getSessionId() +"]: " + command.getPz2queryString());
    pipeStream(response.body, os);
    cacheResults(command.getCommand(), os, response.contentType);
    return response;
  }

  /**
   * For doing 'show' command and settings results as part of a bootstrap
   * process i.e. after Pazpar2 init exception or missing record exception
   * 
   * @throws IOException
   * @throws Pazpar2ErrorException
   * @throws Pazpar2IOException
   */
  private void doShow() throws IOException, Pazpar2ErrorException,
    Pazpar2IOException {
    HttpResponse showResponse = request("command=show&block=1");
    OutputStream showOs = new ByteArrayOutputStream();
    pipeStream(showResponse.body, showOs);
    cacheResults("show", showOs, showResponse.contentType);
    showOs.close();
  }

  /**
   * Looks for a recordquery in the record command and executes that query. Will
   * check that Pazpar2 session is alive and re-initialize if not
   * 
   * @param command
   * @throws Pazpar2IOException
   * @throws Pazpar2ErrorException
   * @throws IOException
   */
  private void bootstrapRecord(ClientCommand command) throws
    Pazpar2IOException, Pazpar2ErrorException,
    IOException {
    if (command.record()) {
      if (command.hasRecordQuery()) {
        logger.info("Running the search [" + command.getRecordQuery()
            + "] to bring in records in Pazpar2 memory before record command + ["
            + command.getPz2queryString() + "]");
        this.setSearchCommand(new ClientCommand("search", command.
          getRecordQuery()));
        if (!sessionIsAlive()) {
          init();
        }
        request(command.getRecordQuery());
        doShow();
      } else {
        logger.warn(
          "No record query provided to bring up record in Pazpar2 memory. The requested record will probably be missing.");
      }
    } else {
      logger.error("Wrong input: Cannot bootstrap record with command + ["
        + command.getCommand() + "]");
    }
  }

  /**
   * Will re-initialize a dead session and
   * 
   * @param command
   * @throws Pazpar2IOException
   * @throws Pazpar2ErrorException
   * @throws IOException
   */
  private void bootstrapSession(ClientCommand command) throws
    Pazpar2IOException, Pazpar2ErrorException,
    IOException {
    if (!sessionIsAlive() && (command.record() || command.bytarget() || command.
      show() || command.termlist() || command.stat() || command.ping())) {
      logger.info("Reinitializing session before command "
        + command.getCommand());
      init();
      logger.info("Reinitialized session with ID [" + getSessionId() + "]");
      logger.info("Request command '" + command.getCommand()
        + "' requires to re-execute the previous search ["
        + pazpar2Session.getSearchQueryString() + "] on session ["
        + getSessionId() + "]");
      if (pazpar2Session.getSearchCommand() != null) {
        request(pazpar2Session.getSearchCommand().getPz2queryString());
        doShow();
      } else {
        logger.error("No previous search was found in the http session ["
          + getSessionId() + "].");
      }
      //bootstrap the session for any command
    } else if (!sessionIsAlive()) {
      logger.info("Session is dead. Reinitializing before command " + command.
        getCommand());
      init();
      logger.info("Reinitialized session with ID [" + getSessionId() + "]");
    } else {
      logger.warn("Session was alive");
    }
  }

  /**
   * Time stamps most recent request of this command per executed search
   * 
   * @param command
   */
  public void setTimeStamp(String command, int searchCount) {
    long timeStamp = System.currentTimeMillis();
    if (commandTimeStamps != null) {
      if (commandTimeStamps.get(command + "-" + searchCount) == null) {
        commandTimeStamps.put(command + "-" + searchCount, new long[]{timeStamp});
      } else {
        commandTimeStamps.get(command + "-" + searchCount)[0] = timeStamp;
      }
    }
    if (logger.isDebugEnabled()) {
      logger.debug(getTimeStampsLogStmt());
    }
  }

  /**
   * Returns the number of times this command has be executed with a result
   * since the current search was initiated.
   * 
   */
  @Override
  public long getTimeStamp(String command, int searchNumber) {
    if (commandTimeStamps.containsKey(command + "-" + searchNumber)) {
      return commandTimeStamps.get(command + "-" + searchNumber)[0];
    } else {
      logger.debug("No results time stamped for command [" + command + "-"
        + searchNumber + "]");
      return 0;
    }
  }

  /**
   * Caches results XML of a Pazpar2 request
   * 
   * @param command
   *          Cache key
   * @param os
   *          OutputStream to get results from
   * @throws ProxyErrorException
   * @throws IOException
   */
  private boolean cacheResults(String command, OutputStream os,
    String contentType) throws
    ProxyErrorException, IOException {
    try {
      if (!contentType.contains("xml")) {
        logger.warn("Results cannot be cached for Content-Type: " + contentType);
        return false;
      }
      if (!(os instanceof ByteArrayOutputStream)) {
        logger.warn(
          "Relay seems to be operating in a direct mode, result caching not possible.");
        return false;
      }
      logger.debug("Storing " + command + " results on pazpar2 client.");
      Document result =
        XmlUtils.parse(new StringReader(((ByteArrayOutputStream) os).toString(
        "UTF-8")));
      results.put(command, result);
      setTimeStamp(command, searchCount);
      return true;
    } catch (SAXException se) {
      throw new ProxyErrorException("SAX error when caching results of "
        + command + " command.",
        ProxyErrorException.ErrorCode.PAZPAR2_IO_ERROR);
    }
  }

  @Override
  public Document getResults(String command) {
    if (results.get(command) == null) {
      logger.error("No cached results found for " + command);
    }
    return results.get(command);
  }

  /**
   * Send pazpar2 protocol request.
   * 
   * @param queryString
   *          url encoded query string
   * @return response
   * @throws com.indexdata.masterkey.pazpar2.exceptions.Pazpar2IOException
   * @throws com.indexdata.masterkey.pazpar2.exceptions.Pazpar2ErrorException
   * @throws com.indexdata.masterkey.pazpar2.exceptions.Pazpar2MalformedOutputException
   */
  protected HttpResponse request(String queryString) throws Pazpar2IOException,
    Pazpar2ErrorException {
    queryString = (queryString != null) ? "&" + queryString : "";
    return sendRequest("session=" + getSessionId() + queryString);
  }

  /**
   * Pipes the last response input stream to a specified output stream.
   * 
   * @param os
   *          output stream to pipe to
   * @throws java.io.IOException
   */
  protected void pipeStream(InputStream is, OutputStream os) throws IOException {
    byte[] buf = new byte[cfg.STREAMBUFF_SIZE];
    for (int len = -1; (len = is.read(buf)) != -1;) {
      os.write(buf, 0, len);
    }
  }

  /**
   * Initializes a Pazpar2 session and stores the Pazpar2 session ID with the
   * proxy client
   * 
   * @param clear
   * @throws Pazpar2IOException
   * @throws Pazpar2ErrorException
   */
  protected void sendInit(boolean clear) throws Pazpar2IOException,
    Pazpar2ErrorException {
    sendInit(clear, null);
  }

  protected void sendInit(Document service) throws Pazpar2IOException,
    Pazpar2ErrorException {
    sendInit(false, service);
  }

  private void sendInit(boolean clear, Document service) throws
    Pazpar2IOException,
    Pazpar2ErrorException {
    HttpResponse response = null;
    String clearOpt = clear ? "&clear=1" : "";
    String requestUrl = cfg.PAZPAR2_URL + "?" + "command=init" + clearOpt;
    long startTime = PerformanceLogger.start(" <PZ2 INIT", requestUrl);
    HttpClient hc = new HttpClient();
    HttpMethod hm = null;
    if (service != null) {
      logger.debug("Sending Pazpar2 init using auto-generated service definition: "
        + requestUrl);
      hm = new PostMethod(requestUrl);
      StringWriter sw = new StringWriter();
      try {
        XmlUtils.serialize(service, sw);
        ((EntityEnclosingMethod) hm).setRequestEntity(new StringRequestEntity(
          sw.toString(), "text/xml", "UTF-8"));
      } catch (Exception ex) {
        throw new Pazpar2IOException(
          "Error encoding auto-generated service definition for POST method", ex);
      }
    } else if (serviceDefinition.usesXml()) {
      logger.debug("Sending Pazpar2 init using service definition from XML: "
        + requestUrl);
      hm = new PostMethod(requestUrl);
      try {
        ((EntityEnclosingMethod) hm).setRequestEntity(
          new StringRequestEntity(serviceDefinition.getServiceXml(),
          "text/xml", "UTF-8"));
      } catch (UnsupportedEncodingException e) {
        throw new Pazpar2IOException(
          "Error encoding service definition XML for POST method");
      }
    } else if (serviceDefinition.usesId()) {
      requestUrl = requestUrl + "&service=" + serviceDefinition.getServiceId();
      logger.debug("Sending Pazpar2 init using service ID: " + requestUrl);
      hm = new GetMethod(requestUrl);
    } else {
      logger.debug("Sending Pazpar2 init using default service: " + requestUrl);
      hm = new GetMethod(requestUrl);
    }
    try {
      response = new HttpResponse(hc.executeMethod(hm), hm.
        getResponseBodyAsStream(), hm.getResponseHeader(
        "Content-Type").getValue());
    } catch (IOException e) {
      throw new Pazpar2IOException("HTTP I/O error when contacting pazpar2", e);
    }
    if (response.statusCode == HttpStatus.SC_EXPECTATION_FAILED) // 417
    {
      parseAndThrowError(response.body);
    } else if (response.statusCode != HttpStatus.SC_OK) // 200
    {
      throw new Pazpar2IOException("Unexpected HTTP response code ("
        + response.statusCode + ") returned for "
        + hm.getName() + " " + cfg.PAZPAR2_URL + "?" + hm.getQueryString());
    }

    try {
      Document domDoc = XmlUtils.parse(response.body);
      Node sessNode = domDoc.getElementsByTagName("session").item(0);
      pazpar2Session.setSessionId(sessNode.getTextContent());
    } catch (Exception e) {
      throw new Pazpar2MalformedOutputException(
        "Cannot parse pazpar2 session id.", e);
    }
    logger.info("Initialized a Pazpar2 session with id [" + getSessionId() + "]");
    PerformanceLogger.finish(" <INIT DONE", requestUrl, startTime);
  }

  /**
   * Sends a Pazpar2 ping command for the current Pazpar2 session ID (if any
   * exists) Returns true if the Pazpar2 session ID exists and Pazpar2 says the
   * session is alive
   */
  protected boolean sessionIsAlive() {
    if (pazpar2Session.getSessionId() == null) {
      return false;
    } else {
      try {
        sendRequest("command=ping&session=" + pazpar2Session.getSessionId());
      } catch (Exception e) {
        setSessionId(null);
        return false;
      }
    }
    return true;
  }

  /**
   * Sends a Pazpar2 request off to the configured Pazpar2 URL and with the
   * provided parameters
   * 
   * @param encodedParams
   * @return
   * @throws Pazpar2IOException
   * @throws Pazpar2ErrorException
   */
  protected HttpResponse sendRequest(String encodedParams) throws
    Pazpar2IOException, Pazpar2ErrorException {
    HttpResponse response = null;
    String requestUrl = cfg.PAZPAR2_URL + "?" + encodedParams;
    logger.debug("Sending request: " + requestUrl);
    long startTime = PerformanceLogger.start(" >PZ2REQ", requestUrl);
    HttpClient hc = new HttpClient();
    HttpMethod hm = null;

    if (requestUrl.length() < MAX_URL_LENGTH) {
      hm = new GetMethod(requestUrl);
    } else {
      hm = new PostMethod(cfg.PAZPAR2_URL);
      RequestEntity re;
      try {
        re = new StringRequestEntity(encodedParams,
          "application/x-www-form-urlencoded", "UTF-8");
      } catch (UnsupportedEncodingException ex) {
        throw new Pazpar2IOException(
          "Cannot encode parameters for the POST request");
      }
      ((EntityEnclosingMethod) hm).setRequestEntity(re);
    }
    try {
      response = new HttpResponse(hc.executeMethod(hm), hm.
        getResponseBodyAsStream(), hm.getResponseHeader(
        "Content-Type").getValue());
    } catch (IOException e) {
      throw new Pazpar2IOException("HTTP I/O error when contacting pazpar2", e);
    }
    PerformanceLogger.finish(" <PZ2REQ DONE", requestUrl, startTime);
    if (response.statusCode == HttpStatus.SC_EXPECTATION_FAILED) // 417
    {
      parseAndThrowError(response.body);
    } else if (response.statusCode != HttpStatus.SC_OK) // 200
    {
      throw new Pazpar2IOException("Unexpected HTTP response code ("
        + response.statusCode + ") returned for "
        + hm.getName() + " " + cfg.PAZPAR2_URL + "?" + hm.getQueryString());
    }
    return response;
  }
  
  protected HttpResponse post(String queryString, Document entity) 
    throws Pazpar2IOException, Pazpar2ErrorException {
    HttpResponse response = null;
    queryString = "?session=" + getSessionId() + "&" +
      (queryString != null && !queryString.isEmpty() 
        ? (queryString.charAt(0) == '?' || queryString.charAt(0) == '&'
           ? queryString.substring(1) : queryString) 
        : "");
    String requestUrl = cfg.PAZPAR2_URL + queryString;
    long startTime = PerformanceLogger.start(" <PZ2 POST", requestUrl);
    HttpClient hc = new HttpClient();
    HttpMethod hm = new PostMethod(requestUrl);
    StringWriter sw = new StringWriter();
    try {
      XmlUtils.serialize(entity, sw);
      ((EntityEnclosingMethod) hm).setRequestEntity(new StringRequestEntity(
        sw.toString(), "text/xml", "UTF-8"));
    } catch (Exception ex) {
      throw new Pazpar2IOException(
        "Error serializing document for POST", ex);
    }
    try {
      response = new HttpResponse(hc.executeMethod(hm), hm.
        getResponseBodyAsStream(), hm.getResponseHeader(
        "Content-Type").getValue());
    } catch (IOException e) {
      throw new Pazpar2IOException("HTTP I/O error when contacting pazpar2", e);
    }
    if (response.statusCode == HttpStatus.SC_EXPECTATION_FAILED) // 417
    {
      parseAndThrowError(response.body);
    } else if (response.statusCode != HttpStatus.SC_OK) { // 200 
      throw new Pazpar2IOException("Unexpected HTTP response code ("
        + response.statusCode + ") returned for "
        + hm.getName() + " " + cfg.PAZPAR2_URL + "?" + hm.getQueryString());
    }
    PerformanceLogger.finish(" <POST DONE", requestUrl, startTime);
    return response;
  }

  private void parseAndThrowError(InputStream is) throws Pazpar2ErrorException,
    Pazpar2MalformedOutputException {
    int errorCode;
    String errorMsg, addInfo;
    try {
      Document domDoc = XmlUtils.parse(is);
      Node errorNode = domDoc.getElementsByTagName("error").item(0);
      errorCode = Integer.parseInt(errorNode.getAttributes().getNamedItem("code").
        getTextContent());
      errorMsg = errorNode.getAttributes().getNamedItem("msg").getTextContent();
      addInfo = errorNode.getTextContent();
    } catch (Exception e) {
      throw new Pazpar2MalformedOutputException(
        "Cannot parse pazpar2 error response", e);
    }
    switch (errorCode) {
      case 1:
        throw new Pazpar2InitException(errorMsg + ": " + addInfo, errorCode,
          errorMsg, addInfo);
      case 7:
        throw new Pazpar2MissingRecordException(errorMsg + ": " + addInfo,
          errorCode, errorMsg, addInfo);
      default:
        throw new Pazpar2ErrorException(errorMsg + ": " + addInfo, errorCode,
          errorMsg, addInfo);
    }
  }

  /**
   * Logs (debug) the latest time stamps for each command type on each search 
   */
  private String getTimeStampsLogStmt() {
    StringBuilder logstr = new StringBuilder("Requests on pz2 session ["
      + pazpar2Session.getSessionId() + "]: ");
    Map<Date, String> sort = new ConcurrentHashMap<Date, String>();
    Iterator<String> timestampIter = commandTimeStamps.keySet().iterator();
    while (timestampIter.hasNext()) {
      String key = timestampIter.next();
      sort.put(new Date(commandTimeStamps.get(key)[0]), key);
    }
    Iterator<Date> sortIterator = sort.keySet().iterator();
    Format formatter = new SimpleDateFormat("HH:mm:ss");
    while (sortIterator.hasNext()) {
      Date date = sortIterator.next();
      logstr.append(formatter.format(date)).append(": ").append(sort.get(date)).
        append(" ");
    }
    return logstr.toString();
  }

  /**
   * Returns a 'hit' by recid from the latest 'show' document.
   * @return A Record Document based on a hit in search results
   */
  @Override
  public Document getHit(String recid) {
    NodeList hits = null;
    Node hit = null;
    Document recordDoc = null;
    try {
      hits = XmlUtils.getNodeList(getResults("show"), "//hit[recid='" + recid
        + "']");
    } catch (XPathExpressionException e) {
      logger.error("XPath expression into show document failed for recid ["
        + recid + "]");
    }

    if (hits != null && hits.getLength() == 1) {
      logger.debug("Found hit for record id [" + recid + "]");
      hit = hits.item(0);
      recordDoc = XmlUtils.newDoc();
      Node importedHit = recordDoc.importNode(hit, true);
      recordDoc.renameNode(importedHit, null, "record");
      recordDoc.appendChild(importedHit);
    } else {
      logger.warn("Did not find hit for record id [" + recid + "]");
    }
    return recordDoc;
  }

  /* (non-Javadoc)
   * @see com.indexdata.masterkey.pazpar2.proxy.Pazpar2Client#recordRequest(java.lang.String, int)
   */
  @Override
  public Document recordRequest(String recid, int offset) throws
    Pazpar2ErrorException, Pazpar2IOException {
    Document doc = null;
    try {
      String queryString = "command=record&id=" + URLEncoder.encode(recid,
        "UTF-8") + "&offset=" + offset;
      HttpResponse response = request(queryString);
      doc = XmlUtils.parse(response.body);
    } catch (UnsupportedEncodingException uee) {
      throw new RuntimeException(uee);
    } catch (SAXException se) {
      throw new Pazpar2MalformedOutputException(se);
    } catch (IOException ioe) { //coming from sax parsing
      throw new Pazpar2MalformedOutputException(ioe);
    } 
    return doc;
  }
  
  /* (non-Javadoc)
   * @see com.indexdata.masterkey.pazpar2.proxy.Pazpar2Client#recordRequest(java.lang.String, int)
   */
  @Override
  public Document recordRequest(String recid, int offset, String syntax) throws
    Pazpar2ErrorException, Pazpar2IOException {
    Document doc = null;
    try {
      String queryString = "command=record&id=" + URLEncoder.encode(recid,
        "UTF-8") + "&offset=" + offset + "&syntax=" + syntax;
      HttpResponse response = request(queryString);
      doc = XmlUtils.parse(response.body);
    } catch (UnsupportedEncodingException uee) {
      throw new RuntimeException(uee);
    } catch (SAXException se) {
      throw new Pazpar2MalformedOutputException(se);
    } catch (IOException ioe) { //coming from sax parsing
      throw new Pazpar2MalformedOutputException(ioe);
    } 
    return doc;
  }


  /* (non-Javadoc)
   * @see com.indexdata.masterkey.pazpar2.proxy.Pazpar2Client#recordRequest(java.lang.String)
   */
  @Override
  public Document recordRequest(String recid) throws Pazpar2ErrorException, 
    Pazpar2IOException{
    Document doc = null;
    try {
      String queryString = "command=record&id=" + URLEncoder.encode(recid,
        "UTF-8");
      HttpResponse response = request(queryString);
      doc = XmlUtils.parse(response.body);
    } catch (UnsupportedEncodingException uee) {
      throw new RuntimeException(uee);
    } catch (SAXException se) {
      throw new Pazpar2MalformedOutputException(se);
    } catch (IOException ioe) { //coming from sax parsing
      throw new Pazpar2MalformedOutputException(ioe);
    } 
    return doc;
  }

  /* (non-Javadoc)
   * @see com.indexdata.masterkey.pazpar2.proxy.Pazpar2Client#showRequest()
   */
  @Override
  public Document showRequest() throws Pazpar2ErrorException, 
    Pazpar2IOException {
    Document doc = null;
    try {
      String queryString = "command=show";
      HttpResponse response = request(queryString);
      doc = XmlUtils.parse(response.body);
    } catch (UnsupportedEncodingException uee) {
      throw new RuntimeException(uee);
    } catch (SAXException se) {
      throw new Pazpar2MalformedOutputException(se);
    } catch (IOException ioe) { //coming from sax parsing
      throw new Pazpar2MalformedOutputException(ioe);
    } finally {
      logger.error("Error processing record offset request, message passed on");
    }
    return doc;
  }

  /* (non-Javadoc)
   * @see com.indexdata.masterkey.pazpar2.proxy.Pazpar2Client#searchRequest(java.lang.String)
   */
  @Override
  public Document searchRequest(String queryString) throws Pazpar2ErrorException, 
    Pazpar2IOException {
    if (searchCount > 0) {
      logger.error(
        "Attempt to make an 'embedded' search on a pazpar2 client that already has an active, managed search.");
      throw new UnsupportedOperationException(
        "Attempt to make an 'embedded' search on a pazpar2 client that already has an active, managed search.");
    }
    Document doc = null;
    try {
      HttpResponse response = request("command=search&query=" + queryString);
      doc = XmlUtils.parse(response.body);
    } catch (UnsupportedEncodingException uee) {
      throw new RuntimeException(uee);
    } catch (SAXException se) {
      throw new Pazpar2MalformedOutputException(se);
    } catch (IOException ioe) { //coming from sax parsing
      throw new Pazpar2MalformedOutputException(ioe);
    } finally {
      logger.error("Error processing record offset request, message passed on");
    }
    return doc;
  }
}
