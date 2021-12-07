package org.dspace.app.webui.ldn;

import java.util.List;

import org.dspace.content.Item;

public abstract class LDNPayloadProcessor {

	public final void removeMetadata(Item item, String schema, String element, String... qualifiers) {
		for (String qualifier : qualifiers) {
			item.clearMetadata(schema, element, qualifier, null);
		}
	}

	public abstract void processLDNPayload(NotifyLDNRequestDTO ldnRequestDTO);

	protected abstract String generateMetadataValue(NotifyLDNRequestDTO ldnRequestDTO);
}
