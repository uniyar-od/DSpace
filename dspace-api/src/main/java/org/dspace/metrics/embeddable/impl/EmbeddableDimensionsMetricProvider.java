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

    protected String doiField;

    protected String pmidField;

    protected String listDataLegend;

    protected String listDataStyle;

    protected boolean listBadgeInstalled;

    protected boolean hideZeroCitations;

    private boolean detailViewEnabled;

    private boolean listViewEnabled;

    @Override
    public void setEnabled(boolean enabled) {
        log.error("The enabled property is not used by " + this.getClass().getName()
                + " please rely on the detail and list view enabled properties instead");
    }

    @Override
    public boolean isEnabled() {
        return this.detailViewEnabled || this.listViewEnabled;
    }

    public void setDetailViewEnabled(boolean detailViewEnabled) {
        this.detailViewEnabled = detailViewEnabled;
    }

    public void setListViewEnabled(boolean listViewEnabled) {
        this.listViewEnabled = listViewEnabled;
    }

    @Override
    public String innerHtml(Context context, Item item) {
        String doiValue = getValueFromMetadataField(item, doiField);
        String pmidValue = getValueFromMetadataField(item, pmidField);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("data-badge-enabled", this.detailViewEnabled);
        jsonObject.addProperty("list-data-badge-enabled", this.listViewEnabled);

        jsonObject.addProperty("data-legend", this.dataLegend);
        jsonObject.addProperty("data-style", this.dataStyle);
        jsonObject.addProperty("data-doi", doiValue);
        jsonObject.addProperty("data-pmid", pmidValue);
        jsonObject.addProperty("data-hide-zero-citations", this.hideZeroCitations);

        jsonObject.addProperty("list-data-legend", this.listDataLegend);
        jsonObject.addProperty("list-data-style", this.listDataStyle);
        jsonObject.addProperty("list-data-doi", doiValue);
        jsonObject.addProperty("list-data-pmid", pmidValue);
        jsonObject.addProperty("list-data-hide-zero-citations", this.hideZeroCitations);

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

    public void setDataLegend(String dataLegend) {
        this.dataLegend = dataLegend;
    }

    public void setDataStyle(String dataStyle) {
        this.dataStyle = dataStyle;
    }

    public void setDoiField(String doiField) {
        this.doiField = doiField;
    }

    public void setPmidField(String pmidField) {
        this.pmidField = pmidField;
    }

    public void setListDataLegend(String listDataLegend) {
        this.listDataLegend = listDataLegend;
    }

    public void setListDataStyle(String listDataStyle) {
        this.listDataStyle = listDataStyle;
    }

    public void setListBadgeInstalled(boolean listBadgeInstalled) {
        this.listBadgeInstalled = listBadgeInstalled;
    }

    public void setHideZeroCitations(boolean hideZeroCitations) {
        this.hideZeroCitations = hideZeroCitations;
    }
}
