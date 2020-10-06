package org.dspace.app.webui.cris.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.SolrDocument;

import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldId;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldLink;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldNamespace;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldProperty;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldResource;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldType;

@JsonldNamespace(name = "URL", uri = "https://api.eurocris.org/dris/contexts/vocabs.jsonld")
public class JsonLdVocabs extends AbstractJsonLdResult {
	
	private String name;
	
	public static JsonLdVocabs buildFromSolrDoc(SolrDocument solrDoc) {
		JsonLdVocabs jldItem = new JsonLdVocabs();
		if (solrDoc == null) {
			return jldItem;
		}
		jldItem.setName(StringUtils.trimToEmpty((String)(solrDoc.getFirstValue("crisdris.drisname"))));
		String crisId = StringUtils.trimToEmpty((String)solrDoc.getFirstValue("cris-id"));
		jldItem.setId(AbstractJsonLdResult.buildEntryIdLink(crisId));
		return jldItem;
	}
	
    public JsonLdVocabs() {
		super();
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
}
