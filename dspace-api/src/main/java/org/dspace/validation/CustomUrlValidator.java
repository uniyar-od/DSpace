/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.validation;

import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.dspace.validation.service.ValidationService.OPERATION_PATH_SECTIONS;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.dspace.app.util.SubmissionStepConfig;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.InProgressSubmission;
import org.dspace.content.Item;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.dspace.validation.model.ValidationError;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implementation of {@link SubmissionStepValidator} to validate custom url
 * section data.
 *
 * @author Luca Giamminonni (luca.giamminonni at 4science.it)
 *
 */
public class CustomUrlValidator implements SubmissionStepValidator {

    private static final String ERROR_VALIDATION_EMPTY = "error.validation.custom-url.empty";

    private static final String ERROR_VALIDATION_INVALID_CHARS = "error.validation.custom-url.invalid-characters";

    private static final String ERROR_VALIDATION_CONFLICT = "error.validation.custom-url.conflict";

    private static final Pattern URL_PATH_PATTERN = Pattern.compile("[\\/.a-zA-Z0-9-]+$");

    @Autowired
    private ItemService itemService;

    private String name;

    @Override
    public List<ValidationError> validate(Context context, InProgressSubmission<?> obj, SubmissionStepConfig config) {

        Item item = obj.getItem();

        String customUrl = getCustomUrl(item);
        if (customUrl == null) {
            return List.of();
        }

        if (customUrl.isBlank() && hasRedirectUrls(item)) {
            return validationError(ERROR_VALIDATION_EMPTY, config);
        }

        if (hasInvalidCharacters(customUrl)) {
            return validationError(ERROR_VALIDATION_INVALID_CHARS, config);
        }

        if (existsAnotherItemWithSameCustomUrl(context, item, customUrl)) {
            return validationError(ERROR_VALIDATION_CONFLICT, config);
        }

        return List.of();
    }

    private String getCustomUrl(Item item) {
        return itemService.getMetadataFirstValue(item, "cris", "customurl", null, Item.ANY);
    }

    private boolean hasRedirectUrls(Item item) {
        return isNotEmpty(itemService.getMetadataByMetadataString(item, "cris.customurl.old"));
    }

    private boolean hasInvalidCharacters(String customUrl) {
        return !URL_PATH_PATTERN.matcher(customUrl).matches();
    }

    private boolean existsAnotherItemWithSameCustomUrl(Context context, Item item, String customUrl) {
        return convertToStream(findItemsByCustomUrl(context))
            .anyMatch(foundItem -> foundItem.isArchived() && !foundItem.getID().equals(item.getID()));
    }

    private Iterator<Item> findItemsByCustomUrl(Context context) {
        try {
            return itemService.findUnfilteredByMetadataField(context, "cris", "customurl", null, Item.ANY);
        } catch (SQLException | AuthorizeException e) {
            throw new RuntimeException(e);
        }
    }

    private Stream<Item> convertToStream(Iterator<Item> iterator) {
        return stream(spliteratorUnknownSize(iterator, ORDERED), false);
    }

    private List<ValidationError> validationError(String message, SubmissionStepConfig config) {
        ValidationError error = new ValidationError();
        error.setMessage(message);
        error.getPaths().add("/" + OPERATION_PATH_SECTIONS + "/" + config.getId());
        return List.of(error);
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
