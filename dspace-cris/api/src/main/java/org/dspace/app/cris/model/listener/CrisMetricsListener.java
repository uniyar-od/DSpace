/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * https://github.com/CILEA/dspace-cris/wiki/License
 */
package org.dspace.app.cris.model.listener;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.common.SolrInputDocument;
import org.dspace.app.cris.discovery.CrisSearchService;
import org.dspace.app.cris.metrics.common.model.CrisMetrics;
import org.dspace.core.Context;

import it.cilea.osd.common.listener.NativePostUpdateEventListener;
import it.cilea.osd.common.model.Identifiable;

/**
 *  Listener to save metrics fields using Solr atomic update  
 */
public class CrisMetricsListener implements NativePostUpdateEventListener {

	@Transient
	private static Logger log = Logger.getLogger(CrisMetricsListener.class);

    private CrisSearchService searchService;
    
    private final static String placeholder = "crismetrics";
    private final static String separator = "_";

	@Override
	public <T extends Identifiable> void onPostUpdate(T entity) {
		
		Object object = entity;
		if (!(object instanceof CrisMetrics)) {
			// nothing to do
			return;
		}

		log.debug("Call onPostUpdate " + CrisMetricsListener.class);
		
		CrisMetrics metric = (CrisMetrics) object;

		String uuid = null;
		try {
			log.debug("###Work on Identifier " + metric.getResourceId());
		    double count = metric.getMetricCount();
		    uuid = metric.getUuid();
		    String metrictype = metric.getMetricType();
		    Context context = metric.getContext();
		    //maybe a safety check to not encounter an inconsistent state (it could be removed.. good luck)
		    if(context!=null) {
    			if (StringUtils.isNotBlank(uuid) && count>0) {
			        // create the document for the atomic update
	                SolrInputDocument document = new SolrInputDocument();
	                document.addField("search.uniqueid", metric.getResourceTypeId() +"-"+ metric.getResourceId());
	                
	                Map<String,Object> fieldModifier = new HashMap<>(1);
	                fieldModifier.put("set",count);
	                document.addField(placeholder+separator+metrictype, fieldModifier);  // add the map as the field value
	                
	                  
	                if (StringUtils.isNotBlank(metric.getRemark()))
	                {
	                    Map<String,Object> fieldModifierAdditional = new HashMap<>(1);
	                    fieldModifierAdditional.put("set",metric.getRemark());
	                    document.addField(metrictype+"_remark" + separator + placeholder, fieldModifierAdditional); // add the map as the field value
	                }
	                if (metric.getTimeStampInfo() != null && metric.getTimeStampInfo().getTimestampCreated() != null)
	                {
                        Map<String,Object> fieldModifierAdditional = new HashMap<>(1);
                        fieldModifierAdditional.put("set",metric.getTimeStampInfo().getTimestampCreated().getTimestamp());
	                    document.addField(metrictype+"_time" + separator + placeholder, fieldModifierAdditional); // add the map as the field value
	                }
	                if (metric.getStartDate() != null)
	                {
                        Map<String,Object> fieldModifierAdditional = new HashMap<>(1);
                        fieldModifierAdditional.put("set", metric.getStartDate());
	                    document.addField(metrictype+"_starttime" + separator + placeholder, fieldModifierAdditional); // add the map as the field value
	                }
	                if (metric.getEndDate() != null)
	                {
                        Map<String,Object> fieldModifierAdditional = new HashMap<>(1);
                        fieldModifierAdditional.put("set", metric.getEndDate());
	                    document.addField(metrictype+"_endtime" + separator + placeholder, fieldModifierAdditional); // add the map as the field value
	                }       
	                getSearchService().getSolr().add(document);
    			}
		    }
		} catch (Exception e) {
			log.error("Failed to reindex entity " + uuid);
		}
		
		log.debug("End onPostUpdate " + CrisMetricsListener.class);
	}

    public CrisSearchService getSearchService()
    {
        return searchService;
    }

    public void setSearchService(CrisSearchService crisSearchService)
    {
        this.searchService = crisSearchService;
    }
}
