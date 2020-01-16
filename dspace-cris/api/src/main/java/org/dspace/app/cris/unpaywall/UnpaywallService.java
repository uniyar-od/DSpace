package org.dspace.app.cris.unpaywall;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URISyntaxException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.dspace.app.cris.unpaywall.model.Unpaywall;
import org.dspace.app.cris.unpaywall.services.UnpaywallPersistenceService;
import org.dspace.core.ConfigurationManager;
import org.dspace.utils.DSpace;
import org.json.JSONArray;
import org.json.JSONObject;

import it.cilea.osd.common.core.SingleTimeStampInfo;
import it.cilea.osd.common.core.TimeStampInfo;

public class UnpaywallService 
{
    private CloseableHttpClient client = null;

    private UnpaywallPersistenceService unpaywallPersistenceService;
    
    private int timeout;

    /** log4j category */
    private static final Logger log = Logger.getLogger(UnpaywallService.class);

    public UnpaywallService()
    {
        HttpClientBuilder custom = HttpClients.custom();
        client = custom.disableAutomaticRetries().setMaxConnTotal(5)
                .setDefaultSocketConfig(
                        SocketConfig.custom().setSoTimeout(timeout).build())
                .build();
    }

    public Unpaywall unpaywallCall(String doi) throws HttpException
    {
        String endpoint = ConfigurationManager.getProperty("unpaywall", "url");
        String apiKey = ConfigurationManager.getProperty("unpaywall", "apikey");

        Unpaywall unpaywall = new Unpaywall();
        UnpaywallRecord unpaywallRecord = new UnpaywallRecord();

        HttpGet method = null;

        try
        {
            endpoint = endpoint + doi;
            URIBuilder uriBuilder = new URIBuilder(endpoint);
            uriBuilder.addParameter("email", apiKey);
            method = new HttpGet(uriBuilder.build());
        }
        catch (URISyntaxException ex)
        {
            throw new HttpException("Request not sent", ex);
        }
        try
        {
            HttpResponse response = client.execute(method);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK)
            {
                throw new RuntimeException("Http call failed: " + statusLine);
            }

            /*
             * aggiorna riga sul DB
             */
            UnpaywallBestOA unpBestOA = new UnpaywallBestOA();

            InputStream is = response.getEntity().getContent();
            StringWriter writer = new StringWriter();
            IOUtils.copy(is, writer, "UTF-8");
            String source = writer.toString();
            JSONObject obj = new JSONObject(source);

//            SPOSTARE IN UNPAYWALLSTEP
            JSONObject bestOA = obj.getJSONObject("best_oa_location");
            unpBestOA.setUrl_for_pdf(bestOA.getString("url_for_pdf"));
            unpBestOA.setHost_type(bestOA.getString("host_type"));
            unpBestOA.setVersion(bestOA.getString("version"));
            unpBestOA.setEvidence(bestOA.getString("evidence"));

            JSONArray oaLocations = obj.getJSONArray("oa_locations");
            UnpaywallOA[] unpaywallOAArray = new UnpaywallOA[oaLocations
                    .length()];
            UnpaywallOA unpaywallOA = new UnpaywallOA();

//            SETTING UNPAYWALL OA LOCATIONS
            for (int i = 0; i < oaLocations.length(); i++)
            {
                unpaywallOA.setEndpoint_id(
                        oaLocations.getJSONObject(i).optString("endpoint_id"));
                unpaywallOA.setEvidence(
                        oaLocations.getJSONObject(i).optString("evidence"));
                unpaywallOA.setHost_type(
                        oaLocations.getJSONObject(i).optString("host_type"));
                unpaywallOA.setIs_best(
                        oaLocations.getJSONObject(i).optBoolean("is_best"));
                unpaywallOA.setLicense(
                        oaLocations.getJSONObject(i).optString("license"));
                unpaywallOA.setPmh_id(
                        oaLocations.getJSONObject(i).optString("pmh_id"));
                unpaywallOA.setRepository_institution(oaLocations
                        .getJSONObject(i).optString("repository_institution"));
                unpaywallOA.setUpdated(
                        oaLocations.getJSONObject(i).optString("updated"));
                unpaywallOA
                        .setUrl(oaLocations.getJSONObject(i).optString("url"));
                unpaywallOA.setUrl_for_landing_page(oaLocations.getJSONObject(i)
                        .optString("url_for_landing_page"));
                unpaywallOA.setUrl_for_pdf(
                        oaLocations.getJSONObject(i).optString("url_for_pdf"));
                unpaywallOA.setVersion(
                        oaLocations.getJSONObject(i).optString("version"));
                unpaywallOAArray[i] = unpaywallOA;
            }

            unpaywallRecord.setDoi(doi);
            unpaywallRecord.setPublished_date(obj.optString("published_date"));
            unpaywallRecord.setUpdated("updated");
            unpaywallRecord.setUnpaywallBestOA(unpBestOA);
            unpaywallRecord.setUnpaywallOA(unpaywallOAArray);

            unpaywall.setDOI(doi);
            unpaywall.setTimeStampInfo(new TimeStampInfo());
            unpaywall.setDOI(doi);
            unpaywall.setUnpaywallRecord(unpaywallRecord);
            unpaywall.setUnpaywallJson(obj);
            method.releaseConnection();
            getUnpaywallPersistenceService().saveOrUpdate(Unpaywall.class, unpaywall);
        }
        catch (ClientProtocolException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally
        {
            if (method != null)
            {
                method.releaseConnection();
            }
        }
        return unpaywall;
    }

    public Unpaywall internalCall(String doi) throws HttpException
    {
        Long cacheTime = ConfigurationManager.getLongProperty("unpaywall",
                "cachetime") * 1000;

        Unpaywall unpaywall = null;

        if (StringUtils.isBlank(doi))
        {
            log.warn("trying to call unpaywall service with blank or null DOI");
            return null;
        }
        
        unpaywall = getUnpaywallPersistenceService().uniqueByDOI(doi);

        // If nothing is found locally call the service and exit
        if (unpaywall == null)
        {
            unpaywall = unpaywallCall(doi);
            return unpaywall;
        }
        
        Long currentDate = System.currentTimeMillis();
        SingleTimeStampInfo lastModified = unpaywall.getTimeStampInfo().getTimestampLastModified();
        Long lastCall;
        if(lastModified == null)
        {
        	lastCall = unpaywall.getTimeStampInfo().getTimestampCreated().getTimestamp().getTime();
        }else {
			lastCall = unpaywall.getTimeStampInfo().getTimestampLastModified().getTimestamp().getTime(); 
		}
        
        long diffTime = currentDate - lastCall;
        if ( diffTime <= cacheTime)
        {
            return unpaywall;
        }
        else
        {
            unpaywall = unpaywallCall(doi);
            return unpaywall;
        }
    }

    public void setTimeout(int timeout)
    {
        this.timeout = timeout;
    }

	public UnpaywallPersistenceService getUnpaywallPersistenceService() {
		if(unpaywallPersistenceService == null)
		{
			unpaywallPersistenceService = new DSpace().getSingletonService(UnpaywallPersistenceService.class);
		}
		return unpaywallPersistenceService;
	}

	public void setUnpaywallPersistenceService(UnpaywallPersistenceService unpaywallPersistenceService) {
		this.unpaywallPersistenceService = unpaywallPersistenceService;
	}

}
