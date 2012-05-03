/*
 * Copyright (c) 1995-2012, Index Datassss
 * All rights reserved.
 * See the file LICENSE for details.
 */
package com.indexdata.masterkey.pazpar2.client;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;

import com.indexdata.utils.XmlUtils;

/**
 *
 * @author jakub
 */
public class FieldMapperTest {
  public FieldMapperTest() {
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
   * Test of getStylesheet method, of class FieldMapper.
   */
  @Test
  public void testGetStylesheet() throws Exception {
    System.out.println("getStylesheet");
    String map =
      "%ns marc http://loc.gov/\n"+
      "%import tmarc-base.xsl\n"+
      "%import some-base.xsl\n"+
      "999$* jtitle\n" +
      "999$ab jtitle-add\n" +
      "100$abc author\n" + 
      "245$!cd title\n";
    FieldMapper mapper = new FieldMapper(map);
    Document result = mapper.getStylesheet();
    XmlUtils.serialize(result, System.out);
    //assert
    //ns
    assertEquals("http://loc.gov/", mapper.getNamespaces().get("marc"));
  }
  
  /**
   * Test of getStylesheet method, of class FieldMapper.
   */
  @Test
  public void testGetStylesheetXML() throws Exception {
    System.out.println("getStylesheet");
    String map = 
      "/some/other jtitle\n" +
      "/some/other1 jtitle-add\n" +
      "/some/other3 author\n" + 
      "/some/other4 title\n";
    FieldMapper instance = new FieldMapper(map);
    Document result = instance.getStylesheet();
    XmlUtils.serialize(result, System.out);
    //assert
  }

}
