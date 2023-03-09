/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.content.authority;

import static org.apache.solr.client.solrj.util.ClientUtils.escapeQueryChars;
import static org.dspace.discovery.SolrServiceBestMatchIndexingPlugin.PUNCT_CHARS_REGEX;
import static org.dspace.discovery.SolrServiceStrictBestMatchIndexingPlugin.cleanNameWithStrictPolicies;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.dspace.content.authority.service.ItemAuthorityService;
import org.dspace.discovery.SolrServiceBestMatchIndexingPlugin;
import org.dspace.discovery.SolrServiceStrictBestMatchIndexingPlugin;

public class ItemAuthorityServiceImpl implements ItemAuthorityService {

    @Override
    public String getSolrQuery(String searchTerm) {
        String luceneQuery = ClientUtils.escapeQueryChars(searchTerm.toLowerCase()) + "*";
        String solrQuery = null;
        luceneQuery = luceneQuery.replaceAll("\\\\ "," ");
        String subLuceneQuery = luceneQuery.substring(0,
                luceneQuery.length() - 1);
        solrQuery = "{!lucene q.op=AND df=itemauthoritylookup}("
                        + luceneQuery
                        + ") OR (\""
                        + subLuceneQuery + "\")^2 OR "
                        + "(itemauthoritylookupexactmatch:\"" + subLuceneQuery + "\")^10 ";

        return solrQuery;
    }

    @Override
    public String generateSearchQueryCoarseBestMatch(String searchTerm,
        boolean isSkipPunctuation) {
        searchTerm = StringUtils.normalizeSpace(searchTerm.replaceAll(PUNCT_CHARS_REGEX, " "));
        return SolrServiceBestMatchIndexingPlugin.BEST_MATCH_INDEX + ":" + escapeQueryChars(searchTerm);
    }

    @Override
    public String generateSearchQueryStrictBestMatch(String searchTerm) {
        return SolrServiceStrictBestMatchIndexingPlugin.BEST_MATCH_INDEX + ":"
            + escapeQueryChars(cleanNameWithStrictPolicies(searchTerm));
    }

}