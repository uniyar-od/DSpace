package org.dspace.versioning;

import java.util.Comparator;

public class VersionComparator implements Comparator<Version> {

	@Override
	public int compare(Version o1, Version o2) {
		if(o1.getVersionNumber() < o2.getVersionNumber()) {
			return -1;
		}
		if(o1.getVersionNumber() > o2.getVersionNumber()) {
			return 1;
		}
		return 0;
	}

}
