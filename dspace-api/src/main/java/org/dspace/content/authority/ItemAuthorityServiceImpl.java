/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.content.authority;

import org.apache.solr.client.solrj.util.ClientUtils;
import org.dspace.content.authority.service.ItemAuthorityService;

public class ItemAuthorityServiceImpl implements ItemAuthorityService {

    @Override
    public String getSolrQuery(String searchTerm) {
        String solrQuery = "{!lucene q.op=AND df=itemauthoritylookup}";
        if (searchTerm != null) {
            String luceneQuery = ClientUtils.escapeQueryChars(searchTerm.toLowerCase()) + "*";
            
            luceneQuery = luceneQuery.replaceAll("\\\\ "," ");
            String subLuceneQuery = luceneQuery.substring(0,
                    luceneQuery.length() - 1);
            solrQuery = solrQuery + "("
                            + luceneQuery
                            + ") OR (\""
                            + subLuceneQuery + "\")^2 OR "
                            + "(itemauthoritylookupexactmatch:\"" + subLuceneQuery + "\")^10 ";
        } else {
            // if searchTerm is null search for all the authority entries
            solrQuery = solrQuery + "*";
        }

        return solrQuery;
    }

}