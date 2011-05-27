/*
 * Copyright (c) 1995-2008, Index Data
 * All rights reserved.
 * See the file LICENSE for details.
 */

package com.indexdata.torus.layer;

import com.indexdata.torus.Layer;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author jakub
 */
@XmlRootElement(name="layer")
public class IdentityTypeLayer extends Layer {
    private String displayName;
    private String userName;
    private String ipRanges;
    private String iconUrl;
    private String proxyPattern;
    private String referer;
    private String comment;
    private String identityId;
    //for older identity layers fallback to construction of searchableRealm and
    // categoryRealm from the (no longer public) idenityId field
    @XmlElement(name="searchablesRealm")
    private String searchablesRealm;
    @XmlElement(name="categoriesRealm")
    private String categoriesRealm;

    public String getIdentityId() {
      return identityId;
    }

   public void setIdentityId(String identityId) {
      this.identityId = identityId;
    }

    @XmlTransient
    public String getSearchablesRealm() {
      return searchablesRealm != null ? searchablesRealm : "searchable."+identityId;
    }

    public void setSearchablesRealm(String searchablesRealm) {
      this.searchablesRealm = searchablesRealm;
    }

    @XmlTransient
    public String getCategoriesRealm() {
      return categoriesRealm != null ? categoriesRealm : "cat."+identityId;
    }

    public void setCategoriesRealm(String categoriesRealm) {
      this.categoriesRealm = categoriesRealm;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getIpRanges() {
        return ipRanges;
    }

    public void setIpRanges(String ipRanges) {
        this.ipRanges = ipRanges;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getProxyPattern() {
        return proxyPattern;
    }

    public void setProxyPattern(String proxyPattern) {
        this.proxyPattern = proxyPattern;
    }

}
