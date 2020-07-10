/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.submit.lookup;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.xerces.impl.dv.util.Base64;
import org.dspace.app.util.XMLUtils;
import org.dspace.core.ConfigurationManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import gr.ekt.bte.core.Record;

public class WOSService {
    private String loginSID;
    private long loginTime;

    private int numReqInSecond = 0;
    private long lastRequest = 0;
    private long lastInvalidateByRetry = 0;

	private static Logger log = Logger.getLogger(WOSService.class);
	
	private final String SEARCH_HEAD_BY_AFFILIATION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:wok=\"http://woksearch.v3.wokmws.thomsonreuters.com\"><soapenv:Header/><soapenv:Body><wok:search><queryParameters><databaseId>WOK</databaseId>";
	private final String SEARCH_HEAD = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:wok=\"http://woksearch.v3.wokmws.thomsonreuters.com\"><soapenv:Header/><soapenv:Body><wok:search><queryParameters><databaseId>WOK</databaseId>";
	private final String RETRIEVEBYID_HEAD = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:wok=\"http://woksearch.v3.wokmws.thomsonreuters.com\"><soapenv:Header/><soapenv:Body><wok:retrieveById><databaseId>WOK</databaseId>";
	private final int MAX_COUNT = 100;
	private final String SEARCH_END_BY_AFFILIATION = "<queryLanguage>en</queryLanguage></queryParameters><retrieveParameters><firstRecord>::firstRecord::</firstRecord><count>"+MAX_COUNT+"</count></retrieveParameters></wok:search></soapenv:Body></soapenv:Envelope>";
	private final String SEARCH_END = "<queryLanguage>en</queryLanguage></queryParameters><retrieveParameters><firstRecord>::firstRecord::</firstRecord><count>"+MAX_COUNT+"</count></retrieveParameters></wok:search></soapenv:Body></soapenv:Envelope>";
	private final String RETRIEVEBYID_END = "<queryLanguage>en</queryLanguage><retrieveParameters><firstRecord>::firstRecord::</firstRecord><count>"+MAX_COUNT+"</count></retrieveParameters></wok:retrieveById></soapenv:Body></soapenv:Envelope>";

	private final String AUTH_MESSAGE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:ns=\"http://auth.cxf.wokmws.thomsonreuters.com\">"
			+ "<soapenv:Header></soapenv:Header><soapenv:Body><ns:authenticate/></soapenv:Body></soapenv:Envelope>";
	private final String CLOSE_MESSAGE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:auth=\"http://auth.cxf.wokmws.thomsonreuters.com\"><soapenv:Header/><soapenv:Body>"
			+ "<auth:closeSession/></soapenv:Body></soapenv:Envelope>";

	private final String endPointAuthService = "http://search.webofknowledge.com/esti/wokmws/ws/WOKMWSAuthenticate";
	//private final String endPointAuthService = "http://localhost:9998/esti/wokmws/ws/WOKMWSAuthenticate";
	private final String endPointSearchService = "http://search.webofknowledge.com/esti/wokmws/ws/WokSearch";
	//private final String endPointSearchService = "http://localhost:9998/esti/wokmws/ws/WokSearch";

	public List<Record> search(String doi, String title, String author, int year, String username, String password, boolean ipAuth)
			throws HttpException,
			IOException {
		StringBuffer query = new StringBuffer("<userQuery>");
		query.append(buildQueryPart(doi, title, author, year));
		query.append("</userQuery>");
		String message = SEARCH_HEAD + query.toString() + SEARCH_END;
		return internalSearch(message, username, password, ipAuth);
	}

	public List<Record> search(Set<String> dois, String username, String password, boolean ipAuth) throws HttpException, IOException {
		if (dois != null && dois.size() > 0) {
			StringBuffer query = new StringBuffer("<userQuery>");
			for (String doi : dois) {
				if (query.length() > "<userQuery>".length()) {
					query.append(" OR ");
				}
				query.append(buildQueryPart(doi, null, null, -1));
			}
			query.append("</userQuery>");
			String message = SEARCH_HEAD + query.toString() + SEARCH_END;
			return internalSearch(message, username, password, ipAuth);
		}
		return null;
	}

	public Record retrieve(String wosid, String username, String password, boolean ipAuth) throws IOException {
		Record result = null;
		if (StringUtils.isNotBlank(wosid)) {
			String message = RETRIEVEBYID_HEAD + "<uid>" + wosid + "</uid>" + RETRIEVEBYID_END;
			List<Record> ret = internalSearch(message, username, password, ipAuth);
			if (ret.size() > 0) {
				result = ret.get(0);
			}
		}
		return result;
	}

