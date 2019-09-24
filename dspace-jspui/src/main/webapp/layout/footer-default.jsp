<%--

    The contents of this file are subject to the license and copyright
    detailed in the LICENSE and NOTICE files at the root of the source
    tree and available online at

    http://www.dspace.org/license/

--%>
<%--
  - Footer for home page
  --%>

<%@page import="org.dspace.core.ConfigurationManager"%>
<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@page import="org.dspace.eperson.EPerson"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="javax.servlet.jsp.jstl.fmt.LocaleSupport" %>
<%@ page import="org.dspace.core.NewsManager" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="org.dspace.app.webui.util.UIUtil" %>
<%@ page import="org.dspace.app.webui.util.LocaleUIHelper" %>

<%
	String footerNews = NewsManager.readNewsFile(LocaleSupport.getLocalizedMessage(pageContext, "news-footer.html"));
    String sidebar = (String) request.getAttribute("dspace.layout.sidebar");
	String[] mlinks = new String[0];
	String mlinksConf = ConfigurationManager.getProperty("cris","navbar.cris-entities");
	if (StringUtils.isNotBlank(mlinksConf)) {
		mlinks = StringUtils.split(mlinksConf, ",");
	}
	
	boolean showCommList = ConfigurationManager.getBooleanProperty("community-list.show.all",true);
	boolean isRtl = StringUtils.isNotBlank(LocaleUIHelper.ifLtr(request, "","rtl"));
%>

            <%-- Right-hand side bar if appropriate --%>
<%
    if (sidebar != null)
    {
%>
	</div>
	<div class="col-md-3">
                    <%= sidebar %>
    </div>
    </div>       
<%
    }
%>
</div>
<br/>
</main>
            <%-- Page footer --%>
            <footer class="navbar navbar-inverse navbar-bottom navbar-square" id="footer-back" style="background-image:url('<%= request.getContextPath() %>/image/background-dark.jpg');">
             <div>
	             <div class="row">
	             	<div class="col-md-12 col-sm-12">
	             		<%= footerNews %>
	             	</div>
	            </div> 
             </div>
			<div class="extra-footer row"
				style="background-image: linear-gradient(to bottom, #000A0E 0, #00263E 100%);">

				<div id="footer_feedback" class="col-sm-12">
					<p class="text-muted">
					<center>
						<a href="https://www.hsu-hh.de/" target="_blank">
							<img alt="HSU" src="<%=request.getContextPath()%>/image/header-logo-hsu.png" style="height:55px; width:55px;">
						</a>
						&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;-&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
						<a href="https://ub.hsu-hh.de/" target="_blank">HSU Library</a>
						&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;-&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
						<a href="<%=request.getContextPath()%>/feedback"> <fmt:message key="jsp.layout.footer-default.feedback" />
						</a> <br> <br>
						<hr style="margin: 5px; width: 95%;">
						<fmt:message key="jsp.layout.footer-default.notes" />

					</center>
					<a href="<%=request.getContextPath()%>/htmlmap"></a>
					</p>
				</div>
			</div>

			<div class="extra-footer row">
				<div id="footer_feedback" class="col-sm-4 text-left">
					<fmt:message key="jsp.layout.footer-default.text" />
				</div>
				<div id="designedby" class="col-sm-8 text-right">
					<fmt:message key="jsp.layout.footer-default.version-by" />
					<a href="http://www.4science.it/en/dspace-and-dspace-cris-services/">
						<img src="<%= request.getContextPath() %>/image/logo-4science-small.png" alt="Logo 4SCIENCE" height="32px" />
					</a>
				</div>
			</div>
			<button onclick="topFunction()" id="myBtn" title="<fmt:message key="jsp.layout.footer-default.backtotop"/>">&nbsp;&nbsp;<i class="fa fa-angle-up"></i>&nbsp;&nbsp;</button>
<script>
// When the user scrolls down 20px from the top of the document, show the button
window.onscroll = function() {scrollFunction()};

function scrollFunction() {
    if (document.body.scrollTop > 20 || document.documentElement.scrollTop > 20) {
        document.getElementById("myBtn").style.display = "block";
    } else {
        document.getElementById("myBtn").style.display = "none";
    }
}

// When the user clicks on the button, scroll to the top of the document
function topFunction() {
    document.body.scrollTop = 0;
    document.documentElement.scrollTop = 0;
}
</script>
	    </footer>
    </body>
</html>
