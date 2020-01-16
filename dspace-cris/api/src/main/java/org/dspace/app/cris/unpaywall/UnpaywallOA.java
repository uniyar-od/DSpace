package org.dspace.app.cris.unpaywall;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UnpaywallOA
{
    private String endpoint_id;

    private String evidence;
    
    private String host_type;
    
    @JsonProperty(value ="is_best")
    private Boolean is_best;
    
    @JsonProperty(value ="license")
    private String license;
    
    @JsonProperty(value ="pmh_id")
    private String pmh_id;
    
    private String repository_institution;
    
    private String updated;
    
    private String url;
    
    private String url_for_landing_page;
    
    private String url_for_pdf;
    
    private String version;

    public String getEndpoint_id()
    {
        return endpoint_id;
    }

    public void setEndpoint_id(String endpoint_id)
    {
        this.endpoint_id = endpoint_id;
    }

    public String getEvidence()
    {
        return evidence;
    }

    public void setEvidence(String evidence)
    {
        this.evidence = evidence;
    }

    public String getHost_type()
    {
        return host_type;
    }

    public void setHost_type(String host_type)
    {
        this.host_type = host_type;
    }

    public Boolean getIs_best()
    {
        return is_best;
    }

    public void setIs_best(Boolean is_best)
    {
        this.is_best = is_best;
    }

    public String getLicense()
    {
        return license;
    }

    public void setLicense(String license)
    {
        this.license = license;
    }

    public String getPmh_id()
    {
        return pmh_id;
    }

    public void setPmh_id(String pmh_id)
    {
        this.pmh_id = pmh_id;
    }

    public String getRepository_institution()
    {
        return repository_institution;
    }

    public void setRepository_institution(String repository_institution)
    {
        this.repository_institution = repository_institution;
    }

    public String getUpdated()
    {
        return updated;
    }

    public void setUpdated(String updated)
    {
        this.updated = updated;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public String getUrl_for_landing_page()
    {
        return url_for_landing_page;
    }

    public void setUrl_for_landing_page(String url_for_landing_page)
    {
        this.url_for_landing_page = url_for_landing_page;
    }

    public String getUrl_for_pdf()
    {
        return url_for_pdf;
    }

    public void setUrl_for_pdf(String url_for_pdf)
    {
        this.url_for_pdf = url_for_pdf;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

}
