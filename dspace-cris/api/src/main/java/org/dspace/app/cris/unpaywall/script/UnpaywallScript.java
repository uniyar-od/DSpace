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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.dspace.app.cris.unpaywall.UnpaywallService;
import org.dspace.app.cris.unpaywall.dao.UnpaywallDAO;
import org.dspace.app.cris.unpaywall.model.Unpaywall;
import org.dspace.app.cris.unpaywall.services.UnpaywallPersistenceService;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Item;
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
import org.dspace.kernel.ServiceManager;
import org.dspace.utils.DSpace;

import com.fasterxml.jackson.annotation.JsonFormat.Value;

public class UnpaywallScript {

    private static Long cacheTime = ConfigurationManager.getLongProperty("unpaywall", "cachetime");
    private static final String DOI = ConfigurationManager.getProperty("unpaywall", "metadata.doi");
    private static final String apiKey = ConfigurationManager.getProperty("unpaywall", "apikey");
    private static final String url = ConfigurationManager.getProperty("unpaywall", "url");

    /** log4j logger */
    private static Logger log = Logger.getLogger(UnpaywallScript.class);

    private static Context context;
    
    private static UnpaywallPersistenceService pService;

    private static UnpaywallDAO unpaywallDAO;

    private static UnpaywallService sService;

    private static SearchService searcher;

    private static long timeElapsed = 3600000 * 24 * 7; // 1 week

    private static int maxItemToWork = 100;

    private static String queryDefault = "";

    private static int MAX_QUERY_RESULTS = 50;

    public static void main(String[] args)
            throws SearchServiceException, SQLException, AuthorizeException, ParseException, IOException {

    	context = new Context();
    	
        CommandLineParser parser = new PosixParser();

        Options options = new Options();
        options.addOption("h", "help", false, "help");

        options.addOption("t", "time", true,
                "Limit to update only citation more old than <t> seconds. Use 0 to force update of all record");

        options.addOption("q", "query", true,
                "Override the default query to retrieve puntual publication (used for test scope, the default query will be deleted");
        
        options.addOption("m", "mail", true,
                "send dem mails");

        options.addOption("f", "nofulltext", true,
        		"Call Unpaywall Service for every item without fulltext");

        options.addOption("i", "fulltext", true,
        		"Delete line on Unpaywall table for every item with fulltext");
        
        CommandLine line = parser.parse(options, args);

        if (line.hasOption('h')) {
            HelpFormatter myhelp = new HelpFormatter();
            myhelp.printHelp("Unpaywall \n", options);
            System.out.println("\n\nUSAGE:\n Unpaywall [-t 3600] [-x 100]\n");
            System.exit(0);
        }

        DSpace dspace = new DSpace();
        int itemWorked = 0;
        int itemForceWorked = 0;

        if (line.hasOption('q')) {
            queryDefault = line.getOptionValue('q').trim();
        }

        if (line.hasOption('t')) {
            cacheTime  = Long.valueOf(line.getOptionValue('t').trim()) * 1000; // option
                                                                                // is
                                                                                // in
                                                                                // seconds
        }
        if(line.hasOption('f')) {
        	updateUnpaywallCiting();
 	
        }
        if(line.hasOption('i')) {
        	removeFromUnpaywall();
        	
        }
        
        /*
        
        ServiceManager serviceManager = dspace.getServiceManager();

        searcher = serviceManager.getServiceByName(SearchService.class.getName(), SearchService.class);

        pService = serviceManager.getServiceByName(UnpaywallPersistenceService.class.getName(),
                UnpaywallPersistenceService.class);

        sService = serviceManager.getServiceByName(UnpaywallService.class.getName(), UnpaywallService.class);

        Context context = null;
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
     */
        
        
        if (line.hasOption('m')) {
        	
        	String opt = line.getOptionValue('m');
        	
        	notifyUsers(opt);
        	
        }
    }
    
