package org.dspace.notify;

import org.apache.log4j.Logger;
import org.dspace.content.Item;
import org.dspace.content.MetadataValue;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.ItemService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.event.Consumer;
import org.dspace.event.Event;
import org.dspace.ldn.LDNUtils;
import static org.dspace.ldn.LDNMetadataFields.ELEMENT;
import static org.dspace.ldn.LDNMetadataFields.SCHEMA;

import java.util.List;

import static org.dspace.ldn.LDNMetadataFields.INITIALIZE;

public class NotifyConsumer implements Consumer {

	/** log4j logger */
	private static Logger log = Logger.getLogger(NotifyConsumer.class);
	private final transient ItemService itemService = ContentServiceFactory.getInstance().getItemService();

	@Override
	public void initialize() throws Exception {

	}

	@Override
	public void consume(Context ctx, Event event) throws Exception {
		if (event.getSubjectType() == Constants.ITEM) {
			ctx.turnOffAuthorisationSystem();
			Item item = (Item) event.getSubject(ctx);
			List<MetadataValue> ar = null;

			ar = itemService.getMetadata(item, SCHEMA, ELEMENT, INITIALIZE, Item.ANY);

			// Each metadata coar.notify.initialize is a different service to reach
			// NotifyBusinessDelegate will reach out the service to notify

			for (MetadataValue metadatum : ar) {
				new NotifyBusinessDelegate(ctx).askServicesForReviewEndorsement(ctx, item, metadatum.getValue());
			}
			ctx.restoreAuthSystemState();
			ctx.commit();

		}
	}

	@Override
	public void end(Context ctx) throws Exception {

	}

	@Override
	public void finish(Context ctx) throws Exception {

	}

}
