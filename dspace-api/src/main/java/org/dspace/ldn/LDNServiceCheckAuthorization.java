package org.dspace.ldn;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.core.ConfigurationManager;

public class LDNServiceCheckAuthorization {
	/** Logger */
	private static Logger logger = Logger.getLogger(LDNServiceCheckAuthorization.class);

	private static List<String> authorizedIpList;

	static {
		String authorisedIpString = ConfigurationManager.getProperty("ldn-trusted-services", "ldn-trusted.from.ip");

		List<String> tmpList = new LinkedList<>();

			if (StringUtils.isNotEmpty(authorisedIpString)) {
				if (authorisedIpString.contains(",")) {
					String[] ipArray = authorisedIpString.split(",");
					for (String tmpIp : ipArray) {
						tmpList.add(tmpIp);
					}
				} else {
					tmpList.add(authorisedIpString);
				}
			}


		authorizedIpList = tmpList;
	}

	public static boolean isHostAuthorized(HttpServletRequest request) {
		String ipFromRequest = request.getRemoteAddr();
		InetAddress ip = null;
		try {
			ip = InetAddress.getByName(ipFromRequest);
			return authorizedIpList.contains(ipFromRequest) || ip.isLoopbackAddress();
		} catch (UnknownHostException e) {
			logger.error(e);
		}
		return authorizedIpList.contains(ipFromRequest);
	}

}
