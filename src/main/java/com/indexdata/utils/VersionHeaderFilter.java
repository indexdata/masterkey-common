/*
 * Copyright (c) 1995-2012, Index Data
 * All rights reserved.
 * See the file LICENSE for details.
 */
package com.indexdata.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

/**
 * Read the version from the manifest file and embed in the response.
 * @author jakub
 */
public class VersionHeaderFilter implements Filter {
  private final Logger logger = Logger.getLogger("com.indexdata.masterkey");
  private String versionHeader;
  private String environmentHeader;
  
  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    //version header
    try {
      InputStream is = filterConfig.getServletContext()
        .getResourceAsStream("/META-INF/MANIFEST.MF");
      if (is == null) {
        logger.warn("No /META-INF/MANIFEST.MF found in the webapp, "
          + "'X-MK-Component' header will not be reported.");
      } else {
        Manifest mf = new Manifest(is);
        Attributes atts = mf.getMainAttributes();
        versionHeader = atts.getValue("Implementation-Title") 
        + " " + atts.getValue("Implementation-Version");
        if (atts.getValue("Implementation-Build") != null
        && !atts.getValue("Implementation-Build").isEmpty())
          versionHeader += " (" + atts.getValue("Implementation-Build") + ") ";
      }
    } catch (IOException ioe) {
      logger.warn("Cannot read /META-INF/MANIFEST.MF file, "
        + "'X-MK-Component' header will not be reported.", ioe);
    }
    //environment
    environmentHeader = "localhost";
    try {
      environmentHeader = InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException uhe) {
      logger.warn("Cannot look up name of the localhost", uhe);
    }
    //OS
    String osName = System.getProperty("os.name");
    boolean openBracket = false;
    if (osName != null && !osName.isEmpty()) {
      if (!openBracket) {
        environmentHeader += " (";
        openBracket = true;
      } else {
        environmentHeader += "; ";
      }
      environmentHeader += osName;
      String osVersion = System.getProperty("os.version");
      if (osVersion != null) {
        environmentHeader += " " + osVersion;
      }
      String osArch = System.getProperty("os.arch");
      if (osArch != null) {
        environmentHeader += "; " + osArch;
      }
    }
    //Java
    String jvmName = System.getProperty("java.vendor");
    if (jvmName != null && !jvmName.isEmpty()) {
      if (!openBracket) {
        environmentHeader += " (";
        openBracket = true;
      } else {
        environmentHeader += "; ";
      }
      environmentHeader += jvmName;
      String jvmVersion = System.getProperty("java.version");
      if (jvmVersion != null) {
        environmentHeader += " " + jvmVersion;
      }
    }
    //server
    String srvNameVersion = filterConfig.getServletContext().getServerInfo();
    if (srvNameVersion != null && !srvNameVersion.isEmpty()) {
      if (!openBracket) {
        environmentHeader += " (";
        openBracket = true;
      } else {
        environmentHeader += "; ";
      }
      environmentHeader += srvNameVersion;
    }
    if (openBracket)
      environmentHeader += ")";
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response,
    FilterChain chain) throws IOException, ServletException {
    ((HttpServletResponse) response).setHeader("X-MK-Component", versionHeader);
    ((HttpServletResponse) response).setHeader("X-MK-Environment", environmentHeader);
    chain.doFilter(request, response);
  }

  @Override
  public void destroy() {
  }

}
