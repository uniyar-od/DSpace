/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.importer.external.cinii;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import javax.el.MethodNotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.http.HttpException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dspace.content.Item;
import org.dspace.importer.external.datamodel.ImportRecord;
import org.dspace.importer.external.datamodel.Query;
import org.dspace.importer.external.exception.MetadataSourceException;
import org.dspace.importer.external.metadatamapping.MetadatumDTO;
import org.dspace.importer.external.service.AbstractImportMetadataSourceService;
import org.dspace.importer.external.service.components.QuerySource;
import org.dspace.services.ConfigurationService;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class CiniiImportMetadataSourceServiceImpl
    extends AbstractImportMetadataSourceService<Element> implements QuerySource {

    private final static Logger log = LogManager.getLogger();

    Client client;

    @Autowired
    private ConfigurationService configurationService;

    @Override
    public String getImportSource() {
        return "cinii";
    }

    @Override
    public void init() throws Exception {
        this.client = ClientBuilder.newClient();
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
            List<ImportRecord> records = new LinkedList<ImportRecord>();
            Integer count = query.getParameterAsClass("count", Integer.class);
            Integer start = query.getParameterAsClass("start", Integer.class);
            String queryString = query.getParameterAsClass("query", String.class);
            String appId = configurationService.getProperty("submission.lookup.cinii.appid");
            List<String> ids = getCiniiIds(appId, count, null, null, null, start, queryString);
            if (ids != null && ids.size() > 0) {
                for (String id : ids) {
                    List<ImportRecord> tmp = search(id, appId);
                    if (tmp != null) {
                        MetadatumDTO metadatumDto = new MetadatumDTO();
                        metadatumDto.setSchema("dc");
                        metadatumDto.setElement("identifier");
                        metadatumDto.setQualifier("other");
                        metadatumDto.setValue(id);
                        for (ImportRecord ir : tmp) {
                            ir.addValue(metadatumDto);
                        }
                        records.addAll(tmp);
                    }
                }
            }
            return records;
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
            String appId = configurationService.getProperty("submission.lookup.cinii.appid");
            String id = query.getParameterAsClass("id", String.class);
            List<ImportRecord> importRecord = search(id, appId);
            MetadatumDTO metadatumDto = new MetadatumDTO();
            metadatumDto.setSchema("dc");
            metadatumDto.setElement("identifier");
            metadatumDto.setQualifier("other");
            metadatumDto.setValue(id);
            for (ImportRecord ir : importRecord) {
                ir.addValue(metadatumDto);
            }
            return importRecord;
        }
    }

    private class FindMatchingRecordCallable implements Callable<List<ImportRecord>> {

        private Query query;

        private FindMatchingRecordCallable(Query q) {
            query = q;
        }

        @Override
        public List<ImportRecord> call() throws Exception {
            List<ImportRecord> records = new LinkedList<ImportRecord>();
            String title = query.getParameterAsClass("title", String.class);
            String author = query.getParameterAsClass("author", String.class);
            Integer year = query.getParameterAsClass("year", Integer.class);
            Integer maxResult = query.getParameterAsClass("maxResult", Integer.class);
            Integer start = query.getParameterAsClass("start", Integer.class);
            String appId = configurationService.getProperty("submission.lookup.cinii.appid");
            List<String> ids = getCiniiIds(appId, maxResult, author, title, year, start, null);
            if (ids != null && ids.size() > 0) {
                for (String id : ids) {
                    List<ImportRecord> tmp = search(id, appId);
                    if (tmp != null) {
                        MetadatumDTO metadatumDto = new MetadatumDTO();
                        metadatumDto.setSchema("dc");
                        metadatumDto.setElement("identifier");
                        metadatumDto.setQualifier("other");
                        metadatumDto.setValue(id);
                        for (ImportRecord ir : tmp) {
                            ir.addValue(metadatumDto);
                        }
                        records.addAll(tmp);
                    }
                }
            }
            return records;
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
            String appId = configurationService.getProperty("submission.lookup.cinii.appid");
            String queryString = query.getParameterAsClass("query", String.class);
            return countCiniiElement(appId, null, null, null, null, null, queryString);
        }
    }


    /**
     * Get metadata by searching CiNii RDF API with CiNii NAID
     *
     * @param id    CiNii NAID to search by
     * @param appId registered application identifier for the API
     * @return record metadata
     * @throws IOException   A general class of exceptions produced by failed or interrupted I/O operations.
     * @throws HttpException Represents a XML/HTTP fault and provides access to the HTTP status code.
     */
    protected List<ImportRecord> search(String id, String appId)
        throws IOException, HttpException {
        List<ImportRecord> records = new LinkedList<ImportRecord>();
        WebTarget webTarget = client.target("https://ci.nii.ac.jp/naid/" + id + ".rdf?appid=" + appId);
        Invocation.Builder invocationBuilder = webTarget.request();
        Response response = invocationBuilder.get();
        String responseString = response.readEntity(String.class);
        System.out.println(responseString);
        if (response.getStatus() != 200) {
            return null;
        }
        List<Element> omElements = splitToRecords(responseString);
        for (Element record : omElements) {
            records.add(transformSourceRecords(record));
        }
        return records;
    }

    private List<Element> splitToRecords(String recordsSrc) {
        try {
            SAXBuilder saxBuilder = new SAXBuilder();
            Document document = saxBuilder.build(new StringReader(recordsSrc));
            Element root = document.getRootElement();
            List<Namespace> namespaces = Arrays
                .asList(Namespace.getNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#"));
            XPathExpression<Element> xpath = XPathFactory.instance().compile("rdf:Description", Filters.element(),
                null, namespaces);
            Element record = xpath.evaluateFirst(root);
            return Arrays.asList(record);
        } catch (JDOMException | IOException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private List<String> getCiniiIds(String appId, Integer maxResult, String author, String title,
        Integer year, Integer start, String query) {

        try {
            List<String> ids = new ArrayList<>();
            WebTarget webTarget = client.target("https://ci.nii.ac.jp/opensearch/search");
            webTarget = webTarget.queryParam("format", "rss");
            webTarget = webTarget.queryParam("appid", appId);
            if (maxResult != null && maxResult != 0) {
                webTarget = webTarget.queryParam("count", maxResult);
            }
            if (start != null) {
                webTarget = webTarget.queryParam("start", start);
            }
            if (title != null) {
                webTarget = webTarget.queryParam("title", title);
            }
            if (author != null) {
                webTarget = webTarget.queryParam("author", author);
            }
            if (query != null) {
                webTarget = webTarget.queryParam("q", query);
            }
            if (year != null && year != -1 && year != 0) {
                webTarget = webTarget.queryParam("year_from", String.valueOf(year));
                webTarget = webTarget.queryParam("year_to", String.valueOf(year));
            }
            Invocation.Builder invocationBuilder = webTarget.request();
            Response response = invocationBuilder.get();
            String responseString = response.readEntity(String.class);
            System.out.println(responseString);
            if (response.getStatus() != 200) {
                return null;
            }

            int url_len = "http://ci.nii.ac.jp/naid/".length();
            SAXBuilder saxBuilder = new SAXBuilder();
            Document document = saxBuilder.build(new StringReader(responseString));
            Element root = document.getRootElement();
            List<Namespace> namespaces = Arrays.asList(
                Namespace.getNamespace("ns", "http://purl.org/rss/1.0/"),
                Namespace.getNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#"));
            XPathExpression<Attribute> xpath = XPathFactory.instance().compile("//ns:item/@rdf:about",
                Filters.attribute(), null, namespaces);
            List<Attribute> recordsList = xpath.evaluate(root);
            for (Attribute item : recordsList) {
                String value = item.getValue();
                if (value.length() > url_len) {
                    ids.add(value.substring(url_len + 1));
                }
            }
            return ids;
        } catch (JDOMException | IOException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }

    }

    private Integer countCiniiElement(String appId, Integer maxResult, String author, String title,
            Integer year, Integer start, String query) {
        try {
            List<String> ids = new ArrayList<>();
            WebTarget webTarget = client.target("https://ci.nii.ac.jp/opensearch/search");
            webTarget = webTarget.queryParam("format", "rss");
            webTarget = webTarget.queryParam("appid", appId);
            if (maxResult != null && maxResult != 0) {
                webTarget = webTarget.queryParam("count", maxResult);
            }
            if (start != null) {
                webTarget = webTarget.queryParam("start", start);
            }
            if (title != null) {
                webTarget = webTarget.queryParam("title", title);
            }
            if (author != null) {
                webTarget = webTarget.queryParam("author", author);
            }
            if (query != null) {
                webTarget = webTarget.queryParam("q", query);
            }
            if (year != null && year != -1 && year != 0) {
                webTarget = webTarget.queryParam("year_from", String.valueOf(year));
                webTarget = webTarget.queryParam("year_to", String.valueOf(year));
            }
            Invocation.Builder invocationBuilder = webTarget.request();
            Response response = invocationBuilder.get();
            String responseString = response.readEntity(String.class);
            System.out.println(responseString);
            if (response.getStatus() != 200) {
                return null;
            }

            SAXBuilder saxBuilder = new SAXBuilder();
            Document document = saxBuilder.build(new StringReader(responseString));
            Element root = document.getRootElement();
            List<Namespace> namespaces = Arrays
                .asList(Namespace.getNamespace("opensearch", "http://a9.com/-/spec/opensearch/1.1/"));
            XPathExpression<Element> xpath = XPathFactory.instance().compile("//opensearch:totalResults",
                Filters.element(), null, namespaces);
            List<Element> nodes = xpath.evaluate(root);
            if (nodes != null && !nodes.isEmpty()) {
                return Integer.parseInt(((Element) nodes.get(0)).getText());
            }
            return 0;
        } catch (JDOMException | IOException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }

    }

}
