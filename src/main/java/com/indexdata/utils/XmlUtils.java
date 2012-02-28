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
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Some XML helper methods to hide DOM complexity.
 * Uses thread local variables to create Builders once per thread.
 * @author jakub
 */
public class XmlUtils {
    private static final ThreadLocal<DocumentBuilder> builderLocal =
    new ThreadLocal<DocumentBuilder>() {
        @Override
        protected DocumentBuilder initialValue() {
          try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setIgnoringElementContentWhitespace(true);
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
    
    public static Document parse(InputStream source) throws SAXException, IOException {
        return builderLocal.get().parse(source);
    }
    
    public static Document parse(String uri) throws SAXException, IOException {
        return builderLocal.get().parse(uri);
    }
    
    public static Document parse(StringReader reader) throws SAXException, IOException {
        return builderLocal.get().parse(new InputSource(reader));
    }
    
    public static Document parse(File file) throws SAXException, IOException {
        return builderLocal.get().parse(file);
    }

    public static void serialize(Node doc, OutputStream dest) throws TransformerException {
      serialize(doc, dest, null);
    }

    public static void serialize(Node doc, Writer writer) throws TransformerException {
      serialize(doc, writer, null);
    }
    
    public static void serialize(Node doc, OutputStream dest, Properties props) throws TransformerException {
      Transformer tf = transformerLocal.get();
      if (props != null) tf.setOutputProperties(props);
      tf.transform(new DOMSource(doc), new StreamResult(dest));
    }

    public static void serialize(Node doc, Writer writer, Properties props) throws TransformerException {
      Transformer tf = transformerLocal.get();
      if (props != null) tf.setOutputProperties(props);
      transformerLocal.get().transform(new DOMSource(doc), new StreamResult(writer));
    }

    /**
     * Escape five, basic XML entities.
     * @param s string to be escaped
     * @return xml-escaped string
     */
    public static String escape(String s) {
        StringBuilder result = new StringBuilder();
        StringCharacterIterator i = new StringCharacterIterator(s);
        char c =  i.current();
        while (c != CharacterIterator.DONE ){
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
     * @param startingPoint
     *          The context node
     * @param xPathString
     *          The search string
     * @return The node list found by the XPath
     * @throws StandardServiceException
     *           If XPath evaluation fails.
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

}
