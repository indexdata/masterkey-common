/*
 * Copyright (c) 1995-2008, Index Data
 * All rights reserved.
 * See the file LICENSE for details.
 */

package com.indexdata.rest.client;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import com.indexdata.utils.PerformanceLogger;
import com.indexdata.utils.TextUtils;

/**
 * 
 * @param <T> resource binded type
 * @author jakub
 */
public class ResourceConnector<T> {
    private URL url;
    private String mimeType = "application/xml";
    private String entityPackages;
    private Class<T> entityType;
    private JAXBContext jaxbCtx;

    public ResourceConnector(URL url, Class<T> type) {
        this.url = url;
        this.entityType = type;
    }

    public ResourceConnector(URL url, String entityPackages) {
        this.url = url;
        this.entityPackages = entityPackages;
    }

    private JAXBContext getJAXBContext() throws JAXBException {
        if (jaxbCtx == null) {
            if (entityPackages != null) {
                jaxbCtx = JAXBContext.newInstance(entityPackages);
            } else {
                jaxbCtx = JAXBContext.newInstance(entityType);
            }
        }
        return jaxbCtx;
    }

    public URL getURL() {
      return url;
    }

    @SuppressWarnings("unchecked")
	public T get() throws ResourceConnectionException {
    	long start = PerformanceLogger.start();
        Object obj = null;
        try {        
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();        
            if (responseCode == 200) {            
                JAXBContext context = getJAXBContext();
                obj = context.createUnmarshaller().unmarshal(conn.getInputStream());                
            } else {
                throw new ResourceConnectionException("Cannot retrieve resource " + url.toString() + " - status code " + responseCode);
            }
            Logger.getLogger(getClass()).debug("GET " + url.toString() + ". Status: " + responseCode +  ". Location: " + conn.getHeaderField("Location"));
        } catch (JAXBException jaxbe) {
            throw new ResourceConnectionException("Get URL " + url.toString() + " failed: " + jaxbe.getMessage(), jaxbe);
        } catch (IOException ioe) {
            throw new ResourceConnectionException("Get URL " + url.toString() + " failed: " + ioe.getMessage(), ioe);
        }
        PerformanceLogger.finish("TORUS",url.getPath()+"?"+url.getQuery(),start);
        return (T) obj;
    }

    public void put(T t) throws ResourceConnectionException {
        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", mimeType);
            conn.setRequestMethod("PUT");

            JAXBContext context = getJAXBContext();
            context.createMarshaller().marshal(t, conn.getOutputStream());

            int responseCode = conn.getResponseCode();
            switch (responseCode) {
                case 200: //OK
                case 201: //Created
                case 202: //Accepted
                case 203: //Non-authoritative
                case 204: //No-content
                case 205: //Reset
                case 206:
                    //Partial
                    break;
                case 405:
                    throw new ResourceConnectionException("Cannot update resource " + url.toString() + " - HTTP method not allowed (405)");
                default:
                    throw new ResourceConnectionException("Cannot update resource " + url.toString() + " status code " + responseCode);
            }
            Logger.getLogger(getClass()).debug("PUT " + url.toString() + ". Status: " + responseCode +  ". Location:" + conn.getHeaderField("Location"));
        } catch (JAXBException jaxbe) {
            throw new ResourceConnectionException("Put URL " + url.toString() + " failed: " + jaxbe.getMessage(), jaxbe);
        } catch (IOException ioe) {
            throw new ResourceConnectionException("Put URL " + url.toString() + " failed: " + ioe.getMessage(), ioe);
        }
    }

    public void delete() throws ResourceConnectionException {
        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");
            int responseCode = conn.getResponseCode();
            //executes the request
            switch (responseCode) {
                case 200:   //OK
                case 201:   //Created
                case 202:   //Accepted
                case 203:   //Non-authoritative
                case 204:   //No-content
                case 205:   //Reset
                case 206:   //Partial
                    break;
                case 400:
                  String resp = TextUtils.readStream(conn.getErrorStream());
                  throw new ResourceConnectionException(400, resp);
                case 405:
                    throw new ResourceConnectionException("Cannot delete resource " + url.toString() + " - HTTP method not allowed (405)");
                default:
                    throw new ResourceConnectionException("Cannot delete resource " + url.toString() + " - status code " + responseCode);
            }
            Logger.getLogger(getClass()).debug("DELETE "+ url.toString() + ". Status: " + responseCode +  ". Location: " + conn.getHeaderField("Location"));
        } catch (IOException ioe) {
            throw new ResourceConnectionException("Delete URL " + url.toString() + " failed: " + ioe.getMessage(), ioe);
        }
    }

    public URL post(T t) throws ResourceConnectionException {
        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", mimeType);
            conn.setRequestMethod("POST");

            JAXBContext context = getJAXBContext();
            context.createMarshaller().marshal(t, conn.getOutputStream());

            int responseCode = conn.getResponseCode();
            switch (responseCode) {
                case 200:   //OK
                case 201:   //Created
                case 202:   //Accepted
                case 203:   //Non-authoritative
                case 204:   //No-content
                case 205:   //Reset
                case 206:   //Partial
                    break;
                case 405:
                    throw new ResourceConnectionException("Cannot create resource " + url.toString() + " - HTTP method not allowed (405)");
                default:
                    throw new ResourceConnectionException("Cannot create resource " + url.toString() + " - status code " + responseCode);
            }        
            Logger.getLogger(getClass()).debug("POST " + url.toString() + ". Status: " + responseCode +  ". Location: " + conn.getHeaderField("Location"));
            return new URL(conn.getHeaderField("Location"));
        } catch (IOException ioe) {
            throw new ResourceConnectionException("Post URL " + url.toString() + " failed: " + ioe.getMessage(), ioe);
        } catch (JAXBException jaxbe) {
            throw new ResourceConnectionException("Post URL " + url.toString() + " failed: " + jaxbe.getMessage(), jaxbe);
        }
    }

    public URL postAny(Object obj) throws ResourceConnectionException {
        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", mimeType);
            conn.setRequestMethod("POST");

            JAXBContext context = getJAXBContext();
            context.createMarshaller().marshal(obj, conn.getOutputStream());

            int responseCode = conn.getResponseCode();
            switch (responseCode) {
                case 200:   //OK
                case 201:   //Created
                case 202:   //Accepted
                case 203:   //Non-authoritative
                case 204:   //No-content
                case 205:   //Reset
                case 206:   //Partial
                    break;
                case 405:
                    throw new ResourceConnectionException("Cannot create resource " + url.toString() + " - HTTP method not allowed (405)");
                default:
                    throw new ResourceConnectionException("Cannot create resource " + url.toString() + " - staus code " + responseCode);
            }
            Logger.getLogger(getClass()).debug("POST Any " + url.toString() + ". Status: " + responseCode +  ". Location: " + conn.getHeaderField("Location"));
            return new URL(conn.getHeaderField("Location"));
        } catch (IOException ioe) {
            throw new ResourceConnectionException("PostAny URL " + url.toString() + " failed: " + ioe.getMessage(), ioe);
        } catch (JAXBException jaxbe) {
            throw new ResourceConnectionException("PostAny URL " + url.toString() + " failed: " + jaxbe.getMessage(), jaxbe);
        }
    }
}
