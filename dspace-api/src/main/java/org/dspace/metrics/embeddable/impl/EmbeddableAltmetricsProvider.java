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

public class EmbeddableAltmetricsProvider extends AbstractEmbeddableMetricProvider {

    protected String doiField;

    protected String doiDataAttr;

    protected String pmidField;

    protected String pmidDataAttr;

    protected String badgeType;

    protected String popover;

    private String details;

    private Integer minScore;

    private Boolean hideNoMentions;

    private String listDetails;

    protected String listBadgeType;

    protected String listPopOver;

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
        String doiAttr = this.calculateAttribute(item, doiField, doiDataAttr);
        String pmidAtt = this.calculateAttribute(item, pmidField, pmidDataAttr);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("data-badge-enabled", this.detailViewEnabled);
        jsonObject.addProperty("list-data-badge-enabled", this.listViewEnabled);

        jsonObject.addProperty("popover", this.popover);
        jsonObject.addProperty("badgeType", this.badgeType);
        jsonObject.addProperty("data-badge-details", this.details);
        jsonObject.addProperty("doiAttr", doiAttr);
        jsonObject.addProperty("pmidAttr", pmidAtt);
        jsonObject.addProperty("data-hide-less-than", this.minScore);
        jsonObject.addProperty("data-hide-no-mentions", this.hideNoMentions);

        jsonObject.addProperty("list-popover", this.listPopOver);
        jsonObject.addProperty("list-badgeType", this.listBadgeType);
        jsonObject.addProperty("list-data-badge-details", this.listDetails);
        jsonObject.addProperty("list-doiAttr", doiAttr);
        jsonObject.addProperty("list-pmidAttr", pmidAtt);
        jsonObject.addProperty("list-data-hide-less-than", this.minScore);
        jsonObject.addProperty("list-data-hide-no-mentions", this.hideNoMentions);

        return jsonObject.toString();
    }

    protected String calculateAttribute(Item item, String field, String attr) {
        if (field != null && attr != null) {
            List<MetadataValue> values = this.getItemService().getMetadataByMetadataString(item, field);
            if (!values.isEmpty()) {
                return Optional.ofNullable(values.get(0))
                        .map(MetadataValue::getValue).orElse("");
            }
        }
        return "";
    }

    @Override
    public String getMetricType() {
        return "altmetric";
    }

    public void setBadgeType(String badgeType) {
        this.badgeType = badgeType;
    }

    public void setPopover(String popover) {
        this.popover = popover;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public void setListDetails(String listDetails) {
        this.listDetails = listDetails;
    }

    public void setMinScore(Integer minScore) {
        this.minScore = minScore;
    }

    public void setHideNoMentions(Boolean hideNoMentions) {
        this.hideNoMentions = hideNoMentions;
    }

    public void setDoiField(String doiField) {
        this.doiField = doiField;
    }

    public void setDoiDataAttr(String doiDataAttr) {
        this.doiDataAttr = doiDataAttr;
    }

    public void setPmidField(String pmidField) {
        this.pmidField = pmidField;
    }

    public void setPmidDataAttr(String pmidDataAttr) {
        this.pmidDataAttr = pmidDataAttr;
    }

    public void setListBadgeType(String listBadgeType) {
        this.listBadgeType = listBadgeType;
    }

    public void setListPopOver(String listPopOver) {
        this.listPopOver = listPopOver;
    }
}
