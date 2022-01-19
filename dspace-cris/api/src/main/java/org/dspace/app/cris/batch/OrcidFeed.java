/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * https://github.com/CILEA/dspace-cris/wiki/License
 */
package org.dspace.app.cris.batch;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpException;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.dspace.app.cris.batch.bte.ImpRecordItem;
import org.dspace.app.cris.batch.bte.ImpRecordMetadata;
import org.dspace.app.cris.batch.bte.ImpRecordOutputGenerator;
import org.dspace.app.cris.batch.dao.ImpRecordDAO;
import org.dspace.app.cris.batch.dao.ImpRecordDAOFactory;
import org.dspace.app.cris.batch.dto.DTOImpRecord;
import org.dspace.app.cris.integration.orcid.OrcidOnlineDataLoader;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.discovery.SearchService;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.factory.EPersonServiceFactory;
import org.dspace.eperson.service.EPersonService;
import org.dspace.submit.lookup.MultipleSubmissionLookupDataLoader;
import org.dspace.submit.lookup.NetworkSubmissionLookupDataLoader;
import org.dspace.submit.lookup.SubmissionItemDataLoader;
import org.dspace.submit.lookup.SubmissionLookupDataLoader;
import org.dspace.submit.lookup.SubmissionLookupOutputGenerator;
import org.dspace.submit.util.ItemSubmissionLookupDTO;
import org.dspace.utils.DSpace;
import org.hibernate.SQLQuery;
import org.hibernate.Session;

import com.ctc.wstx.util.StringUtil;

import gr.ekt.bte.core.Record;
import gr.ekt.bte.core.RecordSet;
import gr.ekt.bte.core.TransformationEngine;
import gr.ekt.bte.core.TransformationSpec;
import gr.ekt.bte.exceptions.BadTransformationSpec;
import gr.ekt.bte.exceptions.MalformedSourceException;

public class OrcidFeed {

    private static final String IMP_SOURCE_REF = "imp_sourceref";

    private static final String IMP_RECORD_ID = "imp_record_id";

    private static final String IMP_EPERSON_ID = "imp_eperson_id";

    private static final Logger log = Logger.getLogger(OrcidFeed.class);

    private static TransformationEngine feedTransformationEnginePhaseOne = new DSpace()
            .getServiceManager()
            .getServiceByName("orcidFeedTransformationEnginePhaseOne",
                    TransformationEngine.class);

    private static TransformationEngine feedTransformationEnginePhaseTwo = new DSpace()
            .getServiceManager()
            .getServiceByName("orcidFeedTransformationEnginePhaseTwo",
                    TransformationEngine.class);

    private static OrcidOnlineDataLoader orcidOnlineDataLoader = new DSpace()
            .getServiceManager().getServiceByName("orcidOnlineDataLoader",
                    OrcidOnlineDataLoader.class);

    private static EPersonService ePersonService = EPersonServiceFactory.getInstance()
			.getEPersonService();

    // default 1 day
    private static long retentionQueryTime = 24 * 60 * 60000;

