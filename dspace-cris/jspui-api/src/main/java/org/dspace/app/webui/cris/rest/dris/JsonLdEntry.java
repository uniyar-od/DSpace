/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * https://github.com/CILEA/dspace-cris/wiki/License
 */
package org.dspace.app.webui.cris.rest.dris;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.dspace.app.webui.cris.rest.dris.annotation.JsonldIn;
import org.dspace.app.webui.cris.util.AbstractJsonLdResult;
import org.dspace.discovery.SearchService;
import org.dspace.discovery.SearchServiceException;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import ioinformarics.oss.jackson.module.jsonld.annotation.JsonldNamespace;

@JsonldNamespace(name = "URL", uri = "https://api.eurocris.org/dris/contexts/entries.jsonld")
//@JsonldLink(rel = "s:knows", name = "knows", href = "http://example.com/person/2345")
public class JsonLdEntry extends AbstractJsonLdResult {
    
    private static Logger log = Logger.getLogger(JsonLdEntry.class);
    
    private final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	
	private String acronym = "";
	private String name = "";
	private String status = "";
	private String scope = "";
	private String uri = "";
	private String crisPlatform = "";
	private String crisPlatformNote = "";
	private String organization = "";
	private String country = "";
	private List<String> coverages;
	private Map<String,String> metadata = new HashMap<>();
	
	@JsonldIn
	private List<Map<String,Object>> included = new ArrayList<>();
	
	public static JsonLdEntry buildFromSolrDoc(SearchService service, SolrDocument solrDoc) {
		JsonLdEntry jldItem = new JsonLdEntry();
		if (solrDoc == null) {
			return jldItem;
		}
		String crisId = StringUtils.trimToEmpty((String)solrDoc.getFirstValue("crisdris.this_authority"));
		jldItem.setId(AbstractJsonLdResult.buildEntryIdLink(crisId));
		
		
		String acronym = (String)(solrDoc.getFirstValue("crisdris.drisacronym"));
		if(StringUtils.isNotBlank(acronym)) {
		    jldItem.setAcronym(StringUtils.trimToEmpty(acronym));
		}
		
		jldItem.setName(StringUtils.trimToEmpty((String)(solrDoc.getFirstValue("crisdris.drisname"))));
		
		String statusId = StringUtils.trimToEmpty((String)solrDoc.getFirstValue("crisdris.drisstatus_authority"));
		if(StringUtils.isNotBlank(statusId)) {
		    jldItem.setStatus(AbstractJsonLdResult.buildMiniVocabStatusIdLink(statusId));
		    jldItem.getIncluded().add(buildVocabsIncluded(service, statusId, AbstractJsonLdResult.buildVocabStatusIdLink(statusId)));
		}
		
		String scopeId = StringUtils.trimToEmpty((String)solrDoc.getFirstValue("crisdris.drisscope_authority"));
		if(StringUtils.isNotBlank(scopeId)) {
		    jldItem.setScope(AbstractJsonLdResult.buildMiniVocabScopeIdLink(scopeId));
		    jldItem.getIncluded().add(buildVocabsIncluded(service, scopeId, AbstractJsonLdResult.buildVocabScopeIdLink(scopeId)));
		}
		
		String uri = (String)(solrDoc.getFirstValue("crisdris.drisuri"));
		if(StringUtils.isNotBlank(uri)) {
		    jldItem.setUri(StringUtils.trimToEmpty(uri));
		}
		
		String crisPlatform = StringUtils.trimToEmpty((String)solrDoc.getFirstValue("crisdris.drissoftware_authority"));
		if(StringUtils.isNotBlank(crisPlatform)) {
		    jldItem.setCrisPlatform(AbstractJsonLdResult.buildMiniVocabPlatformIdLink(crisPlatform));
		    jldItem.getIncluded().add(buildVocabsIncluded(service, crisPlatform, AbstractJsonLdResult.buildVocabPlatformIdLink(crisPlatform)));
		}
		
		String crisPlatformNote = StringUtils.trimToEmpty((String)solrDoc.getFirstValue("crisdris.driscrissoftwarenote"));
		if(StringUtils.isNotBlank(crisPlatformNote)) {
		    jldItem.setCrisPlatformNote(crisPlatformNote);
		}
		
		String orgId = StringUtils.trimToEmpty((String)solrDoc.getFirstValue("crisdris.drisproviderOrgUnit_authority"));
		if(StringUtils.isNotBlank(orgId)) {
		    jldItem.setOrganization(AbstractJsonLdResult.buildMiniOrgUnitIdLink(orgId));
		    jldItem.getIncluded().add(buildOrgUnitIncluded(service, orgId, AbstractJsonLdResult.buildOrgunitIdLink(orgId)));
		}
		
		String countryId = StringUtils.trimToEmpty((String)solrDoc.getFirstValue("crisdris.driscountry_authority"));
		if(StringUtils.isNotBlank(countryId)) {
		    jldItem.setCountry(AbstractJsonLdResult.buildMiniVocabCountryIdLink(countryId));
		    jldItem.getIncluded().add(buildCountryIncluded(service, countryId, AbstractJsonLdResult.buildVocabCountryAuthLink(countryId)));
		}
		
		Collection<Object> coverages = (Collection<Object>)solrDoc.getFieldValues("crisdris.driscoverage_authority");
        if (coverages != null)
        {
            for (Object coverage : coverages)
            {
                String coverageString = (String) coverage;
                if(StringUtils.isNotBlank(coverageString)) {
                    jldItem.getCoverages().add(AbstractJsonLdResult
                            .buildMiniVocabCoverageIdLink(coverageString));
                    jldItem.getIncluded().add(buildVocabsIncluded(service, coverageString, AbstractJsonLdResult.buildVocabCoverageIdLink(coverageString)));
                }
            }
        }
		
        Map<String,String> metadata = new HashMap<>();
		Date creationdate = (Date)solrDoc.getFirstValue("crisdris.time_creation_dt");
		if(creationdate!=null) {
		    metadata.put("created", dateFormat.format(creationdate));
		}
		Date modificationdate = (Date)solrDoc.getFirstValue("crisdris.time_lastmodified_dt");
		if(modificationdate!=null) {
		    metadata.put("lastModified", dateFormat.format(modificationdate));
		}
		jldItem.setMetadata(metadata);
		
		return jldItem;
	}

