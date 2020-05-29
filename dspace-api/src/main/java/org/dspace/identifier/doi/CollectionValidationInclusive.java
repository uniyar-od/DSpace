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
import java.util.List;
import java.util.Map;

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
	private Boolean checkFileDefault;
	private Boolean checkFile;
	private String checkMetadata;
	private String checkMetadataValue;
	private Map<String,String> defaultConfiguration;
	private Map<String,String> noCheckConfiguration;
 
	private ArrayList<String> collectionList;
	Logger log = Logger.getLogger(CollectionValidationInclusive.class);

	@Override
	public boolean canRegister(Context context,DSpaceObject dso) {
		
		checkFileDefault = BooleanUtils.toBooleanObject(defaultConfiguration.get("checkFile"));
		checkFile = BooleanUtils.toBooleanObject(noCheckConfiguration.get("checkFile"));
		checkMetadataValue = defaultConfiguration.get("checkMetadataValue");
		checkMetadata = defaultConfiguration.get("checkMetadata");
		
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
						if (StringUtils.isNotBlank(checkMetadata) && StringUtils.isNotBlank(checkMetadataValue)) {
							
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

	public Map<String,String> getDefaultConfiguration() {
		return defaultConfiguration;
	}

	public void setDefaultConfiguration(Map<String,String> defaultConfiguration) {
		this.defaultConfiguration = defaultConfiguration;
	}

	public Map<String,String> getNoCheckConfiguration() {
		return noCheckConfiguration;
	}

	public void setNoCheckConfiguration(Map<String,String> noCheckConfiguration) {
		this.noCheckConfiguration = noCheckConfiguration;
	}
	

}
