<%--

    The contents of this file are subject to the license and copyright
    detailed in the LICENSE and NOTICE files at the root of the source
    tree and available online at

    https://github.com/CILEA/dspace-cris/wiki/License

--%>
<%@ taglib uri="http://www.dspace.org/dspace-tags.tld" prefix="dspace"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="jdynatags" prefix="dyna"%>
<c:set var="root"><%=request.getContextPath()%></c:set>

<c:set var="showoaicompliancebadge" value="false" />
<c:set var="showrdmbadge" value="false" />
<div class="container" id="${holder.shortName}">
	
			<div class="dynaClear">&nbsp;</div>
            <div class="dynaClear">&nbsp;</div>
            <div class="dynaClear">&nbsp;</div>
			<div class="dynaField"></div>

	<c:if
		test="${!empty anagraficaObject.anagrafica4view['driscomplianceopenaire']}">
		<c:if
			test="${anagraficaObject.anagrafica4view['driscomplianceopenaire'][0].value.real==true}">

			<c:set var="showoaicompliancebadge" value="true" />

		</c:if>
	</c:if>

	<c:if test="${!empty anagraficaObject.anagrafica4view['driscoverage']}">
		<c:forEach items="${anagraficaObject.anagrafica4view['driscoverage']}"
			var="value">
			<c:if test="${value.value.real eq 'Dataset'}">

				<c:set var="showrdmbadge" value="true" />

			</c:if>
		</c:forEach>
	</c:if>


	<c:if
		test="${showoaicompliancebadge==true}">
		
		<img src="${root}/image/dris/openaire-horizontal.png" class="img-thumbnail-badge"/>

	</c:if>
	
	<c:if
		test="${showrdmbadge==true}">
		
		<img src="${root}/image/dris/rdm-plus.png" class="img-thumbnail-badge"/>

	</c:if>
</div>
