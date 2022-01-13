/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.statistics;

import static java.lang.String.valueOf;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static org.apache.solr.common.params.FacetParams.FACET_LIMIT;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.solr.client.solrj.SolrServerException;
import org.dspace.content.Collection;
import org.dspace.core.Context;
import org.dspace.core.exception.SQLRuntimeException;
import org.dspace.discovery.DiscoverQuery;
import org.dspace.discovery.DiscoverResult;
import org.dspace.discovery.DiscoverResult.FacetPivotResult;
import org.dspace.discovery.SearchService;
import org.dspace.discovery.SearchServiceException;
import org.dspace.discovery.indexobject.IndexableClaimedTask;
import org.dspace.discovery.indexobject.IndexablePoolTask;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.service.EPersonService;
import org.dspace.statistics.SolrLoggerServiceImpl.StatisticsType;
import org.dspace.statistics.content.filter.StatisticsSolrDateFilter;
import org.dspace.statistics.service.SolrLoggerService;
import org.dspace.statistics.service.WorkflowStatisticsService;
import org.dspace.util.UUIDUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implementation of {@link WorkflowStatisticsService}.
 *
 * @author Luca Giamminonni (luca.giamminonni at 4science.it)
 *
 */
public class WorkflowStatisticsServiceImpl implements WorkflowStatisticsService {

    private static final String OWNER_STATISTICS_FACET_PIVOT = "actor,previousWorkflowStep";

    private static final String CURRENT_STEPS_STATISTICS_FACET_PIVOT = "step_keyword,action_keyword";

    @Autowired
    private SolrLoggerService solrLoggerService;

    @Autowired
    private SearchService searchService;

    @Autowired
    private EPersonService ePersonService;

    @Override
    public Optional<WorkflowStepStatistics> findStepStatistics(Context context, String stepName) {

        String queryFilter = composeQueryFilter() + " AND workflowStep: " + stepName;

        long count = queryTotal(queryFilter);
        if (count == 0L) {
            return Optional.empty();
        }

        return Optional.of(new WorkflowStepStatistics(stepName, count));
    }

    @Override
    public Optional<WorkflowOwnerStatistics> findOwnerStatistics(Context context, UUID ownerId) {

        String queryFilter = composeQueryFilter() + " AND actor:" + ownerId;
        FacetPivotResult[] facetPivotResults = queryWithPivotField(queryFilter, 1, OWNER_STATISTICS_FACET_PIVOT);
        if (facetPivotResults.length == 0) {
            return Optional.empty();
        }

        return convertToOwnerStatistics(context, facetPivotResults[0]);

    }

    @Override
    public List<WorkflowStepStatistics> findCurrentWorkflows(Context context, int size) {
        return searchClaimedAndPoolTasks(context, size).stream()
            .map(this::convertToStepStatistics)
            .collect(Collectors.toList());
    }

    @Override
    public List<WorkflowStepStatistics> findByDateRange(Context context, Date startDate, Date endDate,
        Collection collection, int limit) {

        String queryFilter = composeQueryFilter(startDate, endDate, collection);

        return Arrays.stream(queryWithFacetField(queryFilter, limit, "workflowStep"))
            .map(this::convertToStepStatistics)
            .filter(stepStatistics -> stepStatistics.getCount() > 0L)
            .collect(Collectors.toList());
    }

    @Override
    public List<WorkflowOwnerStatistics> findOwnersByDateRange(Context context, Date startDate, Date endDate,
        Collection collection, int limit) {

        String queryFilter = composeQueryFilter(startDate, endDate, collection);

        FacetPivotResult[] facetPivotFields = queryWithPivotField(queryFilter, limit, OWNER_STATISTICS_FACET_PIVOT);

        return Arrays.stream(facetPivotFields)
            .flatMap(pivotObjectCount -> convertToOwnerStatistics(context, pivotObjectCount).stream())
            .collect(Collectors.toList());
    }

    private List<FacetPivotResult> searchClaimedAndPoolTasks(Context context, int size) {

        DiscoverQuery query = new DiscoverQuery();
        query.addDSpaceObjectFilter(IndexableClaimedTask.TYPE);
        query.addDSpaceObjectFilter(IndexablePoolTask.TYPE);
        query.addFacetPivot(CURRENT_STEPS_STATISTICS_FACET_PIVOT);
        query.addProperty("f." + CURRENT_STEPS_STATISTICS_FACET_PIVOT.split(",")[0] + "." + FACET_LIMIT, valueOf(size));

        try {
            DiscoverResult discoverResult = searchService.search(context, query);
            return discoverResult.getFacetPivotResult(CURRENT_STEPS_STATISTICS_FACET_PIVOT);
        } catch (SearchServiceException e) {
            throw new RuntimeException(e);
        }

    }

    private String composeQueryFilter(Date startDate, Date endDate, Collection collection) {
        return Stream.of(of(composeQueryFilter()), getDateFilter(startDate, endDate), getCollectionFilter(collection))
            .flatMap(Optional::stream)
            .collect(Collectors.joining(" AND "));
    }


    private String composeQueryFilter() {
        return getWorkflowTypeFilter() + " AND previousActionRequiresUI: true";
    }

    private Optional<String> getCollectionFilter(Collection collection) {
        return Optional.ofNullable(collection)
            .map(col -> "owningColl:" + collection.getID());
    }

    private Optional<String> getDateFilter(Date startDate, Date endDate) {
        if (startDate == null && endDate == null) {
            return Optional.empty();
        }

        StatisticsSolrDateFilter dateFilter = new StatisticsSolrDateFilter();
        dateFilter.setStartDate(startDate != null ? startDate : new Date(0L));
        dateFilter.setEndDate(endDate != null ? endDate : new Date());
        return Optional.of(dateFilter.toQuery());
    }

    private FacetPivotResult[] queryWithPivotField(String filter, int limit, String pivotField) {
        try {
            return solrLoggerService.queryFacetPivotField("*:*", filter, pivotField, limit, false, null, 0);
        } catch (SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ObjectCount[] queryWithFacetField(String filter, int limit, String facetField) {
        try {
            return solrLoggerService.queryFacetField("*:*", filter, facetField, limit, false, null, 0);
        } catch (SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private long queryTotal(String queryFilter) {
        try {
            return solrLoggerService.queryTotal("*:*", queryFilter, 0).getCount();
        } catch (SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Optional<WorkflowOwnerStatistics> convertToOwnerStatistics(Context context, FacetPivotResult pivot) {

        String owner = pivot.getValue();
        long count = pivot.getCount();

        return findUserById(context, UUIDUtils.fromString(owner))
            .map(user -> new WorkflowOwnerStatistics(user, count, createActionCountMap(pivot)));

    }

    private WorkflowStepStatistics convertToStepStatistics(ObjectCount objectCount) {
        return new WorkflowStepStatistics(objectCount.getValue(), objectCount.getCount());
    }

    private WorkflowStepStatistics convertToStepStatistics(FacetPivotResult pivot) {
        return new WorkflowStepStatistics(pivot.getValue(), pivot.getCount(), createActionCountMap(pivot));
    }

    private Map<String, Long> createActionCountMap(FacetPivotResult facetPivotResult) {
        return Arrays.stream(facetPivotResult.getPivot())
            .collect(Collectors.toMap(FacetPivotResult::getValue, FacetPivotResult::getCount));
    }

    private String getWorkflowTypeFilter() {
        return "statistics_type:" + StatisticsType.WORKFLOW.text();
    }

    private Optional<EPerson> findUserById(Context context, UUID uuid) {
        try {
            return ofNullable(ePersonService.find(context, uuid));
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

}
