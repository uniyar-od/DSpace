package org.dspace.submit.lookup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        return Arrays.asList(new String[] { WOSID, PUBMED, DOI, SCOPUSEID, ISBN});
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

        boolean appender = false;
        
        if(dois != null && !dois.isEmpty()) {
        int doicount = 0;
        for(String doi : dois) {
            if(doicount>0) {
                query.append(" OR ");    
            }
            query.append(getDoiMetadata() + ":\"" +doi + "\"");
            doicount++;
            appender = true;
            }
        }
        
        if (isbnid != null && !isbnid.isEmpty())
        {
            if(appender) query.append(" OR ");
            int isbncount = 0;
            for (String isbn : isbnid)
            {
                if (isbncount > 0)
                {
                    query.append(" OR ");
                }
                query.append(getIsbnMetadata() + ":\"" + isbn + "\"");
                isbncount++;
                appender = true;
            }
        }
   
        if (scopuseid != null && !scopuseid.isEmpty())
        {
            int scopuscount = 0;
            for (String scid : scopuseid)
            {
                if (scopuscount > 0)
                {
                    query.append(" OR ");
                }
                query.append(getDoiMetadata() + ":\"" + scid + "\"");
                scopuscount++;
                appender = true;
            }
        }

        if (wosid != null && !wosid.isEmpty())
        {
            if(appender) query.append(" OR ");
            int wosidcount = 0;
            for (String wos : wosid)
            {
                if (wosidcount > 0)
                {
                    query.append(" OR ");
                }
                query.append(getWosMetadata() + ":\"" + wos + "\"");
                wosidcount++;
                appender = true;
            }
        }

        if (pmids != null && !pmids.isEmpty())
        {
            if(appender) query.append(" OR ");
            int pmcount = 0;
            for (String pmid : pmids)
            {
                if (pmcount > 0)
                {
                    query.append(" OR ");
                }
                query.append(getPubmedMetadata() + ":\"" + pmid + "\"");
                pmcount++;
                appender = true;
            }
        }
       
       solrQuery.setQuery(query.toString());
       
       List<Record> solrRecords = new ArrayList<Record>();
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
               String handle = (String) doc
                       .getFirstValue("handle");
               String url = ConfigurationManager.getProperty("dspace.url")
                       + "/handle/" + handle;
               MutableRecord record = new SubmissionLookupPublication("solr");
              
               record.addValue("id",new StringValue(""+resourceId));
               record.addValue("handle",new StringValue(handle));
               record.addValue("url", new StringValue(url));
               
               Object scopusID =  doc.getFirstValue(getScopusMetadata());
               Object isbnID =  doc.getFirstValue(getIsbnMetadata());
               Object wosID =  doc.getFirstValue(getWosMetadata());
               Object pubmedID =  doc.getFirstValue(getPubmedMetadata());
               Object doiID =  doc.getFirstValue(getDoiMetadata());
              
               if(scopusID != null) {
                   record.addValue(SCOPUSEID,  new StringValue(scopusID.toString() ) );
               }
               if(isbnID != null) {
                   record.addValue(ISBN,  new StringValue(isbnID.toString() ) );
               }
               if(wosID != null) {
                   record.addValue(WOSID,  new StringValue(wosID.toString() ) );
               }
               if(doiID!= null) {
                    record.addValue(DOI,  new StringValue(doiID.toString() ) );
                }
               if(pubmedID != null) {
                   record.addValue(PUBMED,  new StringValue(pubmedID.toString() ) );
               }
               solrRecords.add(record);
           }
       }
       catch (SearchServiceException e)
       {
           log
           .error("Error retrieving documents", e);
       }
       
       for (Record p : solrRecords)
       {
           results.add(convertFields(p));
       }
       return results;
       
    }
    
}
