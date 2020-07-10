/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.submit.lookup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpException;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.discovery.SearchService;
import org.dspace.discovery.SearchServiceException;
import org.dspace.submit.util.SubmissionLookupPublication;

import gr.ekt.bte.core.MutableRecord;
import gr.ekt.bte.core.Record;
import gr.ekt.bte.core.StringValue;

public class LocalDuplicateDataLoader extends NetworkSubmissionLookupDataLoader
{
    private static final String LOCAL_DUPLICATE_DATALOADER_NAME = "localduplicate";
    private SearchService searchService;
    private Map<String, String> identifiers2metadata;
    
    private static final Logger log = Logger.getLogger(LocalDuplicateDataLoader.class);
    
    public SearchService getSearchService()
    {
        return searchService;
    }

    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    public void setIdentifiers2metadata(
            Map<String, String> identifiers2metadata)
    {
        this.identifiers2metadata = identifiers2metadata;
    }
    
    public Map<String, String> getIdentifiers2metadata()
    {
        return identifiers2metadata;
    }
    
    @Override
    public List<String> getSupportedIdentifiers()
    {
        List<String> supportedIdentifiers = new ArrayList<String>();
        for (String id : identifiers2metadata.keySet()) {
            supportedIdentifiers.add(id);
        }
        return supportedIdentifiers;
    }

    @Override
    public boolean isSearchProvider()
    {
        return false;
    }

    @Override
    public List<Record> search(Context context, String title, String author,
            int year) throws HttpException, IOException
    {
        return null;
    }

    @Override
    public List<Record> getByIdentifier(Context context,
            Map<String, Set<String>> keys) throws HttpException, IOException
    {
        List<Record> results = new ArrayList<Record>();
        SolrQuery solrQuery = new SolrQuery();
        StringBuffer query = new StringBuffer();
        
        for (String id : identifiers2metadata.keySet()) {
            Set<String> idValues = keys != null ? keys.get(id) : null;    
            buildQuery(idValues, query, identifiers2metadata.get(id));
        }
        
        List<Record> solrRecords = new ArrayList<Record>();

        if (query.length() > 0)
        {
            solrQuery.setQuery(query.toString());
            try
            {
                QueryResponse response = searchService.search(solrQuery);
                SolrDocumentList docList = response.getResults();
                Iterator<SolrDocument> solrDoc = docList.iterator();
                while (solrDoc.hasNext())
                {
                    SolrDocument doc = solrDoc.next();
                    Integer resourceId = (Integer) doc
                            .getFirstValue("search.resourceid");
                    String handle = (String) doc.getFirstValue("handle");
                    String url = ConfigurationManager.getProperty("dspace.url")
                            + "/handle/" + handle;
                    MutableRecord record = new SubmissionLookupPublication(
                            LOCAL_DUPLICATE_DATALOADER_NAME);

                    record.addValue("id", new StringValue("" + resourceId));
                    record.addValue("handle", new StringValue(handle));
                    record.addValue("url", new StringValue(url));

                    for (String id : identifiers2metadata.keySet()) {
                        Object idValue = doc.getFirstValue(identifiers2metadata.get(id));
                        if (idValue != null)
                        {
                            record.addValue(id,
                                    new StringValue(idValue.toString()));
                        }
                    }
                    solrRecords.add(record);
                }
            }
            catch (SearchServiceException e)
            {
                log.error("Error retrieving documents", e);
            }

            for (Record p : solrRecords)
            {
                results.add(convertFields(p));
            }
        }
        return results;
    }

    private void buildQuery(Set<String> identifiers, StringBuffer query,
            String metadata)
    {
        if (StringUtils.isNotEmpty(metadata) && identifiers != null
                && !identifiers.isEmpty())
        {
            if (query.length() > 0)
            {
                query.append(" OR ");
            }
            int count = 0;
            for (String identifier : identifiers)
            {
                if (count > 0)
                {
                    query.append(" OR ");
                }
                query.append(metadata + ":\"" + identifier + "\"");
                count++;
            }
        }
    }
    
}
