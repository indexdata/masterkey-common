/*
 * Copyright (c) 1995-2008, Index Data
 * All rights reserved.
 * See the file LICENSE for details.
 */

package com.indexdata.torus;

import java.util.List;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.indexdata.torus.layer.KeyValue;
import com.indexdata.torus.layer.KeyValueAdapter;
import java.util.ArrayList;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Represents a single abstract layer within a record.
 * @author jakub
 */
@XmlRootElement(name="layer")
public abstract class Layer {
    private String id;
    private String layerName;
    private List<KeyValue> elements = new ArrayList<KeyValue>();

    public Layer() {
    }

    public Layer(String name) {
        layerName = name;
    }
    
    @XmlAttribute(name="name")
    final public String getLayerName() {
        return layerName;
    }

    /**
     *  use setLayerName 
     */
    @Deprecated
    final public void seLayertName(String name) {
    	setLayerName(name);
    }

    final public void setLayerName(String name) {
        layerName = name;
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @XmlAnyElement
    @XmlJavaTypeAdapter(KeyValueAdapter.class)
    public List<KeyValue> getDynamicElements() {
        return elements;
    }

    public void setDynamicElements(List<KeyValue> elements) {
        this.elements = elements;
    }    

}
