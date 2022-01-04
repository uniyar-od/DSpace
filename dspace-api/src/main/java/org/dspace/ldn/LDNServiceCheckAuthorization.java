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
		for(String tmp:authorisedHostnameString) {
			tmpList.add(parseHostnameToString(tmp));
		}

		authorizedIpList = tmpList;

		authorizedServiceList = new LinkedList<>();
		authorizedServiceList.addAll(Arrays.asList(LDNUtils.getServicesForReviewEndorsement()));
	}

	public static boolean isHostAuthorized(HttpServletRequest request) {
		String ipFromRequest = request.getRemoteAddr();
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
		return authorizedServiceList.contains(notifyLDNDTO.getOrigin().parseIdWithRemovedProtocol());
	}

}
