/*
 * Copyright (c) 1995-2011, Index Data
 * All rights reserved.
 * See the file LICENSE for details.
 */

package com.indexdata.rest.client;

import com.indexdata.utils.TextUtils;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

/**
 *
 * @author jakub
 */
public class TorusConnectorFactory {
  public static ResourceConnector getConnector(String torusURL, String prefix, String identityId, String queryParams, Class type) throws MalformedURLException {
    return new ResourceConnector(new URL(TorusConnectorFactory.getTorusURL(
      torusURL, prefix, identityId, queryParams)), type);
  }
  
  public static String getTorusURL(String torusURL, String prefix, String identityId, String queryParams) {
    String url = null;    
    try {
      if (torusURL.contains("torus2")) {
        //use the dotted notation to construct realm names
        url = TextUtils.joinPath(torusURL,
          URLEncoder.encode(prefix+"."+identityId, "UTF-8").replaceAll("[+]","%20"),
          "records/");
      } else {
        //now, the old toruses already organize realms in "buckets" with common parents and the URL
        //should already contain the prefix as the last path component, if not append it
        if (torusURL.matches(".*?"+prefix+"/?$"))
          url = TextUtils.joinPath(torusURL, "records",
            URLEncoder.encode(identityId, "UTF-8").replaceAll("[+]", "%20"), "/");
        else
          url = TextUtils.joinPath(torusURL, prefix, "records",
            URLEncoder.encode(identityId, "UTF-8").replaceAll("[+]", "%20"), "/");
      }
      if (url != null && queryParams != null) {
        url += queryParams;
      }
      return url;      
    } catch (UnsupportedEncodingException uee) {
      throw new RuntimeException(uee);
    }
  }
}
