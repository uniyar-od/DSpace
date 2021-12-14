package org.dspace.ldn;

import static org.dspace.ldn.LDNMetadataFields.ELEMENT;
import static org.dspace.ldn.LDNMetadataFields.SCHEMA;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.handle.HandleManager;

public class LDNAnnounceEndorsementAction extends LDNPayloadProcessor {

	@Override
	protected void processLDNPayload(NotifyLDNDTO ldnRequestDTO, Context context)
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
	protected String generateMetadataValue(NotifyLDNDTO ldnRequestDTO) {
		//coar.notify.endorsement

		StringBuilder builder = new StringBuilder();

		String timestamp = new SimpleDateFormat(LDNUtils.DATE_PATTERN).format(Calendar.getInstance().getTime());
		String reviewServiceId = ldnRequestDTO.getObject().getId();
		String repositoryInitializedMessageId = ldnRequestDTO.getInReplyTo();
		String linkToTheEndorsment = ldnRequestDTO.getObject().getId();

		builder.append(timestamp);
		builder.append(LDNUtils.METADATA_DELIMITER);

		builder.append(reviewServiceId);
		builder.append(LDNUtils.METADATA_DELIMITER);

		builder.append(repositoryInitializedMessageId);
		builder.append(LDNUtils.METADATA_DELIMITER);

		builder.append("success");
		builder.append(LDNUtils.METADATA_DELIMITER);

		builder.append(linkToTheEndorsment);

		logger.info(builder.toString());

		return builder.toString();
	}

	
}
