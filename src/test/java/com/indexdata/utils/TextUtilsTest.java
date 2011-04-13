/*
 * Copyright (c) 1995-2011, Index Data
 * All rights reserved.
 * See the file LICENSE for details.
 */

package com.indexdata.utils;

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
public class TextUtilsTest {

    public TextUtilsTest() {
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
   * Test of joinPath method, of class TextUtils.
   */
  @Test
  public void testJoinPathWithSlashes() {
    System.out.println("joinPath");
    String expResult = "http://user:user@www.indexdata.com/service-proxy/index.html";
    String result = TextUtils.joinPath("http://user:user@www.indexdata.com/", "/", "/service-proxy/", "index.html");
    System.out.println(expResult + " ?= " + result);
    assertEquals(expResult, result);
  }

    @Test
  public void testJoinPathNoSlashes() {
    System.out.println("joinPath");
    String expResult = "http://user:user@www.indexdata.com/service-proxy/index.html";
    String result = TextUtils.joinPath("http://user:user@www.indexdata.com", "service-proxy", "index.html");
    System.out.println(expResult + " ?= " + result);
    assertEquals(expResult, result);
  }

}