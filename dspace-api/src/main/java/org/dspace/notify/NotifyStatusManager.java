package org.dspace.notify;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.dspace.content.Item;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.ldn.LDNUtils;
import org.dspace.storage.rdbms.DatabaseManager;
import org.dspace.storage.rdbms.TableRow;
import org.dspace.storage.rdbms.TableRowIterator;

public class NotifyStatusManager {
	/** log4j category */
	private static Logger log = Logger.getLogger(NotifyStatusManager.class);

	public static final String SELECT_WITH_WHERE_PLACEHOLDER = "SELECT item.item_id as item, metadatafieldregistry.qualifier, metadatavalue.text_value, item.last_modified\n"
			+ "	FROM public.metadatafieldregistry JOIN public.metadatavalue ON metadatavalue.metadata_field_id = metadatafieldregistry.metadata_field_id\n"
			+ "	JOIN public.metadataschemaregistry ON metadataschemaregistry.metadata_schema_id = metadatafieldregistry.metadata_schema_id\n"
			+ "	JOIN public.item ON metadatavalue.resource_id = item.item_id\n"
			+ "	JOIN handle ON handle.resource_id = item.item_id\n"
			+ "	where metadataschemaregistry.short_id = 'coar' and element = 'notify' and [[WHERE_CLAUSE]]"
			+ "	GROUP BY item.item_id, metadatafieldregistry.qualifier, metadatavalue.text_value\n"
			+ "	ORDER BY item.last_modified DESC";

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

		for (NotifyStatus status : NotifyStatus.values()) {
			try {
				itemForEachStatus.put(status, getItemListForStatus(status));
			} catch (SQLException e) {
				log.error("Error while retrieving items for status", e);
			}
		}

		return itemForEachStatus;
	}

	private static List<Item> getItemsInEndorsed(List<Item> itemsInStatus, Context context) {
		String whereClause = "metadatafieldregistry.qualifier = 'endorsement'";
		return retrieveItems(itemsInStatus, context, whereClause);
	}

	private static List<Item> getItemsInPendingEndorsement(List<Item> itemsInStatus, Context context) {
		// check if the request is sent to one of the Endorsement Services
		String whereClause = "metadatafieldregistry.qualifier = 'request' AND "
				+ generateWhereForServicesMatch("endorsement");
		return retrieveItems(itemsInStatus, context, whereClause);
	}

	private static List<Item> getItemsInReviewed(List<Item> itemsInStatus, Context context) {
		String whereClause = "metadatafieldregistry.qualifier = 'review'";
		return retrieveItems(itemsInStatus, context, whereClause);
	}

	private static List<Item> getItemsInOngoing(List<Item> itemsInStatus, Context context) {
		String whereClause = "metadatafieldregistry.qualifier = 'examination'";
		return retrieveItems(itemsInStatus, context, whereClause);
	}

	private static List<Item> getItemsInPendingReview(List<Item> itemsInStatus, Context context) {
		// check if the request is sent to one of the Review Services
		String whereClause = "metadatafieldregistry.qualifier = 'request' AND "
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
		StringBuilder builder = new StringBuilder("(");

		String serviceEndpoint = "";
		String[] services;
		if (serviceType.equals("review")) {
			serviceEndpoint = ConfigurationManager.getProperty("ldn-trusted-services", "review.service-id.ldn");
		} else if (serviceType.equals("endorsement")) {
			serviceEndpoint = ConfigurationManager.getProperty("ldn-trusted-services", "endorsement.service-id.ldn");
		}
		services = serviceEndpoint.split(",");
		for (int i = 0; i < services.length; i++) {
			if (i != 0 && i != services.length - 1)
				builder.append(" OR ");
			builder.append("metadatavalue.text_value like '%" + LDNUtils.METADATA_DELIMITER + services[i].trim()
					+ LDNUtils.METADATA_DELIMITER + "%'");
		}
		builder.append(")");
		return builder.toString();
	}
}
