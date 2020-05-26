/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.cris.unpaywall.script;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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
import org.dspace.app.cris.unpaywall.UnpaywallBestOA;
import org.dspace.app.cris.unpaywall.UnpaywallRecord;
import org.dspace.app.cris.unpaywall.UnpaywallService;
import org.dspace.app.cris.unpaywall.UnpaywallUtils;
import org.dspace.app.cris.unpaywall.model.Unpaywall;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.core.Email;
import org.dspace.core.I18nUtil;
import org.dspace.discovery.SearchService;
import org.dspace.discovery.SearchServiceException;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;
import org.dspace.kernel.ServiceManager;
import org.dspace.utils.DSpace;

public class UnpaywallScript {

    /** log4j logger */
    private static Logger log = Logger.getLogger(UnpaywallScript.class);

    private static Context context;
    
    private static ApplicationService applicationService;

    private static UnpaywallService sService;

    private static SearchService searcher;

    private static String fulltext;

    private static String nofulltext;

    private static String metadataDOI;

    private static final String EMAIL_TO_AUTHORS = "authors";

    private static final String EMAIL_TO_ADMINISTRATORS = "administrators";

    public static void main(String[] args)
            throws SearchServiceException, SQLException, AuthorizeException, ParseException, IOException, MessagingException {

        log.info("#### START UnpaywallScript: -----" + new Date() + " ----- ####");

        CommandLineParser parser = new PosixParser();

        Options options = new Options();
        options.addOption("h", "help", false, "help");

        options.addOption("m", "mail", true,
                "Send a mail notification to the selected user group (ex: \"authors\" or \"administrators\")");

        options.addOption("f", "nofulltext", false,
        		"Call Unpaywall Service for every item without fulltext");

        options.addOption("i", "fulltext", false,
        		"Delete line on Unpaywall table for every item with fulltext");
        
        CommandLine line = parser.parse(options, args);

        if (line.hasOption('h')) {
            HelpFormatter myhelp = new HelpFormatter();
            myhelp.printHelp("Unpaywall \n", options);
            System.out.println("\n\nUSAGE:\n Unpaywall [-m authors|administrators] [-f] [-i]\n");
            System.exit(0);
        }

        context = new Context();

        ServiceManager serviceManager = new DSpace().getServiceManager();

        searcher = serviceManager.getServiceByName(SearchService.class.getName(), SearchService.class);

        applicationService = serviceManager.getServiceByName(
                "applicationService", ApplicationService.class);

        sService = serviceManager.getServiceByName(
                "unpaywallService", UnpaywallService.class);

        fulltext = I18nUtil
            .getMessage("defaultvalue.fulltextdescription.fulltext");

        nofulltext = I18nUtil
            .getMessage("defaultvalue.fulltextdescription.nofulltext");

        metadataDOI = ConfigurationManager.getProperty("unpaywall",
                "metadata.doi");

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

        log.info("#### END UnpaywallScript: -----" + new Date() + " ----- ####");
        System.exit(0);
    }
    
