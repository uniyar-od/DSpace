/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.metrics.embeddable.impl;

import java.util.List;

import com.google.gson.JsonObject;
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
    /**
     * Script to render widget for persons
     */
    protected String personPlumXScript;
    /**
     * Script to render widget for publication
     */
    protected String publicationPlumXScript;
    /**
     * Href link for publication researches
     */
    protected String publicationHref;
    /**
     * Href link for persons
     */
    protected String personHref;

    protected String dataLang;

    protected boolean dataNoName;

    protected int dataNumArtifacts;

    protected String dataWidth;

    protected boolean dataNoDescription;

    protected boolean dataNoStats;

    protected boolean dataNoThumbnail;

    protected boolean dataNoArtifacts;

    protected String dataPopup;

    protected boolean dataHideWhenEmpty;

    protected boolean dataHideUsage;

    protected boolean dataHideCaptures;

    protected boolean dataHideMentions;

    protected boolean dataHideSocialMedia;

    protected boolean dataHideCitations;

    protected boolean dataPassHiddenCategories;

    protected boolean dataDetailSamePage;

    @Autowired
    private ItemService itemService;
    String doiIdentifier;
    String orcid;

    @Override
    public boolean hasMetric(Context context, Item item, List<CrisMetrics> retrivedStoredMetrics) {
        String entityType = getEntityType(item);
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
        JsonObject innerHtml = new JsonObject();
        String entityType = getEntityType(item);
        innerHtml.addProperty("type", entityType);
        if (entityType.equals("Person")) {
            innerHtml.addProperty("src", personPlumXScript);
            innerHtml.addProperty("href", personHref + "?orcid=" + orcid);
        } else {
            innerHtml.addProperty("src", publicationPlumXScript);
            innerHtml.addProperty("href", publicationHref + "?doi=" + doiIdentifier);
        }

        innerHtml.addProperty("data-lang", this.dataLang);
        innerHtml.addProperty("data-no-name", this.dataNoName);
        innerHtml.addProperty("data-num-artifacts", this.dataNumArtifacts);
        innerHtml.addProperty("data-width", this.dataWidth);
        innerHtml.addProperty("data-no-description", this.dataNoDescription);
        innerHtml.addProperty("data-no-stats", this.dataNoStats);
        innerHtml.addProperty("data-no-thumbnail", this.dataNoThumbnail);
        innerHtml.addProperty("data-no-artifacts", this.dataNoArtifacts);
        innerHtml.addProperty("data-popup", this.dataPopup);
        innerHtml.addProperty("data-hide-when-empty", this.dataHideWhenEmpty);
        innerHtml.addProperty("data-hide-usage", this.dataHideUsage);
        innerHtml.addProperty("data-hide-captures", this.dataHideCaptures);
        innerHtml.addProperty("data-hide-mentions", this.dataHideMentions);
        innerHtml.addProperty("data-hide-socialmedia", this.dataHideSocialMedia);
        innerHtml.addProperty("data-hide-citations", this.dataHideCitations);
        innerHtml.addProperty("data-pass-hidden-categories", this.dataPassHiddenCategories);
        innerHtml.addProperty("data-detail-same-page", this.dataDetailSamePage);

        return innerHtml.toString();
    }

    @Override
    public String getMetricType() {
        return "plumX";
    }

    protected String getEntityType(Item item) {
        return getItemService().getMetadataFirstValue(item,
                "dspace", "entity", "type", Item.ANY);
    }

    protected ItemService getItemService() {
        return itemService;
    }

    public String getPersonPlumXScript() {
        return personPlumXScript;
    }

    public void setPersonPlumXScript(String personPlumXScript) {
        this.personPlumXScript = personPlumXScript;
    }

    public String getPublicationPlumXScript() {
        return publicationPlumXScript;
    }

    public void setPublicationPlumXScript(String publicationPlumXScript) {
        this.publicationPlumXScript = publicationPlumXScript;
    }

    public String getPublicationHref() {
        return publicationHref;
    }

    public void setPublicationHref(String publicationHref) {
        this.publicationHref = publicationHref;
    }

    public String getPersonHref() {
        return personHref;
    }

    public void setPersonHref(String personHref) {
        this.personHref = personHref;
    }

    public String getDataLang() {
        return dataLang;
    }

    public void setDataLang(String dataLang) {
        this.dataLang = dataLang;
    }

    public boolean isDataNoName() {
        return dataNoName;
    }

    public void setDataNoName(boolean dataNoName) {
        this.dataNoName = dataNoName;
    }

    public int getDataNumArtifacts() {
        return dataNumArtifacts;
    }

    public void setDataNumArtifacts(int dataNumArtifacts) {
        this.dataNumArtifacts = dataNumArtifacts;
    }

    public String getDataWidth() {
        return dataWidth;
    }

    public void setDataWidth(String dataWidth) {
        this.dataWidth = dataWidth;
    }

    public boolean isDataNoDescription() {
        return dataNoDescription;
    }

    public void setDataNoDescription(boolean dataNoDescription) {
        this.dataNoDescription = dataNoDescription;
    }

    public boolean isDataNoStats() {
        return dataNoStats;
    }

    public void setDataNoStats(boolean dataNoStats) {
        this.dataNoStats = dataNoStats;
    }

    public boolean isDataNoThumbnail() {
        return dataNoThumbnail;
    }

    public void setDataNoThumbnail(boolean dataNoThumbnail) {
        this.dataNoThumbnail = dataNoThumbnail;
    }

    public boolean isDataNoArtifacts() {
        return dataNoArtifacts;
    }

    public void setDataNoArtifacts(boolean dataNoArtifacts) {
        this.dataNoArtifacts = dataNoArtifacts;
    }

    public String getDataPopup() {
        return dataPopup;
    }

    public void setDataPopup(String dataPopup) {
        this.dataPopup = dataPopup;
    }

    public boolean isDataHideWhenEmpty() {
        return dataHideWhenEmpty;
    }

    public void setDataHideWhenEmpty(boolean dataHideWhenEmpty) {
        this.dataHideWhenEmpty = dataHideWhenEmpty;
    }

    public boolean isDataHideUsage() {
        return dataHideUsage;
    }

    public void setDataHideUsage(boolean dataHideUsage) {
        this.dataHideUsage = dataHideUsage;
    }

    public boolean isDataHideCaptures() {
        return dataHideCaptures;
    }

    public void setDataHideCaptures(boolean dataHideCaptures) {
        this.dataHideCaptures = dataHideCaptures;
    }

    public boolean isDataHideMentions() {
        return dataHideMentions;
    }

    public void setDataHideMentions(boolean dataHideMentions) {
        this.dataHideMentions = dataHideMentions;
    }

    public boolean isDataHideSocialMedia() {
        return dataHideSocialMedia;
    }

    public void setDataHideSocialMedia(boolean dataHideSocialMedia) {
        this.dataHideSocialMedia = dataHideSocialMedia;
    }

    public boolean isDataHideCitations() {
        return dataHideCitations;
    }

    public void setDataHideCitations(boolean dataHideCitations) {
        this.dataHideCitations = dataHideCitations;
    }

    public boolean isDataPassHiddenCategories() {
        return dataPassHiddenCategories;
    }

    public void setDataPassHiddenCategories(boolean dataPassHiddenCategories) {
        this.dataPassHiddenCategories = dataPassHiddenCategories;
    }

    public boolean isDataDetailSamePage() {
        return dataDetailSamePage;
    }

    public void setDataDetailSamePage(boolean dataDetailSamePage) {
        this.dataDetailSamePage = dataDetailSamePage;
    }

}
