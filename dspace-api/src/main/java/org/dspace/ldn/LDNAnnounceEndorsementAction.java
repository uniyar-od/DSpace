package org.dspace.ldn;

import static org.dspace.ldn.LDNMetadataFields.ELEMENT;
import static org.dspace.ldn.LDNMetadataFields.SCHEMA;

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
		
		String metadataIdentifierServiceID = new StringBuilder(LDNUtils.METADATA_DELIMITER).append(ldnRequestDTO.getOrigin().getId())
				.append(LDNUtils.METADATA_DELIMITER).toString();

		removeMetadata(item, SCHEMA, ELEMENT,
				new String[] { LDNMetadataFields.REQUEST, LDNMetadataFields.EXAMINATION, LDNMetadataFields.REFUSED },
				metadataIdentifierServiceID);
		
		
		String metadataValue = generateMetadataValue(ldnRequestDTO);
		item.addMetadata(SCHEMA, ELEMENT, LDNMetadataFields.ENDORSMENT, null, metadataValue);

		
	}

	@Override
	protected String generateMetadataValue(NotifyLDNRequestDTO ldnRequestDTO) {
		//coar.notify.endorsement
		return null;
	}

	
}
