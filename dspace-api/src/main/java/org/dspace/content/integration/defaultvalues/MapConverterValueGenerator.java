/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.integration.defaultvalues;

import java.util.ArrayList;
import java.util.List;

import org.dspace.content.Item;
import org.dspace.util.SimpleMapConverter;

public class MapConverterValueGenerator  implements EnhancedValuesGenerator {

	private SimpleMapConverter converter;
	@Override
	public DefaultValuesBean generateValues(Item item, String schema, String element, String qualifier, String value) {
	

		DefaultValuesBean result = new DefaultValuesBean();
		result.setLanguage("en");
		result.setMetadataSchema(schema);
		result.setMetadataQualifier(qualifier);
		result.setMetadataElement(element);
		
		List<String> newValues = new ArrayList<String>();
		String newValue = converter.getValue(value);
    	newValues.add(newValue);
    
    	String[] values = new String[newValues.size()];
    	result.setValues(newValues.toArray(values));

		return result;
	}

	public SimpleMapConverter getConverter() {
		return converter;
	}

	public void setConverter(SimpleMapConverter converter) {
		this.converter = converter;
	}

}
