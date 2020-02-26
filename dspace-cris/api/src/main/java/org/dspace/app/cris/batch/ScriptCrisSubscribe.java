/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * https://github.com/CILEA/dspace-cris/wiki/License
 */
/*
 * Subscribe.java
 *
 * Version: $Revision: 3762 $
 *
 * Date: $Date: 2009-05-07 06:36:47 +0200 (gio, 07 mag 2009) $
 *
 * Copyright (c) 2002-2009, The DSpace Foundation.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * - Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Neither the name of the DSpace Foundation nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 */
package org.dspace.app.cris.batch;

import java.io.IOException;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.mail.MessagingException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.dspace.app.cris.configuration.RelationConfiguration;
import org.dspace.app.cris.discovery.CrisSearchService;
import org.dspace.app.cris.discovery.OwnerRPAuthorityIndexer;
import org.dspace.app.cris.integration.CrisComponentsService;
import org.dspace.app.cris.integration.ICRISComponent;
import org.dspace.app.cris.model.ACrisObject;
import org.dspace.app.cris.model.CrisConstants;
import org.dspace.app.cris.model.CrisSubscription;
import org.dspace.app.cris.service.ApplicationService;
import org.dspace.app.cris.service.CrisSubscribeService;
import org.dspace.app.cris.util.Researcher;
import org.dspace.content.Item;
import org.dspace.content.Metadatum;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.core.Email;
import org.dspace.core.I18nUtil;
import org.dspace.core.LogManager;
import org.dspace.discovery.SearchService;
import org.dspace.discovery.SearchServiceException;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Subscribe;
import org.dspace.handle.HandleManager;
import org.dspace.utils.DSpace;

/**
 * Class defining methods for sending new item e-mail alerts to users. Based on
 * {@link Subscribe} written by Robert Tansley
 * 
 * @author Luigi Andrea Pascarelli
 * @author Andrea Bollini
 */
public class ScriptCrisSubscribe
{
    /** log4j logger */
    private static Logger log = Logger.getLogger(ScriptCrisSubscribe.class);
    
