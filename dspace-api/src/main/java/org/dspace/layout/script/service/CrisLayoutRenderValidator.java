/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.layout.script.service;

import org.dspace.layout.InvalidRenderingException;

/**
 * validate the layout configuration rendering before upload
 * to avoid unexpected behavior when item details pages are loaded
 *
 * @author Mohamed Eskander (mohamed.eskander at 4science.it)
 */
public interface CrisLayoutRenderValidator {
    public static final String METADATA_SEPARATOR = "\\|\\|";

    /**
     * @param renderType value of rendering type
     * @param fieldType  value of field type
     * @throws InvalidRenderingException if invalid rendering type or field type
     */
    public void validate(String renderType, String fieldType) throws InvalidRenderingException;

    /**
     * @return rendering type value
     */
    public String getName();

}
