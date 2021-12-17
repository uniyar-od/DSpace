package org.dspace.notify;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.dspace.content.Item;
import org.dspace.content.Metadatum;
import org.dspace.core.Context;
import org.dspace.ldn.LDNMetadataFields;
import org.dspace.ldn.LDNUtils;
import org.dspace.storage.rdbms.DatabaseManager;
import org.dspace.storage.rdbms.TableRow;
import org.dspace.storage.rdbms.TableRowIterator;

import com.google.common.base.Joiner;

public class NotifyStatusManager {
	/** log4j category */
	private static Logger log = Logger.getLogger(NotifyStatusManager.class);

	public static final String SELECT_WITH_WHERE_PLACEHOLDER = "SELECT item.item_id as item, metadatafieldregistry.qualifier, item.last_modified\n"
			+ "	FROM public.metadatafieldregistry JOIN public.metadatavalue ON metadatavalue.metadata_field_id = metadatafieldregistry.metadata_field_id\n"
			+ "	JOIN public.metadataschemaregistry ON metadataschemaregistry.metadata_schema_id = metadatafieldregistry.metadata_schema_id\n"
			+ "	JOIN public.item ON metadatavalue.resource_id = item.item_id\n"
			+ "	where metadataschemaregistry.short_id = 'coar' and element = 'notify' and [[WHERE_CLAUSE]]"
			+ "	GROUP BY item.item_id, metadatafieldregistry.qualifier\n" + "	ORDER BY item.last_modified DESC";

	public static List<Item> getItemListForStatus(NotifyStatus status) throws SQLException {
		List<Item> itemsInStatus = new LinkedList<>();
		Context context = new Context();
		switch (status) {
		case PENDING_REVIEW:
			return getItemsInPendingReview(itemsInStatus, context);
		case ONGOING:
			return getItemsInOngoing(itemsInStatus, context);
		case REVIEWED:
			return getItemsInReviewed(itemsInStatus, context);
		case PENDING_ENDORSEMENT:
			return getItemsInPendingEndorsement(itemsInStatus, context);
		case ENDORSED:
			return getItemsInEndorsed(itemsInStatus, context);
		default:
			return itemsInStatus;
		}
	}

	public static HashMap<NotifyStatus, List<Item>> getItemsForEachNotifyStatus() {
		HashMap<NotifyStatus, List<Item>> itemForEachStatus = new HashMap<>();

		for (NotifyStatus status : NotifyStatus.getOrderedValues()) {
			try {
				itemForEachStatus.put(status, getItemListForStatus(status));
			} catch (SQLException e) {
				log.error("Error while retrieving items for status", e);
			}
		}

		return itemForEachStatus;
	}

	public static List<NotifyStatus> getNotifyStatusForItem(Item filteringItem) {
		HashMap<NotifyStatus, List<Item>> itemForEachStatus = new HashMap<>();
		itemForEachStatus = getItemsForEachNotifyStatus();

		List<NotifyStatus> statuses = new LinkedList();
		for (Entry<NotifyStatus, List<Item>> entry : itemForEachStatus.entrySet()) {
			if (entry.getValue().contains(filteringItem)) {
				// then the item is found in that status
				statuses.add(entry.getKey());
			}
		}

		return statuses;
	}

