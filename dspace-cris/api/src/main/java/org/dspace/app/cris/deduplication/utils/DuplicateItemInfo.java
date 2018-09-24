/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * https://github.com/CILEA/dspace-cris/wiki/License
 */
package org.dspace.app.cris.deduplication.utils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dspace.app.cris.deduplication.model.DuplicateDecisionType;
import org.dspace.app.cris.deduplication.model.DuplicateDecisionValue;
import org.dspace.app.cris.model.dto.SimpleViewEntityDTO;
import org.dspace.browse.BrowsableDSpaceObject;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.util.ActionUtils;

public class DuplicateItemInfo
{

    private int dedupID = -1;

    private BrowsableDSpaceObject duplicateItem;
    
    private int duplicateItemType;

    private String note;

//    private EPerson eperson;
//
//    private boolean notDuplicate;
//
//    private boolean rejected;
//
//    private EPerson reader;
//
//    private boolean toFix;
//
//    private Date rejectDate;
//
//    private Date readDate;
//
//    private EPerson admin;
//
//    private Date adminDate;
    
    private Map<DuplicateDecisionType, DuplicateDecisionValue> decisions = new HashMap<DuplicateDecisionType, DuplicateDecisionValue>();
	
    public int getDedupID()
    {
        return dedupID;
    }

    public void setDedupID(int dedupID)
    {
        this.dedupID = dedupID;
    }

    public BrowsableDSpaceObject getDuplicateItem()
    {
        return duplicateItem;
    }

    public void setDuplicateItem(BrowsableDSpaceObject duplicateItem)
    {
        this.duplicateItem = duplicateItem;
    }

	public int getDuplicateItemType() {
		return duplicateItemType;
	}

	public void setDuplicateItemType(int duplicateItemType) {
		this.duplicateItemType = duplicateItemType;
	}

//    public EPerson getEperson()
//    {
//        return eperson;
//    }
//
//    public void setEperson(EPerson eperson)
//    {
//        this.eperson = eperson;
//    }
//
//    public void setRejected(boolean rejected)
//    {
//        this.rejected = rejected;
//    }
//
//    public boolean isRejected()
//    {
//        return rejected;
//    }

    public String getNote()
    {
        return note;
    }

    public void setNote(String note)
    {
        this.note = note;
    }

//    public boolean isNotDuplicate()
//    {
//        return notDuplicate;
//    }
//
//    public void setNotDuplicate(boolean notDuplicate)
//    {
//        this.notDuplicate = notDuplicate;
//    }
//
//    public EPerson getAdmin()
//    {
//        return admin;
//    }
//
//    public EPerson getReader()
//    {
//        return reader;
//    }
//
//    public void setReader(EPerson reader)
//    {
//        this.reader = reader;
//    }
//
//    public boolean isToFix()
//    {
//        return toFix;
//    }
//
//    public void setToFix(boolean toFix)
//    {
//        this.toFix = toFix;
//    }
//
//    public Date getRejectDate()
//    {
//        return rejectDate;
//    }
//
//    public void setRejectDate(Date rejectDate)
//    {
//        this.rejectDate = rejectDate;
//    }
//
//    public Date getReadDate()
//    {
//        return readDate;
//    }
//
//    public void setReadDate(Date readDate)
//    {
//        this.readDate = readDate;
//    }
//
//    public Date getAdminDate()
//    {
//        return adminDate;
//    }
//
//    public void setAdminDate(Date adminDate)
//    {
//        this.adminDate = adminDate;
//    }
//
//    public void setAdmin(EPerson admin)
//    {
//        this.admin = admin;
//    }
    
    public DuplicateDecisionValue getDecision(DuplicateDecisionType type) {
		return decisions.get(type);
	}

	public void setDecision(DuplicateDecisionType type, DuplicateDecisionValue decision) {
		this.decisions.put(type, decision);
	}
}
