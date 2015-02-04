/*
 * Copyright (c) 1995-2015, Index Data
 * All rights reserved.
 * See the file LICENSE for details.
 */

package com.indexdata.utils;

import java.util.Date;

/**
 *
 * @author jakub
 */
public class CacheEntry {
  private Date timestamp;
  private Object payload;

  public CacheEntry(Date timestamp, Object payload) {
    this.timestamp = timestamp;
    this.payload = payload;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  public Object getPayload() {
    return payload;
  }

  public void setPayload(Object payload) {
    this.payload = payload;
  }
  
  
  
}
