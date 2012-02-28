/*
 * Copyright (c) 1995-2008, Index Data
 * All rights reserved.
 * See the file LICENSE for details.
 */

package com.indexdata.masterkey.pazpar2.client.exceptions;

import com.indexdata.utils.XmlUtils;

public class Pazpar2ErrorException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7987177126235115417L;
	private int errorCode;
	private String errorMsg;
	private String addInfo;

	public Pazpar2ErrorException(String msg, int errorCode, 
			String errorMsg, String addInfo) {
		super(msg);
		this.errorCode = errorCode;
		this.errorMsg = errorMsg;
		this.addInfo = addInfo;
	}
	
	public int getErrorCode() {
		return errorCode;
	}
	
	public String getErrorMsg () {
		return errorMsg;
	}
	
	public String getAddInfo () {
		return addInfo;
	}
	
	public String toXML() {
		return "<error code=\"" + errorCode
        	+ "\" msg=\"" + XmlUtils.escape(errorMsg) + "\">"
        	+ XmlUtils.escape(addInfo) + "</error>";
        
	}
}
