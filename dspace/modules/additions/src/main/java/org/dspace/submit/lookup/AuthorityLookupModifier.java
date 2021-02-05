package org.dspace.submit.lookup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.app.cris.model.ACrisObject;
import org.dspace.app.cris.util.ResearcherPageUtils;
import org.dspace.content.authority.Choices;
import org.dspace.discovery.DiscoverQuery;
import org.dspace.discovery.DiscoverResult;
import org.dspace.discovery.SearchService;
import org.dspace.discovery.SearchServiceException;

import gr.ekt.bte.core.AbstractModifier;
import gr.ekt.bte.core.MutableRecord;
import gr.ekt.bte.core.Record;
import gr.ekt.bte.core.StringValue;
import gr.ekt.bte.core.Value;

/**
 * This class enrich a BTE record with information extracted from a CRIS
 * authority. The metadataInput map contains the mapping between the information
 * potentially available in the incoming BTE record and the SOLR field to use to
 * lookup for the CRIS entity (i.e using the jissn field of the BTE make a solr
 * search on the crisjournal.journalissn field). When multiple inputs are
 * provided they will be used in the exact order and only the first match will
 * be returned
 *
 * The mappingOutput map contains the information to add/override to/in the BTE
 * record if a CRIS entity is found. The mapptingAuthorityConfiguration contains
 * the list of BTE field to enrich with the CRIS ID as authority if the match is
 * found
 *
 */
