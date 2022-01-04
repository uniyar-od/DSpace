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
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.core.Context;
import org.dspace.handle.factory.HandleServiceFactory;

public class LDNAcceptReviewAction extends LDNPayloadProcessor {

	@Override
	protected void processLDNPayload(NotifyLDNDTO ldnRequestDTO, Context context)
			throws IllegalStateException, SQLException, AuthorizeException {

		String itemHandle = LDNUtils.getHandleFromURL(ldnRequestDTO.getContext().getId());
		DSpaceObject dso = HandleServiceFactory.getInstance().getHandleService().resolveToObject(context, itemHandle);
		Item item = (Item) dso;

		String metadataIdentifierServiceID = new StringBuilder(LDNUtils.METADATA_DELIMITER)
				.append(ldnRequestDTO.getOrigin().parseIdWithRemovedProtocol()).append(LDNUtils.METADATA_DELIMITER)
				.toString();
		LDNUtils.removeMetadata(context, item, SCHEMA, ELEMENT,
				new String[] { LDNMetadataFields.REQUEST_REVIEW, LDNMetadataFields.REFUSED },
				metadataIdentifierServiceID);

		if (StringUtils.isNotBlank(ldnRequestDTO.getInReplyTo())) {
			String repositoryMessageID = new StringBuilder(LDNUtils.METADATA_DELIMITER)
					.append(ldnRequestDTO.getInReplyTo()).toString();
			LDNUtils.removeMetadata(context, item, SCHEMA, ELEMENT, new String[] { LDNMetadataFields.REFUSED },
					new String[] { metadataIdentifierServiceID, repositoryMessageID });

			String metadataValue = generateMetadataValue(ldnRequestDTO);
			ContentServiceFactory.getInstance().getItemService().addMetadata(context, item, SCHEMA, ELEMENT, LDNMetadataFields.EXAMINATION,
					LDNUtils.getDefaultLanguageQualifier(), metadataValue);
			ContentServiceFactory.getInstance().getItemService().update(context, item);

		}

	}

	@Override
	protected String generateMetadataValue(NotifyLDNDTO ldnRequestDTO) {
		// coar.notify.examination
		StringBuilder builder = new StringBuilder();

		String timestamp = new SimpleDateFormat(LDNUtils.DATE_PATTERN).format(Calendar.getInstance().getTime());
		String reviewServiceId = ldnRequestDTO.getOrigin().parseIdWithRemovedProtocol();
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