    private static void notifyUsers(String option) throws SearchServiceException, SQLException, IOException, MessagingException {
        // retrieve items without fulltext and with doi from Solr
        SolrDocumentList documents = retrieveItemFromSolr();

        // for each item check local Unpaywall table
        List<UnpaywallItem> unpaywallItems = checkItemOnUnpaywall(documents);

        if (option.equalsIgnoreCase(EMAIL_TO_AUTHORS)) {
            // for each item retrieve owner and create map owner2Items
            Map<Integer, List<UnpaywallItem>> owner2Items = retrieveOwnerFromSolr(unpaywallItems);

            // for each owner send email
            for (Integer ownerI : owner2Items.keySet()) {
                EPerson ePerson = EPerson.find(context, ownerI);

                Email mail = getUnpaywallMailTemplate(EMAIL_TO_AUTHORS);

                if (StringUtils.isBlank(ePerson.getEmail())) continue;

                mail.addRecipient(ePerson.getEmail());

                String emailBody = makeItemListBuilder(owner2Items.get(ownerI)).toString();
                mail.addArgument(emailBody);

                System.out.println("Sending mail to user: " + ePerson.getEmail());
                log.info("Sending mail to user: " + ePerson.getEmail());
                mail.send();
            }
        }
        else {
            // send email to administrators
            Email mail = getUnpaywallMailTemplate(EMAIL_TO_ADMINISTRATORS);

            String adminEmail = ConfigurationManager.getProperty("mail.admin");
            mail.addRecipient(adminEmail);

            List<String> adminEmails = new ArrayList<>();
            for (EPerson admin : Arrays.asList(Group.allMembers(context, Group.find(context, Group.ADMIN_ID)))) {
                adminEmails.add(admin.getEmail());
                mail.addRecipientCC(admin.getEmail());
            }

            String emailBody = makeItemListBuilder(unpaywallItems).toString();
            mail.addArgument(emailBody);

            System.out.println("Sending mail to administrator: " + adminEmail + " with CC to administrators: " + StringUtils.strip(Arrays.toString(adminEmails.toArray()), "[]"));
            log.info("Sending mail to administrator: " + adminEmail + " with CC to administrators: " + StringUtils.strip(Arrays.toString(adminEmails.toArray()), "[]"));
            mail.send();
        }
    }

