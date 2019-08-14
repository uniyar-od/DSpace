/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.cris.discovery;

import it.cilea.osd.jdyna.model.ANestedPropertiesDefinition;
import it.cilea.osd.jdyna.model.ANestedProperty;
import it.cilea.osd.jdyna.model.ATypeNestedObject;
import it.cilea.osd.jdyna.model.PropertiesDefinition;
import it.cilea.osd.jdyna.model.Property;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.SolrInputDocument;
import org.dspace.app.cris.model.ACrisObject;
import org.dspace.app.cris.model.ResearcherPage;
import org.dspace.app.cris.model.jdyna.ACrisNestedObject;
import org.dspace.core.ConfigurationManager;
import org.dspace.discovery.SolrServiceImpl;
import org.dspace.discovery.configuration.DiscoverySearchFilter;
import org.dspace.services.ConfigurationService;

public class CrisRPStatusSolrIndexPlugin implements CrisServiceIndexPlugin {

    private String fieldName = "staffStatus";
    private String separator;
    private ConfigurationService configurationService;
    
    private void init() {
        if(StringUtils.isBlank(separator)) {
            
            separator = configurationService
                    .getProperty("discovery.solr.facets.split.char");
            if (separator == null)
            {
                separator = SolrServiceImpl.FILTER_SEPARATOR;
            }
        }
    }
    
    @Override
    public <P extends Property<TP>, TP extends PropertiesDefinition, NP extends ANestedProperty<NTP>, NTP extends ANestedPropertiesDefinition, ACNO extends ACrisNestedObject<NP, NTP, P, TP>, ATNO extends ATypeNestedObject<NTP>> void additionalIndex(
            ACrisObject<P, TP, NP, NTP, ACNO, ATNO> crisObject,
            SolrInputDocument document, Map<String, List<DiscoverySearchFilter>> searchFilters) {
        init();
        if (crisObject instanceof ResearcherPage) {
            String status = ConfigurationManager.getProperty("cris", "researcher.cris.rp.ref.display.strategy.metadata.icon");
            if (StringUtils.isBlank(status) || StringUtils.isBlank(crisObject.getMetadata(status))) {
                document.addField(fieldName, "undefined");
                document.addField(fieldName + "_keyword", "undefined");
                document.addField(fieldName + "_keyword", "en_undefined" + SolrServiceImpl.AUTHORITY_SEPARATOR + "undefined");
                document.addField(fieldName + "_ac", "undefined" +separator+ "undefined");
                document.addField(fieldName + "_ac", "en_undefined" +separator+ "undefined");
                document.addField(fieldName + "_acid", "en_undefined"+separator+"undefined" + SolrServiceImpl.AUTHORITY_SEPARATOR + "undefined");
                document.addField(fieldName + "_filter", "undefined"+separator+"undefined");
                document.addField(fieldName + "_filter", "en_undefined"+separator+"undefined" + SolrServiceImpl.AUTHORITY_SEPARATOR + "undefined");
                document.addField(fieldName + "_authority", "undefined");
            }
        }

    }

    @Override
    public <P extends Property<TP>, TP extends PropertiesDefinition, NP extends ANestedProperty<NTP>, NTP extends ANestedPropertiesDefinition, ACNO extends ACrisNestedObject<NP, NTP, P, TP>, ATNO extends ATypeNestedObject<NTP>> void additionalIndex(
            ACNO dso, SolrInputDocument sorlDoc, Map<String, List<DiscoverySearchFilter>> searchFilters) {
        // FIXME NOT SUPPORTED OPERATION
    }

    public void setFieldName(String fieldName)
    {
        this.fieldName = fieldName;
    }

    public String getSeparator()
    {
        return separator;
    }

    public void setSeparator(String separator)
    {
        this.separator = separator;
    }

    public ConfigurationService getConfigurationService()
    {
        return configurationService;
    }

    public void setConfigurationService(ConfigurationService configurationService)
    {
        this.configurationService = configurationService;
    }

}

