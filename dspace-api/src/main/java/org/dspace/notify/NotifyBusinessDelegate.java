package org.dspace.notify;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.dspace.app.sherpa.SHERPAResponse;
import org.dspace.app.sherpa.SHERPAService;
import org.dspace.content.Item;
import org.dspace.core.ConfigurationManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;

public class NotifyBusinessDelegate {
	
	private CloseableHttpClient client = null;
	private int maxNumberOfTries;
	private long sleepBetweenTimeouts;
	private int timeout;
	// Endpoint is configured in ldn-trusted-services.cfg
	private static final String NOTIFY_ENDOINT = ConfigurationManager.getProperty("coar.notify.inbox-endpoint");
	/** log4j category */
	private static final Logger log = Logger.getLogger(NotifyBusinessDelegate.class);
	private static final ObjectMapper objectMapper;

	public NotifyBusinessDelegate() {
		HttpClientBuilder custom = HttpClients.custom();
		client = custom.disableAutomaticRetries().setMaxConnTotal(5)
				.setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(timeout).build()).build();
	}
	
	static {
		objectMapper = new ObjectMapper();
		objectMapper.registerModule(LDNRequestDTO.getJacksonHydraSerializerModule());
		objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
	}

	public void setInitializeMetadataForItem(Item item) {
		try {

			HttpPost method = null;
			SHERPAResponse sherpaResponse = null;
			int numberOfTries = 0;

			while (numberOfTries < maxNumberOfTries && sherpaResponse == null) {
				numberOfTries++;

				try {
					Thread.sleep(sleepBetweenTimeouts * (numberOfTries - 1));

					URIBuilder uriBuilder = new URIBuilder(NOTIFY_ENDOINT);
					uriBuilder.addParameter("versions", "all");

					
					String jsonld= createLDNRequestObjectToString(item);
					
					StringEntity requestEntity = new StringEntity(jsonld, "application/json", "utf-8");
					
					method = new HttpPost(uriBuilder.build());
					method.setEntity(requestEntity);
					// Execute the method.

					HttpResponse response = client.execute(method);
					int statusCode = response.getStatusLine().getStatusCode();

					if (statusCode != HttpStatus.SC_OK) {
						log.error("LDN InBox has returned " + statusCode);
						log.error("LDN InBox has was probably not properly configured!");
						log.error("Check ldn-trusted-services.cfg to set up LDN InBox");
					} else {

						log.info("LDN InBox has returned " + statusCode);
					}

				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		} catch (Exception e) {
			log.error(e);
		} finally {
			try {
				client.close();
			} catch (IOException e) {
				log.error(e);
			}
		}

	}

	private static String createLDNRequestObjectToString(Item item) throws JsonProcessingException {
		LDNRequestDTO ldnRequestDTO =new LDNRequestDTO();
		
		//call setter methods on ldnRequestDTO to set required data
		
		return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(ldnRequestDTO);
	}

	public void setMaxNumberOfTries(int maxNumberOfTries) {
		this.maxNumberOfTries = maxNumberOfTries;
	}

	public void setSleepBetweenTimeouts(long sleepBetweenTimeouts) {
		this.sleepBetweenTimeouts = sleepBetweenTimeouts;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

}
