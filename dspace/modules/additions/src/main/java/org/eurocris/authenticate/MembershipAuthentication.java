/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.eurocris.authenticate;

import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.dspace.app.cris.model.ResearcherPage;
import org.dspace.app.cris.model.jdyna.RPProperty;
import org.dspace.app.cris.service.ApplicationService;
import org.dspace.authenticate.AuthenticationMethod;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.dspace.discovery.SearchService;
import org.dspace.discovery.SearchServiceException;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;
import org.dspace.utils.DSpace;

/**
 * Adds users to special groups if they are euroCRIS members
 * 
 * @version $Revision$
 * @author Andrea Bollini
 */
public class MembershipAuthentication implements AuthenticationMethod
{
    /** Our logger */
    private static Logger log = Logger.getLogger(MembershipAuthentication.class);

    private String membershipGroupName = ConfigurationManager.getProperty("authentication", "MembershipAuthentication.specialGroupName");

    private SearchService searchService = new DSpace().getSingletonService(SearchService.class);
    
    private ApplicationService applicationService = new DSpace()
            .getServiceManager()
            .getServiceByName("applicationService", ApplicationService.class);
    
    public MembershipAuthentication()
    {
    }

    public boolean canSelfRegister(Context context, HttpServletRequest request,
            String username) throws SQLException
    {
        return false;
    }

    public void initEPerson(Context context, HttpServletRequest request,
            EPerson eperson) throws SQLException
    {
    }

    public boolean allowSetPassword(Context context,
            HttpServletRequest request, String username) throws SQLException
    {
        return false;
    }

    public boolean isImplicit()
    {
        return true;
    }

    public int[] getSpecialGroups(Context context, HttpServletRequest request)
            throws SQLException
    {
    	String crisID = context.getCrisID();
		if (request == null || crisID == null)
        {
            return new int[0];
        }
        
        Group group = Group.findByName(context, membershipGroupName);
        if (group != null)
        {
        	ResearcherPage rp = applicationService.getEntityByCrisId(crisID, ResearcherPage.class);
        	List<RPProperty> scope = rp.getAnagrafica4view().get("eurocrisScope");
        	if (scope!= null && scope.size() > 0) {
        		return new int[]{group.getID()};
        	}
        	
        	SolrQuery solrQuery = new SolrQuery("principalcontact:"+crisID+" OR institutionalmembers:"+crisID);
        	solrQuery.addFilterQuery("eurocrisScope:[* TO *]");
        	try {
				if (searchService.search(solrQuery).getResults().getNumFound() > 0) {
					return new int[]{group.getID()};
				}
			} catch (SearchServiceException e) {
				log.error(LogManager.getHeader(context, "getSpecialGroup", e.getMessage()), e);
			}
        }
        else
        {
            log.warn(LogManager.getHeader(context,
                    "configuration_error", "unknown_group="
                            + membershipGroupName));
        }

        return new int[0];
    }

    public int authenticate(Context context, String username, String password,
            String realm, HttpServletRequest request) throws SQLException
    {
        return BAD_ARGS;
    }

    public String loginPageURL(Context context, HttpServletRequest request,
            HttpServletResponse response)
    {
        return null;
    }

    public String loginPageTitle(Context context)
    {
        return null;
    }
}
