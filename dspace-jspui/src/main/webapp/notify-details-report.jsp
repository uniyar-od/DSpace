<%--

    The contents of this file are subject to the license and copyright
    detailed in the LICENSE and NOTICE files at the root of the source
    tree and available online at

    http://www.dspace.org/license/

--%>

<%--
  - Display hierarchical list of communities and collections
  -
  - Attributes to be passed in:
  -    communities         - array of communities
  -    collections.map  - Map where a keys is a community IDs (Integers) and 
  -                      the value is the array of collections in that community
  -    subcommunities.map  - Map where a keys is a community IDs (Integers) and 
  -                      the value is the array of subcommunities in that community
  -    admin_button - Boolean, show admin 'Create Top-Level Community' button
  --%>

<%@page import="java.util.Date"%>
<%@page import="org.dspace.content.DCDate"%>
<%@page import="org.dspace.content.Bitstream"%>
<%@page import="org.apache.commons.lang.StringUtils"%>
<%@ page contentType="text/html;charset=UTF-8"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<%@ page import="org.dspace.app.webui.servlet.admin.EditCommunitiesServlet"%>
<%@ page import="org.dspace.app.webui.util.UIUtil"%>
<%@ page import="org.dspace.browse.ItemCountException"%>
<%@ page import="org.dspace.browse.ItemCounter"%>
<%@ page import="org.dspace.content.Collection"%>
<%@ page import="org.dspace.content.Community"%>
<%@ page import="org.dspace.content.DCDate"%>
<%@ page import="org.dspace.core.ConfigurationManager"%>
<%@ page import="javax.servlet.jsp.jstl.fmt.LocaleSupport"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.sql.SQLException"%>
<%@ page import="java.util.List"%>
<%@ page import="org.dspace.handle.HandleManager"%>
<%@ page import="org.dspace.content.Item"%>
<%@ page import="java.text.SimpleDateFormat"%>
<%@ page import="org.dspace.notify.NotifyStatus"%>
<%@ page import="org.dspace.ldn.LDNUtils"%>
<%@ taglib uri="http://www.dspace.org/dspace-tags.tld" prefix="dspace"%>

<%
	Item[] items = (Item[]) request.getAttribute("list-of-items");

	Integer offset = 0;
	if (request.getParameter("offset") != null)
		offset = Integer.parseInt(request.getParameter("offset"));
	Integer pageSize = (Integer) request.getAttribute("page-size");
	String selectedStatus = request.getParameter("selected_status");
	NotifyStatus status = NotifyStatus.getEnumFromString(selectedStatus);
	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
%>


<script type="text/javascript"
	src="<%=request.getContextPath()%>/notify.js"></script>

<dspace:layout titlekey="jsp.coar-notify.title">


	<dspace:sidebar>

	</dspace:sidebar>

	<h1>
		<fmt:message key="jsp.coar-notify.title" />
	</h1>
	<p>
		<fmt:message key="jsp.coar-notify.text1" />
	</p>

	<ul class="media-list">
		<li class="media well">
			<ul class="media-list">
				<li class="media">
					<div class="media-body">
						<span class="h5 pull-left">
							<div class="h2">
								<fmt:message
									key='<%=(items.length != 0) ? "jsp.coar-notify.items-message" : "jsp.coar-notify.no-items-message"%>'>
									<fmt:param><%=selectedStatus%></fmt:param>
								</fmt:message>
							</div>
						</span>
					</div>
				</li>
				<%
					String itemHandleCanonicalForm;
						Date requestDate;
						String[] metadataValueSplitted;
						for (int i = offset; i < (offset + pageSize) && i < items.length; i++) {
							Item item = items[i];
							metadataValueSplitted = LDNUtils.getNotifyMetadataValueFromStatus(item, status);

							requestDate = format.parse(metadataValueSplitted[0]);
							itemHandleCanonicalForm = HandleManager.getCanonicalForm(item.getHandle());
				%>

				<li class="media well">
					<div class="media-body">
						<span class="h5 pull-left">
							<p>
								<a class="h3" href="<%=itemHandleCanonicalForm%>"><%=item.getName()%></a>
							</p>
							<p class="h4">
								Handle:	<%=item.getHandle()%>
							</p>
						</span> 
						<span class="pull-right">
							<p class="text-right"><b>Request Date</b></p>
							<p class="h4">
								<dspace:date date="<%=new DCDate(requestDate)%>" />
							</p>
						</span>
					</div>
					<div class="media">
						<span class="h4 pull-left">Service: <b><%=ConfigurationManager.getProperty("ldn-coar-notify", "service."+metadataValueSplitted[1]+".name")%></b> </span> 
						
					</div>
				</li>
				<%
					}
				%>
			</ul>
		</li>
	</ul>
	<%
		if (items.length != 0) {
	%>
	<form method="get"
		action="<%=request.getContextPath()%>/notify-details-report"
		id="notify_report_pagination_form">

		<button class="btn btn-success"
			onclick="goToPage('<%=offset%>','<%=pageSize%>','previous')"
			<%=(offset == 0) ? "disabled" : ""%>>
			<i class="h2">&#8592;</i>
		</button>
		<button class="btn btn-success"
			onclick="goToPage('<%=offset%>','<%=pageSize%>','next')"
			<%=(offset + pageSize >= items.length) ? "disabled" : ""%>>
			<i class="h2">&#8594;</i>
		</button>

		<input type="hidden" id="selected_status" name="selected_status"
			value="<%=selectedStatus%>" /> <input type="hidden" id="offset"
			name="offset" value="<%=offset%>" />
	</form>
	<%
		}
	%>
</dspace:layout>
