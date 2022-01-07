package org.dspace.ldn;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.core.ConfigurationManager;
import org.dspace.notify.NotifyStatus;
import org.dspace.notify.NotifyStatusManager;

public class LDNServiceCheckAuthorization {
	/** Logger */
	private static Logger logger = Logger.getLogger(LDNServiceCheckAuthorization.class);

	private static List<String> authorizedIpList;
	
	private static List<String> authorizedServiceList;

	private static boolean isLocalhostTrustedByDefault;

	static {
		isLocalhostTrustedByDefault = ConfigurationManager.getBooleanProperty("ldn-coar-notify",
				"ldn-trusted.localhost.default");
		String authorisedIpString = ConfigurationManager.getProperty("ldn-coar-notify", "ldn-trusted.from.ip");
		String authorisedHostnameString = ConfigurationManager.getProperty("ldn-coar-notify","ldn-trusted.from.hostname");

		List<String> tmpList = new LinkedList<>();

		// Authorized IP Addresses
		if (StringUtils.isNotEmpty(authorisedIpString)) {
			if (authorisedIpString.contains(",")) {
				String[] ipArray = authorisedIpString.split(",");
				for (String tmpIp : ipArray) {
					tmpList.add(tmpIp);
					logger.info("Authorized ip "+tmpIp);
				}
			} else {
				tmpList.add(authorisedIpString);
			}
		}

		// Authorized Hostnames
		if (StringUtils.isNotEmpty(authorisedHostnameString)) {
			if (authorisedHostnameString.contains(",")) {
				String[] hostnameArray = authorisedHostnameString.split(",");
				String ipAddr;
				for (String hostname : hostnameArray) {
					ipAddr=parseHostnameToString(hostname);
					tmpList.add(ipAddr);
					logger.info("Authorized parsed to ip by hostname "+ipAddr);
				}
			} else {
				tmpList.add(parseHostnameToString(authorisedHostnameString));
			}
		}

		authorizedIpList = tmpList;
		
		authorizedServiceList=new LinkedList<>();
		authorizedServiceList.addAll(Arrays.asList(LDNUtils.getServicesForReviewEndorsement()));
	}

	public static boolean isHostAuthorized(HttpServletRequest request) {
		String ipFromRequest = request.getRemoteAddr();
		logger.info("IP ADDRESS OF THE REQUEST "+ipFromRequest);
		logger.info("LOCALHOST TRUSTED "+isLocalhostTrustedByDefault);
		if (isLocalhostTrustedByDefault) {
			try {
				InetAddress ip = InetAddress.getByName(ipFromRequest);
				return authorizedIpList.contains(ipFromRequest) || ip.isLoopbackAddress();
			} catch (UnknownHostException e) {
				logger.error(e);
			}
		}
		return authorizedIpList.contains(ipFromRequest);
	}

	public static String parseHostnameToString(String hostname) {
		String tmpIpAddress = null;
		try {
			tmpIpAddress = InetAddress.getByName(hostname.trim()).getHostAddress();
		} catch (UnknownHostException e) {
			logger.error("Hostname " + hostname + " is unknown ", e);
		}
		return tmpIpAddress;
	}

	public static boolean isServiceIdAuthorized(NotifyLDNDTO notifyLDNDTO) {
		logger.info("Service requesting auhtorizaytion: "+notifyLDNDTO.getOrigin().parseIdWithRemovedProtocol());
		return authorizedServiceList.contains(notifyLDNDTO.getOrigin().parseIdWithRemovedProtocol());
	}

}
