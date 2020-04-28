/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * https://github.com/CILEA/dspace-cris/wiki/License
 */
package org.dspace.app.cris.unpaywall.script;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpException;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.dspace.app.cris.service.ApplicationService;
import org.dspace.app.cris.unpaywall.UnpaywallService;
import org.dspace.app.cris.unpaywall.UnpaywallUtils;
import org.dspace.app.cris.unpaywall.model.Unpaywall;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.core.Email;
import org.dspace.core.I18nUtil;
import org.dspace.discovery.DiscoverQuery;
import org.dspace.discovery.DiscoverResult;
import org.dspace.discovery.SearchService;
import org.dspace.discovery.SearchServiceException;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;
import org.dspace.kernel.ServiceManager;
import org.dspace.utils.DSpace;

public class UnpaywallScript {

    private static Long cacheTime = ConfigurationManager.getLongProperty("unpaywall", "cachetime");
    private static final String DOI = ConfigurationManager.getProperty("unpaywall", "metadata.doi");
    private static final String apiKey = ConfigurationManager.getProperty("unpaywall", "apikey");
    private static final String url = ConfigurationManager.getProperty("unpaywall", "url");

    /** log4j logger */
    private static Logger log = Logger.getLogger(UnpaywallScript.class);

    private static Context context;
    
    private static ApplicationService applicationService;

    private static UnpaywallService sService;

    private static SearchService searcher;

    private static long timeElapsed = 3600000 * 24 * 7; // 1 week

    private static int maxItemToWork = 100;

    private static String queryDefault = "";

    private static int MAX_QUERY_RESULTS = 50;

    public static void main(String[] args)
            throws SearchServiceException, SQLException, AuthorizeException, ParseException, IOException, MessagingException {

    	context = new Context();
    	
        CommandLineParser parser = new PosixParser();

        Options options = new Options();
        options.addOption("h", "help", false, "help");

        options.addOption("t", "time", true,
                "Limit to update only citation more old than <t> seconds. Use 0 to force update of all record");

        options.addOption("q", "query", true,
                "Override the default query to retrieve puntual publication (used for test scope, the default query will be deleted");
        
        options.addOption("m", "mail", true,
                "Sena a mail notification to the selected user group (ex: \"authors\" or \"administrators\")");

        options.addOption("f", "nofulltext", false,
        		"Call Unpaywall Service for every item without fulltext");

        options.addOption("i", "fulltext", false,
        		"Delete line on Unpaywall table for every item with fulltext");
        
        CommandLine line = parser.parse(options, args);

        if (line.hasOption('h')) {
            HelpFormatter myhelp = new HelpFormatter();
            myhelp.printHelp("Unpaywall \n", options);
            System.out.println("\n\nUSAGE:\n Unpaywall [-t 3600] [-x 100]\n");
            System.exit(0);
        }

        ServiceManager serviceManager = new DSpace().getServiceManager();

        searcher = serviceManager.getServiceByName(SearchService.class.getName(), SearchService.class);

        applicationService = serviceManager.getServiceByName(
                "applicationService", ApplicationService.class);

        sService = serviceManager.getServiceByName(
                "unpaywallService", UnpaywallService.class);


        if (line.hasOption('t')) {
            cacheTime  = Long.valueOf(line.getOptionValue('t').trim()) * 1000; // option
                                                                                // is
                                                                                // in
                                                                                // seconds
        }
        if (line.hasOption('q')) {
        	
            int itemWorked = 0;
            int itemForceWorked = 0;
        	
        	queryDefault = line.getOptionValue('q').trim();

            long resultsTot = -1;
            try {
                context = new Context();
                context.turnOffAuthorisationSystem();
                all: for (int page = 0;; page++) {
                    int start = page * MAX_QUERY_RESULTS;
                    if (resultsTot != -1 && start >= resultsTot) {
                        break all;
                    }
                    if (maxItemToWork != 0 && itemWorked >= maxItemToWork  && itemForceWorked > 50)
                        break all;

                    // get all items that contains DOI 
                    DiscoverQuery query = new DiscoverQuery();
                    query.setStart(start);
                    query.setQuery(queryDefault);
                    query.setDSpaceObjectFilter(Constants.ITEM);
                    
                    DiscoverResult qresp = searcher.search(context, query);
                    resultsTot = qresp.getTotalSearchResults();

                    context.commit();
                    context.clearCache();
                }
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            } finally {

                if (context != null && context.isValid()) {
                    context.abort();
                }
            }
        	
        }
        if(line.hasOption('f')) {
        	updateUnpaywallCiting();
 	
        }
        if(line.hasOption('i')) {
        	removeFromUnpaywall();
        	
        }
        
        
        if (line.hasOption('m')) {
        	
        	String opt = line.getOptionValue('m');
        	
        	notifyUsers(opt);
        	
        }
    }
    
