/*
 * Copyright (c) 1995-2008, Index Data
 * All rights reserved.
 * See the file LICENSE for details.
 */

package com.indexdata.rest.client;

/**
 * General resource connection exception
 * @author jakub
 */
public class ResourceConnectionException extends Exception {

    /**
	 * 
	 */
	private static final long serialVersionUID = 3794084230779019415L;

	public ResourceConnectionException(String message) {
        super(message);
    }
    
    public ResourceConnectionException(Exception cause) {
        super(cause);
    }
    
}
