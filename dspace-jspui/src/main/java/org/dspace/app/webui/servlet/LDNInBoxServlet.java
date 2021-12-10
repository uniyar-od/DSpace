package org.dspace.app.webui.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Set;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.dspace.app.webui.ldn.LDNPayloadProcessor;
import org.dspace.app.webui.ldn.LDNServiceCheckAuthorization;
import org.dspace.app.webui.ldn.LDNUtils;
import org.dspace.app.webui.ldn.NotifyLDNRequestDTO;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;
import org.dspace.servicemanager.spring.SpringServiceManager;
import org.dspace.utils.DSpace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.ServiceManager;

/**
 * *
 * 
 * @author Stefano Maffei (4Science.it)
 * 
 */
public class LDNInBoxServlet extends DSpaceServlet {

	public static Map<Set<String>, LDNPayloadProcessor> ldnActionsMapping;

	/** Logger */
	private static Logger logger = Logger.getLogger(LDNInBoxServlet.class);

	@SuppressWarnings("unchecked")
	@Override
	public void init() throws ServletException {
		super.init();
		DSpace dspace = new DSpace();
		org.dspace.kernel.ServiceManager manager = dspace.getServiceManager();
		manager.getServiceByName("ldnActionsMapping", Map.class);
		ldnActionsMapping = manager.getServiceByName("ldnActionsMapping", Map.class);
	}

	@Override
	protected void doDSPost(Context context, HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, SQLException, AuthorizeException {
		if (!LDNServiceCheckAuthorization.isHostAuthorized(request)) {
			// No authorization found for the requesting ip
			sendErrorCode(HttpServletResponse.SC_UNAUTHORIZED, response);
			return;
		}

		String payloadRequest = getPayload(request);
		NotifyLDNRequestDTO ldnRequestDTO = payloadToDTO(payloadRequest);

		Set<String> actionIdentifier = new HashSet<String>();
		Collections.addAll(actionIdentifier, ldnRequestDTO.getType());

		try {
			ldnActionsMapping.get(actionIdentifier).processRequest(ldnRequestDTO);
		} catch (NullPointerException e) {
			logger.error("Action is not supported!");
		} catch (Exception e2) {
			logger.error("Error\n" + payloadRequest, e2);
		}

	}

	private static String getPayload(HttpServletRequest request) throws IOException {
		return IOUtils.toString(request.getReader());
	}

	private static NotifyLDNRequestDTO payloadToDTO(String payload) {

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

		NotifyLDNRequestDTO notifyLDNRequestDTO = null;
		try {
			notifyLDNRequestDTO = objectMapper.readValue(payload, NotifyLDNRequestDTO.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return notifyLDNRequestDTO;
	}

	private static final void sendErrorCode(int statusCode, HttpServletResponse response) {
		try {
			response.sendError(statusCode);
		} catch (IOException e) {
			logger.error(e);
		}
	}

}
