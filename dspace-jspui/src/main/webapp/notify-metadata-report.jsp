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

<%@page import="org.dspace.content.Bitstream"%>
<%@page import="org.apache.commons.lang.StringUtils"%>
<%@ page contentType="text/html;charset=UTF-8"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<%@ page
	import="org.dspace.app.webui.servlet.admin.EditCommunitiesServlet"%>
<%@ page import="org.dspace.app.webui.util.UIUtil"%>
<%@ page import="org.dspace.browse.ItemCountException"%>
<%@ page import="org.dspace.browse.ItemCounter"%>
<%@ page import="org.dspace.content.Collection"%>
<%@ page import="org.dspace.content.Community"%>
<%@ page import="org.dspace.core.ConfigurationManager"%>
<%@ page import="javax.servlet.jsp.jstl.fmt.LocaleSupport"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.sql.SQLException"%>
<%@ page import="java.util.HashMap"%>
<%@ page import="java.util.Map.Entry"%>

<%@ taglib uri="http://www.dspace.org/dspace-tags.tld" prefix="dspace"%>

<%
	HashMap<String, Integer> metadata = (HashMap<String, Integer>) request.getAttribute("coar-notify-metadata");
%>

<link rel="stylesheet" href="<%= request.getContextPath() %>/notify-custom.css" type="text/css">
	<script type="text/javascript" src="<%= request.getContextPath() %>/notify.js"></script>

<dspace:layout titlekey="jsp.coar-notify.title">


	<dspace:sidebar>

	</dspace:sidebar>

	<h1>
		<fmt:message key="jsp.coar-notify.title" />
	</h1>
	<p>
		<fmt:message key="jsp.coar-notify.text1" />
	</p>

	<form method="get" action="<%= request.getContextPath() %>/notify-details-report" id="choose_metadata_for_details">
		<ul class="media-list">
			<li class="media well">
				<ul class="media-list">
					<li class="media well">
						<div class="media-body">
							<span class="h5 pull-left">
								<div class="h2">Status</div>
							</span> <span class="h2 pull-right"><p>Occurences</p></span>
						</div>
					</li>
					<%
						for (Entry<String, Integer> entry : metadata.entrySet()) {
								String key = entry.getKey();
								Integer value = entry.getValue();
					%>
					<li class="media well notifyListButton" onclick="selectMetadataAndSubmit('<%=key%>');">
						<div class="media-body">
							<span class="h5 pull-left">
								<div class="h3"><%=key.replace("coar.notify.", "")%></div>
								<div class="h5"><%=key%></div>
							</span> <span class="h3 pull-right"><p><%=value%></p></span>
						</div>
					</li>
					<%
						}
					%>
				</ul>
			</li>
		</ul>
		<input type="hidden" id="selected_metadata" name="selected_metadata"/>
	</form>
</dspace:layout>
