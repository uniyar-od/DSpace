/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.statistics.service;

import java.util.List;

import org.dspace.core.Context;
import org.dspace.statistics.LoginStatistics;

public interface LoginStatisticsService {

    List<LoginStatistics> find(Context context, String startDate, String endDate, Integer limit);
}