    private static Map<String, Object> buildCountryIncluded(
            SearchService service, String crisid,
            String url)
    {
        Map<String, Object> obj = new HashMap<>();
        SolrQuery solrQuery = new SolrQuery();
        QueryResponse rsp;
        try {
            solrQuery = new SolrQuery();
            solrQuery.setQuery("cris-id:\"" + crisid +"\"");
            solrQuery.setRows(1);
            rsp = service.search(solrQuery);
            SolrDocumentList solrResults = rsp.getResults();
            Iterator<SolrDocument> iter = solrResults.iterator();
            while (iter.hasNext()) {
                SolrDocument doc = iter.next();
                obj.put("@id", url);
                
                Map<String, String> label = new HashMap<>();
                label.put("en", (String)doc.getFirstValue("crisdo.name"));
                obj.put("label", label);
                
                Map<String, String> isocode = new HashMap<>();
                isocode.put("Alpha2", (String)doc.getFirstValue("criscountry.countryalphacode2"));
                isocode.put("Alpha3", (String)doc.getFirstValue("criscountry.countryalphacode3"));
                isocode.put("Numeric", (String)doc.getFirstValue("criscountry.countrynumericcode"));
                obj.put("iso_3166_codes", isocode);                
                break;
            }
        } catch (SearchServiceException e) {
            log.error(e.getMessage(), e);
        }   
        return obj;
    }

    private static Map<String, Object> buildOrgUnitIncluded(
            SearchService service, String crisid, String url)
    {
        Map<String, Object> obj = new HashMap<>();
        SolrQuery solrQuery = new SolrQuery();
        QueryResponse rsp;
        try {
            solrQuery = new SolrQuery();
            solrQuery.setQuery("cris-id:\"" + crisid +"\"");
            solrQuery.setRows(1);
            rsp = service.search(solrQuery);
            SolrDocumentList solrResults = rsp.getResults();
            Iterator<SolrDocument> iter = solrResults.iterator();
            while (iter.hasNext()) {
                SolrDocument doc = iter.next();
                obj.put("@id", url);
                obj.put("label",  (String)doc.getFirstValue("crisou.name"));
                obj.put("country",  AbstractJsonLdResult.buildMiniVocabCountryIdLink((String)doc.getFirstValue("crisou.countrylink_authority")));
                break;
            }
        } catch (SearchServiceException e) {
            log.error(e.getMessage(), e);
        }   
        return obj;
    }

    private static Map<String, Object> buildVocabsIncluded(SearchService service, String crisid, String url)
    {
        Map<String, Object> obj = new HashMap<>();
        SolrQuery solrQuery = new SolrQuery();
        QueryResponse rsp;
        try {
            solrQuery = new SolrQuery();
            solrQuery.setQuery("cris-id:\"" + crisid +"\"");
            solrQuery.setRows(1);
            rsp = service.search(solrQuery);
            SolrDocumentList solrResults = rsp.getResults();
            Iterator<SolrDocument> iter = solrResults.iterator();
            while (iter.hasNext()) {
                SolrDocument doc = iter.next();
                obj.put("@id", url);
                
                Map<String, String> label = new HashMap<>();
                label.put("en", (String)doc.getFirstValue("crisdo.name"));
                obj.put("label", label);
                break;
            }
        } catch (SearchServiceException e) {
            log.error(e.getMessage(), e);
        }   
        return obj;
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

    public String getCrisPlatformNote()
    {
        return crisPlatformNote;
    }

    public void setCrisPlatformNote(String crisPlatformNote)
    {
        this.crisPlatformNote = crisPlatformNote;
    }

    public List<String> getCoverages()
    {
        if(this.coverages == null) {
            this.coverages = new ArrayList<String>();
        }
        return coverages;
    }

    public void setCoverages(List<String> coverages)
    {
        this.coverages = coverages;
    }

    public Map<String, String> getMetadata()
    {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata)
    {
        this.metadata = metadata;
    }

    public List<Map<String, Object>> getIncluded()
    {
        return included;
    }

    public void setIncluded(List<Map<String, Object>> included)
    {
        this.included = included;
    }


}
