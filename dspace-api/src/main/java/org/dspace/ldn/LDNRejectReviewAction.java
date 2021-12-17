package org.dspace.ldn;

import static org.dspace.ldn.LDNMetadataFields.ELEMENT;
import static org.dspace.ldn.LDNMetadataFields.SCHEMA;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.handle.HandleManager;

public class LDNRejectReviewAction extends LDNPayloadProcessor {

	@Override
	protected void processLDNPayload(NotifyLDNDTO ldnRequestDTO, Context context)
			throws IllegalStateException, SQLException, AuthorizeException {

		String itemHandle = LDNUtils.getHandleFromURL(ldnRequestDTO.getContext().getId());

		DSpaceObject dso = HandleManager.resolveToObject(context, itemHandle);
		Item item = (Item) dso;

		String metadataIdentifierServiceID = new StringBuilder(LDNUtils.METADATA_DELIMITER)
				.append(ldnRequestDTO.getOrigin().getId()).append(LDNUtils.METADATA_DELIMITER).toString();

		if (StringUtils.isNotBlank(ldnRequestDTO.getInReplyTo())) {
			String repositoryMessageID = new StringBuilder(LDNUtils.METADATA_DELIMITER).append(ldnRequestDTO.getInReplyTo())
					.toString();
			LDNUtils.removeMetadata(item, SCHEMA, ELEMENT, LDNMetadataFields.EXAMINATION,
					new String[] { metadataIdentifierServiceID, repositoryMessageID });
		}

		String metadataValue = generateMetadataValue(ldnRequestDTO);
		item.addMetadata(SCHEMA, ELEMENT, LDNMetadataFields.REFUSED, LDNUtils.getDefaultLanguageQualifier(), metadataValue);
		item.update();

	}

	@Override
	protected String generateMetadataValue(NotifyLDNDTO ldnRequestDTO) {
		// coar.notify.refused
		StringBuilder builder = new StringBuilder();

		String timestamp = new SimpleDateFormat(LDNUtils.DATE_PATTERN).format(Calendar.getInstance().getTime());
		String reviewServiceId = ldnRequestDTO.getOrigin().getId();
		String repositoryInitializedMessageId = ldnRequestDTO.getInReplyTo();

		builder.append(timestamp);
		builder.append(LDNUtils.METADATA_DELIMITER);

		builder.append(reviewServiceId);
		builder.append(LDNUtils.METADATA_DELIMITER);

		builder.append(repositoryInitializedMessageId);

		logger.info(builder.toString());
		
		return builder.toString();
	}

}
