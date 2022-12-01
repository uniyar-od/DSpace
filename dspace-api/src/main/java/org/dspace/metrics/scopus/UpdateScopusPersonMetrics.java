/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.metrics.scopus;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dspace.app.metrics.CrisMetrics;
import org.dspace.app.metrics.service.CrisMetricsService;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Item;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.dspace.metrics.MetricsExternalServices;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Mykhaylo Boychuk (mykhaylo.boychuk at 4science.it)
 */
public class UpdateScopusPersonMetrics extends MetricsExternalServices {

    private static Logger log = LogManager.getLogger(UpdateScopusPersonMetrics.class);

    @Autowired
    private ScopusPersonProvider hindexProvider;

    @Autowired
    private ItemService itemService;

    @Autowired
    private CrisMetricsService crisMetricsService;

    @Override
    public List<String> getFilters() {
        return Arrays.asList("dspace.entity.type:Person", "person.identifier.scopus-author-id:*");
    }

    @Override
    public String getServiceName() {
        return "scopus-person";
    }

    @Override
    public boolean updateMetric(Context context, Item item, String param) {
        List<CrisMetricDTO> metricDTOs = null;
        String authorId = itemService.getMetadataFirstValue(item, "person", "identifier", "scopus-author-id", Item.ANY);
        if (StringUtils.isNotBlank(authorId)) {
            metricDTOs = hindexProvider.getCrisMetricDTOs(authorId, param);
        }
        if (Objects.isNull(metricDTOs)) {
            return false;
        }
        for (CrisMetricDTO metricDTO : metricDTOs) {
            try {
                if (Objects.isNull(metricDTO)) {
                    return false;
                }
                CrisMetrics scopusMetrics = crisMetricsService.findLastMetricByResourceIdAndMetricsTypes(context,
                                                               metricDTO.getMetricType(), item.getID());
                if (!Objects.isNull(scopusMetrics)) {
                    scopusMetrics.setLast(false);
                    crisMetricsService.update(context, scopusMetrics);
                }
                Optional<CrisMetrics> metricLastWeek = crisMetricsService.getCrisMetricByPeriod(context,
                                                       metricDTO.getMetricType(), item.getID(), new Date(), "week");
                Optional<CrisMetrics> metricLastMonth = crisMetricsService.getCrisMetricByPeriod(context,
                                                        metricDTO.getMetricType(), item.getID(), new Date(), "month");

                Double deltaPeriod1 = getDeltaPeriod(metricDTO, metricLastWeek);
                Double deltaPeriod2 = getDeltaPeriod(metricDTO, metricLastMonth);

                createNewMetric(context, item, metricDTO, deltaPeriod1, deltaPeriod2);
            } catch (SQLException | AuthorizeException e) {
                log.error(e.getMessage(), e);
                throw new IllegalStateException("Failed to run metric update", e);
            }
        }
        return true;
    }

    private void createNewMetric(Context context, Item item, CrisMetricDTO metricDTO,
            Double deltaPeriod1, Double deltaPeriod2) throws SQLException, AuthorizeException {
        CrisMetrics newMetric = crisMetricsService.create(context, item);
        newMetric.setMetricType(metricDTO.getMetricType());
        newMetric.setLast(true);
        newMetric.setMetricCount(metricDTO.getMetricCount());
        newMetric.setAcquisitionDate(new Date());
        newMetric.setDeltaPeriod1(deltaPeriod1);
        newMetric.setDeltaPeriod2(deltaPeriod2);
    }

    private Double getDeltaPeriod(CrisMetricDTO currentMetric, Optional<CrisMetrics> metric) {
        if (!metric.isEmpty()) {
            return currentMetric.getMetricCount() - metric.get().getMetricCount();
        }
        return null;
    }

}