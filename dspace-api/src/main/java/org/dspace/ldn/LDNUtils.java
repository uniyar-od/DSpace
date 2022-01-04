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
import java.util.ListIterator;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.dspace.app.util.Util;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.factory.AuthorizeServiceFactory;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.Item;
import org.dspace.content.MetadataValue;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.notify.NotifyStatus;
import org.dspace.services.factory.DSpaceServicesFactory;

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

	public static boolean deleteMetadataByValue(Context context, Item item, String schema, String element,
			String qualifier, String[] valueIdentifiers) throws SQLException, AuthorizeException {
		List<MetadataValue> ar = null;

		ar = ContentServiceFactory.getInstance().getItemService().getMetadata(item, schema, element, qualifier,
				Item.ANY);

		boolean found = false;

		// build new set minus the one to delete
		List<String> vals = new ArrayList<String>();
		for (MetadataValue dcv : ar) {
			if (metadataValueContainsAll(dcv, valueIdentifiers)) {
				// this metadata will be deleted
				found = true;
			} else {
				// save metadata to restore them later
				vals.add(dcv.getValue());
			}
		}

		// if not found we don't need to remove any metadata
		if (found) // remove all for given type
		{

			ContentServiceFactory.getInstance().getItemService().clearMetadata(context, item, schema, element,
					qualifier, Item.ANY);

			ContentServiceFactory.getInstance().getItemService().addMetadata(context, item, schema, element, qualifier,
					Item.ANY, vals);
		}
		ContentServiceFactory.getInstance().getItemService().update(context, item);
		return found;
	}

	private static boolean metadataValueContainsAll(MetadataValue metadatum, String[] identifiers) {

		for (String identifier : identifiers) {
			if (!metadatum.getValue().contains(identifier)) {
				return false;
			}
		}
		return true;
	}

	public static void saveMetadataRequestForItem(Context context, Item item, String serviceId,
			String repositoryMessageID, boolean isEndorsementSupported) throws SQLException, AuthorizeException {

		boolean removed = LDNUtils.removeMetadata(context, item, SCHEMA, ELEMENT,
				new String[] { LDNMetadataFields.INITIALIZE }, new String[] { serviceId });
		if (removed) {
			ContentServiceFactory.getInstance().getItemService().addMetadata(context, item, SCHEMA, ELEMENT,
					REQUEST_REVIEW, getDefaultLanguageQualifier(),
					generateMetadataValueForRequestQualifier(serviceId, repositoryMessageID));
			if (isEndorsementSupported) {
				ContentServiceFactory.getInstance().getItemService().addMetadata(context, item, SCHEMA, ELEMENT,
						REQUEST_ENDORSEMENT, getDefaultLanguageQualifier(),
						generateMetadataValueForRequestQualifier(serviceId, repositoryMessageID));
			}
			ContentServiceFactory.getInstance().getItemService().update(context, item);
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

	public static final boolean removeMetadata(Context context, Item item, String schema, String element,
			String qualifier, String value) {
		return removeMetadata(context, item, schema, element, qualifier, new String[] { value });
	}

	public static final boolean removeMetadata(Context context, Item item, String schema, String element,
			String[] qualifiers, String value) {
		boolean anyOfThem = false;
		for (String qualifier : qualifiers)
			anyOfThem = anyOfThem || removeMetadata(context, item, schema, element, qualifier, value);
		return anyOfThem;
	}

	public static final boolean removeMetadata(Context context, Item item, String schema, String element,
			String qualifier, String[] identifiers) {
		try {
			return LDNUtils.deleteMetadataByValue(context, item, schema, element, qualifier, identifiers);
		} catch (Exception e) {
			logger.error("An error occurred while deleting metadata", e);
		}
		return false;
	}

	public static final boolean removeMetadata(Context context, Item item, String schema, String element,
			String[] qualifiers, String[] identifiers) {
		boolean anyOfThem = false;
		for (String qualifier : qualifiers)
			anyOfThem = anyOfThem || removeMetadata(context, item, schema, element, qualifier, identifiers);
		return anyOfThem;
	}

	public static String[] getServicesForReviewEndorsement() {
		String[] serviceEndpoint;
		serviceEndpoint = DSpaceServicesFactory.getInstance().getConfigurationService()
				.getArrayProperty("service.service-id.ldn");
		return serviceEndpoint;
	}

	public static HashMap<String, String> getServicesAndNames() {
		HashMap<String, String> map = new HashMap<String, String>();
		for (String serviceID : getServicesForReviewEndorsement()) {
			map.put(serviceID, DSpaceServicesFactory.getInstance().getConfigurationService()
					.getProperty("service." + serviceID + ".name"));
		}

		return map;
	}

	private static boolean isPublic(Bitstream bitstream) {
		if (bitstream == null) {
			return false;
		}
		boolean result = false;
		Context context = null;
		try {
			context = new Context();
			result = AuthorizeServiceFactory.getInstance().getAuthorizeService().authorizeActionBoolean(context,
					bitstream, Constants.READ, true);
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
	private static Bitstream findLinkableFulltext(Item item) throws SQLException {
		Bitstream bestSoFar = null;

		List<Bundle> contentBundles = ContentServiceFactory.getInstance().getItemService().getBundles(item, "ORIGINAL");

		for (Bundle bundle : contentBundles) {
			List<Bitstream> bitstreams = bundle.getBitstreams();

			for (Bitstream candidate : bitstreams) {
				if (candidate.equals(bundle.getPrimaryBitstream())) { // is primary -> use this one
					if (isPublic(candidate)) {
						return candidate;
					}
				} else {
					if (bestSoFar == null && isPublic(candidate)) { // if bestSoFar is null but the candidate is not
																	// public you don't use it and try to find another
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
	public static String getPDFSimpleUrl(Item item) {
		try {
			Bitstream bitstream = findLinkableFulltext(item);
			if (bitstream != null) {
				StringBuilder path = new StringBuilder();
				path.append(DSpaceServicesFactory.getInstance().getConfigurationService().getProperty("dspace.url"));

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
		List<MetadataValue> metadatum = ContentServiceFactory.getInstance().getItemService().getMetadata(item, SCHEMA,
				ELEMENT, notifyStatus.getQualifierForNotifyStatus(), getDefaultLanguageQualifier());
		String[][] metadataMatrix = new String[metadatum.size()][];
		String[] splitted;
		ListIterator<MetadataValue> iterator=metadatum.listIterator();
		for (int i = 0; i < metadataMatrix.length;i++) {
			splitted=iterator.next().getValue().split(Pattern.quote(METADATA_DELIMITER));
			metadataMatrix[i]=splitted;
		}
		return metadataMatrix;
	}
}
