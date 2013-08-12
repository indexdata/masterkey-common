/*
 * Copyright (c) 1995-2008, Index Data
 * All rights reserved.
 * See the file LICENSE for details.
 */
package com.indexdata.masterkey.pazpar2.client;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.indexdata.torus.Record;
import com.indexdata.torus.Records;
import com.indexdata.torus.layer.SearchableTypeLayer;
import com.indexdata.utils.PerformanceLogger;
import com.indexdata.utils.XmlUtils;
import java.io.IOException;
import java.io.StringReader;
import org.w3c.dom.Node;

/**
 * Manages Pazpar2Settings
 * <ul>
 * <li>Loads target settings from a realm in the torus</li>
 * <li>Sets record filtering settings on all or selected targets in the realm</li>
 * <li>Sends settings to Pazpar2
 * </ul>
 * </ul>
 * 
 * TODO: THIS CLASS IS CONFUSED ABOUT IT'S ROLE (CONTROLER OR CONTROLEE?) AND
 * DEPENDENCY TO PAZPAR2 CLIENT OBJECT. THE LOGIC FOR FETCHING TARGETS IS
 * SCATTERED IN TWO PLACES (FROM SEARCHABLES AND IN THE TARGET FILTER APPLY
 * METHOD)
 */
public class Pazpar2Settings {
  //avoid re-parssing
  private static class Setting {
    String string;
    Document xml;
    Setting(Document xml) {
      this.xml = xml;
      this.string = "[XML encoded]"; //possibly serialize the XML here
    }
    Setting(String string) {
      this.string = string;
    }
  }
  private Map<String, Map<String, Setting>> settings = new HashMap<String, Map<String, Setting>>();
  private static Logger logger = Logger.getLogger(Pazpar2Settings.class);
  private Pazpar2ClientConfiguration cfg;
  Pattern hostPortRegEx = Pattern.compile(".*:[0-9]*$");
  protected Pazpar2Settings(Pazpar2ClientConfiguration cfg) {
    this.cfg = cfg;
  }
  
  public static Pazpar2Settings fromSearchables(Records records, Pazpar2ClientConfiguration cfg) {
    Pazpar2Settings setts = new Pazpar2Settings(cfg);
    setts.loadSearchables(records);
    return setts;
  }

  /**
   * Loads torus target setting records.
   * 
   * @param record target setting record
   */
  public void loadSearchables(Records records) {
    for (Record rec : records.getRecords()) {
      loadSearchable(rec);
    }
  }
   
