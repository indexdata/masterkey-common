package com.indexdata.masterkey.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletContext;
import org.apache.log4j.Logger;

/**
 * Structure to hold all elements of the path leading to the config file,
 * including method for troubleshooting.
 */
class ConfigFileLocation {

    public static String MASTERKEY_ROOT_CONFIG_DIR;
    public static final String MASTERKEY_ROOT_CONFIG_DIR_PARAM = "MASTERKEY_ROOT_CONFIG_DIR";
    public static final String MASTERKEY_COMPONENT_CONFIG_DIR_PARAM = "MASTERKEY_COMPONENT_CONFIG_DIR";
    public static final String MASTERKEY_CONFIG_FILE_NAME_PARAM = "MASTERKEY_CONFIG_FILE_NAME";
    public static final String DOMAIN_CONFIG_DIR_PROPERTY_NAME = "CONFIG_DIR";
    public static final String DOMAIN_CONFIG_FILE_POSTFIX = "_confd";
    private String componentDir = null;
    private String fileName = null;
    private String serverName = null;
    private String configDirForServerName = null;
    private String domainMappingFileName = null;
    Properties domainConfigMappingProperties = null;
    private static Logger logger = Logger.getLogger("com.indexdata.masterkey.config");

    /**
     *
     * @param context
     * @param serverName Used for resolving the config directory
     * @param configFileName optionally overriding the (base) name of the config file.
     * @throws javax.servlet.IOException If any of three basic context params are missing
     */
    public ConfigFileLocation(ServletContext context, String serverName)
            throws IOException {
        init(context, serverName,"");
    }
    public ConfigFileLocation(ServletContext context, String serverName, String configFileName)
            throws IOException {
        init(context, serverName,configFileName);
    }

    private void init(ServletContext context, String serverName, String configFileName) throws IOException {
        String rootConfigDir = context.getInitParameter(MASTERKEY_ROOT_CONFIG_DIR_PARAM);
        //look for sepcial paths that are relative to the servlet context
        if (rootConfigDir.startsWith("war://")) {
          MASTERKEY_ROOT_CONFIG_DIR = context.getRealPath(rootConfigDir.substring(6));
          logger.debug("MASTERKEY_ROOT_CONFIG is relative to servlet context, resolving as " + MASTERKEY_ROOT_CONFIG_DIR);
        } else {
          MASTERKEY_ROOT_CONFIG_DIR = rootConfigDir;
        }
        checkMandatoryParameter(MASTERKEY_ROOT_CONFIG_DIR_PARAM, MASTERKEY_ROOT_CONFIG_DIR);
        this.componentDir = context.getInitParameter(MASTERKEY_COMPONENT_CONFIG_DIR_PARAM);
        checkMandatoryParameter(MASTERKEY_COMPONENT_CONFIG_DIR_PARAM, componentDir);
        if ( (configFileName != null) && ( !configFileName.isEmpty()) )
            this.fileName = configFileName;
        else
            this.fileName = context.getInitParameter(MASTERKEY_CONFIG_FILE_NAME_PARAM);
        checkMandatoryParameter(MASTERKEY_CONFIG_FILE_NAME_PARAM, fileName);
        this.serverName = serverName;
        this.domainMappingFileName = serverName + DOMAIN_CONFIG_FILE_POSTFIX;
        this.configDirForServerName = getConfigDirForServerName(componentDir, domainMappingFileName);
    }
    /**
     * Gets the full path to the configuration file, excluding the file itself
     * @return String representing the path
     */
    String getConfigDir() {
        return MASTERKEY_ROOT_CONFIG_DIR + componentDir + "/conf.d" + configDirForServerName;
    }

    /**
     * Gets the root directory for the component, excluding the host specific subdirectory
     * @return
     */
    private String getComponentDir() {
        return MASTERKEY_ROOT_CONFIG_DIR + componentDir;
    }

    private String getComponentConfDDir() {
        return MASTERKEY_ROOT_CONFIG_DIR + componentDir + "/conf.d";
    }

    /**
     * Gets the full path to the configuration file, including the file name.
     * @return
     */
    public String getConfigFilePath() {
        return getConfigDir() + "/" + fileName;
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
            File rootDirFile = new File(MASTERKEY_ROOT_CONFIG_DIR);
            if (!rootDirFile.exists()) {
                logger.error("Masterkey root config directory not found: '" + MASTERKEY_ROOT_CONFIG_DIR + "'");
                throw new IOException("Masterkey root config directory not found: " + MASTERKEY_ROOT_CONFIG_DIR);
            } else {
                logger.info("Masterkey root config directory was found: '" + MASTERKEY_ROOT_CONFIG_DIR + "'");
            }
            File componentDirFile = new File(getComponentDir());
            if (!componentDirFile.exists()) {
                logger.error("Masterkey component config directory '" + componentDir + "' not found in '" + MASTERKEY_ROOT_CONFIG_DIR + "'. Please check web.xml and compare with file system.");
                throw new IOException("Masterkey component config directory '" + componentDir + "' not found in '" + MASTERKEY_COMPONENT_CONFIG_DIR_PARAM + "'");
            } else {
                logger.info("Masterkey component config directory was found: '" + getComponentDir() + "'");
            }
            File confdDirFile = new File(getComponentConfDDir());
            if (!confdDirFile.exists()) {
                logger.error("The component directory '" + getComponentDir() + "' must contain a directory named 'conf.d'");
                throw new IOException("Directory 'conf.d' was not found in masterkey component config directory '" + getComponentDir() + "'");
            } else {
                logger.info("Masterkey component 'conf.d' directory was found: '" + getComponentConfDDir() + "'");
            }
            File configDirFile = new File(getConfigDir());
            if (!configDirFile.exists()) {
                if (configDirForServerName != null || configDirForServerName.length() > 0) {
                    logger.error("The directory '" + configDirForServerName + "' was not found in '" + getComponentConfDDir() + " '" + configDirForServerName + "' is configured for '" + serverName + "' in '" + getComponentDir() + "/" + serverName + "'" + " Please check the file '" + serverName + "' and compare with the file system.");
                    throw new IOException("Directory '" + configDirForServerName + "' was not found in '" + getComponentConfDDir());
                } else {
                    logger.warn("Configuration directory not resolved for host name " + serverName);

                }
            } else {
                logger.info("Masterkey config directory was found: '" + getConfigDir() + "'");
            }
            logger.error("Configuration file '" + fileName + "' was not found in '" + getConfigDir() + ". Please check web.xml and the filesystem.");
            throw new IOException("Configuration file '" + fileName + "' was not found in '" + getConfigDir());
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
            	FileInputStream fis = new FileInputStream(MASTERKEY_ROOT_CONFIG_DIR + componentConfigDirectory + "/" + domainMappingFileName);
                domainConfigMappingProperties.load(fis);
                fis.close();
            } catch (IOException ioe) {
            	domainConfigMappingProperties = null;
                logger.warn(ioe + "Could not load domain-to-config mapping file " + MASTERKEY_ROOT_CONFIG_DIR + componentConfigDirectory + "/" + domainMappingFileName + ".");
            }
        }
        return domainConfigMappingProperties;
    }

    /**
     * Convenience method for checking the existence of a given init parameter in web.xml
     * @param name
     * @param value
     */
    private void checkMandatoryParameter(String name, String value) throws IOException {
        if (value == null || value.length() == 0 || value.equals("null")) {
            logger.error("Init parameter " + name + " missing in deployment descriptor (web.xml)");
            throw new IOException("Init parameter " + name + " missing in deployment descriptor");
        }
    }
}
