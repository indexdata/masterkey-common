/*
 * Copyright (c) 1995-2012, Index Datassss
 * All rights reserved.
 * See the file LICENSE for details.
 */
package com.indexdata.masterkey.pazpar2.client;

import com.indexdata.masterkey.pazpar2.client.exceptions.ProxyErrorException;
import com.indexdata.torus.Layer;
import com.indexdata.torus.Record;
import com.indexdata.torus.Records;
import com.indexdata.torus.layer.SearchableTypeLayer;
import java.util.ArrayList;
import java.util.List;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author jakub
 */
public class Pazpar2SettingsTest {
  public Pazpar2SettingsTest() {
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
   * Test of fromSearchables method, of class Pazpar2Settings.
   */
  @Test
  public void testFromSearchables() throws ProxyErrorException {
    Record record = new Record("searchable");
    SearchableTypeLayer layer = new SearchableTypeLayer();
    layer.setId("test-target-1");
    layer.setZurl("test-target.com:8888/test-database");
    layer.setElementSet(null);
    layer.setMaxRecords("");
    layer.setCclMapTi("");
    layer.setFieldMap(
      "999$* jtitle\n" +
      "999$ab jtitle-add\n" +
      "100$abc author\n" + 
      "245$!cd title\n");
    List<Layer> layers = new ArrayList<Layer>();
    layers.add(layer);
    record.setLayers(layers);
    List<Record> list = new ArrayList<Record>();
    list.add(record);
    Records records = new Records();
    records.setRecords(list);
    Pazpar2ClientConfiguration pcc = new Pazpar2ClientConfiguration(null);
    pcc.USE_OPAQUE_ID = "yes";
    Pazpar2Settings result = Pazpar2Settings.fromSearchables(records, pcc);
    
    //simple test
    String zurl = result.getSetting("test-target-1", "pz:url");
    assertEquals("test-target.com:8888/test-database", zurl);
    //null setting without defaults
    String elm = result.getSetting("test-target-1", "pz:elements");
    assertEquals(null, elm);
    //empty setting without defaults
    String max = result.getSetting("test-target-1", "pz:maxrecs");
    assertEquals(null, max);
    //empty setting with defaults
    String cclTi = result.getSetting("test-target-1", "pz:cclmap:ti");
    assertEquals("u=4 s=al",cclTi);
    //test fieldMap
  }
}
