/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.authority.service;

import static org.apache.solr.client.solrj.util.ClientUtils.escapeQueryChars;
import static org.dspace.discovery.SolrServiceBestMatchIndexingPlugin.PUNCT_CHARS_REGEX;
import static org.dspace.discovery.SolrServiceStrictBestMatchIndexingPlugin.cleanNameWithStrictPolicies;

import org.apache.commons.lang3.StringUtils;
import org.dspace.discovery.SolrServiceBestMatchIndexingPlugin;
import org.dspace.discovery.SolrServiceStrictBestMatchIndexingPlugin;

/**
 *
 * @author Giuseppe Digilio (giuseppe dot digilio at 4science dot it)
 */
public interface ItemAuthorityService {

    /**
     * Get solr query
     * @param searchTerm The search term string
     * @return solr query
     */
    public String getSolrQuery(String searchTerm);

    /**
     * Get solr query for best match
     * @param  searchTerm        The search term string
     * @param  isSkipPunctuation if punctuation must be removed from searchTerm
     * @return                   solr query
     */
    public default String generateSearchQueryCoarseBestMatch(String searchTerm,
        boolean isSkipPunctuation) {
        searchTerm = StringUtils.normalizeSpace(searchTerm.replaceAll(PUNCT_CHARS_REGEX, " "));
        return SolrServiceBestMatchIndexingPlugin.BEST_MATCH_INDEX + ":" + escapeQueryChars(searchTerm);
    }

    /**
     * Get solr query for best match
     * @param  searchTerm The search term string
     * @return            solr query
     */
    public default String generateSearchQueryStrictBestMatch(String searchTerm) {
        return SolrServiceStrictBestMatchIndexingPlugin.BEST_MATCH_INDEX + ":"
            + escapeQueryChars(cleanNameWithStrictPolicies(searchTerm));
    }

}