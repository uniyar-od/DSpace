/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.xoai.filter;

import java.sql.SQLException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.dspace.app.cris.model.ACrisObject;
import org.dspace.app.cris.util.Researcher;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.content.Item;
import org.dspace.core.Constants;
import org.dspace.handle.HandleManager;
import org.dspace.xoai.data.DSpaceItem;
import org.dspace.xoai.filter.results.SolrFilterResult;

/**
 * 
 * @author Lyncode Development Team <dspace@lyncode.com>
 */
public class DSpaceAuthorizationFilter extends DSpaceFilter
{
    private static Logger log = LogManager.getLogger(DSpaceAuthorizationFilter.class);

    @Override
    public boolean isShown(DSpaceItem item)
    {
        boolean pub = false;
        try
        {
            // If Handle or Item are not found, return false
            String handle = DSpaceItem.parseHandle(item.getHandle());
            if (handle == null)
                return false;

            Item dspaceItem = (Item) HandleManager.resolveToObject(context, handle);
            if (dspaceItem == null) {
            	ACrisObject crisObj = new Researcher().getApplicationService().getEntityByUUID(handle);
				return crisObj != null && Boolean.valueOf(true).equals(crisObj.getStatus());
            }

            // Check if READ access allowed on Item
            pub = AuthorizeManager.authorizeActionBoolean(context, dspaceItem, Constants.READ);
        }
        catch (SQLException ex)
        {
            log.error(ex.getMessage(), ex);
        }
        return pub;
    }

    @Override
    public SolrFilterResult buildSolrQuery()
    {
        return new SolrFilterResult("item.public:true");
    }

}
