package org.dspace.app.webui.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.dspace.app.webui.util.JSPManager;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.storage.rdbms.DatabaseManager;
import org.dspace.storage.rdbms.TableRow;
import org.dspace.storage.rdbms.TableRowIterator;

public class NotifyDetailsReportServlet extends DSpaceServlet {

	/** log4j category */
	private static Logger log = Logger.getLogger(NotifyDetailsReportServlet.class);
	public static final String SELECT_ITEM_FOR_EACH_STATUS = "SELECT metadatafieldregistry.qualifier, metadatavalue.text_value, handle.handle ,item.item_id, item.last_modified\n"
			+ "	FROM public.metadatafieldregistry JOIN public.metadatavalue ON metadatavalue.metadata_field_id = metadatafieldregistry.metadata_field_id\n"
			+ "	JOIN public.metadataschemaregistry ON metadataschemaregistry.metadata_schema_id = metadatafieldregistry.metadata_schema_id\n"
			+ "	JOIN public.item ON metadatavalue.resource_id = item.item_id\n"
			+ "	JOIN handle ON handle.resource_id = item.item_id\n"
			+ "	where metadataschemaregistry.short_id = 'coar' and element = 'notify' and metadatafieldregistry.qualifier = ?\n"
			+ "	ORDER BY item.last_modified DESC\n" + "";

	@Override
	protected void doDSGet(Context context, HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, SQLException, AuthorizeException {

		String selectedMetadata = request.getParameter("selected_metadata");
		String qualifier = selectedMetadata.replace("coar.notify.", "");
		List<Item> items = retrieveItems(context, qualifier);


		request.setAttribute("list-of-items", items);
		
		JSPManager.showJSP(request, response, "/notify-details-report.jsp");
	}

	@SuppressWarnings("deprecation")
	private List<Item> retrieveItems(Context context, String qualifier) {
		List<Item> items = new LinkedList();
		TableRowIterator tableRowIterator = null;
		try {
			tableRowIterator = DatabaseManager.query(context, SELECT_ITEM_FOR_EACH_STATUS, qualifier);

			Integer id;
			while (tableRowIterator.hasNext()) {
				TableRow row = tableRowIterator.next();

				id = row.getIntColumn("item_id");
				
				items.add(Item.find(context, id));

			}
		} catch (SQLException e) {
			log.error(e);
		} finally {
			if (tableRowIterator != null) {
				tableRowIterator.close();
			}
		}

		return items;
	}
}
