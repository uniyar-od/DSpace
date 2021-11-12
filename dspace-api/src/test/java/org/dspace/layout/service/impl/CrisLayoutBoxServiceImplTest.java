/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.layout.service.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.dspace.app.metrics.CrisMetrics;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.content.MetadataField;
import org.dspace.content.MetadataValue;
import org.dspace.core.Context;
import org.dspace.discovery.configuration.DiscoveryConfigurationUtilsService;
import org.dspace.layout.CrisLayoutBox;
import org.dspace.layout.CrisLayoutField;
import org.dspace.layout.CrisLayoutMetric2Box;
import org.dspace.layout.dao.CrisLayoutBoxDAO;
import org.dspace.metrics.CrisItemMetricsAuthorizationService;
import org.dspace.metrics.CrisItemMetricsService;
import org.dspace.metrics.embeddable.model.EmbeddableCrisMetrics;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Unit tests for CrisLayoutBoxServiceImpl, so far only findByItem method is tested.
 *
 * @author Corrado Lombardi (corrado.lombardi at 4science.it)
 */

@RunWith(MockitoJUnitRunner.class)
public class CrisLayoutBoxServiceImplTest {

    @InjectMocks
    private CrisLayoutBoxServiceImpl crisLayoutBoxService;

    @Mock
    private Context context;
    @Mock
    private CrisLayoutBoxDAO dao;
    @Mock
    private AuthorizeService authorizeService;
    @Mock
    private CrisItemMetricsAuthorizationService crisItemMetricsAuthorizationService;
    @Mock
    private CrisItemMetricsService crisItemMetricsService;
    @Mock
    private DiscoveryConfigurationUtilsService searchConfigurationUtilsService;

    @Test
    public void hasMetricsBoxContent() {

        when(crisItemMetricsAuthorizationService.isAuthorized(any(), any(UUID.class))).thenReturn(true);

        // should return false when the box has no metrics associated
        CrisLayoutBox boxWithoutMetrics = crisLayoutMetricBox();
        assertFalse(crisLayoutBoxService.hasMetricsBoxContent(context, boxWithoutMetrics, UUID.randomUUID()));

        // should return true when the box has at least one embeddable associated (stored mocked to empty)
        CrisLayoutBox boxMetric1 = crisLayoutMetricBox("metric1");
        mockStoredCrisMetrics();
        mockEmbeddableCrisMetrics("metric1");
        assertTrue(crisLayoutBoxService.hasMetricsBoxContent(context, boxMetric1, UUID.randomUUID()));

        // should return true when the box has at least one stored associated (embeded mocked to empty)
        mockStoredCrisMetrics("metric1");
        mockEmbeddableCrisMetrics();
        assertTrue(crisLayoutBoxService.hasMetricsBoxContent(context, boxMetric1, UUID.randomUUID()));

        // shuld return false when the box has embedded but not associated (stored mocked to empty)
        mockStoredCrisMetrics();
        mockEmbeddableCrisMetrics("metric2");
        assertFalse(crisLayoutBoxService.hasMetricsBoxContent(context, boxMetric1, UUID.randomUUID()));

        // shuld return false when the box has stored but not associated (embedded mocked to empty)
        mockStoredCrisMetrics("metric2");
        mockEmbeddableCrisMetrics();
        assertFalse(crisLayoutBoxService.hasMetricsBoxContent(context, boxMetric1, UUID.randomUUID()));

    }

    @Test
    public void hasMetricsBoxContentNotAuthorized() {

        // should return false if there is content but context has not an authenticated user
        when(crisItemMetricsAuthorizationService.isAuthorized(any(), any(UUID.class))).thenReturn(false);
        CrisLayoutBox boxMetric1 = crisLayoutMetricBox("metric1");
        mockStoredCrisMetrics();
        mockEmbeddableCrisMetrics("metric1");

        assertFalse(crisLayoutBoxService.hasMetricsBoxContent(context, boxMetric1, UUID.randomUUID()));
    }

    private CrisLayoutBox crisLayoutBox(String shortname, MetadataField metadataField) {
        return crisLayoutBox(shortname, metadataField, null);
    }

    private CrisLayoutBox crisLayoutBox(String shortname, MetadataField metadataField, String boxType) {
        CrisLayoutBox o = new CrisLayoutBox();
        o.addLayoutField(crisLayoutField(metadataField));
        o.setShortname(shortname);
        o.setType(boxType);
        return o;
    }

    private MetadataValue metadataValue(MetadataField field) {
        MetadataValue metadataValue = mock(MetadataValue.class);
        when(metadataValue.getMetadataField()).thenReturn(field);
        return metadataValue;
    }

    private CrisLayoutField crisLayoutField(MetadataField metadataField) {
        CrisLayoutField crisLayoutField = new CrisLayoutField();
        crisLayoutField.setMetadataField(metadataField);
        return crisLayoutField;
    }

    private CrisLayoutBox crisLayoutMetricBox(String ...metricTypes) {
        CrisLayoutBox crisLayoutMetricBox = mock(CrisLayoutBox.class);
        List<CrisLayoutMetric2Box> metric2boxList = Arrays.stream(metricTypes).map(mt -> {
            CrisLayoutMetric2Box metric2box = mock(CrisLayoutMetric2Box.class);
            when(metric2box.getType()).thenReturn(mt);
            return metric2box;
        }).collect(Collectors.toList());
        when(crisLayoutMetricBox.getMetric2box()).thenReturn(metric2boxList);
        return crisLayoutMetricBox;
    }

    private List<EmbeddableCrisMetrics> mockEmbeddableCrisMetrics(String ...metricTypes) {
        List<EmbeddableCrisMetrics> metrics = Arrays.stream(metricTypes).map(mt -> {
            EmbeddableCrisMetrics metric = mock(EmbeddableCrisMetrics.class);
            when(metric.getMetricType()).thenReturn(mt);
            return metric;
        }).collect(Collectors.toList());
        when(crisItemMetricsService.getEmbeddableMetrics(any(), any(), any())).thenReturn(metrics);
        return metrics;
    }

    private List<CrisMetrics> mockStoredCrisMetrics(String ...metricTypes) {
        List<CrisMetrics> metrics = Arrays.stream(metricTypes).map(mt -> {
            CrisMetrics metric = mock(CrisMetrics.class);
            when(metric.getMetricType()).thenReturn(mt);
            return metric;
        }).collect(Collectors.toList());
        when(crisItemMetricsService.getStoredMetrics(any(), any())).thenReturn(metrics);
        return metrics;

    }


}