	/**
	 * Process subscriptions. This must be invoked only once a day. Messages are
	 * only sent out when a subscribed cris object has actually received new or
	 * updated related items, so that people's mailboxes are not clogged with many
	 * "no new items" mails.
	 * <P>
	 * Yesterday's newly available items are included. If this is run at for example
	 * midday, any items that have been made available during the current day will
	 * not be included, but will be included in the next day's run.
	 * <P>
	 * For example, if today's date is 2002-10-10 (in UTC) items made available
	 * during 2002-10-09 (UTC) will be included.
	 * 
	 * The eperson.subscription.onlynew configuration can be set to true to force
	 * notification only about new item, by default updated items are included as
	 * well
	 * 
	 * @param researcher         the CRIS Researcher spring service
	 * @param applicationService
	 * 
	 * @param context            DSpace context object * @param mapRelationFields a
	 *                           map where the key is the integer representing the
	 *                           cris object type and the values are all the
	 *                           RelationConfiguration available for such type. For
	 *                           ResearcherObject all the relations are exposed
	 *                           under the start type ID
	 * 
	 * @param test               if true the email content is printed in the log
	 *                           instead than send any real email
	 * @throws SearchServiceException, IOException, SQLException
	 */
    public static void processDaily(Researcher researcher,
            ApplicationService applicationService, Context context, Map<Integer, Set<RelationConfiguration>> mapRelationFields, boolean test)
            throws SQLException, IOException, SearchServiceException
    {
        List<CrisSubscription> rpSubscriptions = applicationService
                .getList(CrisSubscription.class);
        EPerson currentEPerson = null;
        List<ACrisObject> crisObjects = new ArrayList<ACrisObject>();
        for (CrisSubscription rpSubscription : rpSubscriptions)
        {
        	ACrisObject entityByUUID = applicationService
			        .getEntityByUUID(rpSubscription.getUuid());
        	
        	if (entityByUUID == null) {
        		// found a subscription for a removed object
				log.warn("Deleting subscription " + rpSubscription.getId() + " related to the deleted object "
						+ rpSubscription.getUuid());
				applicationService.delete(CrisSubscription.class, rpSubscription.getId());
				continue;
        	}
            // Does this row relate to the same e-person as the last?
            if ((currentEPerson == null)
                    || (rpSubscription.getEpersonID() != currentEPerson.getID()))
            {
                // New e-person. Send mail for previous e-person
                if (currentEPerson != null)
                {
                    try
                    {
                        sendEmail(context, mapRelationFields, currentEPerson, crisObjects,
	                                test);
                    }
                    catch (MessagingException me)
                    {
                        log.error("Failed to send subscription to eperson_id="
                                + currentEPerson.getID());
                        log.error(me);
                    }
                }

                currentEPerson = EPerson.find(context,
                        rpSubscription.getEpersonID());
                if (currentEPerson == null) {
            		// found a subscription from a removed user
					log.warn("Deleting subscription from a deleted user " + rpSubscription.getEpersonID()
							+ " about the object " + rpSubscription.getUuid());
    				applicationService.delete(CrisSubscription.class, rpSubscription.getId());
            	}
                crisObjects.clear();
            }

            Integer typeDef = rpSubscription.getTypeDef();
            if (typeDef == null) {
            	log.error("Found wrong subscription " + rpSubscription.getId() + " no target type defined -- skip it");
            	continue;
            }
            int typeToUse = typeDef;
            if (typeToUse > CrisConstants.CRIS_DYNAMIC_TYPE_ID_START) {
            	typeToUse = CrisConstants.CRIS_DYNAMIC_TYPE_ID_START;
            }
			Set<RelationConfiguration> relationsQueries = mapRelationFields.get(typeDef);
            if (relationsQueries == null || relationsQueries.size() == 0) {
        		log.error("No relations defined to satisfy the subscription request " + rpSubscription.getId());
        	}
            else {
            	crisObjects.add(entityByUUID);
            }
        }
        // Process the last person
        if (currentEPerson != null)
        {
            try
            {
                sendEmail(context, mapRelationFields, currentEPerson, crisObjects, test);
            }
            catch (MessagingException me)
            {
                log.error("Failed to send subscription to eperson_id="
                        + currentEPerson.getID());
                log.error(me);
            }
        }
    }

