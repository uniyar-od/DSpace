package org.dspace.app.webui.ldn;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.dspace.core.ConfigurationManager;

public class LDNServiceCheckAuthorization {

	private static List<String> authorizedIps;

	static {
		String authorisedIpsString = ConfigurationManager.getProperty("ldn-trusted-services", "ldn-trusted.from.ip");
		
		List<String> tmpList = Collections.emptyList();
		
		if (StringUtils.isNotEmpty(authorisedIpsString)) {
			tmpList = Arrays.asList(authorisedIpsString.split(","));
		}
		
		authorizedIps = tmpList;
	}

	public static boolean checkIfHostIsAuthorized(HttpServletRequest request) {
		String ipFromRequest=request.getRemoteAddr();
		return authorizedIps.contains(ipFromRequest);
	}

}
