/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.statistics;

import java.io.IOException;
import java.util.List;

import org.apache.solr.client.solrj.SolrServerException;
import org.dspace.core.Context;
import org.dspace.statistics.SolrLoggerServiceImpl.StatisticsType;
import org.dspace.statistics.content.filter.StatisticsSolrDateFilter;
import org.dspace.statistics.service.LoginStatisticsService;
import org.dspace.statistics.service.SolrLoggerService;
import org.springframework.beans.factory.annotation.Autowired;

public class LoginStatisticsServiceImpl implements LoginStatisticsService {

    @Autowired
    private SolrLoggerService solrLoggerService;

    @Override
    public List<LoginStatistics> find(Context context, String startDate, String endDate, Integer limit) {

        StatisticsSolrDateFilter dateFilter = new StatisticsSolrDateFilter();
        dateFilter.setStartStr(startDate);
        dateFilter.setEndStr(endDate);

        String filterQuery = dateFilter.toQuery() + " AND statistics_type:" + StatisticsType.LOGIN.text();

        ObjectCount[] count = queryFacetField(filterQuery, limit);

        for (int i = 0; i < count.length; i++) {
            // TODO: build LoginStatistics instances
        }

        return null;
    }

    private ObjectCount[] queryFacetField(String filter, Integer limit) {
        try {
            return solrLoggerService.queryFacetField("*:*", filter, "epersonid", limit, false, null, 0);
        } catch (SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }
    }

}
