/*
 * Copyright (c) 1995-2011, Index Data
 * All rights reserved.
 * See the file LICENSE for details.
 */

package com.indexdata.utils;

/**
 *
 * @author jakub
 */
public class TextUtils {

  public static String joinPath(String... comps) {
    StringBuilder sb = new StringBuilder();
    char prev = '\0';
    for (String comp : comps) {
      int idx = comp.indexOf("://");
      int bound = idx == -1 ? -1 : idx + 3;
      for (int i = 0 ; i<comp.length();i++) {
        char c = comp.charAt(i);
        if (i > bound && prev == '/' && c == '/') {
        } else {
          sb.append(c);
        }
        prev = c;
      }
    }
    return sb.toString();
  }

}
