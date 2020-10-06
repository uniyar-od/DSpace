package org.dspace.app.webui.cris.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.SolrDocument;

import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldId;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldLink;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldNamespace;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldProperty;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldResource;
import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldType;

@JsonldNamespace(name = "URL", uri = "https://api.eurocris.org/dris/contexts/orgunits.jsonld")
//@JsonldType("s:OrganizationalUnit")
//@JsonldLink(rel = "s:knows", name = "knows", href = "http://example.com/person/2345")
public class JsonLdOrganizationalUnit extends AbstractJsonLdResult {
	
	private String name;
	private String country;
	
	public static JsonLdOrganizationalUnit buildFromSolrDoc(SolrDocument solrDoc) {
		JsonLdOrganizationalUnit jldItem = new JsonLdOrganizationalUnit();
		if (solrDoc == null) {
			return jldItem;
		}
		jldItem.setName(StringUtils.trimToEmpty((String)(solrDoc.getFirstValue("crisou.name"))));
		String countryAuth = StringUtils.trimToEmpty((String)solrDoc.getFirstValue("crisou.countrylink_authority"));
		String crisId = StringUtils.trimToEmpty((String)solrDoc.getFirstValue("cris-id"));
		jldItem.setCountry(AbstractJsonLdResult.buildVocabCountryAuthLink(countryAuth));
		jldItem.setId(AbstractJsonLdResult.buildEntryIdLink(crisId));
		return jldItem;
	}
	
    public JsonLdOrganizationalUnit() {
		super();
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	
}
