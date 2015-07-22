/*
 * Copyright (c) 1995-2012, Index Data
 * All rights reserved.
 * See the file LICENSE for details.
 */
package com.indexdata.masterkey.pazpar2.client;

import com.indexdata.utils.XmlUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author jakub
 */
public class FieldMapper {
  public final static String NS_NS = "http://www.w3.org/2000/xmlns/";
  public final static String XSL_NS = "http://www.w3.org/1999/XSL/Transform";
  public final static String PZ2_NS = "http://www.indexdata.com/pazpar2/1.0";
  public final static String TMARC_NS = "http://www.indexdata.com/turbomarc";
  
  private final static String[] reservedPrefixes = {"xsl", "pz", "tmarc"};
  
  public enum MapType {
    MARC("base-tmarc"), XML("base-xml");
    private String baseXSLName;
    private MapType(String baseXSLName) {
      this.baseXSLName = baseXSLName;
    }
    public String getBaseXSLName() {
      return baseXSLName;
    }
  }
  
  public enum Directive {
    NAMESPACE("ns"), IMPORT("import");
    private String name;
    private Directive(String name) {
      this.name = name;
    }
    public static Directive fromString(String name) {
      for (Directive d : Directive.values()) {
        if (d.name.equalsIgnoreCase(name)) {
          return d;
        }
      }
      return null;
    }
  }
  
  public static class FieldMap {
    private String tag;
    private String name;
    private String subfields;
    private boolean include;

    public FieldMap(String tag, String name, String subfields, boolean include) {
      this.tag = tag;
      this.name = name;
      this.subfields = subfields;
      this.include = include;
    }

    public FieldMap(String tag, String name) {
      this.tag = tag;
      this.name = name;
    }

    public String getTag() {
      return tag;
    }

    public String getName() {
      return name;
    }

    public String getSubfields() {
      return subfields;
    }

    public boolean inlcudeSubfields() {
      return include;
    }
    
    @Override
    public String toString() {
      String str = tag;
      if (subfields != null) {
        str += "$" + (include ? "" : "!") + subfields;
      }
      str += " " + name;
      return str;
    }
  }
  
  public class ParsingException extends Exception {

    private static final long serialVersionUID = -673112951362595374L;

    public ParsingException(String message) {
      super(message);
    }
    
    public ParsingException(String message, Throwable cause) {
      super(message, cause);
    }
    
  }
  
  private ArrayList<FieldMap> fieldMaps;
  private MapType type;
  private List<String> imports;
  private Map<String,String> namespaces;

  public List<String> getImports() {
    return imports;
  }

  public ArrayList<FieldMap> getFieldMaps() {
    return fieldMaps;
  }

  public Map<String, String> getNamespaces() {
    return namespaces;
  }
  
  private void parseNamespace(String ns, int lineNo) throws ParsingException {
    int fws = ns.indexOf(" ");
    if (fws < 1) 
      throw new ParsingException("Malformed namespace directive: "+ns+" at line "+lineNo);
    String name = ns.substring(0, fws).trim();
    for (String v : reservedPrefixes) {
      if (v.equals(name)) throw new ParsingException("Reserved prefix '"+name+"' cannot be redefined");
    }
    String value = ns.substring(fws).trim();
    namespaces.put(name, value);
  }

