package org.dspace.app.webui.ldn;

import java.sql.SQLException;

import org.dspace.core.Context;

public class LDNOfferIngestAction extends LDNPayloadProcessor {

	@Override
	protected void processLDNPayload(NotifyLDNRequestDTO ldnRequestDTO, Context context)
			throws IllegalStateException, SQLException {

	}

	@Override
	protected String generateMetadataValue(NotifyLDNRequestDTO ldnRequestDTO) {
		return null;
	}
	
}
