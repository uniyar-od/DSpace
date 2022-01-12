/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.versioning;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.dspace.content.MetadataField;
import org.dspace.content.MetadataSchema;
import org.dspace.content.Metadatum;
import org.dspace.content.WorkspaceItem;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.event.Consumer;
import org.dspace.event.Event;
import org.dspace.utils.DSpace;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

/**
 *
 *
 * @author Fabio Bolognesi (fabio at atmire dot com)
 * @author Mark Diggory (markd at atmire dot com)
 * @author Ben Bosman (ben at atmire dot com)
 */
public class VersioningConsumer implements Consumer {

    private static Set<Item> itemsToProcess;
    
    public void initialize() throws Exception {}

    public void finish(Context ctx) throws Exception {}

    public void consume(Context ctx, Event event) throws Exception {
        if(itemsToProcess == null){
            itemsToProcess = new HashSet<Item>();
        }

        int st = event.getSubjectType();
        int et = event.getEventType();

        if(st == Constants.ITEM && et == Event.INSTALL){
            Item item = (Item) event.getSubject(ctx);

			copySubmissionItemToOriginalItem(ctx, item);

            if (item != null && item.isArchived()) {
                VersionHistory history = retrieveVersionHistory(ctx, item);
                if (history != null) {
                    Version latest = history.getLatestVersion();
                    Version previous = history.getPrevious(latest);
                    if(previous != null){
                        Item previousItem = previous.getItem();
                        if(previousItem != null){
                            previousItem.setArchived(false);
                            itemsToProcess.add(previousItem);
                            //Fire a new modify event for our previous item
                            //Due to the need to reindex the item in the search 
                            //and browse index we need to fire a new event
                            ctx.addEvent(new Event(Event.MODIFY, 
                                    previousItem.getType(), previousItem.getID(),
                                    null, previousItem.getIdentifiers(ctx)));
                        }
                    }
                }
            }
        }
    }

    private void copySubmissionItemToOriginalItem(Context context, Item item) throws Exception {
		AbstractVersionProvider versionProvider = new DSpace().getServiceManager()
				.getServiceByName("sameItemVersionProvider", SameItemVersionProvider.class);
        Set<String> ignoredMetadataFields = versionProvider.getIgnoredMetadataFields();
    	
    	Metadatum[] metadatum = item.getMetadata("local", "fakeitem", "versioning", Item.ANY);
		//if local.fakeitem.versioning is set a capy of all metadata fields is required
		if (metadatum.length > 0) {
			int itemToEditID = Integer.parseInt(metadatum[0].value);
			Item originalItem = Item.find(context, itemToEditID);

			Metadatum[] metadataFields = originalItem.getMetadata(Item.ANY, Item.ANY, Item.ANY, Item.ANY);
			String fullMetadata;

			for (Metadatum field : metadataFields) {
				fullMetadata = String.join(".", field.schema, field.element, field.qualifier);
				
				//delete matadata if it is not contained in the ignore list
				if (!ignoredMetadataFields.contains(fullMetadata)) {
					originalItem.clearMetadata(field.schema, field.element, field.qualifier, Item.ANY);
				}
			}

			versionProvider.copyMetadata(originalItem, item);

			// Remove the tmp metadata copied from the tmp object
			originalItem.clearMetadata("local", "fakeitem", "versioning", Item.ANY);
			originalItem.update();

			// Remove the tmp item from any collection
			Collection[] collections = item.getCollections();
			for (int i = 0; i < collections.length; i++) {
				collections[i].removeItem(item);
			}
			item.update();
			
			WorkspaceItem workspaceItem = WorkspaceItem.findByItem(context, item);
			workspaceItem.deleteAll();
			workspaceItem.update();
			
			
			context.commit();

			// the new reference of item is the original item
			// so keep doing logic on item
			item = originalItem;
		}
		
	}

	public void end(Context ctx) throws Exception {
        if(itemsToProcess != null){
            for(Item item : itemsToProcess){
                ctx.turnOffAuthorisationSystem();
                try {
                    item.update();
                } finally {
                    ctx.restoreAuthSystemState();
                }
            }
            ctx.getDBConnection().commit();
        }

        itemsToProcess = null;
    }


    private static org.dspace.versioning.VersionHistory retrieveVersionHistory(Context c, Item item) {
        VersioningService versioningService = new DSpace().getSingletonService(VersioningService.class);
        return versioningService.findVersionHistory(c, item.getID());
    }
}
