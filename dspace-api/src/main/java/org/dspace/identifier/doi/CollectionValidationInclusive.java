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

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.content.Collection;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.WorkspaceItem;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.workflow.WorkflowItem;

public class CollectionValidationInclusive implements IdentifierRegisterValidation {
	
	/**
	 * Switch from exclude collection list if true; permits only to collection list if false; 
	 */
	private String checkFile;
	private String checkMetadata;
	private boolean checkBoolValue;
	private boolean checkBoolFile;
	private String collectionHandle;
 
	Logger log = Logger.getLogger(CollectionValidationInclusive.class);

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

			checkBoolValue = BooleanUtils.toBooleanObject(item.getMetadata(checkMetadata));
			checkBoolFile = BooleanUtils.toBooleanObject(checkFile);
				
			if(collectionHandle.equalsIgnoreCase(collHandle)) {
				register = true;
			}
				if (register && checkBoolFile) {
					if (StringUtils.isNotBlank(checkMetadata) && checkBoolValue) {
						
						if (!(checkBoolValue && item.hasUploadedFiles())) {
							register = false;
						}
						
					}else {
						register = item.hasUploadedFiles();
					}
				}else {
					register = checkBoolValue;
				}
			} catch(SQLException e) {
				log.error(e.getMessage(), e);
			} catch(NullPointerException e) {
				log.error(e.getMessage(), e);
			}
		}
		return register;
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