    private static void notifyUsers(String option) throws SearchServiceException, SQLException, IOException {
		
        SolrDocumentList docList = query1();
        
        Map<String, Integer> researcherList = new HashMap<>();
        
        if (docList.size() > 0)
        {
        	for (SolrDocument result : docList) {
        		
        		researcherList.put(result.getFieldValue("cris-id").toString(), (Integer) result.getFieldValue("owner_i"));
        		
        	}
        }else {
			return;
		}        
        
        
        Map<String, Map<Integer, String>> itemIdMap = new HashMap<>();
        List<String> res = new ArrayList<>(researcherList.keySet());
        
        for (int i = 0; i < res.size(); i++) {
			
        	String currResearcher = res.get(i);
        	docList = query2(currResearcher);
            
            if (docList.size() > 0)
            {
            	for (SolrDocument result : docList) {
            		
            		Object value = result.getFieldValue("dc.identifier.doi");
            		
            		if (value != null && StringUtils.isNotBlank(value.toString())) {
						if(itemIdMap.get(currResearcher) == null) {
							
							Map<Integer, String> tempMap = new HashMap<>();
							tempMap.put((Integer) result.getFieldValue("search.resourceid"), 
										result.getFieldValue("dc.identifier.doi").toString());
							itemIdMap.put(res.get(i).toString(), tempMap);
							
						}else {
							
							itemIdMap.get(currResearcher)
										.put(
											(Integer) result.getFieldValue("search.resourceid"), 
											result.getFieldValue("dc.identifier.doi").toString()
												);
							
						}
					}
            	}
            }else {
            	
				researcherList.remove(currResearcher);
				
			}
		}
        
        if(itemIdMap.isEmpty())
        {
        	return;
        }
        
        UnpaywallService unpaywallService = new UnpaywallService();
        Map<Integer, Unpaywall> foundUnpaywalls = new HashMap<>();
        for(String researcher : itemIdMap.keySet())
        {
        	
        	Map<Integer, String> tempMap = itemIdMap.get(researcher);
        	for (Integer id : tempMap.keySet()) {
        		
        		if(foundUnpaywalls.containsKey(id))
        		{
        			continue;
        		}
        		
        		String doi = tempMap.get(id);
        		Unpaywall unpaywall = unpaywallService.getUnpaywallPersistenceService().uniqueByDOIAndItemID(doi, id);
        		
        		if(unpaywall == null)
        		{
        			continue;
        		}
        		
        		foundUnpaywalls.put(id, unpaywall);
        		
			}
        }
        
    	if(StringUtils.equals(option, "authors"))
    	{
    		
    		List<EPerson> ePersonList = new ArrayList<>();
    		for (Integer id : researcherList.values()) {
    			
    			ePersonList.add(EPerson.find(context, id));
    			
    		}
    		
    		for (EPerson ePerson : ePersonList) {
				
    			Email mail = Email.getEmail("unpaywall_alert");
    			String email = ePerson.getEmail();
    			
    			if (StringUtils.isBlank(email)) {
					continue;
				}
    			
    			mail.addRecipient(email);
//    			mail.addArgument(arg);
    			//finish building mail
    			
			}
    		
    		mailToAuthors();
    		
    	}
    	
    	if(StringUtils.equals(option, "administrators"))
    	{
    	
    		
    		Email mail = new Email();
    		
    		//finish building email
    		
    		mailToAdmin(option);
    	}
    	
	}
    
    private static SolrDocumentList query1() throws SearchServiceException
    {
    	
    	SolrQuery query = new SolrQuery();
        query.setRequestHandler("/select");
        query.setRows(10000);
        
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
    
    private static SolrDocumentList query2(String id) throws SearchServiceException
    {
    	
    	SolrQuery query = new SolrQuery();
        query.setRequestHandler("/select");
        query.setRows(10000);
        
        query.setQuery("search.resourcetype:2");
        
        query.addField("search.resourceid");
        query.addField("dc.identifier.doi");
        
        query.addFilterQuery("author_authority:\"" + id + "\"");
        query.addFilterQuery("dc.identifier.doi:[* TO *]");
        String fulltext = I18nUtil
    			.getMessage("defaultvalue.fulltextdescription.nofulltext");  //TODO Does not do a exact match??
        query.addFilterQuery("item.fulltext:\"" + fulltext + "\"");
        
        //execute and parse the query
        SearchService searcher = new DSpace().getServiceManager()
                .getServiceByName(SearchService.class.getName(),
                        SearchService.class);
        QueryResponse qResp = searcher.search(query);
        return qResp.getResults();
    	
    }
    
    private static void mailToAdmin(String option) {
		
    	
    	
	}
    
    private static void mailToAuthors() {
		
    	
    	
	}
    
    private static void updateUnpaywallCiting() throws SearchServiceException{

    	String fulltext = I18nUtil
    			.getMessage("defaultvalue.fulltextdescription.fulltext");
		
    	String metadataDOI = ConfigurationManager.getProperty("unpaywall",
                "metadata.doi");
    	
    	SolrQuery sQuery = new SolrQuery("item.fulltext:\""+fulltext);
		QueryResponse qResp = searcher.search(sQuery);
		
		if (qResp.getResults() != null) {
			SolrDocumentList solrDocList = qResp.getResults();
			for(SolrDocument result : solrDocList) {
				int itemID = (int)result.getFieldValue("search.resourceid");
				String doiValue = result.getFieldValue(metadataDOI).toString();
					Unpaywall unpaywall = pService.uniqueByDOIAndItemID(doiValue, itemID);
					pService.delete(Unpaywall.class, unpaywall.getId());
			}
		}
    }
    
    private static void removeFromUnpaywall(){

    	String metadataDOI = ConfigurationManager.getProperty("unpaywall",
                "metadata.doi");
    	
    	SolrQuery sQuery = new SolrQuery("!item.fulltext:*");
		QueryResponse qResp;
		try {
			qResp = searcher.search(sQuery);
		if (qResp.getResults() != null) {
			SolrDocumentList solrDocList = qResp.getResults();
			for(SolrDocument result : solrDocList) {
				String doiValue = result.getFieldValue(metadataDOI).toString();
				sService.searchByDOI(doiValue);
				}
			}
		} catch (SearchServiceException | HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
    
    