/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.converter;

import org.dspace.app.rest.model.CrisLayoutSectionRest.CrisLayoutTopComponentRest;
import org.dspace.layout.CrisLayoutSectionComponent;
import org.dspace.layout.CrisLayoutTopComponent;
import org.springframework.stereotype.Component;

/**
 * Implementation of {@link CrisLayoutSectionComponentConverter} for
 * {@link CrisLayoutTopComponent}.
 * 
 * @author Luca Giamminonni (luca.giamminonni at 4science.it)
 */
@Component
public class CrisLayoutTopComponentConverter implements CrisLayoutSectionComponentConverter {

    @Override
    public boolean support(CrisLayoutSectionComponent component) {
        return component instanceof CrisLayoutTopComponent;
    }

    @Override
    public CrisLayoutTopComponentRest convert(CrisLayoutSectionComponent component) {
        CrisLayoutTopComponent topComponent = (CrisLayoutTopComponent) component;
        CrisLayoutTopComponentRest rest = new CrisLayoutTopComponentRest();
        rest.setDiscoveryConfigurationName(topComponent.getDiscoveryConfigurationName());
        rest.setOrder(topComponent.getOrder());
        rest.setSortField(topComponent.getSortField());
        rest.setStyle(component.getStyle());
        rest.setTitleKey(topComponent.getTitleKey());
        rest.setNumberOfItems(topComponent.getNumberOfItems());
        rest.setShowAsCard(topComponent.isShowAsCard());
        rest.setShowLayoutSwitch(topComponent.isShowLayoutSwitch());
        rest.setDefaultLayoutMode(topComponent.getDefaultLayoutMode().toString());
        rest.setCardColumnStyle(topComponent.getCardColumnStyle());
        rest.setCardStyle(topComponent.getCardStyle());
        rest.setItemListStyle(topComponent.getItemListStyle());
        rest.setShowAllResults(topComponent.isShowAllResults());
        return rest;
    }

}
