/*
 * Copyright (c) 1995-2011, Index Data
 * All rights reserved.
 * See the file LICENSE for details.
 */
package com.indexdata.torus.layer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.TransformerException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.indexdata.torus.Layer;
import com.indexdata.torus.Record;
import com.indexdata.utils.XmlUtils;

/**
 *
 * @author Dennis
 */
public class SearchableTypeLayerTest {
  private static JAXBContext jaxbCtx;

  public SearchableTypeLayerTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
    jaxbCtx = JAXBContext.newInstance("com.indexdata.torus.layer:com.indexdata.torus");
    //DynamicElement.class, Layer.class, Record.class, Records.class, CategoryTypeLayer.class, IdentityTypeLayer.class, SearchableTypeLayer.class
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

  @Test
  public void testSearchableLayer() {
    Document doc = XmlUtils.newDoc("record");
    doc.getDocumentElement().setAttribute("type", "searchable");
    Element layer = doc.createElement("layer");
    layer.setAttribute("name", "override");
    layer.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:type", "searchableTypeLayer");
    doc.getDocumentElement().appendChild(layer);
    XmlUtils.appendTextNode(layer, "cclmap_au", "1=author");
    XmlUtils.appendTextNode(layer, "cclmap_term", "1=text");
    String values[][] = {{ "facetmap_author", "author" },
			 {"limitmap_author", "rpn: @attr 1=author @attr 6=3"}};
    Map<String, String> testValues = new HashMap<String, String>();
    for (String[] keyValue : values) {
      XmlUtils.appendTextNode(layer, keyValue[0], keyValue[1]);
      testValues.put(keyValue[0], keyValue[1]);
    }
    XmlUtils.appendTextNode(layer, "categories", "id_openaccess");
    XmlUtils.appendTextNode(layer, "categories", "id_other");
    try {
      Properties prop = new Properties();
      prop.setProperty("indent", "YES");
      XmlUtils.serialize(doc, System.out, prop);
    } catch (TransformerException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    Record rec = null;
    try {
      DynamicElementAdapter adapter = new DynamicElementAdapter(jaxbCtx);
      Unmarshaller unmarshall = jaxbCtx.createUnmarshaller();
      unmarshall.setAdapter(adapter);
      rec = (Record) unmarshall.unmarshal(doc);
    } catch (JAXBException ex) {
      fail("Unmarshalling failed: " + ex.getMessage());
    }
    List<Layer> layers = rec.getLayers();
    SearchableTypeLayer stl = (SearchableTypeLayer) layers.get(0);
    assertEquals("1=author", stl.getCclMapAu());
    assertEquals("1=text",   stl.getCclMapTerm());
    
    Collection<DynamicElement> dynamicElements = stl.getDynamicElements();
    assertTrue("Wrong count: " + dynamicElements.size(), dynamicElements.size() == testValues.size());
    for (DynamicElement  element :dynamicElements) {
      String value = testValues.get(element.getName());
      assertTrue("Value differs for " + element.getName(), element.getValue().equals(value));
    }
  }

}
