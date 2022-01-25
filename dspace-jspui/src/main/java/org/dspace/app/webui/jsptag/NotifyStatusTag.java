package org.dspace.app.webui.jsptag;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.dspace.app.webui.util.JSPManager;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.core.I18nUtil;
import org.dspace.eperson.Group;
import org.dspace.handle.factory.HandleServiceFactory;
import org.dspace.ldn.LDNUtils;
import org.dspace.notify.NotifyStatus;
import org.dspace.notify.NotifyStatusManager;

/**
 * @author Stefano Maffei (4Science.it)
 *
 */
public class NotifyStatusTag extends TagSupport {

	private static final long serialVersionUID = 5705577771162255844L;

	private transient String handle;

	private transient String componentTitle;

	public String getHandle() {
		return handle;
	}

	public void setHandle(String handle) {
		this.handle = handle;
	}

	public String getComponentTitle() {
		return componentTitle;
	}

	public void setComponentTitle(String componentTitle) {
		this.componentTitle = componentTitle;
	}

	public int doStartTag() throws JspException {
		Context context = null;
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		DateFormat formatFromString = new SimpleDateFormat(LDNUtils.DATE_PATTERN);
		try {
			context = new Context();

			Item item = (Item) HandleServiceFactory.getInstance().getHandleService().resolveToObject(context, handle);
			JspWriter out = pageContext.getOut();
			List<NotifyStatus> notifyStatuses = NotifyStatusManager.getNotifyStatusForItem(context, item);
			if (notifyStatuses.size() == 0) {
				// do not show the component
				return SKIP_BODY;
			}
			if (componentTitle == null) {
				componentTitle = "coar.notify.default.title-component-tag";
			}
			// Container and table
			out.println("<div class=\"panel panel-info\"><div class=\"panel-heading\">"
					+ I18nUtil.getMessage(componentTitle, context) + "</div>");
			out.println("<table class=\"table panel-body\"><tbody>");

			// heading columns
			out.println("<tr>");
			out.println("<th class=\"standard\">Service</th>");
			out.println("<th class=\"standard\">Status</th>");
			out.println("<th class=\"standard\">Date</th>");
			out.println("<th class=\"standard\">Link</th>");
			out.println("</tr>");

			String[] metadataValues;
			String[] splittedMetadataValues;
			HashMap<String, String> servicesAndNames = LDNUtils.getServicesAndNames();
			for (NotifyStatus status : notifyStatuses) {

				metadataValues = NotifyStatusManager.getMetadataValueFor(status, item);
				for (String metadataValue : metadataValues) {
					splittedMetadataValues = metadataValue.split(Pattern.quote(LDNUtils.METADATA_DELIMITER));

					// adding a row for each element
					out.println("<tr>");
					out.println("<td class=\"standard\">" + servicesAndNames.get(splittedMetadataValues[1]) + "</td>");
					out.println("<td class=\"standard\">" + status + "</td>");
					out.println("<td class=\"standard\">"
							+ format.format(formatFromString.parse(splittedMetadataValues[0])) + "</td>");
					String linkIfAvailable = (splittedMetadataValues.length > 3)
							? "<a href=\"" + splittedMetadataValues[3] + "\">" + splittedMetadataValues[3] + "</a>"
							: "N/A";
					out.println("<td class=\"standard\">" + linkIfAvailable + "</td>");
					out.println("</tr>");
				}
			}

			out.println("</tbody></table></div>");
		} catch (Exception ie) {
			throw new JspException(ie);
		} 

		return SKIP_BODY;
	}

}
