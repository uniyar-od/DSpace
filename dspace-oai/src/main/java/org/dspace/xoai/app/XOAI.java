/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.xoai.app;

import static com.lyncode.xoai.dataprovider.core.Granularity.Second;
import static org.dspace.xoai.util.ItemUtils.retrieveMetadata;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import javax.xml.stream.XMLStreamException;

import com.lyncode.xoai.dataprovider.exceptions.ConfigurationException;
import com.lyncode.xoai.dataprovider.exceptions.MetadataBindException;
import com.lyncode.xoai.dataprovider.exceptions.WritingXmlException;
import com.lyncode.xoai.dataprovider.xml.XmlOutputContext;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.dspace.app.cris.model.ACrisObject;
import org.dspace.app.cris.model.CrisConstants;
import org.dspace.app.cris.util.MetadatumAuthorityDecorator;
import org.dspace.app.cris.util.UtilsCrisMetadata;
import org.dspace.authorize.ResourcePolicy;
import org.dspace.authorize.factory.AuthorizeServiceFactory;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.browse.BrowsableDSpaceObject;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.IMetadataValue;
import org.dspace.content.Item;
import org.dspace.content.MetadataValue;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.ItemService;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.core.Utils;
import org.dspace.discovery.DiscoverQuery;
import org.dspace.discovery.DiscoverResult;
import org.dspace.discovery.SearchServiceException;
import org.dspace.discovery.SearchUtils;
import org.dspace.xoai.services.api.CollectionsService;
import org.dspace.utils.DSpace;
import org.dspace.xoai.services.api.solr.SolrServerResolver;
import org.dspace.xoai.solr.DSpaceSolrSearch;
import org.dspace.xoai.solr.exceptions.DSpaceSolrException;
import org.dspace.xoai.solr.exceptions.DSpaceSolrIndexerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.lyncode.xoai.dataprovider.xml.xoai.Metadata;

/**
 * @author Lyncode Development Team <dspace@lyncode.com>
 */
@SuppressWarnings("deprecation")
public class XOAI {
    public static final String ITEMTYPE_DEFAULT = "item";

    public static final String ITEMTYPE_SPECIAL = "cfitem";

    private static Logger log = LogManager.getLogger(XOAI.class);

    private final Context context;
    private boolean optimize;
    private final boolean verbose;
    private boolean clean;

    @Autowired
    private SolrServerResolver solrServerResolver;
    @Autowired
    private CollectionsService collectionsService;

    private final AuthorizeService authorizeService;
    private final ItemService itemService;

    private List<String> getFileFormats(Item item) {
        List<String> formats = new ArrayList<>();
        try {
            for (Bundle b : itemService.getBundles(item, "ORIGINAL")) {
                for (Bitstream bs : b.getBitstreams()) {
                    if (!formats.contains(bs.getFormat(context).getMIMEType())) {
                        formats.add(bs.getFormat(context).getMIMEType());
                    }
                }
            }
        } catch (SQLException ex) {
            log.error(ex.getMessage(), ex);
        }
        return formats;
    }

    public XOAI(Context context, boolean optimize, boolean clean, boolean verbose) {
        this.context = context;
        this.optimize = optimize;
        this.clean = clean;
        this.verbose = verbose;

        // Load necessary DSpace services
        this.authorizeService = AuthorizeServiceFactory.getInstance().getAuthorizeService();
        this.itemService = ContentServiceFactory.getInstance().getItemService();
    }

    public XOAI(Context ctx, boolean hasOption) {
        this.context = ctx;
        this.verbose = hasOption;

        // Load necessary DSpace services
        this.authorizeService = AuthorizeServiceFactory.getInstance().getAuthorizeService();
        this.itemService = ContentServiceFactory.getInstance().getItemService();
    }

    private void println(String line) {
        System.out.println(line);
    }

