/*
 * Copyright (c) 1995-2008, Index Data
 * All rights reserved.
 * See the file LICENSE for details.
 */
package com.indexdata.masterkey.pazpar2.client;

import com.indexdata.masterkey.pazpar2.client.exceptions.Pazpar2ErrorException;
import com.indexdata.masterkey.pazpar2.client.exceptions.Pazpar2IOException;
import com.indexdata.masterkey.pazpar2.client.exceptions.ProxyErrorException;


/**
 * @author nielserik, jakub
 *
 */
public class Pazpar2ClientGeneric extends AbstractPazpar2Client {
  private static final long serialVersionUID = -972629015790377227L;

  public Pazpar2ClientGeneric(Pazpar2ClientConfiguration cfg) 
    throws ProxyErrorException {
    super(cfg);
  }


  @Override
  protected boolean requiresForcedInit() {
    return false;
  }
  
  /**
   * Initializes a Pazpar2 session while retaining statically defined databases
   * from Pazpar2's configuration
   */
  @Override
  public void init() throws Pazpar2IOException, Pazpar2ErrorException {
    sendInit(false);
  }
  
  @Override
  public Pazpar2Client cloneMe() throws Pazpar2ErrorException, 
    Pazpar2IOException {
    Pazpar2Client client = new Pazpar2ClientGeneric(this.cfg);
    client.init();
    return client;
  }

  @Override
  public Pazpar2Settings getSettings() {
    return null;
  }
}
