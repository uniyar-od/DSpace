/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.importer.external.epo.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xerces.impl.dv.util.Base64;
import org.dspace.content.Item;
import org.dspace.importer.external.datamodel.ImportRecord;
import org.dspace.importer.external.datamodel.Query;
import org.dspace.importer.external.exception.MetadataSourceException;
import org.dspace.importer.external.metadatamapping.MetadataFieldConfig;
import org.dspace.importer.external.metadatamapping.contributor.EpoIdMetadataContributor.EpoDocumentId;
import org.dspace.importer.external.service.AbstractImportMetadataSourceService;
import org.dspace.importer.external.service.components.QuerySource;
import org.jaxen.JaxenException;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.Text;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;


/**
 * Implements a data source for querying EPO
 * 
 * @author Pasquale Cavallo (pasquale.cavallo at 4Science dot it)
 *
 */
public class EpoImportMetadataSourceServiceImpl extends AbstractImportMetadataSourceService<Element>
    implements QuerySource {

    private String consumerKey;
    private String consumerSecret;
    private MetadataFieldConfig dateFiled;
    private MetadataFieldConfig applicationNumber;

    private static final Logger log = LogManager.getLogger(EpoImportMetadataSourceServiceImpl.class);

    private static final String endPointAuthService =
            "https://ops.epo.org/3.2/auth/accesstoken";
    private static final String endPointPublisherDataSearchService =
            "http://ops.epo.org/rest-services/published-data/search";
    private static final String endPointPublisherDataRetriveService =
            "http://ops.epo.org/rest-services/published-data/publication/$(doctype)/$(id)/biblio";

    public static final String APP_NO_DATE_SEPARATOR = "$$$";
    private static final String APP_NO_DATE_SEPARATOR_REGEX = "\\$\\$\\$";

/**
     * Initialize the class
     *
     * @throws Exception on generic exception
     */
    @Override
    public void init() throws Exception {
    }

    /**
     * The string that identifies this import implementation. Preferable a URI
     *
     * @return the identifying uri
     */
    @Override
    public String getImportSource() {
        return "epo";
    }

    /**
     * Set the customer epo key
     * @param consumerKey the customer consumer key
     */
    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }

    /**
     * Set the costumer epo secret
     * @param consumerSecret the customer epo secret
     */
    public void setConsumerSecret(String consumerSecret) {
        this.consumerSecret = consumerSecret;
    }

    public void setDateFiled(MetadataFieldConfig dateFiled) {
        this.dateFiled = dateFiled;
    }

    public MetadataFieldConfig getDateFiled() {
        return dateFiled;
    }

    public void setApplicationNumber(MetadataFieldConfig applicationNumber) {
        this.applicationNumber = applicationNumber;
    }

    public MetadataFieldConfig getApplicationNumber() {
        return applicationNumber;
    }

    /***
     * Log to EPO, bearer is valid for 20 minutes
     * 
     * @param consumerKey       The consumer Key
     * @param consumerSecretKey The consumer secret key
     * @return
     * @throws IOException
     * @throws HttpException
     */
    protected String login() throws IOException, HttpException {
        HttpPost method = null;
        String accessToken = null;
        try {
            HttpClient client = HttpClientBuilder.create().build();
            method = new HttpPost(endPointAuthService);
            String authString = consumerKey + ":" + consumerSecret;
            method.setHeader("Authorization", "Basic " + Base64.encode(authString.getBytes()));
            method.setHeader("Content-type", "application/x-www-form-urlencoded");
            StringEntity entity = new StringEntity("grant_type=client_credentials");
            method.setEntity(entity);
            HttpResponse httpResponse = client.execute(method);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                throw new RuntimeException(
                        "Call to " + endPointAuthService + " fails: " + httpResponse.getStatusLine());
            }
            String json = EntityUtils.toString(httpResponse.getEntity());
            JsonFactory factory = new JsonFactory();
            ObjectMapper mapper = new ObjectMapper(factory);
            JsonNode rootNode = mapper.readTree(json);
            JsonNode accessTokenNode = rootNode.get("access_token");
            accessToken = accessTokenNode.asText();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            if (method != null) {
                method.releaseConnection();
            }
        }
        return accessToken;
    }


    @Override
    public int getRecordsCount(String query) throws MetadataSourceException {
        if (StringUtils.isNotBlank(consumerKey) && StringUtils.isNotBlank(consumerSecret)) {
            try {
                String bearer = login();
                return retry(new CountRecordsCallable(query, bearer));
            } catch (IOException | HttpException e) {
                e.printStackTrace();
            }
        }
        return 0;

    }

    @Override
    public int getRecordsCount(Query query) throws MetadataSourceException {
        if (StringUtils.isNotBlank(consumerKey) && StringUtils.isNotBlank(consumerSecret)) {
            try {
                String bearer = login();
                return retry(new CountRecordsCallable(query, bearer));
            } catch (IOException | HttpException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    @Override
    public Collection<ImportRecord> getRecords(String query, int start,
            int count) throws MetadataSourceException {
        if (StringUtils.isNotBlank(consumerKey) && StringUtils.isNotBlank(consumerSecret)) {
            try {
                String bearer = login();
                return retry(new SearchByQueryCallable(query, bearer, start, count));
            } catch (IOException | HttpException e) {
                e.printStackTrace();
            }
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public Collection<ImportRecord> getRecords(Query query)
            throws MetadataSourceException {
        if (StringUtils.isNotBlank(consumerKey) && StringUtils.isNotBlank(consumerSecret)) {
            try {
                String bearer = login();
                return retry(new SearchByQueryCallable(query, bearer));
            } catch (IOException | HttpException e) {
                e.printStackTrace();
            }
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public ImportRecord getRecord(String id) throws MetadataSourceException {
        if (StringUtils.isNotBlank(consumerKey) && StringUtils.isNotBlank(consumerSecret)) {
            try {
                String bearer = login();
                List<ImportRecord> list = retry(new SearchByIdCallable(id, bearer));
                if (list == null || list.isEmpty()) {
                    return null;
                } else {
                    return list.get(0);
                }
            } catch (IOException | HttpException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public ImportRecord getRecord(Query query) throws MetadataSourceException {
        return null;
    }

    @Override
    public Collection<ImportRecord> findMatchingRecords(Item item)
            throws MetadataSourceException {
        return null;
    }

    @Override
    public Collection<ImportRecord> findMatchingRecords(Query query)
            throws MetadataSourceException {
        return null;
    }

    private class CountRecordsCallable implements Callable<Integer> {

        String bearer;
        String query;

        private CountRecordsCallable(Query query, String bearer) {
            this.query = query.getParameterAsClass("query", String.class);
            this.bearer = bearer;
        }

        private CountRecordsCallable(String query, String bearer) {
            this.query = query;
            this.bearer = bearer;
        }

        public Integer call() throws Exception {
            return countDocument(bearer, query);
        }
    }


    private class SearchByIdCallable implements Callable<List<ImportRecord>> {
        String bearer;
        String id;

        private SearchByIdCallable(String id, String bearer) {
            this.id = id;
            this.bearer = bearer;
        }

        public List<ImportRecord> call() throws Exception {
            int positionToSplit = id.indexOf(":");
            String docType = EpoDocumentId.EPODOC;
            String idS = id;
            if (positionToSplit != -1) {
                docType = id.substring(0, positionToSplit);
                idS = id.substring(positionToSplit + 1, id.length());
            } else if (id.contains(APP_NO_DATE_SEPARATOR)) {
                 // special case the id is the combination of the applicationnumber and date filed
                String query = "applicationnumber=" + id.split(APP_NO_DATE_SEPARATOR_REGEX)[0];
                SearchByQueryCallable search = new SearchByQueryCallable(query, bearer, 0, 10);
                List<ImportRecord> records = search.call().stream()
                        .filter(r -> r.getValue(dateFiled.getSchema(), dateFiled.getElement(),
                                    dateFiled.getQualifier())
                                .stream()
                                .anyMatch(m -> StringUtils.equals(m.getValue(),
                                        id.split(APP_NO_DATE_SEPARATOR_REGEX)[1])
                        ))
                        .limit(1).collect(Collectors.toList());
                return records;
            }
            List<ImportRecord> records = searchDocument(bearer, idS, docType);
            if (records.size() > 1) {
                log.warn("More record are returned with epocID " + id);
            }
            return records;
        }
    }


    private class SearchByQueryCallable implements Callable<List<ImportRecord>> {
        private Query query;
        private String bearer;
        private Integer start;
        private Integer count;

        private SearchByQueryCallable(Query query, String bearer) {
            this.query = query;
            this.bearer = bearer;
        }


        public SearchByQueryCallable(String queryValue, String bearer, int start, int count) {
            this.query = new Query();
            query.addParameter("query", queryValue);
            this.start = query.getParameterAsClass("start", Integer.class) != null ?
                query.getParameterAsClass("start", Integer.class) : 0;
            this.count = query.getParameterAsClass("count", Integer.class) != null ?
                query.getParameterAsClass("count", Integer.class) : 20;
            this.bearer = bearer;
        }

        @Override
        public List<ImportRecord> call() throws Exception {
            List<ImportRecord> records = new ArrayList<ImportRecord>();
            String queryString = query.getParameterAsClass("query", String.class);
            if (StringUtils.isNotBlank(consumerKey) && StringUtils.isNotBlank(consumerSecret)) {
                if (StringUtils.isNotBlank(queryString) && StringUtils.isNotBlank(bearer)) {
                    List<EpoDocumentId> epoDocIds = searchDocumentIds(bearer, queryString, start + 1, count);
                    for (EpoDocumentId epoDocId : epoDocIds) {
                        List<ImportRecord> recordfounds = searchDocument(bearer, epoDocId);
                        if (recordfounds.size() > 1) {
                            log.warn("More record are returned with epocID " + epoDocId.toString());
                        }
                        records.addAll(recordfounds);
                    }
                }

            }
            return records;
        }
    }

    private Integer countDocument(String bearer, String query) {
        if (StringUtils.isBlank(bearer)) {
            return null;
        }
        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet method = new HttpGet(endPointPublisherDataSearchService);
            method.setHeader("Authorization", "Bearer " + bearer);
            method.setHeader("X-OPS-Range", "1-1");
            URI uri = new URIBuilder(method.getURI()).addParameter("q", query).build();
            ((HttpRequestBase) method).setURI(uri);
            HttpResponse httpResponse = client.execute(method);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                throw new RuntimeException(
                        "Call to " + endPointPublisherDataSearchService + " fails: " + httpResponse.getStatusLine());
            }
            InputStream is = httpResponse.getEntity().getContent();
            String response = IOUtils.toString(is, Charsets.UTF_8);
            log.debug(response);
            SAXBuilder saxBuilder = new SAXBuilder();
            Document document = saxBuilder.build(new StringReader(response));
            Element root = document.getRootElement();

            List<Namespace> namespaces = Arrays.asList(
                Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink"),
                Namespace.getNamespace("ops", "http://ops.epo.org"),
                Namespace.getNamespace("ns", "http://www.epo.org/exchange"));

            String totalRes = getElement(root, namespaces, "//ops:biblio-search/@total-result-count");
            return Integer.parseInt(totalRes);
        } catch (JDOMException | IOException | URISyntaxException | JaxenException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }
    private List<EpoDocumentId> searchDocumentIds(String bearer, String query, int start, int count) {
        List<EpoDocumentId> results = new ArrayList<EpoDocumentId>();
        int end = start + count;
        if (StringUtils.isBlank(bearer)) {
            return results;
        }
        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet method = new HttpGet(endPointPublisherDataSearchService);
            method.setHeader("Authorization", "Bearer " + bearer);
            if (start >= 1 && end > start) {
                method.setHeader("X-OPS-Range", start + "-" + end);
            }
            URI uri = new URIBuilder(method.getURI()).addParameter("q", query).build();
            ((HttpRequestBase) method).setURI(uri);
            HttpResponse httpResponse = client.execute(method);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                throw new RuntimeException(
                        "Call to " + endPointPublisherDataSearchService + " fails: " + httpResponse.getStatusLine());
            }
            InputStream is = httpResponse.getEntity().getContent();
            String response = IOUtils.toString(is, Charsets.UTF_8);
            log.debug(response);
            SAXBuilder saxBuilder = new SAXBuilder();
            Document document = saxBuilder.build(new StringReader(response));
            Element root = document.getRootElement();

            List<Namespace> namespaces = Arrays.asList(
                Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink"),
                Namespace.getNamespace("ops", "http://ops.epo.org"),
                Namespace.getNamespace("ns", "http://www.epo.org/exchange"));
            XPathExpression<Element> xpath = XPathFactory.instance()
                .compile("//ns:document-id", Filters.element(), null, namespaces);

            List<Element> documentIds = xpath.evaluate(root);
            for (Element documentId : documentIds) {
                results.add(new EpoDocumentId(documentId, namespaces));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return results;
    }

    private List<ImportRecord> searchDocument(String bearer, EpoDocumentId id) {
        return searchDocument(bearer, id.getId(), id.getDocumentIdType());
    }

    private List<ImportRecord> searchDocument(String bearer, String id, String docType) {
        List<ImportRecord> results = new ArrayList<ImportRecord>();
        if (StringUtils.isBlank(bearer)) {
            return results;
        }
        try {
            String endPointPublisherDataRetriveService = this.endPointPublisherDataRetriveService;
            endPointPublisherDataRetriveService = endPointPublisherDataRetriveService
                    .replace("$(doctype)", docType).replace("$(id)", id);
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet method = new HttpGet(endPointPublisherDataRetriveService);
            method.setHeader("Authorization", "Bearer " + bearer);

            HttpResponse httpResponse = client.execute(method);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                throw new RuntimeException(
                        "Call to " + endPointPublisherDataRetriveService + " fails: " + httpResponse.getStatusLine());
            }
            InputStream is = httpResponse.getEntity().getContent();
            String response = IOUtils.toString(is, Charsets.UTF_8);
            log.debug(response);
            List<Element> elements = splitToRecords(response);
            for (Element element : elements) {
                results.add(transformSourceRecords(element));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return results;
    }

    private List<Element> splitToRecords(String recordsSrc) {
        try {
            SAXBuilder saxBuilder = new SAXBuilder();
            Document document = saxBuilder.build(new StringReader(recordsSrc));
            Element root = document.getRootElement();
            List<Namespace> namespaces = Arrays.asList(Namespace.getNamespace("ns", "http://www.epo.org/exchange"));
            XPathExpression<Element> xpath = XPathFactory.instance().compile("//ns:exchange-document",
                Filters.element(), null, namespaces);

            List<Element> recordsList = xpath.evaluate(root);
            return recordsList;
        } catch (JDOMException | IOException e) {
            log.error(e.getMessage(), e);
            return new LinkedList<Element>();
        }
    }

    private String getElement(Element document, List<Namespace> namespaces, String path) throws JaxenException {
        XPathExpression<Object> xpath = XPathFactory.instance().compile(path, Filters.fpassthrough(), null, namespaces);
        List<Object> nodes = xpath.evaluate(document);
        //exactly one element expected for any field
        if (CollectionUtils.isEmpty(nodes)) {
            return "";
        } else {
            return getValue(nodes.get(0));
        }
    }


    private String getValue(Object el) {
        if (el instanceof Element) {
            return ((Element) el).getText();
        } else if (el instanceof Attribute) {
            return ((Attribute) el).getValue();
        } else if (el instanceof String) {
            return (String)el;
        } else if (el instanceof Text) {
            return ((Text) el).getText();
        } else {
            log.error("node of type: " + el.getClass());
            return "";
        }
    }

}
