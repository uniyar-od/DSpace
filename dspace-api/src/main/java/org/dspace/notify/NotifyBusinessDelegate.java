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
import org.dspace.content.Item;
import org.dspace.ldn.LDNUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;

public class NotifyBusinessDelegate {

	private CloseableHttpClient client = null;
	private int maxNumberOfTries = 3;
	private long sleepBetweenTimeouts;
	private int timeout;
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

	@SuppressWarnings("deprecation")
	public void reachEndpoitToRequestReview(Item item, String endpointId) {
		try {

			HttpPost method = null;
			int statusCode = HttpStatus.SC_INTERNAL_SERVER_ERROR;
			int numberOfTries = 0;

			while (numberOfTries < maxNumberOfTries && statusCode != HttpStatus.SC_OK) {
				numberOfTries++;

				try {
					Thread.sleep(sleepBetweenTimeouts * (numberOfTries - 1));

					URIBuilder uriBuilder = new URIBuilder(endpointId);

					LDNRequestDTO objectJsonLd = createLDNRequestObject(item);

					String repositoryMessageID = objectJsonLd.getId();
					objectJsonLd.setId(repositoryMessageID);

					StringEntity requestEntity = new StringEntity(LDNRequestObjectToString(objectJsonLd),
							"application/json", "utf-8");

					method = new HttpPost(uriBuilder.build());
					method.setEntity(requestEntity);
					// Execute the method.

					HttpResponse response = client.execute(method);
					statusCode = response.getStatusLine().getStatusCode();

					if (statusCode == HttpStatus.SC_OK) {
						log.info(endpointId + " has returned " + statusCode);
						LDNUtils.saveMetadataRequestForItem(item, endpointId, repositoryMessageID);
					} else {
						log.error(endpointId + " has returned " + statusCode);
						log.error(endpointId + " was probably not properly configured!");
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

	private static LDNRequestDTO createLDNRequestObject(Item item) {
		LDNRequestDTO ldnRequestDTO = new LDNRequestDTO();
		// call setter methods on ldnRequestDTO to set required data
		ldnRequestDTO.setId(LDNUtils.generateRandomUrnUUID());

		return ldnRequestDTO;
	}

	private static String LDNRequestObjectToString(LDNRequestDTO ldnRequestDTO) throws JsonProcessingException {
		String jsonLd = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(ldnRequestDTO);

		jsonLd = jsonLd.replace("\"@context\" : {\n" + "    \"@vocab\" : \"http://schema.org/\"\n" + "  }",
				"\"@context\": [\n" + "    \"https://www.w3.org/ns/activitystreams\",\n"
						+ "    \"https://purl.org/coar/notify\"\n" + "  ]");
		return jsonLd;
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
