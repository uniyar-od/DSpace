/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.layout.script.service.impl;

import static org.dspace.layout.script.service.CrisLayoutToolValidator.BOX2METRICS_SHEET;
import static org.dspace.layout.script.service.CrisLayoutToolValidator.BOXES_COLUMN;
import static org.dspace.layout.script.service.CrisLayoutToolValidator.BOX_COLUMN;
import static org.dspace.layout.script.service.CrisLayoutToolValidator.BOX_POLICY_SHEET;
import static org.dspace.layout.script.service.CrisLayoutToolValidator.BOX_SHEET;
import static org.dspace.layout.script.service.CrisLayoutToolValidator.CELL_STYLE_COLUMN;
import static org.dspace.layout.script.service.CrisLayoutToolValidator.COLLAPSED_COLUMN;
import static org.dspace.layout.script.service.CrisLayoutToolValidator.CONTAINER_COLUMN;
import static org.dspace.layout.script.service.CrisLayoutToolValidator.ENTITY_COLUMN;
import static org.dspace.layout.script.service.CrisLayoutToolValidator.HEADER_COLUMN;
import static org.dspace.layout.script.service.CrisLayoutToolValidator.LEADING_COLUMN;
import static org.dspace.layout.script.service.CrisLayoutToolValidator.METADATA_COLUMN;
import static org.dspace.layout.script.service.CrisLayoutToolValidator.METRIC_TYPE_COLUMN;
import static org.dspace.layout.script.service.CrisLayoutToolValidator.MINOR_COLUMN;
import static org.dspace.layout.script.service.CrisLayoutToolValidator.PRIORITY_COLUMN;
import static org.dspace.layout.script.service.CrisLayoutToolValidator.ROW_COLUMN;
import static org.dspace.layout.script.service.CrisLayoutToolValidator.ROW_STYLE_COLUMN;
import static org.dspace.layout.script.service.CrisLayoutToolValidator.SECURITY_COLUMN;
import static org.dspace.layout.script.service.CrisLayoutToolValidator.SHORTNAME_COLUMN;
import static org.dspace.layout.script.service.CrisLayoutToolValidator.STYLE_COLUMN;
import static org.dspace.layout.script.service.CrisLayoutToolValidator.TAB2BOX_SHEET;
import static org.dspace.layout.script.service.CrisLayoutToolValidator.TAB_COLUMN;
import static org.dspace.layout.script.service.CrisLayoutToolValidator.TAB_POLICY_SHEET;
import static org.dspace.layout.script.service.CrisLayoutToolValidator.TAB_SHEET;
import static org.dspace.layout.script.service.CrisLayoutToolValidator.TYPE_COLUMN;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.dspace.content.EntityType;
import org.dspace.content.MetadataField;
import org.dspace.content.service.EntityTypeService;
import org.dspace.content.service.MetadataFieldService;
import org.dspace.core.Context;
import org.dspace.core.exception.SQLRuntimeException;
import org.dspace.layout.CrisLayoutBox;
import org.dspace.layout.CrisLayoutBoxTypes;
import org.dspace.layout.CrisLayoutCell;
import org.dspace.layout.CrisLayoutField;
import org.dspace.layout.CrisLayoutMetric2Box;
import org.dspace.layout.CrisLayoutRow;
import org.dspace.layout.CrisLayoutTab;
import org.dspace.layout.LayoutSecurity;
import org.dspace.layout.script.service.CrisLayoutToolParser;
import org.dspace.util.WorkbookUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implementation of {@link CrisLayoutToolParser}.
 *
 * @author Luca Giamminonni (luca.giamminonni at 4science.it)
 *
 */
public class CrisLayoutToolParserImpl implements CrisLayoutToolParser {

    @Autowired
    private EntityTypeService entityTypeService;

    @Autowired
    private MetadataFieldService metadataFieldService;

    @Override
    public List<CrisLayoutTab> parse(Context context, Workbook workbook) {
        Sheet tabSheet = workbook.getSheet(TAB_SHEET);
        if (tabSheet == null) {
            throw new IllegalArgumentException("The given workbook has not the " + TAB_SHEET + " sheet");
        }

        return WorkbookUtils.getNotEmptyRowsSkippingHeader(tabSheet).stream()
            .map(row -> buildTab(context, row))
            .collect(Collectors.toList());
    }

