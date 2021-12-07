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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.dspace.app.webui.ldn.LDNPayloadProcessor;
import org.dspace.app.webui.ldn.NotifyLDNRequestDTO;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * *
 * 
 * @author Stefano Maffei (4Science.it)
 * 
 */
public class LDNInBoxServlet extends DSpaceServlet {

	@Autowired
	@Qualifier("ldnActionsMapping")
	Map<Set<String>, LDNPayloadProcessor> ldnActionsMapping;

	/** Logger */
	private static Logger logger = Logger.getLogger(LDNInBoxServlet.class);

	@Override
	protected void doDSPost(Context context, HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, SQLException, AuthorizeException {
		String payloadRequest = getPayload(request);
		NotifyLDNRequestDTO ldnRequestDTO = payloadToDTO(payloadRequest);
		
		Set<String> actionIdentifier = new HashSet<String>();
		Collections.addAll(actionIdentifier, ldnRequestDTO.getType());
		
		try {
			ldnActionsMapping.get(actionIdentifier).processLDNPayload(ldnRequestDTO);
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
		NotifyLDNRequestDTO notifyLDNRequestDTO = null;
		try {
			notifyLDNRequestDTO = objectMapper.readValue(payload, NotifyLDNRequestDTO.class);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return notifyLDNRequestDTO;
	}
	
}
