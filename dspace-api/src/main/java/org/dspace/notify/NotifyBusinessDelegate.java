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
import org.dspace.content.Metadatum;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.handle.HandleManager;
import org.dspace.ldn.LDNUtils;
import org.dspace.ldn.NotifyLDNDTO;
import org.dspace.ldn.NotifyLDNDTO.Actor;
import org.dspace.ldn.NotifyLDNDTO.Object;
import org.dspace.ldn.NotifyLDNDTO.Origin;
import org.dspace.ldn.NotifyLDNDTO.Target;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;

public class NotifyBusinessDelegate {

	private CloseableHttpClient client = null;
	private int maxNumberOfAttempts = ConfigurationManager.getIntProperty("ldn-coar-notify",
			"coar.notify.max.attempts.request-review");
	private long sleepBetweenTimeouts = ConfigurationManager.getLongProperty("ldn-coar-notify",
			"coar.notify.sleep-between-attempts.request-review");
	private int timeout = ConfigurationManager.getIntProperty("ldn-coar-notify", "coar.notify.timeout.request-review");
	private static final Logger log = Logger.getLogger(NotifyBusinessDelegate.class);
	private static final ObjectMapper objectMapper;
	private Context context;

	public NotifyBusinessDelegate(Context context) {
		HttpClientBuilder custom = HttpClients.custom();
		timeout = timeout == 0 ? 2_000 : timeout;
		client = custom.disableAutomaticRetries().setMaxConnTotal(5)
				.setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(timeout).build()).build();
		this.context = context;
	}

	static {
		objectMapper = new ObjectMapper();
		objectMapper.registerModule(NotifyLDNDTO.getJacksonHydraSerializerModule());
		objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		objectMapper.enable(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED);
		objectMapper.enable(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED);
	}

	@SuppressWarnings("deprecation")
	public void askServicesForReviewEndorsement(Item item, String serviceId) {
		try {

			String serviceEndpoint = ConfigurationManager.getProperty("ldn-coar-notify",
					"service." + serviceId + ".endpoint");
			boolean isEndorsementSupported = ConfigurationManager.getBooleanProperty("ldn-coar-notify",
					"service." + serviceId + ".endorsement");

			HttpPost method = null;
			int statusCode = HttpStatus.SC_INTERNAL_SERVER_ERROR;
			int numberOfTries = 0;

			while (numberOfTries < maxNumberOfAttempts && (statusCode < 200 || statusCode > 300)) {
				numberOfTries++;
				try {
					Thread.sleep(sleepBetweenTimeouts * (numberOfTries - 1));

					URIBuilder uriBuilder = new URIBuilder(serviceEndpoint);

					NotifyLDNDTO objectJsonLd = createLDNRequestObject(item, serviceId, serviceEndpoint);

					String repositoryMessageID = objectJsonLd.getId();
					objectJsonLd.setId(repositoryMessageID);

					StringEntity requestEntity = new StringEntity(LDNRequestObjectToString(objectJsonLd),
							"application/ld+json", "utf-8");

					method = new HttpPost(uriBuilder.build());
					method.setEntity(requestEntity);

					HttpResponse response = client.execute(method);
					statusCode = response.getStatusLine().getStatusCode();

					if (statusCode == HttpStatus.SC_OK) {
						LDNUtils.saveMetadataRequestForItem(item, serviceId, repositoryMessageID,
								isEndorsementSupported);
					} else {
						log.error(serviceId + " check if service id is properly configured!");
					}
					log.info(serviceEndpoint + " has returned " + statusCode);
					log.info("Response message: " + response.getStatusLine().getReasonPhrase());
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

	private NotifyLDNDTO createLDNRequestObject(Item item, String serviceId, String endpoint) {
		String localLDNInBoxEndpoint = ConfigurationManager.getProperty("ldn-coar-notify",
				"coar.notify.local-inbox-endpoint");
		String dspaceBaseUrl = ConfigurationManager.getProperty("dspace.baseUrl");

		NotifyLDNDTO ldnRequestDTO = new NotifyLDNDTO();
		// call setter methods on ldnRequestDTO to set required data
		ldnRequestDTO.setId(LDNUtils.generateRandomUrnUUID());

		Actor actor = new Actor();
		actor.setId(ConfigurationManager.getProperty("dspace.url"));
		actor.setName(ConfigurationManager.getProperty("dspace.name"));
		actor.setType("Service");
		ldnRequestDTO.setActor(actor);

		NotifyLDNDTO.Object object = new Object();
		object.setId(dspaceBaseUrl + "/handle/" + item.getHandle());
		object.setIetfCiteAs(HandleManager.getCanonicalForm(item.getHandle()));
		object.setType(new String[] { item.getName() });
		ldnRequestDTO.setObject(object);

		NotifyLDNDTO.Object.Url url = new NotifyLDNDTO.Object.Url();
		String bitstreamId = LDNUtils.getPDFSimpleUrl(context, item);
		url.setId(bitstreamId);
		url.setType(parseItemMetadataType(item));
		url.setMediaType(LDNUtils.retrieveMimeTypeFromFilePath(bitstreamId));
		object.setUrl(url);

		Origin origin = new Origin();
		origin.setId(localLDNInBoxEndpoint);
		origin.setInbox(localLDNInBoxEndpoint);
		origin.setType("Service");
		ldnRequestDTO.setOrigin(origin);

		Target target = new Target();
		target.setId(serviceId);
		target.setInbox(endpoint);
		target.setType("Service");
		ldnRequestDTO.setTarget(target);

		ldnRequestDTO.setType(new String[] { "Offer", "coar-notify:ReviewAction" });

		return ldnRequestDTO;
	}

	private static String[] parseItemMetadataType(Item item) {
		Metadatum[] metadatum = item.getMetadataByMetadataString("dc.type");
		String[] types = new String[metadatum.length + 1];
		try {
			int counter = 0;
			for (Metadatum tmp : metadatum) {
				types[counter++] = tmp.value;
			}
			types[types.length - 1] = item.getName();
		} catch (Exception e) {
			log.error("error while parsing metadata", e);
		}
		return types;
	}

	private static String LDNRequestObjectToString(NotifyLDNDTO ldnRequestDTO) throws JsonProcessingException {
		String jsonLd = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(ldnRequestDTO);

		log.info("JSON-LD Request body to ask a review to a service");
		jsonLd = jsonLd.replace("\"@context\" : {\n" + "    \"@vocab\" : \"http://schema.org/\"\n  }",
				"\"@context\": [\n    \"https://www.w3.org/ns/activitystreams\",\n"
						+ "    \"https://purl.org/coar/notify\"\n  ]");

		log.info(jsonLd);

		return jsonLd;
	}

	public void setMaxNumberOfTries(int maxNumberOfTries) {
		this.maxNumberOfAttempts = maxNumberOfTries;
	}

	public void setSleepBetweenTimeouts(long sleepBetweenTimeouts) {
		this.sleepBetweenTimeouts = sleepBetweenTimeouts;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

}
