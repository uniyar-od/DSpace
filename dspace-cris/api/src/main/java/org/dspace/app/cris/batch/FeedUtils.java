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
import java.util.UUID;

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
import org.dspace.eperson.EPerson;
import org.dspace.eperson.factory.EPersonServiceFactory;
import org.dspace.eperson.service.EPersonService;
import org.dspace.utils.DSpace;
import org.hibernate.Session;

public class FeedUtils
{
    private static final Logger log = Logger.getLogger(FeedUtils.class);

    private static final String BATCH_USER = "batchjob@%";

	private static EPersonService ePersonService = EPersonServiceFactory.getInstance().getEPersonService();

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
        Set<String> identifiersList = new HashSet<String>(10000);
		List<Object> tri = getHibernateSession(context)
				.createSQLQuery(
						"SELECT imp_value FROM imp_metadatavalue WHERE imp_schema like :schema and imp_element like :element and imp_qualifier like :qualifier")
				.setParameter("schema", schema)
				.setParameter("element", element)
				.setParameter("qualifier", qualifier).list();
        for (Object tr : tri)
        {
            identifiersList.add(tr.toString());
        }

        tri = getHibernateSession(context).createSQLQuery("SELECT textvalue FROM imp_values_toignore WHERE metadata = :metadata")
        		.setParameter("metadata", schema + "." + element + "." + qualifier).list();
        for (Object tr : tri)
        {
            identifiersList.add(tr.toString());
        }

        return identifiersList;
    }

    static void writeExecutedQuery(Context context, String q, String checksum,
            int i, int deleted, String feeder)
            throws SQLException
    {
    	getHibernateSession(context).createSQLQuery("delete from imp_feed_queries where checksum = :checksum")
		.setParameter("checksum", checksum);

    	getHibernateSession(context).createSQLQuery(
                "INSERT INTO imp_feed_queries(imp_feed_queries_id, checksum, query, execution_time, num_insert, num_delete, feeder)"
                        + " VALUES (nextval(\'imp_feed_queries_seq\'), :checksum, :query, :execution_time, :num_insert, :num_delete, :feeder)")
				.setParameter("checksum", checksum)
				.setParameter("query", q)
				.setParameter("execution_time", new Date())
				.setParameter("num_insert", i)
				.setParameter("num_delete", deleted)
				.setParameter("feeder", feeder).executeUpdate();
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
            	getHibernateSession(context).createSQLQuery(
                        "INSERT INTO imp_values_toignore(imp_values_toignore_id, metadata, textvalue, note, creation_time)"
                                + " VALUES (nextval(\'imp_values_toignore_seq\'), :metadata, :textvalue, :note, :creation_time)")
        				.setParameter("metadata", metadata)
        				.setParameter("textvalue", value.getValue())
        				.setParameter("note", canonicalName)
        				.setParameter("creation_time", new Date()).executeUpdate();
            }
        }
    }

    static boolean shouldRunQuery(Context context, String checksum, long retentionQueryTime)
            throws SQLException
    {
    	Object row = getHibernateSession(context).createSQLQuery("select execution_time from imp_feed_queries where checksum = :checksum").setParameter("checksum", checksum).uniqueResult();
        if (row != null)
        {
            Date excTime = (Date)row;
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
            ImpRecordDAO dao, UUID collection_id, ImpRecordItem pmeItem,
            String action, UUID epersonId, String status) throws SQLException
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

    public static Session getHibernateSession(Context context) throws SQLException {
        return ((Session) context.getDBConnection().getSession());
    }

    public static UUID retrieveDefaultSubmitter(Context context, String defaultSubmitter) throws SQLException {
        UUID id = null;
        if (StringUtils.isNotBlank(defaultSubmitter)) {
            EPerson eperson = null;
            if (StringUtils.indexOf(defaultSubmitter, '@') == -1) {
                eperson = ePersonService.find(context, UUID.fromString(defaultSubmitter));
            } else {
                eperson = ePersonService.findByEmail(context, defaultSubmitter);
            }
            if (eperson != null) {
                id = eperson.getID();
            }
        } else {
            // try to get user batchjob
            List<EPerson> row = EPersonServiceFactory.getInstance().getEPersonService().search(context, BATCH_USER);
            if (row != null && !row.isEmpty()) {
                id = row.get(0).getID();
            }
        }

        return id;
    }
}
