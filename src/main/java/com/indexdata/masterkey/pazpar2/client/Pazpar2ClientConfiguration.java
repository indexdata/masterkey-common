package com.indexdata.masterkey.pazpar2.client;

import java.io.Serializable;

import org.apache.log4j.Logger;

import com.indexdata.masterkey.config.MissingMandatoryParameterException;
import com.indexdata.masterkey.config.ModuleConfigurationGetter;
import com.indexdata.masterkey.pazpar2.client.exceptions.ProxyErrorException;

/**
 * Data object representing all configuration parameters for a Pazpar2 proxy client.
 * 
 * Basically repeats the Pazpar2RelayPlugin properties but in way that it can be 
 * cached and reused by other plug-ins. 
 *  
 * @author Niels Erik
 *
 */
public class Pazpar2ClientConfiguration implements Serializable {
  private static final long serialVersionUID = -656354982746551862L;
  public int PROXY_MODE = 0;
  public String TORUS_BASEURL = null;
  public String USE_TURBO_MARC = null;
  public String TORUS_PARAMS = null;
  public String TORUS_REALM = null;
  public String PAZPAR2_URL = null;
  public int STREAMBUFF_SIZE = 0;
  public String PAZPAR2_SERVICE_ID = null;
  public String PAZPAR2_SERVICE_XML = null;
  public String PAZPAR2_SETTINGS_XML = null;
  public String XML_FILE_PATH = null;
  public String USE_OPAQUE_ID;
  public String CF_ENGINE_ADDRESS = "";
  
  //map values
  public final String CCLMAP_TERM;
  public final String CCLMAP_SU;
  public final String CCLMAP_AU;
  public final String CCLMAP_TI;
  public final String CCLMAP_ISBN;
  public final String CCLMAP_ISSN;
  public final String CCLMAP_JT;
  public final String CCLMAP_DATE;
  //map names
  public final String CCLMAP_TERM_KEY;
  public final String CCLMAP_SU_KEY;
  public final String CCLMAP_AU_KEY;
  public final String CCLMAP_TI_KEY;
  public final String CCLMAP_ISBN_KEY;
  public final String CCLMAP_ISSN_KEY;
  public final String CCLMAP_JT_KEY;
  public final String CCLMAP_DATE_KEY;
  // the following are fallbacks specified when target profile info is missing
  // they can be override using config file
  private static final String CCLMAP_TERM_FB = "u=1016 t=l,r s=al";
  private static final String CCLMAP_SU_FB = "u=21 s=al";
  private static final String CCLMAP_AU_FB = "u=1003 s=al";
  private static final String CCLMAP_TI_FB = "u=4 s=al";
  private static final String CCLMAP_ISBN_FB = "u=7";
  private static final String CCLMAP_ISSN_FB = "u=8";
  private static final String CCLMAP_JT_FB = "u=1033 s=al";
  private static final String CCLMAP_DATE_FB = "u=30 r=r";
  //the following are customizable prefixes for the field names
  private static final String CCLMAP_TERM_KEY_FB = "term";
  private static final String CCLMAP_SU_KEY_FB = "su";
  private static final String CCLMAP_AU_KEY_FB = "au";
  private static final String CCLMAP_TI_KEY_FB = "ti";
  private static final String CCLMAP_ISBN_KEY_FB = "isbn";
  private static final String CCLMAP_ISSN_KEY_FB = "issn";
  private static final String CCLMAP_JT_KEY_FB = "jt";
  private static final String CCLMAP_DATE_KEY_FB = "date";
  
  private static Logger logger = Logger.getLogger(Pazpar2ClientConfiguration.class);


