/*
 * Copyright (c) 1995-2011, Index Data
 * All rights reserved.
 * See the file LICENSE for details.
 */
package com.indexdata.masterkey.pazpar2.client;

/**
 *
 * @author jakub
 */
public interface Pazpar2HttpResponse {
  public int getStatusCode();
  public String getContentType();
}
