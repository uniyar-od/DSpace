<%--

    The contents of this file are subject to the license and copyright
    detailed in the LICENSE and NOTICE files at the root of the source
    tree and available online at

    http://www.dspace.org/license/

--%>
<%--
  - Perform task page
  -
  - Attributes:
  -    workflow.item: The workflow item for the task being performed
  --%>

<%@ page contentType="text/html;charset=UTF-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"
    prefix="fmt" %> 
    
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ taglib uri="http://www.dspace.org/dspace-tags.tld" prefix="dspace" %>

<%@ page import="org.dspace.app.webui.servlet.MyDSpaceServlet" %>
<%@ page import="org.dspace.content.Collection" %>
<%@ page import="org.dspace.content.Item" %>
<%@ page import="org.dspace.content.MetadataField"%>
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

<dspace:layout style="submission" locbar="link"
               parentlink="/mydspace"
               parenttitlekey="jsp.mydspace"
               titlekey="jsp.mydspace.perform-task.title"
               nocache="true">

    <%-- <h1>Perform Task</h1> --%>
    <h1><fmt:message key="jsp.mydspace.perform-task.title"/></h1>
    
<%
    if (workflowItem.getState() == WorkflowManager.WFSTATE_STEP1)
    {
%>
	<p><fmt:message key="jsp.mydspace.perform-task.text1">
        <fmt:param><%= collection.getMetadata("name") %></fmt:param>
         </fmt:message></p>
<%
    }
    else if (workflowItem.getState() == WorkflowManager.WFSTATE_STEP2)
    {
%>
	<p><fmt:message key="jsp.mydspace.perform-task.text3">
        <fmt:param><%= collection.getMetadata("name") %></fmt:param>
	</fmt:message></p>
<%
    }
    else if (workflowItem.getState() == WorkflowManager.WFSTATE_STEP3)
    {
%>
	<p><fmt:message key="jsp.mydspace.perform-task.text4">
        <fmt:param><%= collection.getMetadata("name") %></fmt:param>
    </fmt:message></p>
<%
    }
%>
<%@ include file="version-differences-component.jsp" %>
	
    <dspace:item item="<%= item %>" />

    <p>&nbsp;</p>

    <form action="<%= request.getContextPath() %>/mydspace" method="post">
        <input type="hidden" name="workflow_id" value="<%= workflowItem.getID() %>"/>
        <input type="hidden" name="step" value="<%= MyDSpaceServlet.PERFORM_TASK_PAGE %>"/>
        <ul class="list-group">
<%
    
    if (workflowItem.getState() == WorkflowManager.WFSTATE_STEP1 ||
        workflowItem.getState() == WorkflowManager.WFSTATE_STEP2)
    {
%>
                <li class="list-group-item">
                    <%-- <input type="submit" name="submit_approve" value="Approve"> --%>
					<div class="row">
					<input class="btn btn-success col-md-2" type="submit" name="submit_approve" value="<fmt:message key="jsp.mydspace.general.approve"/>" />
					
                    <span class="col-md-10">
                    <%-- If you have reviewed the item and it is suitable for inclusion in the collection, select "Approve". --%>
					<fmt:message key="jsp.mydspace.perform-task.instruct1"/>
					</span>
					</div>
                </li>
<%
    }
    else
    {
        // Must be an editor (step 3)
%>
                    
                 <li class="list-group-item">
					<%-- <input type="submit" name="submit_approve" value="Commit to Archive"> --%>
					<div class="row">
					<input class="btn btn-success col-md-2" type="submit" name="submit_approve" value="<fmt:message key="jsp.mydspace.perform-task.commit.button"/>" />
                    <%-- Once you've edited the item, use this option to commit the
                    item to the archive. --%>
                    <span class="col-md-10">
						<fmt:message key="jsp.mydspace.perform-task.instruct2"/>
					</span>
					</div>
                 </li>
<%
    }

    if (workflowItem.getState() == WorkflowManager.WFSTATE_STEP1 ||
        workflowItem.getState() == WorkflowManager.WFSTATE_STEP2)
    {
%>
				<li class="list-group-item">
					<div class="row">
                    <input class="btn btn-danger col-md-2" type="submit" name="submit_reject" value="<fmt:message key="jsp.mydspace.general.reject"/>"/>
                   	
                    <%-- If you have reviewed the item and found it is <strong>not</strong> suitable
                    for inclusion in the collection, select "Reject".  You will then be asked 
                    to enter a message indicating why the item is unsuitable, and whether the
                    submitter should change something and re-submit. --%>
                    <span class="col-md-10">
						<fmt:message key="jsp.mydspace.perform-task.instruct3"/>
					</span>
					</div>
	        	</li>
	        		
<%
    }

    if (workflowItem.getState() == WorkflowManager.WFSTATE_STEP2 ||
        workflowItem.getState() == WorkflowManager.WFSTATE_STEP3)
    {
%>
				<li class="list-group-item">
					<div class="row">
                    <input class="btn btn-primary col-md-2" type="submit" name="submit_edit" value="<fmt:message key="jsp.mydspace.perform-task.edit.button"/>" />
                    <%-- Select this option to correct, amend or otherwise edit the item's metadata. --%>
                    <span class="col-md-10">
						<fmt:message key="jsp.mydspace.perform-task.instruct4"/>
					</span>
					</div>
				</li>
			
<%
    }
%>
				<li class="list-group-item">
					<div class="row">
                    <input class="btn btn-default col-md-2" type="submit" name="submit_cancel" value="<fmt:message key="jsp.mydspace.perform-task.later.button"/>" />
                    <%-- If you wish to leave this task for now, and return to your "My DSpace", use this option. --%>
                    <span class="col-md-10">
                    	<fmt:message key="jsp.mydspace.perform-task.instruct5"/>
                    </span>
                    </div>
                </li>
                <li class="list-group-item">
                	<div class="row">
                    <input class="btn btn-default col-md-2" type="submit" name="submit_pool" value="<fmt:message key="jsp.mydspace.perform-task.return.button"/>" />
                    <%-- To return the task to the pool so that another user can perform the task, use this option. --%>
                    <span class="col-md-10">
                    	<fmt:message key="jsp.mydspace.perform-task.instruct6"/>
                    </span>
                    </div>
                </li>
        </ul>   
    </form>
</dspace:layout>