    private static SolrDocumentList retrieveItemFromSolr() throws SearchServiceException {
        SolrQuery sQuery = new SolrQuery("item.fulltext_s:\""+nofulltext+"\" AND "+metadataDOI+":*");
        sQuery.setRows(Integer.MAX_VALUE);
        sQuery.addField("search.resourceid");
        sQuery.addField("handle");
        sQuery.addField(metadataDOI);
        sQuery.addField("author_authority");

        QueryResponse qResp;
        try {
            qResp = searcher.search(sQuery);
            if (qResp.getResults() != null && qResp.getResults().size() > 0) {
                return qResp.getResults();
            }
        } catch (SearchServiceException e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }

    private static List<UnpaywallItem> checkItemOnUnpaywall(SolrDocumentList documents) {
        List<UnpaywallItem> unpaywallItems = new ArrayList<>();

        for(SolrDocument document : documents) {
            Integer itemID = (Integer)document.getFieldValue("search.resourceid");
            String handle = (String)document.getFieldValue("handle");
            String doi = ((List<Object>)document.getFieldValues(metadataDOI)).get(0).toString();

            HashSet<Object> authors = new HashSet<>();
            List<Object> authorsList = (List<Object>)document.getFieldValues("author_authority");
            if (authorsList != null) {
                authors = new HashSet<>(authorsList);
            }

            if (itemID != null && StringUtils.isNotBlank(doi)) {
                Unpaywall unpaywall = applicationService.uniqueByDOIAndItemID(UnpaywallUtils.resolveDoi(doi), itemID);
                if (unpaywall != null && unpaywall.getJsonRecord() != null) {
                	UnpaywallRecord record = UnpaywallUtils.convertStringToUnpaywallRecord(unpaywall.getJsonRecord());
                	if (record.getUnpaywallBestOA() !=null && StringUtils.isNotBlank(record.getUnpaywallBestOA().getUrl_for_pdf())) {						
                		unpaywallItems.add(new UnpaywallItem(itemID, handle, doi, authors));
					}
                }
            }
        }

        return unpaywallItems;
    }

    private static Map<Integer, List<UnpaywallItem>> retrieveOwnerFromSolr(List<UnpaywallItem> unpaywallItems) {
        Map<Integer, List<UnpaywallItem>> owner2Items = new HashMap<>();

        for (UnpaywallItem unpaywallItem : unpaywallItems) {
            StringBuilder authorsQuery = new StringBuilder();
            for (Object author : unpaywallItem.getAuthors()) {
                authorsQuery.append("cris-id:" + author + " OR ");
            }

            if (StringUtils.isBlank(authorsQuery.toString())) continue;

            String sAuthorsQuery = authorsQuery.toString();
            sAuthorsQuery = sAuthorsQuery.substring(0, sAuthorsQuery.length()-4);

            SolrQuery sQuery = new SolrQuery("search.resourcetype:9 AND owner_i:* AND (" + sAuthorsQuery + ")");
            sQuery.setRows(Integer.MAX_VALUE);
            sQuery.addField("owner_i");

            QueryResponse qResp;
            try {
                qResp = searcher.search(sQuery);
                if (qResp.getResults() != null && qResp.getResults().size() > 0) {
                    for(SolrDocument document : qResp.getResults()) {
                        Integer ownerI = (Integer) document.getFieldValue("owner_i");

                        if (!owner2Items.containsKey(ownerI)) {
                            owner2Items.put(ownerI, new ArrayList<UnpaywallItem>());
                        }
                        owner2Items.get(ownerI).add(unpaywallItem);
                    }
                }
            } catch (SearchServiceException e) {
                log.error(e.getMessage(), e);
            }
        }

        return owner2Items;
    }
    
    private static Email getUnpaywallMailTemplate(String option) throws IOException
    {
        return Email.getEmail(ConfigurationManager.getProperty("dspace.dir") + "/config/emails/" + ConfigurationManager.getProperty("unpaywall", "script." + option + "_email_template.name"));
    }
    
    private static String makeItemListBuilder(List<UnpaywallItem> unpaywallItems) {
    	Integer notificationLimit = ConfigurationManager.getIntProperty("unpaywall", "mail.item.limit", 25);
        int index = 1;
        StringBuilder stringBuilder = new StringBuilder();
        for (UnpaywallItem unpaywallItem : unpaywallItems) {
        	if (index > notificationLimit) {
				stringBuilder.append("The above list shows ").append(notificationLimit).append(" records on a total of ").append(unpaywallItems.size()).append(" /n");
				break;
			}
        	
            String handleLink = ConfigurationManager.getProperty("dspace.url") + "/handle/" + unpaywallItem.getHandle();
            stringBuilder.append(index++).append(". ")
                .append("DOI: ").append(unpaywallItem.getDoi()).append(" - ")
                .append("Link: ").append(handleLink).append("\n");
        }

        return stringBuilder.toString();
    }

    private static void removeFromUnpaywall() throws SearchServiceException{

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
						System.out.println("Delete item from local Unpaywall registry with id: " + itemID + " and doi: " + doi);
						log.info("Delete item from local Unpaywall registry with id: " + itemID + " and doi: " + doi);
						applicationService.delete(Unpaywall.class, unpaywall.getId());
					}
				}
			}
		}
    }
    
    private static void updateUnpaywallCiting(){

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
						System.out.println("Update item with id: " + itemID + " and doi: " + doi);
						log.info("Update item with id: " + itemID + " and doi: " + doi);
						sService.searchByDOI(doi, itemID);
					}
				}
			}
		} catch (SearchServiceException | HttpException e) {
			log.error(e.getMessage(), e);
		}
    }

    private static class UnpaywallItem {
        private Integer id;
        private String handle;
        private String doi;
        private HashSet<Object> authors;

        public UnpaywallItem(Integer id, String handle, String doi, HashSet<Object> authors) {
            this.id = id;
            this.handle = handle;
            this.doi = doi;
            this.authors = authors;
        }
        public Integer getId() {
            return id;
        }
        public void setId(Integer id) {
            this.id = id;
        }
        public String getHandle() {
            return handle;
        }
        public void setHandle(String handle) {
            this.handle = handle;
        }
        public String getDoi() {
            return doi;
        }
        public void setDoi(String doi) {
            this.doi = doi;
        }
        public HashSet<Object> getAuthors() {
            return authors;
        }
        public void setAuthors(HashSet<Object> authors) {
            this.authors = authors;
        }
    }
}
    
    