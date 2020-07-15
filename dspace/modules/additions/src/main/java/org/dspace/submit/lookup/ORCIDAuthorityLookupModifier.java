package org.dspace.submit.lookup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.dspace.app.cris.model.ACrisObject;
import org.dspace.app.cris.model.ResearcherPage;
import org.dspace.app.cris.util.ResearcherPageUtils;
import org.dspace.content.MetadataValue;
import org.dspace.content.authority.Choices;
import org.dspace.discovery.DiscoverQuery;
import org.dspace.discovery.DiscoverResult;

import gr.ekt.bte.core.MutableRecord;
import gr.ekt.bte.core.Record;
import gr.ekt.bte.core.StringValue;

public class ORCIDAuthorityLookupModifier<T extends ACrisObject> extends AuthorityLookupModifier<ACrisObject> {

    private static Logger log = Logger.getLogger(ORCIDAuthorityLookupModifier.class);
    private Class<T> clazzCrisObject;
    private HashMap<String,String> mappingAuthorityConfiguration;

    private boolean useScopusAuthorID = false;
    private boolean useAuthorVariants = false;
    private boolean useContainsMatchInAffiliation = true;

    @Override
    public Record modify(MutableRecord rec) {
        try
        {
            LinkedHashMap<String, LinkedHashMap<String, T>> results = new LinkedHashMap<String, LinkedHashMap<String, T>>();
            boolean found = false;
            List<String> orcids = getValue(rec,"orcid");
            List<String> authScopusids = getValue(rec,"authorScopusID");
            List<String> authorsWithAffiliation = getValue(rec,"authorsWithAffiliation");
            List<String> pmid = getValue(rec,"pubmedID");

            HashMap<String,String[]> authors2affs = new HashMap<String,String[]>();
            for(String awa :authorsWithAffiliation) {
                String[] awaArray = StringUtils.split(awa, "##");
                if(awaArray!= null && awaArray.length==2) {
                    authors2affs.put(awaArray[0], StringUtils.split(awaArray[1], ":::"));
                }
            }
            for (String mm : metadataInputConfiguration.keySet())
            {
                LinkedHashMap<String, T> internalResults = new LinkedHashMap<String, T>();
                List<String> values = new ArrayList<String>();

                values.addAll(normalize(getValue(rec, mm)));

                boolean addOrcidFilter = orcids.size() == values.size();
                boolean addAuthIDFilter = authScopusids.size() == values.size() && useScopusAuthorID;
                int x=0;
                for (String value : values)
                {
                    if (StringUtils.isNotBlank(value))
                    {
                        DiscoverQuery query = new DiscoverQuery();
                        DiscoverResult result = null;
                        T crisObject = null;
                        int confidence =0;
                        StringBuilder sb = new StringBuilder("search.resourcetype:").append(resourceTypeID).append(" AND (");

                        boolean runQuery = false;
                        if (addOrcidFilter)
                        {
                            String orcid = orcids.get(x);
                            if (StringUtils.isNotBlank(orcid)
                                    && !StringUtils.equals(orcid, MetadataValue.PARENT_PLACEHOLDER_VALUE))
                            {
                                sb.append("crisrp.orcid:").append(
                                        ClientUtils.escapeQueryChars(orcid));
                                runQuery = true;
                            }
                        }

                        if (addAuthIDFilter)
                        {
                            String authid = authScopusids.get(x);
                            if (StringUtils.isNotBlank(authid)
                                    && !StringUtils.equals(authid, MetadataValue.PARENT_PLACEHOLDER_VALUE))
                            {
                                // there is also the orcid
                                if (runQuery) {
                                    sb.append(" OR ");
                                }
                                sb.append("crisrp.scopusid:").append(
                                        ClientUtils.escapeQueryChars(authid));
                                runQuery = true;
                            }
                        }
                        sb.append(")");

                        if (runQuery)
                        {
                            query.setQuery(sb.toString());
                            result = searchService.search(null, query, true);

                            if (result.getTotalSearchResults() == 1)
                            {
                                crisObject = (T) result.getDspaceObjects()
                                        .get(0);
                                confidence = Choices.CF_ACCEPTED;
                                found = true;
                            }
                        }

                        if (confidence == 0)
                        {
                            SolrQuery solrQuery = new SolrQuery();
                            solrQuery.setQuery("search.resourcetype:"
                                    + resourceTypeID + " AND "
                                    + metadataInputConfiguration.get(mm) + ":\""
                                    + value + "\"");
                            QueryResponse response = searchService
                                    .search(solrQuery);
                            SolrDocumentList docs = response.getResults();

                            SolrDocumentList rpVariants = checkExactMatch(docs,
                                    useAuthorVariants ? "crisrp.variants"
                                            : "crisrp.fullName",
                                    value);
                            SolrDocumentList rpVariantsAffs = new SolrDocumentList();
                            if (authors2affs.get(value) != null)
                            {
                                String[] affs = authors2affs.get(value);
                                rpVariantsAffs = useContainsMatchInAffiliation?
                                        checkContainsMatch(rpVariants,
                                                "crisrp.dept.variants", affs):
                                        checkExactMatch(rpVariants,
                                        "crisrp.dept.variants", affs);
                            }

                            int id = 0;
                            if (rpVariantsAffs.size() == 1 && !pmid.isEmpty())
                            {
                                id = (Integer) rpVariantsAffs.get(0)
                                        .getFirstValue("search.resourceid");
                                confidence = Choices.CF_UNCERTAIN;
                                found = true;
                            }
                            else if (rpVariantsAffs.size() == 1)
                            {
                                id = (Integer) rpVariantsAffs.get(0)
                                        .getFirstValue("search.resourceid");
                                confidence = Choices.CF_AMBIGUOUS;
                                found = true;
                            }
                            else if (rpVariants.size() == 1)
                            {
                                id = (Integer) rpVariants.get(0)
                                        .getFirstValue("search.resourceid");
                                confidence = Choices.CF_NOTFOUND;
                                found = true;
                            }

                            if (id > 0)
                            {
                                crisObject = (T) ResearcherPageUtils
                                        .getCrisObject(id,
                                                ResearcherPage.class);
                            }
                        }


                        internalResults.put(value+ SubmissionLookupService.SEPARATOR_VALUE_REGEX+confidence, crisObject);
                    }
                    x++;
                }
                results.put(mm, internalResults);
            }

            if(found) {
                for (String shortname : mappingOutputConfiguration.keySet())
                {
                    rec.removeField(mappingOutputConfiguration.get(shortname));

                    for(Map<String, T> mapInternal : results.values()) {

                        for(String key : mapInternal.keySet()) {
                            T object = mapInternal.get(key);
                            String[] keyVal = StringUtils.split(key, SubmissionLookupService.SEPARATOR_VALUE_REGEX);

                            if(object!=null) {
                                List<String> valuesMetadata = object
                                        .getMetadataValue(shortname);
                                if (valuesMetadata != null)
                                {

                                    for (String valueMetadata : valuesMetadata)
                                    {
                                        if (mappingAuthorityConfiguration
                                                .containsKey(shortname))
                                        {
                                            rec.addValue(
                                                    mappingAuthorityConfiguration
                                                            .get(shortname),
                                                    new StringValue(valueMetadata
                                                            + SubmissionLookupService.SEPARATOR_VALUE_REGEX
                                                            + object.getCrisID()
                                                            + SubmissionLookupService.SEPARATOR_VALUE_REGEX
                                                            + keyVal[1]));
                                        }
                                        else
                                        {
                                            rec.addValue(
                                                    mappingOutputConfiguration
                                                            .get(shortname),
                                                    new StringValue(valueMetadata));
                                        }
                                    }
                                }
                            }
                            else
                            {
                                rec.addValue(mappingOutputConfiguration.get(shortname),
                                        new StringValue(keyVal[0]));
                            }
                        }

                    }

                }
            }
            return rec;
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    private SolrDocumentList checkExactMatch(SolrDocumentList list,String property, String value) {
        Iterator<SolrDocument> iter = list.iterator();
        SolrDocumentList result = new SolrDocumentList();
        while(iter.hasNext()) {
            SolrDocument doc = iter.next();
            Collection<Object> fieldValues = doc.getFieldValues(property);
            if (fieldValues != null) {
                for(Object str : fieldValues) {
                    if(StringUtils.equalsIgnoreCase((String)str,value)) {
                        result.add(doc);
                        break;
                    }
                }
            }
        }
        return result;
    }

    private SolrDocumentList checkContainsMatch(SolrDocumentList list,String property, String[] values) {
        Iterator<SolrDocument> iter = list.iterator();
        SolrDocumentList result = new SolrDocumentList();
        while(iter.hasNext()) {
            SolrDocument doc = iter.next();
            Collection<Object> fieldValues = doc.getFieldValues(property);
            if (fieldValues != null) {
                props:
                for(Object str :fieldValues) {
                    for(String value: values) {
                        if(StringUtils.containsIgnoreCase((String) str,value) ||
                                StringUtils.containsIgnoreCase(value, (String) str)) {
                            result.add(doc);
                            break props;
                        }
                    }
                }
            }
        }
        return result;
    }

    private SolrDocumentList checkExactMatch(SolrDocumentList list,String property, String[] values) {
        Iterator<SolrDocument> iter = list.iterator();
        SolrDocumentList result = new SolrDocumentList();
        while(iter.hasNext()) {
            SolrDocument doc = iter.next();
            Collection<Object> fieldValues = doc.getFieldValues(property);
            if (fieldValues != null) {
                props:
                for(Object str :fieldValues) {
                    for(String value: values) {
                        if(StringUtils.equalsIgnoreCase((String) str,value)) {
                            result.add(doc);
                            break props;
                        }
                    }
                }
            }
        }
        return result;
    }
    public HashMap<String, String> getMappingAuthorityConfiguration() {
        return mappingAuthorityConfiguration;
    }

    public void setMappingAuthorityConfiguration(HashMap<String, String> mappingAuthorityConfiguration) {
        this.mappingAuthorityConfiguration = mappingAuthorityConfiguration;
    }

    public void setUseScopusAuthorID(boolean useScopusAuthorID)
    {
        this.useScopusAuthorID = useScopusAuthorID;
    }

    public boolean isUseScopusAuthorID()
    {
        return useScopusAuthorID;
    }

    public void setUseContainsMatchInAffiliation(
            boolean useContainsMatchInAffiliation)
    {
        this.useContainsMatchInAffiliation = useContainsMatchInAffiliation;
    }

    public boolean isUseContainsMatchInAffiliation()
    {
        return useContainsMatchInAffiliation;
    }

}
