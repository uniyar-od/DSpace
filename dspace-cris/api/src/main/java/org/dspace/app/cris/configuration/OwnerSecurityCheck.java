/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.cris.configuration;

import org.dspace.app.cris.model.ACrisObject;
import org.dspace.content.DSpaceObject;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;

public class OwnerSecurityCheck implements SecurityCheck {

    public boolean isAuthorized(Context context, DSpaceObject dso) {
        if (context != null && dso instanceof ACrisObject) {
            EPerson currentUser = context.getCurrentUser();
            ACrisObject cris = (ACrisObject)dso;
            return cris.isOwner(currentUser);
        }

        return false;
    }

}
