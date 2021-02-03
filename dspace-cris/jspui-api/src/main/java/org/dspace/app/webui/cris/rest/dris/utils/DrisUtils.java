/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * https://github.com/CILEA/dspace-cris/wiki/License
 */
package org.dspace.app.webui.cris.rest.dris.utils;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.dspace.app.webui.cris.rest.dris.JsonLdEntry;
import org.dspace.app.webui.cris.servlet.DrisQueryingServlet;
import org.dspace.core.ConfigurationManager;
import org.dspace.discovery.SearchService;
import org.dspace.discovery.SearchServiceException;

/**
 * Utility class to provide search support and url builder
 *
 */
public class DrisUtils
{

    private static Logger log = Logger.getLogger(JsonLdEntry.class);
    
    public final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    
    // General (production) URL of the API, now setted by dsGet for any call
    public static String API_URL = ConfigurationManager.getProperty("dris-rest", "dris.endpoint.baseurl");
    
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
                obj.put("name",  (String)doc.getFirstValue("crisou.name"));
                String discoverable = (String)doc.getFirstValue("discoverable");
                if("true".equals(discoverable)) {
                    obj.put("country",  buildMiniVocabCountryIdLink((String)doc.getFirstValue("crisou.countrylink_authority")));                    
                }
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

    public static String buildEntryIdLink(String crisId) {
    	return DrisQueryingServlet.getMainApiUrl() + "/" + DrisQueryingServlet.ENTRIES_QUERY_TYPE_NAME + "/" + StringUtils.trimToEmpty(crisId);
    }
    
    public static String buildVocabCountryAuthLink(String countryAuth) {
        return DrisQueryingServlet.getMainApiUrl() + "/" + DrisQueryingServlet.VOCABS_QUERY_TYPE_NAME + "/" + 
                   DrisQueryingServlet.VOCABS_QUERY_TYPE_COUNTRIES_SUB_TYPE + "/" + StringUtils.trimToEmpty(countryAuth);
    }
    
    public static String buildMiniOrgUnitIdLink(String id) {
        return DrisQueryingServlet.ORG_UNITS_QUERY_TYPE_NAME + ":" + StringUtils.trimToEmpty(id);
    }
    
    public static String buildMiniVocabStatusIdLink(String id) {
        return DrisQueryingServlet.VOCABS_QUERY_TYPE_NAME + ":" + 
                   DrisQueryingServlet.VOCABS_QUERY_TYPE_STATUSES_SUB_TYPE + "/" + StringUtils.trimToEmpty(id);
    }
    
    public static String buildMiniVocabCountryIdLink(String id) {
        return DrisQueryingServlet.VOCABS_QUERY_TYPE_NAME + ":" + 
                   DrisQueryingServlet.VOCABS_QUERY_TYPE_COUNTRIES_SUB_TYPE + "/" + StringUtils.trimToEmpty(id);
    }
    
    public static String buildMiniVocabScopeIdLink(String id) {
        return DrisQueryingServlet.VOCABS_QUERY_TYPE_NAME + ":" + 
                   DrisQueryingServlet.VOCABS_QUERY_TYPE_SCOPES_SUB_TYPE + "/" + StringUtils.trimToEmpty(id);
    }
    
    public static String buildMiniVocabPlatformIdLink(String id) {
        return DrisQueryingServlet.VOCABS_QUERY_TYPE_NAME + ":" + 
                   DrisQueryingServlet.VOCABS_QUERY_TYPE_CRIS_PLATFORMS_SUB_TYPE + "/" + StringUtils.trimToEmpty(id);
    }

    public static String buildMiniVocabCoverageIdLink(String id) {
        return DrisQueryingServlet.VOCABS_QUERY_TYPE_NAME + ":" + 
                   DrisQueryingServlet.VOCABS_QUERY_TYPE_COVERAGES_SUB_TYPE + "/" + StringUtils.trimToEmpty(id);
    }

    public static String buildVocabStatusIdLink(String id)
    {
        return DrisQueryingServlet.getMainApiUrl() + "/" + DrisQueryingServlet.VOCABS_QUERY_TYPE_NAME + "/" + DrisQueryingServlet.VOCABS_QUERY_TYPE_STATUSES_SUB_TYPE + "/" + StringUtils.trimToEmpty(id);
    }

    public static String buildVocabScopeIdLink(String id)
    {
        return DrisQueryingServlet.getMainApiUrl() + "/" + DrisQueryingServlet.VOCABS_QUERY_TYPE_NAME + "/" + DrisQueryingServlet.VOCABS_QUERY_TYPE_SCOPES_SUB_TYPE + "/" + StringUtils.trimToEmpty(id);
    }
    
    public static String buildVocabPlatformIdLink(String id)
    {
        return DrisQueryingServlet.getMainApiUrl() + "/" + DrisQueryingServlet.VOCABS_QUERY_TYPE_NAME + "/" + DrisQueryingServlet.VOCABS_QUERY_TYPE_CRIS_PLATFORMS_SUB_TYPE + "/" + StringUtils.trimToEmpty(id);
    }
    
    public static String buildVocabCoverageIdLink(String id)
    {
        return DrisQueryingServlet.getMainApiUrl() + "/" + DrisQueryingServlet.VOCABS_QUERY_TYPE_NAME + "/" + DrisQueryingServlet.VOCABS_QUERY_TYPE_COVERAGES_SUB_TYPE + "/" + StringUtils.trimToEmpty(id);
    }

    public static String buildOrgunitIdLink(String id)
    {
        return DrisQueryingServlet.getMainApiUrl() + "/" + DrisQueryingServlet.ORG_UNITS_QUERY_TYPE_NAME + "/" + StringUtils.trimToEmpty(id);        
    }

    public static String buildVocabsIdLink(String crisId,
            String crisVocabularyType)
    {
        String result = "";
        switch (crisVocabularyType)
        {
        case "status":
            result = buildVocabStatusIdLink(crisId);
            break;
        case "scope":
            result = buildVocabScopeIdLink(crisId);
            break;
        case "coverage":
            result = buildVocabCoverageIdLink(crisId);
            break;
        case "cris-platform":
            result = buildVocabPlatformIdLink(crisId);
            break;            
        default:
            result = buildVocabCountryAuthLink(crisId);
            break;
        }
        return result;
    }
}
