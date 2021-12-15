package org.dspace.ldn;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dspace.content.Item;
import org.dspace.content.Metadatum;

import static org.dspace.ldn.LDNMetadataFields.ELEMENT;
import static org.dspace.ldn.LDNMetadataFields.SCHEMA;
import static org.dspace.ldn.LDNMetadataFields.REQUEST;

public class LDNUtils {

	public final static String DATE_PATTERN = "yyyy-mm-dd HH:MM:SSZ";
	public final static String METADATA_DELIMITER = "||";

	private static Pattern handleRegexMatch = Pattern.compile("\\d{1,}\\/\\d{1,}");

	private static HashMap<String, String> notifyStatuses;

	static {
		notifyStatuses = new HashMap<>();
		notifyStatuses.put("coar.notify.initialize", "initialized");
		notifyStatuses.put("coar.notify.request", "pending review");
		notifyStatuses.put("coar.notify.examination", "ongoing");
		notifyStatuses.put("coar.notify.refused", "refused");
		notifyStatuses.put("coar.notify.review", "reviewed");
		notifyStatuses.put("coar.notify.endorsement", "endorsed");
	}

	public static String getHandleFromURL(String url) {
		Matcher matcher = handleRegexMatch.matcher(url);
		StringBuilder handle = new StringBuilder();
		if (matcher.find()) {
			handle.append(matcher.group(0));
		}
		return handle.toString();
	}

	public static boolean deleteMetadataByValue(Item item, String schema, String element, String qualifier,
			String[] valueIdentifiers) {
		Metadatum[] ar = null;

		ar = item.getMetadata(schema, element, qualifier, Item.ANY);

		boolean found = false;

		// build new set minus the one to delete
		List<String> vals = new ArrayList<String>();
		for (Metadatum dcv : ar) {
			if (metadataValueContainsAll(dcv, valueIdentifiers)) {
				// this metadata will be deleted
				found = true;
			} else {
				// save metadata to restore them later
				vals.add(dcv.value);
			}
		}

		// if not found we don't need to remove any metadata
		if (found) // remove all for given type
		{

			item.clearMetadata(schema, element, qualifier, Item.ANY);

			item.addMetadata(schema, element, qualifier, null, vals.toArray(new String[vals.size()]));
		}
		return found;
	}

	private static boolean metadataValueContainsAll(Metadatum metadatum, String[] identifiers) {

		for (String identifier : identifiers) {
			if (!metadatum.value.contains(identifier)) {
				return false;
			}
		}
		return true;
	}

	public static void saveMetadataRequestForItem(Item item, String endpointId, String repositoryMessageID) {
		item.addMetadata(SCHEMA, ELEMENT, REQUEST, null,
				generateMetadataValueForRequestQualifier(endpointId, repositoryMessageID));
	}

	private static String generateMetadataValueForRequestQualifier(String endpointId, String repositoryMessageID) {
		// coar.notify.request
		StringBuilder builder = new StringBuilder();

		String timestamp = new SimpleDateFormat(DATE_PATTERN).format(Calendar.getInstance().getTime());

		builder.append(timestamp);
		builder.append(METADATA_DELIMITER);

		builder.append(endpointId);
		builder.append(METADATA_DELIMITER);

		builder.append(repositoryMessageID);

		return builder.toString();
	}

	public static String generateRandomUrnUUID() {
		return "urn:uuid:" + UUID.randomUUID().toString();
	}

	public static String getNotifyStatusFromMetadata(String metadata) {
		String status = notifyStatuses.get(metadata);
		return status != null ? status : "";
	}

}
