/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.identifier.doi;

/**
 * This implementation provide the possibility prevent/permit DOI registration based on Item Owning Collection
 * and other criteria, such as check on metadata or if item has files
 * 
 * @author Riccardo Fazio (riccardo.fazio at 4science.it)
 *
 */
import java.sql.SQLException;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.dspace.content.WorkspaceItem;
import org.dspace.core.Context;
import org.dspace.workflow.WorkflowItem;

public abstract class ACollectionValidation implements IdentifierRegisterValidation {

	Logger log = Logger.getLogger(ACollectionValidation.class);

	public boolean isToRegister (boolean register, String checkFile, String checkMetadata, Item item) {
		if (register) {
			if (BooleanUtils.toBoolean(checkFile)) {
				try {
					register = item.hasUploadedFiles();
				} catch (SQLException e) {
					log.error(e.getMessage(), e);
				}
			}
			if (StringUtils.isNotBlank(checkMetadata)) {
				register = register && BooleanUtils.toBoolean(item.getMetadata(checkMetadata));
			}
		}

		return register;
	}
	
	public String getHandle(Context context, Item item) {
		String collHandle = null;
		try {
			if(!item.isInProgressSubmission()) {
				Collection owningCollection = item.getOwningCollection();
				if (owningCollection != null) {
					collHandle = owningCollection.getHandle();
				}
			} 
			else {
				WorkspaceItem wsi = WorkspaceItem.findByItem(context, item);
				if(wsi != null) {
					collHandle =wsi.getCollection().getHandle();
				} else {
					WorkflowItem wfi = WorkflowItem.findByItem(context, item);
					collHandle = wfi.getCollection().getHandle();
				}
			}
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
		}
		return collHandle;
	}
}