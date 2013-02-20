package com.indexdata.masterkey.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.ServletContext;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.indexdata.utils.PerformanceLogger;

/**
 * Represents the configuration context for modules within this J2EE component.
 * 
 * An instance of this class basically has a one-to-one relationship with the
 * properties file for the component/vhost
 * 
 * From this context it is possible to obtain configurations for sub-modules
 * within the component. The configuration of a sub-module would be those
 * properties in the components properties file that are prefixed with a given
 * module name.
 *
 * It is possible to force the file name (but not the path to it) to be
 * something data-dependent.
 *
 * 'Component': Basically a .war file
 * 'Module': Sub-functions within the component, i.e. a Servlet, a REST service, a plug-in, etc.
 * 
 * Throws ServletException 
 *    If the configuration file (property file) is not found
 *
 * @author Niels Erik
 */
public class MasterkeyConfiguration implements Serializable {

  private static final long serialVersionUID = -8887051580796525503L;

    public static final String MASTERKEY_CONFIG_LIFE_TIME_PARAM = "MASTERKEY_CONFIG_LIFE_TIME";

    private static Logger logger = Logger.getLogger(MasterkeyConfiguration.class);
    private static ConcurrentHashMap<String,MasterkeyConfiguration> configLocationCache = new ConcurrentHashMap<String, MasterkeyConfiguration>();    
    private boolean cacheConfigParams = true;           
    private String contextKey = null;
    private String hostName = null;

    private ConcurrentHashMap<String,Properties> configParametersCache = new ConcurrentHashMap<String, Properties>();
    private ConfigFileLocation configFileLocation = null;

    private MasterkeyConfiguration(ServletContext servletContext, String appId,
                String hostName, String configFilename)
            throws IOException {
        initMasterkeyConfiguration(servletContext, appId, hostName, configFilename);

    }
    private void initMasterkeyConfiguration(ServletContext servletContext,
                String appId, String hostName, String configFileName)
            throws IOException {
    	contextKey = appId + "@"+hostName;
    	this.hostName = hostName;
        cacheConfigParams = areConfigParamsCached(servletContext.getInitParameter(MASTERKEY_CONFIG_LIFE_TIME_PARAM));
        configFileLocation = new ConfigFileLocation(servletContext, hostName, configFileName);
    }

    private Logger getLogger() {
        return logger;
    }
    
    /**
     * Creates a singleton MasterkeyConfiguration for each combination of component name and host name.
     * 
     * @param servletContext Needed to pick up init parameters regarding the location of config files
     * @param hostName Used for resolving the path to config files.
     */
    public static MasterkeyConfiguration getInstance (ServletContext servletContext, String appId, String hostName) throws IOException {
        return init(servletContext, appId, hostName, "");
    }
    
    /**
     * Creates a singleton MasterkeyConfiguration for each combination of component name, host name, and property file name.
     * <br/>
     * Provides support for a special case scenario where some feature is deployed separately from the .war file of the
     * component. It allows for a deploy script (ie Debian) to <i>add</i>  a new property file for the new feature rather than 
     * updating a sub-section within the already existing configuration file for the .war file.
     * <br/>
     * Was specifically implemented for the Torus, which can have additional data stores deployed after the .war file.   
     * 
     * @param servletContext Needed to pick up init parameters regarding the location of config files
     * @param hostName       Used for resolving the path to config files.
     * @param configFileName Defined by components code.
     */
    public static MasterkeyConfiguration getInstance (ServletContext servletContext, String appId, String hostName, String configFileName) throws IOException {
        return init(servletContext, appId, hostName, configFileName);

    }

