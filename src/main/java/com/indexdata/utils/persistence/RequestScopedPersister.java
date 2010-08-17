/*
 * Copyright (c) 1995-2009, Index Data
 * All rights reserved.
 * See the file LICENSE for details.
 */

package com.indexdata.utils.persistence;

import javax.persistence.PersistenceException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import org.apache.log4j.Logger;

/**
 *
 * @author jakub
 */
public class RequestScopedPersister implements ServletRequestListener, ServletContextListener {

    private final static Logger logger = Logger.getLogger("com.indexdata.masterkey");

    @Override
    public void requestInitialized(ServletRequestEvent e) {
        //do nothing here EntityManager is lazy loaded
    }

    @Override
    public void requestDestroyed(ServletRequestEvent e) {
        if (EntityUtil.isInitialized()) {
          EntityUtil.closeManager();
        } else {
          logger.warn("EnityUtil is not initialized -- persistence will not be closed.");
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent e) {
        String puName = e.getServletContext().getInitParameter("persistence-unit-name");
        if (puName == null || puName.isEmpty()) {
          logger.warn("Missing init paremeter 'persistence-unit-name' from web.xml");
          return;
        }
        try {
          EntityUtil.initialize(puName);
        } catch (PersistenceException pe) {
          logger.warn("Cannot initialize EntityUtil (persistence context cannot be created), plugins that need persistence may throw errors");
          logger.debug(pe);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent e) {
        EntityUtil.terminate();
    }

}
