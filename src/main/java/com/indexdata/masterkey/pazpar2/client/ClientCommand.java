package com.indexdata.masterkey.pazpar2.client;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/**
 * Wraps the query string of a request to the Service Proxy.     
 * 
 * @author Niels Erik Nielsen
 *
 */
public class ClientCommand implements Serializable {
  private static final long serialVersionUID = 5584296831521617821L;
  private static Logger logger = Logger.getLogger(ClientCommand.class);
  public final static String CLIENT_WINDOW_ID_PARAMETER = "windowid";
  public final static String RECORD_FILTER_PARAMETER = "recordfilter";
  public final static String RECORD_QUERY_PARAMETER = "recordquery";  
  public final static String TORUS_QUERY_PARAMETER = "torusquery";
  private String[] nonPz2Parameters = {
    CLIENT_WINDOW_ID_PARAMETER,
    TORUS_QUERY_PARAMETER,    
    RECORD_FILTER_PARAMETER,
    RECORD_QUERY_PARAMETER
  };
  
  public final static String COMMAND_PARAMETER = "command";
  public final static String TARGET_FILTER_PARAMETER = "filter";
  
  private String command = "";
  private String queryString = "";
  private String pz2queryString = "";
  private String targetFilter = "";
  private String recordFilter = "";
  private String recordFilterTargetCriteria = "";
  private String torusParams = "";
  private String recordQuery = "";
  
  /**
   * Create an instance of pazpar2 search command.
   * @param params decoded HTTP params (e.g as returned by the Servlet#getParametersMap)
   * @param queryString original, encoded query string
   * @param nonPz2Params additional names of non-pz2 params
   */
  public ClientCommand(Map<String,String[]> params, String queryString) {
    logger.debug("Creating command object for the request: " + queryString);
    command = getParameter(params, COMMAND_PARAMETER);      
    recordFilter = getParameter(params, RECORD_FILTER_PARAMETER);    
    setRecordFilterElements(recordFilter);       
    torusParams = selectAndEncodeParams(params, "torus");    
    targetFilter = getParameter(params,TARGET_FILTER_PARAMETER);
    this.queryString = queryString;
    setPz2queryString(queryString); 
    if (this.record() && getParameter(params, RECORD_QUERY_PARAMETER).length()>0) {
      setRecordQuery(getParameter(params, RECORD_QUERY_PARAMETER));
    }
    
  }
  
  /*
   * Servlet parameters are a mltivalue map, util method to deal with it. 
   */
  private String getParameter(Map<String,String[]> map, String name) {
    String[] values = map.get(name);
    String value = values != null && values.length > 0 ? values[0] : null;
    if (value == null || value.equals("null")) {
      value = "";
    } 
    return value;
  }
   
  public ClientCommand(String command, String queryString) {
    this.command = command;
    this.queryString = queryString;
    setPz2queryString(queryString);
  }

  /**
   * <pre>
   *  Extracts filter and target criteria into two different elements
   *  i.e. the query string: 
   *      &recordfilter=record-key~record-value[target-key=target-value]
   *  is parsed as:  
   *      recordFilter: record-key~record-value
   *      targetCriteria: target-key=target-value
   * </pre>
   * @param filterExpression
   */
  private void setRecordFilterElements (String filterExpression) {
    if (filterExpression != null && filterExpression.length()>0) {
      Pattern p = Pattern.compile(".*\\[(.*)\\]");
      Matcher m = p.matcher(filterExpression);
      if (m.matches()) {
        recordFilterTargetCriteria = m.group(1);        
      } else {
        logger.debug("No match found for .*\\[(.*)\\] in "+ filterExpression);
      }
      this.recordFilter = filterExpression.replaceAll("\\[.*\\]", "");            
    }
  }

