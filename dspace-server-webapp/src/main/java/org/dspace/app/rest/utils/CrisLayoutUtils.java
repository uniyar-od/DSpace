/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.utils;

import java.sql.SQLException;
import java.util.List;

import org.dspace.app.rest.converter.ItemConverter;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.layout.CrisLayoutBox;
import org.dspace.layout.CrisLayoutField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Utility methods
 *
 * @author Mykhaylo Boychuk (mykhaylo.boychuk at 4science.it)
 */
@Component
public class CrisLayoutUtils {

    @Autowired
    private ItemConverter itemConverter;

    public boolean isAccessibleBox(Context context, Item item, CrisLayoutBox box) {
        List<CrisLayoutField> fields = box.getLayoutFields();
        for (CrisLayoutField field : fields) {
            try {
                if (itemConverter.checkMetadataFieldVisibility(context, item, field.getMetadataField())) {
                    return true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