    /**
     * Creates a singleton MasterkeyConfiguration for each combination of component name and host name (and possibly config file name).
	 */
    private static MasterkeyConfiguration init (ServletContext servletContext, String appId, String hostName, String configFileName) throws IOException {
        MasterkeyConfiguration cfg = null;
        if (configFileName == null)
            configFileName = "";
        String cfgKey = appId + "/" + configFileName + "@"+hostName;
        if (configLocationCache.containsKey(cfgKey)) {
            cfg = (MasterkeyConfiguration) (configLocationCache.get(cfgKey));
            cfg.getLogger().debug("Returning cached config location for '" + cfgKey + "': '" + cfg.getConfigFileLocation().getConfigFilePath() + "'");
        } else {            
            cfg = new MasterkeyConfiguration(servletContext, appId, hostName, configFileName);
            cfg.getLogger().debug("No previously cached config location reference found for '" + cfgKey + "'. Instantiating a new config location reference: '" + cfg.getConfigFileLocation().getConfigFilePath() + "'");
            // Check that config file is readable, if not, analyze and throw exception
            cfg.getConfigFileLocation().evaluate();
            // The file location passed tests, store it
            configLocationCache.put(cfgKey, cfg);
        }
        return cfg;
    }
    
    /**
     * Provides a unique key for this configuration context (unique per j2ee component and vhost)
     *  
     * @return Unique key for the context within which given sub-modules are executed.
     */
    public String getContextKey () {
    	return contextKey;
    }
    
    /**
     * Creates a ModuleConfiguration holding the subset of properties that applies to the given module
     * 
     * @param moduleName Must match the prefix of the properties to pick up
     * @return
     * @throws IOException
     */
    public ModuleConfiguration getModuleConfiguration (String moduleName) throws IOException {    	
    	return new ModuleConfiguration(this, moduleName);
    }
    
    public boolean areConfigParamsCached () {
    	return cacheConfigParams;
    }
    
    /**
     * Sets the life time of configuration parameters to 'servlet' (cache=true) or 'request' (cache=false
     * @param configLifeTime
     * @return
     * @throws javax.servlet.ServletException
     */
    private boolean areConfigParamsCached(String configLifeTime) {
        boolean setting = true;
        if (configLifeTime == null || configLifeTime.length() == 0) {
            logger.warn(MASTERKEY_CONFIG_LIFE_TIME_PARAM + " init parameter not defined in deployment descriptor. Can be 'REQUEST' or 'SERVLET'. Defaulting to 'SERVLET'.");
        } else {
            if (configLifeTime.equalsIgnoreCase("REQUEST")) {
                setting = false;
            } else {
                if (!configLifeTime.equalsIgnoreCase("SERVLET")) {
                    logger.warn(MASTERKEY_CONFIG_LIFE_TIME_PARAM + " init parameter can be one of 'REQUEST' or 'SERVLET'. Was '" + configLifeTime + "'. Defaulting to 'SERVLET'.");
                }
            }
        }
        return setting;
    }


    /**
     * Retrieves all config parameter names for the module
     * @return
     * @throws javax.servlet.ServletException
     */
    public Enumeration<String> getConfigParameterNames(String prefix) throws IOException {
        Properties prop = getComponentProperties(configFileLocation.getConfigFilePath());
        Hashtable<String, String> keyList = new Hashtable<String, String>();
        Iterator<Object> keysIter = prop.keySet().iterator();
        int i = 0;
        while (keysIter.hasNext()) {
            String key = (String) keysIter.next();
            if (key.startsWith(prefix)) {
                key = key.replace(prefix + ".", "");
                keyList.put("" + (i++), key);
            }
        }
        return keyList.elements();
    }
        
    /**
     * Retrieves a given config parameter by name and module/prefix.
     * @param name
     * @return
     * @throws javax.servlet.ServletException
     */
    public String getConfigParameter(String prefix, String name) throws IOException {
    	logger.debug("Looking for key " + name + " in module " + prefix);
        Properties prop = getComponentProperties(configFileLocation.getConfigFilePath());
        String propertyValue = ((String) prop.get(prefix + "." + name))+"";
        propertyValue = dereference(prop, propertyValue);
        if (propertyValue == null || propertyValue.length() == 0 || propertyValue.equals("null")) {
            logger.info("Could not find value for key '" + name + "'");
            propertyValue = "";
        } else {
        	propertyValue = propertyValue.trim();
            logger.debug("Found value '" + propertyValue + "' for key '" + name + "' in module " + prefix);
            propertyValue = substituteVariables(propertyValue);
        }
        return propertyValue;
    }
    
