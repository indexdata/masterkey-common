package com.indexdata.masterkey.config;

public interface ModuleConfigurationGetter {
  
  String get(String value);
  String get(String value, String defaultValue);
  String getMandatory(String name) throws MissingMandatoryParameterException;
  String getConfigFilePath ();

}
