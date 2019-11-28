/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.cris.integration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.dspace.app.cris.model.ACrisObject;
import org.dspace.app.cris.util.ResearcherPageUtils;
import org.dspace.content.MetadataValue;
import org.dspace.core.ConfigurationManager;

public class CRISExtraBasicMetadataGenerator
    implements CRISExtraMetadataGenerator
{
    private String relatedMetadata;
    private String relatedInputformMetadata;
    private String type;

    public Map<String, String> build(ACrisObject crisObject)
    {
        Map<String, String> extras = new HashMap<String, String>();

        List<MetadataValue> mm = crisObject.getMetadataValue(getRelatedMetadata());
        if (mm != null && !mm.isEmpty())
        {
            MetadataValue m = mm.get(0);
            if (m != null)
            {
                if (ConfigurationManager.getBooleanProperty("authority.controlled." + getRelatedInputformMetadata().replaceAll("_", "."), false))
                {
                    if (StringUtils.isNotBlank(m.getAuthority()))
                    {
                        extras.put("data-" + getRelatedInputformMetadata(), m.getValue() + "::" + m.getAuthority());
                    }
                    else
                    {
                        extras.put("data-" + getRelatedInputformMetadata(), m.getValue() + "::" + ResearcherPageUtils.getPersistentIdentifier(crisObject));
                    }
                }
                else
                {
                    extras.put("data-" + getRelatedInputformMetadata(), m.getValue());
                }
                return extras;
            }
        }

        extras.put("data-" + getRelatedInputformMetadata(), "");
        return extras;
    }

    public void setRelatedInputformMetadata(String relatedInputformMetadata)
    {
        this.relatedInputformMetadata = relatedInputformMetadata;
    }

    public String getRelatedInputformMetadata()
    {
        return relatedInputformMetadata;
    }

    public void setRelatedMetadata(String relatedMetadata)
    {
        this.relatedMetadata = relatedMetadata;
    }

    public String getRelatedMetadata()
    {
        return relatedMetadata;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String newType)
    {
        this.type = newType;
    }
}
