/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.layout.script.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.dspace.layout.InvalidRenderingException;
import org.dspace.layout.enumeration.Type;
import org.dspace.layout.script.service.CrisLayoutRenderValidator;

/**
 * @author Mohamed Eskander (mohamed.eskander at 4science.it)
 */
public class CrisLayoutRenderValidatorImpl implements CrisLayoutRenderValidator {

    private String name;
    private String fieldType;
    private Type type;
    private List<String> subTypes = new ArrayList<>();

    @Override
    public void validate(String renderType, String fieldType) throws InvalidRenderingException {
        if (!isMatchedFieldType(fieldType)) {
            throw new IllegalArgumentException("contains wrong field type of RENDERING " + getName());
        }
        validateSubTypes(renderType);
    }

    private boolean isMatchedFieldType(String fieldType) {
        String [] fieldTypes = this.fieldType.split(METADATA_SEPARATOR);
        for (String field : fieldTypes) {
            if (field.equals(fieldType)) {
                return true;
            }
        }
        return false;
    }

    private void validateSubTypes(String renderType) {
        if (getType().equals(Type.NOT_ALLOWED) && renderType.contains(".")) {
            throw new IllegalArgumentException("contains not allowed subtype of RENDERING " + getName());
        } else if (getType().equals(Type.MANDATORY) && !renderType.contains(".")) {
            throw new IllegalArgumentException("must contain subType for RENDERING " + getName());
        }

        if (renderType.contains(".")) {
            String subType = renderType.split("\\.")[1];
            if (!getSubTypes().isEmpty() && !getSubTypes().contains(subType)) {
                throw new IllegalArgumentException("contains wrong subtype for RENDERING " + getName());
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public List<String> getSubTypes() {
        return subTypes;
    }

    public void setSubTypes(Supplier<List<String>> supplier) {
        if (getType().equals(Type.NOT_ALLOWED)) {
            throw new IllegalArgumentException("subtypes is not allowed for RENDERING " + getName());
        }
        this.subTypes = supplier.get();
    }
}
