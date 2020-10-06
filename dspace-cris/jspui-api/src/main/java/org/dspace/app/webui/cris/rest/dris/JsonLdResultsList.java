package org.dspace.app.webui.cris.rest.dris;

import java.util.LinkedList;
import java.util.List;

import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldId;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldLink;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldNamespace;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldProperty;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldResource;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldType;

@JsonldResource
@JsonldNamespace(name = "URL", uri = "https://schema.org")
@JsonldType(value = "ItemList")
public class JsonLdResultsList {
	
	List <? extends JsonLdResult> itemListElement;
	Integer numberOfItems;
	
    public JsonLdResultsList() {
		super();
		this.setItemListElement(new LinkedList<JsonLdResult>());
	}
	
	public List<? extends JsonLdResult> getItemListElement() {
		return itemListElement;
	}
	
	public void setItemListElement(List<? extends JsonLdResult> itemListElement) {
		this.itemListElement = itemListElement;
		this.setNumberOfItems((this.itemListElement != null)? this.itemListElement.size(): 0);
	}
	
	public Integer getNumberOfItems() {
		return numberOfItems;
	}
	
	private void setNumberOfItems(Integer numberOfItems) {
		this.numberOfItems = numberOfItems;
	}
	
}