  /**
   * Loads torus target setting record (enveloped, first layer).
   * 
   * @param record target setting record
   */
  public void loadSearchable(Record record) {
    if (record.getLayers().isEmpty()) {
      logger.warn("A searchable record is missing any data layers and will be ignored");
      return;
    }
    SearchableTypeLayer l = (SearchableTypeLayer) record.getLayers().get(0);
    loadSearchable(l);
  }
  
  
  /**
   * Loads torus target setting record (layer).
   * 
   * @param record target setting record
   */
  public void loadSearchable(SearchableTypeLayer l) {
    //build url
    String url = null, auth = null;
    boolean isCf = false;
    if (l.getZurl() != null && !l.getZurl().isEmpty()) {   
      StringBuffer urlBuilder = new StringBuffer(l.getZurl());
      // Ends with a port number, append a path
      if (hostPortRegEx.matcher(l.getZurl()).matches()) {
        urlBuilder.append("/");
      }
      //append CF params, select appropriate authentication
      isCf = checkAndAppendCfParams(l, urlBuilder);
      auth = isCf ? l.getCfAuth() : l.getAuthentication();
      //append RDP params
      if (!isCf) {
        String extraPath = encodeRichDatabaseParameters("targetmap", 
          l.getOtherElements());  
        if (extraPath != null) {
          urlBuilder.append(",").append(extraPath);
          logger.debug("Zurl appended with Rich Database Parameters: " + urlBuilder.toString());
        }
      }
      url = urlBuilder.toString();
    }

    // we either use the above construct URL as the target identifier or the
    // explicit ID -- service proxy setting
    String id = "yes".equalsIgnoreCase(cfg.USE_OPAQUE_ID)
      && l.getId() != null && !l.getId().isEmpty()
      ? l.getId()
      : url;
    
    if (id == null || id.isEmpty()) {
      logger.warn("Ignoring target specified in the configuration due to missing ID "
        + "("+l.getId()+") or URL ("+l.getZurl()+")");
      return;
    }
    List<String> excludeList = new LinkedList<String>();
    setSetting(id, "pz:authentication", auth, excludeList);
    setSetting(id, "pz:url", url, excludeList);
    setSetting(id, "pz:name", l.getName(), excludeList);
    setSetting(id, "pz:xslt", l.getTransform(), excludeList);
    //fieldMap overrides xslt
    if (l.getLiteralTransform() != null && !l.getLiteralTransform().isEmpty()) {
      try {
        Document lT = XmlUtils.parse(new StringReader(l.getLiteralTransform()));
        setXMLSetting(id, "pz:xslt", lT);
      } catch (Exception ioe) {
        logger.error("Cannot parse literalTransform", ioe);
      }
    } else if (l.getFieldMap() != null && !l.getFieldMap().isEmpty()) {
      try {
        FieldMapper mapper = new FieldMapper(l.getFieldMap());
        setXMLSetting(id, "pz:xslt", mapper.getStylesheet());
      } catch (FieldMapper.ParsingException pe) {
        logger.error("Cannot parse fieldMap - " + pe.getMessage());
      }
    }      
    setSetting(id, "pz:elements", l.getElementSet(), excludeList);
    setSetting(id, "pz:queryencoding", l.getQueryEncoding(), excludeList);
    setSetting(id, "pz:requestsyntax", l.getRequestSyntax(), excludeList);

    if (l.getRequestSyntax() != null) {
      boolean useTmarc = !"no".equalsIgnoreCase(cfg.USE_TURBO_MARC);
      if (l.getRequestSyntax().equalsIgnoreCase("xml")) {
        if (l.getRecordEncoding() != null) {
          //we force txml to deal with xml-embedded marc records (they will end up tmarc)
          String ns = "txml; " + l.getRecordEncoding();
          setSetting(id, "pz:nativesyntax", ns, excludeList);
          logger.debug("Nativesyntax chosen for target [" + url + "] ("+ns+")");
        } else {
          logger.debug("No nativesyntax chosen for target [" + url + "] (xml)");
        }
      } else if (useTmarc
          && (l.getRequestSyntax().toLowerCase().contains("opac")
            || l.getRequestSyntax().toLowerCase().contains("marc"))
          && (l.getTransform() == null || l.getTransform().contains("tmarc") 
            || l.getTransform().contains("turbomarc"))) {
        String encoding = l.getRecordEncoding() != null ? l.getRecordEncoding() : "MARC8";
        setSetting(id, "pz:nativesyntax", "txml;" + encoding, excludeList);
        logger.debug("Using Turbo MARC for target [" + url + "]");
      } else {
        String encoding = l.getRecordEncoding() != null ? l.getRecordEncoding() : "MARC8";
        setSetting(id, "pz:nativesyntax", "iso2709;" + encoding, excludeList);
        logger.debug("Using iso2709;" + encoding + " for target [" + url + "]");
      }
    }

    setPrefixedSettings(id, "cclmap", l.getOtherElements());
    //the following are the default that we support, the key is configurable
    //through SP config
    setSetting(id, "pz:cclmap:"+cfg.CCLMAP_TERM_KEY,
      l.getCclMapTerm(), cfg.CCLMAP_TERM, excludeList);
    setSetting(id, "pz:cclmap:"+cfg.CCLMAP_SU_KEY, 
      l.getCclMapSu(), cfg.CCLMAP_SU, excludeList);
    setSetting(id, "pz:cclmap:"+cfg.CCLMAP_AU_KEY, 
      l.getCclMapAu(), cfg.CCLMAP_AU, excludeList);
    setSetting(id, "pz:cclmap:"+cfg.CCLMAP_TI_KEY, 
      l.getCclMapTi(), cfg.CCLMAP_TI, excludeList);
    setSetting(id, "pz:cclmap:"+cfg.CCLMAP_ISBN_KEY, 
      l.getCclMapIsbn(), cfg.CCLMAP_ISBN, excludeList);
    setSetting(id, "pz:cclmap:"+cfg.CCLMAP_ISSN_KEY, 
      l.getCclMapIssn(), cfg.CCLMAP_ISSN, excludeList);
    setSetting(id, "pz:cclmap:"+cfg.CCLMAP_JT_KEY, 
      l.getCclMapJournalTitle(), cfg.CCLMAP_JT, excludeList);
    setSetting(id, "pz:cclmap:"+cfg.CCLMAP_DATE_KEY, 
      l.getCclMapDate(), cfg.CCLMAP_DATE, excludeList);
    //'term' is magic CCL index that has to be defined even when the actual key
    //has been redefined
    if (!"term".equals(cfg.CCLMAP_TERM_KEY))
      setSetting(id, "pz:cclmap:term", l.getCclMapTerm(), cfg.CCLMAP_TERM, excludeList);

    setPrefixedSettings(id, "facetmap", l.getOtherElements());

    /*
     * Values: Attribute list or cclmap reference for how to query when
     * limiting a field
     */
    setPrefixedSettings(id, "limitmap", l.getOtherElements());

    setPrefixedSettings(id, "sortmap", l.getOtherElements());

    setSetting(id, "pz:sru", l.getSRU(), excludeList);
    setSetting(id, "pz:sru_version", l.getSruVersion(), excludeList);

    setSetting(id, "pz:apdulog", l.getApduLog(), excludeList);

    setSetting(id, "pz:termlist_term_count", l.getTermlistTermCount(), excludeList);
    setSetting(id, "pz:termlist_term_sort", l.getTermlistTermSort(), excludeList);
    setSetting(id, "pz:termlist_term_factor", l.getTermlistUseTermFactor(), null, excludeList);

    setSetting(id, "pz:preferred", l.getPreferredTarget(), excludeList);
    setSetting(id, "pz:block_timeout", l.getBlockTimeout(),excludeList);
    setSetting(id, "pz:pqf_prefix", l.getPqfPrefix(), excludeList);
    setSetting(id, "pz:piggyback", l.getPiggyback(), excludeList);
    setSetting(id, "pz:maxrecs", l.getMaxRecords(), excludeList);
    setSetting(id, "pz:extra_args", l.getExtraArgs(), excludeList);
    setSetting(id, "pz:query_syntax", l.getQuerySyntax(), excludeList);
    if (!isCf) setSetting(id, "pz:zproxy", l.getCfProxy(), excludeList);

    setSetting(id, "url_recipe", l.getUrlRecipe(), excludeList);
    setSetting(id, "category", l.getCategories(), excludeList);
    setSetting(id, "medium", l.getMedium(), excludeList);
    setSetting(id, "comment", l.getComment(), excludeList);
    setSetting(id, "explode", l.getExplode(), excludeList);
    setSetting(id, "use_url_proxy", l.getUseUrlProxy(), "0", excludeList);
    setSetting(id, "use_thumbnails", l.getUseThumbnails(), "1", excludeList);
    setSetting(id, "secondary_request_syntax", l.getSecondaryRequestSyntax(), null, excludeList);
    setSetting(id, "full_text_target", l.getFullTextTarget(), "NO", excludeList);
    setSetting(id, "place_holds", l.getPlaceHolds(), "no", excludeList);

    /* Now set all other pz_<name> as pz:<name> */
    setPzSettings(id, l.getOtherElements(), excludeList);

  }

