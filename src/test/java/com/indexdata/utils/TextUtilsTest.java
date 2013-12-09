/*
 * Copyright (c) 1995-2011, Index Data
 * All rights reserved.
 * See the file LICENSE for details.
 */

package com.indexdata.utils;

import java.io.UnsupportedEncodingException;
import java.util.Map;
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
  
  @Test
  public void testParseQueryString() throws UnsupportedEncodingException {
    String queryString = "key1=value1&key2&key3=&key4=value4=value4";
    Map<String, String> params = TextUtils.parseQueryString(queryString);
    assertTrue(params.containsKey("key1"));
    assertEquals("value1", params.get("key1"));
    assertTrue(params.containsKey("key2"));
    assertEquals("", params.get("key2"));
    assertTrue(params.containsKey("key3"));
    assertEquals("", params.get("key3"));
    assertTrue(params.containsKey("key4"));
    assertEquals("value4=value4", params.get("key4"));
  }

}