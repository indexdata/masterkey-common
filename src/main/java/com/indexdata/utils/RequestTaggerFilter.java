/*
 * Copyright (c) 1995-2014, Index Datassss
 * All rights reserved.
 * See the file LICENSE for details.
 */

package com.indexdata.utils;

import java.io.IOException;
import java.util.Random;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.apache.log4j.MDC;

/**
 *
 * @author jakub
 */
public class RequestTaggerFilter implements Filter {

  @Override
  public void init(FilterConfig fc) throws ServletException {
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain fc)
    throws IOException, ServletException {
    Random rand = new Random();
    //generate random six digit number
    int randomNum = rand.nextInt((999999 - 100000) + 1) + 100000;
    MDC.put("requestTag", Integer.toString(randomNum));
    try {
      fc.doFilter(req, res);
    } finally {
      MDC.remove("requestTag");
    }
  }

  @Override
  public void destroy() {
  }
  
}
