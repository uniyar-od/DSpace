package org.dspace.app.webui.cris.rest.dris;

import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldId;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldResource;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldTypeFromJavaClass;

@JsonldResource
@JsonldTypeFromJavaClass
public class AbstractJsonLdResult implements JsonLdResult {
	
    @JsonldId
	private String id;
    
    public AbstractJsonLdResult() {
		super();
	}

	@Override
	public String getId() {
		return id;
	}
    
    @Override
	public void setId(String id) {
		this.id = id;
	}
    
}
