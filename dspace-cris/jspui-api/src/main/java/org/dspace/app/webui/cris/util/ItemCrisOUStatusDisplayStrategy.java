/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.webui.cris.util;

import java.text.MessageFormat;
import java.util.MissingResourceException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dspace.app.webui.util.ASimpleDisplayStrategy;
import org.dspace.content.Metadatum;
import org.dspace.core.I18nUtil;

public class ItemCrisOUStatusDisplayStrategy extends ASimpleDisplayStrategy
{

    /** log4j category */
    public static final Log log = LogFactory
            .getLog(ItemCrisOUStatusDisplayStrategy.class);

    @Override
    public String getMetadataDisplay(HttpServletRequest hrq, int limit,
            boolean viewFull, String browseType, int colIdx, int itemId,
            String field, Metadatum[] metadataArray, boolean disableCrossLinks,
            boolean emph) throws JspException {
        String type = (metadataArray != null && metadataArray.length > 0) ? metadataArray[0].value : null;
        if (StringUtils.isNotBlank(type)) {
            type = type.replaceAll("[^A-Za-z0-9 ]", "").replaceAll(" ", "-").toLowerCase();
        }
        String icon = "";
        try {
            icon = MessageFormat.format(
                    I18nUtil.getMessage("ItemCrisOUStatusDisplayStrategy." + type + ".icon", true),
                    I18nUtil
                    .getMessage("ItemCrisOUStatusDisplayStrategy." + type + ".title", true));
        } catch (Exception e) {
            log.debug("Error when build icon (perhaps missing this configuration: on cris module key:researcher.cris.rp.ref.display.strategy.metadata.icon)", e);
            try {
                icon = I18nUtil.getMessage("ItemCrisRefDisplayStrategy.ou.icon", true);
            } catch (MissingResourceException e2) {
                log.debug("Error when build icon (perhaps missing this configuration: on cris module key:researcher.cris.rp.ref.display.strategy.metadata.icon)", e2);
                icon = I18nUtil.getMessage("ItemCrisRefDisplayStrategy.default.icon");
            }
        }
        return icon;
    }
}
