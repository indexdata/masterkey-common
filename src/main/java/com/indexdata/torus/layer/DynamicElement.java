package com.indexdata.torus.layer;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


@XmlJavaTypeAdapter(DynamicElementAdapter.class)
public class DynamicElement {
  
  private String name;
  private Object value;

  public String getName() {
      return name;
  }

  public DynamicElement() {
    
  }
    
  public DynamicElement(String name, Object value) {
    this.name = name;
    this.value = value;
  }
  
  public void setName(String name) {
      this.name = name;
  }

  public Object getValue() {
      return value;
  }

  public void setValue(Object value) {
      this.value = value;
  }

}
