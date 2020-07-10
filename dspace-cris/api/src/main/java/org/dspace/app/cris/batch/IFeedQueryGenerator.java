package org.dspace.app.cris.batch;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.dspace.core.Context;

public interface IFeedQueryGenerator {
	
	public HashMap<UUID,List<String>> generate(Context context);

}