    public static void main(String[] args) throws SQLException,
            BadTransformationSpec, MalformedSourceException, AuthorizeException
    {
        // the configuration will hold the value in seconds, -1 don't make sense for the orcid feeder
        retentionQueryTime = ConfigurationManager.getIntProperty("orcidfeed",
                "query-retention") * 1000;

        Context context = new Context();
        context.turnOffAuthorisationSystem();
        HelpFormatter formatter = new HelpFormatter();

        String usage = "org.dspace.app.cris.batch.OrcidFeed [-i orcid|-a] [-p submitter] -c collectionID";

        Options options = new Options();
        CommandLine line = null;

        options.addOption(Option.builder("i").argName("orcid").hasArg(true)
				.desc("Identifier of the Orcid registry")
				.build());

        options.addOption(Option.builder("a").argName("all").hasArg(false)
				.desc("Process all ORCID known in the system")
				.build());
        
        options.addOption(Option.builder("c").required(true)
				.argName("collectionID").hasArg(true)
				.desc("Collection for item submission")
				.build());
        
        options.addOption(Option.builder("l").required(true)
				.argName("limit").hasArg(true)
				.desc("max number of profiles to process (50 default)")
				.build());
        
        options.addOption(Option.builder("p").required(true).argName("Eperson")
				.hasArg(true).desc("Submitter of the records")
				.build());

        options.addOption(Option.builder("d").required(false).argName("default")
				.hasArg(true).desc("Use such submitter for orcid not associated with an eperson. If not specified such orcid will be not processed")
				.build());
        
        options.addOption(Option.builder("f").argName("forceCollectionID")
				.hasArg(false).desc("force use the collectionID")
				.build());

		options.addOption(Option.builder("o").argName("status").hasArg(true)
				.desc("Status of new item p = workspace, w = workflow step 1, y = workflow step 2, x = workflow step 3, z = inarchive")
				.build());
		try {
			line = new DefaultParser().parse(options, args);
        }
		catch (ParseException e) {
            formatter.printHelp(usage, e.getMessage(), options, "");
            System.exit(1);
        }

        if (line.hasOption('i') && line.hasOption('a')) {
            System.out.println("only one between -i and -a can be used");
            formatter.printHelp(usage, "", options, "");
            System.exit(1);
        }
        
        if (line.hasOption('p') && line.hasOption('d')) {
            System.out.println("only one between -p and -d can be used");
            formatter.printHelp(usage, "", options, "");
            System.exit(1);
        }

        boolean forcePerson = line.hasOption('p');

        String person;
        if (forcePerson) {
            person = line.getOptionValue('p');
        } else {
            person = line.getOptionValue('d');
        }
        int limit = 50;
        if (line.hasOption('l')) {
            limit = Integer.parseInt(line.getOptionValue('l'));
        }
        EPerson eperson = null;
        if (StringUtils.indexOf(person, '@') == -1) {
            eperson = ePersonService.find(context, UUID.fromString(person));
        } else {
            eperson = ePersonService.findByEmail(context, person);
        }

        if (eperson != null && forcePerson)
        {
            context.setCurrentUser(eperson);
        }

        UUID collection_id = UUID.fromString(line.getOptionValue("c"));
        boolean forceCollectionId = line.hasOption("f");
        String orcid = line.getOptionValue("i");

        // p = workspace, w = workflow step 1, y = workflow step 2, x =
        // workflow step 3, z = inarchive
        String status = "p";
        
        try
        {
            System.out.println("Starting query");
            if (line.hasOption("o"))
            {
                status = line.getOptionValue("o");
            }

            //getIdentifiers
            Set<String> DOIList = FeedUtils.getIdentifiersToSkip(context, "dc", "identifier", "doi");
            Set<String> PMIDList = FeedUtils.getIdentifiersToSkip(context, "dc", "identifier", "pmid");
            Set<String> EIDList = FeedUtils.getIdentifiersToSkip(context, "dc", "identifier", "scopus");
            Set<String> ISIIDList = FeedUtils.getIdentifiersToSkip(context, "dc", "identifier", "isi");

            if (StringUtils.isBlank(orcid)) {
                SolrQuery solrQuery = new SolrQuery();
                solrQuery.setQuery("crisrp.orcid:[* TO *]");
                if (eperson == null) {
                    solrQuery.addFilterQuery("owner_s:[* TO *]");
                }
                solrQuery.setFields("cris-id", "crisrp.orcid", "owner_s");
                solrQuery.setRows(Integer.MAX_VALUE);
                SearchService searchService = new DSpace().getServiceManager().getServiceByName(SearchService.class.getName(), SearchService.class);
                QueryResponse resp = searchService.search(solrQuery);
                long numFound = resp.getResults().getNumFound();
                System.out.println("Found " + numFound + " profiles with ORCID to process...");
                int numProcessed = 0;
                for (SolrDocument doc : resp.getResults()) {
                    String crisID = (String) doc.getFirstValue("cris-id");
                    orcid = (String) doc.getFirstValue("crisrp.orcid");
                    String epersonId = (String) doc.getFirstValue("owner_s");
                    EPerson submitter = eperson;
                    if (!forcePerson && epersonId != null) {
                        submitter = ePersonService.find(context, UUID.fromString(epersonId));
                    }
                    if (submitter != null) {
                        System.out.println("processing " + crisID + " / " + orcid + " / " + submitter.getEmail());
                        boolean processed = retrievePublication(context, eperson, collection_id,
                                forceCollectionId, orcid, status, DOIList, PMIDList, EIDList, ISIIDList);
                        context.commit();
                        if (processed) {
                            numProcessed++;
                        }
                    }
                    else {
                        System.out.println(crisID + " / " + orcid + " cannot be processed as no submitter is defined");
                    }
                    if (numProcessed >= limit) {
                        System.out.println("Reach the limit of processable ORCID in one go");
                        context.complete();
                        return;
                    }
                }
            }
            else {
                retrievePublication(context, eperson, collection_id,
                        forceCollectionId, orcid, status, DOIList, PMIDList, EIDList, ISIIDList);
            }

            context.complete();

        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        finally
        {
            if (context != null && context.isValid())
            {
                context.abort();
            }
        }

    }

    public static boolean retrievePublication(Context context, EPerson eperson,
            UUID collection_id, boolean forceCollectionId, String orcid, String status,
            Set<String> DOIList, Set<String> PMIDList, Set<String> EIDList, Set<String> ISIIDList) throws HttpException, IOException,
            BadTransformationSpec, MalformedSourceException, SQLException
    {
        int total = 0;
        int errored = 0;
        String checksum;
		try {
			checksum = FeedUtils.getChecksum(
			        OrcidFeed.class.getCanonicalName(), orcid, null,
			        null, null);
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		if (!FeedUtils.shouldRunQuery(context, checksum, retentionQueryTime)) {
            System.out.println("QUERY: "+ orcid);
            System.out.println("--> SKIPPED");
            return false;
        }

        ImpRecordDAO dao = ImpRecordDAOFactory.getInstance(context);

        List<ImpRecordItem> pmeItemList = new ArrayList<ImpRecordItem>();
        pmeItemList.addAll(convertToImpRecordItem(context, orcid));

        for (ImpRecordItem pmeItem : pmeItemList)
        {
            boolean foundAlready =
                    FeedUtils.checkAlreadyImportedIdentifier("dc.identifier.doi", DOIList,
                            pmeItem) ||
                    FeedUtils.checkAlreadyImportedIdentifier("dc.identifier.pmid", PMIDList,
                            pmeItem) ||
                    FeedUtils.checkAlreadyImportedIdentifier("dc.identifier.scopus", EIDList,
                            pmeItem) ||
                    FeedUtils.checkAlreadyImportedIdentifier("dc.identifier.isi", ISIIDList,
                            pmeItem);

            if (!foundAlready)
            {
                try
                {
                    UUID tmpCollectionID = collection_id;
                    if (!forceCollectionId)
                    {
                        Set<ImpRecordMetadata> t = pmeItem.getMetadata()
                                .get("dc.source.type");
                        if (t != null && !t.isEmpty())
                        {
                            String stringTmpCollectionID = "";
                            Iterator<ImpRecordMetadata> iterator = t
                                    .iterator();
                            while (iterator.hasNext())
                            {
                                String stringTrimTmpCollectionID = iterator
                                        .next().getValue();
                                stringTmpCollectionID += stringTrimTmpCollectionID
                                        .trim();
                            }
                            tmpCollectionID = UUID.fromString(ConfigurationManager
                                    .getProperty("cris",
                                            "orcid.type."
                                                    + stringTmpCollectionID
                                                    + ".collectionid"));
                            if (tmpCollectionID==null) {
                            	tmpCollectionID = collection_id;
                            }
                        }
                    }

                    total++;
                    String action = "insert";
                    DTOImpRecord impRecord = FeedUtils.writeImpRecord("orcidfeed", context, dao,
	                        tmpCollectionID, pmeItem, action, eperson.getID(), status);

                    dao.write(impRecord, false);
                    FeedUtils.addProcessedIdentifiers(pmeItem, DOIList, PMIDList, EIDList, ISIIDList);
                }
                catch (Exception ex)
                {
                    errored++;
                }
            }
            if (total % 100 == 0) {
                context.commit();
            }
        }

        System.out.println("Imported " + (total - errored) + " record; "
                + errored + " discarded see log for details");

        FeedUtils.writeExecutedQuery(context, orcid, checksum, total - errored,
                errored, OrcidFeed.class.getCanonicalName());
        context.commit();
        return true;
    }

    private static List<ImpRecordItem> convertToImpRecordItem(Context context,
            String orcid) throws HttpException, IOException,
            BadTransformationSpec, MalformedSourceException
    {

        Map<String, Set<String>> keys = new HashMap<String, Set<String>>();
        Set<String> orcidToSearch = new HashSet<String>();
        orcidToSearch.add(orcid);
        keys.put(SubmissionLookupDataLoader.ORCID, orcidToSearch);

        List<ImpRecordItem> pmeResult = new ArrayList<ImpRecordItem>();
        List<Record> resultDataloader = orcidOnlineDataLoader
                .getByIdentifier(context, keys);
        List<ItemSubmissionLookupDTO> results = new ArrayList<ItemSubmissionLookupDTO>();
        if (resultDataloader != null && !resultDataloader.isEmpty())
        {

            TransformationEngine transformationEngine1 = getFeedTransformationEnginePhaseOne();
            if (transformationEngine1 != null)
            {
                HashMap<String, Set<String>> map = new HashMap<String, Set<String>>();
                HashSet<String> set = new HashSet<String>();
                // don't pass the orcid as we don't want the other providers to add more records but only extend the ones already retrieved
//                set.add(orcid);
//                map.put(SubmissionLookupDataLoader.ORCID, set);

                MultipleSubmissionLookupDataLoader mdataLoader = (MultipleSubmissionLookupDataLoader) transformationEngine1
                        .getDataLoader();
                mdataLoader.setIdentifiers(map);
                RecordSet cachedRecordSet = new RecordSet();
                cachedRecordSet.setRecords(resultDataloader);
                ((NetworkSubmissionLookupDataLoader) mdataLoader.getProvidersMap().get(SubmissionLookupDataLoader.ORCID)).setRecordSetCache(cachedRecordSet);
                SubmissionLookupOutputGenerator outputGenerator = (SubmissionLookupOutputGenerator) transformationEngine1
                        .getOutputGenerator();
                outputGenerator
                        .setDtoList(new ArrayList<ItemSubmissionLookupDTO>());

                transformationEngine1.transform(new TransformationSpec());
                log.debug("BTE transformation finished!");
                results.addAll(outputGenerator.getDtoList());
            }

            TransformationEngine transformationEngine2 = getFeedTransformationEnginePhaseTwo();
            if (transformationEngine2 != null && results != null)
            {
                SubmissionItemDataLoader dataLoader = (SubmissionItemDataLoader) transformationEngine2
                        .getDataLoader();
                dataLoader.setDtoList(results);

                ImpRecordOutputGenerator outputGenerator = (ImpRecordOutputGenerator) transformationEngine2
                        .getOutputGenerator();
                transformationEngine2.transform(new TransformationSpec());
                pmeResult = outputGenerator.getRecordIdItems();
            }
        }
        return pmeResult;
    }

    public static OrcidOnlineDataLoader getOrcidOnlineDataLoader()
    {
        return orcidOnlineDataLoader;
    }

    public static void setOrcidOnlineDataLoader(
            OrcidOnlineDataLoader orcidOnlineDataLoader)
    {
        OrcidFeed.orcidOnlineDataLoader = orcidOnlineDataLoader;
    }

    public static TransformationEngine getFeedTransformationEnginePhaseOne()
    {
        return feedTransformationEnginePhaseOne;
    }

    public static void setFeedTransformationEnginePhaseOne(
            TransformationEngine feedTransformationEnginePhaseOne)
    {
        OrcidFeed.feedTransformationEnginePhaseOne = feedTransformationEnginePhaseOne;
    }

    public static TransformationEngine getFeedTransformationEnginePhaseTwo()
    {
        return feedTransformationEnginePhaseTwo;
    }

    public static void setFeedTransformationEnginePhaseTwo(
            TransformationEngine feedTransformationEnginePhaseTwo)
    {
        OrcidFeed.feedTransformationEnginePhaseTwo = feedTransformationEnginePhaseTwo;
    }

}
