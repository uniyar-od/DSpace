/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * https://github.com/CILEA/dspace-cris/wiki/License
 */
package org.dspace.app.webui.cris.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dspace.app.cris.integration.CRISAuthority;
import org.dspace.app.cris.model.ResearcherPage;
import org.dspace.app.cris.service.ApplicationService;
import org.dspace.app.cris.util.ResearcherPageUtils;
import org.dspace.app.webui.util.UIUtil;
import org.dspace.authority.AuthorityValueGenerator;
import org.dspace.content.Metadatum;
import org.dspace.content.authority.ChoiceAuthority;
import org.dspace.content.authority.ChoiceAuthorityManager;
import org.dspace.content.authority.Choices;
import org.dspace.content.authority.MetadataAuthorityManager;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.I18nUtil;
import org.dspace.core.Utils;
import org.dspace.utils.DSpace;

public class OrcidItemCrisRefDisplayStrategy extends ItemCrisRefDisplayStrategy
{

    /**
     * log4j category
     */
    public static final Log log = LogFactory
            .getLog(ItemCrisRefDisplayStrategy.class);

    private ApplicationService applicationService = new DSpace()
            .getServiceManager()
            .getServiceByName("applicationService", ApplicationService.class);

    @Override
    public String getMetadataDisplay(HttpServletRequest hrq, int limit,
            boolean viewFull, String browseType, int colIdx, int itemId,
            String field, Metadatum[] metadataArray, boolean disableCrossLinks,
            boolean emph) throws JspException
    {
        String publicPath = null;
        int minConfidence = -1;
        if (metadataArray.length > 0)
        {
            ChoiceAuthorityManager cam = ChoiceAuthorityManager.getManager();
            ChoiceAuthority ca = cam.getChoiceAuthority(metadataArray[0].schema,
                    metadataArray[0].element, metadataArray[0].qualifier);
            minConfidence = MetadataAuthorityManager.getManager()
                    .getMinConfidence(metadataArray[0].schema,
                            metadataArray[0].element,
                            metadataArray[0].qualifier);
            if (ca != null && ca instanceof CRISAuthority)
            {
                CRISAuthority crisAuthority = (CRISAuthority) ca;
                publicPath = crisAuthority.getPublicPath();
                if (publicPath == null)
                {
                    publicPath = ConfigurationManager.getProperty(
                            "ItemCrisRefDisplayStrategy.publicpath." + field);
                    if (publicPath == null)
                    {
                        publicPath = metadataArray[0].qualifier;
                    }
                }
            }
        }

        if (publicPath == null)
        {
            return "";
        }

        String metadata;
        // limit the number of records if this is the author field (if
        // -1, then the limit is the full list)
        boolean truncated = false;
        int loopLimit = metadataArray.length;
        if (limit != -1)
        {
            loopLimit = (limit > metadataArray.length ? metadataArray.length
                    : limit);
            truncated = (limit < metadataArray.length);
        }

        StringBuffer sb = new StringBuffer();
        for (int j = 0; j < loopLimit; j++)
        {
            buildBrowseLink(hrq, viewFull, browseType, metadataArray,
                    minConfidence, disableCrossLinks, sb, j);
            if (StringUtils.isNotBlank(metadataArray[j].authority)
                    && metadataArray[j].confidence >= minConfidence)
            {
                buildAuthority(hrq, metadataArray, publicPath, sb, j);
            }
            if (j < (loopLimit - 1))
            {
                if (colIdx != -1) // we are showing metadata in a table row
                                  // (browse or item list)
                {
                    sb.append("; ");
                }
                else
                {
                    // we are in the item tag
                    sb.append("<br />");
                }
            }
        }
        if (truncated)
        {
            Locale locale = UIUtil.getSessionLocale(hrq);
            String etal = I18nUtil.getMessage("itemlist.et-al", locale);
            sb.append(", " + etal);
        }

        if (colIdx != -1) // we are showing metadata in a table row (browse or
                          // item list)
        {
            metadata = (emph ? "<strong><em>" : "<em>") + sb.toString()
                    + (emph ? "</em></strong>" : "</em>");
        }
        else
        {
            // we are in the item tag
            metadata = (emph ? "<strong>" : "") + sb.toString()
                    + (emph ? "</strong>" : "");
        }

        return metadata;
    }