  public FieldMapper(String input) throws ParsingException {
    String[] lines = input.split("\\n");
    fieldMaps = new ArrayList<FieldMap>(lines.length);
    namespaces = new TreeMap<String, String>();
    imports = new ArrayList<String>();
    for (int k = 0; k < lines.length; k++) {
      //make sure to trim 
      String line = lines[k].trim();
      if (line.isEmpty())
        continue;
      if (line.charAt(0) == '%') {
        //lines starting with '%' are directives
        String directive = line.substring(1).trim();
        //%dir name some value
        int fws = directive.indexOf(" ");
        if (fws < 1)
          throw new ParsingException("Malformed directive at line "+k+": "+line);
        String dName = directive.substring(0, fws).trim();
        String dValue = directive.substring(fws).trim();
        Directive d = Directive.fromString(dName);
        if (d == null)
          throw new ParsingException("Unknown directive '"+dName+"' at line "+k+": "+line);
        switch (d) {
          case IMPORT: imports.add(dValue); break;
          case NAMESPACE: parseNamespace(dValue, k); break;
          default: throw new 
            ParsingException("Unsupported directive '"+dName+"' at line "+k+": "+line);
        }
      } else {
        //xpath can contain whitespace but element name do not
        int lws = line.lastIndexOf(" ");
        if (lws < 1)
          throw new ParsingException("Delimiter at wrong position ("
            + lws + "): " + line);
        String selector = line.substring(0, lws).trim();
        String name = line.substring(lws).trim();
        //xpath cannot start with a digit
        MapType lineType = Character.isDigit(selector.charAt(0)) ? MapType.MARC
          : MapType.XML;
        if (type == null) {
          type = lineType;
        } else if (type != lineType) {
          throw new ParsingException("Unexpected line of type '"
            + lineType + "' in the fieldMap type " + type + ": " + line);
        }
        if (type == MapType.XML) {
          fieldMaps.add(new FieldMap(selector, name));
        } else if (type == MapType.MARC) {
          String[] pair = selector.split("\\$");
          if (pair.length != 2)
            throw new ParsingException("Malformed MARC selector (" + selector
              + "): " + line);
          String datafield = pair[0];
          String subfields = pair[1];
          boolean inc = true;
          if (!subfields.isEmpty()
            && subfields.charAt(0) == '!') {
            subfields = subfields.substring(1);
            inc = false;
          }
          if (subfields.equals("*"))
            subfields = "";
          fieldMaps.add(new FieldMap(datafield, name, subfields, inc));
        }
      }
    }
  }
  
  public Document getStylesheet() throws ParsingException {
    Document ss;
    Element tmpl = null, pzRecord = null;
    InputStream is = this.getClass().getResourceAsStream("/xsl/"+type.getBaseXSLName()+".xsl");
    Document defaults = null;
    if (is != null) {
      //log it
      try {
        defaults = XmlUtils.parse(is);
      } catch (SAXException se) {
        throw new ParsingException("Base stylesheet error", se);
      } catch (IOException ioe) {
        throw new ParsingException("Base stylesheet error", ioe);
      }
    }
    if (defaults != null) {
      ss = defaults;
      // for some reason this doesn't work wiht namespaces!!
      NodeList pzRecs = ss.getElementsByTagName("pz:record");
      if (pzRecs.getLength() != 1)
        throw new ParsingException("The default stylesheet must contain exactly one pz:record elem");
      pzRecord = (Element) pzRecs.item(0);
    } else {
      ss = XmlUtils.newDoc();
      Element root = ss.createElementNS(XSL_NS, "xsl:stylesheet");
      root.setAttributeNS(NS_NS, "xmlns:xsl", XSL_NS);
      root.setAttributeNS(NS_NS, "xmlns:pz", PZ2_NS);
      ss.appendChild(root);
      //output indent="yes" method="xml" version="1.0" encoding="UTF-8"
      Element out = ss.createElementNS(XSL_NS, "xsl:output");
      out.setAttribute("indent", "yes");
      out.setAttribute("method", "xml");
      out.setAttribute("version", "1.0");
      out.setAttribute("encoding", "UTF-8");
      root.appendChild(out);
      //template
      tmpl = ss.createElementNS(XSL_NS, "xsl:template");
      root.appendChild(tmpl);
      //tex > dev null
      Element txtTmpl = ss.createElementNS(XSL_NS, "xsl:template");
      txtTmpl.setAttribute("match", "text()");
      root.appendChild(txtTmpl);
    }
    //namespaces
    Element root = ss.getDocumentElement();
    for (Entry<String, String> e : namespaces.entrySet()) {
      root.setAttributeNS(NS_NS, "xmlns:" + e.getKey(), e.getValue());
    }
    //imports
    Node firstChild = root.getFirstChild();
    for (String href : imports) {
      Element impE = ss.createElementNS(XSL_NS, "xsl:import");
      impE.setAttribute("href", href);
      root.insertBefore(impE, firstChild);
    }
    switch (type) {
      case MARC: return getMarcStylesheet(tmpl, pzRecord);
      case XML: return getXmlStylesheet(tmpl, pzRecord);
      default: throw new ParsingException("Unsupported stylesheet type: " + type);
    }
  }
  