	public StringBuffer buildQueryPart(String doi, String title, String author, int year) {
		StringBuffer query = new StringBuffer("");
		if (StringUtils.isNotBlank(doi)) {
			query.append("DO=(");
			query.append(StringEscapeUtils.escapeXml(doi));
			query.append(")");
		}
		if ((StringUtils.isNotBlank(title)) && (query.length() > 0)) {
			query.append(" AND ");
		}
		if (StringUtils.isNotBlank(title)) {
			query.append("TI=(\"");
			query.append(StringEscapeUtils.escapeXml(title));
			query.append("\")");
		}
		if ((StringUtils.isNotBlank(author)) && (query.length() > 0)) {
			query.append(" AND ");
		}
		if (StringUtils.isNotBlank(author)) {
			query.append("AU=(\"");
			query.append(StringEscapeUtils.escapeXml(author));
			query.append("\")");
		}
		if ((year != -1) && (query.length() > 0)) {
			query.append(" AND ");
		}
		if (year != -1) {
			query.append("PY=(");
			query.append(year);
			query.append(")");
		}
		return query;
	}

	private synchronized String login(String username, String password, boolean ipAuth) throws IOException, HttpException {
		if (loginSID != null && new Date().getTime() - loginTime < 1000*ConfigurationManager.getIntProperty("wos.login.timeout", 3600)) {
		    return loginSID;
		}
		else if (loginSID != null && new Date().getTime() - loginTime >= 1000*ConfigurationManager.getIntProperty("wos.login.timeout", 3600)) {
		    logout(loginSID);
		}
	    String ret = null;
		PostMethod method = null;
		try {
			// open session
			HttpClient client = new HttpClient();
			method = new PostMethod(endPointAuthService);
			
			if(!ipAuth) {
			    String authString = username + ":" + password;
			    method.setRequestHeader("Authorization", "Basic " + Base64.encode(authString.getBytes()));
			    method.setDoAuthentication(true);
			}	
			
			RequestEntity re = new StringRequestEntity(AUTH_MESSAGE, "text/xml", "UTF-8");
			method.setRequestEntity(re);
			
			// Execute the method.
			int statusCode = client.executeMethod(method);

			if (statusCode != HttpStatus.SC_OK) {
				throw new RuntimeException("Chiamata al webservice fallita: " + method.getStatusLine());
			}

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			factory.setIgnoringElementContentWhitespace(true);

			DocumentBuilder builder;
			try {
				builder = factory.newDocumentBuilder();

				InputStream responseBodyAsStream = method.getResponseBodyAsStream();
				Document inDoc = builder.parse(responseBodyAsStream);

				Element xmlRoot = inDoc.getDocumentElement();
				Element soapBody = XMLUtils.getSingleElement(xmlRoot, "soap:Body");

				Element response = XMLUtils.getSingleElement(soapBody, "ns2:authenticateResponse");
				Element sidTag = XMLUtils.getSingleElement(response, "return");
				ret = sidTag.getTextContent();
				loginSID = ret;
				loginTime = new Date().getTime();
			} catch (ParserConfigurationException e) {
				log.error(e.getMessage(), e);
			} catch (SAXException e1) {
				log.error(e1.getMessage(), e1);
			}
		} finally {
			if (method != null) {
				method.releaseConnection();
			}
		}
		return ret;
	}

	private void logout(String sid) throws IOException, HttpException {
		if (StringUtils.isNotBlank(sid)) {
			// close session
			PostMethod method = null;
			try {
			    loginSID = null;
			    loginTime = 0;
				HttpClient client = new HttpClient();
				method = new PostMethod(endPointAuthService);
				method.setRequestHeader("Cookie", "SID=" + sid);
				RequestEntity re = new StringRequestEntity(CLOSE_MESSAGE, "text/xml", "UTF-8");
				method.setRequestEntity(re);

				int statusCode = client.executeMethod(method);
				if (statusCode != HttpStatus.SC_OK) {
					log.info("Invalidation of the session failed: " + method.getStatusLine());
				}
			} finally {
				if (method != null) {
					method.releaseConnection();
				}
			}
		}
	}