  /*
   * Still the "old" way of encoding cfTarget parameters. Should move to encode
   * Rich Database Parameters or we should start using the more general target
   * map settings, which already uses this.
   */
  private boolean checkAndAppendCfParams(SearchableTypeLayer l, StringBuffer zurl) {
    if (l.getCfAuth() != null && !l.getCfAuth().isEmpty()) {
      // this is a CF target, build the DB name
      Map<String, String> params = new HashMap<String, String>();
      if (l.getAuthentication() != null) {
	String[] auths = l.getAuthentication().split("/");
	if (auths.length > 1) {
	  params.put("user", auths[0]);
	  params.put("password", auths[1]);
	}
      }
      if (l.getCfSubDB() != null) {
	params.put("subdatabase", l.getCfSubDB());
      }
      if (l.getCfProxy() != null) {
	params.put("proxy", l.getCfProxy());
      }
      //all others settings prefixed with cf_ are encoded too
      if (l.getOtherElements() != null) {
        for (Object obj : l.getOtherElements()) {
          if (obj instanceof Element) {
            Element element = (Element) obj;
            if (element.getTagName().startsWith("cf_")) {
              params.put(element.getTagName().substring(3), 
                element.getTextContent());
            }
          }
        }
      }
      String sep = "?";
      for (Entry<String, String> e : params.entrySet()) {
	try {
	  zurl.append(sep);
	  zurl.append(URLEncoder.encode(e.getKey(), "UTF-8")
            .replace("+", "%20"));
	  zurl.append("=");
	  zurl.append(URLEncoder.encode(e.getValue(), "UTF-8")
            .replace("+", "%20"));
	  sep = "&";
	} catch (UnsupportedEncodingException uee) {
	  logger.warn("Cannot encode CF parameter (" + e.getKey() + "=" + e.getValue() + ", "
	      + uee.getMessage());
	}
      }
      logger.debug("CF target zurl modified to " + zurl);
      return true;
    } else {
      return false;
    }
  }

