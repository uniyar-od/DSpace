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
import java.util.Calendar;
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
import org.dspace.submit.lookup.SubmissionItemDataLoader;
import org.dspace.submit.lookup.SubmissionLookupDataLoader;
import org.dspace.submit.lookup.SubmissionLookupOutputGenerator;
import org.dspace.submit.lookup.WOSOnlineDataLoader;
import org.dspace.submit.util.ItemSubmissionLookupDTO;
import org.dspace.utils.DSpace;

import gr.ekt.bte.core.Record;
import gr.ekt.bte.core.RecordSet;
import gr.ekt.bte.core.TransformationEngine;
import gr.ekt.bte.core.TransformationSpec;
import gr.ekt.bte.exceptions.BadTransformationSpec;
import gr.ekt.bte.exceptions.MalformedSourceException;

public class WosFeed
{

    private static final Logger log = Logger.getLogger(WosFeed.class);

    private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    // p = workspace, w = workflow step 1, y = workflow step 2, x =
    // workflow step 3, z = inarchive
    private static String status = "y";

    private static TransformationEngine feedTransformationEnginePhaseOne = new DSpace()
            .getServiceManager()
            .getServiceByName("wosFeedTransformationEnginePhaseOne",
                    TransformationEngine.class);
    
    private static TransformationEngine feedTransformationEnginePhaseTwo = new DSpace()
            .getServiceManager()
            .getServiceByName("wosFeedTransformationEnginePhaseTwo",
                    TransformationEngine.class);

    private static WOSOnlineDataLoader wosOnlineDataLoader = new DSpace()
            .getServiceManager()
            .getServiceByName("wosOnlineDataLoader", WOSOnlineDataLoader.class);

    private static EPersonService ePersonService = EPersonServiceFactory.getInstance().getEPersonService();

    private static long retentionQueryTime = Long.MAX_VALUE;

