package org.dspace.versioning;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.dspace.content.Collection;
import org.dspace.content.DCDate;
import org.dspace.content.Item;
import org.dspace.content.Metadatum;
import org.dspace.core.Context;
import org.dspace.storage.rdbms.DatabaseManager;
import org.dspace.storage.rdbms.TableRow;
import org.dspace.utils.DSpace;
import org.springframework.beans.factory.annotation.Required;

public class VersioningServiceSameItemImpl implements VersioningService {

	private VersionHistoryDAO versionHistoryDAO;
	private VersionDAO versionDAO;
	private ItemVersionProvider provider;

	/** Service Methods */
	public Version createNewVersion(Context c, int itemId) {
		return createNewVersion(c, itemId, null);
	}

	public Item createItemCopy(Context context, Item toCopy) {
		Item itemTmp = provider.createNewItemAndAddItInWorkspace(context, toCopy);
		provider.updateItemState(context, itemTmp, toCopy);
		return itemTmp;
	}

	public Version createNewVersion(Context c, int itemId, String summary) {
		try {
			Item item = Item.find(c, itemId);
			VersionHistory vh = versionHistoryDAO.find(c, item.getID(), versionDAO);
			if (vh == null) {
				// first time: create 2 versions, .1(old version) and .2(new version)
				vh = versionHistoryDAO.create(c);

				// get dc:date.accessioned to be set as first version date...
				Metadatum[] values = item.getMetadata("dc", "date", "accessioned", Item.ANY);
				Date versionDate = new Date();
				if (values != null && values.length > 0) {
					String date = values[0].value;
					versionDate = new DCDate(date).toDate();
				}
				createVersion(c, vh, item, "", versionDate);
			}
			Version version = provider.createNewVersion(c, this, vh, summary, item);

			return version;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void removeVersion(Context c, int versionID) {
		Version version = versionDAO.find(c, versionID);
		if (version != null) {
			removeVersion(c, version);
		}
	}

	public void removeVersion(Context c, Item item) {
		Version version = versionDAO.findByItem(c, item);
		if (version != null) {
			removeVersion(c, version);
		}
	}

	protected void removeVersion(Context c, Version version) {
		try {
			VersionHistory history = versionHistoryDAO.findById(c, version.getVersionHistoryID(), versionDAO);

			Version previous=history.getPrevious(version);
			if(history.isLastVersion(version) && previous!=null) {
				AbstractVersionProvider versionProvider = new DSpace().getServiceManager()
						.getServiceByName("sameItemVersionProvider", SameItemVersionProvider.class);
				
				Item itemPrev = previous.getItem();
				Item latest = version.getItem();
				
				//To keep the same item we need to copy the previous item metadata on the latest
				//switch the references of the item and then delete the latest version

				// restore the item with the original metadata
				versionProvider.clearMetadataOnItem(latest);
				try {
					versionProvider.copyMetadata(latest, itemPrev);
					latest.update();
				}catch(Exception e) {
					throw new RuntimeException("Cannot clear item metadata!");
				}
				
				previous.setItemID(latest.getID());
				TableRow tableRow = ((VersionImpl)previous).getMyRow();
				tableRow.setTable("versionitem");
	            DatabaseManager.update(((VersionImpl)previous).getMyContext(), tableRow);

				
				version.setItemID(itemPrev.getID());
				tableRow = ((VersionImpl)version).getMyRow();
				tableRow.setTable("versionitem");
	            DatabaseManager.update(((VersionImpl)version).getMyContext(), tableRow);
				
			}
			
			provider.deleteVersionedItem(c, version, history);
			versionDAO.delete(c, version.getVersionId());

			history.remove(version);

			if (history.isEmpty()) {
				versionHistoryDAO.delete(c, version.getVersionHistoryID(), versionDAO);
			}
			// Delete the item linked to the version
			Item item = version.getItem();
			Collection[] collections = item.getCollections();

			// Remove item from all the collections it's in (so our item is also deleted)
			for (Collection collection : collections) {
				collection.removeItem(item);
			}
		} catch (Exception e) {
			c.abort();
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public Version getVersion(Context c, int versionID) {
		return versionDAO.find(c, versionID);
	}

	public Version restoreVersion(Context c, int versionID) {
		return restoreVersion(c, versionID, null);
	}

	public Version restoreVersion(Context c, int versionID, String summary) {
		return null;
	}

	public VersionHistory findVersionHistory(Context c, int itemId) {
		return versionHistoryDAO.find(c, itemId, versionDAO);
	}

	public Version updateVersion(Context c, int itemId, String summary) {
		VersionImpl version = versionDAO.findByItemId(c, itemId);
		version.setSummary(summary);
		versionDAO.update(version);
		return version;
	}

	public Version getVersion(Context c, Item item) {
		return versionDAO.findByItemId(c, item.getID());
	}

	public VersionImpl createVersion(Context c, VersionHistory vh, Item itemNew, String summary, Date date) {
		try {
			VersionImpl version = versionDAO.create(c);

			int originalItemId;
			
			//keep the same item ID
			List<Version> versions = vh.getVersions();
			if(versions!=null) {
				Version latestVersion = versions.get(versions.size() - 1);
				originalItemId=latestVersion.getItemID();
				latestVersion.setItemID(itemNew.getID());
				getVersionDAO().update((VersionImpl) latestVersion);
			}else {
				//first time versioning this item
				originalItemId=itemNew.getID();
			}
			version.setVersionNumber(getNextVersionNumer(vh.getLatestVersion()));
			version.setVersionDate(date);
			version.setEperson(Item.find(c, originalItemId).getSubmitter());
			version.setItemID(originalItemId);
			version.setSummary(summary);
			version.setVersionHistory(vh.getVersionHistoryId());
			versionDAO.update(version);
			vh.add(version);
			return version;
		} catch (SQLException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	protected int getNextVersionNumer(Version latest) {
		if (latest == null)
			return 1;

		return latest.getVersionNumber() + 1;
	}

	public VersionHistoryDAO getVersionHistoryDAO() {
		return versionHistoryDAO;
	}

	public void setVersionHistoryDAO(VersionHistoryDAO versionHistoryDAO) {
		this.versionHistoryDAO = versionHistoryDAO;
	}

	public VersionDAO getVersionDAO() {
		return versionDAO;
	}

	public void setVersionDAO(VersionDAO versionDAO) {
		this.versionDAO = versionDAO;
	}

	@Required
	public void setProvider(ItemVersionProvider provider) {
		this.provider = provider;
	}

	@Override
	public ItemVersionProvider getItemVersionProvider() {
		return provider;
	}

}
