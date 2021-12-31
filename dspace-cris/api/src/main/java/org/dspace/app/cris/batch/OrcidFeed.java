/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * https://github.com/CILEA/dspace-cris/wiki/License
 */
package org.dspace.app.cris.batch;

import java.io.IOException;
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
import org.dspace.app.cris.batch.bte.ImpRecordItem;
import org.dspace.app.cris.batch.bte.ImpRecordMetadata;
import org.dspace.app.cris.batch.bte.ImpRecordOutputGenerator;
import org.dspace.app.cris.batch.dao.ImpRecordDAO;
import org.dspace.app.cris.batch.dao.ImpRecordDAOFactory;
import org.dspace.app.cris.batch.dto.DTOImpRecord;
import org.dspace.app.cris.integration.orcid.OrcidOnlineDataLoader;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.factory.EPersonServiceFactory;
import org.dspace.eperson.service.EPersonService;
import org.dspace.submit.lookup.MultipleSubmissionLookupDataLoader;
import org.dspace.submit.lookup.SubmissionItemDataLoader;
import org.dspace.submit.lookup.SubmissionLookupDataLoader;
import org.dspace.submit.lookup.SubmissionLookupOutputGenerator;
import org.dspace.submit.util.ItemSubmissionLookupDTO;
import org.dspace.utils.DSpace;
import org.hibernate.SQLQuery;
import org.hibernate.Session;

