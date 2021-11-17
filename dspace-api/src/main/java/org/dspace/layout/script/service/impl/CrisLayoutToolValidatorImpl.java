/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.layout.script.service.impl;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.dspace.util.WorkbookUtils.getCellIndexFromHeaderName;
import static org.dspace.util.WorkbookUtils.getCellValue;
import static org.dspace.util.WorkbookUtils.getColumnWithoutHeader;
import static org.dspace.util.WorkbookUtils.getNotEmptyRowsSkippingHeader;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.dspace.content.service.EntityTypeService;
import org.dspace.content.service.MetadataFieldService;
import org.dspace.core.Context;
import org.dspace.core.exception.SQLRuntimeException;
import org.dspace.layout.CrisLayoutBoxTypes;
import org.dspace.layout.script.service.CrisLayoutToolValidationResult;
import org.dspace.layout.script.service.CrisLayoutToolValidator;
import org.dspace.util.WorkbookUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implementation of {@link CrisLayoutToolValidator}.
 *
 * @author Luca Giamminonni (luca.giamminonni at 4science.it)
 *
 */
public class CrisLayoutToolValidatorImpl implements CrisLayoutToolValidator {

    @Autowired
    private EntityTypeService entityTypeService;

    @Autowired
    private MetadataFieldService metadataFieldService;

    @Override
    public CrisLayoutToolValidationResult validate(Context context, Workbook workbook) {

        List<String> allEntityTypes = getAllEntityTypes(context);

        CrisLayoutToolValidationResult result = new CrisLayoutToolValidationResult();
        validateTabSheet(context, workbook, result, allEntityTypes);
        validateBoxSheet(context, workbook, result, allEntityTypes);
        validateTab2BoxSheet(context, workbook, result);
        validateBox2MetadataSheet(context, workbook, result);

        return result;
    }

    private void validateTabSheet(Context context, Workbook workbook, CrisLayoutToolValidationResult result,
        List<String> allEntityTypes) {

        Sheet tabSheet = workbook.getSheet(TAB_SHEET);
        if (tabSheet == null) {
            result.addError("The " + TAB_SHEET + " is missing");
            return;
        }

        int entityTypeColumn = getCellIndexFromHeaderName(tabSheet, ENTITY_COLUMN);
        if (entityTypeColumn != -1) {
            validateEntityTypes(result, tabSheet, entityTypeColumn, allEntityTypes);
        } else {
            result.addError("The sheet " + TAB_SHEET + " has no " + ENTITY_COLUMN + " column");
        }

        int shortnameColumn = getCellIndexFromHeaderName(tabSheet, SHORTNAME_COLUMN);
        if (shortnameColumn == -1) {
            result.addError("The sheet " + TAB_SHEET + " has no " + SHORTNAME_COLUMN + " column");
        }

        if (entityTypeColumn != -1 && shortnameColumn != -1) {
            validatePresenceInTab2BoxSheet(result, tabSheet, TAB_COLUMN, entityTypeColumn, shortnameColumn);
        }

    }

    private void validateBoxSheet(Context context, Workbook workbook, CrisLayoutToolValidationResult result,
        List<String> allEntityTypes) {

        Sheet boxSheet = workbook.getSheet(BOX_SHEET);
        if (boxSheet == null) {
            result.addError("The " + BOX_SHEET + " is missing");
            return;
        }

        int typeColumn = getCellIndexFromHeaderName(boxSheet, TYPE_COLUMN);
        if (typeColumn != -1) {
            validateBoxTypes(result, boxSheet, typeColumn);
        } else {
            result.addError("The sheet " + BOX_SHEET + " has no " + TYPE_COLUMN + " column");
        }

        int entityTypeColumn = getCellIndexFromHeaderName(boxSheet, ENTITY_COLUMN);
        if (entityTypeColumn != -1) {
            validateEntityTypes(result, boxSheet, entityTypeColumn, allEntityTypes);
        } else {
            result.addError("The sheet " + BOX_SHEET + " has no " + ENTITY_COLUMN + " column");
        }

        int shortnameColumn = getCellIndexFromHeaderName(boxSheet, SHORTNAME_COLUMN);
        if (shortnameColumn == -1) {
            result.addError("The sheet " + BOX_SHEET + " has no " + SHORTNAME_COLUMN + " column");
        }

        if (entityTypeColumn != -1 && shortnameColumn != -1) {
            validatePresenceInTab2BoxSheet(result, boxSheet, BOXES_COLUMN, entityTypeColumn, shortnameColumn);
        }

    }

