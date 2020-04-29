/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.cris.unpaywall;

public class UnpaywallBestOA
{
    private String license;

    private String host_type;
    
    private String url_for_pdf;
    
    private String version;
    
    private String evidence;

    public String getLicense()
    {
        return license;
    }

    public void setLicense(String license)
    {
        this.license = license;
    }

    public String getHost_type()
    {
        return host_type;
    }

    public void setHost_type(String host_type)
    {
        this.host_type = host_type;
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

    public String getEvidence()
    {
        return evidence;
    }

    public void setEvidence(String evidence)
    {
        this.evidence = evidence;
    }
    

}
