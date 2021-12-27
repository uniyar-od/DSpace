/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.submit.factory.impl;

import static org.dspace.content.Item.ANY;

import java.sql.SQLException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import org.dspace.app.rest.model.step.CustomUrl;
import org.dspace.content.InProgressSubmission;
import org.dspace.content.Item;
import org.dspace.content.MetadataValue;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Operation to remove redirected URL.
 *
 * @author Luca Giamminonni (luca.giamminonni at 4science.it)
 *
 */
public class RedirectedUrlRemoveOperation extends RemovePatchOperation<CustomUrl> {

    @Autowired
    private ItemService itemService;

    @Override
    @SuppressWarnings("rawtypes")
    void remove(Context context, HttpServletRequest currentRequest, InProgressSubmission source, String path,
        Object value) throws Exception {

        Item item = source.getItem();
        int index = calculateRemoveIndex(path);

        if (index == -1) {
            removeAllRedirectedUrls(context, item);
        } else {
            removeRedirectedUrlByIndex(context, item, index);
        }

    }

    private int calculateRemoveIndex(String path) {
        String absolutePath = getAbsolutePath(path);
        String[] splittedPath = absolutePath.split("/");
        return splittedPath.length == 1 ? -1 : Integer.valueOf(splittedPath[1]);
    }

    private void removeAllRedirectedUrls(Context context, Item item) throws SQLException {
        itemService.clearMetadata(context, item, "cris", "customurl", "old", ANY);
    }

    private void removeRedirectedUrlByIndex(Context context, Item item, int index) throws SQLException {

        List<MetadataValue> redirectedUrls = itemService.getMetadata(item, "cris", "customurl", "old", ANY);

        if (index >= redirectedUrls.size()) {
            throw new IllegalArgumentException(
                "The provided index is not consistent with the cardinality of redirected urls");
        }

        itemService.removeMetadataValues(context, item, List.of(redirectedUrls.get(index)));
    }

    @Override
    protected Class<CustomUrl[]> getArrayClassForEvaluation() {
        return CustomUrl[].class;
    }

    @Override
    protected Class<CustomUrl> getClassForEvaluation() {
        return CustomUrl.class;
    }

}
