/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * https://github.com/CILEA/dspace-cris/wiki/License
 */
package org.dspace.app.cris.batch;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.app.cris.batch.bte.ImpRecordItem;
import org.dspace.app.cris.batch.bte.ImpRecordMetadata;
import org.dspace.app.cris.batch.bte.ImpRecordOutputGenerator;
import org.dspace.app.cris.batch.dao.ImpRecordDAO;
import org.dspace.app.cris.batch.dao.ImpRecordDAOFactory;
import org.dspace.app.cris.batch.dto.DTOImpRecord;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.factory.EPersonServiceFactory;
import org.dspace.eperson.service.EPersonService;
import org.dspace.submit.lookup.MultipleSubmissionLookupDataLoader;
import org.dspace.submit.lookup.NetworkSubmissionLookupDataLoader;
import org.dspace.submit.lookup.PubmedOnlineDataLoader;
import org.dspace.submit.lookup.SubmissionItemDataLoader;
import org.dspace.submit.lookup.SubmissionLookupDataLoader;
import org.dspace.submit.lookup.SubmissionLookupOutputGenerator;
import org.dspace.submit.util.ItemSubmissionLookupDTO;
import org.dspace.utils.DSpace;

import gr.ekt.bte.core.Record;
import gr.ekt.bte.core.RecordSet;
import gr.ekt.bte.core.TransformationEngine;
import gr.ekt.bte.core.TransformationSpec;
import gr.ekt.bte.exceptions.BadTransformationSpec;
import gr.ekt.bte.exceptions.MalformedSourceException;

public class PubmedFeed
{
    private static final String IMP_SOURCE_REF = "imp_sourceref";

    private static final String IMP_SOURCE_REF_PUBMED= "pubmed";

    private static final Logger log = Logger.getLogger(PubmedFeed.class);

    private static DateFormat df = new SimpleDateFormat("yyyy/MM/dd");

    // p = workspace, w = workflow step 1, y = workflow step 2, x =
    // workflow step 3, z = inarchive
    private static String status = "y";

    private static TransformationEngine feedTransformationEnginePhaseOne = new DSpace()
            .getServiceManager()
            .getServiceByName("pubmedFeedTransformationEnginePhaseOne",
                    TransformationEngine.class);

    private static TransformationEngine feedTransformationEnginePhaseTwo = new DSpace()
            .getServiceManager()
            .getServiceByName("pubmedFeedTransformationEnginePhaseTwo",
                    TransformationEngine.class);

    private static PubmedOnlineDataLoader pubmedOnlineDataLoader = new DSpace()
            .getServiceManager().getServiceByName("pubmedOnlineDataLoader",
                   PubmedOnlineDataLoader.class);

    private static EPersonService ePersonService = EPersonServiceFactory.getInstance().getEPersonService();

    private static long retentionQueryTime = Long.MAX_VALUE;

