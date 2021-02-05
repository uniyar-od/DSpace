package org.dspace.submit.lookup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.app.cris.model.ACrisObject;
import org.dspace.content.authority.Choice;
import org.dspace.content.authority.Choices;
import org.dspace.content.authority.factory.ContentAuthorityServiceFactory;
import org.dspace.content.authority.service.ChoiceAuthorityService;
import org.dspace.core.Context;
import org.dspace.discovery.SearchServiceException;

import gr.ekt.bte.core.MutableRecord;
import gr.ekt.bte.core.StringValue;
import gr.ekt.bte.core.Value;

/**
 * This class is an advanced version of {@link AuthorityLookupModifier}
 * that is able to enrich a BTE record with information extracted from a CRIS
 * authority.
 * If the field is not behind authority the default implementation of {@link AuthorityLookupModifier}
 * is used.
 * <p>The metadataInputAdvancedConfiguration contains the mapping between BTE fields
 * and metadata behind authority used for authority lookup.
 * <p>The mappingOutputAdvancedConfiguration contains the mapping between choice properties
 * and BTE fields.
 *
 */
public class AuthorityLookupModifierAdvanced<T extends ACrisObject>
        extends AuthorityLookupModifier<T> {

    private static Logger log = Logger.getLogger(AuthorityLookupModifierAdvanced.class);

    public static final String NAME = "name";

    /** the key is the BTE field, the value the metadata to use for CRIS lookup */
    protected Map<String, String> metadataInputAdvancedConfiguration;
    /** the key is the Choice prop, the value the BTE field */
    protected Map<String, String> mappingOutputAdvancedConfiguration;

    public AuthorityLookupModifierAdvanced() {
        super("AuthorityLookupModifierAdvanced");
    }

    /**
     * Retrieve the object by authority lookup on the authority field retrieved from the BTE field.
     * If the metadata is not behind authority use the default implementation
     * of {@link AuthorityLookupModifier}.
     * @param rec the MutableRecord
     * @param mm the BTE field
     * @param results the list of results
     * @param confidences the list of confidences
     * @throws SearchServiceException
     */
    protected void lookup(MutableRecord rec, String mm, List results, List<Integer> confidences)
            throws SearchServiceException {
        // check implementation to use
        if (!mappingAuthorityConfiguration.contains(mm)) {
            // if metadata is NOT behind authority use solr lookup
            super.lookup(rec, mm, results, confidences);
            return;
        }

        // if metadata is behind authority use authority lookup
        ChoiceAuthorityService cam = ContentAuthorityServiceFactory.getInstance().getChoiceAuthorityService();
        List<String> values = new ArrayList<String>(normalize(getValue(rec, mm)));
        int pos = 0;
        for (String value : values) {
            // if we have already identified the choice object using another field is not
            // necessary to make a second lookup
            if (results.size() > pos && results.get(pos) != null) {
                continue;
            }

            if (StringUtils.isNotBlank(value)) {
                // use the dedicated authority implementation
                Choices choices = cam.getBestMatch(metadataInputAdvancedConfiguration.get(mm),
                        value, null, null);
                Choice[] choiceValues = choices.values;

                Choice choiceValue = null;
                if (choiceValues == null || choiceValues.length == 0) {
                    confidences.add(Choices.CF_UNSET);
                } else {
                    confidences.add(choices.confidence);
                    if (choiceValues.length == 1) {
                        choiceValue = choiceValues[0];
                    }
                }
                if (results.size() > pos) {
                    results.set(pos, choiceValue);
                }
                else {
                    results.add(choiceValue);
                }
            }
            pos++;
        }
    }

    /**
     * Process the retrieved object to populate the MutableRecord.
     * If the retrieved object is not retrieved by authority lookup
     * use the default implementation of {@link AuthorityLookupModifier}.
     * @param rec the MutableRecord
     * @param result the retrieved object
     * @param pos the position of the retrieved object
     * @param confidence the confidence of the retrieved object
     * @throws SearchServiceException
     */
    protected void process(MutableRecord rec, Object result, int pos, int confidence) {
        // check implementation to use
        if (result != null && !(result instanceof Choice)) {
            // if object is not retrieved by authority lookup use the default implementation
            super.process(rec, result, pos, confidence);
            return;
        }

        // otherwise retrieve metadata on choice
        if (result != null) {
            Choice choice = (Choice) result;
            for (String propShortname : mappingOutputAdvancedConfiguration.keySet()) {
                String value = null;
                if (StringUtils.equals(propShortname, NAME)) {
                    // retrieve name
                    value = choice.value;
                } else {
                    // retrieve on extras
                    value = choice.extras.get("data-" + propShortname);
                }

                String bteField = mappingOutputAdvancedConfiguration.get(propShortname);
                List<Value> exValues = rec.getValues(bteField);
                List<Value> newValues = new ArrayList<Value>();
                rec.removeField(bteField);

                // add back all the existing values as is
                for (int iPos = 0; iPos < pos; iPos++) {
                    newValues.add(exValues.size() > iPos ? exValues.get(iPos) : new StringValue(""));
                }

                if (value != null) {
                    if (mappingAuthorityConfiguration.contains(bteField)) {
                        newValues.add(new StringValue(value
                                + SubmissionLookupService.SEPARATOR_VALUE_REGEX + choice.authority
                                + SubmissionLookupService.SEPARATOR_VALUE_REGEX + Choices.CF_ACCEPTED));
                    } else {
                        newValues.add(new StringValue(value));
                    }
                }
                else {
                    newValues.add(new StringValue(""));
                }

                // add back all the existing values not yet processed
                for (int iPos = pos + 1; iPos < exValues.size(); iPos++) {
                    newValues.add(exValues.get(iPos));
                }

                rec.addField(bteField, newValues);
            }
        } else {
            for (String propShortname : mappingOutputAdvancedConfiguration.keySet()) {
                String bteField = mappingOutputAdvancedConfiguration.get(propShortname);
                if (mappingAuthorityRequiredConfiguration.contains(bteField)) {
                    // in case of authority required keep only values with authority
                    List<Value> exValues = rec.getValues(bteField);
                    rec.removeField(bteField);
                    if (exValues != null && !exValues.isEmpty()) {
                        List<Value> newValues = new ArrayList<Value>();
                        for (Value value : exValues) {
                            if (StringUtils.contains(value.getAsString(), SubmissionLookupService.SEPARATOR_VALUE_REGEX)) {
                                newValues.add(value);
                            }
                        }
                        if (newValues != null && !newValues.isEmpty()) {
                            rec.addField(bteField, newValues);
                        }
                    }
                }
            }
        }
    }

    public void setMetadataInputAdvancedConfiguration(
            Map<String, String> metadataInputAdvancedConfiguration) {
        this.metadataInputAdvancedConfiguration = metadataInputAdvancedConfiguration;
    }

    public void setMappingOutputAdvancedConfiguration(
            Map<String, String> mappingOutputAdvancedConfiguration) {
        this.mappingOutputAdvancedConfiguration = mappingOutputAdvancedConfiguration;
    }

}
