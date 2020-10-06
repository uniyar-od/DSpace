package org.dspace.app.webui.cris.util;

import org.apache.commons.lang3.StringUtils;
import org.dspace.app.webui.cris.servlet.DrisQueryingServlet;

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
    
    public static String buildEntryIdLink(String crisId) {
    	return DrisQueryingServlet.getMainApiUrl() + "/" + DrisQueryingServlet.ENTRIES_QUERY_TYPE_NAME + "/" + StringUtils.trimToEmpty(crisId);
    }
    
    public static String buildVocabCountryAuthLink(String countryAuth) {
		return DrisQueryingServlet.getMainApiUrl() + "/" + DrisQueryingServlet.VOCABS_QUERY_TYPE_NAME + "/" + 
				   DrisQueryingServlet.VOCABS_QUERY_TYPE_COUNTRIES_SUB_TYPE + "/" + StringUtils.trimToEmpty(countryAuth);
    }
    
    public static String buildMiniOrgUnitIdLink(String id) {
    	return DrisQueryingServlet.ORG_UNITS_QUERY_TYPE_NAME + ":" + StringUtils.trimToEmpty(id);
    }
    
    public static String buildMiniVocabStatusIdLink(String id) {
    	return DrisQueryingServlet.VOCABS_QUERY_TYPE_NAME + ":" + 
				   DrisQueryingServlet.VOCABS_QUERY_TYPE_STATUSES_SUB_TYPE + "/" + StringUtils.trimToEmpty(id);
    }
    
    public static String buildMiniVocabCountryIdLink(String id) {
    	return DrisQueryingServlet.VOCABS_QUERY_TYPE_NAME + ":" + 
				   DrisQueryingServlet.VOCABS_QUERY_TYPE_COUNTRIES_SUB_TYPE + "/" + StringUtils.trimToEmpty(id);
    }
    
    public static String buildMiniVocabScopeIdLink(String id) {
    	return DrisQueryingServlet.VOCABS_QUERY_TYPE_NAME + ":" + 
				   DrisQueryingServlet.VOCABS_QUERY_TYPE_SCOPES_SUB_TYPE + "/" + StringUtils.trimToEmpty(id);
    }
	
}
