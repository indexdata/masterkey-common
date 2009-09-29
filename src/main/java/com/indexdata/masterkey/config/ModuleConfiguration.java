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
    public Map<String, String> getConfigParamsAsMap() throws IOException {
    	return mkContext.getConfigParamsAsMap(moduleName);
    }


    public Properties getConfigParamsAsProperties () throws IOException {
    	return mkContext.getConfigParamsAsProperties(moduleName);
    }

    public String getContextKey () {
    	return mkContext.getContextKey();
    }
}
