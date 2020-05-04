/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
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
        @NamedQuery(name = "Unpaywall.uniqueByDOIAndItemID", query = "from Unpaywall where doi =:par0 AND itemId =:par1")})
public class Unpaywall implements Identifiable, HasTimeStampInfo
{
	private static final long serialVersionUID = 1L;

	/** timestamp info for creation and last modify */
    @Embedded
    private TimeStampInfo timeStampInfo;

    @Id
    @GeneratedValue(generator = "CRIS_UNPAYWALL_SEQ")
    @SequenceGenerator(name = "CRIS_UNPAYWALL_SEQ", sequenceName = "CRIS_UNPAYWALL_SEQ", allocationSize = 1)
    private int id;

    @Column(name = "doi")
    private String doi;
    
    @Column 
    @Type(type="org.hibernate.type.StringClobType")
    private String jsonRecord;
    
    @Column(unique = true)
    private Integer itemId;

    public String getDoi()
    {
        return doi;
    }

    public void setDoi(String dOI)
    {
        doi = dOI;
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

	public String getJsonRecord() {
		return jsonRecord;
	}

	public void setJsonRecord(String unpaywallJsonString) {
		this.jsonRecord = unpaywallJsonString;
	}

	public Integer getItemId() {
		return itemId;
	}

	public void setItemId(Integer item_id) {
		this.itemId = item_id;
	}

}
