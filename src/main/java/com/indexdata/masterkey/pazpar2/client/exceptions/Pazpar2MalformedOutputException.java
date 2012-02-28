/*
 * Copyright (c) 1995-2008, Index Data
 * All rights reserved.
 * See the file LICENSE for details.
 */

package com.indexdata.masterkey.pazpar2.client.exceptions;

public class Pazpar2MalformedOutputException extends Pazpar2IOException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7737301732604957157L;

	public Pazpar2MalformedOutputException(String msg) {
		super(msg);
	}

	public Pazpar2MalformedOutputException(Throwable e) {
		super(e);
	}

	public Pazpar2MalformedOutputException(String msg, Throwable e) {
		super(msg, e);
	}

}
