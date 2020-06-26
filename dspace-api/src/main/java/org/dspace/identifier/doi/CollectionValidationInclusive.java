/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.identifier.doi;

import org.apache.log4j.Logger;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.core.Constants;
import org.dspace.core.Context;

public class CollectionValidationInclusive extends ACollectionValidation {

	private String checkFile;
	private String checkMetadata;
	private String collectionHandle;

	Logger log = Logger.getLogger(CollectionValidationInclusive.class);

	@Override
	public boolean canRegister(Context context, DSpaceObject dso) {
		if (dso.getType() == Constants.ITEM) {
			Item item = (Item) dso;
			String collHandle = getHandle(context, item);

			if (collectionHandle.equalsIgnoreCase(collHandle)) {
				return isToRegister(true, checkFile, checkMetadata, item);
			}
		}
		return false;
	}

	public String getCollectionHandle() {
		return collectionHandle;
	}

	public void setCollectionHandle(String collectionHandle) {
		this.collectionHandle = collectionHandle;
	}

	public String getCheckFile() {
		return checkFile;
	}

	public void setCheckFile(String checkFile) {
		this.checkFile = checkFile;
	}

	public String getCheckMetadata() {
		return checkMetadata;
	}

	public void setCheckMetadata(String checkMetadata) {
		this.checkMetadata = checkMetadata;
	}

}
