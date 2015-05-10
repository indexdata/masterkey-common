/*
 * Copyright (c) 1995-2008, Index Data
 * All rights reserved.
 * See the file LICENSE for details.
 */
package com.indexdata.masterkey.pazpar2.client;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.indexdata.masterkey.pazpar2.client.exceptions.Pazpar2ErrorException;
import com.indexdata.masterkey.pazpar2.client.exceptions.Pazpar2IOException;
import com.indexdata.masterkey.pazpar2.client.exceptions.ProxyErrorException;
import com.indexdata.utils.XmlUtils;

/**
 * @author nielserik, jakub
 * 
 */
public class Pazpar2ClientGeneric extends AbstractPazpar2Client {
  private static final long serialVersionUID = -972629015790377227L;

  private static Logger logger = Logger.getLogger(Pazpar2ClientGeneric.class);

  public Pazpar2ClientGeneric(Pazpar2ClientConfiguration cfg)
      throws ProxyErrorException {
    super(cfg);
  }

  @Override
  protected boolean requiresForcedInit() {
    return false;
  }

  /**
   * Initializes a Pazpar2 session, either retaining statically defined databases
   * from Pazpar2's configuration or posting statically defined databases from local file
   */
  @Override
  public void init() throws Pazpar2IOException, Pazpar2ErrorException {
    if (cfg.PAZPAR2_SETTINGS_XML != null && cfg.PAZPAR2_SETTINGS_XML.length() > 0) {
      String pz2SettingsXmlFileName = cfg.XML_FILE_PATH + "/" + cfg.PAZPAR2_SETTINGS_XML;
      logger.debug("Found pointer to pazpar2 settings xml: " + pz2SettingsXmlFileName);      
      try {
        Document pz2TargetSettings = loadSettingsXml(pz2SettingsXmlFileName);
        sendInit(true);
        post("command=settings", pz2TargetSettings);
      } catch (SAXException e) {
        logger.error("Error in generic Pazpar2 client - could not load target settings.");
        throw new Pazpar2ErrorException("SAX error while attempting to load target settings from file " + pz2SettingsXmlFileName,0,e.getMessage(),e.toString());
      } catch (IOException e) {
        logger.error("Error in generic Pazpar2 client - could not load or POST target settings.");
        throw new Pazpar2ErrorException("IO error while attempting to load and POST target settings from file " + pz2SettingsXmlFileName,0,e.getMessage(),e.toString());      
      }
    } else {
      logger.debug("No target settings defined locally - will initiate Pazpar2 session with remote settings.");
      sendInit(false);
    }
  }

  @Override
  public Pazpar2Client cloneMe() throws Pazpar2ErrorException,
      Pazpar2IOException {
    Pazpar2Client client = new Pazpar2ClientGeneric(this.cfg);
    client.init();
    return client;
  }

  @Override
  public Pazpar2Settings getSettings() {
    return null;
  }

  /**
   * Reads the pazpar2 target settings from a XML file
   * 
   * @param settingsXmlFileName
   * @return The settings XML
   * @throws ProxyErrorException
   */
  private Document loadSettingsXml(String serviceXmlFileName)
      throws SAXException, IOException {
    File serviceXmlFile = new File(serviceXmlFileName);
    return XmlUtils.parse(serviceXmlFile);
  }

}
