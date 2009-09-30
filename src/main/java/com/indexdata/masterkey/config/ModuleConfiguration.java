package com.indexdata.masterkey.config;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

public class ModuleConfiguration {

	private Logger logger = Logger.getLogger("com.indexdata.masterkey.config.");
	private MasterkeyConfiguration mkContext = null;
	private String moduleName = null;

	
	
	public ModuleConfiguration (MasterkeyConfiguration mkConfigContext, String moduleName) {
		mkContext = mkConfigContext;
		this.moduleName = moduleName;	
		try {
			if (getConfigMap().size()==0) {
				logger.warn("There are no properties in " + mkContext.getConfigFileLocation().getConfigFilePath() + " with prefix '" + moduleName + "'");
			}
		} catch (IOException e) {
			// will be thrown and caught later
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
    public Properties getConfigProperties () throws IOException {
    	return mkContext.getConfigParamsAsProperties(moduleName);
    }

    public String getContextKey () {
    	return mkContext.getContextKey();
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
    		return null;
    	}    	
    }

    /**
     * Gets a mandatory parameter value by name
     * @param name parameter key
     * @return the parameter value
     * @throws Exception if mandatory parameter was not found
     */
    public String getMandatory (String name) throws Exception {
    	try {
    		String value = mkContext.getConfigParameter(moduleName, name);    		
    		if (value == null || value.length()==0) {
    			logger.error("Mandatory parameter [" + name + "] not found");
    			throw new Exception("Mandatory parameter [" + name + "] not found");
    		} else {
    			return value;
    		}
    	} catch (IOException ioe) {
    		logger.error("Error reading config parameter [" + name + "]");
    		throw new Exception("Mandatory parameter [" + name + "] not found");    		
    	}    	    	
    }
    
    /**
     * Short-hand for checking for the existence of a parameter
     * @param name
     * @return true if the parameter name exists in the modules properties
     */
    public boolean containsKey (String name) {
    	try {
    		return mkContext.getConfigParamsAsMap(moduleName).containsKey(name);
    	} catch	(IOException ioe) {
    		logger.error("Error checking config param [" + name + "] for module ["+ moduleName + "]");
    		return false;
    	}    	
    }
}