public class AuthorityLookupModifier<T extends ACrisObject>
        extends AbstractModifier
{
    private static Logger log = Logger.getLogger(AuthorityLookupModifier.class);

    /**
     * the key is the BTE field, the value the SOLR field
     */
    protected Map<String, String> metadataInputConfiguration;

    /**
     * the key is the CRIS object prop name, the value the BTE field
     */
    protected Map<String, String> mappingOutputConfiguration;

	// the list of BTE field that should be linked to the CRIS object if found via
	// the authority framework. They need to appear in the enhanced fields map as
	// well
    protected List<String> mappingAuthorityConfiguration;

    protected List<String> mappingAuthorityRequiredConfiguration;

    protected Integer resourceTypeID;

    protected SearchService searchService;

    private Class<T> clazzCrisObject;

    public AuthorityLookupModifier()
    {
        this("AuthorityLookupModifier");
    }

    public AuthorityLookupModifier(String name)
    {
        super(name);
    }

    @Override
    public Record modify(MutableRecord rec)
    {
        try
        {
            List results = new ArrayList();
        	List<Integer> confidences = new ArrayList<Integer>();
        	
            for (String mm : metadataInputConfiguration.keySet())
            {
                lookup(rec, mm, results, confidences);
            }

            int pos = 0;
            for (Object object : results) {
                process(rec, object, pos, confidences.get(pos));
            	pos++;
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            return null;
        }
        return rec;

    }

    /**
     * Retrieve the object doing a solr search on the CRIS field retrieved from the BTE field.
     * @param rec the MutableRecord
     * @param mm the BTE field
     * @param results the list of results
     * @param confidences the list of confidences
     * @throws SearchServiceException
     */
    protected void lookup(MutableRecord rec, String mm, List results, List<Integer> confidences)
            throws SearchServiceException {
        // lookup for the cris object using the preferred field
        List<String> values = new ArrayList<String>();
        values.addAll(normalize(getValue(rec, mm)));
        int pos = 0;
        for (String value : values) {
			// if we have already identified the cris object using another field is not
			// necessary to make a second lookup
        	if (results.size() > pos && results.get(pos) != null) {
        		continue;
        	}
            if (StringUtils.isNotBlank(value)) {
                DiscoverQuery query = new DiscoverQuery();
                query.setQuery("search.resourcetype:" + resourceTypeID
                        + " AND " + metadataInputConfiguration.get(mm)
                        + ":\"" + value + "\"");
                DiscoverResult result = searchService.search(null,
                        query, true);

                T cris = null;
                if (result.getTotalSearchResults() > 0) {
                    cris = (T) result.getDspaceObjects().get(0);
					confidences.add(
							result.getTotalSearchResults() == 1 ? Choices.CF_UNCERTAIN : Choices.CF_AMBIGUOUS);
                }
                else {
                	confidences.add(Choices.CF_UNSET);
                }
                if (results.size() > pos) {
                	results.set(pos, cris);
                }
                else {
                	results.add(cris);
                }
            }
            pos++;
        }
    }

    /**
     * Process the retrieved object to populate the MutableRecord.
     * @param rec the MutableRecord
     * @param object the retrieved object
     * @param pos the position of the retrieved object
     * @param confidence the confidence of the retrieved object
     * @throws SearchServiceException
     */
    protected void process(MutableRecord rec, Object object, int pos, int confidence) {
        ACrisObject cris = null;
        if (object instanceof ACrisObject) {
            cris = (ACrisObject) object;
        }
        if (cris != null) {
			// only process the additional information if a CRIS object has been found
			for (String propShortname : mappingOutputConfiguration.keySet()) {
				String bteField = mappingOutputConfiguration.get(propShortname);
				List<Value> exValues = rec.getValues(bteField);
				List<Value> newValues = new ArrayList<Value>();
				rec.removeField(bteField);

				// add back all the existing values as is
				for (int iPos = 0; iPos < pos; iPos++) {
					newValues.add(exValues.size() > iPos ? exValues.get(iPos) : new StringValue(""));
				}

				if (ResearcherPageUtils.getStringValue(cris, propShortname) != null) {
					if (mappingAuthorityConfiguration.contains(bteField)) {
						newValues.add(new StringValue(ResearcherPageUtils.getStringValue(cris, propShortname)
								+ SubmissionLookupService.SEPARATOR_VALUE_REGEX + cris.getCrisID()
								+ SubmissionLookupService.SEPARATOR_VALUE_REGEX + confidence));
					} else {
						newValues.add(new StringValue(ResearcherPageUtils.getStringValue(cris, propShortname)));
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
			for (String propShortname : mappingOutputConfiguration.keySet()) {
				String bteField = mappingOutputConfiguration.get(propShortname);
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

    public List<String> normalize(List<String> values)
    {
        // overrided this method to perform any normalization
        return values;
    }

    protected List<String> getValue(MutableRecord rec, String md)
    {
        List<String> result = new ArrayList<String>();
        if (StringUtils.isNotBlank(md))
        {
            List<Value> vals = rec.getValues(md);
            if (vals != null && vals.size() > 0)
            {
                for (Value val : vals)
                {
                    result.add(val.getAsString());
                }
            }
        }

        return result;
    }

    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    public Class<T> getClazzCrisObject()
    {
        return clazzCrisObject;
    }

    public void setClazzCrisObject(Class<T> clazzCrisObject)
    {
        this.clazzCrisObject = clazzCrisObject;
    }

    public void setMetadataInputConfiguration(
            Map<String, String> metadataInputConfiguration)
    {
        this.metadataInputConfiguration = metadataInputConfiguration;
    }

    public void setMappingOutputConfiguration(
            Map<String, String> mappingOutputConfiguration)
    {
        this.mappingOutputConfiguration = mappingOutputConfiguration;
    }

    public void setMappingAuthorityConfiguration(
            List<String> mappingAuthorityConfiguration)
    {
        this.mappingAuthorityConfiguration = mappingAuthorityConfiguration;
    }

    public void setMappingAuthorityRequiredConfiguration(
            List<String> mappingAuthorityRequiredConfiguration)
    {
        this.mappingAuthorityRequiredConfiguration = mappingAuthorityRequiredConfiguration;
    }

    public void setResourceTypeID(Integer resourceTypeID)
    {
        this.resourceTypeID = resourceTypeID;
    }

}
