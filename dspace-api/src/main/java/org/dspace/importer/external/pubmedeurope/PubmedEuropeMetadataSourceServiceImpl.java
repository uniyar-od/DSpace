/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.importer.external.pubmedeurope;

import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import javax.el.MethodNotFoundException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dspace.content.Item;
import org.dspace.importer.external.datamodel.ImportRecord;
import org.dspace.importer.external.datamodel.Query;
import org.dspace.importer.external.exception.MetadataSourceException;
import org.dspace.importer.external.service.AbstractImportMetadataSourceService;
import org.dspace.importer.external.service.components.QuerySource;
import org.dspace.services.ConfigurationService;
import org.jaxen.JaxenException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.springframework.beans.factory.annotation.Autowired;


public class PubmedEuropeMetadataSourceServiceImpl
    extends AbstractImportMetadataSourceService<Element> implements QuerySource {

    private static final Logger log = LogManager.getLogger(PubmedEuropeMetadataSourceServiceImpl.class);

    @Autowired
    private ConfigurationService configurationService;


    @Override
    public String getImportSource() {
        return "pubmedeu";
    }

    @Override
    public ImportRecord getRecord(String id) throws MetadataSourceException {
        List<ImportRecord> records = retry(new SearchByIdCallable(id));
        return records == null || records.isEmpty() ? null : records.get(0);
    }

    @Override
    public int getRecordsCount(String query) throws MetadataSourceException {
        return retry(new CountByQueryCallable(query));
    }

    @Override
    public int getRecordsCount(Query query) throws MetadataSourceException {
        return retry(new CountByQueryCallable(query));
    }

    @Override
    public Collection<ImportRecord> getRecords(String query, int start, int count) throws MetadataSourceException {
        return retry(new SearchByQueryCallable(query, count, start));
    }

    @Override
    public Collection<ImportRecord> getRecords(Query query) throws MetadataSourceException {
        return retry(new SearchByQueryCallable(query));
    }

    @Override
    public ImportRecord getRecord(Query query) throws MetadataSourceException {
        List<ImportRecord> records = retry(new SearchByIdCallable(query));
        return records == null || records.isEmpty() ? null : records.get(0);
    }

    @Override
    public Collection<ImportRecord> findMatchingRecords(Query query) throws MetadataSourceException {
        return retry(new FindMatchingRecordCallable(query));
    }

    @Override
    public Collection<ImportRecord> findMatchingRecords(Item item) throws MetadataSourceException {
        throw new MethodNotFoundException("This method is not implemented for CrossRef");
    }

    @Override
    public void init() throws Exception {
    }

    private class SearchByQueryCallable implements Callable<List<ImportRecord>> {

        private Query query;

        private SearchByQueryCallable(String queryString, Integer maxResult, Integer start) {
            query = new Query();
            query.addParameter("query", queryString);
            query.addParameter("count", maxResult);
            query.addParameter("start", start);
        }

        private SearchByQueryCallable(Query query) {
            this.query = query;
        }

        @Override
        public List<ImportRecord> call() throws Exception {
            Integer count = query.getParameterAsClass("count", Integer.class);
            Integer start = query.getParameterAsClass("start", Integer.class);
            String queryString = query.getParameterAsClass("query", String.class);
            return search(queryString, count, start);

        }
    }

    private class SearchByIdCallable implements Callable<List<ImportRecord>> {
        private Query query;

        private SearchByIdCallable(Query query) {
            this.query = query;
        }

        private SearchByIdCallable(String id) {
            this.query = new Query();
            query.addParameter("id", id);
        }

        @Override
        public List<ImportRecord> call() throws Exception {
            return getByPubmedEuropeID(query.getParameterAsClass("id", String.class), 1 ,0);
        }
    }

    public class FindMatchingRecordCallable implements Callable<List<ImportRecord>> {

        private Query query;

        private FindMatchingRecordCallable(Query q) {
            query = q;
        }

        @Override
        public List<ImportRecord> call() throws Exception {
            String title = query.getParameterAsClass("title", String.class);
            String author = query.getParameterAsClass("author", String.class);
            Integer year = query.getParameterAsClass("year", Integer.class);
            Integer maxResult = query.getParameterAsClass("maxResult", Integer.class);
            Integer start = query.getParameterAsClass("start", Integer.class);
            return search(title, author, year, maxResult, start);
        }

    }

    private class CountByQueryCallable implements Callable<Integer> {
        private Query query;


        private CountByQueryCallable(String queryString) {
            query = new Query();
            query.addParameter("query", queryString);
        }

        private CountByQueryCallable(Query query) {
            this.query = query;
        }

        @Override
        public Integer call() throws Exception {
            try {
                return count(query.getParameterAsClass("query", String.class));
            } catch (Exception e) {
                throw new RuntimeException();
            }
        }
    }

    public Integer count(String query) throws URISyntaxException, ClientProtocolException, IOException, JaxenException {
        try {
            String proxyHost = configurationService.getProperty("http.proxy.host");
            String proxyPort = configurationService.getProperty("http.proxy.port");
            HttpGet method = null;
            HttpHost proxy = null;
            HttpClient client = new DefaultHttpClient();
            client.getParams().setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 2000);
            URIBuilder uriBuilder = new URIBuilder(
                "https://www.ebi.ac.uk/europepmc/webservices/rest/search");
            uriBuilder.addParameter("format", "xml");
            uriBuilder.addParameter("resulttype", "core");
            uriBuilder.addParameter("pageSize", "1");
            uriBuilder.addParameter("query", query);
            method = new HttpGet(uriBuilder.build());
            if (StringUtils.isNotBlank(proxyHost) && StringUtils.isNotBlank(proxyPort)) {
                proxy = new HttpHost(proxyHost, Integer.parseInt(proxyPort), "http");
                client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
            }
            // Execute the method.
            HttpResponse response = client.execute(method);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                throw new RuntimeException("WS call failed: "
                    + statusLine);
            }
            String input = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
            System.out.println("Value: " + input);
            SAXBuilder saxBuilder = new SAXBuilder();
            Document document = saxBuilder.build(new StringReader(input));
            Element root = document.getRootElement();
            Element element = root.getChild("hitCount");
            return Integer.parseInt(element.getValue());
        } catch (JDOMException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public List<ImportRecord> search(String title, String author, int year, int count, int start)
            throws HttpException, IOException {
        StringBuffer query = new StringBuffer();
        query.append("(");
        if (StringUtils.isNotBlank(title)) {
            query.append("TITLE:").append(title);
            query.append(")");
        }
        if (StringUtils.isNotBlank(author)) {
            String splitRegex = "(\\s*,\\s+|\\s*;\\s+|\\s*;+|\\s*,+|\\s+)";
            String[] authors = author.split(splitRegex);
            // [FAU]
            if (query.length() > 0) {
                query.append(" AND ");
            }
            query.append("(");
            int x = 0;
            for (String auth : authors) {
                x++;
                query.append("AUTH:\"").append(auth).append("\"");
                if (x < authors.length) {
                    query.append(" AND ");
                }
            }
            query.append(")");
        }
        if (year != -1) {
            // [DP]
            if (query.length() > 0) {
                query.append(" AND ");
            }
            query.append("( PUB_YEAR:").append(year).append(")");
        }
        query.append(")");
        return search(query.toString(), count, start);
    }

    public List<ImportRecord> search(String query, Integer count, Integer start) throws IOException, HttpException {
        List<ImportRecord> results = new ArrayList<>();
        String proxyHost = configurationService.getProperty("http.proxy.host");
        String proxyPort = configurationService.getProperty("http.proxy.port");
        HttpGet method = null;
        HttpHost proxy = null;
        try {
            HttpClient client = new DefaultHttpClient();
            client.getParams().setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 2000);
            URIBuilder uriBuilder = new URIBuilder(
                "https://www.ebi.ac.uk/europepmc/webservices/rest/search");
            uriBuilder.addParameter("format", "xml");
            uriBuilder.addParameter("resulttype", "core");
            uriBuilder.addParameter("pageSize", String.valueOf(count));
            uriBuilder.addParameter("query", query);
            boolean lastPage = false;
            int skipped = 0;
            while (!lastPage || results.size() < count) {
                method = new HttpGet(uriBuilder.build());
                if (StringUtils.isNotBlank(proxyHost) && StringUtils.isNotBlank(proxyPort)) {
                    proxy = new HttpHost(proxyHost, Integer.parseInt(proxyPort), "http");
                    client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
                }
                // Execute the method.
                HttpResponse response = client.execute(method);
                StatusLine statusLine = response.getStatusLine();
                int statusCode = statusLine.getStatusCode();
                if (statusCode != HttpStatus.SC_OK) {
                    throw new RuntimeException("WS call failed: "
                     + statusLine);
                }
                String input = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
                SAXBuilder saxBuilder = new SAXBuilder();
                Document document = saxBuilder.build(new StringReader(input));
                XPathFactory xpfac = XPathFactory.instance();
                XPathExpression<Element> xPath = xpfac.compile("//responseWrapper/resultList/result",
                    Filters.element());
                List<Element> records = xPath.evaluate(document);
                if (records.size() > 0) {
                    for (Element item : records) {
                        if (start > skipped) {
                            skipped++;
                        } else {
                            results.add(transformSourceRecords(item));
                        }
                        if (results.size() == count) {
                            break;
                        }
                    }
                } else {
                    lastPage = true;
                    break;
                }
                Element root = document.getRootElement();
                Element nextCursorMark = root.getChild("nextCursorMark");
                String cursorMark = Objects.nonNull(nextCursorMark) ? nextCursorMark.getValue() : StringUtils.EMPTY;
                if (!"*".equals(cursorMark)) {
                    uriBuilder.setParameter("cursorMar", cursorMark);
                } else {
                    lastPage = true;
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException();
        }
        return results;
    }

    public List<ImportRecord> getByPubmedEuropeID(String pubmedID, Integer start, Integer count)
        throws IOException, HttpException {
        String query = "(EXT_ID:" + pubmedID + ")";
        return search(query.toString(), count, start);
    }


    private String getKey() {
        return configurationService.getProperty("submission.lookup.pubmed.key");
    }
}