    private void validateTab2BoxSheet(Context context, Workbook workbook, CrisLayoutToolValidationResult result) {

        Sheet tab2boxSheet = workbook.getSheet(TAB2BOX_SHEET);
        if (tab2boxSheet == null) {
            result.addError("The " + TAB2BOX_SHEET + " is missing");
            return;
        }

        if (getCellIndexFromHeaderName(tab2boxSheet, ENTITY_COLUMN) == -1) {
            result.addError("The sheet " + tab2boxSheet.getSheetName() + " has no " + ENTITY_COLUMN + " column");
        }

        if (getCellIndexFromHeaderName(tab2boxSheet, TAB_COLUMN) == -1) {
            result.addError("The sheet " + tab2boxSheet.getSheetName() + " has no " + TAB_COLUMN + " column");
        }

        if (getCellIndexFromHeaderName(tab2boxSheet, BOXES_COLUMN) == -1) {
            result.addError("The sheet " + tab2boxSheet.getSheetName() + " has no " + BOXES_COLUMN + " column");
        }

        validateTab2BoxRowsReferences(tab2boxSheet, result);
        validateTab2BoxRowsStyle(tab2boxSheet, result);

    }

    private void validateBox2MetadataSheet(Context context, Workbook workbook, CrisLayoutToolValidationResult result) {

        Sheet box2metadataSheet = workbook.getSheet(BOX2METADATA_SHEET);
        if (box2metadataSheet == null) {
            result.addError("The " + BOX2METADATA_SHEET + " is missing");
            return;
        }

        int fieldTypeColumn = getCellIndexFromHeaderName(box2metadataSheet, FIELD_TYPE_COLUMN);
        if (fieldTypeColumn == -1) {
            result.addError("The sheet " + BOX2METADATA_SHEET + " has no " + FIELD_TYPE_COLUMN + " column");
        } else {
            validateBox2MetadataFieldTypes(box2metadataSheet, result, fieldTypeColumn);
        }

        int metadataColumn = getCellIndexFromHeaderName(box2metadataSheet, METADATA_COLUMN);
        if (metadataColumn == -1) {
            result.addError("The sheet " + BOX2METADATA_SHEET + " has no " + METADATA_COLUMN + " column");
        } else {
            validateBox2MetadataFields(context, box2metadataSheet, result, metadataColumn);
        }

        int entityTypeColumn = getCellIndexFromHeaderName(box2metadataSheet, ENTITY_COLUMN);
        if (entityTypeColumn == -1) {
            result.addError("The sheet " + BOX2METADATA_SHEET + " has no " + ENTITY_COLUMN + " column");
        }

        int boxColumn = getCellIndexFromHeaderName(box2metadataSheet, BOX_COLUMN);
        if (boxColumn == -1) {
            result.addError("The sheet " + BOX2METADATA_SHEET + " has no " + BOX_COLUMN + " column");
        }

        if (entityTypeColumn != -1 && boxColumn != -1) {
            validatePresenceInBoxSheet(result, box2metadataSheet, entityTypeColumn, boxColumn);
        }

    }

