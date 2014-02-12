/*
 * Copyright (c) 1995-2008, Index Data
 * All rights reserved.
 * See the file LICENSE for details.
 */

package com.indexdata.torus;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.w3c.dom.Element;

import com.indexdata.torus.layer.DynamicElement;
import com.indexdata.torus.layer.DynamicElementAdapter;

/**
 * Represents a single abstract layer within a record.
 * @author jakub
 */
@XmlRootElement(name="layer")
public abstract class Layer {
    private String id;
    private String layerName;
    @XmlAnyElement
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
    
    /**
     * Returns the elements not matched by the concrete implementation
     * of this class.
     * @return a list of Elements or JAXBElements objects
     */

    @Deprecated
    public List<Object> getOtherElements() {
        DynamicElementAdapter adapter = new DynamicElementAdapter();
        List<Object> elementList = new LinkedList<Object>();
        for (DynamicElement dynamicElement : elements) {
          try {
	    elementList.add(adapter.marshal(dynamicElement));
	  } catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	  }
        }
      	return elementList;
    }

    /**
     * Use DynamicsElements or set/add Element
     * @param otherElements
     */
    @SuppressWarnings("rawtypes")
    @Deprecated
    public void setOtherElements(List<Object> otherElements) {
      DynamicElementAdapter adapter = new DynamicElementAdapter();
      List<DynamicElement> dynamicElements = new LinkedList<DynamicElement>();
      for (Object object : otherElements) {
        try {
          if (object instanceof Element)
            dynamicElements.add(adapter.unmarshal((Element) object));
          else if (object instanceof JAXBElement)
            dynamicElements.add(adapter.unmarshal((JAXBElement) object));
	  } catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	  }
      }
      this.elements = dynamicElements;
    }

    public Collection<DynamicElement> getDynamicElements() {
        return elements;
    }

    public void setDynamicElements(List<DynamicElement> elements) {
        this.elements = elements;
    }    

    public void setElement(String key, Object value) {
      if (value == null)
	return ; 
      if (elements == null)
	elements = new LinkedList<DynamicElement>();
      for (DynamicElement element : elements) {
	if (key.equals(element.getName())) { 
	  element.setValue(value);
	  return ;
	}
      }
      elements.add(new DynamicElement(key, value));
    }
    
    public void addElement(String key, Object value) {
      if (value == null)
	return ; 
      if (elements == null)
	elements = new LinkedList<DynamicElement>();
      elements.add(new DynamicElement(key, value));
    }

    public Collection<DynamicElement> getElement(String key) {
      Collection<DynamicElement> subset = new LinkedList<DynamicElement>();
      if (elements == null)
	return subset; 
      for (DynamicElement element : elements) {
	if (key.equals(element.getName())) { 
	  subset.add(element);
	}
      }
      return subset;
    }

    public void deleteElement(String key) {
      Collection<DynamicElement> deletes = getElement(key);
      elements.removeAll(deletes);
    }
}
