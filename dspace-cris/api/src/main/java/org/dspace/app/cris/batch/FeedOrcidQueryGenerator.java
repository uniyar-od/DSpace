package org.dspace.app.cris.batch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.dspace.discovery.SearchService;
import org.dspace.discovery.SearchServiceException;
import org.dspace.utils.DSpace;

public class FeedOrcidQueryGenerator extends AFeedQueryGenerator {

	Logger log = Logger.getLogger(FeedOrcidQueryGenerator.class);

	@Override
	public HashMap<Integer, List<String>> generate() {
		HashMap<Integer,List<String>> map = new HashMap<Integer,List<String>>();
		SearchService searchService = new DSpace().getSingletonService(SearchService.class);
		SolrQuery solrQuery = new SolrQuery("search.resourcetype:9");
		solrQuery.addFilterQuery("crisrp.orcid:*");
		solrQuery.setRows(Integer.MAX_VALUE);
		try {
			QueryResponse res = searchService.search(solrQuery);
			SolrDocumentList docs = res.getResults();
			Iterator<SolrDocument> iter = docs.iterator();

			List<String> notOwnedOrcidList = new ArrayList<String>();
			while(iter.hasNext()) {
				SolrDocument doc = iter.next();

				String orcid = (String)doc.getFirstValue("crisrp.orcid");
				StringBuffer buffer = new StringBuffer();
				buffer.append("orcid:"+orcid);

				for(String filter : getExtraFilters()) {
					buffer.append(" ");
					buffer.append(filter);
				}

				if(doc.containsKey("owner_i")){
					Integer submitterId = (Integer) doc.getFirstValue("owner_i");
					List<String> list = new ArrayList<String>();
					list.add(buffer.toString());
					map.put(submitterId, list);
				}else {
					notOwnedOrcidList.add(buffer.toString());
				}
			}
			if(notOwnedOrcidList != null && !notOwnedOrcidList.isEmpty()) {
				map.put(getDefaultSubmitter(),notOwnedOrcidList);
			}
		} catch (SearchServiceException e) {
			log.error(e.getMessage(), e);
		}
		return map;
	}
}