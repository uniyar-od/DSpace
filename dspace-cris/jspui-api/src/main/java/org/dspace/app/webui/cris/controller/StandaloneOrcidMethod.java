/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.webui.cris.controller;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.dspace.app.cris.discovery.CrisSearchService;
import org.dspace.app.cris.model.CrisConstants;
import org.dspace.app.cris.model.ResearcherPage;
import org.dspace.app.cris.service.ApplicationService;
import org.dspace.authenticate.AuthenticationMethod;
import org.dspace.core.Context;
import org.dspace.discovery.SearchService;
import org.dspace.discovery.SearchServiceException;
import org.dspace.eperson.EPerson;

/**
 *
 * 
 */
public class StandaloneOrcidMethod {

    /** log4j category */
    private static Logger log = Logger.getLogger(StandaloneOrcidMethod.class);

    private ApplicationService applicationService;
    private CrisSearchService searchService;
    
    public int connect(Context context, String username, String password, String realm, HttpServletRequest request) throws SQLException, SearchServiceException {

        EPerson currUser = context.getCurrentUser();
        String orcid = (String) request.getAttribute("orcid");
        if(StringUtils.isNotBlank(orcid)){
            log.warn("No orcid found");
            return AuthenticationMethod.NO_SUCH_USER;
        }
        String token = (String) request.getAttribute("access_token");
        String scope = (String) request.getAttribute("scope");        
        EPerson[] epersons = EPerson.search(context, orcid);
        
        if(epersons != null && epersons.length > 1) {                    
            log.warn("Fail to connect user with orcid: " +orcid+ " - Multiple Users found");
        } else {
            EPerson sEperson = epersons[0];
            if(currUser.getID()==sEperson.getID()) {
                log.warn("Another user have the orcid: " + sEperson.getEmail() + "(ID:" + sEperson.getID() + ")");                        
            } else {
                SearchService searcher = getSearchService();
                SolrQuery query = new SolrQuery();
                query.setQuery("search.resourcetype:" + CrisConstants.RP_TYPE_ID);
                String filterQuery = "cris-sourceref:" + orcid;
                if(StringUtils.isNotBlank(orcid)){
                    filterQuery += (StringUtils.isNotBlank(filterQuery)?" OR ":"")+"crisrp.orcid:\""+orcid+"\" OR crisrp.orcid_private:\""+orcid+"\"";
                }
                query.addFilterQuery(filterQuery);
                QueryResponse qResp = searcher.search(query);
                SolrDocumentList docList = qResp.getResults();
                if(docList.size()>=2){
                    log.warn("Found two or more rp please contact administrator");                            
                }else if(docList.size()==1){
                    SolrDocument doc = docList.get(0);
                    String rpKey = (String)doc.getFirstValue("objectpeople_authority");
                    ResearcherPage rp = getApplicationService().getResearcherByAuthorityKey(rpKey);
                    if(rp!=null){
                        if(rp.getEpersonID()!=null) {
                            if (rp.getEpersonID() != currUser.getID())
                            {
                                log.warn("Fail to connect user, the Reseacher Profile " + rp.getCrisID() + " have orcid: " +orcid+ " - Please Contact Administrator");
                            }
                        }
                    }
                }
            }
        }
        return AuthenticationMethod.SUCCESS;
    }

    public ApplicationService getApplicationService()
    {
        return applicationService;
    }

    public void setApplicationService(ApplicationService applicationService)
    {
        this.applicationService = applicationService;
    }

    public CrisSearchService getSearchService()
    {
        return searchService;
    }

    public void setSearchService(CrisSearchService searchService)
    {
        this.searchService = searchService;
    }
}
