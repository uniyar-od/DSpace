package org.dspace.app.webui.ldn;

import static org.dspace.app.webui.ldn.LDNMetadataFields.ELEMENT;
import static org.dspace.app.webui.ldn.LDNMetadataFields.SCHEMA;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.handle.HandleManager;

public class LDNOfferReviewAction extends LDNPayloadProcessor {

	/*
	 * Used in Scenario 2 - 5 - 9
	 * Used to record the decision to start one of the above
	 * scenarios with a notification of type: Offer, ReviewAction
	 * 
	 * uses metadata coar.notify.initialize
	 */

	@Override
	public void processLDNPayload(NotifyLDNRequestDTO ldnRequestDTO) {

		Context context = null;
		try {
			context = new Context();

			String itemHandle = LDNUtils.getHandleFromURL(ldnRequestDTO.getContext().getId());

			DSpaceObject dso = HandleManager.resolveToObject(context, itemHandle);
			Item item = Item.find(context, dso.getID());
			String metadataValue = generateMetadataValue(ldnRequestDTO);
			item.addMetadata(SCHEMA, ELEMENT, LDNMetadataFields.INITIALIZE, null, metadataValue);

			//NO METADATA IS REMOVED

		} catch (SQLException e) {

		} finally {
			// Abort the context if it's still valid
			if ((context != null) && context.isValid()) {
				context.abort();
			}
		}

	}

	@Override
	protected String generateMetadataValue(NotifyLDNRequestDTO ldnRequestDTO) {
		// setting up coar.notify.initialize metadata
		
		String delimiter = "//";
		StringBuilder builder = new StringBuilder();



		return builder.toString();
	}

}
