/*
 * Copyright (c) 1995-2010, Index Data
 * All rights reserved.
 * See the file LICENSE for details.
 */
package com.indexdata.masterkey.pazpar2.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.indexdata.masterkey.pazpar2.client.exceptions.ProxyErrorException;

/**
 * Defines the Pazpar2 service definition to use, based on the configuration
 * in the service proxy's property file
 * <p/>
 * There are three ways to define the service:
 * <ol>
 *   <li>Use service settings as defined in provided XML file 
 *   <li>If no XML file provided: Use service settings as identified by provided ID
 *   <li>If no ID provided: Use default service as defined in pazpar2.cfg
 * </ol>
 * @author Niels Erik
 */
public class Pazpar2ServiceDefinition {
  public static final int UNDEFINED = 0;
  private static final int USES_XML = 1;
  private static final int USES_ID = 2;
  private static final int USES_PZ2_DEFAULT = 3;
  private int serviceDefinitionType = 0;
  private String pz2ServiceId = "";
  private String pz2ServiceXmlFileName = "";
  private String pz2ServiceXml = "";
  private static Logger logger =
    Logger.getLogger(Pazpar2ServiceDefinition.class);

  public Pazpar2ServiceDefinition(Pazpar2ClientConfiguration configuration)
    throws ProxyErrorException {
    if (configuration.PAZPAR2_SERVICE_XML != null && configuration.PAZPAR2_SERVICE_XML.
      length() > 0) {
      serviceDefinitionType = USES_XML;
      pz2ServiceXmlFileName = configuration.XML_FILE_PATH
        + "/"
        + configuration.PAZPAR2_SERVICE_XML;
      logger.debug("Found pointer to pazpar2 service xml: "
        + pz2ServiceXmlFileName);
      pz2ServiceXml = loadServiceXml(pz2ServiceXmlFileName);
    } else if (configuration.PAZPAR2_SERVICE_ID != null && configuration.PAZPAR2_SERVICE_ID.
      length() > 0) {
      serviceDefinitionType = USES_ID;
      pz2ServiceId = configuration.PAZPAR2_SERVICE_ID;
      logger.debug("Found ID for pazpar2 service: " + pz2ServiceId);
    } else {
      serviceDefinitionType = USES_PZ2_DEFAULT;
      logger.debug(
        "No XML or ID for pazpar2 service found, will use default service.");
    }
  }

  /**
   * Returns the chosen service defintion type (XML, ID or default)
   * @return Indicator for either "uses xml", "uses ID", "uses Pazpar2 default", or "undefined"
   */
  public int getServiceDefinitionType() {
    return serviceDefinitionType;
  }

  public boolean usesXml() {
    return serviceDefinitionType == USES_XML;
  }

  public boolean usesId() {
    return serviceDefinitionType == USES_ID;
  }

  public boolean usesPz2DefaultService() {
    return serviceDefinitionType == USES_PZ2_DEFAULT;
  }

  /**
   * Returns the identifier pointing to a statically loaded pazpar2 service definition
   * @return ID if service definition type is "uses ID", otherwise the empty String.
   */
  public String getServiceId() {
    if (serviceDefinitionType == USES_ID) {
      return pz2ServiceId;
    } else {
      return "";
    }
  }

  /**
   * Returns the XML content of the pazpar2 service definition as
   * read from a definition file
   * @return
   */
  public String getServiceXml() {
    return pz2ServiceXml;
  }

  /**
   * Reads the pazpar2 service definition from a XML file
   * @param serviceXmlFileName
   * @return The service XML
   * @throws ProxyErrorException
   */
  private String loadServiceXml(String serviceXmlFileName) throws
    ProxyErrorException {
    StringBuilder serviceXml = new StringBuilder();
    BufferedReader d = null;
    try {
      String line = null;
      d = new BufferedReader(new FileReader(new File(serviceXmlFileName)));
      while ((line = d.readLine()) != null) {
        serviceXml.append(line);
        serviceXml.append(System.getProperty("line.separator"));
      }
    } catch (IOException ioe) {
      logger.error("Could not read service definition XML for Pazpar2: " + ioe.
        getMessage());
      new ProxyErrorException("Error reading service definition XML",
        ProxyErrorException.ErrorCode.CONFIGURATION_ERROR);
    } finally {
      if (d != null) {
        try {
          d.close();
        } catch (IOException e) {
          logger.error("Error closing BufferedReader after reading service definition XML" + e.
            getMessage());
        }
      }
    }
    return serviceXml.toString();
  }

  /**
   * Returns description of the chosen service definition type (for log statements)
   * @return Description of service definition
   */
  public String getDescription() {
    if (serviceDefinitionType == USES_XML) {
      return " Pz2 service definitions in \"" + pz2ServiceXmlFileName
        + "\"";
    } else if (serviceDefinitionType == USES_ID) {
      return " Pz2 service id \"" + pz2ServiceId + "\"";
    } else {
      return " Default Pz2 service ";
    }
  }
}