  private String encodeRichDatabaseParameters(String mapPrefix, List<Object> otherElements) {
    if (otherElements == null) return null;
    StringBuffer richDatabaseParameters = new StringBuffer("");
    for (Object obj : otherElements) {
      if (obj instanceof Element) {
	Element element = (Element) obj;
	if (element.getTagName().startsWith(mapPrefix + "_")) {
	  String parameterName = element.getTagName().substring(mapPrefix.length() + 1);
	  // logger.trace("Parameter for " + mapPrefix + ": " + parameterName + " Value: " + element.getTextContent());
	  if (richDatabaseParameters.length() > 0)
	    richDatabaseParameters.append("&");
	  try {
	    richDatabaseParameters.append(URLEncoder.encode(parameterName, "UTF-8"));
	    richDatabaseParameters.append("=");
	    richDatabaseParameters.append(URLEncoder.encode(element.getTextContent(), "UTF-8"));
	  } catch (UnsupportedEncodingException uee) {
	    logger.warn("Cannot encode CF parameter (" + parameterName + "="
		+ element.getTextContent() + ", " + uee.getMessage());
	  }
	}
      }
    }
    // TODO move the actual append out.
    // So we can use another strategy with these. (Add as query part, add to headers)
    if (richDatabaseParameters.length() > 0) {
      String result = richDatabaseParameters.toString();
      logger.debug("Rich Database Parameters: " + result);
      return result;
    }
    return null;
  }
  
  public Document getXMLSetting(String targetId, String key) {
    Map<String, Setting> setts = settings.get(targetId);
    if (setts == null) return null;
    Setting s = setts.get(key);
    return s != null ? s.xml : null;
  }
  
  public boolean setXMLSetting(String targetId, String key, Document value) {
    if (value == null) return false;
    Map<String, Setting> setts = settings.get(targetId);
    if (setts == null) {
      setts = new HashMap<String, Setting>();
      settings.put(targetId, setts);
    }
    setts.put(key, new Setting(value));
    return true;
  }

  public boolean setSetting(String targetId, String key, String value, List<String> excludeList) {
    return setSetting(targetId, key, value, null, excludeList);
  }

  /**
   * Sets the property 'key' of the given 'targetId' to 'value'. Falls back
   * to 'defaultValue' if 'value' is null and no setting already exists. 
   * Will skip the setting altogether if value and 'defaultValue' are both null.
   * 
   * @param targetId
   *          The target to define a setting for
   * @param key
   *          The name of the setting
   * @param value
   *          The value of the setting
   * @param defaultValue
   *          Fall-back value if 'value' is null
   */
  public boolean setSetting(String targetId, String key, String value, String defaultValue, List<String> excludeList) {
    String val = ((value != null && !value.isEmpty()) ? value : defaultValue);
    if (val != null) {
      Map<String, Setting> setts = settings.get(targetId);
      if (setts != null) {
        //partial application of setting requires that default values are only
        //applied when no settings have been applied previously
        if ((value == null || value.isEmpty()) 
          //this will allow fallling back things new Setting(null), new 
          && (setts.get(key) != null && setts.get(key).string != null 
          && !setts.get(key).string.isEmpty()))
          return false;
      } else {
	setts = new HashMap<String, Setting>();
	settings.put(targetId, setts);
	if (excludeList != null)
	  excludeList.add(key);
      }
      if (logger.isDebugEnabled())
	logger.debug(new StringBuffer("setting on ").append(targetId).append(": ").append(key)
	    .append(":").append(val).toString());
      setts.put(key, new Setting(val));
      return true;
    }
    return false;
  }