    public int index(String idxType) throws DSpaceSolrIndexerException {
        int result = 0;
        try {

            if (clean) {
                clearIndex(idxType);
                System.out.println("Using full import.");
                result = this.indexAll(idxType);
            } else {
                SolrQuery solrParams = new SolrQuery("*:*")
                        .addField("item.lastmodified")
                        .addSortField("item.lastmodified", ORDER.desc).setRows(1);

                SolrDocumentList results = DSpaceSolrSearch.query(solrServerResolver.getServer(), solrParams);
                if (results.getNumFound() == 0) {
                    System.out.println("There are no indexed documents, using full import.");
                    result = this.indexAll(idxType);
                } else {
                    result = this.index(idxType, (Date) results.get(0).getFieldValue("item.lastmodified"));
                }
            }
            solrServerResolver.getServer().commit();


            if (optimize) {
                println("Optimizing Index");
                solrServerResolver.getServer().optimize();
                println("Index optimized");
            }

            return result;
        } catch (DSpaceSolrException | SolrServerException | IOException ex) {
            throw new DSpaceSolrIndexerException(ex.getMessage(), ex);
        }
    }

    /***
     * index all data whose modification date is greater than given start date.
     * 
     * @param idxType The index type (item, rp, ...)
     * @param start The latest date
     * @return The number of indexed fields.
     * @throws DSpaceSolrIndexerException
     */
    private int index(String idxType, Date last) throws DSpaceSolrIndexerException {
        System.out.println("Incremental import. Searching for documents modified after: " + last.toString());
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        df.setTimeZone(tz);
        
        String discoverQuery = "lastModified:{%s TO *}";
        discoverQuery = String.format(discoverQuery, df.format(last));
        switch (idxType) {
        case ITEMTYPE_DEFAULT:
        case "rp":
        case "project":
        case "ou":
        case "other":
        case "all":
            break;
        default:
            throw new DSpaceSolrIndexerException("The partial index is not supported for type " + idxType);
        }
        discoverQuery += " AND " + buildQuery(idxType);

        try {
            return indexWithQuery(discoverQuery);
        } catch (Exception ex) {
            throw new DSpaceSolrIndexerException(ex.getMessage(), ex);
        }
    }

    private int indexAll(String idxType) throws DSpaceSolrIndexerException {
        System.out.println("Full import");
        try {
            String discoverQuery = buildQuery(idxType);
            
            return indexWithQuery(discoverQuery);
        } catch (Exception ex) {
            throw new DSpaceSolrIndexerException(ex.getMessage(), ex);
        }
    }
    
    /***
     * Build the query used to retrieve the data of give type idxType.
     * 
     * The query can be overridden using:
     * 	oai.discover.query.item
     * 	oai.discover.query.crisrp
     * 	oai.discover.query.crisproject
     * 	oai.discover.query.crisou
     * 	oai.discover.query.cris<OtherObject>
     * 
     * @param idxType The index type
     * @return The number of indexed fields.
     * @throws DSpaceSolrIndexerException
     */
    private String buildQuery(String idxType) throws DSpaceSolrIndexerException {
    	String discoverQuery = "";
    	
    	switch (idxType) {
        case ITEMTYPE_DEFAULT:
        	discoverQuery = ConfigurationManager.getProperty("oai", "oai.discover.query.item");
        	if (discoverQuery == null || discoverQuery.trim().length() <= 0) {
        		discoverQuery = "discoverable:true AND search.resourcetype:2";
        	}
	        break;
        case "rp":
        	discoverQuery = ConfigurationManager.getProperty("oai", "oai.discover.query.crisrp");
        	if (discoverQuery == null || discoverQuery.trim().length() <= 0) {
        		discoverQuery = "discoverable:true AND search.resourcetype:" + CrisConstants.RP_TYPE_ID;
        	}
	        break;
        case "project":
        	discoverQuery = ConfigurationManager.getProperty("oai", "oai.discover.query.crisproject");
        	if (discoverQuery == null || discoverQuery.trim().length() <= 0) {
        		discoverQuery = "discoverable:true AND search.resourcetype:" + CrisConstants.PROJECT_TYPE_ID;
        	}
	        break;
        case "ou":
        	discoverQuery = ConfigurationManager.getProperty("oai", "oai.discover.query.crisou");
        	if (discoverQuery == null || discoverQuery.trim().length() <= 0) {
        		discoverQuery = "discoverable:true AND search.resourcetype:" + CrisConstants.OU_TYPE_ID;
        	}
	        break;
        case "other":
        	discoverQuery = ConfigurationManager.getProperty("oai", "oai.discover.query.crisother");
        	if (discoverQuery == null || discoverQuery.trim().length() <= 0) {
        		discoverQuery = "discoverable:true AND search.resourcetype:{" + CrisConstants.CRIS_DYNAMIC_TYPE_ID_START + " TO *}";
        	}
	        break;
        case "all":
        	discoverQuery = ConfigurationManager.getProperty("oai", "oai.discover.query.all");
        	if (discoverQuery == null || discoverQuery.trim().length() <= 0)
        		discoverQuery = "discoverable:true";
        	break;
    	default:
    		throw new DSpaceSolrIndexerException("Index is not supported for type " + idxType);
    	}
    	return discoverQuery;
    }
    
