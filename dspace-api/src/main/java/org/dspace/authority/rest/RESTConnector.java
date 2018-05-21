/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.authority.rest;

<<<<<<< HEAD
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.apache.commons.lang.StringUtils;
=======
import org.apache.commons.lang3.StringUtils;
>>>>>>> refs/heads/dspace-6-rs
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
<<<<<<< HEAD
import org.dspace.services.ConfigurationService;
import org.dspace.utils.DSpace;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
=======

import java.io.InputStream;
import java.util.Scanner;
>>>>>>> refs/heads/dspace-6-rs

/**
 * @author l.pascarelli
 */
public class RESTConnector {

    /**
     * log4j logger
     */
    private static Logger log = Logger.getLogger(RESTConnector.class);

    private String url;

    private ClientConfig clientConfig = null;
    
    public RESTConnector(String url) {
        this.url = url;
    }

<<<<<<< HEAD
    public WebTarget getClientRest(String path) {
    	Client client = ClientBuilder.newClient(getClientConfig());
    	WebTarget target = client.target(url).path(path);
    	return target;
=======
    public InputStream get(String path, String accessToken) {
        InputStream result = null;
        path = trimSlashes(path);

        String fullPath = url + '/' + path;
        HttpGet httpGet = new HttpGet(fullPath);
        if(StringUtils.isNotBlank(accessToken)){
            httpGet.addHeader("Content-Type", "application/vnd.orcid+xml");
            httpGet.addHeader("Authorization","Bearer "+accessToken);
        }
        try {
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpResponse getResponse = httpClient.execute(httpGet);
            //do not close this httpClient
            result = getResponse.getEntity().getContent();

        } catch (Exception e) {
            getGotError(e, fullPath);
        }

        return result;
>>>>>>> refs/heads/dspace-6-rs
    }

	public ClientConfig getClientConfig() {
		if(this.clientConfig == null) {
	        ConfigurationService configurationService = new DSpace().getConfigurationService();
	        
	        String proxyHost =  configurationService.getProperty("http.proxy.host");
	        String proxyPortTmp = configurationService.getProperty("http.proxy.port");
	        
	        this.clientConfig = new ClientConfig();
	        if(StringUtils.isNotBlank(proxyHost)){
	        	
		        int proxyPort = (StringUtils.isNotBlank(proxyPortTmp))?Integer.parseInt(proxyPortTmp):80;
	        	this.clientConfig.connectorProvider(new ApacheConnectorProvider());
	            this.clientConfig.property(ClientProperties.PROXY_URI, proxyHost + ":" + proxyPort);
	        }
		}
		return clientConfig;
	}


}
