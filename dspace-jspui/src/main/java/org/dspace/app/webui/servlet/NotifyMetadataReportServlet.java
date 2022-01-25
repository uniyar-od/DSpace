package org.dspace.app.webui.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.dspace.app.webui.util.JSPManager;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.eperson.Group;
import org.dspace.notify.NotifyStatus;
import org.dspace.notify.NotifyStatusManager;

public class NotifyMetadataReportServlet extends DSpaceServlet {
	/** log4j category */
	private static Logger log = Logger.getLogger(NotifyMetadataReportServlet.class);

	protected void doDSGet(Context context, HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, SQLException, AuthorizeException {

		HashMap<NotifyStatus, List<Item>> notifyItemsReport = NotifyStatusManager.getItemsForEachNotifyStatus(context);
		request.setAttribute("coar-notify-items-report", notifyItemsReport);

		// is the user a member of the Administrator (1) group
		boolean admin = Group.isMember(context, Group.ADMIN_ID);

		if (admin) {
			JSPManager.showJSP(request, response, "/notify-metadata-report.jsp");
		} else {
			throw new AuthorizeException();
		}

	}

}