    public static void main(String[] args)
            throws SQLException, BadTransformationSpec,
            MalformedSourceException, HttpException, IOException, AuthorizeException, NoSuchAlgorithmException
    {
        // the configuration will hold the value in seconds, -1 mean forever
        retentionQueryTime = ConfigurationManager.getIntProperty("wosfeed",
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

        String usage = "org.dspace.app.cris.batch.WosFeed -q queryPMC -p submitter -s start_date(YYYY-MM-DD) -e end_date(YYYY-MM-DD) -c collectionID";

        HelpFormatter formatter = new HelpFormatter();

        Options options = new Options();
        CommandLine line = null;

        options.addOption(OptionBuilder.withArgName("UserQuery").hasArg(true)
                .withDescription(
                        "UserQuery, default query setup in the wosfeed.cfg")
                .create("q"));

        options.addOption(OptionBuilder.withArgName("Query Generator").hasArg(true)
                .withDescription(
                        "Generate query using the plugin, default query setup in the pubmedfeed.cfg")
                .create("g"));

        options.addOption(
                OptionBuilder.withArgName("query SymbolicTimeSpan").hasArg(true)
                        .withDescription(
                                "This element defines a range of load dates. The load date is the date when a record was added to a database. If symbolicTimeSpan is specified,"
                                + " the timeSpan parameter must be omitted. If timeSpan and symbolicTimeSpan are both omitted, then the maximum publication date time span will"
                                + " be inferred from the editions data. Values 1week, 2week or 3week")
                .create("t"));
        
        options.addOption(
                OptionBuilder.withArgName("beginTimeSpan").hasArg(true)
                        .withDescription(
                                "Query start date to retrieve data publications from Wos, default start date is yesterday (begin timeSpan)")
                .create("s"));

        options.addOption(
                OptionBuilder.withArgName("endTimeSpan").hasArg(true)
                        .withDescription(
                                "Query End date to retrieve data publications from Wos, default is today (end timeSpan)")
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
                .hasArg(false).withDescription("Do not import publication with type that is not mapped in wos.cfg")
                .create("d"));

        options.addOption(OptionBuilder.withArgName("skip")
                .hasArg(false).withDescription("Do not import publication already worked, matching is based on DOI,pmid,eid,isi")
                .create("d"));

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

        String symbolicTimeSpan = null;
        String startDate = null;
        String endDate = null;
        
        if (line.hasOption("t"))
        {
            symbolicTimeSpan = line.getOptionValue("t");
        }
        
        if (!line.hasOption("s") && !line.hasOption("e"))
        {
            symbolicTimeSpan = "1week";
        }
        else {
            if (line.hasOption("s"))
            {
                startDate = line.getOptionValue("s");
            }
            else
            {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DATE, -1);
                startDate = df.format(cal.getTime());
            }
            
            if (line.hasOption("e"))
            {
                endDate = line.getOptionValue("e");
            }
            else
            {
                Calendar cal = Calendar.getInstance();
                endDate = df.format(cal.getTime());
            }
        }
        
        String userQuery = ConfigurationManager.getProperty("wosfeed",
                "query.param.default");
        if (line.hasOption("q"))
        {
            userQuery = line.getOptionValue("q");
        }

        if (line.hasOption("o"))
        {
            status = line.getOptionValue("o");
        }

        String queryGen = "";
        if(line.hasOption("g")) {
            queryGen = line.getOptionValue("g");
        }

        boolean excludeImports = line.hasOption("d");
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

        //getIdentifiers
        Set<String> DOIList = FeedUtils.getIdentifiersToSkip(context, "dc", "identifier", "doi");
        Set<String> PMIDList = FeedUtils.getIdentifiersToSkip(context, "dc", "identifier", "pmid");
        Set<String> EIDList = FeedUtils.getIdentifiersToSkip(context, "dc", "identifier", "scopus");
        Set<String> ISIIDList = FeedUtils.getIdentifiersToSkip(context, "dc", "identifier", "isi");

        ImpRecordDAO dao = ImpRecordDAOFactory.getInstance(context);

        Set<UUID> ids = submitterID2query.keySet();
        System.out.println("#Submitters: " + ids.size());
        int submitterCount = 0;
        for(UUID id : ids) {
            submitterCount++;
            List<String> queryList = submitterID2query.get(id);
            System.out.println("Submitter # " + submitterCount + " queries " + queryList.size());

            for(String q: queryList) {
                String checksum = FeedUtils.getChecksum(
                        WosFeed.class.getCanonicalName(), q, startDate, endDate,
                        symbolicTimeSpan);
                if (!FeedUtils.shouldRunQuery(context, checksum, retentionQueryTime)) {
                    System.out.println("QUERY: "+ q);
                    System.out.println("--> SKIPPED");
                    continue;
                }
                List<ImpRecordItem> impItems = convertToImpRecordItem(q,
                        "WOK", symbolicTimeSpan, startDate, endDate, ISIIDList);
                System.out.println("Records returned by the query: "+ impItems.size());
                impRecordLoop:
                for (ImpRecordItem wosItem : impItems)
                {
                    boolean alreadyImported =
                            FeedUtils.checkAlreadyImportedIdentifier("dc.identifier.doi", DOIList,
                                    wosItem) ||
                            FeedUtils.checkAlreadyImportedIdentifier("dc.identifier.pmid", PMIDList,
                                    wosItem) ||
                            FeedUtils.checkAlreadyImportedIdentifier("dc.identifier.scopus", EIDList,
                                    wosItem) ||
                            FeedUtils.checkAlreadyImportedIdentifier("dc.identifier.isi", ISIIDList,
                                    wosItem);

                    if (alreadyImported) {
                        System.out.println("Skip already processed record after providers merge");
                        continue impRecordLoop;
                    }
                    boolean authorityFound = FeedUtils.isAtLeastOneAuthorFound(wosItem);
                    if(!authorityFound) {
                        System.out.println("Skip record " + wosItem.getSourceId() + " no matching authors found");
                        FeedUtils.writeDiscardedIdentifiers(context, wosItem, WosFeed.class.getCanonicalName());
                        FeedUtils.addProcessedIdentifiers(wosItem, DOIList, PMIDList, EIDList, ISIIDList);
                        context.commit();
                        continue impRecordLoop;
                    }
                    boolean affiliationFound = FeedUtils.isAtLeastOneAffiliationFound(wosItem);
                    if(!affiliationFound) {
                        System.out.println("Skip record " + wosItem.getSourceId() + " no matching affiliation found");
                        FeedUtils.writeDiscardedIdentifiers(context, wosItem, WosFeed.class.getCanonicalName());
                        FeedUtils.addProcessedIdentifiers(wosItem, DOIList, PMIDList, EIDList, ISIIDList);
                        context.commit();
                        continue impRecordLoop;
                    }

                    try
                    {
                        UUID tmpCollectionID = collection_id;
                        if (!forceCollectionId)
                        {
                            Set<ImpRecordMetadata> t = wosItem.getMetadata().get("dc.source.type");
                            if (t != null && !t.isEmpty())
                            {
                                String stringTmpCollectionID = "";
                                Iterator<ImpRecordMetadata> iterator = t.iterator();
                                while (iterator.hasNext())
                                {
                                    String stringTrimTmpCollectionID = iterator.next().getValue();
                                    stringTmpCollectionID += stringTrimTmpCollectionID
                                            .trim();
                                    tmpCollectionID = UUID.fromString(ConfigurationManager
                                            .getProperty("wosfeed",
                                                    "wos.type." + stringTmpCollectionID
                                                        + ".collectionid"));
                                    if(tmpCollectionID==null) {
                                        tmpCollectionID = collection_id;
                                    }
                                }
                            }
                        }

                        total++;
                        String action = "insert";
                        DTOImpRecord impRecord = FeedUtils.writeImpRecord("wosfeed", context, dao,
                            tmpCollectionID, wosItem, action, eperson.getID(), status);

                        dao.write(impRecord, true);
                        FeedUtils.addProcessedIdentifiers(wosItem, DOIList, PMIDList, EIDList, ISIIDList);
                    }
                    catch (Exception ex)
                    {
                        deleted++;
                    }
                    if(total %100 ==0) {
                        context.commit();
                    }
                }

                System.out.println("Imported " + (total - deleted) + " record; "
                		+ deleted + " marked as removed");

                FeedUtils.writeExecutedQuery(context, q, checksum, total - deleted,
                        deleted, WosFeed.class.getCanonicalName());
                context.commit();
            }
        }
        context.complete();
    }

