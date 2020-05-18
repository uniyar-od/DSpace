/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.builder;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dspace.content.EntityType;
import org.dspace.content.MetadataField;
import org.dspace.core.Context;
import org.dspace.layout.CrisLayoutBox;
import org.dspace.layout.CrisLayoutTab;
import org.dspace.layout.LayoutSecurity;
import org.dspace.layout.service.CrisLayoutTabService;

public class CrisLayoutTabBuilder extends AbstractBuilder<CrisLayoutTab, CrisLayoutTabService> {

    private static final Logger log = Logger.getLogger(CrisLayoutTabBuilder.class);

    private CrisLayoutTab tab;

    private CrisLayoutTabBuilder(Context context) {
        super(context);
    }

    @Override
    protected CrisLayoutTabService getService() {
        return crisLayoutTabService;
    }

    @Override
    public void cleanup() throws Exception {
        delete(tab);
    }

    public static CrisLayoutTabBuilder createTab(Context context, EntityType eType, Integer priority) {
        CrisLayoutTabBuilder builder = new CrisLayoutTabBuilder(context);
        return builder.create(context, eType, priority);
    }

    private CrisLayoutTabBuilder create(Context context, EntityType eType, Integer priority) {
        try {
            this.context = context;
            this.tab = getService().create(context, eType, priority);
        } catch (Exception e) {
            log.error("Error in CrisLayoutTabBuilder.create(..), error: ", e);
        }
        return this;
    }

    @Override
    public CrisLayoutTab build() {
        try {
            getService().update(context, tab);
            context.dispatchEvents();

            indexingService.commit();
        } catch (Exception e) {
            log.error("Error in CrisLayoutTabBuilder.build(), error: ", e);
        }
        return tab;
    }

    @Override
    public void delete(CrisLayoutTab tab) throws Exception {
        try (Context c = new Context()) {
            c.turnOffAuthorisationSystem();
            CrisLayoutTab attachedTab = c.reloadEntity(tab);
            if (attachedTab != null) {
                getService().delete(c, attachedTab);
            }
            c.complete();
        }

        indexingService.commit();
    }

    public CrisLayoutTabBuilder withShortName(String shortName) {
        this.tab.setShortName(shortName);
        return this;
    }

    public CrisLayoutTabBuilder withHeader(String header) {
        this.tab.setHeader(header);
        return this;
    }

    public CrisLayoutTabBuilder withSecurity(LayoutSecurity security) {
        this.tab.setSecurity(security);
        return this;
    }

    public CrisLayoutTabBuilder withBoxes(Set<CrisLayoutBox> boxes) {
        this.tab.setBoxes(boxes);
        return this;
    }

    public CrisLayoutTabBuilder addBox(CrisLayoutBox box) {
        if (this.tab.getBoxes() == null) {
            this.tab.setBoxes(new HashSet<>());
        }
        this.tab.getBoxes().add(box);
        return this;
    }

    public CrisLayoutTabBuilder withMetadatasecurity(Set<MetadataField> metadataFields) {
        this.tab.setMetadataSecurityFields(metadataFields);
        return this;
    }

    public CrisLayoutTabBuilder addMetadatasecurity(MetadataField metadataField) {
        if (this.tab.getMetadataSecurityFields() == null) {
            this.tab.setMetadataSecurityFields(new HashSet<>());
        }
        this.tab.getMetadataSecurityFields().add(metadataField);
        return this;
    }
}
