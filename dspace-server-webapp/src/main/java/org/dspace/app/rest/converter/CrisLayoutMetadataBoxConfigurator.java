/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.converter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.dspace.app.rest.exception.UnprocessableEntityException;
import org.dspace.app.rest.model.CrisLayoutBoxConfigurationRest;
import org.dspace.app.rest.model.CrisLayoutMetadataConfigurationRest;
import org.dspace.app.rest.model.CrisLayoutMetadataConfigurationRest.Bitstream;
import org.dspace.app.rest.model.CrisLayoutMetadataConfigurationRest.Field;
import org.dspace.app.rest.model.CrisLayoutMetadataConfigurationRest.MetadataGroup;
import org.dspace.app.rest.model.CrisLayoutMetadataConfigurationRest.Row;
import org.dspace.content.CrisLayoutFieldRowPriorityComparator;
import org.dspace.content.MetadataField;
import org.dspace.content.service.MetadataFieldService;
import org.dspace.core.Context;
import org.dspace.core.exception.SQLRuntimeException;
import org.dspace.layout.CrisLayoutBox;
import org.dspace.layout.CrisLayoutBoxTypes;
import org.dspace.layout.CrisLayoutField;
import org.dspace.layout.CrisLayoutFieldBitstream;
import org.dspace.layout.CrisLayoutFieldMetadata;
import org.dspace.layout.CrisMetadataGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This is the configurator for metadata layout box
 * 
 * @author Danilo Di Nuzzo (danilo.dinuzzo at 4science.it)
 *
 */
@Component
public class CrisLayoutMetadataBoxConfigurator implements CrisLayoutBoxConfigurator {

    @Autowired
    private MetadataFieldService metadataFieldService;

    @Override
    public boolean support(CrisLayoutBox box) {
        return StringUtils.equals(box.getType(), CrisLayoutBoxTypes.METADATA.name());
    }

    @Override
    public CrisLayoutBoxConfigurationRest getConfiguration(CrisLayoutBox box) {
        CrisLayoutMetadataConfigurationRest rest = new CrisLayoutMetadataConfigurationRest();
        List<CrisLayoutField> layoutFields = box.getLayoutFields();
        Collections.sort(layoutFields, new CrisLayoutFieldRowPriorityComparator());
        if (layoutFields != null && !layoutFields.isEmpty()) {
            Map<Integer, Row> rows = new HashMap<>();
            for (CrisLayoutField layoutField : layoutFields) {
                Row row = rows.get(layoutField.getRow());
                if (row == null) {
                    row = new Row();
                    rows.put(layoutField.getRow(), row);
                }
                Field field = new Field();
                field.setLabel(layoutField.getLabel());
                field.setRendering(layoutField.getRendering());
                field.setStyle(layoutField.getStyle());
                field.setStyleLabel(layoutField.getStyleLabel());
                field.setStyleValue(layoutField.getStyleValue());
                if (layoutField instanceof CrisLayoutFieldMetadata) {
                    field.setMetadata(composeMetadataFieldIdentifier(layoutField.getMetadataField()));
                    field.setFieldType("METADATA");
                } else if  (layoutField instanceof CrisLayoutFieldBitstream) {
                    CrisLayoutFieldBitstream bitstream = (CrisLayoutFieldBitstream) layoutField;
                    field.setFieldType("BITSTREAM");
                    Bitstream bits = new Bitstream();
                    bits.setBundle(bitstream.getBundle());
                    bits.setMetadataValue(bitstream.getMetadataValue());
                    bits.setMetadataField(composeMetadataFieldIdentifier(bitstream.getMetadataField()));
                    field.setBitstream(bits);
                }
                // if it has metadatagroup
                if (!layoutField.getCrisMetadataGroupList().isEmpty()) {
                    CrisLayoutMetadataConfigurationRest.MetadataGroup metadataGroup =
                            new CrisLayoutMetadataConfigurationRest.MetadataGroup();
                    metadataGroup.setLeading(composeMetadataFieldIdentifier(layoutField.getMetadataField()));
                    List<CrisMetadataGroup> crisMetadataGroupList = layoutField.getCrisMetadataGroupList();
                    List<Field> nestedFieldList = new ArrayList<>();

                    for (CrisMetadataGroup crisMetadataGroup : crisMetadataGroupList) {
                        Field nestedField = new Field();
                        nestedField.setMetadata(composeMetadataFieldIdentifier(crisMetadataGroup.getMetadataField()));
                        nestedField.setLabel(crisMetadataGroup.getLabel());
                        nestedField.setRendering(crisMetadataGroup.getRendering());
                        nestedField.setStyle(crisMetadataGroup.getStyle());
                        nestedField.setStyleLabel(crisMetadataGroup.getStyleLabel());
                        nestedField.setStyleValue(crisMetadataGroup.getStyleValue());
                        nestedField.setFieldType("METADATA");
                        nestedFieldList.add(nestedField);
                    }
                    metadataGroup.setElements(nestedFieldList);
                    field.setMetadataGroup(metadataGroup);
                    field.setFieldType("METADATAGROUP");
                }
                row.addField(field);
            }

            Set<Integer> keySet = rows.keySet();
            for (Integer position : keySet) {
                rest.addRow(rows.get(position));
            }
        }
        return rest;
    }

