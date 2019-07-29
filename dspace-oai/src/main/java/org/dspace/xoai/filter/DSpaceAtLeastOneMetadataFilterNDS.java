/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.xoai.filter;


/**
 * Extend DSpaceAtLeastOneMetadataFilter setting the default schema to an empty string.
 */
public class DSpaceAtLeastOneMetadataFilterNDS extends DSpaceAtLeastOneMetadataFilter {

    @Override
    public String getDefaultSchema() {
    	return "";
    }
}
