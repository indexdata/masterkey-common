/*
 * Copyright (c) 1995-2008, Index Data
 * All rights reserved.
 * See the file LICENSE for details.
 */
package com.indexdata.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.Writer;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.log4j.Logger;
import org.ccil.cowan.tagsoup.AttributesImpl;
import org.ccil.cowan.tagsoup.ElementType;
import org.ccil.cowan.tagsoup.Schema;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Some XML helper methods to hide DOM complexity. Uses thread local variables
 * to create Builders once per thread.
 *
 * @author jakub
 */
public class XmlUtils {
  private static final Logger logger = Logger.getLogger("com.indexdata.masterkey");
  
  private static final ThreadLocal<DocumentBuilder> builderLocal =
    new ThreadLocal<DocumentBuilder>() {
      @Override
      protected DocumentBuilder initialValue() {
        try {
          DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
          factory.setIgnoringElementContentWhitespace(true);
          factory.setNamespaceAware(true);
          //turn off DTD validation and external entities
          factory.setValidating(false);
          try {
            factory.setFeature("http://xml.org/sax/features/validation", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
          } catch (ParserConfigurationException pce) {
            logger.warn("Error setting parser feature", pce);
          }
          return factory.newDocumentBuilder();
        } catch (ParserConfigurationException pce) {
          throw new Error(pce);
        }
      }
    };
  
  private static final ThreadLocal<Transformer> transformerLocal =
    new ThreadLocal<Transformer>() {
      @Override
      protected Transformer initialValue() {
        try {
          return TransformerFactory.newInstance().newTransformer();
        } catch (TransformerConfigurationException tce) {
          throw new Error(tce);
      }
    }
  };
  
  private static final ThreadLocal<SAXParser> saxLocal = 
    new ThreadLocal<SAXParser>() {
    @Override
    protected SAXParser initialValue() {
      return createSAXParser(null); //default sax parser
    }    
  };
  
  private static class NSAwareSchema extends Schema {
    protected NSElementType rootType;
    protected final Map<String,String> namespaces = new HashMap<String,String>();
    protected final Map<String,NSElementType> elementTypes = new HashMap<String, NSElementType>();
    
    public NSAwareSchema() {
      elementType("<pcdata>", M_EMPTY, M_PCDATA, 0);
      elementType("<root>", M_ROOT, M_EMPTY, 0);
    }

    @Override
    public void elementType(String name, int model, int memberOf, int flags) {
      NSElementType e = new NSElementType(name, model, memberOf, flags);
      elementTypes.put(name.toLowerCase(), e);
      if (memberOf == M_ROOT) {
        rootType = e;
      }
    }

    @Override
    public ElementType getElementType(String name) {
      return elementTypes.get(name);
    }

    @Override
    public ElementType rootElementType() {
      return rootType;
    }

    @Override
    public String getURI() {
      return rootType != null ? rootType.namespace : "";
    }
    
    private class NSElementType extends ElementType {
      protected String namespace;
      public final static String XML_NS = "http://www.w3.org/XML/1998/namespace";

      public NSElementType(String name, int model, int memberOf, int flags) {
        //because of the incredibly awful design of the TagSoup super class
        //we need to use the trick with nested classes
        super(name, model, memberOf, flags, NSAwareSchema.this);
      }

      @Override
      public String namespace(String name, boolean isAttr) {
        int colon = name.indexOf(':');
        if (colon == -1) {
          return isAttr ? "" : schema().getURI();
        }
        String prefix = name.substring(0, colon);
        String ns;
        if (prefix.equals("xml")) {
          ns = XML_NS;
        } else {
          ns = schema().namespaces.containsKey(prefix) 
            ? schema().namespaces.get(prefix)
            : schema().getURI();
        }
        if (!isAttr) {//constructor call
          namespace = ns;
        } 
        return ns;
      }

      @Override
      public void setAttribute(AttributesImpl atts, String name, String type, String value) {
        if (name.equals("xmlns")) {
          namespace = value;
        } else if (name.startsWith("xmlns:")) {
          int colon = name.indexOf(':');
          String prefix = name.substring(colon+1);
          schema().namespaces.put(prefix, value);
        }
        super.setAttribute(atts, name, type, value);
      }

      @Override
      public String namespace() {
        return namespace;
      }

      @Override
      public NSAwareSchema schema() {
        return NSAwareSchema.this;
      }

    }
  }
  

  private static final ThreadLocal<SAXParser> tsLocal = 
    new ThreadLocal<SAXParser>() {
    @Override
    protected SAXParser initialValue() {
      SAXParser p = createSAXParser("org.ccil.cowan.tagsoup.jaxp.SAXFactoryImpl"); //tagsoup parser
      try {
        p.setProperty("http://www.ccil.org/~cowan/tagsoup/properties/schema", new NSAwareSchema());
      } catch (Exception se) {
        logger.warn("Cannot override tag soup schema", se);
      }
      return p;
    }    
  };
  
  private static SAXParser createSAXParser(String className) throws Error {
    try {
      SAXParserFactory factory = className == null 
        ? SAXParserFactory.newInstance()
        : SAXParserFactory.newInstance(className, null);
      factory.setNamespaceAware(true);
      factory.setValidating(false);
      try {
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
        //the follwing commented-out features should not be used
        //but are kept for information purposes
        //factory.setFeature("http://xml.org/sax/features/namespaces", false); //raise startPrefixMapping for ns?
        //factory.setFeature("http://xml.org/sax/features/namespace-prefixes", true); //include ns mappings in attrs
        //factory.setFeature("http://xml.org/sax/features/xmlns-uris", true); //report ns uri for xmlns
        factory.setFeature("http://xml.org/sax/features/validation", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      } catch (Exception e) {
        logger.warn("Error setting parser feature", e);
      }
      return factory.newSAXParser();
    } catch (ParserConfigurationException pce) {
      throw new Error(pce);
    } catch (SAXException se) {
      throw new Error(se);
    }
}

  private XmlUtils() {
  }

  public static Document newDoc() {
    return builderLocal.get().newDocument();
  }

  public static Document newDoc(String rootNode) {
    Document doc = newDoc();
    Element root = doc.createElement(rootNode);
    doc.appendChild(root);
    return doc;
  }

  public static Document parse(InputStream source) throws SAXException,
    IOException {
    return builderLocal.get().parse(source);
  }

  public static Document parse(String uri) throws SAXException, IOException {
    return builderLocal.get().parse(uri);
  }

  public static Document parse(StringReader reader) throws SAXException,
    IOException {
    return builderLocal.get().parse(new InputSource(reader));
  }

  public static Document parse(File file) throws SAXException, IOException {
    return builderLocal.get().parse(file);
  }
  
  public static void read(InputSource is, DefaultHandler dh) throws SAXException, IOException {
    saxLocal.get().parse(is, dh);
  }
  
  public static void read(InputSource is, ContentHandler ch) throws SAXException, IOException {
    XMLReader reader = saxLocal.get().getXMLReader();
    reader.setContentHandler(ch);
    reader.parse(is);
  }
  
  public static void read(InputSource is, ContentHandler ch, boolean useTagSoup) throws SAXException, IOException {
    ThreadLocal<SAXParser> localParser = useTagSoup ? tsLocal : saxLocal;
    XMLReader reader = localParser.get().getXMLReader();
    reader.setContentHandler(ch);
    reader.parse(is);
  }

  public static void serialize(Node doc, OutputStream dest) throws
    TransformerException {
    serialize(doc, dest, null);
  }

  public static void serialize(Node doc, Writer writer) throws
    TransformerException {
    serialize(doc, writer, null);
  }

  public static void serialize(Node doc, OutputStream dest, Properties props)
    throws TransformerException {
    Transformer tf = transformerLocal.get();
    if (props != null)
      tf.setOutputProperties(props);
    tf.transform(new DOMSource(doc), new StreamResult(dest));
  }

  public static void serialize(Node doc, Writer writer, Properties props) throws
    TransformerException {
    Transformer tf = transformerLocal.get();
    if (props != null)
      tf.setOutputProperties(props);
    transformerLocal.get().transform(new DOMSource(doc),
      new StreamResult(writer));
  }

  /**
   * Escape five, basic XML entities.
   *
   * @param s string to be escaped
   * @return xml-escaped string
   */
  public static String escape(String s) {
    StringBuilder result = new StringBuilder();
    StringCharacterIterator i = new StringCharacterIterator(s);
    char c = i.current();
    while (c != CharacterIterator.DONE) {
      switch (c) {
        case '<':
          result.append("&lt;");
          break;
        case '>':
          result.append("&gt;");
          break;
        case '"':
          result.append("&quot;");
          break;
        case '\'':
          result.append("&apos;");
          break;
        case '&':
          result.append("&amp;");
          break;
        default:
          result.append(c);
      }
      c = i.next();
    }
    return result.toString();
  }

  public static Node appendTextNode(Node parent, String tagName, String text) {
    Document doc = parent.getOwnerDocument();
    Node newNode = doc.createElement(tagName);
    newNode.setTextContent(text);
    parent.appendChild(newNode);
    return newNode;
  }

  /**
   * Gets a list of nodes by XPath from given starting point
   *
   * @param startingPoint The context node
   * @param xPathString The search string
   * @return The node list found by the XPath
   * @throws StandardServiceException If XPath evaluation fails.
   */
  public static NodeList getNodeList(Object startingPoint, String xPathString)
    throws XPathExpressionException {
    NodeList nodeList = null;
    XPathFactory factory = XPathFactory.newInstance();
    XPath xPath = factory.newXPath();
    XPathExpression expr = xPath.compile(xPathString);
    nodeList = (NodeList) expr.evaluate(startingPoint, XPathConstants.NODESET);
    return nodeList;
  }

  /**
   * Gets a list of nodes by XPath from given starting point and resolve namespace
   * prefixes using the document itself.
   *
   * @param startingPoint The context node
   * @param xPathString The search string
   * @return The node list found by the XPath
   * @throws StandardServiceException If XPath evaluation fails.
   */
  public static NodeList getNodeListNS(Node startingPoint, String xPathString)
    throws XPathExpressionException {
    Document owner = startingPoint.getNodeType() == Node.DOCUMENT_NODE ?
      (Document) startingPoint : startingPoint.getOwnerDocument();
    NodeList nodeList = null;
    XPathFactory factory = XPathFactory.newInstance();
    XPath xPath = factory.newXPath();
    xPath.setNamespaceContext(
      new UniversalNamespaceResolver(owner));
    XPathExpression expr = xPath.compile(xPathString);
    nodeList = (NodeList) expr.evaluate(startingPoint, XPathConstants.NODESET);
    return nodeList;
  }

  private static class UniversalNamespaceResolver implements NamespaceContext {
    // the delegate
    private Document sourceDocument;

    /**
     * This constructor stores the source document to search the namespaces in
     * it.
     *
     * @param document source document
     */
    public UniversalNamespaceResolver(Document document) {
      sourceDocument = document;
    }

    /**
     * The lookup for the namespace uris is delegated to the stored document.
     *
     * @param prefix to search for
     * @return uri
     */
    @Override
    public String getNamespaceURI(String prefix) {
      if (prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
        return sourceDocument.lookupNamespaceURI(null);
      } else {
        return sourceDocument.lookupNamespaceURI(prefix);
      }
    }

    @Override
    public String getPrefix(String namespaceURI) {
      return sourceDocument.lookupPrefix(namespaceURI);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Iterator getPrefixes(String namespaceURI) {
      // not implemented yet
      return null;
    }
  }

}
