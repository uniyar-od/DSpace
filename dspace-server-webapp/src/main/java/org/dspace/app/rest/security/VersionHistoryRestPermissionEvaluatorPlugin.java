/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.security;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.dspace.app.rest.model.VersionHistoryRest;
import org.dspace.app.rest.utils.ContextUtil;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.core.Context;
import org.dspace.services.ConfigurationService;
import org.dspace.services.RequestService;
import org.dspace.services.model.Request;
import org.dspace.versioning.Version;
import org.dspace.versioning.VersionHistory;
import org.dspace.versioning.service.VersionHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * This class acts as a PermissionEvaluator to decide whether a given request to a Versioning endpoint is allowed to
 * pass through or not
 */
@Component
public class VersionHistoryRestPermissionEvaluatorPlugin extends RestObjectPermissionEvaluatorPlugin {

    private static final Logger log = LoggerFactory.getLogger(VersionHistoryRestPermissionEvaluatorPlugin.class);

    @Autowired
    private RequestService requestService;

    @Autowired
    private AuthorizeService authorizeService;

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private VersionHistoryService versionHistoryService;

    @Override
    public boolean hasDSpacePermission(Authentication authentication, Serializable targetId, String targetType,
                                       DSpaceRestPermission restPermission) {

        if (!StringUtils.equalsIgnoreCase(targetType, VersionHistoryRest.NAME)) {
            return false;
        }

        Request request = requestService.getCurrentRequest();
        Context context = ContextUtil.obtainContext(request.getServletRequest());

        try {
            int versionHistoryId = Integer.parseInt(targetId.toString());
            VersionHistory versionHistory = versionHistoryService.find(context, versionHistoryId);
            if (Objects.isNull(versionHistory)) {
                return true;
            }
            Version version = versionHistoryService.getLatestVersion(context, versionHistory);
            if (Objects.isNull(version)) {
                return true;
            }
            boolean isItemAdmin = authorizeService.isAdmin(context, version.getItem());
            if (configurationService.getBooleanProperty("versioning.item.history.view.admin")
                && !authorizeService.isAdmin(context) && !isItemAdmin) {
                return false;
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
        return true;
    }
}
