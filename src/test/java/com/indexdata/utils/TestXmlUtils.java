package com.indexdata.utils;

import junit.framework.TestCase;

import org.w3c.dom.Document;

public class TestXmlUtils extends TestCase {
  
  
  public void testXmlUtilNewDocument() {
    Document doc = XmlUtils.newDoc();
    assertTrue(doc != null);
  }

}
