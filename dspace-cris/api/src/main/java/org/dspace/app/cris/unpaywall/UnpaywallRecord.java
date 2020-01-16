package org.dspace.app.cris.unpaywall;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize
public class UnpaywallRecord
{
    @JsonProperty(value = "best_oa_location")
    private UnpaywallBestOA unpaywallBestOA;

    @JsonProperty(value = "oa_locations")
    private UnpaywallOA[] unpaywallOA;

    private boolean error;

    private String doi;
    
   @JsonProperty
    private String oa_status;
    
    private String published_date;
    
    private String updated;
    
    private String title;
    
    @JsonProperty(value = "is_oa")
    private boolean isOa;

    @JsonProperty(value = "journal_is_in_doaj")
    private boolean IsInDoaj;

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public boolean isInDoaj()
    {
        return IsInDoaj;
    }

    public void setIsInDoaj(boolean isInDoaj)
    {
        IsInDoaj = isInDoaj;
    }

    public UnpaywallOA[] getUnpaywallOA()
    {
        return unpaywallOA;
    }

    public void setUnpaywallOA(UnpaywallOA[] unpaywallOA)
    {
        this.unpaywallOA = unpaywallOA;
    }

    public UnpaywallRecord()
    {
    }

    public UnpaywallRecord(boolean error)
    {
        super();
        this.error = error;
    }

    public boolean isError()
    {
        return error;
    }

    public void setError(boolean error)
    {
        this.error = error;
    }

    public String getOa_status()
    {
        return oa_status;
    }

    public void setOa_status(String oa_status)
    {
        this.oa_status = oa_status;
    }

    public String getPublished_date()
    {
        return published_date;
    }

    public void setPublished_date(String published_date)
    {
        this.published_date = published_date;
    }

    public String getUpdated()
    {
        return updated;
    }

    public void setUpdated(String updated)
    {
        this.updated = updated;
    }

    public UnpaywallBestOA getUnpaywallBestOA()
    {
        return unpaywallBestOA;
    }

    public void setUnpaywallBestOA(UnpaywallBestOA unpaywallBestOA)
    {
        this.unpaywallBestOA = unpaywallBestOA;
    }

    public static UnpaywallRecord getUnpaywallResponse(InputStream xmlData)
    {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        UnpaywallRecord response = null;
        try
        {
            response = objectMapper.readValue(xmlData, UnpaywallRecord.class);
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return response;
    }

    public void setDoi(String doi)
    {
        this.doi = doi;
    }

    public void setOa(boolean isOa)
    {
        this.isOa = isOa;
    }

    public String getDoi()
    {
        return doi;
    }

    public boolean isOa()
    {
        return isOa;
    }
}
