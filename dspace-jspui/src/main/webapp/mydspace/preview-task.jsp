<%--

    The contents of this file are subject to the license and copyright
    detailed in the LICENSE and NOTICE files at the root of the source
    tree and available online at

    http://www.dspace.org/license/

--%>
<%--
  - Preview task page
  -
  -   workflow.item:  The workflow item for the task they're performing
  --%>

<%@ page contentType="text/html;charset=UTF-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"
    prefix="fmt" %>
    
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ taglib uri="http://www.dspace.org/dspace-tags.tld" prefix="dspace" %>

<%@ page import="org.dspace.app.webui.servlet.MyDSpaceServlet" %>
<%@ page import="org.dspace.content.Collection" %>
<%@ page import="org.dspace.content.Item" %>
<%@ page import="org.dspace.eperson.EPerson" %>
<%@ page import="org.dspace.workflow.WorkflowItem" %>
<%@ page import="org.dspace.workflow.WorkflowManager" %>
<%@ page import="org.dspace.content.MetadataField" %>
<%@ page import="org.dspace.content.MetadataSchema" %>
<%@ page import="org.dspace.content.Metadatum"%>
<%@ page import="org.dspace.core.Context" %>
<%@ page import="org.dspace.authorize.ResourcePolicy"%>
<%@ page import="org.dspace.authorize.AuthorizeManager"%>
<%@ page import="org.dspace.app.webui.util.UIUtil" %>
<%@ page import="org.dspace.app.webui.util.BitstreamDifferencesDTO" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>

<%
	// Obtain DSpace context
	Context context = UIUtil.obtainContext(request);    
	
	WorkflowItem workflowItem =
	    (WorkflowItem) request.getAttribute("workflow.item");
	
	Item previousItem = (Item) request
	    .getAttribute("previous.item");

	String versionMessage = (String) request
            .getAttribute("version.message");

	List<MetadataField> modifiedMetadata  = (List<MetadataField>) request
	    .getAttribute("modifiedMetadata");
	
	Map<String, BitstreamDifferencesDTO> modifiedFiles  = (Map<String, BitstreamDifferencesDTO>) request
	        .getAttribute("modifiedFiles");

    Collection collection = workflowItem.getCollection();
    Item item = workflowItem.getItem();
%>

<dspace:layout style="submission"
			   locbar="link"
               parentlink="/mydspace"
               parenttitlekey="jsp.mydspace"
               titlekey="jsp.mydspace.preview-task.title"
               nocache="true">

	<h1><fmt:message key="jsp.mydspace.preview-task.title"/></h1>
    
<%
    if (workflowItem.getState() == WorkflowManager.WFSTATE_STEP1POOL)
    {
%>
	<p><fmt:message key="jsp.mydspace.preview-task.text1"> 
        <fmt:param><%= collection.getMetadata("name") %></fmt:param>
    </fmt:message></p>
<%
    }
    else if(workflowItem.getState() == WorkflowManager.WFSTATE_STEP2POOL)
    {
%>    
	<p><fmt:message key="jsp.mydspace.preview-task.text3"> 
        <fmt:param><%= collection.getMetadata("name") %></fmt:param>
    </fmt:message></p>
<%
    }
    else if(workflowItem.getState() == WorkflowManager.WFSTATE_STEP3POOL)
    {
%>
	<p><fmt:message key="jsp.mydspace.preview-task.text4"> 
        <fmt:param><%= collection.getMetadata("name") %></fmt:param>
    </fmt:message></p>
<%
    }
%>
    <%@ include file="version-differences-component.jsp" %>
    
    <dspace:item item="<%= item %>" />

    <form action="<%= request.getContextPath() %>/mydspace" method="post">
        <input type="hidden" name="workflow_id" value="<%= workflowItem.getID() %>"/>
        <input type="hidden" name="step" value="<%= MyDSpaceServlet.PREVIEW_TASK_PAGE %>"/>
		<input class="btn btn-default col-md-2" type="submit" name="submit_cancel" value="<fmt:message key="jsp.mydspace.general.cancel"/>" />
		<input class="btn btn-primary col-md-2 pull-right" type="submit" name="submit_start" value="<fmt:message key="jsp.mydspace.preview-task.accept.button"/>" />
    </form>
</dspace:layout>
