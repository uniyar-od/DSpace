package org.dspace.app.webui.cris.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.SolrDocument;

import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldNamespace;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldTypeFromJavaClass;

@JsonldNamespace(name = "URL", uri = "https://api.eurocris.org/dris/contexts/entries.jsonld")
//@JsonldLink(rel = "s:knows", name = "knows", href = "http://example.com/person/2345")
public class JsonLdEntry extends AbstractJsonLdResult {
	
	private String acronym;
	private String name;
	private String status;
	private String scope;
	private String uri;
	private String crisPlatform;
	private String organization;
	private String country;
	
	public static JsonLdEntry buildFromSolrDoc(SolrDocument solrDoc) {
		JsonLdEntry jldItem = new JsonLdEntry();
		if (solrDoc == null) {
			return jldItem;
		}
		String crisId = StringUtils.trimToEmpty((String)solrDoc.getFirstValue("crisdris.this_authority"));
		jldItem.setId(AbstractJsonLdResult.buildEntryIdLink(crisId));
		jldItem.setAcronym(StringUtils.trimToEmpty((String)(solrDoc.getFirstValue("crisdris.drisacronym"))));
		jldItem.setName(StringUtils.trimToEmpty((String)(solrDoc.getFirstValue("crisdris.drisname"))));
		String statusId = StringUtils.trimToEmpty((String)solrDoc.getFirstValue("crisdris.drisstatus_authority"));
		jldItem.setStatus(AbstractJsonLdResult.buildMiniVocabStatusIdLink(statusId));
		String scopeId = StringUtils.trimToEmpty((String)solrDoc.getFirstValue("crisdris.drisscope_authority"));
		jldItem.setScope(AbstractJsonLdResult.buildMiniVocabScopeIdLink(scopeId));
		jldItem.setUri(StringUtils.trimToEmpty((String)(solrDoc.getFirstValue("crisdris.drisuri"))));
		String orgId = StringUtils.trimToEmpty((String)solrDoc.getFirstValue("drisorganisation_authority"));
		jldItem.setOrganization(AbstractJsonLdResult.buildMiniOrgUnitIdLink(orgId));
		String countryId = StringUtils.trimToEmpty((String)solrDoc.getFirstValue("crisdris.driscountry_authority"));
		jldItem.setCountry(AbstractJsonLdResult.buildMiniVocabCountryIdLink(countryId));
		/*
			setCrisPlatform
		*/
		return jldItem;
	}

    public JsonLdEntry() {
		super();
	}

	public String getAcronym() {
		return acronym;
	}

	public void setAcronym(String acronym) {
		this.acronym = acronym;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getCrisPlatform() {
		return crisPlatform;
	}

	public void setCrisPlatform(String crisPlatform) {
		this.crisPlatform = crisPlatform;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}
	
}