    private String dereference (Properties props, String propertyValue) throws IOException {
    	if (propertyValue.startsWith("REF:")) {
        	String[] referenceValue = propertyValue.split(":",2);
        	String[] referenceName = referenceValue[1].split("\\.",2);
        	propertyValue = ((String) props.get(referenceName[0] + "." + referenceName[1]))+""; 
        	if (propertyValue.startsWith("REF:")) {
        		logger.error("Recursive config parameter references detected for " + propertyValue);
        		propertyValue = null;
        	}
    	}
    	return propertyValue;
    }
    
    private String substituteVariables(String propertyValue) {
    	String returnStr = propertyValue;
    	returnStr = propertyValue.replace("${thisHost}", getHostName());
    	if (logger.isDebugEnabled()) {
    		if (!propertyValue.equals(returnStr)) {
    			logger.debug("Config parameter after substitution: " + returnStr);
    		}
    	}
    	return returnStr;
    }
       
    /**
     * Retrieves all config parameters for a module/prefix as a HashMap. 
     *
     * @return
     * @throws javax.servlet.ServletException
     */
    public Map<String, String> getConfigParamsAsMap(String prefix) throws IOException {
        Map<String, String> paramMap = new HashMap<String, String>();
        Enumeration<String> paramNames = getConfigParameterNames(prefix);
        while (paramNames.hasMoreElements()) {
            String paramName = (String) paramNames.nextElement();
            paramMap.put(paramName, getConfigParameter(prefix, paramName));
        }
        return paramMap;
    }
    
    public ConfigFileLocation getConfigFileLocation () {
        return configFileLocation;
    }
    
    /**
     * Host name intended for use in logging.
     * 
     * @return Name of the (virtual) host the request is made to
     */
    public String getHostName() {
    	return hostName;
    }
    
    /**
     * Retrieves properties (configuration) for a given J2EE module
     * Will cache the properties if so specified in the deployment descriptor
     * @param configFilePath
     * @return
     * @throws javax.servlet.ServletException
     */
    private Properties getComponentProperties(String configFilePath) throws IOException {
        Properties componentProperties = null;
        long startTime = PerformanceLogger.start();        
        if (cacheConfigParams && configParametersCache.containsKey(configFilePath)) {
            componentProperties = configParametersCache.get(configFilePath);
            logger.trace("Found cached properties for '" + configFilePath + "'");
            PerformanceLogger.finish(Level.TRACE, "CONFIG", "Found cached component properties", startTime);
        } else {
            componentProperties = new Properties();
            try {
            	FileInputStream fis = new FileInputStream(configFileLocation.getConfigFilePath());
                componentProperties.load(fis);
                fis.close();
                logger.trace("Loaded properties from file system using '" + configFilePath + "'");
            } catch (FileNotFoundException fnfe) {
                logger.error(fnfe + "Could not find property file '" + configFilePath + "'");
                configFileLocation.evaluate();
            } catch (IOException ioe) {
                logger.error(ioe + "Could not load property file '" + configFilePath + "'");
                throw new IOException("Could not load property file '" + configFilePath + "'" + ioe.getMessage());
            }
            if (cacheConfigParams) {
               configParametersCache.put(configFilePath, componentProperties);
            }
            PerformanceLogger.finish(Level.TRACE,"CONFIG", "Loaded component properties", startTime);
        }
        
        return componentProperties;
    }

    /**
     * Returns the config parameters as Properties for a module/prefix
     * @return
     * @throws IOException
     */
    public Properties getConfigParamsAsProperties (String prefix) throws IOException {
    	Properties moduleProps = new Properties();
    	Properties componentProps = getComponentProperties(configFileLocation.getConfigFilePath());
    	for (String key : componentProps.stringPropertyNames()) {
    		if (key.startsWith(prefix + ".")) {
    			moduleProps.put(key.replaceFirst(prefix+".", ""), componentProps.get(key));
    		}    		
    	}
    	return moduleProps;
    }
    
    public static ModuleConfiguration getModuleConfiguration (ServletContext servletContext, String hostName, String appId, String moduleName) throws IOException {
      MasterkeyConfiguration context = getInstance (servletContext, appId, hostName);
      return context.getModuleConfiguration(moduleName);
    }

    
}
