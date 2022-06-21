/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.authorize.relationship;

import org.dspace.app.profile.service.ResearcherProfileService;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;

/**
 * Implementation of {@link RelationshipItemAuthorizer} that check if the
 * current user is the owner of the given item.
 * 
 * @author Luca Giamminonni (luca.giamminonni at 4science.it)
 *
 */
public class RelationshipItemOwnerAuthorizer implements RelationshipItemAuthorizer {

    private ResearcherProfileService researcherProfileService;

    @Override
    public boolean isRelationshipCreatableOnItem(Context context, Item item) {
        EPerson currentUser = context.getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        return researcherProfileService.isOwnerOfItem(currentUser, item);
    }

    public void setResearcherProfileService(ResearcherProfileService researcherProfileService) {
        this.researcherProfileService = researcherProfileService;
    }

}
