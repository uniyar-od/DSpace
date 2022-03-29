package org.dspace.app.cris.batch;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.dspace.app.cris.batch.bte.ImpRecordItem;
import org.dspace.app.cris.batch.bte.ImpRecordMetadata;
import org.dspace.app.cris.batch.dao.ImpRecordDAO;
import org.dspace.app.cris.batch.dto.DTOImpRecord;
import org.dspace.app.cris.model.CrisConstants;
import org.dspace.content.authority.Choices;
import org.dspace.core.Context;
import org.dspace.discovery.SearchService;
import org.dspace.discovery.SearchServiceException;
import org.dspace.storage.rdbms.DatabaseManager;
import org.dspace.storage.rdbms.TableRow;
import org.dspace.storage.rdbms.TableRowIterator;
import org.dspace.utils.DSpace;

public class FeedUtils
{
    private static final Logger log = Logger.getLogger(FeedUtils.class);

    public static final char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    static MessageDigest digester;

    static
    {
        try
        {
            digester = MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
    }

    public static Set<String> getIdentifiersToSkip(Context context, String schema,
            String element, String qualifier) throws SQLException
    {
        TableRowIterator tri = DatabaseManager.query(context,
                "SELECT imp_value from imp_metadatavalue where imp_schema like ? and imp_element like ? and imp_qualifier like ?",
                schema, element, qualifier);
        Set<String> identifiersList = new HashSet<String>(10000);
        while (tri.hasNext())
        {
            identifiersList.add(tri.next(context).getStringColumn("imp_value"));
        }
        tri.close();

        tri = DatabaseManager.query(context,
                "select textvalue from imp_values_toignore where metadata = ?",
                schema + "." + element + "." + qualifier);
        while (tri.hasNext())
        {
            identifiersList.add(tri.next(context).getStringColumn("textvalue"));
        }
        tri.close();

        return identifiersList;
    }

    static void writeExecutedQuery(Context context, String q, String checksum,
            int i, int deleted, String feeder)
            throws SQLException
    {
        DatabaseManager.deleteByValue(context, "imp_feed_queries", "checksum",
                checksum);
        TableRow row = DatabaseManager.create(context, "imp_feed_queries");
        row.setColumn("checksum", checksum);
        row.setColumn("query", q);
        row.setColumn("execution_time", new Date());
        row.setColumn("num_insert", i);
        row.setColumn("num_delete", deleted);
        row.setColumn("feeder", feeder);
        DatabaseManager.update(context, row);
    }

    static boolean checkAlreadyImportedIdentifier(String metadata, Set<String> DOIList,
            ImpRecordItem wosItem)
    {
        Set<ImpRecordMetadata> ipm =wosItem.getMetadata().get(metadata);
        if(ipm != null ) {
            for(ImpRecordMetadata value: ipm) {
                if(valueInList(DOIList,value.getValue())) {
                    return true;
                }
            }
        }
        return false;
    }

    static synchronized String getChecksum(String feeder, String q, String start, String end, String symbolic)
            throws NoSuchAlgorithmException, UnsupportedEncodingException
    {
        digester.update(StringUtils
                .join(new String[] { feeder, q, start, end, symbolic }, ":")
                .getBytes("UTF-8"));
        byte[] signature = digester.digest();

        char[] arr = new char[signature.length << 1];
        for (int i = 0; i < signature.length; i++)
        {
            int b = signature[i];
            int idx = i << 1;
            arr[idx] = HEX_DIGITS[(b >> 4) & 0xf];
            arr[idx + 1] = HEX_DIGITS[b & 0xf];
        }
        String checksum = new String(arr);
        digester.reset();
        return checksum;
    }

    private static boolean valueInList(Set<String> identifiers,String toValidate) {
        if(identifiers != null && identifiers.contains(toValidate)) {
            return true;
        }
        return false;
    }

    static void writeDiscardedIdentifiers(Context context,
            ImpRecordItem wosItem, String metadata, String canonicalName) throws SQLException
    {
        Set<ImpRecordMetadata> ipm =wosItem.getMetadata().get(metadata);
        if(ipm != null ) {
            for(ImpRecordMetadata value: ipm) {
                TableRow row = DatabaseManager.create(context, "imp_values_toignore");
                row.setColumn("metadata", metadata);
                row.setColumn("textvalue", value.getValue());
                row.setColumn("note", canonicalName);
                row.setColumn("creation_time", new Date());
                DatabaseManager.update(context, row);
            }
        }
    }

    static boolean shouldRunQuery(Context context, String checksum, long retentionQueryTime)
            throws SQLException
    {
        TableRow row = DatabaseManager.findByUnique(context, "imp_feed_queries",
                "checksum", checksum);
        if (row != null)
        {
            Date excTime = row.getDateColumn("execution_time");
            long now = new Date().getTime();
            if (now - excTime.getTime() > retentionQueryTime)
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        return true;
    }

    static boolean isAtLeastOneAuthorFound(ImpRecordItem wosItem)
    {
        boolean authorityFound = false;
        Set<ImpRecordMetadata> ipm = wosItem.getMetadata().get("dc.contributor.author");
        if(ipm != null) {
            for(ImpRecordMetadata value: ipm ) {
                if(value.getAuthority()!= null) {
                    authorityFound = true;
                    break;
                }
            }
        }
        return authorityFound;
    }

    static boolean isAtLeastOneAffiliationFound(ImpRecordItem wosItem)
    {
        try
        {
            // if there are at least one author confirmed by identifier or exact affiliation match return true immediately
            Set<ImpRecordMetadata> ipm = wosItem.getMetadata().get("dc.contributor.author");
            if(ipm != null) {
                for(ImpRecordMetadata value: ipm ) {
                    if(value.getAuthority() != null && value.getConfidence() > Choices.CF_NOTFOUND) {
                        return true;
                    }
                }
            }
            Set<ImpRecordMetadata> affiliations = wosItem.getMetadata().get("dc.contributor.affiliation");
            StringBuilder query = new StringBuilder();
            if (affiliations == null || affiliations.isEmpty())
            {
                log.error("No affiliations present in " + wosItem.getSourceRef()
                        + " : " + wosItem.getSourceId());
                return false;
            }
            boolean first = true;
            for (ImpRecordMetadata aff : affiliations)
            {
                if (!first)
                {
                    query.append(" OR ");
                }
                first = false;
                query.append("\"").append(ClientUtils.escapeQueryChars(aff.getValue()))
                        .append("\"");
            }

            SearchService searchService = new DSpace()
                    .getSingletonService(SearchService.class);
            SolrQuery solrQuery = new SolrQuery(query.toString());
            solrQuery.addFilterQuery(
                    "search.resourcetype:" + CrisConstants.OU_TYPE_ID);
            QueryResponse response = searchService.search(solrQuery);
            long num = response.getResults().getNumFound();
            if (num > 0)
            {
                return true;
            }
            log.debug("No orgunit found for query: " + query.toString());
        }
        catch (SearchServiceException e)
        {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    static void writeDiscardedIdentifiers(Context context,
            ImpRecordItem wosItem, String feeder) throws SQLException
    {
        writeDiscardedIdentifiers(context, wosItem, "dc.identifier.doi", feeder);
        writeDiscardedIdentifiers(context, wosItem, "dc.identifier.pmid", feeder);
        writeDiscardedIdentifiers(context, wosItem, "dc.identifier.scopus", feeder);
        writeDiscardedIdentifiers(context, wosItem, "dc.identifier.isi", feeder);
    }

    static DTOImpRecord writeImpRecord(String feederName, Context context,
            ImpRecordDAO dao, int collection_id, ImpRecordItem pmeItem,
            String action, Integer epersonId, String status) throws SQLException
    {
        DTOImpRecord dto = new DTOImpRecord(dao);

        HashMap<String, Set<ImpRecordMetadata>> meta = pmeItem.getMetadata();
        for (String md : meta.keySet())
        {
            Set<ImpRecordMetadata> values = meta.get(md);
            String[] splitMd = StringUtils.split(md, "\\.");
            int metadata_order = 0;
            for (ImpRecordMetadata value : values)
            {
                metadata_order++;
                if (splitMd.length > 2)
                {
                    dto.addMetadata(splitMd[0], splitMd[1], splitMd[2], value.getValue(),
                            value.getAuthority(), value.getConfidence(), metadata_order, -1);
                }
                else
                {
                    dto.addMetadata(splitMd[0], splitMd[1], null, value.getValue(), value.getAuthority(),
                            value.getConfidence(), metadata_order, value.getShare());
                }
            }
        }

        dto.setImp_collection_id(collection_id);
        dto.setImp_eperson_id(epersonId);
        dto.setOperation(action);
        dto.setImp_sourceRef(feederName + "." + pmeItem.getSourceRef());
        dto.setImp_record_id(pmeItem.getSourceId());
        dto.setStatus(status);
        return dto;
    }

    public static void addProcessedIdentifiers(ImpRecordItem impRecItem,
            Set<String> dOIList, Set<String> pMIDList, Set<String> eIDList,
            Set<String> iSIIDList)
    {
        addProcessedIdentifiers(impRecItem, "dc.identifier.doi", dOIList);
        addProcessedIdentifiers(impRecItem, "dc.identifier.pmid", pMIDList);
        addProcessedIdentifiers(impRecItem, "dc.identifier.scopus", eIDList);
        addProcessedIdentifiers(impRecItem, "dc.identifier.isi", iSIIDList);
    }

    private static void addProcessedIdentifiers(ImpRecordItem impRecItem,
            String metadata, Set<String> idList)
    {
        Set<ImpRecordMetadata> ipm = impRecItem.getMetadata().get(metadata);
        if(ipm != null ) {
            for(ImpRecordMetadata value: ipm) {
                String value2 = value.getValue();
                if (StringUtils.isNotBlank(value2)) {
                    idList.add(value2);
                }
            }
        }
    }

}
