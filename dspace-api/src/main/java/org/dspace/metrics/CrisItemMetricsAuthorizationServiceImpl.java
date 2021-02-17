/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.metrics;

import java.sql.SQLException;
import java.util.UUID;

import org.dspace.authorize.service.AuthorizeService;
import org.dspace.content.Item;
import org.dspace.content.service.ItemService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class CrisItemMetricsAuthorizationServiceImpl implements CrisItemMetricsAuthorizationService {

    private static final Logger log = LoggerFactory.getLogger(CrisItemMetricsAuthorizationServiceImpl.class);

    @Autowired
    protected AuthorizeService authorizeService;

    @Autowired
    protected ItemService itemService;

    @Override
    public boolean isAuthorized(Context context, UUID itemUuid) {
        return isAuthorized(context, null, itemUuid);
    }

    @Override
    public boolean isAuthorized(Context context, Item item) {
        return isAuthorized(context, item, null);
    }

    private boolean isAuthorized(Context context, Item item, UUID itemUuid) {

        try {

            // anonymous user
            if (context.getCurrentUser() == null) {
                return false;
            }

            Item target = item != null ? item : itemService.find(context, itemUuid);

            // can't find a item to check
            if (target == null) {
                return false;
            }

            return authorizeService.authorizeActionBoolean(context, target, Constants.READ);

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
        return false;

    }


}
