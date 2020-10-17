/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.cris.configuration;

import java.sql.SQLException;

import org.dspace.app.cris.model.ACrisObject;
import org.dspace.app.cris.model.VisibilityConstants;
import org.dspace.app.cris.service.ApplicationService;
import org.dspace.app.cris.util.ResearcherPageUtils;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Item;
import org.dspace.content.RootObject;
import org.dspace.content.authority.Choices;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.dspace.utils.DSpace;
import org.springframework.beans.factory.annotation.Autowired;

public class AddRelationMetadataAction extends RelationMetadataAction {
    
    @Autowired
    private ItemService itemService;

    public boolean processSelectedItem(Context context, RootObject target, RootObject selected) throws SQLException, AuthorizeException {
        if (target instanceof ACrisObject && selected instanceof Item) {
            String[] metadataActionSplitted = metadataAction.split("\\.");
            Item item = (Item)selected;
            itemService.addMetadata(context, item, metadataActionSplitted[0],
                    metadataActionSplitted[1],
                    metadataActionSplitted.length == 3 ? metadataActionSplitted[2] : null,
                    null,
                    target.getName(),
                    ((ACrisObject) target).getCrisID(),
                    Choices.CF_ACCEPTED);
            itemService.update(context, item);

            return true;
        }
        else if (target instanceof ACrisObject && selected instanceof ACrisObject) {
            ResearcherPageUtils.
                    buildGenericValue((ACrisObject)selected, (ACrisObject)target, metadataAction, VisibilityConstants.PUBLIC);

            new DSpace().getServiceManager()
                    .getServiceByName(
                            "applicationService",
                            ApplicationService.class)
                    .saveOrUpdate(
                            ((ACrisObject)selected).getCRISTargetClass(),
                            (ACrisObject)selected);

            return true;
        }

        return false;
    }

}
