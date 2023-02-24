/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.edit.service.impl;

import static java.util.stream.Collectors.groupingBy;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.dspace.content.edit.EditItemMode;
import org.dspace.content.edit.service.EditItemModeValidator;

/**
 * Implementation of {@link EditItemModeValidator}.
 *
 * @author Luca Giamminonni (luca.giamminonni at 4science.it)
 *
 */
public class EditItemModeValidatorImpl implements EditItemModeValidator {

    @Override
    public void validate(Map<String, List<EditItemMode>> editItemModesMap) throws IllegalStateException {

        String errorMessage = "";
        for (String entityType : editItemModesMap.keySet()) {
            List<EditItemMode> editModes = editItemModesMap.get(entityType);
            List<String> duplicatedEditModes = getDuplicatedEditModes(editModes);
            if (isNotEmpty(duplicatedEditModes)) {
                errorMessage = errorMessage + " entity type " + entityType +
                    " has the following duplicated edit modes " + duplicatedEditModes;
            }
        }

        if (StringUtils.isNotBlank(errorMessage)) {
            throw new IllegalStateException("Invalid Edit item mode configuration: " + errorMessage);
        }

    }

    private List<String> getDuplicatedEditModes(List<EditItemMode> editModes) {
        return groupEditItemModesByName(editModes).entrySet().stream()
            .filter(entry -> entry.getValue().size() > 1)
            .map(entry -> entry.getKey())
            .collect(Collectors.toList());
    }

    private Map<String, List<EditItemMode>> groupEditItemModesByName(List<EditItemMode> editModes) {
        return editModes.stream().collect(groupingBy(EditItemMode::getName));
    }

}
