package com.indexdata.torus.layer;

import com.indexdata.torus.Layer;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name="layer")
public class CategoryTypeLayer extends Layer {
    private String displayName;
    private String categoryId;
    
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public String getCategoryId () {
    	return categoryId;
    }

    public void setCategoryId (String categoryId) {
    	this.categoryId = categoryId;
    }

}
