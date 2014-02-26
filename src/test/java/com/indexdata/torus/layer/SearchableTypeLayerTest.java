/*
 * Copyright (c) 1995-2011, Index Data
 * All rights reserved.
 * See the file LICENSE for details.
 */
package com.indexdata.torus.layer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.TransformerException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.cedarsoftware.util.DeepEquals;
import com.indexdata.torus.Layer;
import com.indexdata.torus.Record;
import com.indexdata.utils.XmlUtils;

import static java.lang.System.out;

/**
 *
 * @author Dennis
 */
public class SearchableTypeLayerTest {
  private static JAXBContext jaxbCtx;
  private static Properties xmlPrintingProps;
  private Document testDoc;
  private Map<String, String> testValues;

  public SearchableTypeLayerTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
    xmlPrintingProps = new Properties();
    xmlPrintingProps.setProperty("indent", "YES");
    jaxbCtx = JAXBContext.newInstance("com.indexdata.torus.layer:com.indexdata.torus");
    //DynamicElement.class, Layer.class, Record.class, Records.class, CategoryTypeLayer.class, IdentityTypeLayer.class, SearchableTypeLayer.class
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Before
  public void setUp() {
    //set up test doc
    testDoc = XmlUtils.newDoc("record");
    testDoc.getDocumentElement().setAttribute("type", "searchable");
    Element layer = testDoc.createElement("layer");
    layer.setAttribute("name", "override");
    layer.
      setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:type",
      "searchableTypeLayer");
    testDoc.getDocumentElement().appendChild(layer);
    XmlUtils.appendTextNode(layer, "cclmap_au", "1=author");
    XmlUtils.appendTextNode(layer, "cclmap_term", "1=text");
    //dynamic values
    String values[][] = {
      {"facetmap_author", "author"},
      {"limitmap_author", "rpn: @attr 1=author @attr 6=3"}
    };
    testValues = new HashMap<String, String>();
    for (String[] keyValue : values) {
      XmlUtils.appendTextNode(layer, keyValue[0], keyValue[1]);
      testValues.put(keyValue[0], keyValue[1]);
    }
    XmlUtils.appendTextNode(layer, "categories", "id_openaccess");
    XmlUtils.appendTextNode(layer, "categories", "id_other");
  }

  @After
  public void tearDown() {
  }

  @Test
  public void testSearchableLayerUn_marshall() {
    out.println("test doc:");
    printDoc(testDoc);
    
    //unmarshall
    Record rec = null;
    try {
      Unmarshaller unmarshall = jaxbCtx.createUnmarshaller();
      rec = (Record) unmarshall.unmarshal(testDoc);
    } catch (JAXBException ex) {
      fail("Unmarshalling failed: " + ex.getMessage());
    }  
    
    //marshall
    String marshalledXml = null;
    try {
      Marshaller marshall = jaxbCtx.createMarshaller();
      marshall.setAdapter(new KeyValueAdapter());
      marshall.setProperty("jaxb.formatted.output", true);
      StringWriter result = new StringWriter();
      marshall.marshal((Object) rec, result);
      marshalledXml = result.toString();
    } catch (JAXBException ex) {
      fail("Marshalling failed: " + ex.getMessage());
    }
    out.println("test doc after umarshal/marshal:");
    out.println(marshalledXml);
    
    verifyLayer(testValues, rec);
    
    //unmarshall again
    Record newRec = null;
    try {
      Document marshalledDoc = XmlUtils.parse(new StringReader(marshalledXml));
      Unmarshaller unmarshall = jaxbCtx.createUnmarshaller();
      newRec = (Record) unmarshall.unmarshal(marshalledDoc);
      assertTrue(newRec != null);
    } catch (Exception ex) {
      fail("Unmarshalling failed: " + ex.getMessage());
    }
    verifyLayer(testValues, newRec);
    
    assertTrue("Failed to compare records", DeepEquals.deepEquals(rec, newRec));
  }

  private void verifyLayer(Map<String, String> testValues, Record rec) {
    List<Layer> layers = rec.getLayers();
    SearchableTypeLayer stl = (SearchableTypeLayer) layers.get(0);
    assertEquals("1=author", stl.getCclMapAu());
    assertEquals("1=text",   stl.getCclMapTerm());
    
    List<KeyValue> dynamicElements = stl.getDynamicElements();
    assertTrue("Wrong count: " + dynamicElements.size(), dynamicElements.size() == testValues.size());
    
    for (KeyValue  element : dynamicElements) {
      String expectedValue = testValues.get(element.getName());
      assertEquals("check '"+element.getName()+"' "+expectedValue+" ?= "+element.getValue(),
        expectedValue, element.getValue());
    }
  }

  private void printDoc(Document doc) {
    String testXml;
    try {
      StringWriter writer = new StringWriter();
      XmlUtils.serialize(doc, writer, xmlPrintingProps);
      testXml = writer.getBuffer().toString();
      out.println(testXml);
    } catch (TransformerException e) {
      fail("can't print the test doc "+e.getMessage());
    }
  }

}
