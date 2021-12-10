package org.dspace.notify;

import org.apache.log4j.Logger;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.eperson.EPersonConsumer;
import org.dspace.event.Consumer;
import org.dspace.event.Event;
import org.dspace.identifier.DOIIdentifierProvider;
import org.dspace.identifier.IdentifierException;
import org.dspace.identifier.IdentifierNotFoundException;
import org.dspace.utils.DSpace;

public class NotifyConsumer implements Consumer {

	/** log4j logger */
	private static Logger log = Logger.getLogger(NotifyConsumer.class);

	@Override
	public void initialize() throws Exception {

	}

	@Override
	public void consume(Context ctx, Event event) throws Exception {
		if (event.getSubjectType() == Constants.ITEM) {

            Item item = (Item)event.getSubject(ctx);
            
            //NotifyBusinessDelegate will reach out the ldn-inbox to notify 
            new NotifyBusinessDelegate().setInitializeMetadataForItem(item);
		}
	}

	@Override
	public void end(Context ctx) throws Exception {

	}

	@Override
	public void finish(Context ctx) throws Exception {

	}

}
