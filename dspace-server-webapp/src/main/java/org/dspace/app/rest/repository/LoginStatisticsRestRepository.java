/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.repository;

import static java.lang.Math.toIntExact;

import java.util.List;

import org.dspace.app.rest.Parameter;
import org.dspace.app.rest.SearchRestMethod;
import org.dspace.app.rest.exception.RepositoryMethodNotImplementedException;
import org.dspace.app.rest.model.LoginStatisticsRest;
import org.dspace.app.rest.model.StatisticsSupportRest;
import org.dspace.app.rest.model.UsageReportRest;
import org.dspace.core.Context;
import org.dspace.statistics.LoginStatistics;
import org.dspace.statistics.service.LoginStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component(StatisticsSupportRest.CATEGORY + "." + UsageReportRest.NAME)
public class LoginStatisticsRestRepository extends DSpaceRestRepository<LoginStatisticsRest, String> {

    @Autowired
    private LoginStatisticsService loginStatisticsService;

    @Override
    public LoginStatisticsRest findOne(Context context, String id) {
        throw new RepositoryMethodNotImplementedException("No implementation found; Method not allowed!", "findOne");
    }

    @Override
    public Page<LoginStatisticsRest> findAll(Context context, Pageable pageable) {
        throw new RepositoryMethodNotImplementedException("No implementation found; Method not allowed!", "findAll");
    }

    @SearchRestMethod(name = "all")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Page<LoginStatisticsRest> findLoginStatistics(Pageable pageable,
        @Parameter(value = "startDate") String startDate,
        @Parameter(value = "endDate") String endDate) {

        Context context = obtainContext();

        List<LoginStatistics> statistics = loginStatisticsService.find(context,
            startDate, endDate, toIntExact(pageable.getOffset()));

        return converter.toRestPage(statistics, pageable, statistics.size(), utils.obtainProjection());
    }

    @Override
    public Class<LoginStatisticsRest> getDomainClass() {
        return LoginStatisticsRest.class;
    }

}
