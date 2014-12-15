/*
 * Copyright (c) 1995-2014, Index Data
 * All rights reserved.
 * See the file LICENSE for details.
 */

package com.indexdata.masterkey.config;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.servlet.ServletContext;
import org.apache.log4j.Logger;

/**
 *
 * @author jakub
 */
public class ConfigFileBase {
  public static final String MASTERKEY_ROOT_CONFIG_DIR_PARAM = "MASTERKEY_ROOT_CONFIG_DIR";
  public static final String MASTERKEY_COMPONENT_CONFIG_DIR_PARAM = "MASTERKEY_COMPONENT_CONFIG_DIR";
  public static final String MASTERKEY_CONFIG_FILE_NAME_PARAM = "MASTERKEY_CONFIG_FILE_NAME";
  public static final String DOMAIN_CONFIG_FILE_POSTFIX = "_confd";
  
  private final String masterkeyRootConfigDir;
  private final String componentDir;
  private final String defaultFileName;
  private static final Logger logger = Logger.getLogger("com.indexdata.masterkey.config");

  public ConfigFileBase(ServletContext context, String defaultConfigFileName) 
    throws IOException {
    String rootConfigDir = context.getInitParameter(
      MASTERKEY_ROOT_CONFIG_DIR_PARAM);
    checkMandatoryParameter(MASTERKEY_ROOT_CONFIG_DIR_PARAM, rootConfigDir);
    //look for sepcial paths that are relative to the servlet context
    if (rootConfigDir.startsWith("war://")) {
      masterkeyRootConfigDir = context.
        getRealPath(rootConfigDir.substring(6));
      logger.debug(
        "MASTERKEY_ROOT_CONFIG is relative to servlet context, resolving as "
        + masterkeyRootConfigDir);
    } else {
      masterkeyRootConfigDir = rootConfigDir;
    }
    componentDir = context.getInitParameter(
      MASTERKEY_COMPONENT_CONFIG_DIR_PARAM);
    checkMandatoryParameter(MASTERKEY_COMPONENT_CONFIG_DIR_PARAM, componentDir);
    if ((defaultConfigFileName != null) && (!defaultConfigFileName.isEmpty()))
      defaultFileName = defaultConfigFileName;
    else
      defaultFileName = context.getInitParameter(MASTERKEY_CONFIG_FILE_NAME_PARAM);
    checkMandatoryParameter(MASTERKEY_CONFIG_FILE_NAME_PARAM, defaultFileName);
  }

  public ConfigFileBase(String masterkeyRootConfigDir, String componentDir,
    String defaultFileName) {
    this.masterkeyRootConfigDir = masterkeyRootConfigDir;
    this.componentDir = componentDir;
    this.defaultFileName = defaultFileName;
  }

  public String getComponentDir() {
    return componentDir;
  }

  public String getFileName() {
    return defaultFileName;
  }

  public String getMasterkeyRootConfigDir() {
    return masterkeyRootConfigDir;
  }
  
  /**
   * Gets the root directory for the component, excluding the host specific
   * subdirectory
   *
   * @return
   */
  public String getComponentDirPath() {
    return masterkeyRootConfigDir + getComponentDir();
  }
  
  public String getComponentConfDDirPath() {
    return getComponentDirPath() + "/conf.d";
  }
  
  /**
   * Convenience method for checking the existence of a given init parameter in
   * web.xml
   *
   * @param name
   * @param value
   */
  static void checkMandatoryParameter(String name, String value) throws
    IOException {
    if (value == null || value.length() == 0 || value.equals("null")) {
      logger.error("Init parameter " + name
        + " missing in deployment descriptor (web.xml)");
      throw new IOException("Init parameter " + name
        + " missing in deployment descriptor");
    }
  }
  
  public List<String> getConfiguredHosts() throws IOException {
    File compDir = new File(getComponentDirPath());
    if (!compDir.isDirectory()) 
      throw new IOException("'"+getComponentDirPath()+"' does not exist or not a directory.");
    final File[] mapFiles = compDir.listFiles(new FileFilter() {
      @Override
      public boolean accept(File pathname) {
        return pathname.getName().endsWith(DOMAIN_CONFIG_FILE_POSTFIX);
      }
    });
    List<String> hosts = new ArrayList<String>(mapFiles.length);
    for (File mapFile : mapFiles) {
      hosts.add(mapFile.getName().substring(0, mapFile.getName().length()-DOMAIN_CONFIG_FILE_POSTFIX.length()));
    }
    return hosts;
  }
}
