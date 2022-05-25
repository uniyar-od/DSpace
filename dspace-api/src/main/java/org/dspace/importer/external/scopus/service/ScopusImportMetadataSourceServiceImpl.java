/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.importer.external.scopus.service;

import java.io.InputStream;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.el.MethodNotFoundException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.log4j.Logger;
import org.dspace.content.Item;
import org.dspace.importer.external.datamodel.ImportRecord;
import org.dspace.importer.external.datamodel.Query;
import org.dspace.importer.external.exception.MetadataSourceException;
import org.dspace.importer.external.service.AbstractImportMetadataSourceService;
import org.dspace.importer.external.service.DoiCheck;
import org.dspace.importer.external.service.components.QuerySource;
import org.dspace.services.ConfigurationService;
import org.jaxen.JaxenException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implements a data source for querying Scopus
 * 
 * @author Pasquale Cavallo (pasquale.cavallo at 4Science dot it)
 *
 */

public class ScopusImportMetadataSourceServiceImpl extends AbstractImportMetadataSourceService<OMElement>
    implements QuerySource {

    @Autowired
    private ConfigurationService configurationService;

    private int timeout = 1000;

    private static final Logger log = Logger.getLogger(ScopusImportMetadataSourceServiceImpl.class);

    int itemPerPage = 25;

    private static final String ENDPOINT_SEARCH_SCOPUS = "https://api.elsevier.com/content/search/scopus";
    private String apiKey;
    private String instKey;
    private String viewMode;

    /**
     * Initialize the class
     *
     * @throws Exception on generic exception
     */
    @Override
    public void init() throws Exception { }

    /**
     * The string that identifies this import implementation. Preferable a URI
     *
     * @return the identifying uri
     */
    @Override
    public String getImportSource() {
        return "scopus";
    }

    @Override
    public int getRecordsCount(String query) throws MetadataSourceException {
        if (isEID(query)) {
            return retry(new FindByIdCallable(query)).size();
        }
        if (DoiCheck.isDoi(query)) {
            query = DoiCheck.purgeDoiValue(query);
        }
        return retry(new SearchNBByQueryCallable(query));
    }

    @Override
    public int getRecordsCount(Query query) throws MetadataSourceException {
        if (isEID(query.toString())) {
            return retry(new FindByIdCallable(query.toString())).size();
        }
        if (DoiCheck.isDoi(query.toString())) {
            query.addParameter("query", DoiCheck.purgeDoiValue(query.toString()));
        }
        return retry(new SearchNBByQueryCallable(query));
    }

    @Override
    public Collection<ImportRecord> getRecords(String query, int start,
            int count) throws MetadataSourceException {
        if (isEID(query)) {
            return retry(new FindByIdCallable(query));
        }
        if (DoiCheck.isDoi(query)) {
            query = DoiCheck.purgeDoiValue(query);
        }
        return retry(new SearchByQueryCallable(query, count, start));
    }

    @Override
    public Collection<ImportRecord> getRecords(Query query)
            throws MetadataSourceException {
        if (isEID(query.toString())) {
            return retry(new FindByIdCallable(query.toString()));
        }
        if (DoiCheck.isDoi(query.toString())) {
            query.addParameter("query", DoiCheck.purgeDoiValue(query.toString()));
        }
        return retry(new SearchByQueryCallable(query));
    }


    @Override
    public ImportRecord getRecord(Query query) throws MetadataSourceException {
        List<ImportRecord> records = null;
        if (DoiCheck.isDoi(query.toString())) {
            query.addParameter("query", DoiCheck.purgeDoiValue(query.toString()));
        }
        if (isEID(query.toString())) {
            records = retry(new FindByIdCallable(query.toString()));
        } else {
            records = retry(new SearchByQueryCallable(query));
        }
        return records == null || records.isEmpty() ? null : records.get(0);
    }

    @Override
    public Collection<ImportRecord> findMatchingRecords(Item item)
            throws MetadataSourceException {
        throw new MethodNotFoundException("This method is not implemented for Scopus");
    }

    @Override
    public ImportRecord getRecord(String id) throws MetadataSourceException {
        List<ImportRecord> records = retry(new FindByIdCallable(id));
        return records == null || records.isEmpty() ? null : records.get(0);
    }

    @Override
    public Collection<ImportRecord> findMatchingRecords(Query query)
            throws MetadataSourceException {
        if (isEID(query.toString())) {
            return retry(new FindByIdCallable(query.toString()));
        }
        if (DoiCheck.isDoi(query.toString())) {
            query.addParameter("query", DoiCheck.purgeDoiValue(query.toString()));
        }
        return retry(new FindByQueryCallable(query));
    }

    private boolean isEID(String query) {
        Pattern pattern = Pattern.compile("2-s2\\.0-\\d+");
        Matcher match = pattern.matcher(query);
        if (match.matches()) {
            return true;
        }
        return false;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setInstKey(String instKey) {
        this.instKey = instKey;
    }

    /**
 * 
 * This class implements a callable to get the numbers of result
 * @author pasquale
 *
 */
    private class SearchNBByQueryCallable implements Callable<Integer> {

        private String query;

        private SearchNBByQueryCallable(String queryString) {
            this.query = queryString;
        }

        private SearchNBByQueryCallable(Query query) {
            this.query = query.getParameterAsClass("query", String.class);
        }


        @Override
        public Integer call() throws Exception {
            String proxyHost = configurationService.getProperty("http.proxy.host");
            String proxyPort = configurationService.getProperty("http.proxy.port");
            if (StringUtils.isNotBlank(apiKey)) {
                HttpGet method = null;
                try {
                    HttpClientBuilder hcBuilder = HttpClients.custom();
                    Builder requestConfigBuilder = RequestConfig.custom();
                    requestConfigBuilder.setConnectionRequestTimeout(timeout);

                    if (StringUtils.isNotBlank(proxyHost)
                        && StringUtils.isNotBlank(proxyPort)) {
                        HttpHost proxy = new HttpHost(proxyHost, Integer.parseInt(proxyPort), "http");
                        DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
                        hcBuilder.setRoutePlanner(routePlanner);
                    }

                    HttpClient client = hcBuilder.build();
                    // open session
                    method = new HttpGet(getSearchUrl(query));
                    method.setConfig(requestConfigBuilder.build());
                        // Execute the method.
                    HttpResponse httpResponse = client.execute(method);
                    int statusCode = httpResponse.getStatusLine().getStatusCode();
                    if (statusCode != HttpStatus.SC_OK) {
                        throw new RuntimeException("WS call failed: "
                                                           + statusCode);
                    }
                    InputStream is = httpResponse.getEntity().getContent();
                    String response = IOUtils.toString(is, StandardCharsets.UTF_8);
                    OMXMLParserWrapper records = OMXMLBuilderFactory.createOMBuilder(new StringReader(response));
                    OMElement element = records.getDocumentElement();
                    AXIOMXPath xpath = null;
                    try {
                        xpath = new AXIOMXPath("opensearch:totalResults");
                        xpath.addNamespace("opensearch", "http://a9.com/-/spec/opensearch/1.1/");
                        OMElement count = (OMElement) xpath.selectSingleNode(element);
                        return Integer.parseInt(count.getText());
                    } catch (JaxenException e) {
                        return null;
                    }
                } catch (Exception e1) {
                    log.error(e1.getMessage(), e1);
                } finally {
                    if (method != null) {
                        method.releaseConnection();
                    }
                }
            }
            return null;
        }
    }


    private class FindByIdCallable implements Callable<List<ImportRecord>> {

        private String eid;

        private FindByIdCallable(String doi) {
            this.eid = doi;
        }


        @Override
        public List<ImportRecord> call() throws Exception {
            List<ImportRecord> results = new ArrayList<>();
            String queryString = "EID(" + eid.replace("!", "/") + ")";
            String proxyHost = configurationService.getProperty("http.proxy.host");
            String proxyPort = configurationService.getProperty("http.proxy.port");
            if (StringUtils.isNotBlank(apiKey)) {
                HttpGet method = null;
                try {
                    HttpClientBuilder hcBuilder = HttpClients.custom();
                    Builder requestConfigBuilder = RequestConfig.custom();
                    requestConfigBuilder.setConnectionRequestTimeout(timeout);

                    if (StringUtils.isNotBlank(proxyHost)
                        && StringUtils.isNotBlank(proxyPort)) {
                        HttpHost proxy = new HttpHost(proxyHost, Integer.parseInt(proxyPort), "http");
                        DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
                        hcBuilder.setRoutePlanner(routePlanner);
                    }

                    HttpClient client = hcBuilder.build();
                    // open session
                    method = new HttpGet(getSearchUrl(queryString, viewMode));
                    method.setConfig(requestConfigBuilder.build());
                        // Execute the method.
                    HttpResponse httpResponse = client.execute(method);
                    int statusCode = httpResponse.getStatusLine().getStatusCode();
                    if (statusCode != HttpStatus.SC_OK) {
                        throw new RuntimeException("WS call failed: "
                                                           + statusCode);
                    }
                    InputStream is = httpResponse.getEntity().getContent();
                    String response = IOUtils.toString(is, StandardCharsets.UTF_8);
                    List<OMElement> omElements = splitToRecords(response);
                    for (OMElement record : omElements) {
                        results.add(transformSourceRecords(record));
                    }
                } catch (Exception e1) {
                    log.error(e1.getMessage(), e1);
                } finally {
                    if (method != null) {
                        method.releaseConnection();
                    }
                }
            }
            return results;
        }
    }


/**
 * 
 * This class implements a callable to get the items based on query parameters
 * @author pasquale
 *
 */
    private class FindByQueryCallable implements Callable<List<ImportRecord>> {

        private String title;
        private String author;
        private Integer year;
        private Integer start;
        private Integer count;

        private FindByQueryCallable(Query query) {
            this.title = query.getParameterAsClass("title", String.class);
            this.year = query.getParameterAsClass("year", Integer.class);
            this.author = query.getParameterAsClass("author", String.class);
            this.start = query.getParameterAsClass("start", Integer.class) != null ?
                query.getParameterAsClass("start", Integer.class) : 0;
            this.count = query.getParameterAsClass("count", Integer.class) != null ?
                query.getParameterAsClass("count", Integer.class) : 20;
        }


        @Override
        public List<ImportRecord> call() throws Exception {
            List<ImportRecord> results = new ArrayList<>();
            String queryString = "";
            StringBuffer query = new StringBuffer();
            if (StringUtils.isNotBlank(title)) {
                query.append("title(").append(title).append("");
            }
            if (StringUtils.isNotBlank(author)) {
                // [FAU]
                if (query.length() > 0) {
                    query.append(" AND ");
                }
                query.append("AUTH(").append(author).append(")");
            }
            if (year != -1) {
                // [DP]
                if (query.length() > 0) {
                    query.append(" AND ");
                }
                query.append("PUBYEAR IS ").append(year);
            }
            queryString = query.toString();

            String proxyHost = configurationService.getProperty("http.proxy.host");
            String proxyPort = configurationService.getProperty("http.proxy.port");
            if (apiKey != null && !apiKey.equals("")) {
                HttpGet method = null;
                try {
                    HttpClientBuilder hcBuilder = HttpClients.custom();
                    Builder requestConfigBuilder = RequestConfig.custom();
                    requestConfigBuilder.setConnectionRequestTimeout(timeout);

                    if (StringUtils.isNotBlank(proxyHost)
                        && StringUtils.isNotBlank(proxyPort)) {
                        HttpHost proxy = new HttpHost(proxyHost, Integer.parseInt(proxyPort), "http");
                        DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
                        hcBuilder.setRoutePlanner(routePlanner);
                    }

                    HttpClient client = hcBuilder.build();
                    // open session
                    method = new HttpGet(getSearchUrl(queryString, viewMode, start, count));
                    method.setConfig(requestConfigBuilder.build());
                        // Execute the method.
                    HttpResponse httpResponse = client.execute(method);
                    int statusCode = httpResponse.getStatusLine().getStatusCode();
                    if (statusCode != HttpStatus.SC_OK) {
                        throw new RuntimeException("WS call failed: "
                                                           + statusCode);
                    }
                    InputStream is = httpResponse.getEntity().getContent();
                    String response = IOUtils.toString(is, StandardCharsets.UTF_8);
                    List<OMElement> omElements = splitToRecords(response);
                    for (OMElement record : omElements) {
                        results.add(transformSourceRecords(record));
                    }
                } catch (Exception e1) {
                    log.error(e1.getMessage(), e1);
                } finally {
                    if (method != null) {
                        method.releaseConnection();
                    }
                }
            }
            return results;
        }
    }

    /**
     * That's ok, just a separator
     * @author pasquale
     *
     */
    private class SearchByQueryCallable implements Callable<List<ImportRecord>> {
        private Query query;


        private SearchByQueryCallable(String queryString, Integer maxResult, Integer start) {
            query = new Query();
            query.addParameter("query", queryString);
            query.addParameter("start", start);
            query.addParameter("count", maxResult);
        }

        private SearchByQueryCallable(Query query) {
            this.query = query;
        }


        @Override
        public List<ImportRecord> call() throws Exception {
            List<ImportRecord> results = new ArrayList<>();
            String queryString = query.getParameterAsClass("query", String.class);
            Integer start = query.getParameterAsClass("start", Integer.class);
            Integer count = query.getParameterAsClass("count", Integer.class);
            String proxyHost = configurationService.getProperty("http.proxy.host");
            String proxyPort = configurationService.getProperty("http.proxy.port");
            if (apiKey != null && !apiKey.equals("")) {
                HttpGet method = null;
                try {
                    HttpClientBuilder hcBuilder = HttpClients.custom();
                    Builder requestConfigBuilder = RequestConfig.custom();
                    requestConfigBuilder.setConnectionRequestTimeout(timeout);

                    if (StringUtils.isNotBlank(proxyHost)
                        && StringUtils.isNotBlank(proxyPort)) {
                        HttpHost proxy = new HttpHost(proxyHost, Integer.parseInt(proxyPort), "http");
                        DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
                        hcBuilder.setRoutePlanner(routePlanner);
                    }

                    HttpClient client = hcBuilder.build();
                    // open session
                    method = new HttpGet(getSearchUrl(queryString, viewMode, start, count));
                    method.setConfig(requestConfigBuilder.build());
                        // Execute the method.
                    HttpResponse httpResponse = client.execute(method);
                    int statusCode = httpResponse.getStatusLine().getStatusCode();
                    if (statusCode != HttpStatus.SC_OK) {
                        throw new RuntimeException("WS call failed: "
                                                           + statusCode);
                    }
                    InputStream is = httpResponse.getEntity().getContent();
                    String response = IOUtils.toString(is, Charset.defaultCharset());
                    List<OMElement> omElements = splitToRecords(response);
                    for (OMElement record : omElements) {
                        results.add(transformSourceRecords(record));
                    }
                } catch (Exception e1) {
                    log.error(e1.getMessage(), e1);
                } finally {
                    if (method != null) {
                        method.releaseConnection();
                    }
                }
            }
            return results;
        }
    }

    private String getSearchUrl(String query, String viewMode, Integer start, Integer count) throws URISyntaxException {

        String baseUri = getSearchUrl(query, viewMode);

        URIBuilder uriBuilder = new URIBuilder(baseUri);
        uriBuilder.setParameter("start", (start != null ? start + "" : "0"));
        uriBuilder.setParameter("count", (count != null ? count + "" : "20"));

        return uriBuilder.toString();
    }

    private String getSearchUrl(String query) throws URISyntaxException {
        return getSearchUrl(query, null);
    }

    private String getSearchUrl(String query, String viewMode) throws URISyntaxException {

        URIBuilder uriBuilder = new URIBuilder(ENDPOINT_SEARCH_SCOPUS);
        uriBuilder.setParameter("httpAccept", "application/xml");
        uriBuilder.setParameter("apiKey", apiKey);

        if (StringUtils.isNotBlank(instKey)) {
            uriBuilder.setParameter("insttoken", instKey);
        }

        if (StringUtils.isNotBlank(viewMode)) {
            uriBuilder.setParameter("view", viewMode);
        }

        uriBuilder.setParameter("query", query);

        return uriBuilder.toString();
    }

    @SuppressWarnings("unchecked")
    private List<OMElement> splitToRecords(String recordsSrc) {
        OMXMLParserWrapper records = OMXMLBuilderFactory.createOMBuilder(new StringReader(recordsSrc));
        OMElement element = records.getDocumentElement();
        AXIOMXPath xpath = null;
        try {
            xpath = new AXIOMXPath("ns:entry");
            xpath.addNamespace("ns", "http://www.w3.org/2005/Atom");
            List<OMElement> recordsList = xpath.selectNodes(element);
            return recordsList;
        } catch (JaxenException e) {
            return null;
        }
    }

    public String getViewMode() {
        return viewMode;
    }

    public void setViewMode(String viewMode) {
        this.viewMode = viewMode;
    }

}
