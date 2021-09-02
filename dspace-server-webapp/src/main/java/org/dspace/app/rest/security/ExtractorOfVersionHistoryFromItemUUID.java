/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.security;
import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;

import org.dspace.app.rest.utils.ContextUtil;
import org.dspace.content.Item;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.dspace.services.RequestService;
import org.dspace.versioning.VersionHistory;
import org.dspace.versioning.service.VersionHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Mykhaylo Boychuk (mykhaylo.boychuk at 4science.it)
 */
@Component(value = "extractorOfVersionHistoryId")
public class ExtractorOfVersionHistoryFromItemUUID {

    @Autowired
    private ItemService itemService;

    @Autowired
    private VersionHistoryService versionHistoryService;

    @Autowired
    private RequestService requestService;

    public Integer getVersionHistoryFromItemUUID(UUID itemUuid) {
        VersionHistory versionHistory = null;
        try {
            HttpServletRequest request = requestService.getCurrentRequest().getHttpServletRequest();
            Context context = ContextUtil.obtainContext(request);
            if (Objects.nonNull(itemUuid)) {
                Item item = itemService.find(context, itemUuid);
                versionHistory = Objects.nonNull(item) ? versionHistoryService.findByItem(context, item) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return Objects.nonNull(versionHistory) ? versionHistory.getID() : null;
    }

}