    /***
     * Paged solr query used to retrieve data to index.
     * The page size can be modified using:
     * 	oai.discover.pagesize
     * 
     * @param solrQuery The query
     * @return The number of indexed data.
     * @throws DSpaceSolrIndexerException
     */
    private int indexWithQuery(String solrQuery) throws DSpaceSolrIndexerException {
    	String discoverPageSize = "";
		try {
			int pageSize = -1;
			int total = 0;
			int read = -1;
	    	
	    	discoverPageSize = ConfigurationManager.getProperty("oai", "oai.discover.pagesize");
	    	if (discoverPageSize == null || discoverPageSize.trim().length() <= 0)
	    		pageSize = 100;
	    	else {
	    		pageSize = Integer.parseInt(discoverPageSize);
	    	}
	    	
	    	DiscoverResult results = null;
	    	int page = 0;
	    	int offset;
	    	
	    	do {
	    		offset = page > 0 ? page * pageSize : 0;
			
	    		DiscoverQuery query = new DiscoverQuery();
	    		query.setQuery(solrQuery);
	    		query.setMaxResults(pageSize);
	    		query.setStart(offset);
	    		query.addSearchField("item.cerifentitytype");
			 	results = SearchUtils.getSearchService().search(context, query, true);
			 	read = 0;
			 	if (!results.getDspaceObjects().isEmpty())
			 		read = indexResults(results, total);
			 	page++;
			 	total += read;
	    	}
	    	while (read == pageSize);
	    	
	    	System.out.println("Total: " + total + " items");
			return total;
		} catch (SearchServiceException e) {
			String message = "Error while processing solr query results: " + e.getMessage();
			log.error(message, e);
			throw new DSpaceSolrIndexerException(message, e);
		} catch (DSpaceSolrIndexerException e) {
			String message = "Error while processing solr query results: \" + e.getMessage()";
			log.error(message, e);
			throw new DSpaceSolrIndexerException(message, e);
		} catch (NumberFormatException e) {
			String message = "Error in option oai.discover.pagesize: \" + discoverPageSize + \". \" + e.getMessage()";
			log.error(message, e);
			throw new DSpaceSolrIndexerException(message, e);
		}
    }

    /**
     * Check if an item is already indexed. Using this, it is possible to check
     * if withdrawn or nondiscoverable items have to be indexed at all.
     * 
     * @param item
     *            Item that should be checked for its presence in the index.
     * @return has it been indexed?
     */
    private boolean checkIfIndexed(Item item) {
        SolrQuery params = new SolrQuery("item.id:" + item.getID().toString()).addField("item.id");
        try {
            SolrDocumentList documents = DSpaceSolrSearch.query(solrServerResolver.getServer(), params);
            return documents.getNumFound() == 1;
        } catch (DSpaceSolrException | SolrServerException e) {
            return false;
        }
    }
    
    /***
     * Read one page of data.
     * 
     * @param result The paged data
     * @param subtotal The number of data processed so far.
     * @return The number of indexed data.
     * @throws DSpaceSolrIndexerException
     */
    @SuppressWarnings("rawtypes")
    private int indexResults(DiscoverResult result, int subtotal)
            throws DSpaceSolrIndexerException {
        try {
            int i = 0;
            SolrServer server = solrServerResolver.getServer();
            for (BrowsableDSpaceObject o : result.getDspaceObjects()) {
                try {
                	SolrInputDocument solrDoc = null;
                	boolean doublingSolrDocument = false;
                	if (o instanceof Item) {
                	    Item item = (Item)o;
                	    String type = (String)item.getExtraInfo().get("item.cerifentitytype");
                	    solrDoc = this.indexResults(item, false);
                	    if(StringUtils.isNotBlank(type)) {
                	        doublingSolrDocument = true;
                	    }
                	}
                	else if (o instanceof ACrisObject) {
                		solrDoc = this.indexResults((ACrisObject)o);
                	}
                	server.add(solrDoc);
                	
                	if(doublingSolrDocument) {
                	    Item item = (Item)o;
                	    solrDoc = this.indexResults(item, true);
                	    server.add(solrDoc);
                	}
                } catch (SQLException ex) {
                    log.error(ex.getMessage(), ex);
                } catch (MetadataBindException e) {
                    log.error(e.getMessage(), e);
                } catch (ParseException e) {
                    log.error(e.getMessage(), e);
                } catch (XMLStreamException e) {
                    log.error(e.getMessage(), e);
                } catch (WritingXmlException e) {
                    log.error(e.getMessage(), e);
                }
                i++;
                if ((i+subtotal) % 100 == 0) System.out.println((i+subtotal) + " items imported so far...");
            }
            System.out.println("Partial Total: " + (i+subtotal) + " items");
            server.commit();
            return i;
        } catch (SolrServerException ex) {
            throw new DSpaceSolrIndexerException(ex.getMessage(), ex);
        } catch (IOException ex) {
            throw new DSpaceSolrIndexerException(ex.getMessage(), ex);
        }
    }

