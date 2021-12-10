package org.dspace.app.webui.ldn;

import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.dspace.app.webui.servlet.LDNInBoxServlet;
import org.dspace.content.Item;
import org.dspace.core.Context;

public abstract class LDNPayloadProcessor {
	/** Logger */
	protected static Logger logger = Logger.getLogger(LDNPayloadProcessor.class);

	public final void removeMetadata(Item item, String schema, String element, String... qualifiers) {
		for (String qualifier : qualifiers) {
			item.clearMetadata(schema, element, qualifier, null);
		}
	}

	protected abstract void processLDNPayload(NotifyLDNRequestDTO ldnRequestDTO, Context context)
			throws IllegalStateException, SQLException;

	public void processRequest(NotifyLDNRequestDTO ldnRequestDTO) {

		Context context = null;
		try {
			context = new Context();
			processLDNPayload(ldnRequestDTO, context);

		} catch (SQLException e) {
			logger.error(e);
		} finally {
			// Abort the context if it's still valid
			if ((context != null) && context.isValid()) {
				context.abort();
			}
		}
	}

	protected abstract String generateMetadataValue(NotifyLDNRequestDTO ldnRequestDTO);
}