  public String getSetting(String targetId, String key) {
    Map<String, Setting> targetSetts = settings.get(targetId);
    if (targetSetts == null) {
      return null;
    }
    Setting s = targetSetts.get(key);
    return s != null ? s.string : null;
  }

  /**
   * setPrefixedSetttings: set a value of maps based on prefix
   */
  protected void setPrefixedSettings(String targetId, String mapPrefix, List<Object> elements) {
    if (elements == null) return;
    for (Object obj : elements) {
      if (obj instanceof Element) {
	Element element = (Element) obj;
	if (element.getTagName().startsWith(mapPrefix + "_")) {
	  String pzName = "pz:" + element.getTagName().replace("_", ":");
	  setSetting(targetId, pzName, element.getTextContent(), null);
	}
      }
    }
  }

  /**
   * setPrefixedSetttings: set a value of maps based on prefix
   */
  protected void setPzSettings(String targetId, List<Object> elements, List<String> excludes) {
    if (elements == null) return;
    for (Object obj : elements) {
      if (obj instanceof Element) {
	Element element = (Element) obj;
	if (element.getTagName().startsWith("pz_")) {
	  String pzName = element.getTagName().replaceFirst("pz_", "pz:");
	  if (!excludes.contains(pzName))
	    setSetting(targetId, pzName, element.getTextContent(), null); 
	  else {
	    logger.warn("Ignored " + pzName + "=" + element.getTextContent());
	  }
	}
      }
    }
  }

  
  
  public String encode()
      throws UnsupportedEncodingException {
    long startTime = PerformanceLogger.start("  >ENCSETTS", "Encode Target Settings, "
	+ ((settings != null) ? settings.keySet().size() : 0) + " targets ");
    StringBuilder encodedBuf = new StringBuilder("");
    String sep = "";
    for (String targetId : settings.keySet()) {
      for (String settingName : settings.get(targetId).keySet()) {
	Setting setting = settings.get(targetId).get(settingName);
        //can't URL encode XML settings
        if (setting.xml != null) continue;
	String settingValue = setting.string == null ? "" : setting.string;
	encodedBuf.append(sep);
	encodedBuf.append(URLEncoder.encode(settingName, "UTF-8"));
	encodedBuf.append(URLEncoder.encode("[", "UTF-8"));
	encodedBuf.append(URLEncoder.encode(targetId, "UTF-8"));
	encodedBuf.append(URLEncoder.encode("]", "UTF-8"));
	encodedBuf.append("=");
	encodedBuf.append(URLEncoder.encode(settingValue, "UTF-8"));
	sep = "&";
      }
    }
    String encoded = encodedBuf.toString();
    PerformanceLogger.finish("  <ENCSETTS DONE", "Encode Target Settings", startTime);
    return encoded.equals("") ? null : encoded;
  }

  /**
   * Encode settings to an XML
   * 
   * @return XML Document with all target settings
   * @throws Error
   */
  public Document toXml(Element parent) throws Error {
    Document doc = null;
    Element root = null;
    if (parent != null) {
      doc = parent.getOwnerDocument();
      root = parent;
    } else {
      doc = XmlUtils.newDoc("settings");
      root = doc.getDocumentElement();
    }
    root.setAttribute("target", "*");
    for (String targetId : settings.keySet()) {
      for (String settingName : settings.get(targetId).keySet()) {
        Element setElm = doc.createElement("set");
        setElm.setAttribute("target", targetId);
        setElm.setAttribute("name", settingName);
	Setting setting = settings.get(targetId).get(settingName);
        if (setting.xml != null) {
          Node value = doc.importNode(setting.xml.getDocumentElement(), true);
          setElm.appendChild(value);
        } else {
          setElm.setAttribute("value", setting.string);
        }
	root.appendChild(setElm);
      }
    }
    return doc;
  }
  
  public void setRecordFilter(String recordFilter, String recordFilterCriteria) {
    for (Entry<String,Map<String,Setting>> target : settings.entrySet()) {
      String targetId = target.getKey();
      String rF = (recordFilterCriteria == null 
        || recordFilterCriteria.isEmpty()
        || recordFilterCriteria.contains(targetId))
        ? recordFilter
        : null;
      Map<String, Setting> setts = target.getValue();
      if (rF == null || rF.isEmpty())
        setts.remove("pz:recordfilter");
      else
        setts.put("pz:recordfilter", new Setting(rF));
    }
  }
  
}