    private static void notifyUsers(String option) throws SearchServiceException, SQLException, IOException, MessagingException {
		
        SolrDocumentList docList = getUserListFromSOLR();
        
        Map<Integer, String> researcherList = new HashMap<>();
        
        if (docList.size() > 0)
        {
        	for (SolrDocument result : docList) {
        		
        		researcherList.put((Integer) result.getFieldValue("owner_i"), result.getFieldValue("cris-id").toString());
        		
        	}
        }else {
			return;
		}        
        
        
        Map<Integer, Map<Integer, Map<String, String>>> itemMap = new HashMap<>();
        List<Integer> res = new ArrayList<>(researcherList.keySet());
        
        for (int i = 0; i < res.size(); i++) {
			
        	String currResearcher = researcherList.get(res.get(i));
        	docList = getItemListFromSolr(currResearcher);
            
            if (docList.size() > 0)
            {
            	for (SolrDocument result : docList) {
            		if(itemMap.get(res.get(i)) == null) {

            			Map<Integer, Map<String, String>> tempMap = new HashMap<>();
            			Map<String, String> tempMap2 = new HashMap<>();
            			
            			tempMap2.put("doi", StringUtils.strip(result.getFieldValue("dc.identifier.doi").toString(), "[]"));
            			tempMap2.put("handle", result.getFieldValue("handle").toString());
            			tempMap.put((Integer) result.getFieldValue("search.resourceid"), 
            					tempMap2);
            			itemMap.put(res.get(i), tempMap);

            		}else {
            			Map<String, String> tempMap2 = new HashMap<>();
            			
            			tempMap2.put("doi", StringUtils.strip(result.getFieldValue("dc.identifier.doi").toString(), "[]"));
            			tempMap2.put("handle", result.getFieldValue("handle").toString());
            			itemMap.get(res.get(i))
            					.put(
            							(Integer) result.getFieldValue("search.resourceid"), 
            							tempMap2);

            		}
            	}
            	
            }else {
            	
				researcherList.remove(res.get(i));
				
			}
		}
        
        if(itemMap.isEmpty())
        {
        	return;
        }
        
        UnpaywallService unpaywallService = new UnpaywallService();
        Map<Integer, Unpaywall> foundUnpaywalls = new HashMap<>();
        for(Integer eper : itemMap.keySet())
        {
        	
        	Map<Integer, Map<String, String>> tempMap = itemMap.get(eper);
        	for (Integer id : tempMap.keySet()) {
        		
        		if(foundUnpaywalls.containsKey(id))
        		{
        			continue;
        		}
        		
        		String doi = tempMap.get(id).get("doi");
        		Unpaywall unpaywall = applicationService.uniqueByDOIAndItemID(UnpaywallUtils.resolveDoi(doi), id);
        		
        		if(unpaywall == null)
        		{
        			continue;
        		}
        		
        		foundUnpaywalls.put(id, unpaywall);
        		
			}
        }
        
        if (foundUnpaywalls.isEmpty()) {
			return;
		}
        
    	if(StringUtils.equals(option, "authors"))
    	{
    		
    		List<EPerson> ePersonList = new ArrayList<>();
    		for (Integer id : researcherList.keySet()) {
    			
    			ePersonList.add(EPerson.find(context, id));
    			
    		}
    		
    		for (EPerson ePerson : ePersonList) {
				
    			Email mail = getUnpaywallMailTemplate();
    			String email = ePerson.getEmail();
    			
    			if (StringUtils.isBlank(email)) {
					continue;
				}
    			
    			mail.addRecipient(email);
    			
    			Map<Integer, Map<String, String>> tempMap = itemMap.get(ePerson.getID());
    			

    	    	StringBuilder stringBuilder = new StringBuilder();
    			String itemString = makeItemListBuilder(stringBuilder, tempMap, foundUnpaywalls, 0).toString();
    			
    			if (StringUtils.isBlank(itemString)) {
    				continue;
				}
    			
    			mail.addArgument(itemString);
    			System.out.println("Sending mail to: " + email);
    			mail.send();
    			
			}
    		
    	}
    	
    	if(StringUtils.equals(option, "administrators") || StringUtils.isBlank(option))
    	{
    	
    		List<EPerson> ePersonList = Arrays.asList(Group.allMembers(context, 
    											Group.find(context, Group.ADMIN_ID)));
    		
    		for (EPerson ePerson : ePersonList) {
				
        		Email mail = getUnpaywallMailTemplate();
        		String email = ePerson.getEmail();
        		
        		if (StringUtils.isBlank(email)) {
					continue;
				}
    			
    			mail.addRecipient(email);
    			List<Map<Integer, Map<String, String>>> tempList = new ArrayList<>(itemMap.values());
    			StringBuilder stringBuilder = new StringBuilder();
    			Integer i = 0;
    			
    			for (Map<Integer, Map<String, String>> tempMap : tempList) {
    				
    				stringBuilder = makeItemListBuilder(stringBuilder, tempMap, foundUnpaywalls, i);
    				
				}
    			
    			String itemString = stringBuilder.toString();
    			
    			if (StringUtils.isBlank(itemString)) {
    				continue;
				}
    			
    			mail.addArgument(itemString);
    			System.out.println("Sending mail to: " + email);
    			mail.send();
    			
			}
    		
    	}
    	
	}
    
