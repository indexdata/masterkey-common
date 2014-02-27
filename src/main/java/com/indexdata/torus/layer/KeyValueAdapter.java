package com.indexdata.torus.layer;

import com.indexdata.utils.XmlUtils;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class KeyValueAdapter extends XmlAdapter<Object, KeyValue> {

  @Override
  public Object marshal(KeyValue v) throws Exception {
    Document partial = XmlUtils.newDoc(v.getName());
    Element elem = partial.getDocumentElement();
    elem.setTextContent(v.getValue());
    return elem;
  }

  @Override
  public KeyValue unmarshal(Object v) throws Exception {
    Element elem = (Element) v;
    return new KeyValue(elem.getNodeName(), elem.getTextContent());
  }


}