	private List<Record> internalSearch(String message, String username, String password, boolean ipAuth) {
		List<Record> results = new ArrayList<Record>();
		if (!ipAuth && (StringUtils.isBlank(username) || (StringUtils.isBlank(password)))) {
			return results;
		}
		try {
			String sid = login(username, password, ipAuth);
			if (StringUtils.isNotBlank(sid)) {
				try {
					HttpClient client = new HttpClient();

					// in wos the first record is 1-based
					int start = 1;
                    boolean lastPageReached= false;
                    while(!lastPageReached){
                        waitIfNeeded();
                        String paginatedMessage = message.replace("::firstRecord::", String.valueOf(start));
                        PostMethod method = new PostMethod(endPointSearchService);
                        method.setRequestHeader("Cookie", "SID=" + sid);
                        RequestEntity re = new StringRequestEntity(paginatedMessage, "text/xml", "UTF-8");
                        method.setRequestEntity(re);
                        int statusCode = client.executeMethod(method);
                        if (statusCode != HttpStatus.SC_OK) {
                            // don't invalidate the session more than 1 times each 5 minutes to avoid throttling issue
                            long timeNow = new Date().getTime();
                            if (timeNow - lastInvalidateByRetry > 5 * 60000) {
                                lastInvalidateByRetry = timeNow;
                                log.debug("Webservice call failed, try to invalidate the session");
                                logout(sid);
                                sid = login(username, password, ipAuth);
                                method = new PostMethod(endPointSearchService);
                                method.setRequestHeader("Cookie", "SID=" + sid);
                                re = new StringRequestEntity(paginatedMessage, "text/xml", "UTF-8");
                                method.setRequestEntity(re);
                                statusCode = client.executeMethod(method);
                            }
                            if (statusCode != HttpStatus.SC_OK) {
                                Header[] headers = method.getResponseHeaders();
                                log.error("----" + statusCode + "--------");
                                log.error("----" + sid + "--------");
                                if (headers != null) {
                                    for (Header h : headers) {
                                        log.error(h.getName() + " : " + h.getValue());
                                    }
                                }
                                log.error("----");
                                log.error(paginatedMessage);
                                log.error("----");
                                log.error(method.getResponseBodyAsString());
                                log.error("----");
                                throw new RuntimeException("Webservice call failed: " + method.getStatusLine());
                            }
                        }
                        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder builder = factory.newDocumentBuilder();
                        Document inDoc = builder.parse(method.getResponseBodyAsStream());
                        Element xmlRoot = inDoc.getDocumentElement();
                        Element tmp = XMLUtils.getSingleElement(xmlRoot, "soap:Body");
                        if (message.indexOf("<userQuery>") > 0) {
                            tmp = XMLUtils.getSingleElement(tmp, "ns2:searchResponse");
                        } else {
                            tmp = XMLUtils.getSingleElement(tmp, "ns2:retrieveByIdResponse");
                        }
                        tmp = XMLUtils.getSingleElement(tmp, "return");
                        String recordsFound = XMLUtils.getElementValue(tmp, "recordsFound");
                        if (!"0".equals(recordsFound)) {
                            Element records = XMLUtils.getSingleElement(tmp, "records");
                            Document newDoc = builder.parse(new InputSource(new ByteArrayInputStream(records
                                    .getTextContent().getBytes("UTF-8"))));
                            Element recordsElement = newDoc.getDocumentElement();
                            List<Element> recList = XMLUtils.getElementList(recordsElement, "REC");
                            for (int i = 0; i < recList.size(); i++) {
                                Element rec = recList.get(i);
                                results.add(WOSUtils.convertWosDomToRecord(rec));
                            }
                            if (recList.size() < MAX_COUNT
                                    || StringUtils.equals(recordsFound,
                                            String.valueOf(MAX_COUNT)))
                            {
                                lastPageReached = true;
                            }
                        }
                        else {
                            lastPageReached = true;
                        }
                        start += MAX_COUNT;
                    }
				} catch (ParserConfigurationException e) {
					log.error(e.getMessage(), e);
				} catch (SAXException e) {
					log.error(e.getMessage(), e);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		} catch (Exception e1) {
			log.error(e1.getMessage(), e1);
		}
		return results;
	}

	/**
	 * WoS allows a maximum of two search request in a single second window
	 * @throws InterruptedException
	 */
	private synchronized void waitIfNeeded() throws InterruptedException
    {
        if (numReqInSecond == 0) {
            lastRequest = new Date().getTime();
            numReqInSecond++;
            return;
        }
        else {
            long now = new Date().getTime();
            long expTime = now - lastRequest;
            if (expTime > 1000) {
                numReqInSecond = 1;
                lastRequest = now;
                return;
            }
            else {
                if (numReqInSecond >= 2) {
                    TimeUnit.MILLISECONDS.sleep(1001-expTime);
                    lastRequest = new Date().getTime();
                    numReqInSecond = 1;
                    return;
                }
                else {
                    numReqInSecond++;
                    return;
                }
            }
        }
    }

    //TODO databaseID not used
    public List<Record> searchByAffiliation(String userQuery, String databaseID, String symbolicTimeSpan, String start, String end,
            String username, String password, boolean ipAuth)
                    throws HttpException, IOException
    {        
        StringBuffer query = new StringBuffer("<userQuery>");
        query.append(userQuery);
        query.append("</userQuery>");
        if(StringUtils.isNotBlank(symbolicTimeSpan)) {
            query.append("<symbolicTimeSpan>");
            query.append(symbolicTimeSpan);
            query.append("</symbolicTimeSpan>");
        }
        else {
            query.append("<timeSpan>");
            query.append("<begin>");
            query.append(start);
            query.append("</begin>");
            query.append("<end>");
            query.append(end);
            query.append("</end>");
            query.append("</timeSpan>");
        }
        String message = SEARCH_HEAD_BY_AFFILIATION + query.toString() + SEARCH_END_BY_AFFILIATION;
        return internalSearch(message, username, password, ipAuth);
    }
}