    protected void buildBrowseLink(HttpServletRequest hrq, boolean viewFull,
            String browseType, Metadatum[] metadataArray, int minConfidence,
            boolean disableCrossLinks, StringBuffer sb, int j)
    {
        String startLink = "";
        String endLink = "";
        if (!StringUtils.isEmpty(browseType) && !disableCrossLinks)
        {
            String argument;
            String value;
            String authority = metadataArray[j].authority;
            if (authority != null
                    && metadataArray[j].confidence >= minConfidence)
            {
                argument = "authority";
                value = authority;
            }
            else
            {
                argument = "value";
                value = metadataArray[j].value;
            }
            if (viewFull)
            {
                argument = "vfocus";
            }
            try
            {
                startLink = "<a href=\"" + hrq.getContextPath()
                        + "/browse?type=" + browseType + "&amp;" + argument
                        + "=" + URLEncoder.encode(value, "UTF-8");
            }
            catch (UnsupportedEncodingException e)
            {
                throw new RuntimeException(e.getMessage(), e);
            }

            if (metadataArray[j].language != null)
            {
                try
                {
                    startLink = startLink + "&amp;" + argument + "_lang="
                            + URLEncoder.encode(metadataArray[j].language,
                                    "UTF-8");
                }
                catch (UnsupportedEncodingException e)
                {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }

            if ("authority".equals(argument))
            {
                startLink += "\" class=\"authority " + browseType + "\">";
            }
            else
            {
                startLink = startLink + "\">";
            }
            endLink = "</a>";
        }
        sb.append(startLink);
        sb.append(Utils.addEntities(metadataArray[j].value));
        sb.append(endLink);
    }

    protected void buildAuthority(HttpServletRequest hrq,
            Metadatum[] metadataArray, String publicPath, StringBuffer sb,
            int j)
    {
        String startLink = "";
        String endLink = "";
        Boolean hasOrcidLink = false;
        String startOrcidLink = "";
        String endOrcidLink = "";
        String type = null;
        String typefield = null;

        String externalContextPath = ConfigurationManager.getProperty("cris",
                "external.domainname.authority.service.orcid");
        String authority = metadataArray[j].authority;
        int confidence = metadataArray[j].confidence;
        if (StringUtils.isNotBlank(authority))
        {
            if (authority.startsWith(AuthorityValueGenerator.GENERATE))
            {
                String[] split = StringUtils.split(authority,
                        AuthorityValueGenerator.SPLIT);
                String info = null;
                if (split.length == 3)
                {
                    type = split[1];
                    info = split[2];
                    if (type.equals("orcid"))
                    {
                        startLink = "<a target=\"_blank\" href=\""
                                + externalContextPath + info;
                        startLink += "\" class=\"authority\">&nbsp;<img style=\"width: 16px; height: 16px;\" src=\""
                                + hrq.getContextPath()
                                + "/images/mini-icon-orcid.png\" alt=\"\">";
                        endLink = "</a>";
                        sb.append(startLink);
                        sb.append(endLink);
                    }
                }
            }
            else
            {
                startLink = "&nbsp;<a href=\"" + hrq.getContextPath() + "/cris/"
                        + publicPath + "/" + authority;
                startLink += "\" class=\"authority\">";
                endLink = "</a>";
                String icon = "";
                try
                {
                    ResearcherPage rp = applicationService
                            .getEntityByCrisId(authority);
                    String status = "";
                    if (rp == null || !rp.getStatus())
                    {
                        // startLink = "&nbsp;";
                        // endLink = "";
                        status = "private.";
                        typefield = "unknown";
                    }
                    else
                    {
                        typefield = rp.getMetadata(ConfigurationManager
                                .getProperty("cris", "researcher.cris."
                                        + publicPath
                                        + ".ref.display.strategy.metadata.icon"));
                    }

                    // Now add the ORCID icon, if an ORCID is found in the
                    // researcher profile and if the profile has an owner
                    if (rp != null)
                    {
                        try
                        {
                            String info = rp.getMetadata("orcid");
                            if (StringUtils.isNotBlank(info)
                                    && confidence == Choices.CF_ACCEPTED)
                            {
                                startOrcidLink = "<a target=\"_blank\" href=\""
                                        + externalContextPath + info;
                                startOrcidLink += "\" class=\"authority\">&nbsp;<img style=\"width: 16px; height: 16px;\" src=\""
                                        + hrq.getContextPath()
                                        + "/images/mini-icon-orcid.png\" alt=\"\">";
                                endOrcidLink = "</a>";
                                hasOrcidLink = true;
                            }
                        }
                        catch (Exception e)
                        {
                            // NOOP
                        }
                    }
                    
                    String title;
                    try
                    {
                        title = I18nUtil.getMessage(
                                "ItemCrisRefDisplayStrategy." + publicPath + "."
                                        + status + typefield + ".title",
                                true);
                    }
                    catch (MissingResourceException e2)
                    {
                        title = I18nUtil.getMessage(
                                "ItemCrisRefDisplayStrategy." + publicPath + "."
                                        + typefield + ".title");
                    }

                    try
                    {
                        icon = MessageFormat.format(I18nUtil.getMessage(
                                "ItemCrisRefDisplayStrategy." + publicPath + "."
                                        + status + typefield + ".icon",
                                true), title);
                    }
                    catch (MissingResourceException e2)
                    {
                        icon = MessageFormat.format(I18nUtil.getMessage(
                                "ItemCrisRefDisplayStrategy." + publicPath + "."
                                        + typefield + ".icon"),
                                title);
                    }
                }
                catch (Exception e)
                {
                    log.debug(
                            "Error when build icon (perhaps missing this configuration: on cris module key:researcher.cris.rp.ref.display.strategy.metadata.icon)",
                            e);
                    try
                    {
                        icon = I18nUtil.getMessage("ItemCrisRefDisplayStrategy."
                                + publicPath + ".icon", true);
                    }
                    catch (MissingResourceException e2)
                    {
                        log.debug(
                                "Error when build icon (perhaps missing this configuration: on cris module key:researcher.cris.rp.ref.display.strategy.metadata.icon)",
                                e2);
                        icon = I18nUtil.getMessage(
                                "ItemCrisRefDisplayStrategy.default.icon");
                    }
                }
                sb.append(startLink);
                sb.append(icon);
                sb.append(endLink);
                if (hasOrcidLink == true)
                {
                    sb.append(startOrcidLink);
                    sb.append(endOrcidLink);
                }
            }
        }

    }
}
