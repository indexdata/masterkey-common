/*
 * Copyright (c) 1995-2008, Index Data
 * All rights reserved.
 * See the file LICENSE for details.
 */

package com.indexdata.masterkey.pazpar2.client;

import java.io.IOException;
import java.io.OutputStream;

import org.w3c.dom.Document;

import com.indexdata.masterkey.pazpar2.client.exceptions.Pazpar2ErrorException;
import com.indexdata.masterkey.pazpar2.client.exceptions.Pazpar2IOException;
import com.indexdata.masterkey.pazpar2.client.exceptions.ProxyErrorException;

/**
 * Pazpar2 proxy API. The proxy class is always instantiated per request. 
 * No backend HTTP connection reuse occurs.
 * @author jakub
 */
public interface Pazpar2Client {
    /**
     * Obtain new pazpar2 session valid for unspecified time period.
     * @throws Pazpar2ErrorException on pazpar2 application-level errors
     * @throws com.indexdata.masterkey.pazpar2.exceptions.Pazpar2IOException
     * @throws com.indexdata.masterkey.pazpar2.exceptions.Pazpar2MalformedOutputException
     */
    void init() throws Pazpar2ErrorException, Pazpar2IOException;
    
    /**
     * Returns sessionId for currently used pazpar2 session.
     * @return pazpar2 session id
     */
    String getSessionId();
    
    /**
     * 
     * @return The Pazpar2 target settings for the current session
     */
    Pazpar2Settings getSettings();
    
    /**
     * 
     * @return configuration for this client instance 
     */
    Pazpar2ClientConfiguration getConfiguration();
                
    /**
     * Stores the latest search command issued through this proxy client
     * @param command
     */
    void setSearchCommand(ClientCommand command);    
    
    /**
     * Returns the most recent search command registered with this client
     * @return
     */
    ClientCommand getSearchCommand ();
        
    /**
     * Returns the number of searches so far on this pazpar2 client object
     */
    int getSearchCount();
    
    /**
     * Forwards any Pazpar2 command and stores the results in the OutputStream
     * 
     * @param command
     * @param os
     * @return HTTP status of pazpar2 request
     * @throws IOException
     * @throws Pazpar2ErrorException
     */    
    Pazpar2HttpResponse executeCommand(ClientCommand command, OutputStream os) throws  IOException, Pazpar2ErrorException;
    
    /**
     * Returns the results of the latest command of the kind processed by this proxy client
     * @param command
     * @return
     */
    public Document getResults(String command);
    
    
    public long getTimeStamp(String command, int searchNumber);
    
    
    /**
     * Finds a 'hit' element by record ID from the latest 'show' results
     * @param recid
     * @return hit formatted as a record
     */
    public Document getHit(String recid);

    /**
     * Creates a Pazpar2 client clone with the same Pazpar2 configuration and target settings
     * as the original. The client will be initialized but no search state is copied over from
     * the original. The cloned client can be used by plug-ins to make pazpar2 requests 
     * independently of RelayPlugins master pazpar2 session.  
     * 
     * @return Uninitialized clone of this client
     *  
     * @throws ProxyErrorException
     */
    Pazpar2Client cloneMe() throws ProxyErrorException, Pazpar2ErrorException, Pazpar2IOException;
    
    /**
     * Makes a non-managed pazpar2 search request on a cloned pazpar2 client
     * 
     * This is for 'embedded' pazpar2 requests by other plug-ins than Relay and 
     * independently of the main pazpar2 session. It will not update the state of the pazpar2 client object.
     *  
     * The method should only be invoked on a cloned pazpar2 client object, using it on a main  
     * pazpar2 session would compromise the state of the session. 
     * 
     * @param queryString
     * @return pazpar2's response as a Document
     * @throws StandardServiceException on Pazpar2 errors or parsing errors.
     * @throws UnsupportedOperationException if invoked on a client object that was not cloned. 
     */            
    public Document searchRequest (String queryString) throws Pazpar2ErrorException, Pazpar2IOException;

    
    /**
     * Makes a non-managed/transient pazpar2 record offset request.
     * 
     * This is for 'embedded' pazpar2 requests by other plug-ins than Relay and
     * independently of the main pazpar2 session. It will not update the state of the pazpar2 client object.   
     * 
     * @param queryString
     * @return pazpar2's response as a Document
     * @throws StandardServiceException on Pazpar2 errors or parsing errors.
     */
    public Document recordRequest (String recid, int offset) throws Pazpar2ErrorException, Pazpar2IOException;

    /**
     * Makes a non-managed/transient pazpar2 record request.
     * 
     * This is for 'embedded' pazpar2 requests by other plug-ins than Relay and 
     * independently of the main pazpar2 session. It will not update the state of the pazpar2 client object. 
     * 
     * @param queryString
     * @return pazpar2's response as a Document
     * @throws StandardServiceException on Pazpar2 errors or parsing errors. 
     */    
    public Document recordRequest (String recid) throws Pazpar2ErrorException, Pazpar2IOException;

    /**
     * Makes a non-managed/transient pazpar2 show request.
     * 
     * This is for 'embedded' pazpar2 requests by other plug-ins than Relay and
     * independently of the main pazpar2 session. It will not update the state of the pazpar2 client object.
     * 
     * @param queryString
     * @return pazpar2's response as a Document
     * @throws StandardServiceException on Pazpar2 errors or parsing errors.
     */        
    public Document showRequest () throws Pazpar2ErrorException, Pazpar2IOException;
    
        
}
