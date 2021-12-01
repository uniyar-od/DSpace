package org.dspace.xoai.app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;
import org.dspace.app.util.XMLUtils;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.storage.rdbms.DatabaseManager;
import org.dspace.storage.rdbms.TableRow;
import org.dspace.storage.rdbms.TableRowIterator;
import org.dspace.xoai.data.DSpaceItem;
import org.dspace.xoai.solr.exceptions.DSpaceSolrIndexerException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Find all bissing/error entries on the oai core, log them to a file and delete from index the ones returning error
 * you can optionally reindex the missing ones with -i
 *
 */
public class CheckXOAI {

	private static final String OAI_URL = ConfigurationManager.getProperty("oai", "dspace.oai.url");
	private static final String OAI_ENDPOINT_URL = (StringUtils.isNotBlank(OAI_URL) ? OAI_URL : ConfigurationManager.getProperty("dspace.baseUrl") + "/oai") + "/";
	
	public static void main(String[] args) throws ParseException, SQLException, HttpException, IOException, DSpaceSolrIndexerException
	{
		System.out.println("#### START CheckXOAI: -----" + new Date() + " ----- ####");
		AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(new Class[]{
                BasicConfiguration.class
        });

		Context ctx = null;
		
		try {
			
	        String usage = "org.dspace.xoai.app.CheckXOAI [-ivh[c <core>][p <prefix>][f <folder>][u <baseurl>]] or nothing to log missing/error entries and delete error ones.";
	        Options options = new Options();
	        HelpFormatter formatter = new HelpFormatter();
	        CommandLine line = null;

	        options
	                .addOption(OptionBuilder
	                        .withArgName("index")
	                        .isRequired(false)
	                        .withDescription(
	                                "Index missing records")
	                        .create("i"));
	        
	        options
	        .addOption(OptionBuilder
	        		.withArgName("context")
	                .isRequired(false)
	                .hasArg(true)
	                .withDescription(
	                        "Specify wich context to check (default \"request\")")
	                .create("c"));	        

	        options
	        .addOption(OptionBuilder
	        		.withArgName("prefix")
	                .isRequired(false)
	                .hasArg(true)
	                .withDescription(
	                        "Metadata prefix (default \"oai_dc\")")
	                .create("p"));

	        options
	        .addOption(OptionBuilder
	        		.withArgName("folder")
	                .isRequired(false)
	                .hasArg(true)
	                .withDescription(
	                "Output folder (default is dspace log folder)")
	                .create("f"));
	        
	        options
	        .addOption(OptionBuilder
	        		.withArgName("oai-url")
	                .isRequired(false)
	                .hasArg(true)
	                .withDescription("OAI base url")
	                .create("u"));

	        options.addOption(OptionBuilder.withArgName("help").isRequired(false).withDescription(
	                "print this help message").create("h"));

	        options.addOption(OptionBuilder.withArgName("verbose").isRequired(false).withDescription(
	                "Verbose output").create("v"));
	        
	        options.addOption("e", "readfile", true, "Read the identifier from a file to update in the index");
	        
	        try {
	            line = new PosixParser().parse(options, args);
	        } catch (Exception e) {
	            // automatically generate the help statement
	            formatter.printHelp(usage, e.getMessage(), options, "");
	            System.exit(1);
	        }

	        if (line.hasOption("h")) {
	            // automatically generate the help statement
	            formatter.printHelp(usage, options);
	            System.exit(1);
	        }
	        
			boolean indexMissing = line.hasOption('i');
			boolean verbose = line.hasOption('v');
			String fileBase = StringUtils.isNotBlank(line.getOptionValue('f')) ? line.getOptionValue('f') : ConfigurationManager.getProperty("dspace.dir") + "/log";
			String core = StringUtils.isNotBlank(line.getOptionValue('c')) ? line.getOptionValue('c') : "request";
			String prefix = StringUtils.isNotBlank(line.getOptionValue('p')) ? line.getOptionValue('p') : "oai_dc";
			String baseURL = StringUtils.isNotBlank(line.getOptionValue('u')) ? line.getOptionValue('u') : OAI_ENDPOINT_URL;
			ctx = new Context();

            XOAI indexer = new XOAI(ctx, false, false, verbose);
            applicationContext.getAutowireCapableBeanFactory().autowireBean(indexer);
            
			if (line.hasOption('e')) {
				System.out.println("(0)You call forcing reindex some items in the file, the script exit at end of this activity without checking the full index...");
	            try {
	                String filename = line.getOptionValue('e');
	                FileInputStream fstream = new FileInputStream(filename);
	                // Get the object of DataInputStream
	                DataInputStream in = new DataInputStream(fstream);
	                BufferedReader br = new BufferedReader(new InputStreamReader(in));
	                String strLine;
	                // Read File Line By Line

	                int item_id = 0;
	                List<Integer> ids = new ArrayList<Integer>();

	                while ((strLine = br.readLine()) != null) {
	                    item_id = Integer.parseInt(strLine.trim());
	                    ids.add(item_id);
	                }

	                in.close();

	                indexer.clearIndex(ids);
	                indexer.index(ids);
	            } catch (Exception e) {
	            	System.out.println("Error: " + e.getMessage());
	            }
	        }
			else {
	            List<Integer> missingList = new ArrayList<Integer>();
	            List<Integer> errorList = new ArrayList<Integer>();
	            
	            System.out.println("(1)Retrieve information...");
	            populateOaiLists(baseURL, missingList, errorList, ctx, core, prefix, verbose);
	            
	            System.out.println("(2) build report for missed");
	            logFindingsToFile(missingList, fileBase, "/missing_oai.log");
	            System.out.println("(2) build report for errors");
	            logFindingsToFile(errorList, fileBase, "/error_oai.log");
	
	            if (indexMissing) {
	            	System.out.println("Call with index option: try to rebuild missing items");
	                indexer.index(missingList);
				}
	            
	            if (!errorList.isEmpty()) {
	            	System.out.println("Remove documents from solr");
					indexer.clearIndex(errorList);
				}
			}
		} finally {
			if (ctx != null && ctx.isValid()) {
				ctx.abort();
			}			
		}
		System.out.println("#### END CheckXOAI: -----" + new Date() + " ----- ####");
		
	}
	
