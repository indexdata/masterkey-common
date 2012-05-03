/*
 * Copyright (c) 1995-2008, Index Data
 * All rights reserved.
 * See the file LICENSE for details.
 */
package com.indexdata.masterkey.pazpar2.client;

import com.indexdata.masterkey.pazpar2.client.exceptions.Pazpar2ErrorException;
import com.indexdata.masterkey.pazpar2.client.exceptions.Pazpar2IOException;
import com.indexdata.masterkey.pazpar2.client.exceptions.ProxyErrorException;
import com.indexdata.rest.client.ResourceConnectionException;
import com.indexdata.rest.client.ResourceConnector;
import com.indexdata.rest.client.TorusConnectorFactory;
import com.indexdata.torus.Records;
import com.indexdata.utils.PerformanceLogger;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.log4j.Logger;

/**
 * Creates a Pazpar2 client with settings retrieved from the torus 
 *
 * @author jakub
 */
public class Pazpar2ClientTorus extends AbstractPazpar2Client {
  private static Logger logger = Logger.getLogger(Pazpar2ClientTorus.class);
  private Pazpar2Settings targetSettings = null;
  private String realm = null;

  /**
   * Instantiates MODE 2 client
   * @param cfg
   * @throws ProxyErrorException
   */
  public Pazpar2ClientTorus(Pazpar2ClientConfiguration cfg)
    throws ProxyErrorException {
    super(cfg);
    this.realm = cfg.TORUS_REALM;
  }
  

  /**
   * Instantiates MODE 3 client
   * @param proxyCfg
   * @param realm
   * @throws ProxyErrorException
   */
  public Pazpar2ClientTorus(Pazpar2ClientConfiguration proxyCfg, String realm)
    throws ProxyErrorException {
    super(proxyCfg);
    this.realm = realm;
  }
    

  @Override
  public Pazpar2Settings getSettings() {
    return targetSettings;
  }
  
  private void setSettings(Pazpar2Settings settings) {
    targetSettings = settings;
  }
  
  /**
   * Applies torus query for target selection and/or record filter to selected targets.
   */
  @Override
  protected boolean requiresForcedInit() {
    boolean needsInit = false;
    if (pazpar2Session.torusQueryChanged()) {
      logger.info("Torus query changed on session ["
        + pazpar2Session.getSessionId()
        + "]. Reinitializing session and loading new target set.");
      targetSettings = null;
      needsInit = true;
    }
    if (pazpar2Session.recordFilterChanged()) {
      logger.info("Record filtering changed to ["
        + pazpar2Session.getSearchCommand().getRecordFilter()
        + "] on session [" + pazpar2Session.getSessionId()
        + "]. Setting filtering before sending search to Pazpar2.");
      needsInit = true;
    }
    return needsInit;
  }

  /**
   * Initializes a Pazpar2 sessions while clearing any statically defined
   * databases from Pazpar2's configuration and loading and from the target 
   * repository and setting them up on the session.
   */
  @Override
  public void init() throws Pazpar2IOException, Pazpar2ErrorException {
    sendInit(true);
    long startTime = PerformanceLogger.start(" >SETTS", "Target Settings");
    if (targetSettings == null) {
      logger.info("Fetching target settings from the torus...");
      Records targetProfiles =  fetchTargetProfiles();
      targetSettings = Pazpar2Settings.fromSearchables(targetProfiles, cfg);
    }
    String recordFilter = null, recordFilterCriteria = null;
    ClientCommand cmd = pazpar2Session.getSearchCommand();
    if (cmd != null && cmd.hasRecordFilter()) {
      recordFilter = cmd.getRecordFilter();
      recordFilterCriteria = cmd.getRecordFilterTargetCriteria();
    }
    logger.info("Re-instating 'recordfilter' on target settings...");
    targetSettings.setRecordFilter(recordFilter, recordFilterCriteria);
    setup(targetSettings);
    PerformanceLogger.finish(" <SETTS DONE", "Target Settings", startTime);
  }
  
  private void setup(Pazpar2Settings settings)
    throws ProxyErrorException, Pazpar2IOException, Pazpar2ErrorException {
    post("command=settings", settings.toXml(null));
  }
  
  private Records fetchTargetProfiles() throws ProxyErrorException {
    String torusParams = (pazpar2Session.getSearchCommand() == null || pazpar2Session
	.getSearchCommand().getTorusParams() == null) 
      ? cfg.TORUS_PARAMS 
      : pazpar2Session.getSearchCommand().getTorusParams();
    String torusURI = TorusConnectorFactory.getTorusURL(cfg.TORUS_BASEURL, 
      "searchable", realm, torusParams);
    logger.info("Loading target settings from the torus at " + torusURI);
    Records records = null;
    try {
      URL torusUrl = new URL(torusURI);
      logger.info("Connecting to the targets toroid at " + torusUrl.toExternalForm()
	  + "...");
      ResourceConnector<Records> torusConn = new ResourceConnector<Records>(torusUrl,
	  "com.indexdata.torus.layer" + ":com.indexdata.torus");
      records = (Records) torusConn.get();
      if (records == null || records.getRecords() == null) {
	logger.debug("Got no resources to search from Torus");
	throw new ProxyErrorException("No search targets retrieved from the torus",
	    ProxyErrorException.ErrorCode.TARGET_TORUS_ERROR);
      }
    } catch (MalformedURLException male) {
      logger.error("Cannot connect to the target toroid, URL malformed.");
      logger.debug(male);
      throw new ProxyErrorException("Cannot connect to the target torus, URL malformed.",
	  ProxyErrorException.ErrorCode.TARGET_TORUS_ERROR);
    } catch (ResourceConnectionException rce) {
      logger.error("Cannot connect to the target torus");
      logger.debug(rce);
      // torus1 may return 404 on no resources (instead of empty list like
      // torus2)
      // still, it's probably OK to raise since search would cause an error
      // anyway (NO TARGETS)
      throw new ProxyErrorException("Cannot connect to the target torus.",
	  ProxyErrorException.ErrorCode.TARGET_TORUS_ERROR);

    }
    return records;
  }

  @Override
  public Pazpar2Client cloneMe() throws ProxyErrorException, Pazpar2ErrorException, Pazpar2IOException {
    Pazpar2ClientTorus client = null;
    if (cfg.PROXY_MODE == 2) {
      client = new Pazpar2ClientTorus(cfg);
    } else if (cfg.PROXY_MODE == 3) {
      client = new Pazpar2ClientTorus(cfg, realm);
    } else {
      throw new ProxyErrorException(
        "Clone operation cannot determine the PROXY MODE",
        ProxyErrorException.ErrorCode.CONFIGURATION_ERROR);
    }
    // this is unsafe as the cloned instances will share the settings object
    client.setSettings(this.getSettings());
    client.init();
    return client;
  }
  
}
