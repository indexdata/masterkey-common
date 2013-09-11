/*
 * Copyright (c) 1995-2008, Index Data
 * All rights reserved.
 * See the file LICENSE for details.
 */
package com.indexdata.rest.client;

/**
 * General resource connection exception
 *
 * @author jakub
 */
public class ResourceConnectionException extends Exception {
  /**
   *
   */
  private static final long serialVersionUID = 3794084230779019415L;
  private int statusCode;
  private String serverMessage;
  
  public ResourceConnectionException(int code, String message, Exception cause) {
    this("HTTP "+code+": "+message, cause);
    statusCode = code;
    serverMessage = message;
  }
  
  public ResourceConnectionException(int code, String message) {
    this("HTTP "+code+": "+message);
    statusCode = code;
    serverMessage = message;
  }

  public ResourceConnectionException(String message, Exception cause) {
    super(message, cause);
  }
  
  public ResourceConnectionException(String message) {
    super(message);
  }

  public int getStatusCode() {
    return statusCode;
  }

  public String getServerMessage() {
    return serverMessage;
  }

  @Override
  public String toString() {
    return super.getMessage();
  }
}
