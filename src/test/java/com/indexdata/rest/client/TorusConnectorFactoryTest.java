/*
 * Copyright (c) 1995-2011, Index Data
 * All rights reserved.
 * See the file LICENSE for details.
 */

package com.indexdata.rest.client;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jakub
 */
public class TorusConnectorFactoryTest {

    public TorusConnectorFactoryTest() {
    }

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

  /**
   * Test of getTorusURL method, of class TorusConnectorFactory.
   */
  @Test
  public void testGetTorus1URL() {
    System.out.println("getTorusURL");
    String torusURL = "http://host.indexdata.com/torus/searchable";
    String prefix = "searchable";
    String identityId = "my_library";
    String queryParams = "";
    String expResult = "http://host.indexdata.com/torus/searchable/records/my_library/";
    String result =
      TorusConnectorFactory.getTorusURL(torusURL, prefix, identityId,
      queryParams);
    assertEquals(expResult, result);
  }

    /**
   * Test of getTorusURL method, of class TorusConnectorFactory.
   */
  @Test
  public void testGetTorus2URL() {
    System.out.println("getTorusURL");
    String torusURL = "http://host.indexdata.com/torus2/";
    String prefix = "searchable";
    String identityId = "my_library";
    String queryParams = "";
    String expResult = "http://host.indexdata.com/torus2/searchable.my_library/records/";
    String result =
      TorusConnectorFactory.getTorusURL(torusURL, prefix, identityId,
      queryParams);
    assertEquals(expResult, result);
  }

  /**
   * Test of getTorusURL method, of class TorusConnectorFactory.
   */
  @Test
  public void testGetTorus1URLWithoutTorusInBase() {
    System.out.println("getTorusURL");
    String torusURL = "http://host.indexdata.com/torus";
    String prefix = "searchable";
    String identityId = "my_library";
    String queryParams = "";
    String expResult = "http://host.indexdata.com/torus/searchable/records/my_library/";
    String result =
      TorusConnectorFactory.getTorusURL(torusURL, prefix, identityId,
      queryParams);
    assertEquals(expResult, result);
  }

}