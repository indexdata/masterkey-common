/*
 * Copyright (c) 1995-2011, Index Data
 * All rights reserved.
 * See the file LICENSE for details.
 */
package com.indexdata.torus.layer;

import com.indexdata.torus.Layer;

import java.util.Collection;
import java.util.List;

import com.indexdata.torus.Record;

import javax.xml.bind.JAXBException;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

import com.indexdata.utils.XmlUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.transform.TransformerException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

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
    XmlUtils.appendTextNode(layer, "facetmap_author", "author");
    //XmlUtils.appendTextNode(layer, "limitmap_author", "rpn: @attr 1=author @attr 6=3");
    XmlUtils.appendTextNode(layer, "categories", "id_openaccess");
    //XmlUtils.appendTextNode(layer, "categories", "id_other");
    try {
      XmlUtils.serialize(doc, System.out);
    } catch (TransformerException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    Record rec = null;
    try {
      rec =
        (Record) jaxbCtx.createUnmarshaller().unmarshal(doc);
    } catch (JAXBException ex) {
      fail("Unmarshalling failed: " + ex.getMessage());
    }
    List<Layer> layers = rec.getLayers();
    SearchableTypeLayer stl = (SearchableTypeLayer) layers.get(0);
    assertEquals("1=author", stl.getCclMapAu());
    assertEquals("1=text",   stl.getCclMapTerm());

    Collection<DynamicElement> facetmap_author = stl.getElement("facetmap_author");
    assertTrue("Wrong count: " + facetmap_author.size(), facetmap_author.size() == 1);
    for (DynamicElement  element:facetmap_author) {
      assertTrue("Wrong facetmap (Author)", "author".equals(element.getValue()));
    }
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
  }
}
