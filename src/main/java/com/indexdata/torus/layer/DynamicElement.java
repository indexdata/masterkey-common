package com.indexdata.torus.layer;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

public class DynamicElement {
  
  
  private String name;
  private Object value;

  @XmlJavaTypeAdapter(DynamicElementAdapter.class)
  public String getName() {
      return name;
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
