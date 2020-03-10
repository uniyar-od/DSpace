/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.webui.servlet;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.dspace.app.webui.util.JSPManager;
import org.dspace.app.webui.util.UIUtil;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.content.Item;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.util.VersionUtil;

/**
 * Servlet to handling the versioning of the item
 * 
 * @author Pascarelli Luigi Andrea
 * @version $Revision$
 */
public class VersionItemServlet extends DSpaceServlet
{

    /** log4j category */
    private static Logger log = Logger.getLogger(VersionItemServlet.class);


    protected void doDSGet(Context context, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException,
            AuthorizeException
    {
        context.turnOffItemWrapper();
    	Integer itemID = UIUtil.getIntParameter(request,"itemID");
        Item item = Item.find(context,itemID);
        String itemTitle = item.getName();
        String submit = UIUtil.getSubmitButton(request,"submit");
        if (submit!=null && submit.equals("submit")){
            if (AuthorizeManager.authorizeActionBoolean(context, item,
                    Constants.WRITE) || item.canEdit() || item.isOriginalSubmitter(context) || item.isAuthor(context))
            {
                request.setAttribute("itemID", itemID);
                request.setAttribute("itemTitle", itemTitle);
                JSPManager.showJSP(request, response,
                        "/tools/version-summary.jsp");
            }
            else {
                response.sendRedirect(request.getContextPath() + "/handle/" + item.getHandle() + "?nosubmitversion=true");
            }
            return;
        }
        
        String summary = request.getParameter("summary");
        if (submit!=null && submit.equals("submit_version")){                        
            Integer wsid = VersionUtil.processCreateNewVersion(context, itemID, summary);
            if(wsid==null) {
                response.sendRedirect(request.getContextPath() + "/handle/" + item.getHandle() + "?nosubmitversion=true");
            }
            else {
                response.sendRedirect(request.getContextPath()+"/submit?resume=" + wsid);
            }
            context.complete();
            return;
        }
        else if (submit!=null && submit.equals("submit_update_version")){
            String versionID = request.getParameter("versionID");
            request.setAttribute("itemID", itemID);
            request.setAttribute("versionID", versionID);
            request.setAttribute("itemTitle", itemTitle);
            JSPManager.showJSP(request, response,
                    "/tools/version-update-summary.jsp");
            return;
        }
        
        //Send us back to the item page if we cancel !
        response.sendRedirect(request.getContextPath() + "/handle/" + item.getHandle());
        context.complete();
        
    }

   
    protected void doDSPost(Context context, HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException,
            SQLException, AuthorizeException
    {
        // If this is not overridden, we invoke the raw HttpServlet "doGet" to
        // indicate that POST is not supported by this servlet.
        doDSGet(UIUtil.obtainContext(request), request, response);
    }

}
