package org.dspace.app.cris.unpaywall.model;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.dspace.app.cris.unpaywall.UnpaywallRecord;
import org.json.JSONObject;

import it.cilea.osd.common.core.HasTimeStampInfo;
import it.cilea.osd.common.core.ITimeStampInfo;
import it.cilea.osd.common.core.TimeStampInfo;
import it.cilea.osd.common.model.Identifiable;

@Entity
@Table(name = "cris_unpaywall")
@NamedQueries({
        @NamedQuery(name = "Unpaywall.uniqueByDOI", query = "from Unpaywall where doi =:par0") })
public class Unpaywall implements Identifiable, HasTimeStampInfo
{

    /** timestamp info for creation and last modify */
    @Embedded
    private TimeStampInfo timeStampInfo;

    @Id
    @GeneratedValue(generator = "CRIS_UNPAYWALL_SEQ")
    @SequenceGenerator(name = "CRIS_UNPAYWALL_SEQ", sequenceName = "CRIS_UNPAYWALL_SEQ", allocationSize = 1)
    @Column(name="unpaywall_id")
    private int resourceID;

    @Column(name = "doi")
    private String DOI;

    @Transient
    private UnpaywallRecord unpaywallRecord; // JSON
    
    @Transient
    private JSONObject unpaywallJson;

    public JSONObject getUnpaywallJson()
    {
        return unpaywallJson;
    }

    public void setUnpaywallJson(JSONObject unpaywallJson)
    {
        this.unpaywallJson = unpaywallJson;
    }

    public String getDOI()
    {
        return DOI;
    }

    public void setDOI(String dOI)
    {
        DOI = dOI;
    }

    public UnpaywallRecord getUnpaywallRecord()
    {
        return unpaywallRecord;
    }

    public void setUnpaywallRecord(UnpaywallRecord unpaywallRecord)
    {
        this.unpaywallRecord = unpaywallRecord;
    }

    public int getResourceID()
    {
        return resourceID;
    }

    public void setResourceID(int resourceID)
    {
        this.resourceID = resourceID;
    }

    public void setTimeStampInfo(TimeStampInfo timeStampInfo)
    {
        this.timeStampInfo = timeStampInfo;
    }

    @Override
    public ITimeStampInfo getTimeStampInfo()
    {
        return timeStampInfo;
    }

    @Override
    public Integer getId()
    {
        return resourceID;
    }

}