    private void validateBox2MetadataFieldTypes(Sheet sheet, CrisLayoutToolValidationResult result, int typeColumn) {

        int bundleColumn = getCellIndexFromHeaderName(sheet, BUNDLE_COLUMN);
        if (bundleColumn == -1) {
            result.addError("The sheet " + BOX2METADATA_SHEET + " has no " + BUNDLE_COLUMN + " column");
        }

        int valueColumn = getCellIndexFromHeaderName(sheet, VALUE_COLUMN);
        if (valueColumn == -1) {
            result.addError("The sheet " + BOX2METADATA_SHEET + " has no " + VALUE_COLUMN + " column");
        }

        for (Row row : getNotEmptyRowsSkippingHeader(sheet)) {

            String fieldType = getCellValue(row, typeColumn);
            if (!ALLOWED_FIELD_TYPES.contains(fieldType)) {
                result.addError("The " + sheet.getSheetName() + " contains an unknown field type" + fieldType
                    + " at row " + row.getRowNum());
            }

            if (METADATA_TYPE.equals(fieldType) && bundleColumn != -1 && valueColumn != -1) {
                String bundle = getCellValue(row, bundleColumn);
                String value = getCellValue(row, valueColumn);
                if (StringUtils.isNotBlank(bundle) || StringUtils.isNotBlank(value)) {
                    result.addError("The " + sheet.getSheetName() + " contains a " + METADATA_TYPE + " field "
                        + fieldType + " with " + BUNDLE_COLUMN + " or " + VALUE_COLUMN + " set at row "
                        + row.getRowNum());
                }
            }

            if (BITSTREAM_TYPE.equals(fieldType) && bundleColumn != -1) {
                String bundle = getCellValue(row, bundleColumn);
                if (StringUtils.isBlank(bundle)) {
                    result.addError("The " + sheet.getSheetName() + " contains a " + BITSTREAM_TYPE + " field "
                        + fieldType + " without " + BUNDLE_COLUMN + " at row " + row.getRowNum());
                }
            }

        }

    }

    private void validateBox2MetadataFields(Context context, Sheet sheet, CrisLayoutToolValidationResult result,
        int metadataColumn) {

        List<String> allMetadataFields = findAllMetadataFields(context);
        for (Cell cell : getColumnWithoutHeader(sheet, metadataColumn)) {
            String metadataField = WorkbookUtils.getCellValue(cell);
            if (!allMetadataFields.contains(metadataField)) {
                result.addError("The " + sheet.getSheetName() + " contains an unknown metadata field " + metadataField
                    + " at row " + cell.getRowIndex());
            }
        }

    }

    private void validatePresenceInBoxSheet(CrisLayoutToolValidationResult result, Sheet sheet,
        int entityTypeColumn, int nameColumn) {

        for (Row row : getNotEmptyRowsSkippingHeader(sheet)) {
            String entityType = getCellValue(row, entityTypeColumn);
            String name = getCellValue(row, nameColumn);
            if (isNotPresentOnSheet(sheet.getWorkbook(), BOX_SHEET, entityType, name)) {
                result.addError("The box with name " + name +
                    " and entity type " + entityType + " in the row "
                    + row.getRowNum() + " of sheet " + sheet.getSheetName()
                    + " is not present in the " + BOX_SHEET + " sheet");
            }
        }

    }

    private void validateTab2BoxRowsReferences(Sheet tab2boxSheet, CrisLayoutToolValidationResult result) {

        int entityTypeColumn = getCellIndexFromHeaderName(tab2boxSheet, ENTITY_COLUMN);
        int tabColumn = getCellIndexFromHeaderName(tab2boxSheet, TAB_COLUMN);
        int boxesColumn = getCellIndexFromHeaderName(tab2boxSheet, BOXES_COLUMN);

        if (entityTypeColumn != -1 && tabColumn != -1 && boxesColumn != -1) {
            getNotEmptyRowsSkippingHeader(tab2boxSheet)
                .forEach(row -> validateTab2BoxRowReferences(row, result, entityTypeColumn, tabColumn, boxesColumn));
        }

    }

    private void validatePresenceInTab2BoxSheet(CrisLayoutToolValidationResult result, Sheet sheet, String columnName,
        int entityTypeColumn, int shortnameColumn) {

        Sheet tab2boxSheet = sheet.getWorkbook().getSheet(TAB2BOX_SHEET);
        if (tab2boxSheet == null) {
            return;
        }

        for (Row row : getNotEmptyRowsSkippingHeader(sheet)) {
            String entityType = getCellValue(row, entityTypeColumn);
            String shortname = getCellValue(row, shortnameColumn);
            if (isNotPresentOnTab2Box(tab2boxSheet, columnName, entityType, shortname)) {
                result.addWarning("The " + sheet.getSheetName() + " with name " + shortname +
                    " and entity type " + entityType + " in the row "
                    + row.getRowNum() + " of sheet " + sheet.getSheetName()
                    + " is not present in the " + TAB2BOX_SHEET + " sheet");
            }
        }

    }

