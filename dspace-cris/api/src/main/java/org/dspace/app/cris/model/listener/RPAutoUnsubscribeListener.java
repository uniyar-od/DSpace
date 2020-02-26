/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.cris.model.listener;

import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.app.cris.model.ResearcherPage;
import org.dspace.app.cris.service.CrisSubscribeService;
import org.dspace.app.cris.util.Researcher;

import it.cilea.osd.common.listener.NativePostDeleteEventListener;

public class RPAutoUnsubscribeListener implements NativePostDeleteEventListener {

    @Transient
    private static Logger log = Logger.getLogger(RPAutoUnsubscribeListener.class);

    private CrisSubscribeService crisSubscribeService;

    @Override
    public <P> void onPostDelete(P entity) {
        Object object = entity;
        if (!(object instanceof ResearcherPage)) {
            // nothing to do
            return;
        }

        log.debug("Call onPostDelete " + RPAutoUnsubscribeListener.class);

        ResearcherPage crisObj = (ResearcherPage) object;

        try {
            if (StringUtils.isNotBlank(crisObj.getUuid())) {
                getCrisSubscribeService().unsubscribe(crisObj.getUuid());
            }
        } catch (Exception e) {
            log.error("Failed to remove subscriptions for entity " + crisObj.getTypeText() + "/" + crisObj.getCrisID());
        }

        log.debug("End onPostDelete " + RPAutoUnsubscribeListener.class);
    }

    public CrisSubscribeService getCrisSubscribeService() {
        if(crisSubscribeService == null) {
            crisSubscribeService = new Researcher().getCrisSubscribeService();
        }
        return crisSubscribeService;
    }

    public void setCrisSubscribeService(CrisSubscribeService crisSubscribeService) {
        this.crisSubscribeService = crisSubscribeService;
    }
}
