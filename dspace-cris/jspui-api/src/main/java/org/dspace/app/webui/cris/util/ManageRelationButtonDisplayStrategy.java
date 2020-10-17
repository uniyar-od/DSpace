/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.webui.cris.util;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;

import org.dspace.app.cris.model.ACrisObject;
import org.dspace.app.webui.util.IDisplayMetadataValueStrategy;
import org.dspace.browse.BrowsableDSpaceObject;
import org.dspace.browse.BrowseDSpaceObject;
import org.dspace.content.IMetadataValue;
import org.dspace.content.Item;
import org.dspace.core.I18nUtil;
import org.dspace.discovery.IGlobalSearchResult;

public class ManageRelationButtonDisplayStrategy implements IDisplayMetadataValueStrategy
{

    @Override
    public String getMetadataDisplay(HttpServletRequest hrq, int limit,
            boolean viewFull, String browseType, UUID colIdx, String field,
            List<IMetadataValue> metadataArray, BrowsableDSpaceObject item,
            boolean disableCrossLinks, boolean emph)
            throws JspException {
        ACrisObject cris = (ACrisObject) (item);
        if (hrq.getAttribute("added-crisIDs") != null
                && Arrays.asList(((String)hrq.getAttribute("added-crisIDs")).split(",")).contains(cris.getCrisID())) {
            if (hrq.getAttribute("removeRelation") != null && (Boolean)hrq.getAttribute("removeRelation")) {
                return "<a class=\"btn btn-danger\" href=\""
                        + "relations?"
                        + hrq.getQueryString() + "&action=remove&selected-crisID=" + cris.getCrisID() + "\""
                        + ">"
                        + I18nUtil.getMessage("jsp.layout.cris.relations.removerelation.button") + "</a>";
            }
            else {
                return "<span>"
                        + I18nUtil.getMessage("jsp.layout.cris.relations.addrelation.added")
                        + "</span>";
            }
        }
        else {
            return "<a class=\"btn btn-default\" href=\""
                    + "relations?"
                    + hrq.getQueryString() + "&action=add&selected-crisID=" + cris.getCrisID() + "\""
                    + ">"
                    + I18nUtil.getMessage("jsp.layout.cris.relations.addrelation.button")
                    + "</a>";
        }
    }

    @Override
    public String getMetadataDisplay(HttpServletRequest hrq, int limit,
            boolean viewFull, String browseType, UUID colIdx, String field,
            List<IMetadataValue> metadataArray, Item item, boolean disableCrossLinks,
            boolean emph)
            throws JspException {
        if (hrq.getAttribute("added-itemIDs") != null
                && Arrays.asList(((String)hrq.getAttribute("added-itemIDs")).split(",")).contains(String.valueOf(item.getID()))) {
            if (hrq.getAttribute("removeRelation") != null && (Boolean)hrq.getAttribute("removeRelation")) {
                return "<a class=\"btn btn-danger\" href=\""
                        + "relations?"
                        + hrq.getQueryString() + "&action=remove&selected-itemID=" + item.getID() + "\""
                        + ">"
                        + I18nUtil.getMessage("jsp.layout.cris.relations.removerelation.button") + "</a>";
            }
            else {
                return "<span>"
                        + I18nUtil.getMessage("jsp.layout.cris.relations.addrelation.added")
                        + "</span>";
            }
        }
        else {
            return "<a class=\"btn btn-default\" href=\""
                    + "relations?"
                    + hrq.getQueryString() + "&action=add&selected-itemID=" + item.getID() + "\""
                    + ">"
                    + I18nUtil.getMessage("jsp.layout.cris.relations.addrelation.button") + "</a>";
        }
    }

    @Override
    public String getExtraCssDisplay(HttpServletRequest hrq, int limit,
            boolean b, String browseType, UUID colIdx, String field,
            List<IMetadataValue> metadataArray, BrowsableDSpaceObject browseItem,
            boolean disableCrossLinks, boolean emph)
            throws JspException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getExtraCssDisplay(HttpServletRequest hrq, int limit,
            boolean b, String browseType, UUID colIdx, String field,
            List<IMetadataValue> metadataArray, Item item, boolean disableCrossLinks,
            boolean emph)
            throws JspException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getMetadataDisplay(HttpServletRequest hrq, int limit,
            boolean viewFull, String browseType, UUID colIdx, String field,
            List<IMetadataValue> metadataArray, IGlobalSearchResult item,
            boolean disableCrossLinks, boolean emph)
            throws JspException {
        // TODO Auto-generated method stub
        return null;
    }

}
