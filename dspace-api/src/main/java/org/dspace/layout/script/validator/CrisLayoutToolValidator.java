/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.layout.script.validator;

import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.dspace.core.Context;
import org.dspace.layout.script.CrisLayoutToolScript;

/**
 * Validator for excel used by the {@link CrisLayoutToolScript}.
 *
 * @author Luca Giamminonni (luca.giamminonni at 4science.it)
 *
 */
public interface CrisLayoutToolValidator {

    String TAB_SHEET = "tab";

    String TAB2BOX_SHEET = "tab2box";

    String BOX_SHEET = "box";

    String BOX2METADATA_SHEET = "box2metadata";


    String ENTITY_COLUMN = "ENTITY";

    String TYPE_COLUMN = "TYPE";

    String TAB_COLUMN = "TAB";

    String BOX_COLUMN = "BOX";

    String BOXES_COLUMN = "BOXES";

    String ROW_STYLE_COLUMN = "ROW-STYLE";

    String ROW_COLUMN = "ROW";

    String SHORTNAME_COLUMN = "SHORTNAME";

    String FIELD_TYPE_COLUMN = "FIELDTYPE";

    String METADATA_COLUMN = "METADATA";

    String VALUE_COLUMN = "VALUE";

    String BUNDLE_COLUMN = "BUNDLE";


    String METADATA_TYPE = "METADATA";

    String BITSTREAM_TYPE = "BITSTREAM";

    String METADATAGROUP_TYPE = "METADATAGROUP";

    List<String> ALLOWED_FIELD_TYPES = List.of(METADATA_TYPE, BITSTREAM_TYPE, METADATAGROUP_TYPE);

    /**
     * Validate the given workbook.
     *
     * @param  context  the DSpace context
     * @param  workbook the workbook to validate
     * @return          the validation result
     */
    CrisLayoutToolValidationResult validate(Context context, Workbook workbook);

}
