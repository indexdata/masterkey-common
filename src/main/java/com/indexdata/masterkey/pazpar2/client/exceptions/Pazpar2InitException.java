/*
 * Copyright (c) 1995-2008, Index Data
 * All rights reserved.
 * See the file LICENSE for details.
 */

package com.indexdata.masterkey.pazpar2.client.exceptions;


public class Pazpar2InitException extends Pazpar2ErrorException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9213169857531588700L;

	public Pazpar2InitException(String msg, int errorCode, 
			String errorMsg, String addInfo) {
		super(msg, errorCode, errorMsg, addInfo);
	}
}