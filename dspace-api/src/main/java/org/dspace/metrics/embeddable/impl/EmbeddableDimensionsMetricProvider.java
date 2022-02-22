/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.metrics.embeddable.impl;

import java.util.List;
import java.util.Optional;

import com.google.gson.JsonObject;
import org.dspace.content.Item;
import org.dspace.content.MetadataValue;
import org.dspace.core.Context;

/*
* @author Jurgen Mamani
*/
public class EmbeddableDimensionsMetricProvider extends AbstractEmbeddableMetricProvider {

    protected String dataLegend;

    protected String dataStyle;

    protected boolean badgeInstalled;

    protected String doiField;

    protected String pmidField;

    @Override
    public String innerHtml(Context context, Item item) {
        String doiValue = getValueFromMetadataField(item, doiField);
        String pmidValue = getValueFromMetadataField(item, pmidField);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("data-legend", this.dataLegend);
        jsonObject.addProperty("data-style", this.dataStyle);
        jsonObject.addProperty("data-dimensions-badge-installed", this.badgeInstalled);
        jsonObject.addProperty("data-doi", doiValue);
        jsonObject.addProperty("data-pmid", pmidValue);

        return jsonObject.toString();
    }

    @Override
    public String getMetricType() {
        return "dimensions";
    }

    protected String getValueFromMetadataField(Item item, String field) {
        if (field != null) {
            List<MetadataValue> values = this.getItemService().getMetadataByMetadataString(item, field);
            if (!values.isEmpty()) {
                return Optional.ofNullable(values.get(0))
                    .map(MetadataValue::getValue).orElse("");
            }
        }

        return "";
    }

    public String getDataLegend() {
        return dataLegend;
    }

    public void setDataLegend(String dataLegend) {
        this.dataLegend = dataLegend;
    }

    public String getDataStyle() {
        return dataStyle;
    }

    public void setDataStyle(String dataStyle) {
        this.dataStyle = dataStyle;
    }

    public String getDoiField() {
        return doiField;
    }

    public void setDoiField(String doiField) {
        this.doiField = doiField;
    }

    public String getPmidField() {
        return pmidField;
    }

    public void setPmidField(String pmidField) {
        this.pmidField = pmidField;
    }

    public boolean getBadgeInstalled() {
        return badgeInstalled;
    }

    public void setBadgeInstalled(boolean badgeInstalled) {
        this.badgeInstalled = badgeInstalled;
    }
}
