package com.indexdata.masterkey.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;

import javax.servlet.ServletContext;
import org.apache.log4j.Logger;

/**
 * Structure to hold all elements of the path leading to the config file,
 * including method for troubleshooting.
 */
public class ConfigFileLocation implements Serializable {
    private static Logger logger = Logger.getLogger(ConfigFileLocation.class);

  private static final long serialVersionUID = 950891180639044889L;
    public static final String DOMAIN_CONFIG_DIR_PROPERTY_NAME = "CONFIG_DIR";

    private final ConfigFileBase cfb;
    private final String serverName;
    private final String configDirForServerName;
    private final String domainMappingFileName;
    Properties domainConfigMappingProperties = null;


    public ConfigFileLocation(ConfigFileBase cfb, String serverName) throws IOException {
        this.cfb = cfb;
        this.serverName = serverName;
        this.domainMappingFileName = serverName + ConfigFileBase.DOMAIN_CONFIG_FILE_POSTFIX;
        this.configDirForServerName = getConfigDirForServerName(cfb.getComponentDir(), domainMappingFileName);
    }

    /**
     * Gets the full path to the configuration file, excluding the file itself
     * @return String representing the path
     */
    String getConfigDir() {
        return cfb.getComponentDirPath() + "/conf.d" + configDirForServerName;
    }

    /**
     * Gets the full path to the configuration file, including the file name.
     * @return
     */
    public String getConfigFilePath() {
        return getConfigDir() + "/" + cfb.getFileName();
    }
    
    public ConfigFileBase getConfigFileBase() {
      return cfb;
    }

    /**
     * Checks if the config file exists in the calculated path.
     * If not, analyzes what is wrong in the path and throws an exception.
     * Will assume the basic context params exist in web.xml (was tested in
     * the constructor)
     * @throws javax.servlet.IOException
     */
    public void evaluate() throws IOException {
        File configFile = new File(getConfigFilePath());
        if (!configFile.exists()) {
            logger.fatal("Masterkey configuration file not found at: '" + getConfigFilePath() + "'. Will troubleshoot. See the following log statements.");
            File rootDirFile = new File(cfb.getMasterkeyRootConfigDir());
            if (!rootDirFile.exists()) {
                logger.error("Masterkey root config directory not found: '" + cfb.getMasterkeyRootConfigDir() + "'");
                throw new IOException("Masterkey root config directory not found: " + cfb.getMasterkeyRootConfigDir());
            } else {
                logger.info("Masterkey root config directory was found: '" + cfb.getMasterkeyRootConfigDir() + "'");
            }
            File componentDirFile = new File(cfb.getComponentDirPath());
            if (!componentDirFile.exists()) {
                logger.error("Masterkey component config directory '" + cfb.getComponentDir() + "' not found in '" + cfb.getMasterkeyRootConfigDir() + "'. Please check web.xml and compare with file system.");
                throw new IOException("Masterkey component config directory '" + cfb.getComponentDir() + "' not found in '" + cfb.getMasterkeyRootConfigDir() + "'");
            } else {
                logger.info("Masterkey component config directory was found: '" + cfb.getComponentDirPath() + "'");
            }
            File confdDirFile = new File(cfb.getComponentConfDDirPath());
            if (!confdDirFile.exists()) {
                logger.error("The component directory '" + cfb.getComponentDir() + "' must contain a directory named 'conf.d'");
                throw new IOException("Directory 'conf.d' was not found in masterkey component config directory '" + cfb.getComponentDir() + "'");
            } else {
                logger.info("Masterkey component 'conf.d' directory was found: '" + cfb.getComponentConfDDirPath() + "'");
            }
            File configDirFile = new File(getConfigDir());
            if (!configDirFile.exists()) {
                if (configDirForServerName != null || configDirForServerName.length() > 0) {
                    logger.error("The directory '" + configDirForServerName 
                      + "' was not found in '" + cfb.getComponentConfDDirPath() 
                      + " '" + configDirForServerName + "' is configured for '" 
                      + serverName + "' in '" + cfb.getComponentDirPath() + "/" 
                      + serverName + "'" + " Please check the file '" 
                      + serverName + "' and compare with the file system.");
                    throw new IOException("Directory '" + configDirForServerName 
                      + "' was not found in '" + cfb.getComponentConfDDirPath());
                } else {
                    logger.warn("Configuration directory not resolved for host name " 
                      + serverName);
                }
            } else {
                logger.info("Masterkey config directory was found: '" 
                  + getConfigDir() + "'");
            }
            logger.error("Configuration file '" + cfb.getFileName() 
              + "' was not found in '" + getConfigDir() + ". Please check web.xml and the filesystem.");
            throw new IOException("Configuration file '" 
              + cfb.getFileName() + "' was not found in '" + getConfigDir());
        }
    }

    /**
     * Derives a path to the config file, based on the host name from which
     * the component was invoked.
     * @param rootConfigDirectory
     * @param componentConfigDirectory
     * @param domainMappingFileName
     * @return
     */
    private String getConfigDirForServerName(String componentConfigDirectory, String domainMappingFileName) {
        String configDir = "";
        Properties prop = getDomainConfigMappingProperties(componentConfigDirectory, domainMappingFileName);
        if (prop != null) {
            configDir = (String) prop.get(DOMAIN_CONFIG_DIR_PROPERTY_NAME);
            if (configDir == null || configDir.length() == 0) {
                configDir = "";
                logger.warn("Property " + DOMAIN_CONFIG_DIR_PROPERTY_NAME + " not found in " + domainMappingFileName);
                logger.warn("Could not find a config directory for host name \'" + serverName + "\' - will assume configuration file is in 'conf.d'.");
            } else {
                configDir = "/" + configDir;
                logger.info("Found config directory name for host name \'" + serverName + "\': \'" + configDir + "\' under: " + componentConfigDirectory);
            }
        } else {
            logger.warn("Could not find a config directory name for host name \'" + serverName + "\' - will assume configuration file is in 'conf.d'.");
        }
        return configDir;
    }

    /**
     * Looks up the property file containing mapping by host names to config directories.
     * If no property file is found the method will log a warning and MasterkeyConfiguration will
     * look in the 'default' directory for the configuration for this host.
     * @param rootConfigDirectory
     * @param componentConfigDirectory
     * @return
     */
    private Properties getDomainConfigMappingProperties(String componentConfigDirectory, String domainMappingFileName) {
        if (domainConfigMappingProperties == null) {
            domainConfigMappingProperties = new Properties();
            try {
            	FileInputStream fis = new FileInputStream(cfb.getMasterkeyRootConfigDir() + componentConfigDirectory + "/" + domainMappingFileName);
                domainConfigMappingProperties.load(fis);
                fis.close();
            } catch (IOException ioe) {
            	domainConfigMappingProperties = null;
                logger.warn(ioe + "Could not load domain-to-config mapping file " + cfb.getMasterkeyRootConfigDir() + componentConfigDirectory + "/" + domainMappingFileName + ".");
            }
        }
        return domainConfigMappingProperties;
    }


}
