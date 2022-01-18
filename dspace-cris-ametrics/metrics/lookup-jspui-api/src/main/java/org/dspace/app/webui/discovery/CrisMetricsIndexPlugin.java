/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * https://github.com/CILEA/dspace-cris/wiki/License
 */
package org.dspace.app.webui.discovery;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.common.SolrInputDocument;
import org.dspace.app.cris.discovery.CrisServiceIndexPlugin;
import org.dspace.app.cris.metrics.common.model.CrisMetrics;
import org.dspace.app.cris.metrics.common.services.MetricsPersistenceService;
import org.dspace.app.cris.model.ACrisObject;
import org.dspace.app.cris.model.jdyna.ACrisNestedObject;
import org.dspace.content.DSpaceObject;
import org.dspace.core.Context;
import org.dspace.discovery.SolrServiceIndexPlugin;
import org.dspace.discovery.configuration.DiscoverySearchFilter;

import it.cilea.osd.jdyna.model.ANestedPropertiesDefinition;
import it.cilea.osd.jdyna.model.ANestedProperty;
import it.cilea.osd.jdyna.model.ATypeNestedObject;
import it.cilea.osd.jdyna.model.PropertiesDefinition;
import it.cilea.osd.jdyna.model.Property;

/**
 * Index plugin to save the metrics info in the solr document 
 *
 */
public class CrisMetricsIndexPlugin implements CrisServiceIndexPlugin,
        SolrServiceIndexPlugin
{

    private static final String placeholder = "crismetrics";
    private static final String separator = "_";
    
    private static final Logger log = Logger
            .getLogger(CrisMetricsIndexPlugin.class);

    private MetricsPersistenceService metricsPersistenceService;

    @Override
    public void additionalIndex(Context context, DSpaceObject dso,
            SolrInputDocument document,
            Map<String, List<DiscoverySearchFilter>> searchFilters)
    {
        internal(dso.getID(), dso.getType(), document);
    }

    public void internal(Integer id, Integer type,
            SolrInputDocument document)
    {
        List<CrisMetrics> metrics = metricsPersistenceService.findAllLastMetricByResourceIDAndResourceType(id, type);
        for(CrisMetrics metric : metrics) {
            String metrictype = metric.getMetricType();  
            document.addField(placeholder+separator+metrictype, metric.getMetricCount());
            if (StringUtils.isNotBlank(metric.getRemark()))
            {
                document.addField(metrictype+"_remark" + separator + placeholder, metric.getRemark());
            }
            if (metric.getTimeStampInfo() != null && metric.getTimeStampInfo().getTimestampCreated() != null)
            {
                document.addField(metrictype+"_time" + separator + placeholder, metric.getTimeStampInfo().getTimestampCreated().getTimestamp());
            }
            if (metric.getStartDate() != null)
            {
                document.addField(metrictype+"_starttime" + separator + placeholder, metric.getStartDate());
            }
            if (metric.getEndDate() != null)
            {
                document.addField(metrictype+"_endtime" + separator + placeholder, metric.getEndDate());
            }
        }
    }

    @Override
    public <P extends Property<TP>, TP extends PropertiesDefinition, NP extends ANestedProperty<NTP>, NTP extends ANestedPropertiesDefinition, ACNO extends ACrisNestedObject<NP, NTP, P, TP>, ATNO extends ATypeNestedObject<NTP>> void additionalIndex(
            ACrisObject<P, TP, NP, NTP, ACNO, ATNO> crisObject,
            SolrInputDocument sorlDoc,
            Map<String, List<DiscoverySearchFilter>> searchFilters)
    {
        internal(crisObject.getID(), crisObject.getType(), sorlDoc);        
    }

    @Override
    public <P extends Property<TP>, TP extends PropertiesDefinition, NP extends ANestedProperty<NTP>, NTP extends ANestedPropertiesDefinition, ACNO extends ACrisNestedObject<NP, NTP, P, TP>, ATNO extends ATypeNestedObject<NTP>> void additionalIndex(
            ACNO dso, SolrInputDocument sorlDoc,
            Map<String, List<DiscoverySearchFilter>> searchFilters)
    {
        // NOOP
        
    }

    public void setMetricsPersistenceService(
            MetricsPersistenceService metricsPersistenceService)
    {
        this.metricsPersistenceService = metricsPersistenceService;
    }

}
