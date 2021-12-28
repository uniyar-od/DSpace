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
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.dspace.app.rest.model.step.CustomUrl;
import org.dspace.content.InProgressSubmission;
import org.dspace.content.Item;
import org.dspace.content.MetadataValue;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.dspace.validation.CustomUrlValidator;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Operation to replace custom defined URL.
 *
 * @author Luca Giamminonni (luca.giamminonni at 4science.it)
 *
 */
public class CustomUrlReplaceOperation extends ReplacePatchOperation<CustomUrl> {

    @Autowired
    private ItemService itemService;

    @Autowired
    private CustomUrlValidator customUrlValidator;

    @Override
    @SuppressWarnings("rawtypes")
    void replace(Context context, HttpServletRequest currentRequest, InProgressSubmission source, String path,
        Object value) throws Exception {

        Item item = source.getItem();

        String newUrl = (String) value;
        String currentUrl = getCurrentUrl(item);

        if (currentUrl != null && currentUrl.equals(newUrl)) {
            return;
        }

        removeAnyRedirectedUrlsEqualsToNewUrl(context, item, newUrl);
        replaceCustomUrl(context, item, newUrl);

        if (customUrlValidator.isValid(context, item, currentUrl)) {
            addOldCustomUrlToRedirectedUrls(context, item, currentUrl);
        }

    }

    private void replaceCustomUrl(Context context, Item item, String newUrl) throws SQLException {
        itemService.clearMetadata(context, item, "cris", "customurl", null, ANY);
        itemService.addMetadata(context, item, "cris", "customurl", null, null, newUrl);
    }

    private void removeAnyRedirectedUrlsEqualsToNewUrl(Context context, Item item, String newUrl) throws SQLException {

        List<MetadataValue> redirectedUrls = getRedirectedUrlMetadataValuesWithValue(item, newUrl);
        if (CollectionUtils.isNotEmpty(redirectedUrls)) {
            itemService.removeMetadataValues(context, item, redirectedUrls);
        }

    }

    private void addOldCustomUrlToRedirectedUrls(Context context, Item item, String currentUrl) throws SQLException {
        itemService.addMetadata(context, item, "cris", "customurl", "old", null, currentUrl);
    }

    private String getCurrentUrl(Item item) {
        return itemService.getMetadataFirstValue(item, "cris", "customurl", null, Item.ANY);
    }

    private List<MetadataValue> getRedirectedUrlMetadataValuesWithValue(Item item, String value) {
        return itemService.getMetadataByMetadataString(item, "cris.customurl.old").stream()
            .filter(metadataValue -> metadataValue.getValue().equals(value))
            .collect(Collectors.toList());
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
