/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.submit.lookup;

import java.util.ArrayList;
import java.util.List;

import gr.ekt.bte.core.AbstractModifier;
import gr.ekt.bte.core.MutableRecord;
import gr.ekt.bte.core.Record;
import gr.ekt.bte.core.StringValue;
import gr.ekt.bte.core.Value;
import org.apache.commons.lang.StringUtils;

/**
 * @author Luigi Andrea Pascarelli
 */
public class SquashLinesModifier extends AbstractModifier {

    private List<String> fieldKeys;

    public SquashLinesModifier(String name) {
        super(name);
    }

    @Override
    public Record modify(MutableRecord rec) {
        for (String fieldKey : fieldKeys) {
            List<Value> values = rec.getValues(fieldKey);
            if (values != null) {
                List<String> converted_values = new ArrayList<String>();
                for (Value val : values) {
                    converted_values.add(val.getAsString());
                }
                List<Value> final_value = new ArrayList<Value>();
                String v = StringUtils.join(converted_values.iterator()," ");
                final_value.add(new StringValue(v));
                rec.updateField(fieldKey, final_value);
            }
        }
        return rec;
    }

    public List<String> getFieldKeys() {
        return fieldKeys;
    }

    public void setFieldKeys(List<String> fieldKeys) {
        this.fieldKeys = fieldKeys;
    }

}
