<%--

    The contents of this file are subject to the license and copyright
    detailed in the LICENSE and NOTICE files at the root of the source
    tree and available online at

    http://www.dspace.org/license/

--%>
<%--
  - Show the user a license which they may grant or reject
  -
  - Attributes to pass in:
  -    license          - the license text to display
  --%>

<%@page import="org.dspace.eperson.Group"%>
<%@page import="org.dspace.eperson.EPerson"%>
<%@page import="org.apache.commons.lang.StringUtils"%>
<%@page import="java.util.Arrays"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@page import="org.dspace.core.ConfigurationManager"%>
<%@ page contentType="text/html;charset=UTF-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"
    prefix="fmt" %>
 
<%@ page import="javax.servlet.jsp.jstl.fmt.LocaleSupport" %>

<%@ page import="org.dspace.core.Context" %>
<%@ page import="org.dspace.app.webui.servlet.SubmissionController" %>
<%@ page import="org.dspace.app.util.SubmissionInfo" %>
<%@ page import="org.dspace.app.webui.util.UIUtil" %>

<%@ taglib uri="http://www.dspace.org/dspace-tags.tld" prefix="dspace" %>

<script >
$(document).ready(function(){
	jQuery("#checkbox").click(function() {
	    if ($(this).is(':checked')) {
	        
	        $('#submit_grant').removeAttr('disabled');
	        
	    } else {
	        $('#submit_grant').attr('disabled', true);
	    }
	});
});
</script>

<%
    request.setAttribute("LanguageSwitch", "hide");

    // Obtain DSpace context
    Context context = UIUtil.obtainContext(request);    

	//get submission information object
    SubmissionInfo subInfo = SubmissionController.getSubmissionInfo(context, request);
	
    String license = (String) request.getAttribute("license");
%>

<dspace:layout style="submission"
			   locbar="off"
               navbar="off"
               titlekey="jsp.submit.show-license.title"
               nocache="true">
	
    <form action="<%= request.getContextPath() %>/submit" method="post" onkeydown="return disableEnterKey(event);">

        <jsp:include page="/submit/progressbar.jsp"/>

	<h1><fmt:message key="jsp.submit.show-license.title" />
	<dspace:popup page="<%= LocaleSupport.getLocalizedMessage(pageContext, \"help.index\") +\"#license\"%>"><fmt:message key="jsp.morehelp"/></dspace:popup></h1>
	
    <%
		String typeMetadata = ConfigurationManager.getProperty("license.articles.metadata");
		String type = subInfo.getSubmissionItem().getItem().getMetadata(typeMetadata);
		String[] articleType = ConfigurationManager.getProperty("license.articles.value").split(",");
		List<String> list = new ArrayList<String>();
		list = Arrays.asList(articleType);
		if (list.contains(type))
		{
	%>	
    

        <pre class="panel panel-primary col-md-10 col-md-offset-1"><%= license %></pre>
        <br>
        <div class="col-md-6 col-md-offset-3">
                <fmt:message key="jsp.submit.show-license.copyright"/> <input type="checkbox" id="checkbox" ">
		</div>
		<br>
		<%-- Hidden fields needed for SubmissionController servlet to know which step is next--%>
        <%= SubmissionController.getSubmissionParameters(context, request) %>

	    <div class="btn-group col-md-6 col-md-offset-3">
	    	<input class="btn btn-warning col-md-6" type="submit" name="submit_reject" value="<fmt:message key="jsp.submit.show-license.notgrant.button"/>" />
	    	<input id="submit_grant" class="btn btn-success col-md-6" type="submit" name="submit_grant" disabled value="<fmt:message key="jsp.submit.show-license.grant.button"/>" />
        </div>
	<%
    	}else{
    %> 
    
    <div class="row">
    	<p><fmt:message key="jsp.submit.show-license.template-instruction"/></p>
		<span class="col-md-8 col-md-offset-2">
			<select name="license_chooser" id="license_chooser" class="form-control">
    	<%
    	boolean check = true;
        int index = 1;
        while (check){
        	String label = ConfigurationManager.getProperty("jasper","jsp.submit.license."+index);
            if (StringUtils.isNotBlank(label)){
            	label = "jsp.submit.license."+index;
            	String label2 = LocaleSupport.getLocalizedMessage(pageContext,label);
            	String retrodigitalization = LocaleSupport.getLocalizedMessage(pageContext,"jsp.submit.license.retrodigitalization");

            	
            	if (StringUtils.equals(label2, retrodigitalization)){
            		EPerson submitter = subInfo.getSubmissionItem().getSubmitter();
            		String libraryGroupName = ConfigurationManager.getProperty("license.library");
            		Group g = Group.findByName(context, libraryGroupName);
            		if (submitter != null && g!= null && g.isMember(submitter)){
            			%>
    	                <option value="<%=label%>)">
    	                	<%=label2%>
    	                </option>
    					<%
            		}
            	}else{
	                %>
	                <option value=<%=label%>>
	                	<%=label2%>
	                </option>
					<%
            	}
                index++;
            }else{
            	check = false;
            }
        }
        
        
            	%>
        		</select>
			</span>
		</div>
        <%-- Hidden fields needed for SubmissionController servlet to know which step is next--%>
        <%= SubmissionController.getSubmissionParameters(context, request) %>
		<br>
	    <div class="btn-group col-md-6 col-md-offset-3">
	    	<input class="btn btn-warning col-md-6" type="submit" name="submit_reject" value="<fmt:message key="jsp.submit.show-license.notgrant.button"/>" />
	    	<input class="btn btn-success col-md-6" type="submit" name="submit_grant" value="<fmt:message key="jsp.submit.show-license.grant.button"/>" />
        </div>
        
        <input type="hidden" name="pageCallerID" value="<%= request.getAttribute("pageCallerID")%>"/>     
     <%
    	}
    %>    
        
    </form>

</dspace:layout>
