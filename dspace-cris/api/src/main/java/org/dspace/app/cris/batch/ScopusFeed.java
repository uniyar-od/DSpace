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
import org.dspace.submit.lookup.ScopusOnlineDataLoader;
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

public class ScopusFeed
{

    private static final Logger log = Logger.getLogger(ScopusFeed.class);

    // this is the format needed in the scopus query
    private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    
    // p = workspace, w = workflow step 1, y = workflow step 2, x =
    // workflow step 3, z = inarchive
    private static String status = "y";

    private static TransformationEngine feedTransformationEnginePhaseOne = new DSpace()
            .getServiceManager()
            .getServiceByName("scopusFeedTransformationEnginePhaseOne",
                    TransformationEngine.class);
    
    private static TransformationEngine feedTransformationEnginePhaseTwo = new DSpace()
            .getServiceManager()
            .getServiceByName("scopusFeedTransformationEnginePhaseTwo",
                    TransformationEngine.class);
    
    private static ScopusOnlineDataLoader scopusOnlineDataLoader = new DSpace()
            .getServiceManager().getServiceByName("scopusOnlineDataLoader",
                    ScopusOnlineDataLoader.class);

    private static EPersonService ePersonService = EPersonServiceFactory.getInstance().getEPersonService();

    private static long retentionQueryTime = Long.MAX_VALUE;

