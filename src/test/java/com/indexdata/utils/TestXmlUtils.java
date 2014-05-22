package com.indexdata.utils;

import java.io.IOException;
import java.io.InputStream;
import static org.junit.Assert.*;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class TestXmlUtils {
  
  @Test
  public void testMissingDTD() throws IOException, SAXException {
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
  
  private class NamespaceTestHandler implements ContentHandler {
    private int prefixMapCount = 0;
    private int elemCount = 0;

    @Override
    public void setDocumentLocator(Locator locator) {
    }

    @Override
    public void startDocument() throws SAXException {
    }

    @Override
    public void endDocument() throws SAXException {
      assertEquals(3, prefixMapCount);
      assertEquals(4, elemCount);
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws
      SAXException {
      if (prefixMapCount == 0) {
        assertEquals("", prefix);
        assertEquals("http://www.indexdata.com/ns", uri);
      }
      else if (prefixMapCount == 1) {
        assertEquals("bar", prefix);
        assertEquals("http://www.indexdata.com/ns2", uri);
      }
      else if (prefixMapCount == 2) {
        assertEquals("", prefix);
        assertEquals("http://www.indexdata.com/ns3", uri);
      }
      prefixMapCount++;
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
    }

    @Override
    public void startElement(String uri, String localName, String qName,
      Attributes atts) throws SAXException {
      if (elemCount == 0) {
        assertEquals("root", localName);
        assertEquals("root", qName);
        assertEquals("http://www.indexdata.com/ns", uri);
        assertEquals(0, atts.getLength());
      }
      else if (elemCount == 1) {
        assertEquals("tag", localName);
        assertEquals("bar:tag", qName);
        assertEquals("http://www.indexdata.com/ns2", uri);
        assertEquals(0, atts.getLength());
      }
      else if (elemCount == 2) {
        assertEquals("tag2", localName);
        assertEquals("tag2", qName);
        assertEquals("http://www.indexdata.com/ns3", uri);
        assertEquals(0, atts.getLength());
      }
      else if (elemCount == 3) {
        assertEquals("tag3", localName);
        assertEquals("tag3", qName);
        assertEquals("http://www.indexdata.com/ns", uri);
        assertEquals(0, atts.getLength());
      }
      elemCount++;
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws
      SAXException {
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws
      SAXException {
    }

    @Override
    public void processingInstruction(String target, String data) throws
      SAXException {
    }

    @Override
    public void skippedEntity(String name) throws SAXException {
    }
    
    
  }

  @Test
  public void testNamespaceSAX() throws SAXException, IOException {
    InputStream testFile = this.getClass().getClassLoader()
    .getResourceAsStream("xml/namespaces.xml");
    XmlUtils.read(new InputSource(testFile), new NamespaceTestHandler());
  }
}
