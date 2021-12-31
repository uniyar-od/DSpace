package org.dspace.ldn;


import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;
import org.dspace.core.Context;

public abstract class LDNAction {
	
	protected static Logger logger = Logger.getLogger(LDNAction.class);
	
	public abstract ActionStatus executeAction(Context context, NotifyLDNDTO ldnRequestDTO); 
	
}
