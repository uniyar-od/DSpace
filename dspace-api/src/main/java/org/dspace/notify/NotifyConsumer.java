package org.dspace.notify;

import org.apache.log4j.Logger;
import org.dspace.content.Item;
import org.dspace.content.Metadatum;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.event.Consumer;
import org.dspace.event.Event;
import org.dspace.ldn.LDNUtils;
import static org.dspace.ldn.LDNMetadataFields.ELEMENT;
import static org.dspace.ldn.LDNMetadataFields.SCHEMA;
import static org.dspace.ldn.LDNMetadataFields.INITIALIZE;

public class NotifyConsumer implements Consumer {

	/** log4j logger */
	private static Logger log = Logger.getLogger(NotifyConsumer.class);

	@Override
	public void initialize() throws Exception {

	}

	@Override
	public void consume(Context ctx, Event event) throws Exception {
		if (event.getSubjectType() == Constants.ITEM) {

			Item item = (Item) event.getSubject(ctx);
			Metadatum[] ar = null;

			ar = item.getMetadata(SCHEMA, ELEMENT, INITIALIZE, Item.ANY);

			// Each metadata coar.notify.initialize is a different service to reach
			// NotifyBusinessDelegate will reach out the service to notify
			String repositoryMessageIdInitialize;
			for (Metadatum metadatum : ar) {
				repositoryMessageIdInitialize = LDNUtils.generateRandomUrnUUID();
				new NotifyBusinessDelegate().reachEndpoitToRequestReview(item, metadatum.value);
			}

		}
	}

	@Override
	public void end(Context ctx) throws Exception {

	}

	@Override
	public void finish(Context ctx) throws Exception {

	}

}