    public static void main(String[] args) throws SQLException,
            BadTransformationSpec, MalformedSourceException,
            java.text.ParseException, HttpException, IOException, org.apache.http.HttpException, AuthorizeException, NoSuchAlgorithmException
    {
        // the configuration will hold the value in seconds, -1 mean forever
        retentionQueryTime = ConfigurationManager.getIntProperty("pubmedfeed",
                "query-retention");
        if (retentionQueryTime == -1)
        {
            retentionQueryTime = Long.MAX_VALUE;
        }
        else
        {
            // convert second in ms
            retentionQueryTime = retentionQueryTime * 1000;
        }

        Context context = new Context();

        String usage = "org.dspace.app.cris.batch.PubmedFeed -q query -p submitter -s start_date(yyyy/mm/dd) -e end_date(yyyy/mm/dd) -c collectionID";

        HelpFormatter formatter = new HelpFormatter();

        Options options = new Options();
        CommandLine line = null;

        options.addOption(OptionBuilder.withArgName("UserQuery").hasArg(true)
                .withDescription(
                        "UserQuery, default query setup in the pubmedfeed.cfg")
                .create("q"));

        options.addOption(OptionBuilder.withArgName("Query Generator").hasArg(true)
                .withDescription(
                        "Generate query using the plugin, default query setup in the pubmedfeed.cfg")
                .create("g"));

        options.addOption(
                OptionBuilder.withArgName("query Start Date").hasArg(true)
                        .withDescription(
                                "Query start date to retrieve data publications from PubMed, default start date is yesterday")
                .create("s"));

        options.addOption(
                OptionBuilder.withArgName("query End Date").hasArg(true)
                        .withDescription(
                                "Query End date to retrieve data publications from PubMed, default is today")
                .create("e"));

        options.addOption(OptionBuilder.withArgName("forceCollectionID")
                .hasArg(false).withDescription("force use the collectionID")
                .create("f"));

        options.addOption(OptionBuilder.isRequired(true)
                .withArgName("collectionID").hasArg(true)
                .withDescription("Collection for item submission").create("c"));

        options.addOption(OptionBuilder.isRequired(true).withArgName("Eperson")
                .hasArg(true).withDescription("Submitter of the records")
                .create("p"));

        options.addOption(OptionBuilder.withArgName("status").hasArg(true)
                .withDescription(
                        "Status of new item p = workspace, w = workflow step 1, y = workflow step 2, x = workflow step 3, z = inarchive")
                .create("o"));

        options.addOption(OptionBuilder.withArgName("excludeTypes")
                .hasArg(false).withDescription("Do not import publication with type that is not mapped in pubmedfeed.cfg")
                .create("d"));

        //getIdentifiers
        Set<String> DOIList = FeedUtils.getIdentifiersToSkip(context, "dc", "identifier", "doi");
        Set<String> PMIDList = FeedUtils.getIdentifiersToSkip(context, "dc", "identifier", "pmid");
        Set<String> EIDList = FeedUtils.getIdentifiersToSkip(context, "dc", "identifier", "scopus");
        Set<String> ISIIDList = FeedUtils.getIdentifiersToSkip(context, "dc", "identifier", "isi");

        try
        {
            line = new PosixParser().parse(options, args);
        }
        catch (ParseException e)
        {
            formatter.printHelp(usage, e.getMessage(), options, "");
            System.exit(1);
        }

        if (!line.hasOption("c") || !line.hasOption("p"))
        {
            formatter.printHelp(usage, "", options, "");
            System.exit(1);
        }

        String person = line.getOptionValue("p");
        EPerson eperson = null;
        if (StringUtils.indexOf(person, '@') == -1) {
            eperson = ePersonService.find(context, UUID.fromString(person));
        } else {
            eperson = ePersonService.findByEmail(context, person);
        }

        if(eperson != null){
            context.setCurrentUser(eperson);
        }else{
            formatter.printHelp(usage, "No user found", options, "");
            System.exit(1);
        }

        UUID collection_id = UUID.fromString(line.getOptionValue("c"));
        boolean forceCollectionId = line.hasOption("f");

        boolean excludeImports = line.hasOption("d");

        String startDate = "";
        Date sDate = null;
        if (line.hasOption("s"))
        {
            sDate = df.parse(line.getOptionValue("s"));
        }
        else
        {
        	Object tri = FeedUtils.getHibernateSession(context)
    				.createSQLQuery(
    						"SELECT max(impr.last_modified) as LAST_MODIFIED from IMP_RECORD_TO_ITEM imprti join IMP_RECORD impr on "
    			                    + "imprti.imp_record_id = impr.imp_record_id and imprti."+IMP_SOURCE_REF+" like :"+IMP_SOURCE_REF)
    				.setParameter(IMP_SOURCE_REF, IMP_SOURCE_REF_PUBMED).uniqueResult();
            sDate = (Date)tri;
            if (sDate == null)
            {
                sDate = new Date();
            }
        }

        startDate = df.format(sDate);
        String endDate = "3000";
        if (line.hasOption("e"))
        {
            endDate = line.getOptionValue("e");
            Date date = df.parse(endDate);
            endDate = df.format(date);
        }

        String userQuery = ConfigurationManager.getProperty("pubmedfeed",
                "query.param.default");
        String queryGen = "";
        if (line.hasOption("q"))
        {
            userQuery = line.getOptionValue("q");
        }

        if(line.hasOption("g")) {
            queryGen = line.getOptionValue("g");
        }

        if (line.hasOption("o"))
        {
            status = line.getOptionValue("o");
        }

        int total = 0;
        int deleted = 0;

        HashMap<UUID,List<String>> submitterID2query = new HashMap<>();

        IFeedQueryGenerator queryGenerator = new DSpace().getServiceManager()
                .getServiceByName(queryGen, IFeedQueryGenerator.class);

        if(queryGenerator != null) {
	        submitterID2query = queryGenerator.generate(context);
        }else {
            List<String> queries = new ArrayList<String>();
            queries.add(userQuery);
            submitterID2query.put(eperson.getID(), queries);
        }

        ImpRecordDAO dao = ImpRecordDAOFactory.getInstance(context);

        Set<UUID> ids = submitterID2query.keySet();
        System.out.println("#Submitters: " + ids.size());
        int submitterCount = 0;
        for(UUID id : ids) {
            submitterCount++;
            if (id != null)
            {
                List<String> queryList = submitterID2query.get(id);
                System.out.println("Submitter # " + submitterCount + " queries " + queryList.size());
                for (String q : queryList)
                {
                    String checksum = FeedUtils.getChecksum(
                            PubmedFeed.class.getCanonicalName(), q, startDate, endDate,
                            null);
                    if (!FeedUtils.shouldRunQuery(context, checksum, retentionQueryTime)) {
                        System.out.println("QUERY: "+ q);
                        System.out.println("--> SKIPPED");
                        continue;
                    }
                    List<ImpRecordItem> impItems = convertToImpRecordItem(q,
                            startDate, endDate, PMIDList);
                    System.out.println("Records returned by the query: "+ impItems.size());
                    impRecordLoop:
                    for (ImpRecordItem pubmedItem : impItems)
                    {
                        boolean alreadyImported =
                                FeedUtils.checkAlreadyImportedIdentifier("dc.identifier.doi", DOIList,
                                        pubmedItem) ||
                                FeedUtils.checkAlreadyImportedIdentifier("dc.identifier.pmid", PMIDList,
                                        pubmedItem) ||
                                FeedUtils.checkAlreadyImportedIdentifier("dc.identifier.scopus", EIDList,
                                        pubmedItem) ||
                                FeedUtils.checkAlreadyImportedIdentifier("dc.identifier.isi", ISIIDList,
                                        pubmedItem);

                        if (alreadyImported) {
                            System.out.println("Skip already processed record after providers merge");
                            continue impRecordLoop;
                        }

                        boolean authorityFound = FeedUtils.isAtLeastOneAuthorFound(pubmedItem);
                        if(!authorityFound) {
                            System.out.println("Skip record " + pubmedItem.getSourceId() + " no matching authors found");
                            FeedUtils.writeDiscardedIdentifiers(context, pubmedItem, PubmedFeed.class.getCanonicalName());
                            FeedUtils.addProcessedIdentifiers(pubmedItem, DOIList, PMIDList, EIDList, ISIIDList);
                            context.commit();
                            continue impRecordLoop;
                        }

                        boolean affiliationFound = FeedUtils.isAtLeastOneAffiliationFound(pubmedItem);
                        if(!affiliationFound) {
                            System.out.println("Skip record " + pubmedItem.getSourceId() + " no matching affiliation found");
                            FeedUtils.writeDiscardedIdentifiers(context, pubmedItem, PubmedFeed.class.getCanonicalName());
                            FeedUtils.addProcessedIdentifiers(pubmedItem, DOIList, PMIDList, EIDList, ISIIDList);
                            context.commit();
                            continue impRecordLoop;
                        }

                        try
                        {
                        	UUID tmpCollectionID = collection_id;
                            if (!forceCollectionId)
                            {
                                Set<ImpRecordMetadata> t = pubmedItem.getMetadata().get("dc.source.type");
                                if (t != null && !t.isEmpty())
                                {
                                    String stringTmpCollectionID = "";
                                    Iterator<ImpRecordMetadata> iterator = t.iterator();
                                    while (iterator.hasNext())
                                    {
                                        String stringTrimTmpCollectionID = iterator.next().getValue();
                                        stringTmpCollectionID += stringTrimTmpCollectionID
                                                .trim();
                                    }
                                    tmpCollectionID = UUID.fromString(ConfigurationManager
                                            .getProperty("pubmedfeed",
                                                    "pubmed.type."
                                                            + stringTmpCollectionID
                                                            + ".collectionid"));
                                    if(tmpCollectionID==null) {
                                    	tmpCollectionID = collection_id;
                                    }
                                }
                            }

                            total++;
                            String action = "insert";
                            DTOImpRecord impRecord = FeedUtils.writeImpRecord("pubmedfeed", context,
                                    dao, tmpCollectionID, pubmedItem, action,
                                    id, status);

                            dao.write(impRecord, true);
                            FeedUtils.addProcessedIdentifiers(pubmedItem, DOIList, PMIDList, EIDList, ISIIDList);
                        }
                        catch (Exception ex)
                        {
                            deleted++;
                        }
                        if (total % 100 == 0)
                        {
                            context.commit();
                        }
                    }
                    context.commit();

                    System.out.println("Imported " + (total - deleted)
                            + " record; " + deleted + " marked as removed");

                    FeedUtils.writeExecutedQuery(context, q, checksum, total - deleted,
                            deleted, PubmedFeed.class.getCanonicalName());
                    context.commit();
                }
            }
        }
        context.complete();
    }