	/**
	 * Sends an email to the given e-person with details of new items in the given
	 * dspace object (MUST be a community or a collection), items that appeared
	 * yesterday. No e-mail is sent if there aren't any new items in any of the
	 * dspace objects.
	 * 
	 * @param context      DSpace context object
	 * @param mapRelations a map where the key is the integer representing the cris
	 *                     object type and the values are all the
	 *                     RelationConfiguration available for such type. For
	 *                     ResearcherObject all the relations are exposed under the
	 *                     start type ID
	 * @param eperson      eperson to send to
	 * @param crisObjects  List of CRIS Objects
	 * @param test         print the email content instead than send out a real
	 *                     email
	 * @throws SearchServiceException
	 */
    public static void sendEmail(Context context, Map<Integer, Set<RelationConfiguration>> mapRelations,
            EPerson eperson, List<ACrisObject> crisObjects, boolean test) throws IOException,
            MessagingException, SQLException, SearchServiceException
    {
        // Get a resource bundle according to the eperson language preferences
        Locale supportedLocale = I18nUtil.getEPersonLocale(eperson);

        SearchService searchService = new DSpace().getSingletonService(SearchService.class);
        StringBuffer emailText = new StringBuffer();
        boolean isFirst = true;

        for (ACrisObject rp : crisObjects)
        {
            SolrQuery query = new SolrQuery();
            query.setFields("search.resourceid");
            query.addFilterQuery("{!field f=search.resourcetype}"
                    + Constants.ITEM, "{!field f=read}g0","-withdrawn:true","-discoverable:false");

            StringBuffer q = new StringBuffer();
            int type = rp.getType();
            if (type > CrisConstants.CRIS_DYNAMIC_TYPE_ID_START) {
            	type = CrisConstants.CRIS_DYNAMIC_TYPE_ID_START;
            }
			Set<RelationConfiguration> relations = mapRelations.get(type);
			
			if (relations == null) {
				log.debug("No relations defined for type " + type);
				log.debug("the relation map contains the following types " + StringUtils.join(mapRelations.keySet(), ", " ));
			}
            for (RelationConfiguration rel : relations)
            {
            	if (q.length() > 0) {
            		q.append(" OR ");
            	}
                q.append("(").append(MessageFormat.format(rel.getQuery(), rp.getCrisID(), rp.getUuid())).append(")");
            }
            query.setQuery(q.toString());
            query.setRows(Integer.MAX_VALUE);

            if (ConfigurationManager.getBooleanProperty(
                    "eperson.subscription.onlynew", false))
            {
                // get only the items archived yesterday
				query.addFilterQuery("dateaccessioned_dt:[NOW/DAY-1DAY TO NOW/DAY]");
            }
            else
            {
                // get all item modified or archived yesterday 
				query.addFilterQuery("itemLastModified_dt:[NOW/DAY-1DAY TO NOW/DAY] OR dateaccessioned_dt:[NOW/DAY-1DAY TO NOW/DAY]");
            }

            QueryResponse qResponse = searchService.search(query);
            SolrDocumentList results = qResponse.getResults();

            // Only add to buffer if there are new items
            if (results.getNumFound() > 0)

            {
                if (!isFirst)
                {
                    emailText
                            .append("\n---------------------------------------\n");
                }
                else
                {
                    isFirst = false;
                }

                emailText
                        .append(I18nUtil.getMessage(
                                "org.dspace.eperson.Subscribe.new-items",
                                supportedLocale)).append(" ").append(rp.getName())
                        .append(": ").append(results.getNumFound())
                        .append("\n\n");

                for (SolrDocument solrDoc : results)

                {
                    Item item = Item.find(context, (Integer) solrDoc
                            .getFieldValue("search.resourceid"));

                    Metadatum[] titles = item.getDC("title", null, Item.ANY);
                    emailText
                            .append("      ")
                            .append(I18nUtil.getMessage(
                                    "org.dspace.eperson.Subscribe.title",
                                    supportedLocale)).append(" ");

                    if (titles.length > 0)
                    {
                        emailText.append(titles[0].value);
                    }
                    else
                    {
                        emailText.append(I18nUtil.getMessage(
                                "org.dspace.eperson.Subscribe.untitled",
                                supportedLocale));
                    }

					// TODO: these are more properly all the contributors of the items as for some
					// item type the authors are not so relevant/sufficient 
                    Metadatum[] authors = item.getDC("contributor", Item.ANY,
                            Item.ANY);

                    if (authors.length > 0)
                    {
                        emailText
                                .append("\n    ")
                                .append(I18nUtil.getMessage(
                                        "org.dspace.eperson.Subscribe.authors",
                                        supportedLocale)).append(" ")
                                .append(authors[0].value);

                        for (int k = 1; k < authors.length; k++)
                        {
                            emailText.append("\n             ").append(
                                    authors[k].value);
                        }
                    }

                    emailText
                            .append("\n         ")
                            .append(I18nUtil.getMessage(
                                    "org.dspace.eperson.Subscribe.id",
                                    supportedLocale))
                            .append(" ")
                            .append(HandleManager.getCanonicalForm(item
                                    .getHandle())).append("\n\n");
                    context.removeCached(item, item.getID());
                }
            }
        }

        // Send an e-mail if there were any new items
        if (emailText.length() > 0)
        {

            if (test)
            {
                log.info(LogManager.getHeader(context, "subscription:",
                        "eperson=" + eperson.getEmail()));
                log.info(LogManager.getHeader(context, "subscription:", "text="
                        + emailText.toString()));

            }
            else
            {

                Email email = Email.getEmail(I18nUtil
                        .getEmailFilename(supportedLocale, "subscription"));
                email.addRecipient(eperson.getEmail());
                email.addArgument(emailText.toString());
                email.send();

                log.info(LogManager.getHeader(context, "sent_subscription",
                        "eperson_id=" + eperson.getID()));

            }
        }
    }

