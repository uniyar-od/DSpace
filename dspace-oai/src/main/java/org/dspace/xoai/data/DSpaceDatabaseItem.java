/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.xoai.data;

import java.util.Date;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.dspace.app.cris.model.ACrisObject;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;

import com.lyncode.xoai.dataprovider.core.ItemMetadata;
import com.lyncode.xoai.dataprovider.core.ReferenceSet;
import com.lyncode.xoai.dataprovider.xml.xoai.Metadata;

/**
 * 
 * @author Lyncode Development Team <dspace@lyncode.com>
 */
public class DSpaceDatabaseItem extends DSpaceItem
{
    private static Logger log = LogManager.getLogger(DSpaceDatabaseItem.class);

    private DSpaceObject item;
    private List<ReferenceSet> sets;

    public DSpaceDatabaseItem(DSpaceObject item, Metadata metadata, List<ReferenceSet> sets)
    {
        this.item = item;
        this.metadata = new ItemMetadata(metadata);
        this.sets = sets;
    }

    @Override
    public Date getDatestamp()
    {
    	if (item instanceof Item) {
    		return ((Item) item).getLastModified();
    	}
    	else if (item instanceof ACrisObject) {
    		return ((ACrisObject) item).getLastModified();
    	}
    	return new Date();
    }

    @Override
    public List<ReferenceSet> getSets()
    {
        return sets;
    }

    @Override
    public boolean isDeleted()
    {
        return item.isWithdrawn();
    }

    private ItemMetadata metadata = null;
    
    @Override
    public ItemMetadata getMetadata()
    {
        return metadata;
    }

    public DSpaceObject getItem()
    {
        return item;
    }

    @Override
    public String getHandle()
    {
        return item.getHandle();
    }
}
