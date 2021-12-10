package org.dspace.app.webui.ldn;

import static org.dspace.app.webui.ldn.LDNMetadataFields.ELEMENT;
import static org.dspace.app.webui.ldn.LDNMetadataFields.SCHEMA;

import java.sql.SQLException;

import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.handle.HandleManager;

public class LDNAnnounceEndorsementAction extends LDNPayloadProcessor {

	@Override
	protected void processLDNPayload(NotifyLDNRequestDTO ldnRequestDTO, Context context)
			throws IllegalStateException, SQLException {

		String itemHandle = LDNUtils.getHandleFromURL(ldnRequestDTO.getContext().getId());

		DSpaceObject dso = HandleManager.resolveToObject(context, itemHandle);
		Item item = Item.find(context, dso.getID());
		String metadataValue = generateMetadataValue(ldnRequestDTO);
		item.addMetadata(SCHEMA, ELEMENT, LDNMetadataFields.ENDORSMENT, null, metadataValue);

		// Removes metadata from previous step OfferReviewAction
		removeMetadata(item, SCHEMA, ELEMENT, LDNMetadataFields.REQUEST, LDNMetadataFields.EXAMINATION, LDNMetadataFields.REFUSED);
	}

	@Override
	protected String generateMetadataValue(NotifyLDNRequestDTO ldnRequestDTO) {
		return null;
	}

	
}
