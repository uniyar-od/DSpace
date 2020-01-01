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
    private String doiMetadata;
    private String isbnMetadata;
    private String wosMetadata;
    private String scopusMetadata;
    private String pubmedMetadata;
    
    private static final Logger log = Logger.getLogger(LocalDuplicateDataLoader.class);
    
    public String getDoiMetadata()
    {
        return doiMetadata;
    }

    public void setDoiMetadata(String doiMetadata)
    {
        this.doiMetadata = doiMetadata;
    }

    public String getIsbnMetadata()
    {
        return isbnMetadata;
    }

    public void setIsbnMetadata(String isbnMetadata)
    {
        this.isbnMetadata = isbnMetadata;
    }

    public String getWosMetadata()
    {
        return wosMetadata;
    }

    public void setWosMetadata(String wosMetadata)
    {
        this.wosMetadata = wosMetadata;
    }

    public String getScopusMetadata()
    {
        return scopusMetadata;
    }

    public void setScopusMetadata(String scopusMetadata)
    {
        this.scopusMetadata = scopusMetadata;
    }

    public String getPubmedMetadata()
    {
        return pubmedMetadata;
    }

    public void setPubmedMetadata(String pubmedMetadata)
    {
        this.pubmedMetadata = pubmedMetadata;
    }

    public SearchService getSearchService()
    {
        return searchService;
    }

    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    @Override
    public List<String> getSupportedIdentifiers()
    {
        List<String> supportedIdentifiers = new ArrayList<String>();
        
        if (StringUtils.isNotBlank(getDoiMetadata())) {
            supportedIdentifiers.add(DOI);
        }
        if (StringUtils.isNotBlank(getWosMetadata())) {
            supportedIdentifiers.add(WOSID);
        }
        if (StringUtils.isNotBlank(getPubmedMetadata())) {
            supportedIdentifiers.add(PUBMED);
        }
        if (StringUtils.isNotBlank(getScopusMetadata())) {
            supportedIdentifiers.add(SCOPUSEID);
        }
        if (StringUtils.isNotBlank(getIsbnMetadata())) {
            supportedIdentifiers.add(ISBN);
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
        Set<String> dois = keys != null ? keys.get(DOI) : null;
        Set<String> pmids = keys != null ? keys.get(PUBMED) : null;        
        Set<String> scopuseid = keys != null ? keys.get(SCOPUSEID) : null;
        Set<String> wosid = keys != null ? keys.get(WOSID) : null;
        Set<String> isbnid = keys != null ? keys.get(ISBN) : null;
        
        List<Record> results = new ArrayList<Record>();
        
        SolrQuery solrQuery = new SolrQuery();
        
        StringBuffer query = new StringBuffer();
        buildQuery(dois, query, getDoiMetadata());
        buildQuery(isbnid, query, getIsbnMetadata());
        buildQuery(scopuseid, query, getScopusMetadata());
        buildQuery(wosid, query, getWosMetadata());
        buildQuery(pmids, query, getPubmedMetadata());

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

                    Object scopusID = doc.getFirstValue(getScopusMetadata());
                    Object isbnID = doc.getFirstValue(getIsbnMetadata());
                    Object wosID = doc.getFirstValue(getWosMetadata());
                    Object pubmedID = doc.getFirstValue(getPubmedMetadata());
                    Object doiID = doc.getFirstValue(getDoiMetadata());

                    if (scopusID != null)
                    {
                        record.addValue(SCOPUSEID,
                                new StringValue(scopusID.toString()));
                    }
                    if (isbnID != null)
                    {
                        record.addValue(ISBN,
                                new StringValue(isbnID.toString()));
                    }
                    if (wosID != null)
                    {
                        record.addValue(WOSID,
                                new StringValue(wosID.toString()));
                    }
                    if (doiID != null)
                    {
                        record.addValue(DOI, new StringValue(doiID.toString()));
                    }
                    if (pubmedID != null)
                    {
                        record.addValue(PUBMED,
                                new StringValue(pubmedID.toString()));
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
