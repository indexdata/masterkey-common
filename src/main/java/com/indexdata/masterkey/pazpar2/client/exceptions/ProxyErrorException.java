/*
 * Copyright (c) 1995-2008, Index Data
 * All rights reserved.
 * See the file LICENSE for details.
 */

package com.indexdata.masterkey.pazpar2.client.exceptions;

/**
 * Proxy specific errors.
 * @author jakub
 */
public class ProxyErrorException extends Pazpar2ErrorException {

    /**
	 * 
	 */
	private static final long serialVersionUID = -5494202215555250425L;

	public enum ErrorCode {
        NOT_AUTHENTICATED,
        CONFIGURATION_ERROR,
        PAZPAR2_IO_ERROR,
        TARGET_TORUS_ERROR,
        IDENTITY_TORUS_ERROR
    };

    /**
     * Creates a proxy-specifix error returned to the client as a pazpar2 error.
     * @param msg
     * @param code
     */
    public ProxyErrorException(String msg, ErrorCode code) {
        super(msg, 100 + code.ordinal(), code.toString().replaceAll("_", " "), msg);
    }
}
