/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.statistics;

import static java.util.Optional.ofNullable;

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

    @Autowired
    private SolrLoggerService solrLoggerService;

    @Autowired
    private EPersonService ePersonService;

    @Override
    public Optional<WorkflowOwnerStatistics> findStepStatistics(Context context, String stepName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Optional<WorkflowOwnerStatistics> findOwnerStatistics(Context context, UUID ownerId) {

        String queryFilter = "actor:" + ownerId;
        PivotObjectCount[] queryFacetPivotFields = performQuery(queryFilter, 1, OWNER_STATISTICS_FACET_PIVOT);
        if (queryFacetPivotFields.length == 0) {
            return Optional.empty();
        }

        return convertToOwnerStatistics(context, queryFacetPivotFields[0]);

    }

    @Override
    public List<WorkflowStepStatistics> findCurrentWorkflows(Context context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<WorkflowStepStatistics> findByDateRange(Context context, Date startDate, Date endDate,
        Collection collection, int limit) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<WorkflowOwnerStatistics> findOwnersByDateRange(Context context, Date startDate, Date endDate,
        Collection collection, int limit) {

        String queryFilter = composeQueryFilter(startDate, endDate, collection);

        PivotObjectCount[] queryFacetPivotFields = performQuery(queryFilter, limit, OWNER_STATISTICS_FACET_PIVOT);

        return Arrays.stream(queryFacetPivotFields)
            .flatMap(pivotObjectCount -> convertToOwnerStatistics(context, pivotObjectCount).stream())
            .collect(Collectors.toList());
    }

    private String composeQueryFilter(Date startDate, Date endDate, Collection collection) {
        return Stream.of(getLoginTypeFilter(), getDateFilter(startDate, endDate), getCollectionFilter(collection))
            .flatMap(Optional::stream)
            .collect(Collectors.joining(" AND "));
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

    private PivotObjectCount[] performQuery(String filter, int limit, String pivotField) {
        try {
            return solrLoggerService.queryFacetPivotField("*:*", filter, pivotField, limit, false, null, 0);
        } catch (SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Optional<WorkflowOwnerStatistics> convertToOwnerStatistics(Context context, PivotObjectCount pivot) {

        String owner = pivot.getValue();
        long count = pivot.getCount();

        return findUserById(context, UUIDUtils.fromString(owner))
            .map(user -> new WorkflowOwnerStatistics(user, count, createActionCountMap(pivot)));

    }

    private Map<String, Long> createActionCountMap(PivotObjectCount pivotObjectCount) {
        return Arrays.stream(pivotObjectCount.getPivot())
            .collect(Collectors.toMap(PivotObjectCount::getValue, PivotObjectCount::getCount));
    }

    private Optional<String> getLoginTypeFilter() {
        return Optional.of("statistics_type:" + StatisticsType.WORKFLOW.text());
    }

    private Optional<EPerson> findUserById(Context context, UUID uuid) {
        try {
            return ofNullable(ePersonService.find(context, uuid));
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

}