import gr.ekt.bte.core.Record;
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

	public static void main(String[] args) throws SQLException,
			BadTransformationSpec, MalformedSourceException, AuthorizeException {

		Context context = new Context();
		HelpFormatter formatter = new HelpFormatter();

		String usage = "org.dspace.app.cris.batch.OrcidFeed -i orcid -p submitter -c collectionID";

		Options options = new Options();
		CommandLine line = null;

		options.addOption(Option.builder("i").argName("orcid").hasArg(true)
				.desc("Identifier of the Orcid registry")
				.build());

		options.addOption(Option.builder("c").required(true)
				.argName("collectionID").hasArg(true)
				.desc("Collection for item submission")
				.build());

		options.addOption(Option.builder("p").required(true).argName("Eperson")
				.hasArg(true).desc("Submitter of the records")
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

		if (!line.hasOption("i") || !line.hasOption("c")
				|| !line.hasOption("p")) {
			formatter.printHelp(usage, "", options, "");
			System.exit(1);
		}

		String person = line.getOptionValue("p");
		EPerson eperson = null;
		try {
			eperson = ePersonService.find(context, UUID.fromString(person));
		} catch (IllegalArgumentException e) {
			eperson = ePersonService.findByEmail(context, person);
		}

		if (eperson != null) {
			context.setCurrentUser(eperson);
		} else {
			formatter.printHelp(usage, "No user found", options, "");
			System.exit(1);
		}

		UUID collectionId = UUID.fromString(line.getOptionValue("c"));
		boolean forceCollectionId = line.hasOption("f");
		String orcid = line.getOptionValue("i");

		// p = workspace, w = workflow step 1, y = workflow step 2, x =
		// workflow step 3, z = inarchive
		String status = "p";
		if (line.hasOption("o")) {
			status = line.getOptionValue("o");
		}

		try {
			System.out.println("Starting query");
			retrievePublication(context, eperson, collectionId,
					forceCollectionId, orcid, status);
			context.complete();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			if (context != null && context.isValid()) {
				context.abort();
			}
		}
	}

	public static void retrievePublication(Context context, EPerson eperson,
			UUID collectionId, boolean forceCollectionId, String orcid, String status) throws HttpException, IOException,
			BadTransformationSpec, MalformedSourceException, SQLException {
		List<String> alreadyInImpRecord = new ArrayList<String>();

		// select putcode already imported
		String query = "select distinct :imp_record_id from imp_record where :imp_eperson_id=:epersonid and :imp_sourceref=:orcid";
		SQLQuery sq = getHibernateSession(context).createSQLQuery(query);
		sq.setParameter("imp_record_id", IMP_RECORD_ID);
		sq.setParameter("imp_eperson_id", IMP_EPERSON_ID);
		sq.setParameter("epersonid", eperson.getID().toString());
		sq.setParameter("imp_sourceref", IMP_SOURCE_REF);
		sq.setParameter("orcid", "orcid");
		for (Object[] rec : (List<Object[]>)sq.list()) {
			alreadyInImpRecord.add(rec[0].toString());
		}

		int total = 0;
		int deleted = 0;

		ImpRecordDAO dao = ImpRecordDAOFactory.getInstance(context);

		List<ImpRecordItem> pmeItemList = new ArrayList<ImpRecordItem>();
		pmeItemList.addAll(convertToImpRecordItem(context, orcid));

		for (ImpRecordItem pmeItem : pmeItemList) {
			if (isAlreadyInImpRecord(alreadyInImpRecord, pmeItem)) {
				continue;
			}
			try {
				if (!forceCollectionId) {
					collectionId = retrieveCollection(collectionId, pmeItem);
				}

				total++;
				String action = "insert";
				DTOImpRecord impRecord = writeImpRecord(context, dao,
						collectionId, pmeItem, action,
						eperson.getID(), status);

				dao.write(impRecord, false);
			} catch (Exception ex) {
				deleted++;
			}
		}

		System.out.println("Imported " + (total - deleted) + " record; "
				+ deleted + " marked as removed");
	}

	private static DTOImpRecord writeImpRecord(Context context,
			ImpRecordDAO dao, UUID collectionId, ImpRecordItem pmeItem,
			String action, UUID epersonId, String status) throws SQLException {
		DTOImpRecord dto = new DTOImpRecord(dao);

		HashMap<String, Set<ImpRecordMetadata>> meta = pmeItem.getMetadata();
		for (String md : meta.keySet()) {
			Set<ImpRecordMetadata> values = meta.get(md);
			String[] splitMd = StringUtils.split(md, "\\.");
			int metadata_order = 0;
			for (ImpRecordMetadata value : values) {
				metadata_order++;
				if (splitMd.length > 2) {
					dto.addMetadata(splitMd[0], splitMd[1], splitMd[2],
							value.getValue(), value.getAuthority(),
							value.getConfidence(), metadata_order, -1);
				} else {
					dto.addMetadata(splitMd[0], splitMd[1], null,
							value.getValue(), value.getAuthority(),
							value.getConfidence(), metadata_order,
							value.getShare());
				}
			}
		}

		dto.setImp_collection_id(collectionId);
		dto.setImp_eperson_id(epersonId);
		dto.setOperation(action);
		dto.setImp_sourceRef(pmeItem.getSourceRef());
		dto.setImp_record_id(pmeItem.getSourceId());
		dto.setStatus(status);
		return dto;

	}

	private static List<ImpRecordItem> convertToImpRecordItem(Context context,
			String orcid) throws HttpException, IOException,
			BadTransformationSpec, MalformedSourceException {

		Map<String, Set<String>> keys = new HashMap<String, Set<String>>();
		Set<String> orcidToSearch = new HashSet<String>();
		orcidToSearch.add(orcid);
		keys.put(SubmissionLookupDataLoader.ORCID, orcidToSearch);

		List<ImpRecordItem> pmeResult = new ArrayList<ImpRecordItem>();
		List<Record> resultDataloader = orcidOnlineDataLoader
				.getByIdentifier(context, keys);
		List<ItemSubmissionLookupDTO> results = new ArrayList<ItemSubmissionLookupDTO>();
		if (resultDataloader != null && !resultDataloader.isEmpty()) {

			TransformationEngine transformationEngine1 = getFeedTransformationEnginePhaseOne();
			if (transformationEngine1 != null) {
				HashMap<String, Set<String>> map = new HashMap<String, Set<String>>();
				HashSet<String> set = new HashSet<String>();
				set.add(orcid);
				map.put(SubmissionLookupDataLoader.ORCID, set);

				MultipleSubmissionLookupDataLoader mdataLoader = (MultipleSubmissionLookupDataLoader) transformationEngine1
						.getDataLoader();
				mdataLoader.setIdentifiers(map);
				SubmissionLookupOutputGenerator outputGenerator = (SubmissionLookupOutputGenerator) transformationEngine1
						.getOutputGenerator();
				outputGenerator
						.setDtoList(new ArrayList<ItemSubmissionLookupDTO>());

				transformationEngine1.transform(new TransformationSpec());
				log.debug("BTE transformation finished!");
				results.addAll(outputGenerator.getDtoList());
			}

			TransformationEngine transformationEngine2 = getFeedTransformationEnginePhaseTwo();
			if (transformationEngine2 != null && results != null) {
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

	private static boolean isAlreadyInImpRecord(List<String> alreadyInImpRecord, ImpRecordItem pmeItem) {
		for (String already : alreadyInImpRecord) {
			if (pmeItem.getSourceId().equals(already)) {
				return true;
			}
		}
		return false;
	}

	private static UUID retrieveCollection(UUID collectionId, ImpRecordItem pmeItem) {
		Set<ImpRecordMetadata> t = pmeItem.getMetadata()
				.get("dc.source.type");

		if (t != null && !t.isEmpty()) {
			String tmpCollectionName = "";
			Iterator<ImpRecordMetadata> iterator = t
					.iterator();
			while (iterator.hasNext()) {
				String tmpCollectionNameTrimmed = iterator
						.next().getValue();
				tmpCollectionName += tmpCollectionNameTrimmed
						.trim();
			}
			collectionId = UUID.fromString(
					new DSpace().getConfigurationService().getProperty(
							"cris.orcid.type." + tmpCollectionName + ".collectionid", collectionId.toString()));
		}
		return collectionId;
	}

	public static OrcidOnlineDataLoader getOrcidOnlineDataLoader() {
		return orcidOnlineDataLoader;
	}

	public static void setOrcidOnlineDataLoader(
			OrcidOnlineDataLoader orcidOnlineDataLoader) {
		OrcidFeed.orcidOnlineDataLoader = orcidOnlineDataLoader;
	}

	public static TransformationEngine getFeedTransformationEnginePhaseOne() {
		return feedTransformationEnginePhaseOne;
	}

	public static void setFeedTransformationEnginePhaseOne(
			TransformationEngine feedTransformationEnginePhaseOne) {
		OrcidFeed.feedTransformationEnginePhaseOne = feedTransformationEnginePhaseOne;
	}

	public static TransformationEngine getFeedTransformationEnginePhaseTwo() {
		return feedTransformationEnginePhaseTwo;
	}

	public static void setFeedTransformationEnginePhaseTwo(
			TransformationEngine feedTransformationEnginePhaseTwo) {
		OrcidFeed.feedTransformationEnginePhaseTwo = feedTransformationEnginePhaseTwo;
	}

	protected static Session getHibernateSession(Context context) throws SQLException {
		return ((Session) context.getDBConnection().getSession());
	}
}