/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.content.authority;

/**
 *
 * @author Stefano Maffei 4Science.com
 */
public class PersonItemAuthorityCoarseMatchService extends PersonItemAuthorityService {

    @Override
    public String getSolrQueryExactMatch(String searchTerm) {
        return generateSearchQueryCoarseBestMatch(searchTerm, true);
    }

}