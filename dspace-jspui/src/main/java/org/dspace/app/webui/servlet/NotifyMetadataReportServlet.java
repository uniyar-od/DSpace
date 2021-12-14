package org.dspace.app.webui.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.dspace.app.webui.util.JSPManager;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.ResourcePolicy;
import org.dspace.core.Context;
import org.dspace.storage.rdbms.DatabaseManager;
import org.dspace.storage.rdbms.TableRow;
import org.dspace.storage.rdbms.TableRowIterator;

public class NotifyMetadataReportServlet extends DSpaceServlet {
	/** log4j category */
	private static Logger log = Logger.getLogger(NotifyMetadataReportServlet.class);
	private static final String SQL_RETRIEVE_NOTIFY_METADATA = "SELECT metadatafieldregistry.metadata_schema_id, metadatafieldregistry.element, metadatafieldregistry.qualifier as qualif, count(metadatavalue.text_value) as occurrences\n"
			+ "	FROM public.metadatafieldregistry LEFT JOIN public.metadatavalue ON metadatavalue.metadata_field_id = metadatafieldregistry.metadata_field_id\n"
			+ "	JOIN public.metadataschemaregistry ON metadataschemaregistry.metadata_schema_id = metadatafieldregistry.metadata_schema_id\n"
			+ "	where metadataschemaregistry.short_id = 'coar' and element = 'notify'\n"
			+ "	GROUP BY metadatafieldregistry.metadata_schema_id, metadatafieldregistry.element, metadatafieldregistry.qualifier";

	protected void doDSGet(Context context, HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, SQLException, AuthorizeException {

		HashMap<String, Integer> metadata = retrieveNotifyMetadata(context);

		request.setAttribute("coar-notify-metadata", metadata);

		JSPManager.showJSP(request, response, "/notify-metadata-report.jsp");

	}

	@SuppressWarnings("deprecation")
	private HashMap<String, Integer> retrieveNotifyMetadata(Context context) {
		HashMap<String, Integer> metadata = new HashMap<>();
		TableRowIterator tableRowIterator = null;
		try {
			tableRowIterator = DatabaseManager.query(context, SQL_RETRIEVE_NOTIFY_METADATA);
			String tmpMetadata;
			int occurrences;
			while (tableRowIterator.hasNext()) {
				TableRow row = tableRowIterator.next();

				tmpMetadata = "coar.notify." + row.getStringColumn("qualif");
				occurrences = row.getIntColumn("occurrences");
				metadata.put(tmpMetadata, occurrences);

			}
		} catch (SQLException e) {
			log.error(e);
		} finally {
			if (tableRowIterator != null) {
				tableRowIterator.close();
			}
		}

		return metadata;
	}

}
