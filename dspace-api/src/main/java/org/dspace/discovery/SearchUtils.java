/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.discovery;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.solr.common.SolrInputDocument;
import org.dspace.app.metrics.CrisMetrics;
import org.dspace.content.Collection;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.WorkspaceItem;
import org.dspace.discovery.configuration.DiscoveryConfiguration;
import org.dspace.discovery.configuration.DiscoveryConfigurationService;
import org.dspace.discovery.utils.DiscoverQueryBuilder;
import org.dspace.kernel.ServiceManager;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.dspace.workflow.WorkflowItem;

/**
 * Util methods used by discovery
 *
 * @author Kevin Van de Velde (kevin at atmire dot com)
 * @author Mark Diggory (markd at atmire dot com)
 * @author Ben Bosman (ben at atmire dot com)
 */
public class SearchUtils {

    public static final String AUTHORITY_SEPARATOR = "###";
    public static final String LAST_INDEXED_FIELD = "SolrIndexer.lastIndexed";
    public static final String RESOURCE_UNIQUE_ID = "search.uniqueid";
    public static final String RESOURCE_TYPE_FIELD = "search.resourcetype";
    public static final String RESOURCE_ID_FIELD = "search.resourceid";
    public static final String NAMED_RESOURCE_TYPE = "namedresourcetype";
    public static final String FILTER_SEPARATOR = "\n|||\n";

    /**
     * Cached search service
     **/
    private static SearchService searchService;

    /**
     * Default constructor
     */
    private SearchUtils() { }

    /**
     * Return an instance of the {@link SearchService}.
     */
    public static SearchService getSearchService() {
        if (searchService == null) {
            org.dspace.kernel.ServiceManager manager = DSpaceServicesFactory.getInstance().getServiceManager();
            searchService = manager.getServiceByName(SearchService.class.getName(), SearchService.class);
        }
        return searchService;
    }

    /**
     * Clear the cached {@link SearchService} instance, forcing it to be retrieved from the service manager again
     * next time {@link SearchUtils#getSearchService} is called.
     * In practice, this is only necessary for integration tests in some environments
     * where the cached version may no longer be up to date between tests.
     */
    public static void clearCachedSearchService() {
        searchService = null;
    }

    public static DiscoveryConfiguration getDiscoveryConfiguration() {
        return getDiscoveryConfiguration(null, null);
    }

    public static DiscoveryConfiguration getDiscoveryConfiguration(DSpaceObject dso) {
        return getDiscoveryConfiguration(null, dso);
    }

    /**
     * Return the discovery configuration to use in a specific scope for the king of search identified by the prefix. A
     * null prefix mean the normal query, other predefined values are workspace or workflow
     * 
     * @param prefix
     *            the namespace of the configuration to lookup if any
     * @param dso
     *            the DSpaceObject
     * @return the discovery configuration for the specified scope
     */
    public static DiscoveryConfiguration getDiscoveryConfiguration(String prefix, DSpaceObject dso) {
        if (prefix != null) {
            return getDiscoveryConfigurationByName(dso != null ? prefix + "." + dso.getHandle() : prefix);
        } else {
            return getDiscoveryConfigurationByName(dso != null ? dso.getHandle() : null);
        }
    }

    /**
     * Return the discovery configuration identified by the specified name
     * 
     * @param configurationName the configuration name assigned to the bean in the
     *                          discovery.xml
     * @return the discovery configuration
     */
    public static DiscoveryConfiguration getDiscoveryConfigurationByName(
        String configurationName) {
        DiscoveryConfigurationService configurationService = getConfigurationService();

        return configurationService.getDiscoveryConfigurationByNameOrDefault(configurationName);
    }

    public static DiscoveryConfigurationService getConfigurationService() {
        ServiceManager manager = DSpaceServicesFactory.getInstance().getServiceManager();
        return manager
            .getServiceByName(DiscoveryConfigurationService.class.getName(), DiscoveryConfigurationService.class);
    }

    public static List<String> getIgnoredMetadataFields(int type) {
        return getConfigurationService().getToIgnoreMetadataFields().get(type);
    }

    /**
     * Method that retrieves a list of all the configuration objects from the given item
     * A configuration object can be returned for each parent community/collection
     *
     * @param item the DSpace item
     * @return a list of configuration objects
     * @throws SQLException An exception that provides information on a database access error or other errors.
     */
    public static List<DiscoveryConfiguration> getAllDiscoveryConfigurations(Item item) throws SQLException {
        List<Collection> collections = item.getCollections();
        return getAllDiscoveryConfigurations(null, collections, item);
    }

