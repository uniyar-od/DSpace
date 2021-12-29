/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.customurl.service;

import static org.dspace.content.Item.ANY;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.dspace.app.customurl.CustomUrlService;
import org.dspace.content.Item;
import org.dspace.content.MetadataValue;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.dspace.core.exception.SQLRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implementation of {@link CustomUrlService}.
 *
 * @author Luca Giamminonni (luca.giamminonni at 4science.it)
 *
 */
public class CustomUrlServiceImpl implements CustomUrlService {

    @Autowired
    private ItemService itemService;

    @Override
    public String getCustomUrl(Item item) {
        return itemService.getMetadataFirstValue(item, "cris", "customurl", null, Item.ANY);
    }

    @Override
    public List<String> getOldCustomUrls(Item item) {
        return itemService.getMetadataByMetadataString(item, "cris.customurl.old").stream()
            .map(MetadataValue::getValue)
            .collect(Collectors.toList());
    }

    @Override
    public void replaceCustomUrl(Context context, Item item, String newUrl) {
        try {
            itemService.clearMetadata(context, item, "cris", "customurl", null, ANY);
            itemService.addMetadata(context, item, "cris", "customurl", null, null, newUrl);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public void deleteAnyOldCustomUrlEqualsTo(Context context, Item item, String customUrl) {

        List<MetadataValue> redirectedUrls = getRedirectedUrlMetadataValuesWithValue(item, customUrl);
        if (CollectionUtils.isNotEmpty(redirectedUrls)) {
            deleteMetadataValues(context, item, redirectedUrls);
        }

    }

    @Override
    public void addOldCustomUrl(Context context, Item item, String url) {
        try {
            itemService.addMetadata(context, item, "cris", "customurl", "old", null, url);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public void deleteAllOldCustomUrls(Context context, Item item) {
        try {
            itemService.clearMetadata(context, item, "cris", "customurl", "old", ANY);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public void deleteOldCustomUrlByIndex(Context context, Item item, int index) {

        List<MetadataValue> redirectedUrls = itemService.getMetadata(item, "cris", "customurl", "old", ANY);

        if (index >= redirectedUrls.size()) {
            throw new IllegalArgumentException(
                "The provided index is not consistent with the cardinality of the old custom urls");
        }

        deleteMetadataValues(context, item, List.of(redirectedUrls.get(index)));
    }

    private void deleteMetadataValues(Context context, Item item, List<MetadataValue> metadataValues) {
        try {
            itemService.removeMetadataValues(context, item, metadataValues);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    private List<MetadataValue> getRedirectedUrlMetadataValuesWithValue(Item item, String value) {
        return itemService.getMetadataByMetadataString(item, "cris.customurl.old").stream()
            .filter(metadataValue -> metadataValue.getValue().equals(value))
            .collect(Collectors.toList());
    }

}
