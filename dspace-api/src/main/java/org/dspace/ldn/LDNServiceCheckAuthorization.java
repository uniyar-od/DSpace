package org.dspace.ldn;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.services.factory.DSpaceServicesFactory;

public class LDNServiceCheckAuthorization {
	/** Logger */
	private static Logger logger = Logger.getLogger(LDNServiceCheckAuthorization.class);

	private static List<String> authorizedIpList;

	private static List<String> authorizedServiceList;

	private static boolean isLocalhostTrustedByDefault;

	static {
		isLocalhostTrustedByDefault = DSpaceServicesFactory.getInstance().getConfigurationService()
				.getBooleanProperty("ldn-trusted.localhost.default");
		String[] authorisedIpString = DSpaceServicesFactory.getInstance().getConfigurationService()
				.getArrayProperty("ldn-trusted.from.ip");
		String[] authorisedHostnameString = DSpaceServicesFactory.getInstance().getConfigurationService()
				.getArrayProperty("ldn-trusted.from.hostname");

		List<String> tmpList = new LinkedList<>();

		// Authorized IP Addresses
		tmpList.addAll(Arrays.asList(authorisedIpString));
		// Authorized Hostnames
		String ipAddr;
		for(String tmp:authorisedHostnameString) {
			ipAddr=parseHostnameToString(tmp);
			tmpList.add(ipAddr);
			logger.info("Authorized parsed to ip by hostname "+ipAddr);
		}

		authorizedIpList = tmpList;

		authorizedServiceList = new LinkedList<>();
		authorizedServiceList.addAll(Arrays.asList(LDNUtils.getServicesForReviewEndorsement()));
	}

	public static boolean isHostAuthorized(HttpServletRequest request) {
		String ipFromRequest = getClientIpAddr(request);
		logger.info("IP ADDRESS OF THE REQUEST " + ipFromRequest);
		logger.info("LOCALHOST TRUSTED " + isLocalhostTrustedByDefault);
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
		logger.info("Service requesting auhtorizaytion: " + notifyLDNDTO.getOrigin().parseIdWithRemovedProtocol());
		return authorizedServiceList.contains(notifyLDNDTO.getOrigin().parseIdWithRemovedProtocol());
	}

	public static String getClientIpAddr(HttpServletRequest request) {
		String ip = request.getHeader("X-Forwarded-For");
		logger.info("IP X-Forwarded-For " + ip);
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
			logger.info("IP Proxy-Client-IP " + ip);
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
			logger.info("IP WL-Proxy-Client-IP " + ip);
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_CLIENT_IP");
			logger.info("IP HTTP_CLIENT_IP " + ip);
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
			logger.info("IP HTTP_X_FORWARDED_FOR " + ip);
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
			logger.info("IP " + ip);
		}
		return ip;
	}

}
