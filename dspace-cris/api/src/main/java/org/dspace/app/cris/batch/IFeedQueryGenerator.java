package org.dspace.app.cris.batch;

import java.util.HashMap;
import java.util.List;

public interface IFeedQueryGenerator {
	
	public HashMap<Integer,List<String>> generate();

}
