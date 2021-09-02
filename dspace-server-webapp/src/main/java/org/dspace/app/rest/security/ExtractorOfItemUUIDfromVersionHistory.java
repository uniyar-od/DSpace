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

import org.dspace.core.Context;
import org.dspace.versioning.Version;
import org.dspace.versioning.service.VersioningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Mykhaylo Boychuk (mykhaylo.boychuk at 4science.it)
 */
@Component(value = "extractorOfItemUUID")
public class ExtractorOfItemUUIDfromVersionHistory {

    @Autowired
    private VersioningService versioningService;

    public UUID getItemUUIDFromVersion(Context context, Integer id) {
        Version version = null;
        if (Objects.nonNull(id)) {
            try {
                version = versioningService.getVersion(context, id);
            } catch (SQLException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        return Objects.nonNull(version) ? version.getItem().getID() : null;
    }

}