    /**
     * Return all the discovery configuration applicable to the provided workspace item
     * @param witem a workspace item
     * @return a list of discovery configuration
     * @throws SQLException
     */
    public static List<DiscoveryConfiguration> getAllDiscoveryConfigurations(WorkspaceItem witem) throws SQLException {
        List<Collection> collections = new ArrayList<Collection>();
        collections.add(witem.getCollection());
        return getAllDiscoveryConfigurations("workspace", collections, witem.getItem());
    }

    /**
     * Return all the discovery configuration applicable to the provided workflow item
     * @param witem a workflow item
     * @return a list of discovery configuration
     * @throws SQLException
     */
    public static List<DiscoveryConfiguration> getAllDiscoveryConfigurations(WorkflowItem witem) throws SQLException {
        List<Collection> collections = new ArrayList<Collection>();
        collections.add(witem.getCollection());
        return getAllDiscoveryConfigurations("workflow", collections, witem.getItem());
    }

    private static List<DiscoveryConfiguration> getAllDiscoveryConfigurations(String prefix,
                                                                              List<Collection> collections, Item item)
        throws SQLException {
        Set<DiscoveryConfiguration> result = new HashSet<>();

        for (Collection collection : collections) {
            DiscoveryConfiguration configuration = getDiscoveryConfiguration(prefix, collection);
            result.add(configuration);
        }

        //Add alwaysIndex configurations
        DiscoveryConfigurationService configurationService = getConfigurationService();
        result.addAll(configurationService.getIndexAlwaysConfigurations());

        //Also add one for the default
        addConfigurationIfExists(result, prefix);

        return Arrays.asList(result.toArray(new DiscoveryConfiguration[result.size()]));
    }

    private static void addConfigurationIfExists(Set<DiscoveryConfiguration> result, String confName) {
        DiscoveryConfiguration configurationExtra = getDiscoveryConfigurationByName(confName);
        result.add(configurationExtra);
    }

    public static SolrInputDocument addMetricFieldsInSolrDoc(CrisMetrics metric, SolrInputDocument solrInDoc) {
        String type = "metric." + metric.getMetricType();
        String typeSort = type + "_sort";
        String typeId = "metric.id." + metric.getMetricType();
        String typeAcquisitionDate = "metric.acquisitionDate." + metric.getMetricType();
        String typeRemark = "metric.remark." + metric.getMetricType();
        String typeDeltaPeriod1 = "metric.deltaPeriod1." + metric.getMetricType();
        String typeDeltaPeriod2 = "metric.deltaPeriod2." + metric.getMetricType();
        String typeRank = "metric.rank." + metric.getMetricType();

        Map<String, Object> metricCountMap = Collections.singletonMap("set", metric.getMetricCount());
        Map<String, Object> acquisitionDateMap = Collections.singletonMap("set", metric.getAcquisitionDate());
        Map<String, Object> idMap = Collections.singletonMap("set", metric.getId());
        Map<String, Object> remarkMap = Collections.singletonMap("set", metric.getRemark());
        Map<String, Object> deltaPeriod1Map = Collections.singletonMap("set", metric.getDeltaPeriod1());
        Map<String, Object> deltaPeriod2Map = Collections.singletonMap("set", metric.getDeltaPeriod2());
        Map<String, Object> rankMap = Collections.singletonMap("set", metric.getRank());
        solrInDoc.addField(type, metricCountMap);
        solrInDoc.addField(typeSort, metricCountMap);
        solrInDoc.addField(typeId, idMap);
        solrInDoc.addField(typeAcquisitionDate, acquisitionDateMap);
        solrInDoc.addField(typeRemark, remarkMap);
        solrInDoc.addField(typeDeltaPeriod1, deltaPeriod1Map);
        solrInDoc.addField(typeDeltaPeriod2, deltaPeriod2Map);
        solrInDoc.addField(typeRank, rankMap);
        return solrInDoc;
    }

    public static DiscoverQueryBuilder getQueryBuilder() {
        ServiceManager manager = DSpaceServicesFactory.getInstance().getServiceManager();
        return manager
            .getServiceByName(DiscoverQueryBuilder.class.getName(), DiscoverQueryBuilder.class);
    }
}
