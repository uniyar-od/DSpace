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
import org.dspace.services.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
/**
 * 
 * @author Stefano Maffei 4Science.com
 *
 */
public class ItemAuthorityServiceImpl implements ItemAuthorityService {

    @Autowired
    protected ConfigurationService configurationService;

    /**
     * Get the solr query to be executed
     * Priority is given to the itemauthoritylookup field which contains exact names
     * The best match term is lower priority since it generates permutations of the names
     * @param searchTerm the term to be searched
     * @return solr query to be executed
     */
    @Override
    public String getSolrQueryExactMatch(String searchTerm) {
        return getSolrQuery(searchTerm);
    }

    @Override
    public String getSolrQuery(String searchTerm) {
        String luceneQuery = ClientUtils.escapeQueryChars(searchTerm.toLowerCase()) + "*";
        String solrQuery = null;
        luceneQuery = luceneQuery.replaceAll("\\\\ "," ");
        String subLuceneQuery = luceneQuery.substring(0,
                luceneQuery.length() - 1);
        solrQuery = "{!lucene q.op=AND df=itemauthoritylookup}("
            + luceneQuery
            + ")^100 OR (\""
            + subLuceneQuery + "\")^100 OR "
            + "(" + generateSearchQueryCoarseBestMatch(searchTerm, true) + ")^10 ";

        return solrQuery;
    }

    /**
     * Get solr query for best match
     * @param  searchTerm        The search term string
     * @param  isSkipPunctuation if punctuation must be removed from searchTerm
     * @return                   solr query
     */
    public String generateSearchQueryCoarseBestMatch(String searchTerm,
        boolean isSkipPunctuation) {
        searchTerm = StringUtils.normalizeSpace(searchTerm.replaceAll(PUNCT_CHARS_REGEX, " "));
        return SolrServiceBestMatchIndexingPlugin.BEST_MATCH_INDEX + ":" + escapeQueryChars(searchTerm);
    }

    /**
     * Get solr query for best match
     * @param  searchTerm The search term string
     * @return            solr query
     */
    public String generateSearchQueryStrictBestMatch(String searchTerm) {
        return SolrServiceStrictBestMatchIndexingPlugin.BEST_MATCH_INDEX + ":"
            + escapeQueryChars(cleanNameWithStrictPolicies(searchTerm));
    }

    @Override
    public int getConfidenceForChoices(Choice... choices) {
        if (choices.length == 0) {
            return Choices.CF_UNSET;
        }
        if (choices.length == 1) {
            return Choices.CF_UNCERTAIN;
        }
        return Choices.CF_AMBIGUOUS;
    }

}