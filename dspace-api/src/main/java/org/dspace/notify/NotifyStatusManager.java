package org.dspace.notify;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.Metadatum;
import org.dspace.core.Context;
import org.dspace.discovery.DiscoverQuery;
import org.dspace.discovery.DiscoverQuery.SORT_ORDER;
import org.dspace.discovery.DiscoverResult;
import org.dspace.discovery.SearchService;
import org.dspace.discovery.SearchServiceException;
import org.dspace.kernel.ServiceManager;
import org.dspace.ldn.LDNMetadataFields;
import org.dspace.ldn.LDNUtils;
import org.dspace.utils.DSpace;

public class NotifyStatusManager {
	/** log4j category */
	private static Logger log = Logger.getLogger(NotifyStatusManager.class);

	public static List<Item> getItemListForStatus(NotifyStatus status) throws SQLException {
		Context context = new Context();
		switch (status) {
		case PENDING_REVIEW:
			return getItemsInPendingReview(context);
		case ONGOING:
			return getItemsInOngoing(context);
		case REVIEWED:
			return getItemsInReviewed(context);
		case PENDING_ENDORSEMENT:
			return getItemsInPendingEndorsement(context);
		case ENDORSED:
			return getItemsInEndorsed(context);
		default:
			context.abort();
			return Collections.emptyList();
		}
		
	}

	public static LinkedHashMap<NotifyStatus, List<Item>> getItemsForEachNotifyStatus() {
		LinkedHashMap<NotifyStatus, List<Item>> itemForEachStatus = new LinkedHashMap<>();

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

		List<NotifyStatus> statuses = new LinkedList<>();
		for (Entry<NotifyStatus, List<Item>> entry : itemForEachStatus.entrySet()) {
			if (entry.getValue().contains(filteringItem)) {
				// then the item is found in that status
				statuses.add(entry.getKey());
			}
		}

		return statuses;
	}

	public static String[] getMetadataValueFor(NotifyStatus itemStatus, Item item) {
		Metadatum[] metadatum;
		String[] metadataValues;
		metadatum = item.getMetadata(LDNMetadataFields.SCHEMA, LDNMetadataFields.ELEMENT,
				itemStatus.getQualifierForNotifyStatus(), LDNUtils.getDefaultLanguageQualifier());
		metadataValues = new String[metadatum.length];

		for (int i = 0; i < metadataValues.length; i++) {
			metadataValues[i] = metadatum[i].value;
		}

		return metadataValues;

	}

	private static List<Item> getItemsInEndorsed(Context context) {
		return getItemsInSolr(context,NotifyStatus.ENDORSED.getQualifierForNotifyStatus());
	}

	private static List<Item> getItemsInPendingEndorsement(Context context) {
		return getItemsInSolr(context,NotifyStatus.PENDING_ENDORSEMENT.getQualifierForNotifyStatus());
	}

	private static List<Item> getItemsInReviewed(Context context) {
		return getItemsInSolr(context,NotifyStatus.REVIEWED.getQualifierForNotifyStatus());
	}

	private static List<Item> getItemsInOngoing(Context context) {
		return getItemsInSolr(context,NotifyStatus.ONGOING.getQualifierForNotifyStatus());
	}

	private static List<Item> getItemsInPendingReview(Context context) {
		return getItemsInSolr(context,NotifyStatus.PENDING_REVIEW.getQualifierForNotifyStatus());
	}

	private static List<Item> getItemsInSolr(Context context, String qualifier) {
		List<Item> itemsInStatus = new LinkedList<>();
		try {
			ServiceManager manager = new DSpace().getServiceManager();
			SearchService searchService = manager.getServiceByName(SearchService.class.getName(), SearchService.class);

			DiscoverQuery query = new DiscoverQuery();
			
			query.setQuery("*:*");
			query.addFilterQueries(LDNMetadataFields.SCHEMA+"."+LDNMetadataFields.ELEMENT+"."+qualifier+":[* TO *]");
			query.addFilterQueries("search.resourcetype:2");
			query.setSortField("SolrIndexer.lastIndexed", SORT_ORDER.desc);
			
			query.setMaxResults(Integer.MAX_VALUE);

			DiscoverResult discoveryResult;
			discoveryResult = searchService.search(context, query);

			List<DSpaceObject> resultDSOs = discoveryResult.getDspaceObjects();
			for (DSpaceObject dso : resultDSOs) {
				if (dso != null) {
					itemsInStatus.add((Item)dso);
				}
			}
		} catch (SearchServiceException e) {
			log.error(e);
		} finally {
			context.abort();
		}
		return itemsInStatus;
	}
}
