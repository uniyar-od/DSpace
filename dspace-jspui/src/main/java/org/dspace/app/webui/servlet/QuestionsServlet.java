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

import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.dspace.app.webui.util.UIUtil;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Item;
import org.dspace.content.Metadatum;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.core.Email;
import org.dspace.core.I18nUtil;
import org.dspace.core.LogManager;
import org.dspace.utils.DSpace;

/**
 * Servlet to send question from display item
 *
 * A contribution of uni-bamberg
 *
 * @author Pascarelli Luigi Andrea
 */
public class QuestionsServlet extends DSpaceServlet
{
    /** Logger */
    private static Logger log = Logger.getLogger(QuestionsServlet.class);


    DSpace dspace = new DSpace();

    @Override
    protected void doDSGet(Context context, HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException,
            SQLException, AuthorizeException
    {

        doDSPost(context, request, response);
    }

    protected void doDSPost(Context context, HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException,
            SQLException, AuthorizeException
    {
        int item_id = UIUtil.getIntParameter(request, "item_id");
        Item item = Item.find(context, item_id);

        String sTitle = item.getMetadata("dc.title");
        String sDate = item.getMetadata("dc.date.issued");
        String sLink = item.getMetadata("dc.identifier.uri");

        String sAuthor = "";
        for (Metadatum ma : item.getMetadata("dc", "contributor", "author", Item.ANY))
        {
          sAuthor = ("".equals(sAuthor) ? "" : sAuthor + " ; ") + ma.value;
        }

        try
        {
            Email email = Email.getEmail(I18nUtil.getEmailFilename(context.getCurrentLocale(), "publication_question"));
            email.addRecipient(ConfigurationManager.getProperty("mail.publication-question.mailto"));
            // Eventually you need to change the order of the arguments when editing the template
            email.addArgument(sTitle); // Title
            email.addArgument(sDate); // issued
            email.addArgument(sAuthor); // Authors
            email.addArgument(sLink); // Link
            email.addArgument(request.getParameter("q")); // Question
            email.addArgument(request.getParameter("name")); // Name
            email.addArgument(request.getParameter("mail")); // E-MAil
            email.setReplyTo(request.getParameter("mail"));

            email.send();

            response.setStatus(HttpStatus.SC_NO_CONTENT);
        }
        catch (Exception e)
        {
            log.warn(LogManager.getHeader(context, "emailSuccessMessage",
                    "cannot notify user of export"), e);
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
