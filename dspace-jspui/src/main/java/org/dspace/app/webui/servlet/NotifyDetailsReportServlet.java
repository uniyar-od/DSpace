package org.dspace.app.webui.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.dspace.app.webui.util.JSPManager;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.factory.AuthorizeServiceFactory;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.notify.NotifyStatus;
import org.dspace.notify.NotifyStatusManager;
import org.dspace.services.factory.DSpaceServicesFactory;

public class NotifyDetailsReportServlet extends DSpaceServlet {

	/** log4j category */
	private static Logger log = Logger.getLogger(NotifyDetailsReportServlet.class);

	private static final int PAGE_SIZE = DSpaceServicesFactory.getInstance().getConfigurationService().getIntProperty("notify.status.details-page.page-size");

	@Override
	protected void doDSGet(Context context, HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, SQLException, AuthorizeException {

		String selectedStatus = request.getParameter("selected_status");
		
		List<Item> itemsList = NotifyStatusManager.getItemListForStatus(context, NotifyStatus.getEnumFromString(selectedStatus));
		Item[] items = new Item[itemsList.size()];
		items = itemsList.toArray(items);
		Integer offset = null;
		try {
			offset = Integer.parseInt(request.getParameter("offset"));
			request.setAttribute("offset", offset);
		} catch (Exception e) {
		}
		if (offset == null || offset < 0 || offset >= items.length) {
			log.info("Starting with offset: 0");
			request.setAttribute("offset", 0);
		}
		// is the user a member of the Administrator (1) group
		boolean admin = AuthorizeServiceFactory.getInstance().getAuthorizeService().isAdmin(context);

		if (admin) {
			request.setAttribute("list-of-items", items);
			request.setAttribute("page-size", PAGE_SIZE);
			request.setAttribute("selected_status", selectedStatus);

			JSPManager.showJSP(request, response, "/notify-details-report.jsp");
		} else {
			throw new AuthorizeException();
		}
		
	}

}