  /**
   * Parses a torus query 
   * 
   * @param requestParams
   * @param prefix
   * @return encoded torus query
   */
  private String selectAndEncodeParams(Map<String, String[]> requestParams, String prefix) {
    String encParams = "";
    String sep = "";
    for (String paramName : requestParams.keySet()) {
      if (paramName.startsWith(prefix)) {
        try {
          if (requestParams.get(paramName)[0].length()>0) {
            encParams += sep + paramName.replaceFirst(prefix, "") + "="
              + URLEncoder.encode(requestParams.get(paramName)[0], "UTF-8");
            sep = "&";
          }
        } catch (UnsupportedEncodingException ence) {
          logger.error("Cannot encode proxied parameters.", ence);
        }
      }
    }    
    return encParams.length() > 0 ? "?" + encParams : null;
  }
  
  /**
   * Is this a 'search' command
   * @return
   */
  public boolean search() {
    return (command != null && command.equals("search"));
  }
  
  public boolean record() {
    return (command != null && command.equals("record"));
  }
  
  public boolean recordWithOffset() {
    return record() && (queryString.contains("&offset=") || queryString.contains("&checksum="));
  }
    
  public String getRecordQuery () {    
    return recordQuery;
  }
  
  public boolean hasRecordQuery() {
    return (recordQuery != null && recordQuery.length()>0);
  }
  
  public void setRecordQuery(String recordQueryParam) {
    try {
      recordQuery = "command=search&query=" + URLEncoder.encode(recordQueryParam, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      logger.error("Error encoding recordquery: " + e.getMessage());
    }
  }
  
  /**
   * Compares queryStrings
   * @param anotherCommand
   * @return
   */
  public boolean isSameAs(ClientCommand anotherCommand) {
    logger.debug("Comparing " + queryString + " to " + anotherCommand);
    return (queryString.equals(anotherCommand.getQueryString()));
  }
  
  /**
   * Does the SP request contain a record filter
   * @return
   */
  public boolean hasRecordFilter() {
    return (recordFilter != null && !recordFilter.equals("null") && recordFilter.length()>0);
  }
  
  public boolean hasRecordFilterTargetCriteria() {
    return (recordFilterTargetCriteria != null && recordFilterTargetCriteria.length()>0);
  }
  
  /**
   * Does the SP request contain a torus query
   * @return
   */
  public boolean hasTorusParams() {
    logger.debug("hasTorusParams: [" + torusParams + "]" );
    return (torusParams != null && torusParams.length() > 0);
  }
  
  /**
   * Is this a 'bytarget' command
   * @return
   */
  public boolean bytarget() {
    return (command != null && command.equals("bytarget"));
  }
  
  /**
   * Is this a 'show' command
   * @return
   */
  public boolean show() {
    return (command != null && command.equals("show"));
  }
  
  /**
   * Is this a 'termlist' command
   * @return true if this is a termlist command
   */
  public boolean termlist() {
    return (command != null && command.equals("termlist"));
  }
  
  public boolean stat() {
    return (command != null && command.equals("stat"));
  }
  
  public boolean ping() {
    return (command != null && command.equals("ping"));
  }

  public String getCommand() {
    return command;
  }

  public String getRecordFilter() {
    return recordFilter;
  }
    
  public String getRecordFilterTargetCriteria() {
    return recordFilterTargetCriteria;
  }
  

  public String getTorusParams() {
    return torusParams;
  }

  public String getTargetFilter() {
    return targetFilter;
  }
  
  public String getQueryString() {
    return queryString;
  }

  /**
   * Strips non-Pazpar2 parameters from query string  
   * in order to send a clean query to Pazpar2. 
   * 
   * @param queryString
   */
  private void setPz2queryString(String queryString) {     
    String tmp = queryString;
    if (queryString != null) {
      for (String s : nonPz2Parameters) {
        tmp = tmp.replaceAll("&?"+s+"=[^&]*", ""); 
      }
      pz2queryString = tmp;
    }
  }
  
  public String getPz2queryString() {
    return pz2queryString;
  }    
}
