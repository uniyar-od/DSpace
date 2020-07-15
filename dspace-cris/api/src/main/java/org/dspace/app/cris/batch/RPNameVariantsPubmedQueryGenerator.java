package org.dspace.app.cris.batch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.dspace.discovery.SearchService;
import org.dspace.discovery.SearchServiceException;
import org.dspace.utils.DSpace;

public class RPNameVariantsPubmedQueryGenerator extends AFeedVariantsQueryGenerator {

    private static Logger log = Logger.getLogger(RPNameVariantsPubmedQueryGenerator.class);

    @Override
    public HashMap<Integer, List<String>> generate() {
        HashMap<Integer,List<String>> map = new HashMap<Integer,List<String>>();
        SearchService searchService = new DSpace().getSingletonService(SearchService.class);
        SolrQuery solrQuery = new SolrQuery("search.resourcetype:9");
        solrQuery.setRows(Integer.MAX_VALUE);
        if (getSolrExtraFilter() != null) {
            solrQuery.addFilterQuery(getSolrExtraFilter());
        }
        try {
            QueryResponse res = searchService.search(solrQuery);
            SolrDocumentList docs = res.getResults();
            Iterator<SolrDocument> iter = docs.iterator();

            List<String> notOwnedList = new ArrayList<String>();
            while(iter.hasNext()) {

                SolrDocument doc = iter.next();

                List<Object> variants = isUseVariants()
                        ? (List<Object>) doc.getFieldValues("crisrp.variants")
                        : new ArrayList<Object>();
                StringBuffer buffer = new StringBuffer();
                int x=0;
                for(Object variant: variants) {
                    if(x>0) {
                        buffer.append(" OR ");
                    }
                    buffer.append( variant.toString());
                    buffer.append( " [Author - full]");
                    x++;
                }

                for(String filter : getExtraFilters()) {
                    buffer.append(" ");
                    buffer.append(filter);
                }
                if(doc.containsKey("owner_i")){
                    Integer submitterId = (Integer) doc.getFirstValue("owner_i");
                    List<String> list = new ArrayList<String>();
                    list.add(buffer.toString());
                    map.put(submitterId, list);
                }else {
                    notOwnedList.add(buffer.toString());
                }
            }
            if(notOwnedList != null && !notOwnedList.isEmpty()) {
                map.put(getDefaultSubmitter(),notOwnedList);
            }
        } catch (SearchServiceException e) {
            log.error(e.getMessage(), e);
        }
        return map;
    }
}
