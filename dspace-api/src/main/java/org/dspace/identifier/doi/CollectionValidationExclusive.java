/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.identifier.doi;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.core.Constants;
import org.dspace.core.Context;

public class CollectionValidationExclusive extends ACollectionValidation {

	private String checkFile;
	private String checkMetadata;

	private ArrayList<String> collectionList;
	Logger log = Logger.getLogger(CollectionValidationExclusive.class);

	public boolean canRegister(Context context, DSpaceObject dso) {
		boolean register = false;
		if (dso.getType() == Constants.ITEM) {
			Item item = (Item) dso;
			String collHandle = getHandle(context, item);

			if (!collectionList.contains(collHandle)) {
				register = true;
			}

			return isToRegister(register, checkFile, checkMetadata, item);
		}
		return register;
	}

	public ArrayList<String> getCollectionList() {
		return collectionList;
	}

	public void setCollectionList(ArrayList<String> collectionList) {
		this.collectionList = collectionList;
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
