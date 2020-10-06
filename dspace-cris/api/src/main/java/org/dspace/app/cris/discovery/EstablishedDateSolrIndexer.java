/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * https://github.com/CILEA/dspace-cris/wiki/License
 */
package org.dspace.app.cris.discovery;

import it.cilea.osd.jdyna.model.ANestedPropertiesDefinition;
import it.cilea.osd.jdyna.model.ANestedProperty;
import it.cilea.osd.jdyna.model.ATypeNestedObject;
import it.cilea.osd.jdyna.model.PropertiesDefinition;
import it.cilea.osd.jdyna.model.Property;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.SolrInputDocument;
import org.dspace.app.cris.model.ACrisObject;
import org.dspace.app.cris.model.CrisConstants;
import org.dspace.app.cris.model.ResearchObject;
import org.dspace.app.cris.model.jdyna.ACrisNestedObject;
import org.dspace.discovery.configuration.DiscoverySearchFilter;
import org.dspace.util.MultiFormatDateParser;

public class EstablishedDateSolrIndexer implements CrisServiceIndexPlugin
{
    
    @Override
    public <P extends Property<TP>, TP extends PropertiesDefinition, NP extends ANestedProperty<NTP>, NTP extends ANestedPropertiesDefinition, ACNO extends ACrisNestedObject<NP, NTP, P, TP>, ATNO extends ATypeNestedObject<NTP>> void additionalIndex(
            ACrisObject<P, TP, NP, NTP, ACNO, ATNO> crisObject,
            SolrInputDocument document,
            Map<String, List<DiscoverySearchFilter>> searchFilters)
    {

        String result = "";
        Integer type = crisObject.getType();
        if (type > CrisConstants.CRIS_DYNAMIC_TYPE_ID_START)
        {
            if("crisdris".equals(crisObject.getTypeText())) {
                result = crisObject.getMetadata("drisstartdate");
                Date date = isValidDate(result);
                if (date != null)
                {
                    document.addField("drisdateestablished_dt", date);
                }
            }
        }
    }

    @Override
    public <P extends Property<TP>, TP extends PropertiesDefinition, NP extends ANestedProperty<NTP>, NTP extends ANestedPropertiesDefinition, ACNO extends ACrisNestedObject<NP, NTP, P, TP>, ATNO extends ATypeNestedObject<NTP>> void additionalIndex(
            ACNO dso, SolrInputDocument sorlDoc,
            Map<String, List<DiscoverySearchFilter>> searchFilters)
    {
        // FIXME NOT SUPPORTED OPERATION
    }

    private Date isValidDate(String value)
    {
        if (StringUtils.isNotBlank(value))
        {
            return MultiFormatDateParser.parse(value);
        }
        return null;
    }
}
