package com.indexdata.masterkey.config;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

public class ModuleConfiguration implements Serializable {

  private static final long serialVersionUID = -7649779517458494774L;
    private static Logger logger = Logger.getLogger(ModuleConfiguration.class);
    private MasterkeyConfiguration mkContext = null;
    public String moduleName = null;

    public ModuleConfiguration(MasterkeyConfiguration mkConfigContext, String moduleName) {
        mkContext = mkConfigContext;
        this.moduleName = moduleName;
        try {
            if (getConfigMap().size() == 0) {
                logger.warn("There are no properties in " + mkContext.getConfigFileLocation().getConfigFilePath() + " with prefix '" + moduleName + "'");
            } else {
                logger.info(moduleName + " config: " + this.toString());
            }
        } catch (IOException e) {
            logger.warn(e.getMessage());
        }
    }

    /**
     * Retrieves a config parameter prefixed with a given module name (ie a servlet name) 
     * @param name  The name of the parameter, without the service name prefix.
     * @return
     * @throws javax.servlet.ServletException
     */
    public String getConfigParameter(String name) throws IOException {
        return mkContext.getConfigParameter(moduleName, name);
    }

    /**
     * Retrieves all the parameters for the module as a HashMap. 
     *
     * @return
     * @throws javax.servlet.ServletException
     */
    public Map<String, String> getConfigMap() throws IOException {
        return mkContext.getConfigParamsAsMap(moduleName);
    }

    /**
     * Retrieves all parameters for the module as a Properties
     * @return
     * @throws IOException
     */
    public Properties getConfigProperties() throws IOException {
        return mkContext.getConfigParamsAsProperties(moduleName);
    }

    public String getContextKey() {
        return mkContext.getContextKey();
    }

    /**
     * Retrieves the location of this components property file.
     * Allows the module to keep and retrieve additional configuration 
     * files from there - i.e. a Pazpar2 service definition XML file
     *  
     * @return Path to the config file directory
     */
    public String getConfigFilePath () {
    	return mkContext.getConfigFileLocation().getConfigDir();
    }
    
    /**
     * Host name intended for logging.
     * 
     * @return Name of (virtual) host to which the HTTP request was made.
     */
    public String getHostName () {
    	return mkContext.getHostName();
    }

    /**
     * Short-hand for getting a parameter value by name
     * @param name
     * @return
     */
    public String get(String name) {
        try {
            return mkContext.getConfigParameter(moduleName, name);
        } catch (IOException ioe) {
            logger.error("Error reading config parameter [" + name + "]");
            return "";
        }
    }
    
    /**
     * Short-hand for getting a parameter value by name, while returning
     * the provided defaultValue if the parameter is not found
     * @param name
     * @param defaultValue
     * @return The value of the parameter, or defaultValue if not found
     */
    public String get (String name, String defaultValue) {
    	String value = "";
        try {
            value = mkContext.getConfigParameter(moduleName, name);
        } catch (IOException ioe) {
            logger.error("Error reading config parameter [" + name + "]");
            value = "";
        }
    	if (value.length()==0) {
    		logger.debug("Config parameter ["+name+"] not found. Using default value [" + defaultValue + "]");
    		value = defaultValue;
    	}
    	return value;
    }

    /**
     * Gets a mandatory parameter value by name
     * @param name parameter key
     * @return the parameter value
     * @throws Exception if mandatory parameter was not found
     */
    public String getMandatory(String name) throws MissingMandatoryParameterException {
        try {
            String value = mkContext.getConfigParameter(moduleName, name);
            if (value == null || value.length() == 0) {
                logger.error("Mandatory parameter [" + name + "] not found");
                throw new MissingMandatoryParameterException("Mandatory parameter [" + name + "] not found for module [" + moduleName + "]");
            } else {
                return value;
            }
        } catch (IOException ioe) {
            logger.error("Error reading config parameter [" + name + "]");
            throw new MissingMandatoryParameterException("Mandatory parameter [" + name + "] not found for module [" + moduleName + "]");
        }
    }

    /**
     * Short-hand for checking for the existence of a parameter
     * @param name
     * @return true if the parameter name exists in the modules properties
     */
    public boolean hasParameter(String name) {
        try {
            return mkContext.getConfigParamsAsMap(moduleName).containsKey(name);
        } catch (IOException ioe) {
            logger.error("Error checking config param [" + name + "] for module [" + moduleName + "]");
            return false;
        }
    }

    @Override
    public String toString() {
        try {
            return getConfigProperties().toString();
        } catch (IOException e) {
            logger.error("toString() could not read the config properties.");
            return "Error reading properties";
        }
    }

}
