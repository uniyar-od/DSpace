package org.dspace.versioning;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.storage.rdbms.TableRow;

public class VersionHistorySameItemImpl extends VersionHistoryImpl {

	private Comparator<Version> comparator = new VersionComparator();

	protected VersionHistorySameItemImpl(VersionHistoryDAO vhDAO) {
		super(vhDAO);
	}

	protected VersionHistorySameItemImpl(Context c, TableRow row) {
		super(c, row);
	}

	public int getVersionHistoryId() {
		return myRow.getIntColumn(VersionHistoryDAO.VERSION_HISTORY_ID);
	}

	public Version getPrevious(Version version) {

		int index = versions.indexOf(version);

		if (index == 0) {
			return null;
		}

		return versions.get(index - 1);

	}

	public Version getNext(Version version) {
		int index = versions.indexOf(version);

		if ((index + 1) == versions.size())
			return null;

		return versions.get(index + 1);
	}

	public Version getVersion(Item item) {
		for (Version v : versions) {
			if (v.getItem().getID() == item.getID()) {
				return v;
			}
		}
		return null;
	}

	public boolean hasNext(Item item) {
		Version version = getVersion(item);
		return hasNext(version);
	}

	public boolean hasNext(Version version) {
		return getNext(version) != null;
	}

	public List<Version> getVersions() {
		return versions;
	}

	public void setVersions(List<Version> versions) {
		this.versions = versions;
		this.versions.sort(comparator);
	}

	public void add(Version version) {
		if (versions == null)
			versions = new ArrayList<Version>();
		versions.add(version);
		versions.sort(comparator);
	}

	public Version getLatestVersion() {
		if (versions == null || versions.size() == 0) {
			return null;
		}

		return versions.get(versions.size() - 1);
	}

	public Version getFirstVersion() {
		if (versions == null || versions.size() == 0) {
			return null;
		}

		return versions.get(0);
	}

	public boolean isFirstVersion(Version version) {
		Version first = versions.get(0);
		return first.equals(version);
	}

	public boolean isLastVersion(Version version) {
		Version last = versions.get(versions.size() - 1);
		return last.equals(version);
	}

	public void remove(Version version) {
		versions.remove(version);
	}

	public boolean isEmpty() {
		return versions.size() == 0;
	}

	public int size() {
		return versions.size();
	}

	protected TableRow getMyRow() {
		return myRow;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		VersionHistorySameItemImpl that = (VersionHistorySameItemImpl) o;
		return versionHistoryId == that.versionHistoryId;

	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 79 * hash + (this.getVersionHistoryId() ^ (this.getVersionHistoryId() >>> 32));
		return hash;
	}
}