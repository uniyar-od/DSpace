/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * https://github.com/CILEA/dspace-cris/wiki/License
 */
package org.dspace.app.webui.cris.rest.dris;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.SolrDocument;
import org.dspace.app.webui.cris.rest.dris.annotation.JsonldContext;
import org.dspace.app.webui.cris.rest.dris.annotation.JsonldIn;
import org.dspace.app.webui.cris.rest.dris.annotation.JsonldType;
import org.dspace.app.webui.cris.rest.dris.utils.DrisUtils;
import org.dspace.discovery.SearchService;

public class JsonLdEntry extends AbstractJsonLdResult {
    
    @JsonldContext
    private String context = DrisUtils.API_URL + "/contexts/entries.jsonld";
    @JsonldType
    private String type = DrisUtils.API_URL + "/static/JsonLdEntry";
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
	private String description=""; 
	private String established="";
	private String crisDataSupply=""; 
	private String crisDataValidation="";
	private String crisDataOutput="";
	private String openaireCrisEndpointURL="";

	private Map<String,String> metadata = new HashMap<>();
	
	private Map<String,String> badge = new HashMap<>();
	
	@JsonldIn
	private List<Map<String,Object>> included = new ArrayList<>();
	
	public static JsonLdEntry buildFromSolrDoc(SearchService service, SolrDocument solrDoc, boolean isSuperUser) {
		JsonLdEntry jldItem = new JsonLdEntry();
		if (solrDoc == null) {
			return jldItem;
		}
		String crisId = StringUtils.trimToEmpty((String)solrDoc.getFirstValue("crisdris.this_authority"));
		jldItem.setId(DrisUtils.buildEntryIdLink(crisId));
		
		
		String acronym = (String)(solrDoc.getFirstValue("crisdris.drisacronym"));
		if(StringUtils.isNotBlank(acronym)) {
		    jldItem.setAcronym(StringUtils.trimToEmpty(acronym));
		}
		
		jldItem.setName(StringUtils.trimToEmpty((String)(solrDoc.getFirstValue("crisdris.drisname"))));
		
		String statusId = StringUtils.trimToEmpty((String)solrDoc.getFirstValue("crisdris.drisstatus_authority"));
		if(StringUtils.isNotBlank(statusId)) {
		    jldItem.setStatus(DrisUtils.buildMiniVocabStatusIdLink(statusId));
		    jldItem.getIncluded().add(DrisUtils.buildVocabsIncluded(service, statusId, DrisUtils.buildVocabStatusIdLink(statusId)));
		}
		
		String scopeId = StringUtils.trimToEmpty((String)solrDoc.getFirstValue("crisdris.drisscope_authority"));
		if(StringUtils.isNotBlank(scopeId)) {
		    jldItem.setScope(DrisUtils.buildMiniVocabScopeIdLink(scopeId));
		    jldItem.getIncluded().add(DrisUtils.buildVocabsIncluded(service, scopeId, DrisUtils.buildVocabScopeIdLink(scopeId)));
		}
		
		String uri = (String)(solrDoc.getFirstValue("crisdris.drisuri"));
		if(StringUtils.isNotBlank(uri)) {
		    jldItem.setUri(StringUtils.trimToEmpty(uri));
		}

		String description = (String)(solrDoc.getFirstValue("crisdris.drisdescription"));
		if(StringUtils.isNotBlank(description)) {
		    jldItem.setDescription(StringUtils.trimToEmpty(description));
		}
		
		String established = (String)(solrDoc.getFirstValue("crisdris.drisestablished"));
		if(StringUtils.isNotBlank(established)) {
		    jldItem.setEstablished(StringUtils.trimToEmpty(established));
		}
		
		String crisDataSupply = (String)(solrDoc.getFirstValue("crisdris.driscrisDataSupply"));
		if(StringUtils.isNotBlank(crisDataSupply)) {
			jldItem.setCrisDataSupply(StringUtils.trimToEmpty(crisDataSupply));
		}
		String crisDataValidation = (String)(solrDoc.getFirstValue("crisdris.driscrisDataValidation"));
		if(StringUtils.isNotBlank(crisDataValidation)) {
			jldItem.setCrisDataValidation(StringUtils.trimToEmpty(crisDataValidation));
		}
		String crisDataOutput = (String)(solrDoc.getFirstValue("crisdris.driscrisDataOutput"));
		if(StringUtils.isNotBlank(crisDataOutput)) {
			jldItem.setCrisDataOutput(StringUtils.trimToEmpty(crisDataOutput));
		}
		String repproviderOrgUnit = StringUtils.trimToEmpty((String)solrDoc.getFirstValue("crisdris.drisrepproviderOrgUnit_authority"));
		if(StringUtils.isNotBlank(repproviderOrgUnit)) {
		    jldItem.setOrganization(DrisUtils.buildMiniOrgUnitIdLink(repproviderOrgUnit));
		    jldItem.getIncluded().add(DrisUtils.buildOrgUnitIncluded(service, repproviderOrgUnit, DrisUtils.buildOrgunitIdLink(repproviderOrgUnit)));
		}

		String crisPlatform = StringUtils.trimToEmpty((String)solrDoc.getFirstValue("crisdris.drissoftware_authority"));
		if(StringUtils.isNotBlank(crisPlatform)) {
		    jldItem.setCrisPlatform(DrisUtils.buildMiniVocabPlatformIdLink(crisPlatform));
		    jldItem.getIncluded().add(DrisUtils.buildVocabsIncluded(service, crisPlatform, DrisUtils.buildVocabPlatformIdLink(crisPlatform)));
		}
		
		String crisPlatformNote = StringUtils.trimToEmpty((String)solrDoc.getFirstValue("crisdris.driscrissoftwarenote"));
		if(StringUtils.isNotBlank(crisPlatformNote)) {
		    jldItem.setCrisPlatformNote(crisPlatformNote);
		}
		
		String orgId = StringUtils.trimToEmpty((String)solrDoc.getFirstValue("crisdris.drisproviderOrgUnit_authority"));
		if(StringUtils.isNotBlank(orgId)) {
		    jldItem.setOrganization(DrisUtils.buildMiniOrgUnitIdLink(orgId));
		    jldItem.getIncluded().add(DrisUtils.buildOrgUnitIncluded(service, orgId, DrisUtils.buildOrgunitIdLink(orgId)));
		}
		
		String countryId = StringUtils.trimToEmpty((String)solrDoc.getFirstValue("crisdris.driscountry_authority"));
		if(StringUtils.isNotBlank(countryId)) {
		    jldItem.setCountry(DrisUtils.buildMiniVocabCountryIdLink(countryId));
		    jldItem.getIncluded().add(DrisUtils.buildCountryIncluded(service, countryId, DrisUtils.buildVocabCountryAuthLink(countryId)));
		}
		
		Collection<Object> coverages = (Collection<Object>)solrDoc.getFieldValues("crisdris.driscoverage_authority");
        if (coverages != null)
        {
            for (Object coverage : coverages)
            {
                String coverageString = (String) coverage;
                if(StringUtils.isNotBlank(coverageString)) {
                    jldItem.getCoverages().add(DrisUtils
                            .buildMiniVocabCoverageIdLink(coverageString));
                    jldItem.getIncluded().add(DrisUtils.buildVocabsIncluded(service, coverageString, DrisUtils.buildVocabCoverageIdLink(coverageString)));
                }
            }
        }
        
        
        if(isSuperUser) {
            //write oai-pmh url
            String openAIREURL = StringUtils.trimToEmpty((String)solrDoc.getFirstValue("crisdris.drisoaipmhurl"));
            if(StringUtils.isNotBlank(openAIREURL)) {
                jldItem.setOpenaireCrisEndpointURL(openAIREURL);
                
            }
        }

        //badge section
        Map<String,String> badge = new HashMap<>();

        //write rdm indicator
        badge.put("rdmCheck", "false");
        Collection<Object> coveragesName = (Collection<Object>)solrDoc.getFieldValues("crisdris.driscoverage");
        if (coveragesName != null)
        {
            coverageloop : for (Object coverage : coveragesName)
            {
                String coverageString = (String) coverage;
                if (StringUtils.isNotBlank(coverageString))
                {
                    if ("Dataset".equals(coverageString))
                    {
                        badge.put("rdmCheck", "true");
                        break coverageloop;
                    }
                }
            }
        }

        //write Openaire compliance
        badge.put("openAIRECheck", "false");
        String complianceOpenaire = StringUtils.trimToEmpty((String)solrDoc.getFirstValue("crisdris.driscomplianceopenaire"));
        if(StringUtils.isNotBlank(complianceOpenaire)) {
            boolean isOpenaireCompliance = Boolean.parseBoolean(complianceOpenaire);
            if(isOpenaireCompliance) {
                badge.put("openAIRECheck", "true");
            }
        }
        jldItem.setBadge(badge);

        //metadata section
        Map<String,String> metadata = new HashMap<>();
		Date creationdate = (Date)solrDoc.getFirstValue("crisdris.time_creation_dt");
		if(creationdate!=null) {
		    metadata.put("created", DrisUtils.dateFormat.format(creationdate));
		}
		Date modificationdate = (Date)solrDoc.getFirstValue("crisdris.time_lastmodified_dt");
		if(modificationdate!=null) {
		    metadata.put("lastModified", DrisUtils.dateFormat.format(modificationdate));
		}
		jldItem.setMetadata(metadata);
		
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

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getEstablished() {
		return established;
	}

	public void setEstablished(String established) {
		this.established = established;
	}

	public String getCrisDataSupply() {
		return crisDataSupply;
	}

	public void setCrisDataSupply(String crisDataSupply) {
		this.crisDataSupply = crisDataSupply;
	}

	public String getCrisDataValidation() {
		return crisDataValidation;
	}

	public void setCrisDataValidation(String crisDataValidation) {
		this.crisDataValidation = crisDataValidation;
	}

	public String getCrisDataOutput() {
		return crisDataOutput;
	}

	public void setCrisDataOutput(String crisDataOutput) {
		this.crisDataOutput = crisDataOutput;
	}

    public String getOpenaireCrisEndpointURL()
    {
        return openaireCrisEndpointURL;
    }

    public void setOpenaireCrisEndpointURL(String openaireCrisEndpointURL)
    {
        this.openaireCrisEndpointURL = openaireCrisEndpointURL;
    }

    public Map<String, String> getBadge()
    {
        return badge;
    }

    public void setBadge(Map<String, String> badge)
    {
        this.badge = badge;
    }
}
