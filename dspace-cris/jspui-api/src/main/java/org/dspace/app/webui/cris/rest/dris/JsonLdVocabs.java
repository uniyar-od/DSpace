/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * https://github.com/CILEA/dspace-cris/wiki/License
 */
package org.dspace.app.webui.cris.rest.dris;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.SolrDocument;
import org.dspace.app.webui.cris.rest.dris.utils.DrisUtils;

import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldId;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldLink;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldNamespace;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldProperty;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldResource;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldType;

@JsonldNamespace(name = "skos", uri = "http://www.w3.org/2004/02/skos/core#")
public class JsonLdVocabs extends AbstractJsonLdResult {
	
    @JsonldProperty(value = "skos:prefLabel")
	private String name;
	
	public static JsonLdVocabs buildFromSolrDoc(SolrDocument solrDoc) {
		JsonLdVocabs jldItem = new JsonLdVocabs();
		if (solrDoc == null) {
			return jldItem;
		}
		jldItem.setName(StringUtils.trimToEmpty((String)(solrDoc.getFirstValue("crisdo.name"))));
		String crisId = StringUtils.trimToEmpty((String)solrDoc.getFirstValue("cris-id"));
		String crisVocabularyType = StringUtils.trimToEmpty((String)solrDoc.getFirstValue("crisdo.type"));
		if("classcerif".equals(crisVocabularyType)) {
			crisVocabularyType = StringUtils.trimToEmpty((String)solrDoc.getFirstValue("crisclasscerif.classcerifvocabularytype"));
		}
		jldItem.setId(DrisUtils.buildVocabsIdLink(crisId, crisVocabularyType));
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
