package org.dspace.app.cris.integration;

import java.util.Map;

public interface OpenAIREExtraMetadataGenerator
{

    Map<String, String> build(String value);

}
