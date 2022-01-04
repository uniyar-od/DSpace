package org.dspace.ldn;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
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
import org.dspace.app.util.Util;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.Item;
import org.dspace.content.Metadatum;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.notify.NotifyStatus;

import static org.dspace.ldn.LDNMetadataFields.ELEMENT;
import static org.dspace.ldn.LDNMetadataFields.SCHEMA;
import static org.dspace.ldn.LDNMetadataFields.REQUEST_REVIEW;
import static org.dspace.ldn.LDNMetadataFields.REQUEST_ENDORSEMENT;

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

	public static void saveMetadataRequestForItem(Item item, String serviceId, String repositoryMessageID,
			boolean isEndorsementSupported) throws SQLException, AuthorizeException {

		boolean removed = LDNUtils.removeMetadata(item, SCHEMA, ELEMENT, new String[] { LDNMetadataFields.INITIALIZE },
				new String[] { serviceId });
		if (removed) {
			item.addMetadata(SCHEMA, ELEMENT, REQUEST_REVIEW, getDefaultLanguageQualifier(),
					generateMetadataValueForRequestQualifier(serviceId, repositoryMessageID));
			if (isEndorsementSupported) {
				item.addMetadata(SCHEMA, ELEMENT, REQUEST_ENDORSEMENT, getDefaultLanguageQualifier(),
						generateMetadataValueForRequestQualifier(serviceId, repositoryMessageID));
			}
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

	public static String[] getServicesForReviewEndorsement() {
		String serviceEndpoint = "";
		String[] services;
		serviceEndpoint = ConfigurationManager.getProperty("ldn-coar-notify", "service.service-id.ldn");
		services = serviceEndpoint.split(",");
		for (int i = 0; i < services.length; i++) {
			services[i] = services[i].trim();
		}
		return services;
	}

	public static HashMap<String, String> getServicesAndNames() {
		HashMap<String, String> map = new HashMap<String, String>();
		for (String serviceID : getServicesForReviewEndorsement()) {
			map.put(serviceID, ConfigurationManager.getProperty("ldn-coar-notify", "service." + serviceID + ".name"));
		}

		return map;
	}

	private static boolean isPublic(Context context, Bitstream bitstream) {
		if (bitstream == null) {
			return false;
		}
		boolean result = false;
		try {
			result = AuthorizeManager.authorizeActionBoolean(context, bitstream, Constants.READ, true);
		} catch (SQLException e) {
			logger.error("Cannot determine whether bitstream is public, assuming it isn't. bitstream_id="
					+ bitstream.getID(), e);
		}
		return result;
	}

	/**
	 * A bitstream is considered linkable fulltext when it is either
	 * <ul>
	 * <li>the item's only bitstream (in the ORIGINAL bundle); or</li>
	 * <li>the primary bitstream</li>
	 * </ul>
	 * Additionally, this bitstream must be publicly viewable.
	 * 
	 * Copy of the method in org.dspace.app.util.GoogleMetadata to avoid changing
	 * type visibility
	 * 
	 * @param item
	 * @return
	 * @throws SQLException
	 */
	private static Bitstream findLinkableFulltext(Context context, Item item) throws SQLException {
		Bitstream bestSoFar = null;
		Bundle[] contentBundles = item.getBundles("ORIGINAL");
		for (Bundle bundle : contentBundles) {
			int primaryBitstreamId = bundle.getPrimaryBitstreamID();
			Bitstream[] bitstreams = bundle.getBitstreams();
			for (Bitstream candidate : bitstreams) {
				if (candidate.getID() == primaryBitstreamId) { // is primary -> use this one
					if (isPublic(context, candidate)) {
						return candidate;
					}
				} else {

					if (bestSoFar == null && isPublic(context, candidate)) { // if bestSoFar is null but the candidate
																				// is not public you don't use it and
																				// try to find another
						bestSoFar = candidate;
					}
				}
			}
		}

		return bestSoFar;
	}

	/**
	 * Gets the URL to a PDF using a very basic strategy by assuming that the PDF is
	 * in the default content bundle, and that the item only has one public
	 * bitstream and it is a PDF.
	 * 
	 * Copy of the method in org.dspace.app.util.GoogleMetadata to avoid changing
	 * type visibility
	 *
	 * @param item
	 * @return URL that the PDF can be directly downloaded from
	 */
	public static String getPDFSimpleUrl(Context context, Item item) {
		try {
			Bitstream bitstream = findLinkableFulltext(context, item);
			if (bitstream != null) {
				StringBuilder path = new StringBuilder();
				path.append(ConfigurationManager.getProperty("dspace.url"));

				if (item.getHandle() != null) {
					path.append("/bitstream/");
					path.append(item.getHandle());
					path.append("/");
					path.append(bitstream.getSequenceID());
				} else {
					path.append("/retrieve/");
					path.append(bitstream.getID());
				}

				path.append("/");
				path.append(Util.encodeBitstreamName(bitstream.getName(), Constants.DEFAULT_ENCODING));
				return path.toString();
			}
		} catch (UnsupportedEncodingException ex) {
			logger.debug(ex.getMessage());
		} catch (SQLException ex) {
			logger.debug(ex.getMessage());
		}

		return "";
	}

	public static String retrieveMimeTypeFromFilePath(String stringPath) {
		String mimeType = "";
		try {
			Path path = new File(stringPath).toPath();
			mimeType = Files.probeContentType(path);
		} catch (Exception e) {
			logger.error(e);
		}
		return mimeType;
	}

	public static String[][] getNotifyMetadataValueFromStatus(Item item, NotifyStatus notifyStatus) {
		Metadatum[] metadatum = item.getMetadata(SCHEMA, ELEMENT, notifyStatus.getQualifierForNotifyStatus(),
				getDefaultLanguageQualifier());
		String[][] metadataMatrix = new String[metadatum.length][];
		String[] splitted;
		for (int i = 0; i < metadataMatrix.length; i++) {
			splitted = metadatum[i].value.split(Pattern.quote(METADATA_DELIMITER));
			metadataMatrix[i] = splitted;
		}
		return metadataMatrix;
	}
}
