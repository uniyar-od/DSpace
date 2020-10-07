package org.dspace.app.webui.cris.rest.dris;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.dspace.app.webui.cris.util.AbstractJsonLdResult;
import org.dspace.discovery.SearchService;
import org.dspace.discovery.SearchServiceException;

public class DrisUtils
{

    private static Logger log = Logger.getLogger(JsonLdEntry.class);
    
    public final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    
    public static Map<String, Object> buildCountryIncluded(
            SearchService service, String crisid,
            String url)
    {
        Map<String, Object> obj = new HashMap<>();
        SolrQuery solrQuery = new SolrQuery();
        QueryResponse rsp;
        try {
            solrQuery = new SolrQuery();
            solrQuery.setQuery("cris-id:\"" + crisid +"\"");
            solrQuery.setRows(1);
            rsp = service.search(solrQuery);
            SolrDocumentList solrResults = rsp.getResults();
            Iterator<SolrDocument> iter = solrResults.iterator();
            while (iter.hasNext()) {
                SolrDocument doc = iter.next();
                obj.put("@id", url);
                
                Map<String, String> label = new HashMap<>();
                label.put("en", (String)doc.getFirstValue("crisdo.name"));
                obj.put("label", label);
                
                Map<String, String> isocode = new HashMap<>();
                isocode.put("Alpha2", (String)doc.getFirstValue("criscountry.countryalphacode2"));
                isocode.put("Alpha3", (String)doc.getFirstValue("criscountry.countryalphacode3"));
                isocode.put("Numeric", (String)doc.getFirstValue("criscountry.countrynumericcode"));
                obj.put("iso_3166_codes", isocode);                
                break;
            }
        } catch (SearchServiceException e) {
            log.error(e.getMessage(), e);
        }   
        return obj;
    }

    public static Map<String, Object> buildOrgUnitIncluded(
            SearchService service, String crisid, String url)
    {
        Map<String, Object> obj = new HashMap<>();
        SolrQuery solrQuery = new SolrQuery();
        QueryResponse rsp;
        try {
            solrQuery = new SolrQuery();
            solrQuery.setQuery("cris-id:\"" + crisid +"\"");
            solrQuery.setRows(1);
            rsp = service.search(solrQuery);
            SolrDocumentList solrResults = rsp.getResults();
            Iterator<SolrDocument> iter = solrResults.iterator();
            while (iter.hasNext()) {
                SolrDocument doc = iter.next();
                obj.put("@id", url);
                obj.put("label",  (String)doc.getFirstValue("crisou.name"));
                obj.put("country",  AbstractJsonLdResult.buildMiniVocabCountryIdLink((String)doc.getFirstValue("crisou.countrylink_authority")));
                break;
            }
        } catch (SearchServiceException e) {
            log.error(e.getMessage(), e);
        }   
        return obj;
    }

    public static Map<String, Object> buildVocabsIncluded(SearchService service, String crisid, String url)
    {
        Map<String, Object> obj = new HashMap<>();
        SolrQuery solrQuery = new SolrQuery();
        QueryResponse rsp;
        try {
            solrQuery = new SolrQuery();
            solrQuery.setQuery("cris-id:\"" + crisid +"\"");
            solrQuery.setRows(1);
            rsp = service.search(solrQuery);
            SolrDocumentList solrResults = rsp.getResults();
            Iterator<SolrDocument> iter = solrResults.iterator();
            while (iter.hasNext()) {
                SolrDocument doc = iter.next();
                obj.put("@id", url);
                
                Map<String, String> label = new HashMap<>();
                label.put("en", (String)doc.getFirstValue("crisdo.name"));
                obj.put("label", label);
                break;
            }
        } catch (SearchServiceException e) {
            log.error(e.getMessage(), e);
        }   
        return obj;
    }
}
