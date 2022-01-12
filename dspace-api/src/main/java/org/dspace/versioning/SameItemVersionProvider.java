package org.dspace.versioning;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.authorize.ResourcePolicy;
import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.dspace.content.Metadatum;
import org.dspace.content.WorkspaceItem;
import org.dspace.core.Context;
import org.dspace.identifier.IdentifierException;
import org.dspace.identifier.IdentifierService;
import org.dspace.utils.DSpace;

public class SameItemVersionProvider extends AbstractVersionProvider implements ItemVersionProvider {


    public Item createNewItemAndAddItInWorkspace(Context context, Item nativeItem) {
        try
        {
            WorkspaceItem workspaceItem = WorkspaceItem.create(context, nativeItem.getOwningCollection(), false);
            Item itemNew = workspaceItem.getItem();
            itemNew.update();
            return itemNew;
        }catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }catch (AuthorizeException e) {
           throw new RuntimeException(e.getMessage(), e);
        }catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void deleteVersionedItem(Context c, Version versionToDelete, VersionHistory history)
    {
        try
        {
            // if versionToDelete is the current version we have to reinstate the previous version
            // and reset canonical
            if(history.isLastVersion(versionToDelete) && history.size() > 1)
            {
                // reset the previous version to archived
                Item item = history.getPrevious(versionToDelete).getItem();
                item.setArchived(true);
                item.update();
            }

            // assign tombstone to the Identifier and reset canonical to the previous version only if there is a previous version
            IdentifierService identifierService = new DSpace().getSingletonService(IdentifierService.class);
            Item itemToDelete=versionToDelete.getItem();
            identifierService.delete(c, itemToDelete);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (AuthorizeException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (IdentifierException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.dspace.versioning.ItemVersionProvider#updateItemState(org.dspace.core.Context, org.dspace.content.Item, org.dspace.content.Item)
     *	used to copy metadata, bundles and bitstreams
     */
    public Item updateItemState(Context c, Item itemNew, Item previousItem)
    {
        try
        {
            copyMetadata(itemNew, previousItem);
            createBundlesAndAddBitstreams(c, itemNew, previousItem);
            IdentifierService identifierService = new DSpace().getSingletonService(IdentifierService.class);
            try
            {
                identifierService.reserve(c, itemNew);
            } catch (IdentifierException e) {
                throw new RuntimeException("Can't create Identifier!");
            }
            // DSpace knows several types of resource policies (see the class
            // org.dspace.authorize.ResourcePolicy): Submission, Workflow, Custom
            // and inherited. Submission, Workflow and Inherited policies will be
            // set automatically as neccessary. We need to copy the custom policies
            // only to preserve customly set policies and embargos (which are
            // realized by custom policies with a start date).
            List<ResourcePolicy> policies = 
                    AuthorizeManager.findPoliciesByDSOAndType(c, previousItem, ResourcePolicy.TYPE_CUSTOM);
            AuthorizeManager.addPolicies(c, policies, itemNew);
            itemNew.update();
            return itemNew;
        }catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (AuthorizeException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.dspace.versioning.ItemVersionProvider#createNewVersion(org.dspace.core.Context, org.dspace.versioning.VersioningService, org.dspace.versioning.VersionHistory, java.lang.String, org.dspace.content.Item)
     *	Creates a new version of the item and adds it in the archive as a snapshot
     */
	@Override
	public Version createNewVersion(Context c, VersioningService versioningService, VersionHistory versionHistory, String summary, Item current) throws SQLException, AuthorizeException, IOException {
		Item itemNew = createNewItemAndAddItInWorkspace(c, current);

		List<Version> versions = versionHistory.getVersions();

		Version latestVersion = versions.get(versions.size() - 1);

		// create new version
		Version version = versioningService.createVersion(c, versionHistory, itemNew, summary, new Date());

		// the latest version is supposed to have associated the original item being
		// updated during the time

		// Complete any update of the Item and new Identifier generation that needs to
		// happen
		updateItemState(c, itemNew, current);

		// so the new version will keep on being associated with the same item id
		// for keeping stats up to date
		version.setItemID(latestVersion.getItemID());

		// the newly created item is considered as a current snapshot of the item
		latestVersion.setItemID(itemNew.getID());

		versioningService.getVersionDAO().update((VersionImpl) version);
		versioningService.getVersionDAO().update((VersionImpl) latestVersion);
		
		//delete the workspaceItem created for the snapshot, we don't need a workspaceItem if we need to archive the item
		WorkspaceItem workspaceItem = WorkspaceItem.findByItem(c, itemNew);
		workspaceItem.deleteAll();
		workspaceItem.update();
		
		return version;
	}

	@Override
	public void finalizeAfterSubmission(Context context, Item item) throws Exception {
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

}
