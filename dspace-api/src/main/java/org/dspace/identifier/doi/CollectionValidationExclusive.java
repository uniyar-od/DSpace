/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.identifier.doi;

/**
 * This implementation provide the possibility prevent/permit DOI registration based on Item Owning Collection;
 * 
 * @author Riccardo Fazio (riccardo.fazio at 4science.it)
 *
 */

import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.content.Collection;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.WorkspaceItem;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.workflow.WorkflowItem;

public class CollectionValidationExclusive implements IdentifierRegisterValidation {
	
	private Boolean checkFile;
	private String checkMetadata;
	private String checkMetadataValue;
	private boolean checkBoolValue = BooleanUtils.toBooleanObject(checkMetadataValue);

	private ArrayList<String> collectionList;
	Logger log = Logger.getLogger(CollectionValidationExclusive.class);

	@Override
	public boolean canRegister(Context context,DSpaceObject dso) {
		boolean register = false;
		if(dso.getType() == Constants.ITEM) {
			Item item = (Item) dso;
			try {
				String collHandle = null;
				if(!item.isInProgressSubmission()) {
					Collection owningCollection = item.getOwningCollection();
					if (owningCollection != null) {
						collHandle = owningCollection.getHandle();
					}
				} 
				else {
					WorkspaceItem wsi =WorkspaceItem.findByItem(context, item);
					if(wsi != null) {
						collHandle =wsi.getCollection().getHandle();
					} else {
						WorkflowItem wfi = WorkflowItem.findByItem(context, item);
						collHandle = wfi.getCollection().getHandle();
					}
				}
				
					if (checkFile) {
						if (StringUtils.isNotBlank(checkMetadata) && checkBoolValue) {
							
							String metadata = item.getMetadata(checkMetadata);
							if (!(StringUtils.equals(metadata, checkMetadataValue) && item.hasUploadedFiles())) {
								register = false;
							}
							
						}else {
							register = item.hasUploadedFiles();
						}
					}else {
						String metadata = item.getMetadata(checkMetadata);
						register = StringUtils.equals(metadata, checkMetadataValue);
					}
			} catch(SQLException e) {
				log.error(e.getMessage(), e);
			} catch(NullPointerException e) {
				log.error(e.getMessage(), e);
			}
		}
		return register;
	}

	public ArrayList<String> getCollectionList() {
		return collectionList;
	}

	public void setCollectionList(ArrayList<String> collectionList) {
		this.collectionList = collectionList;
	}

	public Boolean getCheckFile() {
		return checkFile;
	}

	public void setCheckFile(Boolean checkFile) {
		this.checkFile = checkFile;
	}

	public String getCheckMetadata() {
		return checkMetadata;
	}

	public void setCheckMetadata(String checkMetadata) {
		this.checkMetadata = checkMetadata;
	}

	public String getCheckMetadataValue() {
		return checkMetadataValue;
	}

	public void setCheckMetadataValue(String checkMetadataValue) {
		this.checkMetadataValue = checkMetadataValue;
	}
	

}
