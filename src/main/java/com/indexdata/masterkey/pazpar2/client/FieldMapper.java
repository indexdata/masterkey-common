/*
 * Copyright (c) 1995-2012, Index Datassss
 * All rights reserved.
 * See the file LICENSE for details.
 */
package com.indexdata.masterkey.pazpar2.client;

import com.indexdata.utils.XmlUtils;
import java.util.ArrayList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author jakub
 */
public class FieldMapper {
  
  public final static String XSL_NS = "http://www.w3.org/1999/XSL/Transform";
  public final static String PZ2_NS = "http://www.indexdata.com/pazpar2/1.0";
  public final static String TMARC_NS = "http://www.indexdata.com/turbomarc";
  
  public enum MapType {
    MARC, XML;
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

    public ParsingException(String message) {
      super(message);
    }
    
  }
  
  private ArrayList<FieldMap> fieldMaps;
  private MapType type;

  public FieldMapper(String input) throws Exception {
     String[] lines = input.split("\\n");
     fieldMaps = new ArrayList<FieldMap>(lines.length);
     for (int k = 0; k < lines.length; k++) {
       //make sure to trim 
       String line = lines[k].trim();
        //xpath can contain whitespace but element name do not
        int lws = line.lastIndexOf(" ");
        if (lws < 1) throw new ParsingException("Delimiter at wrong position ("
          +lws+"): "+line);
        String selector = line.substring(0, lws).trim();
        String name = line.substring(lws).trim();
        //xpath cannot start with a digit
        MapType lineType = Character.isDigit(selector.charAt(0)) ? MapType.MARC : MapType.XML;
        if (type == null) {
          type = lineType;
        } else if (type != lineType) {
          throw new ParsingException("Unexpected line of type '"
            +lineType+"' in the fieldMap type "+type+": "+line);
        }
        if (type == MapType.XML) {
          fieldMaps.add(new FieldMap(selector, name));
        } else if (type == MapType.MARC) {
          String[] pair = selector.split("\\$");
          if (pair.length != 2) throw new 
            ParsingException("Malformed MARC selector ("+selector+"): "+line);
          String datafield = pair[0];
          String subfields = pair[1];
          boolean inc = true;
          if (!subfields.isEmpty()
            && subfields.charAt(0) == '!') {
            subfields = subfields.substring(1);
            inc = false;
          }
          if (subfields.equals("*")) subfields = "";
          fieldMaps.add(new FieldMap(datafield, name, subfields, inc));
        }
      }  
  }
  
  public Document getStylesheet(Document defaults) throws ParsingException {
    Document ss = null;
    Element tmpl = null, pzRecord = null;
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
      root.setAttributeNS("http://www.w3.org/2000/xmlns/",
        "xmlns:xsl",
        XSL_NS);
      root.setAttributeNS("http://www.w3.org/2000/xmlns/",
        "xmlns:pz",
        PZ2_NS);
      root.setAttribute("version", "1.0");
      ss.appendChild(root);
      tmpl = ss.createElementNS(XSL_NS, "xsl:template");
      root.appendChild(tmpl);
    }
    switch (type) {
      case MARC: return getMarcStylesheet(tmpl, pzRecord);
      case XML: return getXmlStylesheet(tmpl, pzRecord);
      default: throw new ParsingException("Unknown stylesheet type: " + type);
    }
  }
  
  private Document getXmlStylesheet(Element tmpl, Element pzRecord) throws ParsingException {
    Document ss = null;
    if (pzRecord == null) {
      ss = tmpl.getOwnerDocument();
      tmpl.setAttribute("match", "Result");
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
