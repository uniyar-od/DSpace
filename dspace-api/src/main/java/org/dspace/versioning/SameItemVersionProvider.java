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
		try {
			WorkspaceItem workspaceItem = WorkspaceItem.create(context, nativeItem.getOwningCollection(), false);
			Item itemNew = workspaceItem.getItem();
			itemNew.update();
			return itemNew;
		} catch (SQLException e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch (AuthorizeException e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void deleteVersionedItem(Context c, Version versionToDelete, VersionHistory history) {
		try {
			// if versionToDelete is the current version we have to reinstate the previous
			// version
			// and reset canonical
			if (history.isLastVersion(versionToDelete) && history.size() > 1) {
				// reset the previous version to archived
				Item item = history.getPrevious(versionToDelete).getItem();
				item.setArchived(true);
				item.update();
			}

			// assign tombstone to the Identifier and reset canonical to the previous
			// version only if there is a previous version
			IdentifierService identifierService = new DSpace().getSingletonService(IdentifierService.class);
			Item itemToDelete = versionToDelete.getItem();
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
	 * 
	 * @see
	 * org.dspace.versioning.ItemVersionProvider#updateItemState(org.dspace.core.
	 * Context, org.dspace.content.Item, org.dspace.content.Item) used to copy
	 * metadata, bundles and bitstreams
	 */
	public Item updateItemState(Context c, Item itemNew, Item previousItem) {
		try {
			copyMetadata(itemNew, previousItem);
			createBundlesAndAddBitstreams(c, itemNew, previousItem);
			IdentifierService identifierService = new DSpace().getSingletonService(IdentifierService.class);
			try {
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
			List<ResourcePolicy> policies = AuthorizeManager.findPoliciesByDSOAndType(c, previousItem,
					ResourcePolicy.TYPE_CUSTOM);
			AuthorizeManager.addPolicies(c, policies, itemNew);
			itemNew.update();
			return itemNew;
		} catch (SQLException e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch (AuthorizeException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.dspace.versioning.ItemVersionProvider#createNewVersion(org.dspace.core.
	 * Context, org.dspace.versioning.VersioningService,
	 * org.dspace.versioning.VersionHistory, java.lang.String,
	 * org.dspace.content.Item) Creates a new version of the item and adds it in the
	 * archive as a snapshot
	 */
	@Override
	public Version createNewVersion(Context c, VersioningService versioningService, VersionHistory versionHistory,
			String summary, Item current) throws SQLException, AuthorizeException, IOException {
		Item itemNew = createNewItemAndAddItInWorkspace(c, current);

		// create new version
		Version version = versioningService.createVersion(c, versionHistory, itemNew, summary, new Date());

		// the latest version is supposed to have associated the original item being
		// updated during the time

		// this method has to be called when the item id has already been added to the
		// version history
		updateItemState(c, itemNew, current);

		itemNew.addMetadata("local", "fakeitem", "versioning", Item.ANY, String.valueOf(current.getID()));
		itemNew.update();

		return version;
	}

	@Override
	public Item finalizeAfterSubmission(Context context, Item item) throws Exception {
		AbstractVersionProvider versionProvider = new DSpace().getServiceManager()
				.getServiceByName("sameItemVersionProvider", SameItemVersionProvider.class);
		Set<String> ignoredMetadataFields = versionProvider.getIgnoredMetadataFields();

		// The received item with local.fakeitem.versioning
		// is associated with an archived version of the current item
		// this item needs to be restored with its original values
		// since it is considered as a snapshot of the current version
		//
		// It is used during the submission flow to exploit the automatically
		// created workspace item

		Metadatum[] metadatum = item.getMetadata("local", "fakeitem", "versioning", Item.ANY);
		// if local.fakeitem.versioning is set a copy of all metadata fields is required
		if (metadatum.length > 0) {
			int itemToEditID = Integer.parseInt(metadatum[0].value);
			Item originalItem = Item.find(context, itemToEditID);

			// list all metadata retrieved from the submission flow
			// saved on item
			Metadatum[] metadataFields = item.getMetadata(Item.ANY, Item.ANY, Item.ANY, Item.ANY);
			String fullMetadata;

			// restore the item with the original metadata
			for (Metadatum field : metadataFields) {
				fullMetadata = String.join(".", field.schema, field.element, field.qualifier);

				// delete matadata if it is not contained in the ignore list
				if (!ignoredMetadataFields.contains(fullMetadata)) {
					item.clearMetadata(field.schema, field.element, field.qualifier, Item.ANY);
				}
			}
			versionProvider.copyMetadata(item, originalItem);
			item.update();
			// end restoring metadata fields on archived item

			// save metadata retrieved from the submission flow
			// on the original item
			for (Metadatum field : metadataFields) {
				fullMetadata = String.join(".", field.schema, field.element, field.qualifier);

				// delete matadata if it is not contained in the ignore list
				if (!ignoredMetadataFields.contains(fullMetadata)) {
					originalItem.clearMetadata(field.schema, field.element, field.qualifier, field.language);
					originalItem.addMetadata(field.schema, field.element, field.qualifier, field.language, field.value);
				}
			}
			// end saving metadata on original item

			// Remove the tmp metadata copied from the tmp object
			originalItem.clearMetadata("local", "fakeitem", "versioning", Item.ANY);
			originalItem.update();

			context.commit();

			// the new reference of item is the original item
			// so keep doing logic on item
			return originalItem;
		}
		return item;
	}

	@Override
	public int processCreateNewVersion(Context context, int itemID, String summary) throws Exception {
		VersioningService versioningService = new DSpace().getSingletonService(VersioningService.class);
		versioningService.createNewVersion(context, itemID, summary);

		VersionHistory history = versioningService.findVersionHistory(context, itemID);
		List<Version> versions = history.getVersions();

		// There will always be at least 2 versions, one is the current
		// the other one is the snapshot
		Item archived = versions.get(versions.size() - 2).getItem();
		WorkspaceItem wsi = WorkspaceItem.findByItem(context, archived);

		// the snapshot item is associeted to a workspaceItem that will we used during
		// the submission
		// the snapshot status will be correctly restored when the VersioningConsumer is
		// called
		archived.addMetadata("local", "fakeitem", "versioning", Item.ANY, String.valueOf(archived.getID()));
		archived.update();

		context.commit();
		return wsi.getID();
	}

}