    private void validateTab2BoxRowReferences(Row row, CrisLayoutToolValidationResult result,
        int entityTypeColumn, int tabColumn, int boxesColumn) {

        Sheet tab2boxSheet = row.getSheet();

        String entityType = WorkbookUtils.getCellValue(row, entityTypeColumn);
        String tab = WorkbookUtils.getCellValue(row, tabColumn);
        String[] boxes = WorkbookUtils.getCellValue(row, boxesColumn).split(",");

        if (isNotPresentOnSheet(tab2boxSheet.getWorkbook(), TAB_SHEET, entityType, tab)) {
            result.addError("The Tab with name " + tab + " and entity type " + entityType + " in the row " +
                row.getRowNum() + " of sheet " + tab2boxSheet.getSheetName() + " is not present in the " + TAB_SHEET
                + " sheet");
        }

        for (String box : boxes) {
            if (isNotPresentOnSheet(tab2boxSheet.getWorkbook(), BOX_SHEET, entityType, box)) {
                result.addError("The Box with name " + box + " and entity type " + entityType + " in the row " +
                    row.getRowNum() + " of sheet " + tab2boxSheet.getSheetName() + " is not present in the " + BOX_SHEET
                    + " sheet");
            }
        }

    }

    private void validateTab2BoxRowsStyle(Sheet tab2boxSheet, CrisLayoutToolValidationResult result) {

        int rowStyleColumn = getCellIndexFromHeaderName(tab2boxSheet, ROW_STYLE_COLUMN);
        if (rowStyleColumn == -1) {
            result.addError("The sheet " + tab2boxSheet.getSheetName() + " has no " + ROW_STYLE_COLUMN + " column");
            return;
        }

        int rowColumn = getCellIndexFromHeaderName(tab2boxSheet, ROW_COLUMN);
        if (rowColumn == -1) {
            result.addError("The sheet " + tab2boxSheet.getSheetName() + " has no " + ROW_COLUMN + " column");
            return;
        }

        int entityTypeColumn = getCellIndexFromHeaderName(tab2boxSheet, ENTITY_COLUMN);
        int tabColumn = getCellIndexFromHeaderName(tab2boxSheet, TAB_COLUMN);
        if (entityTypeColumn == -1 || tabColumn == -1) {
            return;
        }

        List<Integer> detectedRowWIthConflicts = new ArrayList<Integer>();

        for (Row row : getNotEmptyRowsSkippingHeader(tab2boxSheet)) {

            if (detectedRowWIthConflicts.contains(row.getRowNum())) {
                continue;
            }

            String style = getCellValue(row, rowStyleColumn);
            if (StringUtils.isBlank(style)) {
                continue;
            }

            String entityType = getCellValue(row, entityTypeColumn);
            String tab = getCellValue(row, tabColumn);
            String rowCount = getCellValue(row, rowColumn);
            if (isNotInteger(rowCount)) {
                result.addError("The " + ROW_COLUMN + " value specified on the row " + row.getRowNum() + " of sheet "
                    + tab2boxSheet.getSheetName() + " is not valid " + rowCount);
                continue;
            }

            List<Integer> sameRowsWithDifferentStyle = findSameRowsWithDifferentStyle(tab2boxSheet,
                entityType, tab, rowCount, style, row.getRowNum());

            if (CollectionUtils.isNotEmpty(sameRowsWithDifferentStyle)) {
                detectedRowWIthConflicts.addAll(sameRowsWithDifferentStyle);
                result.addError("Row style conflict between rows " + row.getRowNum() + " and rows"
                    + sameRowsWithDifferentStyle.toString() + " of sheet " + tab2boxSheet.getSheetName());
            }

        }
    }

