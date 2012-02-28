/*
 * Copyright (c) 1995-2008, Index Data
 * All rights reserved.
 * See the file LICENSE for details.
 */

package com.indexdata.masterkey.pazpar2.client.exceptions;

public class Pazpar2IOException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8474653946742250299L;

	public Pazpar2IOException(String msg) {
		super(msg);
	}

	public Pazpar2IOException(String msg, Throwable e) {
		super(msg, e);
	}

	public Pazpar2IOException(Throwable e) {
		super(e);
	}
	
}
