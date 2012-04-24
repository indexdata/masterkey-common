/*
 * Copyright (c) 1995-2012, Index Datassss
 * All rights reserved.
 * See the file LICENSE for details.
 */
package com.indexdata.masterkey.pazpar2.client;

import com.indexdata.masterkey.pazpar2.client.FieldMapper.MapType;
import com.indexdata.utils.XmlUtils;
import org.junit.*;
import static org.junit.Assert.*;
import org.w3c.dom.Document;

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
      "999$* jtitle\n" +
      "999$ab jtitle-add\n" +
      "100$abc author\n" + 
      "245$!cd title\n";
    FieldMapper instance = new FieldMapper(map);
    Document result = instance.getStylesheet(null);
    XmlUtils.serialize(result, System.out);
    //assert
  }

}