	public static String[] getMetadataValueFor(NotifyStatus itemStatus, Item item) {
		String[] values;
		Metadatum[] metadatum;

		HashMap<String, Metadatum> uniqueTextValue = new HashMap<>();

		// Remove duplicate Metadata for each qualifier if there is any
		// filtering any duplicated value with an hashmap
		metadatum = item.getMetadata(LDNMetadataFields.SCHEMA, LDNMetadataFields.ELEMENT,
				itemStatus.getQualifierForNotifyStatus(), LDNUtils.getDefaultLanguageQualifier());
		for (Metadatum tmpMetadatum : metadatum) {
			uniqueTextValue.put(tmpMetadatum.value, tmpMetadatum);
		}
		// If status is PENDING_ENDORSEMENT or PENDING_REVIEW the associated metadata is
		// the same and we need to filter by service identifiers
		if (itemStatus.equals(NotifyStatus.PENDING_ENDORSEMENT) || itemStatus.equals(NotifyStatus.PENDING_REVIEW)) {

			String[] services = LDNUtils.getServicesForServiceType(
					itemStatus.equals(NotifyStatus.PENDING_ENDORSEMENT) ? "endorsement" : "review");
			List<String> matchingMetadataValue = new LinkedList<>();
			for (String metadataTextValue : uniqueTextValue.keySet()) {
				for (String service : services) {
					if (metadataTextValue
							.contains(LDNUtils.METADATA_DELIMITER + service + LDNUtils.METADATA_DELIMITER)) {
						// there is a match with the service we are looking for
						matchingMetadataValue.add(metadataTextValue);
					}
				}
			}
			values = new String[matchingMetadataValue.size()];
			return matchingMetadataValue.toArray(values);
		} else {
			values = new String[uniqueTextValue.keySet().size()];
			return uniqueTextValue.keySet().toArray(values);
		}

	}

	private static List<Item> getItemsInEndorsed(List<Item> itemsInStatus, Context context) {
		String whereClause = "metadatafieldregistry.qualifier = '" + NotifyStatus.ENDORSED.getQualifierForNotifyStatus()
				+ "'";
		return retrieveItems(itemsInStatus, context, whereClause);
	}

	private static List<Item> getItemsInPendingEndorsement(List<Item> itemsInStatus, Context context) {
		// check if the request is sent to one of the Endorsement Services
		String whereClause = "metadatafieldregistry.qualifier = '"
				+ NotifyStatus.PENDING_ENDORSEMENT.getQualifierForNotifyStatus() + "' AND "
				+ generateWhereForServicesMatch("endorsement");
		return retrieveItems(itemsInStatus, context, whereClause);
	}

	private static List<Item> getItemsInReviewed(List<Item> itemsInStatus, Context context) {
		String whereClause = "metadatafieldregistry.qualifier = '" + NotifyStatus.REVIEWED.getQualifierForNotifyStatus()
				+ "'";
		return retrieveItems(itemsInStatus, context, whereClause);
	}

	private static List<Item> getItemsInOngoing(List<Item> itemsInStatus, Context context) {
		String whereClause = "metadatafieldregistry.qualifier = '" + NotifyStatus.ONGOING.getQualifierForNotifyStatus()
				+ "'";
		return retrieveItems(itemsInStatus, context, whereClause);
	}

	private static List<Item> getItemsInPendingReview(List<Item> itemsInStatus, Context context) {
		// check if the request is sent to one of the Review Services
		String whereClause = "metadatafieldregistry.qualifier = '"
				+ NotifyStatus.PENDING_REVIEW.getQualifierForNotifyStatus() + "' AND "
				+ generateWhereForServicesMatch("review");
		return retrieveItems(itemsInStatus, context, whereClause);
	}

	private static List<Item> retrieveItems(List<Item> itemsInStatus, Context context, String whereClause) {
		int itemId;
		TableRow row;
		TableRowIterator tableRowIterator = null;
		String localQuery = SELECT_WITH_WHERE_PLACEHOLDER.replace("[[WHERE_CLAUSE]]", whereClause);
		try {
			tableRowIterator = DatabaseManager.query(context, localQuery);
			while (tableRowIterator.hasNext()) {
				row = tableRowIterator.next();
				itemId = row.getIntColumn("item");
				itemsInStatus.add(Item.find(context, itemId));
			}
		} catch (SQLException e) {
			log.error(e);
		} finally {
			if (tableRowIterator != null) {
				tableRowIterator.close();
			}
		}
		return itemsInStatus;
	}

	private static String generateWhereForServicesMatch(String serviceType) {

		String[] services = LDNUtils.getServicesForServiceType(serviceType);
		for (int i = 0; i < services.length; i++) {
			services[i] = "metadatavalue.text_value like '%" + LDNUtils.METADATA_DELIMITER + services[i]
					+ LDNUtils.METADATA_DELIMITER + "%'";
		}

		return "(" + Joiner.on(" OR ").skipNulls().join(Arrays.asList(services)) + ")";
	}

}