    private static List<ImpRecordItem> convertToImpRecordItem(String userQuery,
            String databaseID, String symbolicTimeSpan, String start, String end,
            Set<String> iSIIDList)
                    throws BadTransformationSpec, MalformedSourceException,
                    HttpException, IOException
    {
        List<ImpRecordItem> pmeResult = new ArrayList<ImpRecordItem>();
        List<Record> wosResult = wosOnlineDataLoader
                .searchByAffiliation(userQuery, databaseID, symbolicTimeSpan, start, end);
        List<ItemSubmissionLookupDTO> results = new ArrayList<ItemSubmissionLookupDTO>();
        if (wosResult != null && !wosResult.isEmpty())
        {
            TransformationEngine transformationEngine1 = getFeedTransformationEnginePhaseOne();
            if (transformationEngine1 != null)
            {
                HashMap<String, Set<String>> map = new HashMap<String, Set<String>>(2);
                HashSet<String> set = new HashSet<String>(70); // so that will no grow up when reach 50 entries
                List<Record> cachedResults = new ArrayList<Record>(70);
                for (Record record : wosResult)
                {
                    if (record.getValues("isiId") == null || record.getValues("isiId").isEmpty())
                        continue;
                    String isiId = record.getValues("isiId").get(0).getAsString();
                    // as we extends the records with all the providers we can just check for the primary id
                    if (iSIIDList.contains(isiId)) {
                        System.out.println("Skip already processed record: " + isiId);
                        continue;
                    }
                    else {
                        set.add(isiId);
                        cachedResults.add(record);
                    }

                    if (cachedResults.size() == 50) {
                        map.put(SubmissionLookupDataLoader.WOSID, set);
                        MultipleSubmissionLookupDataLoader mdataLoader = (MultipleSubmissionLookupDataLoader) transformationEngine1
                                .getDataLoader();
                        mdataLoader.setIdentifiers(map);
                        RecordSet cachedRecordSet = new RecordSet();
                        cachedRecordSet.setRecords(cachedResults);
                        ((NetworkSubmissionLookupDataLoader) mdataLoader.getProvidersMap().get("wos")).setRecordSetCache(cachedRecordSet);
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
                    map.put(SubmissionLookupDataLoader.WOSID, set);
                    MultipleSubmissionLookupDataLoader mdataLoader = (MultipleSubmissionLookupDataLoader) transformationEngine1
                            .getDataLoader();
                    mdataLoader.setIdentifiers(map);
                    RecordSet cachedRecordSet = new RecordSet();
                    cachedRecordSet.setRecords(cachedResults);
                    ((NetworkSubmissionLookupDataLoader) mdataLoader.getProvidersMap().get("wos")).setRecordSetCache(cachedRecordSet);
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
                pmeResult = outputGenerator.getRecordIdItems();
            }
        }
        return pmeResult;
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