    private CrisLayoutTab buildTab(Context context, Row tabRow) {
        CrisLayoutTab tab = new CrisLayoutTab();

        Workbook workbook = tabRow.getSheet().getWorkbook();
        String name = getCellValue(tabRow, SHORTNAME_COLUMN);
        String entityType = getCellValue(tabRow, ENTITY_COLUMN);

        tab.setEntity(getEntityType(context, entityType));
        tab.setShortName(name);
        tab.setHeader(getCellValue(tabRow, HEADER_COLUMN));
        tab.setLeading(toBoolean(getCellValue(tabRow, LEADING_COLUMN)));
        tab.setPriority(toInteger(getCellValue(tabRow, PRIORITY_COLUMN)));
        tab.setSecurity(toSecurity(getCellValue(tabRow, SECURITY_COLUMN)));
        buildTabRows(context, workbook, entityType, name).forEach(tab::addRow);
        tab.setMetadataSecurityFields(buildMetadataSecurityField(context, workbook,
            TAB_POLICY_SHEET, entityType, name));

        return tab;
    }

    private List<CrisLayoutRow> buildTabRows(Context context, Workbook workbook, String entityType, String shortname) {
        Sheet tab2boxSheet = workbook.getSheet(TAB2BOX_SHEET);
        if (tab2boxSheet == null) {
            throw new IllegalArgumentException("The given workbook has not the " + TAB2BOX_SHEET + " sheet");
        }

        Map<Integer, List<Row>> tabExcelRows = WorkbookUtils.getNotEmptyRowsSkippingHeader(tab2boxSheet).stream()
            .filter(row -> shortname.equals(getCellValue(row, TAB_COLUMN)))
            .filter(row -> entityType.equals(getCellValue(row, ENTITY_COLUMN)))
            .collect(Collectors.groupingBy(row -> toInteger(getCellValue(row, ROW_COLUMN))));

        return tabExcelRows.keySet().stream().sorted()
            .map(rowIndex -> buildTabRow(context, tabExcelRows.get(rowIndex)))
            .collect(Collectors.toList());

    }

    private CrisLayoutRow buildTabRow(Context context, List<Row> tab2boxRows) {

        CrisLayoutRow row = new CrisLayoutRow();
        getFirstNotEmptyRowStyle(tab2boxRows).ifPresent(row::setStyle);

        tab2boxRows.stream()
            .sorted(Comparator.comparing(Row::getRowNum))
            .forEach(tab2boxRow -> row.addCell(buildCell(context, tab2boxRow)));

        return row;

    }

    private CrisLayoutCell buildCell(Context context, Row tab2boxRow) {
        CrisLayoutCell cell = new CrisLayoutCell();
        cell.setStyle(getCellValue(tab2boxRow, CELL_STYLE_COLUMN));
        buildBoxes(context, tab2boxRow).forEach(cell::addBox);
        return cell;
    }

    private List<CrisLayoutBox> buildBoxes(Context context, Row tab2boxRow) {

        String entityType = getCellValue(tab2boxRow, ENTITY_COLUMN);

        String boxes = getCellValue(tab2boxRow, BOXES_COLUMN);
        if (StringUtils.isBlank(boxes)) {
            throw new IllegalArgumentException("The row " + tab2boxRow.getRowNum() + " of sheet "
                + tab2boxRow.getSheet().getSheetName() + " has no " + BOXES_COLUMN);
        }

        Workbook workbook = tab2boxRow.getSheet().getWorkbook();
        Sheet boxSheet = workbook.getSheet(BOX_SHEET);
        if (boxSheet == null) {
            throw new IllegalArgumentException("The given workbook has not the " + BOX_SHEET + " sheet");
        }

        return Arrays.stream(boxes.split(","))
            .map(String::trim)
            .map(box -> buildBox(context, boxSheet, entityType, box))
            .collect(Collectors.toList());
    }

    private CrisLayoutBox buildBox(Context context, Sheet boxSheet, String entityType, String boxName) {

        Workbook workbook = boxSheet.getWorkbook();
        Row boxRow = getBowRowFromBoxSheet(boxSheet, entityType, boxName);

        CrisLayoutBox box = new CrisLayoutBox();

        String boxType = getCellValue(boxRow, TYPE_COLUMN);
        if (StringUtils.isBlank(boxType)) {
            boxType = CrisLayoutBoxTypes.METADATA.name();
        } else {
            boxType = boxType.toUpperCase();
        }

        box.setType(boxType);
        box.setCollapsed(toBoolean(getCellValue(boxRow, COLLAPSED_COLUMN)));
        box.setContainer(toBoolean(getCellValue(boxRow, CONTAINER_COLUMN)));
        box.setEntitytype(getEntityType(context, entityType));
        box.setHeader(getCellValue(boxRow, HEADER_COLUMN));
        box.setMinor(toBoolean(getCellValue(boxRow, MINOR_COLUMN)));
        box.setSecurity(toSecurity(getCellValue(boxRow, SECURITY_COLUMN)));
        box.setShortname(boxName);
        box.setStyle(getCellValue(boxRow, STYLE_COLUMN));
        box.setMetadataSecurityFields(buildMetadataSecurityField(context, workbook,
            BOX_POLICY_SHEET, entityType, boxName));

        if (boxType.equals(CrisLayoutBoxTypes.METADATA.name())) {
            buildCrisLayoutFields().forEach(box::addLayoutField);
        } else if (boxType.equals(CrisLayoutBoxTypes.METRICS.name())) {
            buildBoxMetrics(workbook, entityType, boxName).forEach(box::addMetric2box);
        }

        return box;
    }

