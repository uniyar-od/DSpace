<%--

    The contents of this file are subject to the license and copyright
    detailed in the LICENSE and NOTICE files at the root of the source
    tree and available online at

    http://www.dspace.org/license/

--%>
<%--
  - Page that displays the list of choices of login pages
  - offered by multiple stacked authentication methods.
  --%>

<%@ page contentType="text/html;charset=UTF-8" %>

<%@ page import="java.util.Iterator" %>

<%@ page import="javax.servlet.jsp.jstl.fmt.LocaleSupport" %>

<%@ page import="java.sql.SQLException" %>

<%@ page import="org.apache.log4j.Logger" %>

<%@ page import="org.dspace.app.webui.util.JSPManager" %>
<%@ page import="org.dspace.app.webui.util.UIUtil" %>
<%@ page import="org.dspace.authenticate.AuthenticationManager" %>
<%@ page import="org.dspace.authenticate.AuthenticationMethod" %>
<%@ page import="org.dspace.core.Context" %>
<%@ page import="org.dspace.core.LogManager" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"
    prefix="fmt" %>

<%@ taglib uri="http://www.dspace.org/dspace-tags.tld" prefix="dspace" %>

<dspace:layout navbar="off" locbar="off" titlekey="jsp.login.chooser.title" nocache="true">

<div style="height:20px;">
	<p></p>
</div>

<div style="border-radius: 10px; box-shadow: 5px 5px 5px 10px grey; background-image:url('/image/background-dark.jpg');">
	<br>
    <table class="miscTable" align="center" width="30%">
        <tr>
            <td class="evenRowEvenCol">
		    
		    <%-- <H1>Log In to DSpace</H1> --%>
		<%--   <h3><fmt:message key="jsp.login.chooser.heading"/></h3> --%>
		
		<h3 style="color:#D3D3D3; border-color:none; border-radius:10px; text-align: center; padding:10px; background-color:#45A29E;">Please choose an option</h3>
		
	
		<td> 
            <%--
	    <td align="right" class="standard">
                <dspace:popup page="<%= LocaleSupport.getLocalizedMessage(pageContext, \"help.index\") + \"#login\" %>"><fmt:message key="jsp.help"/></dspace:popup>
            </td>
	   --%>
        </tr>
    </table> 


    <p></p>


    
    <table class="miscTable" align="center" width="36%">
      <tr>
        <td class="evenRowEvenCol">
          <%--
		<h4><fmt:message key="jsp.login.chooser.chooseyour"/></h4>
		--%>
	  
	  <%--
	  <ul>
		  --%>
		  
<%
    
    Iterator ai = AuthenticationManager.authenticationMethodIterator();
    AuthenticationMethod am;
    Context context = null;
    try
    {
    	context = UIUtil.obtainContext(request);
    	int count = 0;
    	String url = null;
    	while (ai.hasNext())
    	{
        	am = (AuthenticationMethod)ai.next();
        	if ((url = am.loginPageURL(context, request, response)) != null)
        	{
			%>
			<%--
		            <li>
			--%>
		   
		    
		    <h4 style="border-radius:10px; padding:20px; margin-bottom: 30px; text-align: center; background-color:#D3D3D3;"><a href="<%= url %>" style="color:#466675;">
		<%-- This kludge is necessary because fmt:message won't
                     evaluate its attributes, so we can't use it on java expr --%>
                <%= javax.servlet.jsp.jstl.fmt.LocaleSupport.getLocalizedMessage(pageContext, am.loginPageTitle(context)) %>
                        </a></h4>
		    
		    
			    <%--
			    </li>
			    --%>
				<%
	        }
        }
    }
    catch(SQLException se)
    {
    	// Database error occurred.
        Logger log = Logger.getLogger("org.dspace.jsp");
        log.warn(LogManager.getHeader(context,
                "database_error",
                se.toString()), se);

        // Also email an alert
        UIUtil.sendAlert(request, se);
        JSPManager.showInternalError(request, response);
    }
    finally 
    {
    	context.abort();
    }
  
%>
<%--
          </ul>
	  --%>
	</td>
      </tr>
      
    </table> 

  
    </div>

    <div style="height:50px">
	    <p></p>
    </div>

</dspace:layout>
