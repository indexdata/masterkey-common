/*
 * Copyright (c) 1995-2008, Index Data
 * All rights reserved.
 * See the file LICENSE for details.
 */
package com.indexdata.torus.layer;

import java.util.List;

import com.indexdata.torus.Layer;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A "definition" of the searchable type.
 *
 * @author jakub
 */
@XmlRootElement(name = "layer")
public class SearchableTypeLayer extends Layer {
  private String name;
  private String zurl;
  private String transform;
  private String elementSet;
  private String requestSyntax;
  private String queryEncoding;
  private String recordEncoding;
  private String useTurboMarc;
  private String cclMapAu;
  private String cclMapTi;
  private String cclMapSu;
  private String cclMapIsbn;
  private String cclMapIssn;
  private String cclMapDate;
  private String cclMapTerm;
  private String cclMapJournalTitle;
  private String piggyback;
  private String authentication;
  private String authenticationMode;
  private String urlRecipe;
  private String serviceProvider;
  private String categories;
  private String medium;
  private String fullTextTarget;
  private String comment;
  private String explode;
  private String useUrlProxy;
  private String useThumbnails;
  private String cfAuth;
  private String cfSubDB;
  private String cfProxy;
  private String secondaryRequestSyntax;
  private String SRU;
  private String sruVersion;
  private String pqfPrefix;
  // Target specific parameters. e.g. ZOOM option extraArgs
  private String extraArgs;
  // Override target to PQF or CQL
  private String querySyntax;
  private String fieldMap;
  // Termlist/facet settings
  private String termlistTermSort;
  private String termlistTermCount;
  private String termlistUseTermFactor;
  // Classify a target to be local/preferred
  private String preferredTarget;
  // Classify a target to be local/preferred
  private String blockTimeout;
  // APDU log file name
  private String apduLog;
  private String maxRecords;
  private String placeHolds;
  private String literalTransform;
  private String contentConnector;
  private String contentAuthentication;
  private String contentProxy;
  private String udb;
  private String openAccess;
  private String metaData;
  private String originalUrl;

  @XmlAnyElement(lax=true)
  private List<DynamicElement> elements;
  
  public String getLiteralTransform() {
    return literalTransform;
  }

  public void setLiteralTransform(String literalTransform) {
    this.literalTransform = literalTransform;
  }
  

  public String getRecordEncoding() {
    return recordEncoding;
  }

  public void setRecordEncoding(String recordEncoding) {
    this.recordEncoding = recordEncoding;
  }

  public String getRequestSyntax() {
    return requestSyntax;
  }

  public void setRequestSyntax(String requestSyntax) {
    this.requestSyntax = requestSyntax;
  }

  public String getUseTurboMarc() {
    return useTurboMarc;
  }

  public void setUseTurboMarc(String useTurboMarc) {
    this.useTurboMarc = useTurboMarc;
  }

  public String getTransform() {
    return transform;
  }

  public void setTransform(String transform) {
    this.transform = transform;
  }

  public void setElementSet(String elementSet) {
    this.elementSet = elementSet;
  }

  public String getElementSet() {
    return elementSet;
  }

  @XmlElement(name = "displayName")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getZurl() {
    return zurl;
  }

  public void setZurl(String zurl) {
    this.zurl = zurl;
  }

  @XmlElement(name = "cclmap_au")
  public String getCclMapAu() {
    return cclMapAu;
  }

  public void setCclMapAu(String cclMapAu) {
    this.cclMapAu = cclMapAu;
  }

  @XmlElement(name = "cclmap_date")
  public String getCclMapDate() {
    return cclMapDate;
  }

  public void setCclMapDate(String cclMapDate) {
    this.cclMapDate = cclMapDate;
  }

  @XmlElement(name = "cclmap_su")
  public String getCclMapSu() {
    return cclMapSu;
  }

  public void setCclMapSu(String cclMapSu) {
    this.cclMapSu = cclMapSu;
  }

  @XmlElement(name = "cclmap_isbn")
  public String getCclMapIsbn() {
    return cclMapIsbn;
  }

  public void setCclMapIsbn(String cclMapIsbn) {
    this.cclMapIsbn = cclMapIsbn;
  }

  @XmlElement(name = "cclmap_issn")
  public String getCclMapIssn() {
    return cclMapIssn;
  }

  public void setCclMapIssn(String cclMapIssn) {
    this.cclMapIssn = cclMapIssn;
  }

  @XmlElement(name = "cclmap_term")
  public String getCclMapTerm() {
    return cclMapTerm;
  }

  public void setCclMapTerm(String cclMapTerm) {
    this.cclMapTerm = cclMapTerm;
  }

  public String getPiggyback() {
    return piggyback;
  }

  public void setPiggyback(String piggyback) {
    this.piggyback = piggyback;
  }

  @XmlElement(name = "cclmap_ti")
  public String getCclMapTi() {
    return cclMapTi;
  }

  public void setCclMapTi(String cclMapTi) {
    this.cclMapTi = cclMapTi;
  }

  @XmlElement(name = "cclmap_jt")
  public String getCclMapJournalTitle() {
    return cclMapJournalTitle;
  }

  public void setCclMapJournalTitle(String cclMapJournalTitle) {
    this.cclMapJournalTitle = cclMapJournalTitle;
  }

  public String getQueryEncoding() {
    return queryEncoding;
  }

  public void setQueryEncoding(String queryEncoding) {
    this.queryEncoding = queryEncoding;
  }

  public String getAuthentication() {
    return authentication;
  }

  public void setAuthentication(String authentication) {
    this.authentication = authentication;
  }
  
