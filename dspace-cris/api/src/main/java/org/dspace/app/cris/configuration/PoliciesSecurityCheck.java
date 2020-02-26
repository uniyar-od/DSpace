/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.cris.configuration;

import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.app.cris.model.ACrisObject;
import org.dspace.content.DSpaceObject;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;

public class PoliciesSecurityCheck implements SecurityCheck {

    private static Logger log = Logger.getLogger(AdminSecurityCheck.class);

    private List<String> epersonFields;
    private List<String> groupFields;

    public boolean isAuthorized(Context context, DSpaceObject dso) {
        if (context != null && dso instanceof ACrisObject) {
            EPerson currentUser = context.getCurrentUser();
            ACrisObject cris = (ACrisObject)dso;

            if (currentUser != null) {
                for (String epersonField : epersonFields) {
                    String epersonPolicy = cris.getMetadata(epersonField);
                    if (StringUtils.isNotBlank(epersonPolicy)) {
                        if (currentUser.getID() == Integer.parseInt(epersonPolicy)) {
                            return true;
                        }
                    }
                }
            }

            for (String groupField : groupFields) {
                List<String> groupPolicies = cris.getMetadataValue(groupField);
                for (String groupPolicy : groupPolicies) {
                    if (StringUtils.isNotBlank(groupPolicy)) {
                        try {
                            Group group = Group.find(context, Integer.parseInt(groupPolicy));
                            if (group != null) {
                                if (Group.isMember(context, group.getID())) {
                                    return true;
                                }
                            }
                        } catch (NumberFormatException | SQLException e) {
                            log.error(e.getMessage(), e);
                        }
                    }
                }
            }
        }

        return false;
    }

    public List<String> getEpersonFields() {
        return epersonFields;
    }

    public void setEpersonFields(List<String> epersonFields) {
        this.epersonFields = epersonFields;
    }

    public List<String> getGroupFields() {
        return groupFields;
    }

    public void setGroupFields(List<String> groupFields) {
        this.groupFields = groupFields;
    }

}
