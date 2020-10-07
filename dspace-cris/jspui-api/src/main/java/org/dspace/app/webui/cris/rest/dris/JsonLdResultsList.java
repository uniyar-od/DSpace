/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * https://github.com/CILEA/dspace-cris/wiki/License
 */
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
	
	private List <? extends JsonLdResult> itemListElement;
	private long numberOfItems;
	
    public JsonLdResultsList() {
		super();
		this.setItemListElement(new LinkedList<JsonLdResult>());
	}
	
	public List<? extends JsonLdResult> getItemListElement() {
		return itemListElement;
	}
	
	public void setItemListElement(List<? extends JsonLdResult> itemListElement) {
		this.itemListElement = itemListElement;
	}
	
	public long getNumberOfItems() {
		return numberOfItems;
	}
	
	public void setNumberOfItems(long numberOfItems) {
		this.numberOfItems = numberOfItems;
	}
	
}