  private Document getXmlStylesheet(Element tmpl, Element pzRecord) throws ParsingException {
    Document ss = null;
    if (pzRecord == null) {
      ss = tmpl.getOwnerDocument();
      tmpl.setAttribute("match", "/");
      pzRecord = ss.createElementNS(PZ2_NS, "pz:record");
      tmpl.appendChild(pzRecord);
    } else {
      ss = pzRecord.getOwnerDocument();
    }
    for (FieldMap map : fieldMaps) {
      mapField(pzRecord, map, MapType.XML);
    }
    return ss;
  }
  
  private Document getMarcStylesheet(Element tmpl, Element pzRecord) throws ParsingException {
    Document ss = null;
    if (pzRecord == null) {
      ss = tmpl.getOwnerDocument();
      ss.getDocumentElement().setAttributeNS("http://www.w3.org/2000/xmlns/",
        "xmlns:tmarc",
        TMARC_NS);
      tmpl.setAttribute("match", "tmarc:r");
      pzRecord = ss.createElementNS(PZ2_NS, "pz:record");
      tmpl.appendChild(pzRecord);
    } else {
      ss = pzRecord.getOwnerDocument();
    }
    for (FieldMap map : fieldMaps) {
      mapField(pzRecord, map, MapType.MARC);
    }
    return ss;
  }
  
  private void mapField(Node root, FieldMap fm, MapType mt) throws ParsingException {
    Document doc = root.getOwnerDocument();
    //each datafield
    Element forEach = doc.createElementNS(XSL_NS, "xsl:for-each");
    if (mt.equals(MapType.MARC))
      forEach.setAttribute("select", "tmarc:d"+fm.tag);
    else if (mt.equals(MapType.XML))
      forEach.setAttribute("select", fm.tag);
    else
      throw new ParsingException("Unknown MapType: " + mt);
    root.appendChild(forEach);
    // meta container
    Element meta = doc.createElementNS(PZ2_NS, "pz:metadata");
    meta.setAttribute("type", fm.getName());
    forEach.appendChild(meta);
    //each subfield
    Element parent = null;
    if (fm.subfields == null) {
      // xml-like field with no subfields
      parent = meta;
    } else {
      Element forEach2 = doc.createElementNS(XSL_NS, "xsl:for-each");
      StringBuilder sb = new StringBuilder("*");
      String op = "[";
      for (int i = 0; i < fm.subfields.length(); i++) {
        char sf = fm.subfields.charAt(i);
        if (fm.include) {
          sb.append(op).append("self::tmarc:s").append(sf).append("");
          op = " or ";
        } else {
          sb.append(op).append("not(self::tmarc:s").append(sf).append(")");
          op = " and ";
        }
      }
      if (!fm.subfields.isEmpty()) sb.append("]");
      forEach2.setAttribute("select", sb.toString());
      meta.appendChild(forEach2);
      parent = forEach2;
    }
    Element valueOf = doc.createElement("xsl:value-of");
    valueOf.setAttribute("select", ".");
    parent.appendChild(valueOf);
  }
  
  @Override
  public String toString() {
    if (fieldMaps != null) {
      String str = "";
      for (FieldMap map : fieldMaps) {
        str += map;
      }
      return str;
    } else {
      return "EMPTY";
    }
  }
}
