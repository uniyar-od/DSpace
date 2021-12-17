package org.dspace.ldn;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Item;
import org.dspace.content.Metadatum;
import org.dspace.core.ConfigurationManager;

import static org.dspace.ldn.LDNMetadataFields.ELEMENT;
import static org.dspace.ldn.LDNMetadataFields.SCHEMA;
import static org.dspace.ldn.LDNMetadataFields.REQUEST;

public class LDNUtils {
	/** Logger */
	private static Logger logger = Logger.getLogger(LDNUtils.class);

	public final static String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";
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
			String[] valueIdentifiers) throws SQLException, AuthorizeException {
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
		item.update();
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

	public static void saveMetadataRequestForItem(Item item, String serviceId, String repositoryMessageID)
			throws SQLException, AuthorizeException {

		boolean removed = LDNUtils.removeMetadata(item, SCHEMA, ELEMENT, new String[] { LDNMetadataFields.INITIALIZE },
				new String[] { serviceId });
		if (removed) {
			item.addMetadata(SCHEMA, ELEMENT, REQUEST, getDefaultLanguageQualifier(),
					generateMetadataValueForRequestQualifier(serviceId, repositoryMessageID));
			item.update();
		}
	}

	private static String generateMetadataValueForRequestQualifier(String serviceId, String repositoryMessageID) {
		// coar.notify.request
		StringBuilder builder = new StringBuilder();

		String timestamp = new SimpleDateFormat(DATE_PATTERN).format(Calendar.getInstance().getTime());

		builder.append(timestamp);
		builder.append(METADATA_DELIMITER);

		builder.append(serviceId);
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

	public static String getDefaultLanguageQualifier() {
		return Item.ANY;
	}

	public static final boolean removeMetadata(Item item, String schema, String element, String qualifier,
			String value) {
		return removeMetadata(item, schema, element, qualifier, new String[] { value });
	}

	public static final boolean removeMetadata(Item item, String schema, String element, String[] qualifiers,
			String value) {
		boolean anyOfThem = false;
		for (String qualifier : qualifiers)
			anyOfThem = anyOfThem || removeMetadata(item, schema, element, qualifier, value);
		return anyOfThem;
	}

	public static final boolean removeMetadata(Item item, String schema, String element, String qualifier,
			String[] identifiers) {
		try {
			return LDNUtils.deleteMetadataByValue(item, schema, element, qualifier, identifiers);
		} catch (Exception e) {
			logger.error("An error occurred while deleting metadata", e);
		}
		return false;
	}

	public static final boolean removeMetadata(Item item, String schema, String element, String[] qualifiers,
			String[] identifiers) {
		boolean anyOfThem = false;
		for (String qualifier : qualifiers)
			anyOfThem = anyOfThem || removeMetadata(item, schema, element, qualifier, identifiers);
		return anyOfThem;
	}

	public static String[] getServicesForServiceType(String serviceType) {
		String serviceEndpoint = "";
		String[] services;
		if (serviceType.equals("review")) {
			serviceEndpoint = ConfigurationManager.getProperty("ldn-trusted-services", "review.service-id.ldn");
		} else if (serviceType.equals("endorsement")) {
			serviceEndpoint = ConfigurationManager.getProperty("ldn-trusted-services", "endorsement.service-id.ldn");
		}
		services = serviceEndpoint.split(",");
		for (int i = 0; i < services.length; i++) {
			services[i] = services[i].trim();
		}
		return services;
	}

	public static HashMap<String, String> getServicesAndNames() {
		HashMap<String, String> map = new HashMap<String, String>();
		for (String serviceID : getServicesForServiceType("review")) {
			map.put(serviceID,
					ConfigurationManager.getProperty("ldn-trusted-services", "review." + serviceID + ".name"));
		}
		for (String serviceID : getServicesForServiceType("endorsement")) {
			map.put(serviceID,
					ConfigurationManager.getProperty("ldn-trusted-services", "endorsement." + serviceID + ".name"));
		}

		return map;
	}
}
