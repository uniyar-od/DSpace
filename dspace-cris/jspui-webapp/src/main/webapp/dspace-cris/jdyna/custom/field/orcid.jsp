<%@ page import="org.dspace.core.ConfigurationManager" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<% String orcidDomainName = ConfigurationManager.getProperty("cris","external.domainname.authority.service.orcid"); %>
<c:if test="${!empty anagraficaObject.anagrafica4view['orcid']}">
<div class="dynaField" style="min-height:5em;min-width:5em;">
<span class="dynaLabel" style="width:10em;">ORCID</span>

<div id="orcidDiv" class="dynaFieldValue">
<c:choose>
    <c:when test="${empty anagraficaObject.anagrafica4view['system-orcid-token-authenticate']}">
        <span style="min-width: 30em;"><a target="_blank" href="<%= orcidDomainName %>${anagraficaObject.anagrafica4view['orcid'][0]}"> ${anagraficaObject.anagrafica4view['orcid'][0]}</a></span>
    </c:when>
    <c:when test="${!empty anagraficaObject.anagrafica4view['system-orcid-token-authenticate']}">
        <span style="min-width: 30em;"><a target="_blank" href="<%= orcidDomainName %>${anagraficaObject.anagrafica4view['orcid'][0]}"><img src="<%=request.getContextPath()%>/images/orcid_16x16.png" /> ${anagraficaObject.anagrafica4view['orcid'][0]}</a></span>
    </c:when>
</c:choose>
</div>
</div>
<div class="dynaClear">&nbsp;</div>
</c:if>