package org.dspace.ldn;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.dspace.content.Item;
import org.dspace.core.Context;

public abstract class LDNPayloadProcessor {
	/** Logger */
	protected static Logger logger = Logger.getLogger(LDNPayloadProcessor.class);

	public final void removeMetadata(Item item, String schema, String element, String qualifier, String value) {
		removeMetadata(item, schema, element, qualifier, new String[] { value });
	}

	public final void removeMetadata(Item item, String schema, String element, String[] qualifiers, String value) {
		for (String qualifier : qualifiers)
			removeMetadata(item, schema, element, qualifier, value);
	}

	public final void removeMetadata(Item item, String schema, String element, String qualifier, String[] identifiers) {
		LDNUtils.deleteMetadataByValue(item, schema, element, qualifier, identifiers);
	}

	public final void removeMetadata(Item item, String schema, String element, String[] qualifiers,
			String[] identifiers) {
		for (String qualifier : qualifiers)
			removeMetadata(item, schema, element, qualifier, identifiers);
	}

	protected abstract void processLDNPayload(NotifyLDNRequestDTO ldnRequestDTO, Context context)
			throws IllegalStateException, SQLException;

	public final void processRequest(NotifyLDNRequestDTO ldnRequestDTO) {

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
