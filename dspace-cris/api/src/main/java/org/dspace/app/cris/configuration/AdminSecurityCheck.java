/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.cris.configuration;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.content.DSpaceObject;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;

public class AdminSecurityCheck implements SecurityCheck {

    private static Logger log = Logger.getLogger(AdminSecurityCheck.class);

    public boolean isAuthorized(Context context, DSpaceObject dso) {
        if (context != null) {
            EPerson currentUser = context.getCurrentUser();
            if(currentUser == null) {
                return false;
            }

            try {
                if (AuthorizeManager.isAdmin(context)) {
                    return true;
                }
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
            }
        }

        return false;
    }

}