    /***
     * Index one item
     * 
     * @param item The item
     * @return The sorl document
     * @throws SQLException
     * @throws MetadataBindException
     * @throws ParseException
     * @throws XMLStreamException
     * @throws WritingXmlException
     */
    private SolrInputDocument indexResults(Item item, boolean specialIdentifier) throws SQLException, MetadataBindException, ParseException, XMLStreamException, WritingXmlException {
        item.setWrapperEnabled(true);

        SolrInputDocument doc = new SolrInputDocument();
        doc.addField("item.id", item.getID());
        
        boolean isEmbargoed = !this.isPublic(item);
        boolean isCurrentlyVisible = this.checkIfVisibleInOAI(item);
        boolean isIndexed = this.checkIfIndexed(item);

        /*
         * If the item is not under embargo, it should be visible. If it is,
         * make it invisible if this is the first time it is indexed. For
         * subsequent index runs, keep the current status, so that if the item
         * is embargoed again, it is flagged as deleted instead and does not
         * just disappear, or if it is still under embargo, it won't become
         * visible and be known to harvesters as deleted before it gets
         * disseminated for the first time. The item has to be indexed directly
         * after publication even if it is still embargoed, because its
         * lastModified date will not change when the embargo end date (or start
         * date) is reached. To circumvent this, an item which will change its
         * status in the future will be marked as such.
         */

        boolean isPublic = isEmbargoed ? (isIndexed ? isCurrentlyVisible : false) : true;
        
        doc.addField("item.public", isPublic);
        
        String handle = item.getHandle();
        if (verbose) {
            println("Prepare handle " + handle);
        }
        
        String type = (String)item.getExtraInfo().get("item.cerifentitytype");
        if(StringUtils.isNotBlank(type) && specialIdentifier) {
            doc.addField("item.identifier", type +"/"+ handle);
            doc.addField("item.type", ITEMTYPE_SPECIAL);
        }
        else {
            doc.addField("item.identifier", handle);
            doc.addField("item.type", ITEMTYPE_DEFAULT);
        }
        
        doc.addField("item.handle", handle);
        if (item.getSubmitter() != null) {
            doc.addField("item.submitter", item.getSubmitter().getEmail());
        }
        
        /*
         * Mark an item as deleted not only if it is withdrawn, but also if it
         * is made private, because items should not simply disappear from OAI
         * with a transient deletion policy. Do not set the flag for still
         * invisible embargoed items, because this will override the item.public
         * flag.
         */

        doc.addField("item.deleted",
                (item.isWithdrawn() || !item.isDiscoverable() || (isEmbargoed ? isPublic : false)));

        /*
         * An item that is embargoed will potentially not be harvested by
         * incremental harvesters if the from and until params do not encompass
         * both the standard lastModified date and the anonymous-READ resource
         * policy start date. The same is true for the end date, where
         * harvesters might not get a tombstone record. Therefore, consider all
         * relevant policy dates and the standard lastModified date and take the
         * most recent of those which have already passed.
         */
        doc.addField("item.lastmodified", this.getMostRecentModificationDate(item));

        
        doc.addField("item.willChangeStatus", willChangeStatus(item));
        
        for (Collection col : item.getCollections()) {
            doc.addField("item.collections",
                    "col_" + col.getHandle().replace("/", "_"));
            for (Community com : collectionsService.flatParentCommunities(col)) {
                doc.addField("item.communities",
                        "com_" + com.getHandle().replace("/", "_"));
            }
        }
        
        List<IMetadataValue> allData = item.getMetadata(Item.ANY, Item.ANY, Item.ANY,
                Item.ANY);
        for (IMetadataValue dc : allData) {
            String key = "metadata." + Utils.standardize(dc.getSchema(), dc.getElement(), dc.getQualifier(), ".");
            
            String val =StringUtils.equals(dc.getValue(), MetadataValue.PARENT_PLACEHOLDER_VALUE)? "N/D":dc.getValue();  

            doc.addField(key, val);
            if (dc.getAuthority() != null) {
                doc.addField(key + ".authority", dc.getAuthority());
                doc.addField(key + ".confidence", dc.getConfidence() + "");
            }
        }

        for (String f : getFileFormats(item)) {
            doc.addField("metadata.dc.format.mimetype", f);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XmlOutputContext xmlContext = XmlOutputContext.emptyContext(out, Second);
        Metadata metadata = null;
        if(StringUtils.isNotBlank(type) && specialIdentifier) {
            metadata = retrieveMetadata(context, item, true);
        }
        else {
            metadata = retrieveMetadata(context, item, false);
        }

        //Do any additional content on "item.compile" field, depends on the plugins
        List<XOAIItemCompilePlugin> xOAIItemCompilePlugins = new DSpace().getServiceManager().getServicesByType(XOAIItemCompilePlugin.class);
        for (XOAIItemCompilePlugin xOAIItemCompilePlugin : xOAIItemCompilePlugins)
        {
            metadata = xOAIItemCompilePlugin.additionalMetadata(context, metadata, item);
        }

        metadata.write(xmlContext);
        xmlContext.getWriter().flush();
        xmlContext.getWriter().close();
        doc.addField("item.compile", out.toString());

        if (verbose) {
            println(String.format("Item %s with handle %s indexed",
                    item.getID().toString(), handle));
        }

        return doc;
    }

    /***
     * Index one cris item
     * 
     * @param item The cris item
     * @return The sorl document
     * @throws SQLException
     * @throws MetadataBindException
     * @throws ParseException
     * @throws XMLStreamException
     * @throws WritingXmlException
     */
    @SuppressWarnings("rawtypes")
    private SolrInputDocument indexResults(ACrisObject item) throws SQLException, MetadataBindException, ParseException, XMLStreamException, WritingXmlException {
        SolrInputDocument doc = new SolrInputDocument();
        doc.addField("item.id", item.getID());
        boolean pub = item.getStatus();
        doc.addField("item.public", pub);
        String handle = item.getHandle();
        if (verbose) {
            println("Prepare handle " + handle);
        }
        
        String type = ConfigurationManager.getProperty("oai", "identifier.cerifentitytype." + item.getPublicPath());
        if(StringUtils.isNotBlank(type)) {
            doc.addField("item.identifier", type + "/" + handle);    
        }
        else {
            doc.addField("item.identifier", handle);
        }
        
        doc.addField("item.handle", handle);
        doc.addField("item.type", item.getPublicPath());
        doc.addField("item.lastmodified", item.getLastModified());
        doc.addField("item.deleted", "false");

        MetadatumAuthorityDecorator[] allDataAuthDec = UtilsCrisMetadata.getAllMetadata(item, true, true, "oai");
        if (allDataAuthDec != null)
        {
            for (MetadatumAuthorityDecorator dcAuthDec : allDataAuthDec)
            {
                IMetadataValue dc = dcAuthDec.getMetadatum();
                String key = "metadata." + Utils.standardize(dc.getSchema(), dc.getElement(), dc.getQualifier(), ".");

                String val = StringUtils.equals(dc.getValue(),
                        MetadataValue.PARENT_PLACEHOLDER_VALUE) ? "N/D"
                                : dc.getValue();

                doc.addField(key, val);
                if (dc.getAuthority() != null)
                {
                    doc.addField(key + ".authority", dc.getAuthority());
                    doc.addField(key + ".confidence", dc.getConfidence() + "");
                }
            }
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XmlOutputContext xmlContext = XmlOutputContext.emptyContext(out, Second);
        retrieveMetadata(context, item).write(xmlContext);
        xmlContext.getWriter().flush();
        xmlContext.getWriter().close();
        doc.addField("item.compile", out.toString());

        if (verbose) {
            println(String.format("Cris Item %s with handle %s indexed (type: %s)",
                    item.getCrisID(), handle, item.getTypeText()));
        }

        return doc;
    }
    
    /**
     * Check if an item is flagged visible in the index.
     * 
     * @param item
     *            Item that should be checked for its presence in the index.
     * @return has it been indexed?
     */
    private boolean checkIfVisibleInOAI(Item item) {
        SolrQuery params = new SolrQuery("item.id:" + item.getID().toString()).addField("item.public");
        try {
            SolrDocumentList documents = DSpaceSolrSearch.query(solrServerResolver.getServer(), params);
            if (documents.getNumFound() == 1) {
                return (boolean) documents.get(0).getFieldValue("item.public");
            } else {
                return false;
            }
        } catch (DSpaceSolrException | SolrServerException e) {
            return false;
        }
    }

    /**
     * Method to get the most recent date on which the item changed concerning
     * the OAI deleted status (policy start and end dates for all anonymous READ
     * policies and the standard last modification date)
     *
     * @param item
     *            Item
     * @return date
     * @throws SQLException
     */

    private Date getMostRecentModificationDate(Item item) throws SQLException {
        List<Date> dates = new LinkedList<Date>();
        List<ResourcePolicy> policies = authorizeService.getPoliciesActionFilter(context, item, Constants.READ);
        for (ResourcePolicy policy : policies) {        	
        	if ((policy.getGroup()!=null) && (policy.getGroup().getName().equals("Anonymous"))) {
                if (policy.getStartDate() != null) {
                    dates.add(policy.getStartDate());
                }
                if (policy.getEndDate() != null) {
                    dates.add(policy.getEndDate());
                }
            }
        	context.uncacheEntity(policy);
        }
        dates.add(item.getLastModified());
        Collections.sort(dates);
        Date now = new Date();
        Date lastChange = null;
        for (Date d : dates) {
            if (d.before(now)) {
                lastChange = d;
            }
        }
        return lastChange;
    }

    private boolean willChangeStatus(Item item) throws SQLException {

        List<ResourcePolicy> policies = authorizeService.getPoliciesActionFilter(context, item, Constants.READ);
        for (ResourcePolicy policy : policies) {
        	if ((policy.getGroup()!=null) && (policy.getGroup().getName().equals("Anonymous"))) {
                
                if (policy.getStartDate() != null && policy.getStartDate().after(new Date())) {
                    
                    return true;
                }
                if (policy.getEndDate() != null && policy.getEndDate().after(new Date())) {
                    
                    return true;
                }
            }
        	context.uncacheEntity(policy);
        }
        
        return false;
    }

    private boolean isPublic(Item item) {
        boolean pub = false;
        try {
            // Check if READ access allowed on this Item
            pub = authorizeService.authorizeActionBoolean(context, item, Constants.READ);
        } catch (SQLException ex) {
            log.error(ex.getMessage());
        }
        return pub;
    }

    private static boolean getKnownExplanation(Throwable t) {
        if (t instanceof ConnectException) {
            System.err.println(
                    "Solr server (" + ConfigurationManager.getProperty("oai", "solr.url") + ") is down, turn it on.");
            return true;
        }

        return false;
    }

    private static boolean searchForReason(Throwable t) {
        if (getKnownExplanation(t))
            return true;
        if (t.getCause() != null)
            return searchForReason(t.getCause());
        return false;
    }

    private void clearIndex(String idxType) throws DSpaceSolrIndexerException {
        try {
            System.out.println("Clearing index");
            
            String eraseQuery = "";
            switch (idxType) {
            case ITEMTYPE_DEFAULT:
            	eraseQuery = ConfigurationManager.getProperty("oai", "oai.erase.query.item");
            	if (eraseQuery == null || eraseQuery.trim().length() <= 0) {
            		eraseQuery = "item.type:item";
            	}
    	        break;
            case "rp":
            	eraseQuery = ConfigurationManager.getProperty("oai", "oai.erase.query.crisrp");
            	if (eraseQuery == null || eraseQuery.trim().length() <= 0) {
            		eraseQuery = "item.type:rp";
            	}
    	        break;
            case "project":
            	eraseQuery = ConfigurationManager.getProperty("oai", "oai.erase.query.crisproject");
            	if (eraseQuery == null || eraseQuery.trim().length() <= 0) {
            		eraseQuery = "item.type:project";
            	}
    	        break;
            case "ou":
            	eraseQuery = ConfigurationManager.getProperty("oai", "oai.erase.query.crisou");
            	if (eraseQuery == null || eraseQuery.trim().length() <= 0) {
            		eraseQuery = "item.type:ou";
            	}
    	        break;
            case "other":
            	eraseQuery = ConfigurationManager.getProperty("oai", "oai.erase.query.crisother");
            	if (eraseQuery == null || eraseQuery.trim().length() <= 0) {
            		eraseQuery = "NOT (item.type:item OR item.type:rp OR item.type:project OR item.type:ou)";
            	}
    	        break;
            case "all":
            	eraseQuery = ConfigurationManager.getProperty("oai", "oai.erase.query.all");
            	if (eraseQuery == null || eraseQuery.trim().length() <= 0) {
            		eraseQuery = "*:*";
            	}
            	break;
            default:
            	throw new DSpaceSolrIndexerException("Index is not supported for type " + idxType);
            }
            solrServerResolver.getServer().deleteByQuery(eraseQuery);
            solrServerResolver.getServer().commit();
            System.out.println("Index cleared");
        } catch (SolrServerException | IOException ex) {
            throw new DSpaceSolrIndexerException(ex.getMessage(), ex);
        }
    }

    private static final String COMMAND_IMPORT = "import";

    public static void main(String[] argv) throws IOException, ConfigurationException {

        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(new Class[]{
                BasicConfiguration.class
        });
        Context ctx = null;

        try {
            CommandLineParser parser = new PosixParser();
            Options options = new Options();
            options.addOption("c", "clear", false, "Clear index before indexing");
            options.addOption("o", "optimize", false, "Optimize index at the end");
            options.addOption("v", "verbose", false, "Verbose output");
            options.addOption("h", "help", false, "Shows some help");
            options.addOption("n", "number", true, "FOR DEVELOPMENT MUST DELETE");
            options.addOption("t", "type", true, "Type of index (item, rp, project, ou, other, all). The default is 'all'.");
            CommandLine line = parser.parse(options, argv);

            String[] validSolrCommands = {COMMAND_IMPORT};

            boolean run = false;
            if (line.getArgs().length > 0) {
                    if (Arrays.asList(validSolrCommands).contains(line.getArgs()[0])) {
                        run = true;
                    }
            }

            if (!line.hasOption('h') && run) {
                System.out.println("OAI 2.0 manager action started");
                long start = System.currentTimeMillis();

                String command = line.getArgs()[0];

                if (COMMAND_IMPORT.equals(command)) {
                    ctx = new Context(Context.Mode.BATCH_READ);
                    XOAI indexer = new XOAI(ctx, 
                            line.hasOption('o'), 
                            line.hasOption('c'), 
                            line.hasOption('v'));

                    applicationContext.getAutowireCapableBeanFactory().autowireBean(indexer);
                    
                    String idxType = line.getOptionValue("t");
                    if (idxType == null || idxType.trim().length() <= 0)
                    	idxType = "all";
                    indexer.index(idxType);
                }
                System.out.println("OAI 2.0 manager action ended. It took "
                        + ((System.currentTimeMillis() - start) / 1000)
                        + " seconds.");
            } else {
                usage();
            }
        } catch (Throwable ex) {
            if (!searchForReason(ex)) {
                ex.printStackTrace();
            }
            log.error(ex.getMessage(), ex);
        }
        finally
        {
            // Abort our context, if still open
            if(ctx!=null && ctx.isValid())
                ctx.abort();
        }
    }

    private static void usage()
    {
        System.out.println("OAI Manager Script");
        System.out.println("Syntax: oai <action> [parameters]");
        System.out.println("> Possible actions:");
        System.out.println("     " + COMMAND_IMPORT
                + " - To import DSpace items and/or DSpace-CRIS entities into OAI index and cache system");
        System.out.println("> Parameters:");
        System.out.println("     -o Optimize index after indexing ("
                + COMMAND_IMPORT + " only)");
        System.out.println("     -c Clear index (" + COMMAND_IMPORT + " only)");
        System.out.println("     -v Verbose output");
        System.out.println("     -h Shows this text");
    }
}
