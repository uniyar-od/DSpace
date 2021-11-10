/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.converter;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import org.dspace.app.rest.model.CrisLayoutBoxRest;
import org.dspace.app.rest.model.CrisLayoutCellRest;
import org.dspace.app.rest.model.CrisLayoutRowRest;
import org.dspace.app.rest.model.CrisLayoutTabRest;
import org.dspace.app.rest.projection.Projection;
import org.dspace.content.EntityType;
import org.dspace.content.service.EntityTypeService;
import org.dspace.core.Context;
import org.dspace.core.exception.SQLRuntimeException;
import org.dspace.layout.CrisLayoutBox;
import org.dspace.layout.CrisLayoutCell;
import org.dspace.layout.CrisLayoutRow;
import org.dspace.layout.CrisLayoutTab;
import org.dspace.layout.LayoutSecurity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This is the converter from Entity CrisLayoutTab to the REST data model
 *
 * @author Danilo Di Nuzzo (danilo.dinuzzo at 4science.it)
 *
 */
@Component
public class CrisLayoutTabConverter implements DSpaceConverter<CrisLayoutTab, CrisLayoutTabRest> {

    @Autowired
    private EntityTypeService eService;

    @Autowired
    private CrisLayoutBoxConverter boxConverter;

    @Override
    public CrisLayoutTabRest convert(CrisLayoutTab model, Projection projection) {
        CrisLayoutTabRest rest = new CrisLayoutTabRest();
        rest.setId(model.getID());
        rest.setEntityType(model.getEntity().getLabel());
        rest.setShortname(model.getShortName());
        rest.setHeader(model.getHeader());
        rest.setPriority(model.getPriority());
        rest.setSecurity(model.getSecurity());
        rest.setRows(convertRows(model.getRows(), projection));
        rest.setLeading(model.isLeading());
        return rest;
    }

    @Override
    public Class<CrisLayoutTab> getModelClass() {
        return CrisLayoutTab.class;
    }

    public CrisLayoutTab toModel(Context context, CrisLayoutTabRest rest) {
        CrisLayoutTab tab = new CrisLayoutTab();
        tab.setHeader(rest.getHeader());
        tab.setPriority(rest.getPriority());
        tab.setSecurity(LayoutSecurity.valueOf(rest.getSecurity()));
        tab.setShortName(rest.getShortname());
        tab.setEntity(findEntityType(context, rest));
        tab.setLeading(rest.isLeading());
        rest.getRows().forEach(row -> tab.addRow(toRowModel(context, row)));
        return tab;
    }

    private List<CrisLayoutRowRest> convertRows(List<CrisLayoutRow> rows, Projection projection) {
        return rows.stream().map(row -> convertRow(row, projection)).collect(Collectors.toList());
    }

    private CrisLayoutRowRest convertRow(CrisLayoutRow row, Projection projection) {
        CrisLayoutRowRest rest = new CrisLayoutRowRest();
        rest.setStyle(row.getStyle());
        rest.setCells(convertCells(row.getCells(), projection));
        return rest;
    }

    private List<CrisLayoutCellRest> convertCells(List<CrisLayoutCell> cells, Projection projection) {
        return cells.stream().map(cell -> convertCell(cell, projection)).collect(Collectors.toList());
    }

    private CrisLayoutCellRest convertCell(CrisLayoutCell cell, Projection projection) {
        CrisLayoutCellRest rest = new CrisLayoutCellRest();
        rest.setStyle(cell.getStyle());
        rest.setBoxes(convertBoxes(cell.getBoxes(), projection));
        return rest;
    }

    private List<CrisLayoutBoxRest> convertBoxes(List<CrisLayoutBox> boxes, Projection projection) {
        return boxes.stream()
            .map(box -> boxConverter.convert(box, projection))
            .collect(Collectors.toList());
    }

    private CrisLayoutRow toRowModel(Context context, CrisLayoutRowRest rowRest) {
        CrisLayoutRow row = new CrisLayoutRow();
        row.setStyle(rowRest.getStyle());
        rowRest.getCells().forEach(cell -> row.addCell(toCellModel(context, cell)));
        return row;
    }

    private CrisLayoutCell toCellModel(Context context, CrisLayoutCellRest cellRest) {
        CrisLayoutCell cell = new CrisLayoutCell();
        cell.setStyle(cellRest.getStyle());
        cellRest.getBoxes().forEach(box -> boxConverter.toModel(context, box));
        return null;
    }

    private EntityType findEntityType(Context context, CrisLayoutTabRest rest) {
        try {
            return eService.findByEntityType(context, rest.getEntityType());
        } catch (SQLException e) {
            throw new SQLRuntimeException(e.getMessage(), e);
        }
    }
}
