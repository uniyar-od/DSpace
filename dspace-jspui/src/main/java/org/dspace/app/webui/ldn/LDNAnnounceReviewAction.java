package org.dspace.app.webui.ldn;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;

import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.handle.HandleManager;

import static org.dspace.app.webui.ldn.LDNMetadataFields.SCHEMA;
import static org.dspace.app.webui.ldn.LDNMetadataFields.ELEMENT;

public class LDNAnnounceReviewAction extends LDNPayloadProcessor {
	
	/*
	 * Used in Scenario 2 - 5 - 9
	 * Used to record the decision to start one of the above
	 * scenarios with a notification of type: Offer, ReviewAction
	 * 
	 * uses metadata coar.notify.request
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
			item.addMetadata(SCHEMA, ELEMENT, LDNMetadataFields.REVIEW, null, metadataValue);

			//Removes metadata from previous step OfferReviewAction
			removeMetadata(item, SCHEMA, ELEMENT, LDNMetadataFields.INITIALIZE);

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
		// setting up coar.notify.request metadata
		
		String delimiter = "//";
		StringBuilder builder = new StringBuilder();

		String timestamp = new SimpleDateFormat("yyyy-mm-ddTHH:MM:SSZ").format(Calendar.getInstance().getTime());
		String reviewServiceId = ldnRequestDTO.getObject().getId();

		builder.append(timestamp);
		builder.append(delimiter);

		builder.append(reviewServiceId);
		builder.append(delimiter);

		// TODO missing Repository-MessageId- CHECK ReviewServiceId is the value set up

		return builder.toString();
	}

}
