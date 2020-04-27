package org.dspace.app.cris.integration.authority;

import org.dspace.app.cris.service.ApplicationService;
import org.dspace.app.cris.unpaywall.model.Unpaywall;
import org.dspace.content.Bundle;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.core.ConfigurationManager;
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
    if (dso instanceof Item) {
    	if (event.getEventType() == Event.ADD) {
    		Item item = (Item) dso;
        		Bundle[] bundle = item.getBundles("ORIGINAL");
        		if(bundle.length < 0) {
	        		String md = item.getMetadata(ConfigurationManager.getProperty("unpaywall", "metadata.doi"));
	        		Unpaywall unpaywall = applicationService.uniqueByDOI(md);
	        		applicationService.delete(Unpaywall.class, unpaywall.getId());
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
