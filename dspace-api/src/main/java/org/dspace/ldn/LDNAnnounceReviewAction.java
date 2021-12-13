package org.dspace.ldn;

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

import com.fasterxml.jackson.core.JsonProcessingException;

import static org.dspace.ldn.LDNMetadataFields.ELEMENT;
import static org.dspace.ldn.LDNMetadataFields.SCHEMA;

public class LDNAnnounceReviewAction extends LDNPayloadProcessor {

	/*
	 * Used in Scenario 2 - 5 - 9 Used to record the decision to start one of the
	 * above scenarios with a notification of type: Offer, ReviewAction
	 * 
	 * uses metadata coar.notify.request
	 */

	@Override
	protected void processLDNPayload(NotifyLDNRequestDTO ldnRequestDTO, Context context)
			throws IllegalStateException, SQLException {

		generateMetadataValue(ldnRequestDTO);
		
		String itemHandle = LDNUtils.getHandleFromURL(ldnRequestDTO.getContext().getId());

		DSpaceObject dso = HandleManager.resolveToObject(context, itemHandle);
		Item item = Item.find(context, dso.getID());

		String metadataIdentifierServiceID = new StringBuilder(LDNUtils.METADATA_DELIMITER)
				.append(ldnRequestDTO.getOrigin().getId()).append(LDNUtils.METADATA_DELIMITER).toString();

		removeMetadata(item, SCHEMA, ELEMENT,
				new String[] { LDNMetadataFields.REQUEST, LDNMetadataFields.EXAMINATION, LDNMetadataFields.REFUSED },
				metadataIdentifierServiceID);

		String metadataValue = generateMetadataValue(ldnRequestDTO);
		item.addMetadata(SCHEMA, ELEMENT, LDNMetadataFields.REVIEW, null, metadataValue);

	}

	@Override
	protected String generateMetadataValue(NotifyLDNRequestDTO ldnRequestDTO) {
		// setting up coar.notify.review metadata

		StringBuilder builder = new StringBuilder();

		String timestamp = new SimpleDateFormat(LDNUtils.DATE_PATTERN).format(Calendar.getInstance().getTime());
		String reviewServiceId = ldnRequestDTO.getObject().getId();
		String repositoryInitializedMessageId = ldnRequestDTO.getInReplyTo();
		String linkToTheReview = ldnRequestDTO.getObject().getId();

		builder.append(timestamp);
		builder.append(LDNUtils.METADATA_DELIMITER);

		builder.append(reviewServiceId);
		builder.append(LDNUtils.METADATA_DELIMITER);

		builder.append(repositoryInitializedMessageId);
		builder.append(LDNUtils.METADATA_DELIMITER);

		builder.append("success");
		builder.append(LDNUtils.METADATA_DELIMITER);

		builder.append(linkToTheReview);

		logger.info(builder.toString());

		return builder.toString();
	}

}
