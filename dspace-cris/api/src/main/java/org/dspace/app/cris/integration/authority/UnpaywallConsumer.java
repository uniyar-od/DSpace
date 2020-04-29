/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.cris.integration.authority;

import org.apache.commons.lang3.StringUtils;
import org.dspace.app.cris.service.ApplicationService;
import org.dspace.app.cris.unpaywall.UnpaywallUtils;
import org.dspace.app.cris.unpaywall.model.Unpaywall;
import org.dspace.content.Bundle;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.event.Consumer;
import org.dspace.event.Event;
import org.dspace.utils.DSpace;

public class UnpaywallConsumer implements Consumer
{
    
    public void consume(Context ctx, Event event) throws Exception {

        ApplicationService applicationService = new DSpace().getServiceManager().getServiceByName(
                "applicationService", ApplicationService.class);
    
        DSpaceObject dso = event.getSubject(ctx);
        if (dso instanceof Bundle && event.getEventType() == Event.ADD) {
            Bundle bundle = (Bundle) dso;
            if (bundle.getName().equals(Constants.DEFAULT_BUNDLE_NAME)) {
                Item[] items = bundle.getItems();
                if (items != null && items.length > 0) {
                    Item item = items[0];
	                String doi = item.getMetadata(ConfigurationManager.getProperty("unpaywall", "metadata.doi"));
	                if (StringUtils.isNotBlank(doi)) {
	                    Unpaywall unpaywall = applicationService.uniqueByDOIAndItemID(UnpaywallUtils.resolveDoi(doi), item.getID());
	                    if (unpaywall != null) {
	                        applicationService.delete(Unpaywall.class, unpaywall.getId());
	                    }
	                }
                }
            }
        }
    }
    
    @Override
    public void initialize() throws Exception
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void end(Context ctx) throws Exception
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void finish(Context ctx) throws Exception
    {
        // TODO Auto-generated method stub
        
    }
}
