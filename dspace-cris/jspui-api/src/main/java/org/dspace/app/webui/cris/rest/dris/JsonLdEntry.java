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
	private String startDate="";
	private String numberUsers="";
	private String numberDBRequest="";
	private String cerifCompatibility="";
	private String cerifVersion="";
	private String institutionalRepository="";
	private String providerDepartment="";
	private String providerContact="";
	private String responsibleDataSupply="";
	private String responsibleDataValidation="";
	private String responsibleDataOutput="";
	private String ictSupport="";
	private String numberResearchers="";
	private String publication="";
	private String repname="";
	private String repurl="";
	private String repscope="";
	private String repinstscope="";
	private String repoa="";
	private String repsoftware="";
	private String repcoverage="";
	private List<String> repmedia;
	private List<String> repmetadata;
	private String repDataSupply="";
	private String repDataValidation="";
	private String repictSupport="";
	private String repnumberUsers="";
	private String repnumberDBRequest="";
	private String repproviderContact="";
	private String fulltext="";
	private String repositoryLink="";
	private String repproviderOrgUnit="";
	private List<String> linksExternalSystem;
	private List<String> linksInternalSystem;
	private String providerContactInfo="";
	private String latitudelongitude="";

	private Map<String,String> metadata = new HashMap<>();
	
	@JsonldIn
	private List<Map<String,Object>> included = new ArrayList<>();
	
	public static JsonLdEntry buildFromSolrDoc(SearchService service, SolrDocument solrDoc) {
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
		    jldItem.setDescription(StringUtils.trimToEmpty(established));
		}
		
		String repscope = (String)(solrDoc.getFirstValue("crisdris.drisrepscope_authority"));
		if(StringUtils.isNotBlank(repscope)) {
			jldItem.setRepscope(DrisUtils.buildMiniVocabScopeIdLink(repscope));
			jldItem.getIncluded().add(DrisUtils.buildVocabsIncluded(service, repscope, DrisUtils.buildVocabScopeIdLink(repscope)));
		}
		String repintscope = (String)(solrDoc.getFirstValue("crisdris.drisrepintscope_authority"));
		if(StringUtils.isNotBlank(repintscope)) {
			jldItem.setRepscope(DrisUtils.buildMiniVocabScopeIdLink(repintscope));
			jldItem.getIncluded().add(DrisUtils.buildVocabsIncluded(service, repintscope, DrisUtils.buildVocabScopeIdLink(repintscope)));
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
		String startDate = (String)(solrDoc.getFirstValue("crisdris.drisstartDate"));
		if(StringUtils.isNotBlank(startDate)) {
			jldItem.setStartDate(StringUtils.trimToEmpty(startDate));
		}
		String numberUsers = (String)(solrDoc.getFirstValue("crisdris.drisnumberUsers"));
		if(StringUtils.isNotBlank(numberUsers)) {
			jldItem.setNumberUsers(StringUtils.trimToEmpty(numberUsers));
		}
		String numberDBRequest = (String)(solrDoc.getFirstValue("crisdris.drisnumberDBRequest"));
		if(StringUtils.isNotBlank(numberDBRequest)) {
			jldItem.setNumberDBRequest(StringUtils.trimToEmpty(numberDBRequest));
		}
		String cerifCompatibility = (String)(solrDoc.getFirstValue("crisdris.driscerifCompatibility"));
		if(StringUtils.isNotBlank(cerifCompatibility)) {
			jldItem.setCerifCompatibility(StringUtils.trimToEmpty(cerifCompatibility));
		}
		String cerifVersion = (String)(solrDoc.getFirstValue("crisdris.driscerifVersion"));
		if(StringUtils.isNotBlank(cerifVersion)) {
			jldItem.setCerifVersion(StringUtils.trimToEmpty(cerifVersion));
		}
		String institutionalRepository = (String)(solrDoc.getFirstValue("crisdris.drisinstitutionalRepository"));
		if(StringUtils.isNotBlank(institutionalRepository)) {
			jldItem.setInstitutionalRepository(StringUtils.trimToEmpty(institutionalRepository));
		}
		String providerDepartment = (String)(solrDoc.getFirstValue("crisdris.drisproviderDepartment"));
		if(StringUtils.isNotBlank(providerDepartment)) {
			jldItem.setProviderDepartment(StringUtils.trimToEmpty(providerDepartment));
		}
		String providerContact = (String)(solrDoc.getFirstValue("crisdris.drisproviderContact"));
		if(StringUtils.isNotBlank(providerContact)) {
			jldItem.setProviderContact(StringUtils.trimToEmpty(providerContact));
		}
		String responsibleDataSupply = (String)(solrDoc.getFirstValue("crisdris.drisresponsibleDataSupply"));
		if(StringUtils.isNotBlank(responsibleDataSupply)) {
			jldItem.setResponsibleDataSupply(StringUtils.trimToEmpty(responsibleDataSupply));
		}
		String responsibleDataValidation = (String)(solrDoc.getFirstValue("crisdris.drisresponsibleDataValidation"));
		if(StringUtils.isNotBlank(responsibleDataValidation)) {
			jldItem.setResponsibleDataValidation(StringUtils.trimToEmpty(responsibleDataValidation));
		}
		String responsibleDataOutput = (String)(solrDoc.getFirstValue("crisdris.drisresponsibleDataOutput"));
		if(StringUtils.isNotBlank(responsibleDataOutput)) {
			jldItem.setResponsibleDataOutput(StringUtils.trimToEmpty(responsibleDataOutput));
		}
		String ictSupport = (String)(solrDoc.getFirstValue("crisdris.drisictSupport"));
		if(StringUtils.isNotBlank(ictSupport)) {
			jldItem.setIctSupport(StringUtils.trimToEmpty(ictSupport));
		}
		String numberResearchers = (String)(solrDoc.getFirstValue("crisdris.drisnumberResearchers"));
		if(StringUtils.isNotBlank(numberResearchers)) {
			jldItem.setNumberResearchers(StringUtils.trimToEmpty(numberResearchers));
		}
		String publication = (String)(solrDoc.getFirstValue("crisdris.drispublication"));
		if(StringUtils.isNotBlank(publication)) {
			jldItem.setPublication(StringUtils.trimToEmpty(publication));
		}
		String repname = (String)(solrDoc.getFirstValue("crisdris.drisrepname"));
		if(StringUtils.isNotBlank(repname)) {
			jldItem.setRepname(StringUtils.trimToEmpty(repname));
		}
		String repurl = (String)(solrDoc.getFirstValue("crisdris.drisrepurl"));
		if(StringUtils.isNotBlank(repurl)) {
			jldItem.setRepurl(StringUtils.trimToEmpty(repurl));
		}

		String repoa = (String)(solrDoc.getFirstValue("crisdris.drisrepoa"));
		if(StringUtils.isNotBlank(repoa)) {
			jldItem.setRepoa(StringUtils.trimToEmpty(repoa));
		}
		String repsoftware = (String)(solrDoc.getFirstValue("crisdris.drisrepsoftware"));
		if(StringUtils.isNotBlank(repsoftware)) {
			jldItem.setRepsoftware(StringUtils.trimToEmpty(repsoftware));
		}
		String repcoverage = (String)(solrDoc.getFirstValue("crisdris.drisrepcoverage"));
		if(StringUtils.isNotBlank(repcoverage)) {
			jldItem.setRepcoverage(StringUtils.trimToEmpty(repcoverage));
		}
		String repDataSupply = (String)(solrDoc.getFirstValue("crisdris.drisrepDataSupply"));
		if(StringUtils.isNotBlank(repDataSupply)) {
			jldItem.setRepDataSupply(StringUtils.trimToEmpty(repDataSupply));
		}
		String repDataValidation = (String)(solrDoc.getFirstValue("crisdris.drisrepDataValidation"));
		if(StringUtils.isNotBlank(repDataValidation)) {
			jldItem.setRepDataValidation(StringUtils.trimToEmpty(repDataValidation));
		}
		String repictSupport = (String)(solrDoc.getFirstValue("crisdris.drisrepictSupport"));
		if(StringUtils.isNotBlank(repictSupport)) {
			jldItem.setRepictSupport(StringUtils.trimToEmpty(repictSupport));
		}
		String repnumberUsers = (String)(solrDoc.getFirstValue("crisdris.drisrepnumberUsers"));
		if(StringUtils.isNotBlank(repnumberUsers)) {
			jldItem.setRepnumberUsers(StringUtils.trimToEmpty(repnumberUsers));
		}
		String repnumberDBRequest = (String)(solrDoc.getFirstValue("crisdris.drisrepnumberDBRequest"));
		if(StringUtils.isNotBlank(repnumberDBRequest)) {
			jldItem.setRepnumberDBRequest(StringUtils.trimToEmpty(repnumberDBRequest));
		}
		String repproviderContact = (String)(solrDoc.getFirstValue("crisdris.drisrepproviderContact"));
		if(StringUtils.isNotBlank(repproviderContact)) {
			jldItem.setRepproviderContact(StringUtils.trimToEmpty(repproviderContact));
		}
		String fulltext = (String)(solrDoc.getFirstValue("crisdris.drisfulltext"));
		if(StringUtils.isNotBlank(fulltext)) {
			jldItem.setFulltext(StringUtils.trimToEmpty(fulltext));
		}
		String repositoryLink = (String)(solrDoc.getFirstValue("crisdris.drisrepositoryLink"));
		if(StringUtils.isNotBlank(repositoryLink)) {
			jldItem.setRepositoryLink(StringUtils.trimToEmpty(repositoryLink));
		}
		String repproviderOrgUnit = StringUtils.trimToEmpty((String)solrDoc.getFirstValue("crisdris.drisrepproviderOrgUnit_authority"));
		if(StringUtils.isNotBlank(repproviderOrgUnit)) {
		    jldItem.setOrganization(DrisUtils.buildMiniOrgUnitIdLink(repproviderOrgUnit));
		    jldItem.getIncluded().add(DrisUtils.buildOrgUnitIncluded(service, repproviderOrgUnit, DrisUtils.buildOrgunitIdLink(repproviderOrgUnit)));
		}

		String providerContactInfo = (String)(solrDoc.getFirstValue("crisdris.drisproviderContactInfo"));
		if(StringUtils.isNotBlank(providerContactInfo)) {
			jldItem.setProviderContactInfo(StringUtils.trimToEmpty(providerContactInfo));
		}
		String latitudelongitude = (String)(solrDoc.getFirstValue("crisdris.drislatitudelongitude"));
		if(StringUtils.isNotBlank(latitudelongitude)) {
			jldItem.setLatitudelongitude(StringUtils.trimToEmpty(latitudelongitude));
		}
		
		Collection<Object> repmedia = (Collection<Object>)solrDoc.getFieldValues("crisdris.drisrepmedia");
        if (repmedia != null)
        {
            for (Object media : repmedia)
            {
            	String mediaString = (String) media;
                if(StringUtils.isNotBlank(mediaString)) {
                	jldItem.getRepmedia().add(mediaString);
                }
            }
        }

		Collection<Object> repMetadata = (Collection<Object>)solrDoc.getFieldValues("crisdris.drisrepmetadata");
        if (repMetadata != null)
        {
            for (Object meta : repMetadata)
            {
            	String metaString = (String) meta;
                if(StringUtils.isNotBlank(metaString)) {
                	jldItem.getRepmedia().add(metaString);
                }
            }
        }
        
		Collection<Object> linksExternalSystem = (Collection<Object>)solrDoc.getFieldValues("crisdris.drislinksexternalsystem");
        if (linksExternalSystem != null)
        {
            for (Object link : linksExternalSystem)
            {
            	String linkString = (String) link;
                if(StringUtils.isNotBlank(linkString)) {
                	jldItem.getLinksExternalSystem().add(linkString);
                }
            }
        }
		Collection<Object> linksInternalSystem = (Collection<Object>)solrDoc.getFieldValues("crisdris.drislinksinternalsystem");
        if (linksInternalSystem != null)
        {
            for (Object link : linksInternalSystem)
            {
            	String linkString = (String) link;
                if(StringUtils.isNotBlank(linkString)) {
                	jldItem.getLinksInternalSystem().add(linkString);
                }
            }
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

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getNumberUsers() {
		return numberUsers;
	}

	public void setNumberUsers(String numberUsers) {
		this.numberUsers = numberUsers;
	}

	public String getNumberDBRequest() {
		return numberDBRequest;
	}

	public void setNumberDBRequest(String numberDBRequest) {
		this.numberDBRequest = numberDBRequest;
	}

	public String getCerifCompatibility() {
		return cerifCompatibility;
	}

	public void setCerifCompatibility(String cerifCompatibility) {
		this.cerifCompatibility = cerifCompatibility;
	}

	public String getCerifVersion() {
		return cerifVersion;
	}

	public void setCerifVersion(String cerifVersion) {
		this.cerifVersion = cerifVersion;
	}

	public String getInstitutionalRepository() {
		return institutionalRepository;
	}

	public void setInstitutionalRepository(String institutionalRepository) {
		this.institutionalRepository = institutionalRepository;
	}


	public String getProviderDepartment() {
		return providerDepartment;
	}

	public void setProviderDepartment(String providerDepartment) {
		this.providerDepartment = providerDepartment;
	}

	public String getProviderContact() {
		return providerContact;
	}

	public void setProviderContact(String providerContact) {
		this.providerContact = providerContact;
	}

	public String getResponsibleDataSupply() {
		return responsibleDataSupply;
	}

	public void setResponsibleDataSupply(String responsibleDataSupply) {
		this.responsibleDataSupply = responsibleDataSupply;
	}

	public String getResponsibleDataValidation() {
		return responsibleDataValidation;
	}

	public void setResponsibleDataValidation(String responsibleDataValidation) {
		this.responsibleDataValidation = responsibleDataValidation;
	}

	public String getResponsibleDataOutput() {
		return responsibleDataOutput;
	}

	public void setResponsibleDataOutput(String responsibleDataOutput) {
		this.responsibleDataOutput = responsibleDataOutput;
	}

	public String getIctSupport() {
		return ictSupport;
	}

	public void setIctSupport(String ictSupport) {
		this.ictSupport = ictSupport;
	}


	public String getNumberResearchers() {
		return numberResearchers;
	}

	public void setNumberResearchers(String numberResearchers) {
		this.numberResearchers = numberResearchers;
	}

	public String getPublication() {
		return publication;
	}

	public void setPublication(String publication) {
		this.publication = publication;
	}

	public String getRepname() {
		return repname;
	}

	public void setRepname(String repname) {
		this.repname = repname;
	}

	public String getRepurl() {
		return repurl;
	}

	public void setRepurl(String repurl) {
		this.repurl = repurl;
	}

	public String getRepscope() {
		return repscope;
	}

	public void setRepscope(String repscope) {
		this.repscope = repscope;
	}

	public String getRepinstscope() {
		return repinstscope;
	}

	public void setRepinstscope(String repinstscope) {
		this.repinstscope = repinstscope;
	}

	public String getRepoa() {
		return repoa;
	}

	public void setRepoa(String repoa) {
		this.repoa = repoa;
	}

	public String getRepsoftware() {
		return repsoftware;
	}

	public void setRepsoftware(String repsoftware) {
		this.repsoftware = repsoftware;
	}

	public String getRepcoverage() {
		return repcoverage;
	}

	public void setRepcoverage(String repcoverage) {
		this.repcoverage = repcoverage;
	}

	public List<String> getRepmedia() {
		return repmedia;
	}

	public void setRepmedia(List<String> repmedia) {
		this.repmedia = repmedia;
	}

	public List<String> getRepmetadata() {
		return repmetadata;
	}

	public void setRepmetadata(List<String> repmetadata) {
		this.repmetadata = repmetadata;
	}

	public String getRepDataSupply() {
		return repDataSupply;
	}

	public void setRepDataSupply(String repDataSupply) {
		this.repDataSupply = repDataSupply;
	}

	public String getRepDataValidation() {
		return repDataValidation;
	}

	public void setRepDataValidation(String repDataValidation) {
		this.repDataValidation = repDataValidation;
	}

	public String getRepictSupport() {
		return repictSupport;
	}

	public void setRepictSupport(String repictSupport) {
		this.repictSupport = repictSupport;
	}

	public String getRepnumberUsers() {
		return repnumberUsers;
	}

	public void setRepnumberUsers(String repnumberUsers) {
		this.repnumberUsers = repnumberUsers;
	}

	public String getRepnumberDBRequest() {
		return repnumberDBRequest;
	}

	public void setRepnumberDBRequest(String repnumberDBRequest) {
		this.repnumberDBRequest = repnumberDBRequest;
	}

	public String getRepproviderContact() {
		return repproviderContact;
	}

	public void setRepproviderContact(String repproviderContact) {
		this.repproviderContact = repproviderContact;
	}

	public String getFulltext() {
		return fulltext;
	}

	public void setFulltext(String fulltext) {
		this.fulltext = fulltext;
	}

	public String getRepositoryLink() {
		return repositoryLink;
	}

	public void setRepositoryLink(String repositoryLink) {
		this.repositoryLink = repositoryLink;
	}

	public String getRepproviderOrgUnit() {
		return repproviderOrgUnit;
	}

	public void setRepproviderOrgUnit(String repproviderOrgUnit) {
		this.repproviderOrgUnit = repproviderOrgUnit;
	}

	public List<String> getLinksExternalSystem() {
		return linksExternalSystem;
	}

	public void setLinksExternalSystem(List<String> linksExternalSystem) {
		this.linksExternalSystem = linksExternalSystem;
	}

	public List<String> getLinksInternalSystem() {
		return linksInternalSystem;
	}

	public void setLinksInternalSystem(List<String> linksInternalSystem) {
		this.linksInternalSystem = linksInternalSystem;
	}

	public String getProviderContactInfo() {
		return providerContactInfo;
	}

	public void setProviderContactInfo(String providerContactInfo) {
		this.providerContactInfo = providerContactInfo;
	}

	public String getLatitudelongitude() {
		return latitudelongitude;
	}

	public void setLatitudelongitude(String latitudelongitude) {
		this.latitudelongitude = latitudelongitude;
	}

}