    private List<CrisLayoutField> buildCrisLayoutFields() {
        // TODO Auto-generated method stub
        return List.of();
    }

    private List<CrisLayoutMetric2Box> buildBoxMetrics(Workbook workbook, String entityType, String boxName) {
        Sheet metricsSheet = workbook.getSheet(BOX2METRICS_SHEET);
        if (metricsSheet == null) {
            throw new IllegalArgumentException("The given workbook has not the " + BOX2METRICS_SHEET + " sheet");
        }

        return WorkbookUtils.getNotEmptyRowsSkippingHeader(metricsSheet).stream()
            .filter(row -> boxName.equals(getCellValue(row, BOX_COLUMN)))
            .filter(row -> entityType.equals(getCellValue(row, ENTITY_COLUMN)))
            .map(row -> getCellValue(row, METRIC_TYPE_COLUMN))
            .filter(metrics -> StringUtils.isNotBlank(metrics))
            .flatMap(metrics -> buildBoxMetrics(metrics.split(",")))
            .collect(Collectors.toList());
    }

    private Stream<CrisLayoutMetric2Box> buildBoxMetrics(String[] metrics) {
        int position = 0;
        List<CrisLayoutMetric2Box> metricEntities = new ArrayList<>();
        for (String metric : metrics) {
            CrisLayoutMetric2Box metricEntity = new CrisLayoutMetric2Box();
            metricEntity.setPosition(position++);
            metricEntity.setType(metric.trim());
            metricEntities.add(metricEntity);
        }
        return metricEntities.stream();
    }

    private Row getBowRowFromBoxSheet(Sheet boxSheet, String entity, String boxName) {
        return WorkbookUtils.getNotEmptyRowsSkippingHeader(boxSheet).stream()
            .filter(row -> boxName.equals(getCellValue(row, SHORTNAME_COLUMN)))
            .filter(row -> entity.equals(getCellValue(row, ENTITY_COLUMN)))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No box found with entity "
                + entity + " and name " + boxName));
    }

    private Optional<String> getFirstNotEmptyRowStyle(List<Row> tab2boxRows) {
        return tab2boxRows.stream()
            .map(tab2boxRow -> getCellValue(tab2boxRow, ROW_STYLE_COLUMN))
            .filter(style -> StringUtils.isNotBlank(style))
            .findFirst();
    }

    private Set<MetadataField> buildMetadataSecurityField(Context context, Workbook workbook,
        String sheetName, String entity, String name) {

        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            throw new IllegalArgumentException("The given workbook has not the " + sheetName + " sheet");
        }

        return WorkbookUtils.getNotEmptyRowsSkippingHeader(sheet).stream()
            .filter(row -> name.equals(getCellValue(row, SHORTNAME_COLUMN)))
            .filter(row -> entity.equals(getCellValue(row, ENTITY_COLUMN)))
            .map(row -> getCellValue(row, METADATA_COLUMN))
            .map(metadataField -> getMetadataField(context, metadataField))
            .collect(Collectors.toSet());
    }

    private boolean toBoolean(String value) {
        return BooleanUtils.toBoolean(value);
    }

    private Integer toInteger(String value) {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid integer value: " + value);
        }
    }

    private String getCellValue(Row row, String header) {
        String cellValue = WorkbookUtils.getCellValue(row, header);
        return StringUtils.isNotBlank(cellValue) ? cellValue : null;
    }

    private Integer toSecurity(String cellValue) {
        String securityValue = cellValue.trim().toUpperCase().replaceAll(" ", "_").replaceAll("&", "AND");
        if (!EnumUtils.isValidEnum(LayoutSecurity.class, securityValue)) {
            throw new IllegalArgumentException("Invalid security value: " + securityValue);
        }
        return LayoutSecurity.valueOf(securityValue).getValue();
    }

    private MetadataField getMetadataField(Context context, String metadataSecurityField) {
        try {
            return metadataFieldService.findByString(context, metadataSecurityField, '.');
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    private EntityType getEntityType(Context context, String entityType) {
        try {
            return entityTypeService.findByEntityType(context, entityType);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

}