  public Pazpar2ClientConfiguration(ModuleConfigurationGetter cfg) throws ProxyErrorException {
    if (cfg != null) {
      try {
        PAZPAR2_URL = cfg.getMandatory("PAZPAR2_URL");
        PROXY_MODE = Integer.parseInt(cfg.getMandatory("PROXY_MODE"));
        STREAMBUFF_SIZE = Integer.parseInt(cfg.getMandatory("STREAMBUFF_SIZE"));
        if (PROXY_MODE == 2) {
          TORUS_REALM = cfg.getMandatory("TORUS_REALM");
        } else {
          TORUS_REALM = cfg.get("TORUS_REALM");
        }
        if (PROXY_MODE == 2 || PROXY_MODE == 3) {
          TORUS_BASEURL = cfg.getMandatory("TORUS_BASEURL");
        } else {
          TORUS_BASEURL = cfg.get("TORUS_BASEURL"); //load the configuration, nevertheless
        }
        TORUS_PARAMS = cfg.get("TORUS_PARAMS");
        USE_TURBO_MARC = cfg.get("USE_TURBO_MARC");
        PAZPAR2_SERVICE_ID = cfg.get("PAZPAR2_SERVICE_ID");
        PAZPAR2_SERVICE_XML = cfg.get("PAZPAR2_SERVICE_XML");
        PAZPAR2_SETTINGS_XML = cfg.get("PAZPAR2_SETTINGS_XML");
        XML_FILE_PATH = cfg.getConfigFilePath();
        USE_OPAQUE_ID = cfg.get("USE_OPAQUE_ID", "no");
        CF_ENGINE_ADDRESS = cfg.get("CF_ENGINE_ADDRESS");

        CCLMAP_TERM = cfg.get("CCLMAP_TERM", CCLMAP_TERM_FB);
        CCLMAP_SU = cfg.get("CCLMAP_SU", CCLMAP_SU_FB);
        CCLMAP_AU = cfg.get("CCLMAP_AU", CCLMAP_AU_FB);
        CCLMAP_TI = cfg.get("CCLMAP_TI", CCLMAP_TI_FB);
        CCLMAP_ISBN = cfg.get("CCLMAP_ISBN", CCLMAP_ISBN_FB);
        CCLMAP_ISSN = cfg.get("CCLMAP_ISSN", CCLMAP_ISSN_FB);
        CCLMAP_JT = cfg.get("CCLMAP_JT", CCLMAP_JT_FB);
        CCLMAP_DATE = cfg.get("CCLMAP_DATE", CCLMAP_DATE_FB);
        
        CCLMAP_TERM_KEY = cfg.get("CCLMAP_TERM_KEY", CCLMAP_TERM_KEY_FB);
        CCLMAP_SU_KEY = cfg.get("CCLMAP_SU_KEY", CCLMAP_SU_KEY_FB);
        CCLMAP_AU_KEY = cfg.get("CCLMAP_AU_KEY", CCLMAP_AU_KEY_FB);
        CCLMAP_TI_KEY = cfg.get("CCLMAP_TI_KEY", CCLMAP_TI_KEY_FB);
        CCLMAP_ISBN_KEY = cfg.get("CCLMAP_ISBN_KEY", CCLMAP_ISBN_KEY_FB);
        CCLMAP_ISSN_KEY = cfg.get("CCLMAP_ISSN_KEY", CCLMAP_ISSN_KEY_FB);
        CCLMAP_JT_KEY = cfg.get("CCLMAP_JT_KEY", CCLMAP_JT_KEY_FB);
        CCLMAP_DATE_KEY = cfg.get("CCLMAP_DATE_KEY", CCLMAP_DATE_KEY_FB);
      } catch (NumberFormatException e) {
        throw new ProxyErrorException(
          "Error creating configuration for pazpar2 proxy: " + e.getMessage(), 
          ProxyErrorException.ErrorCode.CONFIGURATION_ERROR);
      } catch (MissingMandatoryParameterException e) {
        throw new ProxyErrorException(
          "Error creating configuration for pazpar2 proxy: " +e.getMessage(), 
          ProxyErrorException.ErrorCode.CONFIGURATION_ERROR);
      }
    // this else should GO once we have some mock configuration classes
    // that can be initialized from a hash map
    } else {
      logger.error("ModuleConfiguration is null, the plugin will behave unstable");
      
      USE_OPAQUE_ID = "yes";
      
      CCLMAP_TERM = CCLMAP_TERM_FB;
      CCLMAP_SU = CCLMAP_SU_FB;
      CCLMAP_AU = CCLMAP_AU_FB;
      CCLMAP_TI = CCLMAP_TI_FB;
      CCLMAP_ISBN = CCLMAP_ISBN_FB;
      CCLMAP_ISSN = CCLMAP_ISSN_FB;
      CCLMAP_JT = CCLMAP_JT_FB;
      CCLMAP_DATE = CCLMAP_DATE_FB;
      
      CCLMAP_TERM_KEY = CCLMAP_TERM_KEY_FB;
      CCLMAP_SU_KEY = CCLMAP_SU_KEY_FB;
      CCLMAP_AU_KEY = CCLMAP_AU_KEY_FB;
      CCLMAP_TI_KEY = CCLMAP_TI_KEY_FB;
      CCLMAP_ISBN_KEY = CCLMAP_ISBN_KEY_FB;
      CCLMAP_ISSN_KEY = CCLMAP_ISSN_KEY_FB;
      CCLMAP_JT_KEY = CCLMAP_JT_KEY_FB;
      CCLMAP_DATE_KEY = CCLMAP_DATE_KEY_FB;
    }
  }
}
