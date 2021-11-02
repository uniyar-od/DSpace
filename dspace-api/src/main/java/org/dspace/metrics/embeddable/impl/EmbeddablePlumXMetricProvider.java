/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.metrics.embeddable.impl;

import java.util.List;

import org.dspace.app.metrics.CrisMetrics;
import org.dspace.content.Item;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;
/**
 * Provider class for plumX metric widget
 *
 * @author Alba Aliu (alba.aliu at atis.al)
 */
public class EmbeddablePlumXMetricProvider extends AbstractEmbeddableMetricProvider {
    private static final String PERSON_TEMPLATE =
            "<a href = '{{refUrl}}' class = 'plumx-person'" +
                    " data-site='plum' data-num-artifacts='5'" + ">" +
                    "</a>" +
                    "<script type = 'text/javascript' src='//cdn.plu.mx/widget-person.js'></script>";
    private static final String PUBLICATION_TEMPLATE =
            "<a href ='{{refUrl}}' class = 'plumx-plum-print-popup'" + "></a>" +
                    "<script type = 'text/javascript' src= '//cdn.plu.mx/widget-popup.js'></script>";
    private static final String plumXSiteUrlPlumXSiteUrlGeneral = "https://plu.mx/plum/a/";
    private static final String plumXSiteUrlPlumXSiteUrl = "https://plu.mx/plum/u/";
    @Autowired
    private ItemService itemService;
    String doiIdentifier;
    String orcid;
    @Override
    public boolean hasMetric(Context context, Item item, List<CrisMetrics> retrivedStoredMetrics) {
        String entityType =  getEntityType(item);
        if (entityType != null) {
            if (entityType.equals("Person")) {
                // if it is of type person use orcid
                orcid = getItemService()
                        .getMetadataFirstValue(item, "person", "identifier", "orcid", Item.ANY);
                if (orcid != null) {
                    return true;
                } else {
                    return false;
                }
            } else {
                // if it is of type publication use doi
                if (entityType.equals("Publication")) {
                    doiIdentifier = getItemService()
                            .getMetadataFirstValue(item, "dc", "identifier", "doi", Item.ANY);
                    if (doiIdentifier != null) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        } else {
            return false;
        }
        return false;
    }
    @Override
    public String innerHtml(Context context, Item item) {
        String entityType = getEntityType(item);
        if (entityType.equals("Person")) {
            return getTemplate(false).replace("{{refUrl}}", plumXSiteUrlPlumXSiteUrl + "?orcid=" + orcid);
        } else {
            return getTemplate(true).replace("{{refUrl}}", plumXSiteUrlPlumXSiteUrlGeneral + "?doi=" + doiIdentifier);
        }
    }
    @Override
    public String getMetricType() {
        return "plumX";
    }
    protected String getTemplate(boolean general) {
        if (general) {
            return PUBLICATION_TEMPLATE;
        } else {
            return PERSON_TEMPLATE;
        }
    }
    protected String getEntityType(Item item) {
        return getItemService().getMetadataFirstValue(item,
                "dspace", "entity", "type", Item.ANY);
    }
    protected ItemService getItemService() {
        return itemService;
    }
}
