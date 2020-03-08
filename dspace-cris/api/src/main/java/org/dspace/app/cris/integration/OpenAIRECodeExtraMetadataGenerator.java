package org.dspace.app.cris.integration;

import java.util.HashMap;
import java.util.Map;

public class OpenAIRECodeExtraMetadataGenerator implements OpenAIREExtraMetadataGenerator
{
    private String relatedInputformMetadata = "dc_relation_grantno";
    
    @Override
    public Map<String, String> build(String value)
    {
        Map<String, String> extras = new HashMap<String, String>();
        extras.put("data-" + getRelatedInputformMetadata(), value);
        return extras;
    }

    public String getRelatedInputformMetadata()
    {
        return relatedInputformMetadata;
    }

    public void setRelatedInputformMetadata(String relatedInputformMetadata)
    {
        this.relatedInputformMetadata = relatedInputformMetadata;
    }


}
