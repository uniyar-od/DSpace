/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.content.authority;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.authority.factory.ItemAuthorityServiceFactory;
import org.dspace.content.authority.service.ItemAuthorityService;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.dspace.discovery.SearchService;
import org.dspace.external.factory.ExternalServiceFactory;
import org.dspace.external.provider.ExternalDataProvider;
import org.dspace.external.service.ExternalDataService;
import org.dspace.services.ConfigurationService;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.dspace.util.ItemAuthorityUtils;
import org.dspace.util.UUIDUtils;
import org.dspace.utils.DSpace;

/**
 * Sample authority to link a dspace item with another (i.e a publication with
 * the corresponding dataset or viceversa)
 *
 * @author Andrea Bollini
 * @author Giusdeppe Digilio
 * @version $Revision $
 */
public class ItemAuthority implements ChoiceAuthority, LinkableEntityAuthority {
    private static Logger log = LogManager.getLogger(ItemAuthority.class);
    final static String CHOICES_EXTERNALSOURCE_PREFIX = "choises.externalsource.";

    /** the name assigned to the specific instance by the PluginService, @see {@link NameAwarePlugin} **/
    private String authorityName;

    private DSpace dspace = new DSpace();

    protected ItemService itemService = ContentServiceFactory.getInstance().getItemService();

    private SearchService searchService = dspace.getServiceManager().getServiceByName(
        "org.dspace.discovery.SearchService", SearchService.class);

    private ItemAuthorityServiceFactory itemAuthorityServiceFactory = dspace.getServiceManager().getServiceByName(
            "itemAuthorityServiceFactory", ItemAuthorityServiceFactory.class);

    private ConfigurationService configurationService = DSpaceServicesFactory.getInstance().getConfigurationService();

    private List<CustomAuthorityFilter> customAuthorityFilters = dspace.getServiceManager()
        .getServicesByType(CustomAuthorityFilter.class);

    private ExternalDataService externalDataService = ExternalServiceFactory.getInstance().getExternalDataService();

    // map of field key to presentation type
    protected Map<String, String> externalSource = new HashMap<String, String>();

    // punt!  this is a poor implementation..
    @Override
    public Choices getBestMatch(String text, String locale) {
        return getMatches(text, 0, 2, locale);
    }

    /**
     * Match a proposed value against existent DSpace item applying an optional
     * filter query to limit the scope only to specific item types
     */
    @Override
    public Choices getMatches(String text, int start, int limit, String locale) {
        if (limit <= 0) {
            limit = 20;
        }

        SolrClient solr = searchService.getSolrSearchCore().getSolr();
        if (Objects.isNull(solr)) {
            log.error("unable to find solr instance");
            return new Choices(Choices.CF_UNSET);
        }

        String entityType = getLinkedEntityType();
        ItemAuthorityService itemAuthorityService = itemAuthorityServiceFactory.getInstance(entityType);
        String luceneQuery = itemAuthorityService.getSolrQuery(text);


        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery(luceneQuery);
        solrQuery.setStart(start);
        solrQuery.setRows(limit);
        solrQuery.addFilterQuery("search.resourcetype:" + Item.class.getSimpleName());

        if (StringUtils.isNotBlank(entityType)) {
            solrQuery.addFilterQuery("dspace.entity.type:" + entityType);
        }

        customAuthorityFilters.stream()
            .flatMap(caf -> caf.getFilterQueries(this).stream())
            .forEach(solrQuery::addFilterQuery);

        try {
            QueryResponse queryResponse = solr.query(solrQuery);
            List<Choice> choiceList = getChoiceListFromQueryResults(queryResponse.getResults());
            Choice[] results = new Choice[choiceList.size()];
            results = choiceList.toArray(results);
            long numFound = queryResponse.getResults().getNumFound();

            return new Choices(results, start, (int) numFound, Choices.CF_AMBIGUOUS,
                               numFound > (start + limit), 0);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new Choices(Choices.CF_UNSET);
        }
    }

    private List<Choice> getChoiceListFromQueryResults(SolrDocumentList results) {
        return results
        .stream()
        .map(doc ->  {
            Object fieldValue = doc.getFieldValue("dc.title");
            String title = fieldValue instanceof String ? (String) fieldValue :
                ((ArrayList<String>) fieldValue).get(0);
            Map<String, String> extras = ItemAuthorityUtils.buildExtra(getPluginInstanceName(), doc);
            return new Choice((String) doc.getFieldValue("search.resourceid"),
                title,
                title, extras);
        }).collect(Collectors.toList());
    }

    @Override
    public String getLabel(String key, String locale) {
        String title = key;
        if (key != null) {
            Context context = null;
            try {
                context = new Context();
                DSpaceObject dso = itemService.find(context, UUIDUtils.fromString(key));
                if (dso != null) {
                    title = dso.getName();
                }
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
                return key;
            }
        }
        return title;
    }

    @Override
    public String getLinkedEntityType() {
        return configurationService.getProperty("cris.ItemAuthority." + authorityName + ".entityType");
    }

    public void setPluginInstanceName(String name) {
        authorityName = name;
    }

    @Override
    public String getPluginInstanceName() {
        return authorityName;
    }

    /**
     * Return map of key to presentation
     *
     * @return
     */
    @Override
    public Map<String, String> getExternalSource() {
        // If empty, load from configuration
        if (externalSource.isEmpty()) {
            // Get all configuration keys starting with a given prefix
            List<String> propKeys = configurationService.getPropertyKeys(CHOICES_EXTERNALSOURCE_PREFIX);
            Iterator<String> keyIterator = propKeys.iterator();
            while (keyIterator.hasNext()) {
                String key = keyIterator.next();

                String metadata = key.substring(CHOICES_EXTERNALSOURCE_PREFIX.length());
                if (metadata == null) {
                    log.warn("Skipping invalid external source authority configuration property: " + key +
                        ": does not have schema.element.qualifier");
                    continue;
                }
                String sourceIdentifier = configurationService.getProperty(key);
                if (hasValidExternalSource(sourceIdentifier)) {
                    externalSource.put(metadata, sourceIdentifier);
                } else {
                    log.warn("Skipping invalid external source authority configuration property: " + sourceIdentifier +
                            " does not exist");
                    continue;
                }
            }
        }

        return externalSource;
    }

    @Override
    public Map<String, String> getExtra(String key, String locale) {

        SolrClient solr = searchService.getSolrSearchCore().getSolr();
        if (Objects.isNull(solr)) {
            log.error("unable to find solr instance");
            return new HashMap<String, String>();
        }


        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery("*:*");
        solrQuery.addFilterQuery("search.resourceid:" + key);

        customAuthorityFilters.stream()
            .flatMap(caf -> caf.getFilterQueries(this).stream())
            .forEach(solrQuery::addFilterQuery);

        try {
            QueryResponse queryResponse = solr.query(solrQuery);
            List<Choice> choiceList = getChoiceListFromQueryResults(queryResponse.getResults());
            if (choiceList.isEmpty()) {
                log.warn("No documents found for key=" + key);
                return new HashMap<String, String>();
            }

            return choiceList.iterator().next().extras;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return new HashMap<String, String>();
    }

    protected int calculateConfidence(Choice[] choices) {
        return ArrayUtils.isNotEmpty(choices) ? Choices.CF_AMBIGUOUS : Choices.CF_UNSET;
    }

    private boolean hasValidExternalSource(String sourceIdentifier) {
        if (StringUtils.isNotBlank(sourceIdentifier)) {
            ExternalDataProvider externalsource = externalDataService.getExternalDataProvider(sourceIdentifier);
            return (externalsource != null);
        }
        return false;
    }

}