    private List<Integer> findSameRowsWithDifferentStyle(Sheet sheet, String entity,
        String tab, String row, String style, int excelRowNum) {
        int rowStyleColumn = getCellIndexFromHeaderName(sheet, ROW_STYLE_COLUMN);
        int entityTypeColumn = getCellIndexFromHeaderName(sheet, ENTITY_COLUMN);
        int tabColumn = getCellIndexFromHeaderName(sheet, TAB_COLUMN);
        int rowColumn = getCellIndexFromHeaderName(sheet, ROW_COLUMN);
        return getNotEmptyRowsSkippingHeader(sheet).stream()
            .filter(sheetRow -> excelRowNum != sheetRow.getRowNum())
            .filter(sheetRow -> isNotBlank(getCellValue(sheetRow, rowStyleColumn)))
            .filter(sheetRow -> row.equals(getCellValue(sheetRow, rowColumn)))
            .filter(sheetRow -> tab.equals(getCellValue(sheetRow, tabColumn)))
            .filter(sheetRow -> entity.equals(getCellValue(sheetRow, entityTypeColumn)))
            .map(Row::getRowNum)
            .collect(Collectors.toList());
    }

    private boolean isNotPresentOnSheet(Workbook workbook, String sheetName, String entityType, String shortname) {
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            // Return false to avoid many validation error if the sheet is missing
            return false;
        }

        int entityTypeColumn = getCellIndexFromHeaderName(sheet, ENTITY_COLUMN);
        int shortnameColumn = getCellIndexFromHeaderName(sheet, SHORTNAME_COLUMN);
        if (entityTypeColumn == -1 || shortnameColumn == -1) {
            // Return false to avoid many validation error if one of the two columns is
            // missing
            return false;
        }

        return getNotEmptyRowsSkippingHeader(sheet).stream()
            .noneMatch(row -> sameEntityTypeAndName(row, entityTypeColumn, entityType, shortnameColumn, shortname));
    }

    private boolean isNotPresentOnTab2Box(Sheet tab2boxSheet, String columnName, String entityType, String shortname) {

        int entityTypeColumn = getCellIndexFromHeaderName(tab2boxSheet, ENTITY_COLUMN);
        int nameColumn = getCellIndexFromHeaderName(tab2boxSheet, columnName);
        if (entityTypeColumn == -1 || nameColumn == -1) {
            // Return false to avoid many validation error if one of the two columns is
            // missing
            return false;
        }

        return getNotEmptyRowsSkippingHeader(tab2boxSheet).stream()
            .noneMatch(row -> sameEntityTypeAndName(row, entityTypeColumn, entityType, nameColumn, shortname));

    }

    private boolean sameEntityTypeAndName(Row row, int entityTypeColumn, String entityType,
        int nameColumn, String name) {
        String[] names = name.split(",");
        return entityType.equals(getCellValue(row, entityTypeColumn))
            && ArrayUtils.contains(names, getCellValue(row, nameColumn));
    }

    private void validateEntityTypes(CrisLayoutToolValidationResult result, Sheet sheet,
        int entityColumn, List<String> allEntityTypes) {

        for (Cell entityTypeCell : getColumnWithoutHeader(sheet, entityColumn)) {
            String entityType = WorkbookUtils.getCellValue(entityTypeCell);
            if (!allEntityTypes.contains(entityType)) {
                result.addError("The " + sheet.getSheetName() + " contains an unknown entity type " + entityType
                    + " at row " + entityTypeCell.getRowIndex());
            }
        }
    }

    private void validateBoxTypes(CrisLayoutToolValidationResult result, Sheet sheet, int typeColumn) {

        for (Cell typeCell : getColumnWithoutHeader(sheet, typeColumn)) {
            String type = WorkbookUtils.getCellValue(typeCell);
            if (type != null && !EnumUtils.isValidEnum(CrisLayoutBoxTypes.class, type)) {
                result.addError("The sheet " + sheet.getSheetName() + " has contains an invalid type " + type
                    + " at row " + typeCell.getRowIndex());
            }
        }

    }

    private List<String> getAllEntityTypes(Context context) {
        try {
            return entityTypeService.findAll(context).stream()
                .map(entityType -> entityType.getLabel())
                .collect(Collectors.toList());
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    private List<String> findAllMetadataFields(Context context) {
        try {
            return metadataFieldService.findAll(context).stream()
                .map(metadataField -> metadataField.toString('.'))
                .collect(Collectors.toList());
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    public boolean isNotInteger(String number) {
        try {
            Integer.parseInt(number);
        } catch (Exception e) {
            return true;
        }
        return false;
    }

}