    public static void main(String[] args) throws SQLException,
            BadTransformationSpec, MalformedSourceException,
            java.text.ParseException, HttpException, IOException, org.apache.http.HttpException, AuthorizeException, NoSuchAlgorithmException
    {
        // the configuration will hold the value in seconds, -1 mean forever
        retentionQueryTime = ConfigurationManager.getIntProperty("scopusfeed",
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

        String usage = "org.dspace.app.cris.batch.ScopusFeed -q query -p submitter -s start_date(yyyy-mm-dd) -e end_date(yyyy-mm-dd) -c collectionID";

        HelpFormatter formatter = new HelpFormatter();

        Options options = new Options();
        CommandLine line = null;

        options.addOption(OptionBuilder.withArgName("UserQuery").hasArg(true)
                .withDescription(
                        "UserQuery, default query setup in the scopusfeed.cfg")
                .create("q"));

        options.addOption(OptionBuilder.withArgName("Query Generator").hasArg(true)
                .withDescription(
                        "Generate query using the plugin, default query setup in the pubmedfeed.cfg")
                .create("g"));

        options.addOption(
                OptionBuilder.withArgName("query Start Date").hasArg(true)
                        .withDescription(
                                "Query start date to retrieve data publications from Scopus, default start date is yesterday (format yyyy-mm-dd)")
                .create("s"));

        options.addOption(
                OptionBuilder.withArgName("query End Date").hasArg(true)
                        .withDescription(
                                "Query End date to retrieve data publications from Scopus, default is today")
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

        String startDate = "";
        if (line.hasOption("s"))
        {
            startDate = line.getOptionValue("s");
            Date date = df.parse(startDate);
            startDate = df.format(date);
        }
        else
        {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);
            startDate = df.format(new Date(cal.getTimeInMillis()));
        }

        startDate = startDate.substring(0, 10);
        String endDate = "";
        if (line.hasOption("e"))
        {
            endDate = line.getOptionValue("e");
            Date date = df.parse(endDate);
            endDate = df.format(date);
        }
        else
        {
            Calendar cal = Calendar.getInstance();
            endDate = df.format(new Date(cal.getTimeInMillis()));
        }
        endDate = endDate.substring(0, 10);
        String userQuery = ConfigurationManager.getProperty("scopusfeed",
                "query.param.default");
        if (line.hasOption("q"))
        {
            userQuery = line.getOptionValue("q");
        }

        String queryGen="";
        if(line.hasOption("g")) {
            queryGen = line.getOptionValue("g");
        }

        if (line.hasOption("o"))
        {
            status = line.getOptionValue("o");
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
                        ScopusFeed.class.getCanonicalName(), q, startDate,
                        endDate, null);
                if (!FeedUtils.shouldRunQuery(context, checksum, retentionQueryTime)) {
                    System.out.println("QUERY: "+ q);
                    System.out.println("--> SKIPPED");
                    continue;
                }
                List<ImpRecordItem> impItems = convertToImpRecordItem(q, startDate, endDate, EIDList);
                System.out.println("Records returned by the query: "+ impItems.size());
                impRecordLoop:
                for (ImpRecordItem pmeItem : impItems)
                {
                    boolean alreadyImported =
                            FeedUtils.checkAlreadyImportedIdentifier("dc.identifier.doi", DOIList,
                                    pmeItem) ||
                            FeedUtils.checkAlreadyImportedIdentifier("dc.identifier.pmid", PMIDList,
                                    pmeItem) ||
                            FeedUtils.checkAlreadyImportedIdentifier("dc.identifier.scopus", EIDList,
                                    pmeItem) ||
                            FeedUtils.checkAlreadyImportedIdentifier("dc.identifier.isi", ISIIDList,
                                    pmeItem);

                    if (alreadyImported) {
                        System.out.println("Skip already processed record after providers merge");
                        continue impRecordLoop;
                    }

                    boolean authorityFound = FeedUtils.isAtLeastOneAuthorFound(pmeItem);
                    if(!authorityFound) {
                        System.out.println("Skip record " + pmeItem.getSourceId() + " no matching authors found");
                        FeedUtils.writeDiscardedIdentifiers(context, pmeItem, ScopusFeed.class.getCanonicalName());
                        FeedUtils.addProcessedIdentifiers(pmeItem, DOIList, PMIDList, EIDList, ISIIDList);
                        context.commit();
                        continue impRecordLoop;
                    }

                    boolean affiliationFound = FeedUtils.isAtLeastOneAffiliationFound(pmeItem);
                    if(!affiliationFound) {
                        System.out.println("Skip record " + pmeItem.getSourceId() + " no matching affiliation found");
                        FeedUtils.writeDiscardedIdentifiers(context, pmeItem, ScopusFeed.class.getCanonicalName());
                        FeedUtils.addProcessedIdentifiers(pmeItem, DOIList, PMIDList, EIDList, ISIIDList);
                        context.commit();
                        continue impRecordLoop;
                    }

                    try
                    {
                    	UUID tmpCollectionID = collection_id;
                        if (!forceCollectionId)
                        {
                            Set<ImpRecordMetadata> t = pmeItem.getMetadata().get("dc.source.type");
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
                                        .getProperty("scopusfeed",
                                                "scopus.type." + stringTmpCollectionID
                                                        + ".collectionid"));
                                if(tmpCollectionID==null) {
                                	tmpCollectionID = collection_id;
                                }
                            }
                        }

                        total++;
                        String action = "insert";
                        DTOImpRecord impRecord = FeedUtils.writeImpRecord("scopusfeed", context, dao,
                                tmpCollectionID, pmeItem, action, eperson.getID(), status);

                        dao.write(impRecord, true);
                        FeedUtils.addProcessedIdentifiers(pmeItem, DOIList, PMIDList, EIDList, ISIIDList);
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
                        deleted, ScopusFeed.class.getCanonicalName());
                context.commit();
            }
        }
        context.complete();
    }

    private static List<ImpRecordItem> convertToImpRecordItem(String userQuery,
            String start, String end, Set<String> scopusIDList)
                    throws BadTransformationSpec, MalformedSourceException,
                    HttpException, IOException, org.apache.http.HttpException
    {
        List<ImpRecordItem> pmeResult = new ArrayList<ImpRecordItem>();
        List<Record> scopusResult = new ArrayList<Record>();
        String query = userQuery;
        if (StringUtils.isNotBlank(start))
        {
            query += " AND ORIG-LOAD-DATE AFT " + start;
        }
        if (StringUtils.isNotBlank(end))
        {
            query += " AND ORIG-LOAD-DATE BEF " + end;
        }
        scopusResult = scopusOnlineDataLoader.search(query);

        List<ItemSubmissionLookupDTO> results = new ArrayList<ItemSubmissionLookupDTO>();
        if (scopusResult != null && !scopusResult.isEmpty())
        {
            TransformationEngine transformationEngine1 = getFeedTransformationEnginePhaseOne();
            if (transformationEngine1 != null)
            {
                HashMap<String, Set<String>> map = new HashMap<String, Set<String>>(2);
                HashSet<String> set = new HashSet<String>(70); // so that will no grow up when reach 50 entries
                List<Record> cachedResults = new ArrayList<Record>(70);
                for (Record record : scopusResult)
                {
                    if (record.getValues("eid") == null || record.getValues("eid").isEmpty())
                        continue;
                    String scopusId = record.getValues("eid").get(0).getAsString();
                    // as we extends the records with all the providers we can just check for the primary id
                    if (scopusIDList.contains(scopusId)) {
                        System.out.println("Skip already processed record: " + scopusId);
                        continue;
                    }
                    else {
                        set.add(scopusId);
                        cachedResults.add(record);
                    }

                    if (cachedResults.size() == 50) {
                        map.put(SubmissionLookupDataLoader.SCOPUSEID, set);

                        MultipleSubmissionLookupDataLoader mdataLoader = (MultipleSubmissionLookupDataLoader) transformationEngine1
                                .getDataLoader();
                        mdataLoader.setIdentifiers(map);
                        RecordSet cachedRecordSet = new RecordSet();
                        cachedRecordSet.setRecords(cachedResults);
                        ((NetworkSubmissionLookupDataLoader) mdataLoader.getProvidersMap().get("scopus")).setRecordSetCache(cachedRecordSet);
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
                    map.put(SubmissionLookupDataLoader.SCOPUSEID, set);

                    MultipleSubmissionLookupDataLoader mdataLoader = (MultipleSubmissionLookupDataLoader) transformationEngine1
                            .getDataLoader();
                    mdataLoader.setIdentifiers(map);
                    RecordSet cachedRecordSet = new RecordSet();
                    cachedRecordSet.setRecords(cachedResults);
                    ((NetworkSubmissionLookupDataLoader) mdataLoader.getProvidersMap().get("scopus")).setRecordSetCache(cachedRecordSet);
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