    private static List<ImpRecordItem> convertToImpRecordItem(String userQuery,
            String start, String end, Set<String> pubmedIDList)
                    throws BadTransformationSpec, MalformedSourceException,
                    HttpException, IOException, org.apache.http.HttpException
    {
        List<ImpRecordItem> impResult = new ArrayList<ImpRecordItem>();
        List<Record> pubmedResult = new ArrayList<Record>();
        String query = userQuery;
        if (StringUtils.isNotBlank(start) || StringUtils.isNotBlank(end))
        {
            query +=" AND (" + start +"[PDAT] : " + end +"[PDAT])";
        }
        pubmedResult =pubmedOnlineDataLoader.search(query);

        List<ItemSubmissionLookupDTO> results = new ArrayList<ItemSubmissionLookupDTO>();
        if (pubmedResult != null && !pubmedResult.isEmpty())
        {
            TransformationEngine transformationEngine1 = getFeedTransformationEnginePhaseOne();
            if (transformationEngine1 != null)
            {
                HashMap<String, Set<String>> map = new HashMap<String, Set<String>>(2);
                HashSet<String> set = new HashSet<String>(70); // so that will no grow up when reach 50 entries
                List<Record> cachedResults = new ArrayList<Record>(70);
                for (Record record : pubmedResult)
                {
                    if (record.getValues("pubmedID")== null || record.getValues("pubmedID").isEmpty())
                        continue;
                    String pubmedId = record.getValues("pubmedID").get(0).getAsString();
                    // as we extends the records with all the providers we can just check for the primary id
                    if (pubmedIDList.contains(pubmedId)) {
                        System.out.println("Skip already processed record: " + pubmedId);
                        continue;
                    }
                    else {
                        set.add(pubmedId);
                        cachedResults.add(record);
                    }

                    if (cachedResults.size() == 50) {
                        map.put(SubmissionLookupDataLoader.PUBMED, set);
                        MultipleSubmissionLookupDataLoader mdataLoader = (MultipleSubmissionLookupDataLoader) transformationEngine1
                                .getDataLoader();
                        mdataLoader.setIdentifiers(map);
                        RecordSet cachedRecordSet = new RecordSet();
                        cachedRecordSet.setRecords(cachedResults);
                        ((NetworkSubmissionLookupDataLoader) mdataLoader.getProvidersMap().get("pubmed")).setRecordSetCache(cachedRecordSet);
                        SubmissionLookupOutputGenerator outputGenerator = (SubmissionLookupOutputGenerator) transformationEngine1
                                .getOutputGenerator();
                        outputGenerator
                                .setDtoList(new ArrayList<ItemSubmissionLookupDTO>());

                        transformationEngine1.transform(new TransformationSpec());
                        results.addAll(outputGenerator.getDtoList());

                        map.clear();
                        set.clear();
                        cachedResults.clear();
                    }
                }

                // process the latest records over 50s
                if (cachedResults.size() > 0) {
                    map.put(SubmissionLookupDataLoader.PUBMED, set);
                    MultipleSubmissionLookupDataLoader mdataLoader = (MultipleSubmissionLookupDataLoader) transformationEngine1
                            .getDataLoader();
                    mdataLoader.setIdentifiers(map);
                    RecordSet cachedRecordSet = new RecordSet();
                    cachedRecordSet.setRecords(cachedResults);
                    ((NetworkSubmissionLookupDataLoader) mdataLoader.getProvidersMap().get("pubmed")).setRecordSetCache(cachedRecordSet);
                    SubmissionLookupOutputGenerator outputGenerator = (SubmissionLookupOutputGenerator) transformationEngine1
                            .getOutputGenerator();
                    outputGenerator
                            .setDtoList(new ArrayList<ItemSubmissionLookupDTO>());

                    transformationEngine1.transform(new TransformationSpec());
                    results.addAll(outputGenerator.getDtoList());

                    map.clear();
                    set.clear();
                    cachedResults.clear();
                }
            }

            TransformationEngine transformationEngine2 = getFeedTransformationEnginePhaseTwo();
            if (transformationEngine2 != null && results!=null && results.size() > 0)
            {
                SubmissionItemDataLoader dataLoader = (SubmissionItemDataLoader) transformationEngine2
                        .getDataLoader();
                dataLoader.setDtoList(results);

                ImpRecordOutputGenerator outputGenerator = (ImpRecordOutputGenerator) transformationEngine2
                        .getOutputGenerator();
                transformationEngine2.transform(new TransformationSpec());
                impResult = outputGenerator.getRecordIdItems();
            }
        }
        return impResult;
    }

    public static TransformationEngine getFeedTransformationEnginePhaseOne()
    {
        return feedTransformationEnginePhaseOne;
    }

    public static TransformationEngine getFeedTransformationEnginePhaseTwo()
    {
        return feedTransformationEnginePhaseTwo;
    }

}
