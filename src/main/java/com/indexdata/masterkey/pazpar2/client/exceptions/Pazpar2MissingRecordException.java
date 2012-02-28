package com.indexdata.masterkey.pazpar2.client.exceptions;

public class Pazpar2MissingRecordException extends Pazpar2ErrorException {
  
  /**
   * 
   */
  private static final long serialVersionUID = -7553942519052189583L;

  public Pazpar2MissingRecordException(String msg, int errorCode, 
      String errorMsg, String addInfo) {
    super(msg, errorCode, errorMsg, addInfo);
  }

}
