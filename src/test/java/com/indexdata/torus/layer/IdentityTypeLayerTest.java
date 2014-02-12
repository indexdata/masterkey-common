/*
 * Copyright (c) 1995-2011, Index Data
 * All rights reserved.
 * See the file LICENSE for details.
 */
package com.indexdata.torus.layer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.cedarsoftware.util.DeepEquals;
import com.indexdata.torus.Layer;
import com.indexdata.torus.Record;
import com.indexdata.utils.XmlUtils;

/**
 *
 * @author jakub
 */
public class IdentityTypeLayerTest {
  private static JAXBContext jaxbCtx;

  public IdentityTypeLayerTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
    jaxbCtx = JAXBContext.newInstance("com.indexdata.torus.layer:com.indexdata.torus");
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
   * Tests unmarshalling of identity records containing 'identityId' field
   */
  @Test
  public void testObsoleteIdentityLayer() {
    Document doc = XmlUtils.newDoc("record");
    doc.getDocumentElement().setAttribute("type", "identity");
    Element layer = doc.createElement("layer");
    layer.setAttribute("name", "override");
    layer.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:type", "identityTypeLayer");
    doc.getDocumentElement().appendChild(layer);
    XmlUtils.appendTextNode(layer, "displayName", "Test Identity");
    XmlUtils.appendTextNode(layer, "identityId", "my_library");
    XmlUtils.appendTextNode(layer, "userName", "user");
    XmlUtils.appendTextNode(layer, "password", "pass");
    Record rec = null;
    try {
      rec =
        (Record) jaxbCtx.createUnmarshaller().unmarshal(doc);
    } catch (JAXBException ex) {
      fail("Unmarshalling failed: " + ex.getMessage());
    }
    List<Layer> layers = rec.getLayers();
    IdentityTypeLayer itl = (IdentityTypeLayer) layers.get(0);
    assertEquals("my_library", itl.getIdentityId());
    assertEquals("searchable.my_library", itl.getSearchablesRealm());
    assertEquals("cat.my_library", itl.getCategoriesRealm());
  }

  /**
   * Tests unmarshalling of identity records containing 'searchablesRealm'
   * and 'categoriesRealm' fields
   */
  @Test
  public void testNewFormIdentityLayer() {
    Document doc = XmlUtils.newDoc("record");
    doc.getDocumentElement().setAttribute("type", "identity");
    Element layer = doc.createElement("layer");
    layer.setAttribute("name", "override");
    layer.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:type", "identityTypeLayer");
    doc.getDocumentElement().appendChild(layer);
    XmlUtils.appendTextNode(layer, "displayName", "Test Identity");
    XmlUtils.appendTextNode(layer, "searchablesRealm", "my_library-searchables");
    XmlUtils.appendTextNode(layer, "categoriesRealm", "my_library-categories");
    XmlUtils.appendTextNode(layer, "userName", "user");
    XmlUtils.appendTextNode(layer, "password", "pass");
    Record rec = null;
    try {
      rec =
        (Record) jaxbCtx.createUnmarshaller().unmarshal(doc);
    } catch (JAXBException ex) {
      fail("Unmarshalling failed: " + ex.getMessage());
    }
    List<Layer> layers = rec.getLayers();
    IdentityTypeLayer itl = (IdentityTypeLayer) layers.get(0);
    assertEquals(null, itl.getIdentityId());
    assertEquals("my_library-searchables", itl.getSearchablesRealm());
    assertEquals("my_library-categories", itl.getCategoriesRealm());
  }

  /**
   * Tests unmarshalling of identity records containing both 'identityId' AND
   * 'searchablesRealm', 'categoriesRealm' fields (*Realm fields take precedence)
   */
  @Test
  public void testBothIdentityLayer() {
    Document doc = XmlUtils.newDoc("record");
    doc.getDocumentElement().setAttribute("type", "identity");
    Element layer = doc.createElement("layer");
    layer.setAttribute("name", "override");
    layer.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:type", "identityTypeLayer");
    doc.getDocumentElement().appendChild(layer);
    XmlUtils.appendTextNode(layer, "displayName", "Test Identity");
    XmlUtils.appendTextNode(layer, "displayName", "Test Identity");
    XmlUtils.appendTextNode(layer, "identityId", "my_library");
    XmlUtils.appendTextNode(layer, "searchablesRealm", "my_library-searchables");
    XmlUtils.appendTextNode(layer, "categoriesRealm", "my_library-categories");
    XmlUtils.appendTextNode(layer, "userName", "user");
    XmlUtils.appendTextNode(layer, "password", "pass");
    Record rec = null;
    try {
      rec =
        (Record) jaxbCtx.createUnmarshaller().unmarshal(doc);
    } catch (JAXBException ex) {
      fail("Unmarshalling failed: " + ex.getMessage());
    }
    List<Layer> layers = rec.getLayers();
    IdentityTypeLayer itl = (IdentityTypeLayer) layers.get(0);
    assertEquals("my_library", itl.getIdentityId());
    assertEquals("my_library-searchables", itl.getSearchablesRealm());
    assertEquals("my_library-categories", itl.getCategoriesRealm());
    
    Document node = XmlUtils.newDoc();
    try {
        jaxbCtx.createMarshaller().marshal(rec, node);
        XmlUtils.serialize(doc, System.out);
    } catch (JAXBException ex) {
      fail("Unmarshalling failed: " + ex.getMessage());
    } catch (TransformerException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    Record newRec = null;
    try {
      newRec =
        (Record) jaxbCtx.createUnmarshaller().unmarshal(node);
    } catch (JAXBException ex) {
      fail("Unmarshalling failed: " + ex.getMessage());
    }
    assertTrue("Failed to compare records", DeepEquals.deepEquals(rec, newRec));
    layers.add(new IdentityTypeLayer());
    assertTrue("Failed to differ records", !DeepEquals.deepEquals(rec, newRec));
    layers.remove(1);
    itl.setIdentityId("other_library");
    assertTrue("Failed to differ records", !DeepEquals.deepEquals(rec, newRec));
    itl.setIdentityId("my_library");
    assertTrue("Failed to compare records", DeepEquals.deepEquals(rec, newRec));
    }
}