    @Override
    public void configure(Context context, CrisLayoutBox box, CrisLayoutBoxConfigurationRest rest) {

        if (!(rest instanceof CrisLayoutMetadataConfigurationRest)) {
            throw new IllegalArgumentException("Invalid METADATA configuration provided");
        }

        CrisLayoutMetadataConfigurationRest configuration = ((CrisLayoutMetadataConfigurationRest) rest);
        int rowIndex = 0;
        for (Row row : configuration.getRows()) {
            int priority = 0;
            for (Field field : row.getFields()) {
                CrisLayoutField fieldEntity = new CrisLayoutField();
                fieldEntity.setLabel(field.getLabel());
                fieldEntity.setLabelAsHeading(field.isLabelAsHeading());
                fieldEntity.setMetadataField(getMetadataField(context, field.getMetadata()));
                fieldEntity.setPriority(priority++);
                fieldEntity.setRendering(field.getRendering());
                fieldEntity.setRow(rowIndex);
                fieldEntity.setStyle(field.getStyle());
                fieldEntity.setStyleLabel(field.getStyleLabel());
                fieldEntity.setStyleValue(field.getStyleValue());
                fieldEntity.setValuesInline(field.isValuesInline());
                addMetadataGroup(context, fieldEntity, field.getMetadataGroup());
                box.addLayoutField(fieldEntity);
            }
            rowIndex++;
        }

    }

    private void addMetadataGroup(Context context, CrisLayoutField fieldEntity, MetadataGroup metadataGroup) {

        if (metadataGroup == null) {
            return;
        }

        int priority = 0;
        for (Field element : metadataGroup.getElements()) {
            CrisMetadataGroup nestedField = new CrisMetadataGroup();
            nestedField.setLabel(element.getLabel());
            nestedField.setMetadataField(getMetadataField(context, element.getMetadata()));
            nestedField.setPriority(priority++);
            nestedField.setRendering(element.getRendering());
            nestedField.setStyle(element.getStyle());
            nestedField.setStyleLabel(element.getStyleLabel());
            nestedField.setStyleValue(element.getStyleValue());
            fieldEntity.addCrisMetadataGroupList(nestedField);
        }

    }

    private String composeMetadataFieldIdentifier(MetadataField mf) {
        StringBuffer sb = null;
        if (mf != null) {
            sb = new StringBuffer(mf.getMetadataSchema().getName()).append(".").append(mf.getElement());
            if (mf.getQualifier() != null) {
                sb.append(".").append(mf.getQualifier());
            }
        }
        return sb != null ? sb.toString() : null;
    }

    private MetadataField getMetadataField(Context context, String metadataField) {
        if (metadataField == null) {
            return null;
        }

        try {
            MetadataField entity = metadataFieldService.findByString(context, metadataField, '.');
            if (entity == null) {
                throw new UnprocessableEntityException("MetadataField <" + metadataField + "> not exists!");
            }
            return entity;
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }
}
