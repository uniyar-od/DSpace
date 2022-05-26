/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.app.bulkimport.model;

import java.util.List;


/*
 * @author Jurgen Mamani
 */
public class UploadDetails {

    private final String parentId;

    private final String filePath;

    private final String bundleName;

    private final List<MetadataGroup> metadataGroups;

    public UploadDetails(String parentId, String filePath, String bundleName, List<MetadataGroup> metadataGroups) {
        this.parentId = parentId;
        this.filePath = filePath;
        this.bundleName = bundleName;
        this.metadataGroups = metadataGroups;
    }

    public String getParentId() {
        return parentId;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getBundleName() {
        return bundleName;
    }

    public List<MetadataGroup> getMetadataGroups() {
        return metadataGroups;
    }
}
