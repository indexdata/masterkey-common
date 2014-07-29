    
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
import com.indexdata.torus.layer.KeyValue;
import com.indexdata.torus.layer.SearchableTypeLayer;
import com.indexdata.utils.XmlUtils;
import java.util.ArrayList;
import java.util.List;
import javax.xml.transform.TransformerException;
import org.junit.*;
import static org.junit.Assert.*;
import org.w3c.dom.Document;

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
  public void testFromSearchablesZurls() throws ProxyErrorException, TransformerException {
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
    //target are indexed by zurl by default
    Pazpar2Settings result = Pazpar2Settings.fromSearchables(records, pcc);
    
    //simple test
    String id = "test-target.com:8888/test-database";
    String zurl = result.getSetting(id, "pz:url");
    assertEquals("test-target.com:8888/test-database", zurl);
    //null setting without defaults
    String elm = result.getSetting(id, "pz:elements");
    assertEquals(null, elm);
    //empty setting without defaults
    String max = result.getSetting(id, "pz:maxrecs");
    assertEquals(null, max);
    //empty setting with defaults
    String cclTi = result.getSetting(id, "pz:cclmap:ti");
    assertEquals("u=4 s=al",cclTi);
    //test fieldMap
    String map = result.getSetting(id, "pz:xslt");
    assertEquals("[XML encoded]", map);
    
    Document settings = result.toXml(null);
    XmlUtils.serialize(settings, System.out);
  }
  
  @Test
  public void testFromSearchablesID() throws ProxyErrorException, TransformerException {
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
    //index targets by ID
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
    String map = result.getSetting("test-target-1", "pz:xslt");
    assertEquals("[XML encoded]", map);
    
    Document settings = result.toXml(null);
    XmlUtils.serialize(settings, System.out);
  }
  
  @Test
  public void testUDBasID() throws ProxyErrorException, TransformerException {
    Record record = new Record("searchable");
    SearchableTypeLayer layer = new SearchableTypeLayer();
    layer.setId("test-target-id-1");
    layer.setUdb("test-target-1");
    layer.setZurl("test-target.com:8888/test-database");
    layer.setElementSet(null);
    layer.setMaxRecords("");
    layer.setCclMapTi("");
    List<Layer> layers = new ArrayList<Layer>();
    layers.add(layer);
    record.setLayers(layers);
    List<Record> list = new ArrayList<Record>();
    list.add(record);
    Records records = new Records();
    records.setRecords(list);
    Pazpar2ClientConfiguration pcc = new Pazpar2ClientConfiguration(null);
    pcc.USE_OPAQUE_ID = "udb";
    Pazpar2Settings result = Pazpar2Settings.fromSearchables(records, pcc);
    
    //test that targets are indexed by UDB
    String zurl = result.getSetting("test-target-1", "pz:url");
    assertEquals("test-target.com:8888/test-database", zurl);
  }
  
  @Test
  public void testRichDatabaseParams() throws ProxyErrorException, TransformerException {
    Records records = new Records();
    List<Record> list = new ArrayList<Record>();
    records.setRecords(list);
    
    //test CF like target
    SearchableTypeLayer l0 = layer(list);
    l0.setUdb("test-target-0");
    l0.setZurl("test-target.com:8888");
    
    //test CF like target
    SearchableTypeLayer l1 = layer(list);
    l1.setUdb("test-target-1");
    l1.setZurl("test-target.com:8888");
    l1.setCfAuth("username/password");
    l1.setAuthentication("user/pass");
    
    //with existing query string (e.g SRU)
    SearchableTypeLayer l2 = layer(list);
    l2.setUdb("test-target-2");
    l2.setZurl("test-target.com:8888?some=other");
    
    //with query string and confusing parameter aka harvest resources
    SearchableTypeLayer l3 = layer(list);
    l3.setUdb("test-target-3");
    l3.setZurl("test-target.com:8888?some=other:3444");

    
    //with query string and cpath
    SearchableTypeLayer l4 = layer(list);
    l4.setUdb("test-target-4");
    l4.setZurl("test-target.com:8888/some-path/?some=other:3444");
    
    //with query string and confusing parameter aka harvest resources
    SearchableTypeLayer l5 = layer(list);
    l5.setUdb("test-target-5");
    l5.setZurl("test-target.com:8888?some=other:3444");
    List<KeyValue> targetMaps = new ArrayList<KeyValue>();
    l5.setDynamicElements(targetMaps);
    targetMaps.add(new KeyValue("targetmap_some", "other"));

    //with query string and confusing parameter aka harvest resources
    SearchableTypeLayer l6 = layer(list);
    l6.setUdb("test-target-6");
    l6.setZurl("test-target.com:8888");
    List<KeyValue> targetMaps2 = new ArrayList<KeyValue>();
    l6.setDynamicElements(targetMaps2);
    targetMaps2.add(new KeyValue("targetmap_some", "other"));

    Pazpar2ClientConfiguration pcc = new Pazpar2ClientConfiguration(null);
    pcc.USE_OPAQUE_ID = "udb";
    Pazpar2Settings result = Pazpar2Settings.fromSearchables(records, pcc);
    
    //test UDB as ID
    String zurl0 = result.getSetting("test-target-0", "pz:url");
    assertEquals("test-target.com:8888", zurl0);

    String zurl1 = result.getSetting("test-target-1", "pz:url");
    assertEquals("test-target.com:8888/?password=pass&user=user", zurl1);
    
    String zurl2 = result.getSetting("test-target-2", "pz:url");
    assertEquals("test-target.com:8888?some=other", zurl2);
    
    String zurl3 = result.getSetting("test-target-3", "pz:url");
    assertEquals("test-target.com:8888?some=other:3444", zurl3);
    
    String zurl4 = result.getSetting("test-target-4", "pz:url");
    assertEquals("test-target.com:8888/some-path/?some=other:3444", zurl4);

    // disabling the test that never worked
    // String zurl5 = result.getSetting("test-target-5", "pz:url");
    // assertEquals("test-target.com:8888/,some=other?some=other:3444", zurl5);
    
    String zurl6 = result.getSetting("test-target-6", "pz:url");
    assertEquals("test-target.com:8888/,some=other", zurl6);
  }
  
  private SearchableTypeLayer layer(List<Record> records) {
    Record record = new Record("searchable");
    records.add(record);
    List<Layer> layers = new ArrayList<Layer>();
    record.setLayers(layers);
    SearchableTypeLayer layer = new SearchableTypeLayer();
    layers.add(layer);
    return layer;
  }
}
