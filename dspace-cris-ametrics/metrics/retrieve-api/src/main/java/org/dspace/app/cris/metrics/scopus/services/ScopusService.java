/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * https://github.com/CILEA/dspace-cris/wiki/License
 */
package org.dspace.app.cris.metrics.scopus.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.dspace.app.cris.metrics.common.model.ConstantMetrics;
import org.dspace.app.cris.metrics.scopus.dto.ScopusResponse;
import org.dspace.app.cris.metrics.scopus.script.ScriptRetrieveCitation.ScopusIdentifiersToResponse;
import org.dspace.app.util.XMLUtils;
import org.dspace.core.ConfigurationManager;
import org.dspace.submit.lookup.SubmissionLookupService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.gson.Gson;

public class ScopusService {

	/** log4j category */
	private static final Logger log = Logger.getLogger(ScopusService.class);

	private CloseableHttpClient client = null;

	private int maxNumberOfTries;
	private long sleepBetweenTimeouts;
	private long sleepBetweenEachCall;
	private int timeout;

	public ScopusService() {
		HttpClientBuilder custom = HttpClients.custom();
		// httpclient 4.3+ doesn't appear to have any sensible defaults any
		// more. Setting conservative defaults as not to hammer the Scopus
		// service too much.
		client = custom.disableAutomaticRetries().setMaxConnTotal(5)
				.setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(timeout).build()).build();

	}

	/**
	 * Abstract Citation Count API: This represents a retrieval of citation
	 * counts for specific documents (SCOPUS content) as a branded image.
	 * Performs a search based upon the identifiers provided and returns the
	 * cited-by count of the document(s) returned as a SCOPUS-branded image or
	 * as textual metadata (JSON, XML). Note that each category is considered a
	 * distinct resource and access restrictions may be applicable.
	 * 
	 * @param activateSleep
	 * @param scIDs2R
	 */
	public void getCitations(boolean activateSleep, List<ScopusIdentifiersToResponse> scIDs2R) {

		
		if (!ConfigurationManager.getBooleanProperty(SubmissionLookupService.CFG_MODULE, "remoteservice.demo"))
        {
			if (activateSleep) {
				try {
					Thread.sleep(sleepBetweenEachCall);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
	
			String endpoint = ConfigurationManager.getProperty("cris", "ametrics.elsevier.scopus.endpoint");
			String apiKey = ConfigurationManager.getProperty("cris", "ametrics.elsevier.scopus.apikey");
	
			HttpGet method = null;
			int numberOfTries = 0;
	
			boolean done = false;
			while (numberOfTries < maxNumberOfTries && done) {
				numberOfTries++;
				try {
					Thread.sleep(sleepBetweenTimeouts * (numberOfTries - 1));
	
					URIBuilder uriBuilder = new URIBuilder(endpoint);
					StringBuilder query = new StringBuilder();
					for (ScopusIdentifiersToResponse sc2R : scIDs2R) {
						
						List<String> pmids = sc2R.getPmids();
						List<String> dois = sc2R.getDois();
						List<String> eids = sc2R.getEids();
						
						if (pmids != null && pmids.size() > 0) {
							String pmid = pmids.get(0);
							if (StringUtils.isNotBlank(pmid)) {
								if (query.length() > 0) {
									query.append(" OR ");
								}
								query.append("PMID(").append(pmid).append(")");
							}
						}
						if (dois != null && dois.size() > 0) {
							String doi = dois.get(0);
							if (StringUtils.isNotBlank(doi)) {
								if (query.length() > 0) {
									query.append(" OR ");
								}
								query.append("DOI(").append(doi).append(")");
							}
						}
						if (eids != null && eids.size() > 0) {
							String eid = eids.get(0);
							if (StringUtils.isNotBlank(eid)) {
								if (query.length() > 0) {
									query.append(" OR ");
								}
								query.append("EID(").append(eid).append(")");
							}					
						}
					}
					uriBuilder.addParameter("query", query.toString());
	
					method = new HttpGet(uriBuilder.build());
	
					method.addHeader("Accept", "application/xml");
					method.addHeader("X-ELS-APIKey", apiKey);
	
					// Execute the method.
					HttpResponse response = client.execute(method);
					int statusCode = response.getStatusLine().getStatusCode();
					HttpEntity responseBody = response.getEntity();
					
					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
					factory.setValidating(false);
					factory.setIgnoringComments(true);
					factory.setIgnoringElementContentWhitespace(true);

					DocumentBuilder db = factory.newDocumentBuilder();
					Document inDoc = db.parse(responseBody.getContent());

					if (log.isDebugEnabled())
		            {
		                DOMSource domSource = new DOMSource(inDoc);
		                StringWriter writer = new StringWriter();
		                StreamResult result = new StreamResult(writer);
		                TransformerFactory tf = TransformerFactory.newInstance();
		                Transformer transformer = tf.newTransformer();
		                transformer.transform(domSource, result);
		                log.debug(writer.toString());
		            }
					
					Element xmlRoot = inDoc.getDocumentElement();
					List<Element> dataRoot = XMLUtils.getElementList(xmlRoot, "entry");
					
					ScopusResponse scopusResponse = null;
					
					if (statusCode != HttpStatus.SC_OK) {
						scopusResponse = new ScopusResponse("Scopus return not OK status: " + statusCode, ConstantMetrics.STATS_INDICATOR_TYPE_ERROR);
						pairScopusResponseToScIDs2R(scopusResponse, scIDs2R);
					} else if (null != responseBody) {
						for (Element element : dataRoot) 
						{
							if (log.isDebugEnabled())
							{                      
								log.debug(responseBody.getContent());
							}
							scopusResponse = new ScopusResponse(element);
							pairScopusResponseToScIDs2R(scopusResponse, scIDs2R);
						}
					} else {
						scopusResponse = new ScopusResponse("Scopus returned no response", ConstantMetrics.STATS_INDICATOR_TYPE_ERROR);
						pairScopusResponseToScIDs2R(scopusResponse, scIDs2R);
					}
					
					done = true;
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
        }else {
            InputStream stream = null;
            try
            {
                File file = new File(
                        ConfigurationManager.getProperty("dspace.dir")
                                + "/config/crosswalks/demo/scopus-search.xml");
                stream = new FileInputStream(file);
                DocumentBuilderFactory factory = DocumentBuilderFactory
                        .newInstance();
                factory.setValidating(false);
                factory.setIgnoringComments(true);
                factory.setIgnoringElementContentWhitespace(true);

                DocumentBuilder builder = factory.newDocumentBuilder();
                Document inDoc = builder.parse(stream);

                Element xmlRoot = inDoc.getDocumentElement();

                List<Element> pages = XMLUtils.getElementList(xmlRoot,
                		"link");
                for(Element page: pages){
                	String refPage = page.getAttribute("ref");
                	if(StringUtils.equalsIgnoreCase(refPage, "next")){
                		break;
                	}
                }
                List<Element> pubArticles = XMLUtils.getElementList(xmlRoot,
                		"entry");
                for (Element element : pubArticles) {
                	ScopusResponse scopusResponse = new ScopusResponse(element);
					pairScopusResponseToScIDs2R(scopusResponse, scIDs2R);
				}
            }
            catch (Exception e)
            {
                throw new RuntimeException(e.getMessage(), e);
            }
            finally
            {
                if (stream != null)
                {
                    try
                    {
                        stream.close();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
	}

	private void pairScopusResponseToScIDs2R(ScopusResponse scopusResponse, List<ScopusIdentifiersToResponse> scIDs2R) {
		
		Gson gson = new Gson();
		Map<String, String> responseMap = gson.fromJson(scopusResponse.getScopusCitation().getRemark(), HashMap.class);
		
		String pmid = responseMap.get("pmid");
		String doi = responseMap.get("doi");
		String eid = responseMap.get("identifier");
		for (ScopusIdentifiersToResponse sc2R : scIDs2R) {
			if (sc2R.getResponse() != null) {
				continue;
			}
			if (scopusResponse.isError()) {
				sc2R.setResponse(scopusResponse);
			}
			
				
			List<String> pmids = sc2R.getPmids();
			List<String> dois = sc2R.getDois();
			List<String> eids = sc2R.getEids();
			
			if (eids != null && eids.size() > 0) {
				String sc2Reid = eids.get(0);
				if (StringUtils.isNotBlank(sc2Reid) && StringUtils.isNotBlank(eid) && StringUtils.equals(sc2Reid, eid)) {
					sc2R.setResponse(scopusResponse);
					continue;
				}					
			}
			if (dois != null && dois.size() > 0) {
				String sc2Rdoi = dois.get(0);
				if (StringUtils.isNotBlank(sc2Rdoi) && StringUtils.isNotBlank(doi) && StringUtils.equals(sc2Rdoi, doi)) {
					sc2R.setResponse(scopusResponse);
					continue;
				}
			}
			if (pmids != null && pmids.size() > 0) {
				String sc2Rpmid = pmids.get(0);
				if (StringUtils.isNotBlank(sc2Rpmid) && StringUtils.isNotBlank(pmid) && StringUtils.equals(sc2Rpmid, pmid)) {
					sc2R.setResponse(scopusResponse);
					continue;
				}
			}
		}
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

	public long getSleepBetweenEachCall() {
		return sleepBetweenEachCall;
	}

	public void setSleepBetweenEachCall(long sleepBetweenEachCall) {
		this.sleepBetweenEachCall = sleepBetweenEachCall;
	}
}
