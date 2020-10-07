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

@JsonldNamespace(name = "URL", uri = "https://api.eurocris.org/dris/contexts/orgunits.jsonld")
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
		jldItem.setCountry(DrisUtils.buildVocabCountryAuthLink(countryAuth));
		jldItem.setId(DrisUtils.buildEntryIdLink(crisId));
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
