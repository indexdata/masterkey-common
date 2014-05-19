package com.indexdata.utils;

import java.io.IOException;
import java.io.InputStream;
import static org.junit.Assert.*;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class TestXmlUtils {
  
  @Test
  public void testMissingDTD() throws IOException, SAXException {
    //
    InputStream testFile = this.getClass().getClassLoader()
      .getResourceAsStream("xml/remote-external-dtd_bad-host.xml");
    Document doc = XmlUtils.parse(testFile);
    assertEquals("Root node", "document", doc.getDocumentElement().getNodeName());
    
    testFile = this.getClass().getClassLoader()
      .getResourceAsStream("xml/remote-external-dtd_bad-file.xml");
    doc = XmlUtils.parse(testFile);
    assertEquals("Root node", "document", doc.getDocumentElement().getNodeName());

    testFile = this.getClass().getClassLoader()
      .getResourceAsStream("xml/local-external-dtd.xml");
    doc = XmlUtils.parse(testFile);
    assertEquals("Root node", "document", doc.getDocumentElement().getNodeName());

    testFile = this.getClass().getClassLoader()
      .getResourceAsStream("xml/external-entity.xml");
    doc = XmlUtils.parse(testFile);
    assertEquals("Root node", "document", doc.getDocumentElement().getNodeName());
  }

}
