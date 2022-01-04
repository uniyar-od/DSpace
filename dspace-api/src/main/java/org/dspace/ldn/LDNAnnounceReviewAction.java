package org.dspace.ldn;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.core.Context;
import org.dspace.handle.factory.HandleServiceFactory;

import static org.dspace.ldn.LDNMetadataFields.ELEMENT;
import static org.dspace.ldn.LDNMetadataFields.SCHEMA;

public class LDNAnnounceReviewAction extends LDNPayloadProcessor {

	@Override
	protected void processLDNPayload(NotifyLDNDTO ldnRequestDTO, Context context)
			throws IllegalStateException, SQLException, AuthorizeException {

		generateMetadataValue(ldnRequestDTO);

		String itemHandle = LDNUtils.getHandleFromURL(ldnRequestDTO.getContext().getId());

		DSpaceObject dso = HandleServiceFactory.getInstance().getHandleService().resolveToObject(context, itemHandle);
		Item item = (Item) dso;

		String metadataIdentifierServiceID = new StringBuilder(LDNUtils.METADATA_DELIMITER)
				.append(ldnRequestDTO.getOrigin().parseIdWithRemovedProtocol()).append(LDNUtils.METADATA_DELIMITER)
				.toString();

		String repositoryInitializedMessageId = ldnRequestDTO.getInReplyTo();
		LDNUtils.removeMetadata(context, item, SCHEMA, ELEMENT,
				new String[] { LDNMetadataFields.REQUEST_REVIEW, LDNMetadataFields.EXAMINATION,
						LDNMetadataFields.REFUSED },
				new String[] { metadataIdentifierServiceID, repositoryInitializedMessageId });
		String metadataValue = generateMetadataValue(ldnRequestDTO);
		ContentServiceFactory.getInstance().getItemService().addMetadata(context, item, SCHEMA, ELEMENT, LDNMetadataFields.REVIEW,
				LDNUtils.getDefaultLanguageQualifier(), metadataValue);
		ContentServiceFactory.getInstance().getItemService().update(context, item);
	}

	@Override
	protected String generateMetadataValue(NotifyLDNDTO ldnRequestDTO) {
		// setting up coar.notify.review metadata

		StringBuilder builder = new StringBuilder();

		String timestamp = new SimpleDateFormat(LDNUtils.DATE_PATTERN).format(Calendar.getInstance().getTime());
		String reviewServiceId = ldnRequestDTO.getOrigin().parseIdWithRemovedProtocol();
		String repositoryInitializedMessageId = ldnRequestDTO.getInReplyTo();
		String linkToTheReview = ldnRequestDTO.getObject().getId();

		builder.append(timestamp);
		builder.append(LDNUtils.METADATA_DELIMITER);

		builder.append(reviewServiceId);
		builder.append(LDNUtils.METADATA_DELIMITER);

		builder.append(repositoryInitializedMessageId);
		builder.append(LDNUtils.METADATA_DELIMITER);

		builder.append(linkToTheReview);

		logger.info(builder.toString());

		return builder.toString();
	}

}
