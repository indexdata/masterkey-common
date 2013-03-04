package com.indexdata.masterkey.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class HardcodedModuleConfiguration implements  ModuleConfigurationGetter {

  Logger logger = Logger.getLogger(this.getClass());
  Map<String, String> hardcodedMap = new HashMap<String, String>();
  ModuleConfiguration moduleConfig; 
  
  public HardcodedModuleConfiguration(ModuleConfiguration moduleConfig) {
    this.moduleConfig = moduleConfig;
  }

  public void set(String name, String value) {
    hardcodedMap.put(name, value);
  }

  public String get(String name) {
    if (moduleConfig.get(name) != null)
      return moduleConfig.get(name);
    
    if (hardcodedMap.containsKey(name)) {
      logger.warn("Using hardcoded default for " + name + 
	  ". Please update your plugin configuration for module " + moduleConfig.moduleName + 
	  " with " + name +" = "+ hardcodedMap.get(name) + " to avoid this warning");
      return hardcodedMap.get(name);
    }
    return null; 
  }

  public String get(String name, String defaultValue) {
    if (moduleConfig.get(name) != null)
      return moduleConfig.get(name);
    
    if (hardcodedMap.containsKey(name)) {
      logger.warn("Using hardcoded default for " + name + 
	  ". Please update your plugin configuration for module " + moduleConfig.moduleName + 
	  " with " + name +" = "+ hardcodedMap.get(name) + " to avoid this warning");
      return hardcodedMap.get(name);
    }
    return defaultValue; 
  }
  
  public String getMandatory(String name) throws MissingMandatoryParameterException {
    return moduleConfig.getMandatory(name);
  }

  @Override
  public String getConfigFilePath() {
    return null;    
  }
  
  

}
