/*
 * Copyright (c) 1995-2012, Index Data
 * All rights reserved.
 * See the file LICENSE for details.
 */
package com.indexdata.utils;

import java.io.IOException;
import java.io.InputStream;
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
  private Logger logger = Logger.getLogger("com.indexdata.masterkey");
  private String versionHeader;
  
  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    try {
      InputStream is = filterConfig.getServletContext()
        .getResourceAsStream("/META-INF/MANIFEST.MF");
      if (is == null) {
        logger.warn("No /META-INF/MANIFEST.MF found in the webapp, "
          + "'X-MK-Component' header will not be reported.");
        return;
      }
      Manifest mf = new Manifest(is);
      Attributes atts = mf.getMainAttributes();
      versionHeader = atts.getValue("Implementation-Title") 
        + " " + atts.getValue("Implementation-Version");
      if (atts.getValue("Implementation-Build") != null
        && !atts.getValue("Implementation-Build").isEmpty())
        versionHeader += " (" + atts.getValue("Implementation-Build") + ") ";
    } catch (IOException ioe) {
      throw new ServletException("Cannot read /META-INF/MANIFEST.MF file.", ioe);
    }
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response,
    FilterChain chain) throws IOException, ServletException {
    ((HttpServletResponse) response).setHeader("X-MK-Component", versionHeader);
    chain.doFilter(request, response);
  }

  @Override
  public void destroy() {
  }

}