    /**
     * Method for invoking subscriptions via the command line
     * 
     * @param argv
     *            command-line arguments, none used yet
     */
    public static void main(String[] argv)
    {
        log.info("#### START PROCESS DAILY: -----" + new Date() + " ----- ####");
        String usage = "org.dspace.app.cris.batch.ScriptCrisSubscribe [-t|-s] or nothing to send out subscriptions.";

        Options options = new Options();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine line = null;

        {
            Option opt = new Option("t", "test", false, "Run test session");
            opt.setRequired(false);
            options.addOption(opt);
        }

        {
            Option opt = new Option("h", "help", false,
                    "Print this help message");
            opt.setRequired(false);
            options.addOption(opt);
        }
        
        {
        	Option opt = new Option("s", "subscribe", false,
        			"First to process Daily Notification, try to subscribe all owner of the CRIS entity to the daily content");
            opt.setRequired(false);
            options.addOption(opt);        	
        }

        try
        {
            line = new PosixParser().parse(options, argv);
        }
        catch (Exception e)
        {
            // automatically generate the help statement
            formatter.printHelp(usage, e.getMessage(), options, "");
            System.exit(1);
        }

        if (line.hasOption("h"))
        {
            // automatically generate the help statement
            formatter.printHelp(usage, options);
            System.exit(1);
        }

        boolean test = line.hasOption("t");

        if (test) {
            log.setLevel(Level.DEBUG);
        }
        
        Context context = null;
        try
        {
            context = new Context();
            Researcher researcher = new Researcher();
            ApplicationService applicationService = researcher
                    .getApplicationService();
            CrisSearchService searchService = researcher.getCrisSearchService();
            
            if(line.hasOption("s")) {
                SolrQuery query = new SolrQuery();
                query.setFields("cris-uuid", OwnerRPAuthorityIndexer.OWNER_I);
                query.addFilterQuery("{!field f=search.resourcetype}"
                        + CrisConstants.RP_TYPE_ID);
                query.setRows(Integer.MAX_VALUE);
   				query.setQuery(OwnerRPAuthorityIndexer.OWNER_I + ":[* TO *]");

                QueryResponse qResponse = searchService.search(query);
                SolrDocumentList results = qResponse.getResults();
                CrisSubscribeService crisSubscribeService = researcher.getCrisSubscribeService();
                
                if (results.getNumFound() > 0)
                {
                    for (SolrDocument solrDoc : results)
                    {
                    	String uuid = (String) solrDoc.getFieldValue("cris-uuid");
                    	Integer oo = (Integer) solrDoc.getFieldValue(OwnerRPAuthorityIndexer.OWNER_I);
                		crisSubscribeService.subscribe(oo, uuid, CrisConstants.RP_TYPE_ID);
                    }
                }
            	
            }

            Map<Integer, Set<RelationConfiguration>> mapRelationFields = new HashMap<Integer, Set<RelationConfiguration>>();
            List<CrisComponentsService> serviceComponent = researcher
                    .getAllCrisComponents();
            for (CrisComponentsService service : serviceComponent)
            {
                for (ICRISComponent component : service.getComponents()
                        .values())
                {
                    RelationConfiguration relationConfiguration = component
                            .getRelationConfiguration();
                    if (Item.class.isAssignableFrom(relationConfiguration.getRelationClass()))
                    {
                        Integer key = CrisConstants.getEntityType(component.getTarget());
                        if(!mapRelationFields.containsKey(key)) {
                            Set<RelationConfiguration> rels = new HashSet<RelationConfiguration>();
                            rels.add(relationConfiguration);
                            mapRelationFields.put(key, rels);
                        }
                        else {
                            mapRelationFields.get(key).add(relationConfiguration);
                        }
                    }
                }
            }
            processDaily(researcher, applicationService, context, mapRelationFields, test);
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        finally
        {
            if (context != null && context.isValid())
            {
                // Nothing is actually written
                context.abort();
            }
        }
        log.info("#### END: -----" + new Date() + " ----- ####");
        System.exit(0);
    }
}