  public String getAuthenticationMode() {
    return authenticationMode;
  }

  public void setAuthenticationMode(String authenticationMode) {
    this.authenticationMode = authenticationMode;
  }


  public String getUrlRecipe() {
    return urlRecipe;
  }

  public void setUrlRecipe(String urlRecipe) {
    this.urlRecipe = urlRecipe;
  }

  public String getServiceProvider() {
    return serviceProvider;
  }

  public void setServiceProvider(String serviceProvider) {
    this.serviceProvider = serviceProvider;
  }

  public String getCategories() {
    return categories;
  }

  public void setCategories(String categories) {
    this.categories = categories;
  }

  public String getMedium() {
    return medium;
  }

  public void setMedium(String medium) {
    this.medium = medium;
  }

  public String getFullTextTarget() {
    return fullTextTarget;
  }

  public void setFullTextTarget(String fullTextTarget) {
    this.fullTextTarget = fullTextTarget;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public String getExplode() {
    return explode;
  }

  public void setExplode(String explode) {
    this.explode = explode;
  }

  public String getUseUrlProxy() {
    return useUrlProxy;
  }

  public void setUseUrlProxy(String useUrlProxy) {
    this.useUrlProxy = useUrlProxy;
  }

  public String getUseThumbnails() {
    return useThumbnails;
  }

  public void setUseThumbnails(String useThumbnails) {
    this.useThumbnails = useThumbnails;
  }

  public String getCfAuth() {
    return cfAuth;
  }

  public void setCfAuth(String cfAuth) {
    this.cfAuth = cfAuth;
  }

  public String getCfProxy() {
    return cfProxy;
  }

  public void setCfProxy(String cfProxy) {
    this.cfProxy = cfProxy;
  }

  public String getCfSubDB() {
    return cfSubDB;
  }

  public void setCfSubDB(String cfSubDB) {
    this.cfSubDB = cfSubDB;
  }

  public String getSecondaryRequestSyntax() {
    return secondaryRequestSyntax;
  }

  public void setSecondaryRequestSyntax(String secondaryRequestSyntax) {
    this.secondaryRequestSyntax = secondaryRequestSyntax;
  }

  public String getSruVersion() {
    return sruVersion;
  }

  public void setSruVersion(String sruVersion) {
    this.sruVersion = sruVersion;
  }

  public String getPqfPrefix() {
    return pqfPrefix;
  }

  public void setPqfPrefix(String pqfPrefix) {
    this.pqfPrefix = pqfPrefix;
  }

  @XmlElement(name = "sru")
  public String getSRU() {
    return SRU;
  }

  public void setSRU(String sRU) {
    SRU = sRU;
  }

  public String getTermlistTermSort() {
    return termlistTermSort;
  }

  public void setTermlistTermSort(String termlistTermSort) {
    this.termlistTermSort = termlistTermSort;
  }

  public String getTermlistTermCount() {
    return termlistTermCount;
  }

  public void setTermlistTermCount(String termlistTermCount) {
    this.termlistTermCount = termlistTermCount;
  }

  public String getPreferredTarget() {
    return preferredTarget;
  }

  public void setPreferredTarget(String preferred) {
    this.preferredTarget = preferred;
  }

  public String getBlockTimeout() {
    return blockTimeout;
  }

  public void setBlockTimeout(String blockTimeout) {
    this.blockTimeout = blockTimeout;
  }

  public String getApduLog() {
    return apduLog;
  }

  public void setApduLog(String apduLog) {
    this.apduLog = apduLog;
  }

  public String getMaxRecords() {
    return maxRecords;
  }

  public void setMaxRecords(String maxRecords) {
    this.maxRecords = maxRecords;
  }

  public String getTermlistUseTermFactor() {
    return termlistUseTermFactor;
  }

  public void setTermlistUseTermFactor(String termlistUseTermFactor) {
    this.termlistUseTermFactor = termlistUseTermFactor;
  }

  public String getExtraArgs() {
    return extraArgs;
  }

  public void setExtraArgs(String extraArgs) {
    this.extraArgs = extraArgs;
  }

  public String getQuerySyntax() {
    return querySyntax;
  }

  public void setQuerySyntax(String querySyntax) {
    this.querySyntax = querySyntax;
  }

  public String getFieldMap() {
    return fieldMap;
  }

  public void setFieldMap(String fieldMap) {
    this.fieldMap = fieldMap;
  }

  public String getPlaceHolds() {
    return placeHolds;
  }

  public void setPlaceHolds(String placeHolds) {
    this.placeHolds = placeHolds;
  }

  public String getContentConnector() {
    return contentConnector;
  }

  public void setContentConnector(String contentConnector) {
    this.contentConnector = contentConnector;
  }
  
  public String getContentAuthentication() {
    return contentAuthentication;
  }

  public void setContentAuthentication(String contentAuthentication) {
    this.contentAuthentication = contentAuthentication;
  }

  public String getContentProxy() {
    return contentProxy;
  }

  public void setContentProxy(String contentProxy) {
    this.contentProxy = contentProxy;
  }

  public String getUdb() {
    return udb;
  }

  public void setUdb(String udb) {
    this.udb = udb;
  }

  public String getOpenAccess() {
    return openAccess;
  }

  public void setOpenAccess(String openAccess) {
    this.openAccess = openAccess;
  }

  public String getOriginalUrl() {
    return originalUrl;
  }

  public void setOriginalUrl(String originalUrl) {
    this.originalUrl = originalUrl;
  }

  public String getMetaData() {
    return metaData;
  }

  public void setMetaData(String metaData) {
    this.metaData = metaData;
  }
  
}