	/**
	 * 
	 * Check if all items are present on the specified oai core
	 * if they are not present or they return an error they are put on the appropriate list
	 * 
	 * @param missingList
	 * @param errorList
	 * @param ctx
	 * @param core  The oai core
	 * @param prefix
	 * @throws SQLException
	 * @throws HttpException
	 * @throws IOException
	 */
	private static void populateOaiLists(String baseURL, List<Integer> missingList, List<Integer> errorList, Context ctx, String core, String prefix, boolean verbose) throws SQLException, HttpException, IOException
	{
		String sqlQuery = "SELECT item_id,handle FROM item JOIN handle ON item_id = resource_id WHERE (in_archive=TRUE OR withdrawn=TRUE) AND discoverable=TRUE AND resource_type_id = 2";
        if(DatabaseManager.isOracle()){
            sqlQuery = "SELECT item_id,handle FROM item JOIN handle ON item_id = resource_id WHERE (in_archive=1 OR withdrawn=1) AND discoverable=1 AND resource_type_id = 2";
        }

        TableRowIterator iterator = DatabaseManager.query(ctx,
                sqlQuery);
        HttpClient client = new HttpClient();
        
        int miss = 0, err = 0;
        
        while (iterator.hasNext()) {
			TableRow row = iterator.next(ctx);
			String handle = row.getStringColumn("handle");
			
			GetMethod method = null;
			try {				
				String uri = baseURL + core + "?verb=GetRecord&metadataPrefix=" + prefix + "&identifier=" + DSpaceItem.buildIdentifier(handle,null);
				if(verbose) {
					System.out.println("Work on " + handle);
					System.out.println("URI: " + uri);
				}
				method =  new GetMethod(uri);
				int status = client.executeMethod(method);
                
				if (status == 200) {
					
					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	                factory.setValidating(false);
	                factory.setIgnoringComments(true);
	                factory.setIgnoringElementContentWhitespace(true);

	                DocumentBuilder builder;
                    builder = factory.newDocumentBuilder();
                    InputStream responseBodyAsStream = method.getResponseBodyAsStream();
                    Document inDoc = builder.parse(responseBodyAsStream);
                    Element xmlRoot = inDoc.getDocumentElement();
                    List<Element> pages = XMLUtils.getElementList(xmlRoot,
            				"error");
					
                    if (!pages.isEmpty() && StringUtils.isNotBlank(pages.get(0).getAttribute("code")))
                    {
                    	if (StringUtils.equals(pages.get(0).getAttribute("code"), "idDoesNotExist"))
                    	{
                    		if(verbose) {
                    			System.out.println("RESULT: missed");
                    		}
                    		missingList.add(row.getIntColumn("item_id"));
                    		miss++;
                    		continue;
						}
                		if(verbose) {
                			System.out.println("RESULT: error");
                		}
                    	errorList.add(row.getIntColumn("item_id"));
    					err++;
					}
				} else if (status != 200) {
            		if(verbose) {
            			System.out.println("RESULT: error");
            		}
					errorList.add(row.getIntColumn("item_id"));
					err++;
				}
			} catch (ParserConfigurationException | SAXException e) {
				System.out.println(e);
        		if(verbose) {
        			System.out.println("RESULT: error");
        		}
				errorList.add(row.getIntColumn("item_id"));
				err++;
			} finally
			{
				if (method != null) {
					method.releaseConnection();
				}
			}
		}
        System.out.println("TOTAL missed items: " + miss);
        System.out.println("TOTAL error items: " + err);
	}

	/**
	 * Write all ids to the specified file
	 * either fileBase must end with "/" or filename start with it
	 * 
	 * @param ids
	 * @param fileBase
	 * @param filename
	 * @throws IOException
	 */
	private static void logFindingsToFile(List<Integer> ids, String fileBase, String filename) throws IOException
	{
		BufferedWriter writer = null;
		try {
			File missingFile = new File(fileBase + filename);
			if (!missingFile.createNewFile()) {
				missingFile.delete();
				missingFile.createNewFile();
			}
			writer = new BufferedWriter(new FileWriter(missingFile));
	        for (Integer id : ids)
	        {
				writer.write(id.toString());
				writer.newLine();
			}
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}
	
}
