
@XmlJavaTypeAdapters({
    @XmlJavaTypeAdapter(value=com.indexdata.torus.layer.KeyValueAdapter.class, type=com.indexdata.torus.layer.KeyValue.class)
})
package com.indexdata.torus.layer;
 
import javax.xml.bind.annotation.adapters.*; 