    private static Email getUnpaywallMailTemplate() throws IOException
    {
    	return Email.getEmail(ConfigurationManager.getProperty("dspace.dir") + "/config/emails/" + ConfigurationManager.getProperty("unpaywall", "script.email_template.name"));
    }
    
    private static StringBuilder makeItemListBuilder(StringBuilder stringBuilder, Map<Integer, Map<String, String>> itemMap, Map<Integer, Unpaywall> unpaywalls, Integer index) {
    	
		for (Integer itemId : itemMap.keySet()) {
			if (unpaywalls.get(itemId) != null) {
				
				index++;
				String handle = itemMap.get(itemId).get("handle");
				stringBuilder.append(index).append(". ");
				stringBuilder.append("DOI: ").append(itemMap.get(itemId).get("doi")).append(" - ");
				stringBuilder.append("Link: ").append(ConfigurationManager.getProperty("dspace.url")).append("handle/").append(handle).append(" \n");
				
			}
		}
		
		return stringBuilder;
	}
    
    private static SolrDocumentList getUserListFromSOLR() throws SearchServiceException
    {
    	
    	SolrQuery query = new SolrQuery();
    	query.setRows(Integer.MAX_VALUE);
        
        query.setQuery("search.resourcetype:9");
        
        query.addField("owner_i");
        query.addField("cris-id");
        
        query.addFilterQuery("owner_i:[* TO *]");
        
        //execute and parse the query
        SearchService searcher = new DSpace().getServiceManager()
                .getServiceByName(SearchService.class.getName(),
                        SearchService.class);
        QueryResponse qResp = searcher.search(query);
        return qResp.getResults();
    	
    }
    
    private static SolrDocumentList getItemListFromSolr(String id) throws SearchServiceException
    {
    	
    	SolrQuery query = new SolrQuery();
        query.setRows(Integer.MAX_VALUE);
        
        query.setQuery("search.resourcetype:2");
        
        query.addField("search.resourceid");
        query.addField("dc.identifier.doi");
        query.addField("handle");
        
        query.addFilterQuery("author_authority:\"" + id + "\"");
        query.addFilterQuery("dc.identifier.doi:[* TO *]");
        String fulltext = I18nUtil
    			.getMessage("defaultvalue.fulltextdescription.nofulltext");
        query.addFilterQuery("infofulltext_keyword:\"" + fulltext + "\"");
        
        //execute and parse the query
        SearchService searcher = new DSpace().getServiceManager()
                .getServiceByName(SearchService.class.getName(),
                        SearchService.class);
        QueryResponse qResp = searcher.search(query);
        return qResp.getResults();
    	
    }
    
    private static void removeFromUnpaywall() throws SearchServiceException{

    	String fulltext = I18nUtil
    			.getMessage("defaultvalue.fulltextdescription.fulltext");
		
    	String metadataDOI = ConfigurationManager.getProperty("unpaywall",
                "metadata.doi");
    	
    	SolrQuery sQuery = new SolrQuery("item.fulltext_s:\""+fulltext+"\" AND "+metadataDOI+":*");
    	sQuery.setRows(Integer.MAX_VALUE);
		QueryResponse qResp = searcher.search(sQuery);
		
		if (qResp.getResults() != null) {
			SolrDocumentList solrDocList = qResp.getResults();
			for(SolrDocument result : solrDocList) {
				Integer itemID = (Integer)result.getFieldValue("search.resourceid");
				String doi = ((List<Object>)result.getFieldValues(metadataDOI)).get(0).toString();
				if (itemID != null && StringUtils.isNotBlank(doi)) {
					Unpaywall unpaywall = applicationService.uniqueByDOIAndItemID(UnpaywallUtils.resolveDoi(doi), itemID);
					if (unpaywall != null) {
						applicationService.delete(Unpaywall.class, unpaywall.getId());
					}
				}
			}
		}
    }
    
    private static void updateUnpaywallCiting(){
    	
    	String nofulltext = I18nUtil
    			.getMessage("defaultvalue.fulltextdescription.nofulltext");
    	
    	String metadataDOI = ConfigurationManager.getProperty("unpaywall",
                "metadata.doi");
    	
    	SolrQuery sQuery = new SolrQuery("item.fulltext_s:\""+nofulltext+"\" AND "+metadataDOI+":*");
    	sQuery.setRows(Integer.MAX_VALUE);
		QueryResponse qResp;
		try {
			qResp = searcher.search(sQuery);
			if (qResp.getResults() != null) {
				SolrDocumentList solrDocList = qResp.getResults();
				for(SolrDocument result : solrDocList) {
					Integer itemID = (Integer)result.getFieldValue("search.resourceid");
					String doi = ((List<Object>)result.getFieldValues(metadataDOI)).get(0).toString();
					if (itemID != null && StringUtils.isNotBlank(doi)) {
						sService.searchByDOI(doi, itemID);
					}
				}
			}
		} catch (SearchServiceException | HttpException e) {
			log.error(e.getMessage(), e);
		}
    }
}
    
    