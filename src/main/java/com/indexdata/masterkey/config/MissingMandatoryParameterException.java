package com.indexdata.masterkey.config;

public class MissingMandatoryParameterException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6416652188421378706L;

	public MissingMandatoryParameterException (String msg) {
		super(msg);
	}
}
