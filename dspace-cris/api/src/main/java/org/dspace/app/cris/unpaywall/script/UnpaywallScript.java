/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * https://github.com/CILEA/dspace-cris/wiki/License
 */
package org.dspace.app.cris.unpaywall.script;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.dspace.app.cris.metrics.common.model.ConstantMetrics;
import org.dspace.app.cris.metrics.common.model.CrisMetrics;
import org.dspace.app.cris.metrics.pmc.model.PMCCitation;
import org.dspace.app.cris.metrics.pmc.services.PMCEntrezException;
import org.dspace.app.cris.unpaywall.UnpaywallBestOA;
import org.dspace.app.cris.unpaywall.UnpaywallOA;
import org.dspace.app.cris.unpaywall.UnpaywallRecord;
import org.dspace.app.cris.unpaywall.UnpaywallService;
import org.dspace.app.cris.unpaywall.dao.UnpaywallDAO;
import org.dspace.app.cris.unpaywall.model.Unpaywall;
import org.dspace.app.cris.unpaywall.services.UnpaywallPersistenceService;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.dspace.discovery.DiscoverQuery;
import org.dspace.discovery.DiscoverResult;
import org.dspace.discovery.SearchService;
import org.dspace.discovery.SearchServiceException;
import org.dspace.kernel.ServiceManager;
import org.dspace.utils.DSpace;
import org.springframework.util.StringUtils;

public class UnpaywallScript {

    private static Long cacheTime = ConfigurationManager.getLongProperty("unpaywall", "cachetime");
    private static final String DOI = ConfigurationManager.getProperty("unpaywall", "metadata.doi");
    private static final String apiKey = ConfigurationManager.getProperty("unpaywall", "apikey");
    private static final String url = ConfigurationManager.getProperty("unpaywall", "url");

    /** log4j logger */
    private static Logger log = Logger.getLogger(UnpaywallScript.class);

    private static UnpaywallPersistenceService pService;

    private static UnpaywallDAO unpaywallDAO;

    private static UnpaywallService sService;

    private static Unpaywall unpaywall;

    private static SearchService searcher;

    private static long timeElapsed = 3600000 * 24 * 7; // 1 week

    private static int maxItemToWork = 100;

    private static String queryDefault = "";

    private static int MAX_QUERY_RESULTS = 50;

    public static void main(String[] args)
            throws SearchServiceException, SQLException, AuthorizeException, ParseException {

        CommandLineParser parser = new PosixParser();

        Options options = new Options();
        options.addOption("h", "help", false, "help");

        options.addOption("t", "time", true,
                "Limit to update only citation more old than <t> seconds. Use 0 to force update of all record");

        options.addOption("q", "query", true,
                "Override the default query to retrieve puntual publication (used for test scope, the default query will be deleted");

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
    }
    
    private static void updateUnpaywallCiting(Integer itemID, Integer doiid,
            Set<Integer> doiIDs) throws SearchServiceException
    {}
}