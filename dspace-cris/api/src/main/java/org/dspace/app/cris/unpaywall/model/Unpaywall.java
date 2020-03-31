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

import org.hibernate.annotations.Type;

import it.cilea.osd.common.core.HasTimeStampInfo;
import it.cilea.osd.common.core.ITimeStampInfo;
import it.cilea.osd.common.core.TimeStampInfo;
import it.cilea.osd.common.model.Identifiable;

@Entity
@Table(name = "cris_unpaywall")
@NamedQueries({
        @NamedQuery(name = "Unpaywall.uniqueByDOI", query = "from Unpaywall where doi =:par0"),
        @NamedQuery(name = "Unpaywall.uniqueByDOIAndItemID", query = "from Unpaywall where doi =:par0 AND resource_id =:par1")})
public class Unpaywall implements Identifiable, HasTimeStampInfo
{
	private static final long serialVersionUID = 1L;

	/** timestamp info for creation and last modify */
    @Embedded
    private TimeStampInfo timeStampInfo;

    @Id
    @GeneratedValue(generator = "CRIS_UNPAYWALL_SEQ")
    @SequenceGenerator(name = "CRIS_UNPAYWALL_SEQ", sequenceName = "CRIS_UNPAYWALL_SEQ", allocationSize = 1)
    @Column(name="id")
    private int id;

    @Column(name = "doi"/*, unique=true*/)
    private String DOI;
    
    @Column(name="record") 
    @Type(type="org.hibernate.type.StringClobType")
    private String unpaywallJsonString;
    
    @Column(name="resource_id", unique=true)
    private Integer item_id;

    public String getDOI()
    {
        return DOI;
    }

    public void setDOI(String dOI)
    {
        DOI = dOI;
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
        return id;
    }
    
    public void setId(Integer id)
    {
    	this.id = id;
    }

	public String getUnpaywallJsonString() {
		return unpaywallJsonString;
	}

	public void setUnpaywallJsonString(String unpaywallJsonString) {
		this.unpaywallJsonString = unpaywallJsonString;
	}

	public Integer getItem_id() {
		return item_id;
	}

	public void setItem_id(Integer item_id) {
		this.item_id = item_id;
	}

}
