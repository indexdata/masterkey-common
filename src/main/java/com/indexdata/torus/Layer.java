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

import com.indexdata.torus.layer.DynamicElement;

/**
 * Represents a single abstract layer within a record.
 * @author jakub
 */
@XmlRootElement(name="layer")
public abstract class Layer {
    private String id;
    private String layerName;
    private List<Object> otherElements;
    private List<DynamicElement> elements;

    
    public Layer() {
    }

    public Layer(String name) {
        layerName = name;
    }
    
    @XmlAttribute(name="name")
    final public String getLayerName() {
        return layerName;
    }

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
    
    /**
     * Returns the elements not matched by the concrete implementation
     * of this class.
     * @return a list of Elements or JAXBElements objects
     */
    
    public List<Object> getOtherElements() {
        return otherElements;
    }

    public void setOtherElements(List<Object> otherElements) {
        this.otherElements = otherElements;
    } 

    @XmlAnyElement
    public List<DynamicElement> getDynamicElements() {
        return elements;
    }

    public void setParameters(List<DynamicElement> elements) {
        this.elements = elements;
    }

}
