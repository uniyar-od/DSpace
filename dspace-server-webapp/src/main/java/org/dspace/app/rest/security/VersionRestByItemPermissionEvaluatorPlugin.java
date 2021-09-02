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
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.dspace.app.rest.model.VersionRest;
import org.dspace.app.rest.utils.ContextUtil;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.content.Item;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.dspace.services.ConfigurationService;
import org.dspace.services.RequestService;
import org.dspace.services.model.Request;
import org.dspace.versioning.Version;
import org.dspace.versioning.service.VersioningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * @author Mykhaylo Boychuk (4science.it)
 */
@Component
public class VersionRestByItemPermissionEvaluatorPlugin extends RestObjectPermissionEvaluatorPlugin {

    private static final Logger log = LoggerFactory.getLogger(ResourcePolicyRestPermissionEvaluatorPlugin.class);

    @Autowired
    private ItemService itemService;

    @Autowired
    AuthorizeService authorizeService;

    @Autowired
    private RequestService requestService;

    @Autowired
    private VersioningService versioningService;

    @Autowired
    private ConfigurationService configurationService;

    @Override
    public boolean hasDSpacePermission(Authentication authentication, Serializable targetId, String targetType,
            DSpaceRestPermission permission) {

        DSpaceRestPermission restPermission = DSpaceRestPermission.convert(permission);

        if (!DSpaceRestPermission.READ.equals(restPermission)
            || !StringUtils.equalsIgnoreCase(targetType, VersionRest.NAME)) {
            return false;
        }

        Request request = requestService.getCurrentRequest();
        Context context = ContextUtil.obtainContext(request.getServletRequest());
        if (targetId instanceof UUID) {
            UUID itemUuid = UUID.fromString(targetId.toString());
            try {
                Item item = itemService.find(context, itemUuid);
                if (Objects.isNull(item)) {
                    return true;
                }
                boolean isItemAdmin = authorizeService.isAdmin(context, item);
                boolean isAdmin = authorizeService.isAdmin(context);
                boolean onlyAdminCanSeeVersioning =
                                 configurationService.getBooleanProperty("versioning.item.history.view.admin");

                if (onlyAdminCanSeeVersioning && !isAdmin && !isItemAdmin) {
                    return false;
                }
                Version version = versioningService.getVersion(context, item);
                if (Objects.isNull(version)) {
                    return true;
                }
                if (authorizeService.authorizeActionBoolean(context, version.getItem(),
                        restPermission.getDspaceApiActionId())) {
                    return true;
                }
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
            }
        }
        return false;
    }

}