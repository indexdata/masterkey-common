/*
 * Copyright (c) 1995-2011, Index Data
 * All rights reserved.
 * See the file LICENSE for details.
 */

package com.indexdata.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author jakub
 */
public class TextUtils {

  public static String joinPath(String... comps) {
    return TextUtils.joinPath('/', comps);
  }

  public static String joinPath(char sep, String... comps) {
    StringBuilder sb = new StringBuilder();
    char prev = '\0';
    for (String comp : comps) {
      if (prev != '\0' && prev != sep) {
        sb.append(sep);
        prev = sep;
      }
      int idx = comp.indexOf("://");
      int bound = idx == -1 ? -1 : idx + 3;
      for (int i = 0; i<comp.length(); i++) {
        char c = comp.charAt(i);
        if (i > bound && prev == sep && c == sep) {
        } else {
          sb.append(c);
        }
        prev = c;
      }
    }
    return sb.toString();
  }
 
  
  public static void copyStream(InputStream is, OutputStream os) throws IOException {
    byte[] buf = new byte[4096];
    for (int len = -1; (len = is.read(buf)) != -1;) {
      os.write(buf, 0, len);
    }
    os.flush();
  }

  public static void copyStreamWithReplace(InputStream is, OutputStream os, String[] tokens)
      throws IOException {
    BufferedReader br = new BufferedReader(new InputStreamReader(is));
    StringBuilder sb = new StringBuilder();
    int tokenLen = 0;
    if (tokens != null) {
      tokenLen = tokens.length % 2 == 1 ? tokens.length - 1 : tokens.length;
    }
    for (String line; (line = br.readLine()) != null;) {
      String replaced = line;
      for (int i = 0; i < tokenLen; i += 2) {
	replaced = replaced.replaceAll(tokens[i], tokens[i + 1]);
      }
      sb.append(replaced).append("\n");
    }
    br.close();
    os.write(sb.toString().getBytes());
  }
  
  public static String readStream(InputStream stream) throws IOException {
    return readStream(stream, "\n");
  }

  public static String readStream(InputStream stream, String newLineChar) throws IOException {
    BufferedReader br = new BufferedReader(new InputStreamReader(stream));
    StringBuilder sb = new StringBuilder();
    String line;
    String sep = "";
    while ((line = br.readLine()) != null) {
      sb.append(sep).append(line);
      sep = newLineChar;
    }
    br.close();
    return sb.toString();
  }
  
  public static Map<String, String> parseQueryString(String queryString) 
    throws UnsupportedEncodingException {
    String[] pairs = queryString.split("&");
    Map<String,String> map = new HashMap<String, String>(pairs.length);
    for (String pair : pairs) {
        int eq = pair.indexOf("=");
        if (eq < 0) {
            // key with no value
            map.put(URLDecoder.decode(pair, "UTF-8"), "");
        } else {
            // key=value
            String key = URLDecoder.decode(pair.substring(0, eq), "UTF-8");
            if (eq+1 < pair.length()) {
              String value = URLDecoder.decode(pair.substring(eq + 1), "UTF-8");
              map.put(key, value);
            } else {
              map.put(key, "");
            }
        }
    }
    return map;
  }

}
