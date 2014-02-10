package com.indexdata.torus.layer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DynamicElementAdapter extends XmlAdapter<Element, DynamicElement> {
  
  private ClassLoader classLoader;
  private DocumentBuilder documentBuilder;
  private JAXBContext jaxbContext;

  public DynamicElementAdapter() {
      classLoader = Thread.currentThread().getContextClassLoader();
  }

  public DynamicElementAdapter(JAXBContext jaxbContext) {
      this();
      this.jaxbContext = jaxbContext;
  }

  private DocumentBuilder getDocumentBuilder() throws Exception {
      // Lazy load the DocumentBuilder as it is not used for unmarshalling.
      if (null == documentBuilder) {
          DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
          documentBuilder = dbf.newDocumentBuilder();
      }
      return documentBuilder;
  }

  private JAXBContext getJAXBContext(Class<?> type) throws Exception {
      if (null == jaxbContext) {
          // A JAXBContext was not set, so create a new one based  on the type.
          return JAXBContext.newInstance(type);
      }
      return jaxbContext;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Element marshal(DynamicElement dynamicElement) throws Exception {
      if (null == dynamicElement) {
          return null;
      }
      // 1. Build the JAXBElement to wrap the instance of DynamicElement.
      QName rootElement = new QName(dynamicElement.getName());
      Object value = dynamicElement.getValue();
      Class<?> type = value.getClass();
      @SuppressWarnings("rawtypes")
      JAXBElement jaxbElement = new JAXBElement(rootElement, type, value);

      // 2.  Marshal the JAXBElement to a DOM element.
      Document document = getDocumentBuilder().newDocument();
      Marshaller marshaller = getJAXBContext(type).createMarshaller();
      marshaller.marshal(jaxbElement, document);
      Element element = document.getDocumentElement();

      // 3.  Set the type attribute based on the value's type.
      if (! type.getName().equals("java.lang.String"))
	element.setAttribute("type", type.getName());
      return element;
  }

  @Override
  public DynamicElement unmarshal(Element element) throws Exception {
      if (null == element) {
          return null;
      }

      // 1. Determine the values type from the type attribute.
      Class<?> type = String.class;
      if (element.getAttribute("type") != null)
	type = classLoader.loadClass(element.getAttribute("type"));

      // 2. Unmarshal the element based on the value's type.
      DOMSource source = new DOMSource(element);
      Unmarshaller unmarshaller = getJAXBContext(type).createUnmarshaller();
      @SuppressWarnings("rawtypes")
      JAXBElement jaxbElement = unmarshaller.unmarshal(source, type);

      // 3. Build the instance of Element
      DynamicElement dynamicElement = new DynamicElement();
      dynamicElement.setName(element.getLocalName());
      dynamicElement.setValue(jaxbElement.getValue());
      return dynamicElement;
  }

}