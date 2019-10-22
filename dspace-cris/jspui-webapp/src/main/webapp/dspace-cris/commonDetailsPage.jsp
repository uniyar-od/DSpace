<%--

    The contents of this file are subject to the license and copyright
    detailed in the LICENSE and NOTICE files at the root of the source
    tree and available online at

    https://github.com/CILEA/dspace-cris/wiki/License

--%>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://www.dspace.org/dspace-tags.tld" prefix="dspace" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>

<%@ taglib uri="jdynatags" prefix="dyna"%>
<%@ taglib uri="researchertags" prefix="researcher"%>

<%@page import="it.cilea.osd.jdyna.components.IComponent"%>
<%@page import="org.dspace.app.webui.cris.components.ASolrConfigurerComponent"%>
<%@page import="java.util.Map" %>
<%@page import="org.dspace.core.ConfigurationManager" %>
<%@page import="org.dspace.app.cris.model.ACrisObject" %>	
<%@page import="it.cilea.osd.jdyna.components.IComponent"%>
<%@page import="org.apache.commons.lang.StringUtils" %>

<% 
	ACrisObject entity = (ACrisObject)request.getAttribute("entity");
	Map<String, IComponent> mapInfo = ((Map<String, IComponent>)(request.getAttribute("components"))); 
	boolean showBadgeCount = ConfigurationManager.getBooleanProperty("cris", "webui.tab.show.count.for.firstcomponent", false);
	String[] verticalSections = new String[0];
	String verticalSectionsConfigured = ConfigurationManager.getProperty("cris-vertical-components", "cris-entities." + entity.getTypeText().toLowerCase() + ".sections");
	if (StringUtils.isNotBlank(verticalSectionsConfigured)) {
		verticalSections = verticalSectionsConfigured.split(",");
	}
%>

	<c:set var="verticalSections" value="<%= verticalSections %>"></c:set>
	<div class="col-lg-3 tab-content-left ${empty verticalSections ? 'researcher-menu-item-hidden': 'researcher-menu-item'}">
		<ul class="nav nav-pills">
			<c:forEach items="${verticalSections}" var="verticalSection">
				<c:set var="verticalSection" value="${verticalSection}" scope="request"></c:set>

				<c:set var="tabName" value="researcher-menu-item-hidden"/>
				<c:forEach items="${tabList}" var="area" varStatus="rowCounter">
					<c:if test="${researcher:isSectionVerticalTab(verticalSection,entity.getTypeText(),area.shortName) == true && researcher:isTabHidden(entity,area.shortName) == false}">
						<c:set var="tabName" value="researcher-menu-item"/>
					</c:if>
				</c:forEach>

				<div class="panel panel-default ${tabName}">
					<div class="panel-heading">
						<fmt:message key="jsp.layout.cris.${entity.getTypeText().toLowerCase()}.sections.${verticalSection}.title" />
					</div>
					<div id="dddtabs" class="panel-body">
						<ul class="nav nav-pills nav-stacked">
							<c:forEach items="${tabList}" var="area" varStatus="rowCounter">
								<c:if test="${researcher:isSectionVerticalTab(verticalSection,entity.getTypeText(),area.shortName) == true}">
									<c:set var="tablink">
										<c:choose>
											<c:when test="${rowCounter.count == 1}">${root}/cris/${specificPartPath}/${authority}</c:when>
											<c:otherwise>${root}/cris/${specificPartPath}/${authority}/${area.shortName}.html</c:otherwise>
										</c:choose>
									</c:set>

									<c:choose>
										<c:when test="${(researcher:isTabHidden(entity,area.shortName) == false)}">
											<c:set var="tabName" value="researcher-menu-item"/>
										</c:when>
										<c:otherwise>
											<c:set var="tabName" value="researcher-menu-item-hidden"/>
										</c:otherwise>
									</c:choose>

									<li data-tabname="${area.shortName}" class="${tabName}${area.id == tabId ? ' selected-' : ' '}vertical-tab" id="bar-tab-${area.id}">
										<a href="${tablink}">
										<c:if test="${!empty area.ext}">
										<img style="width: 16px;vertical-align: middle;" border="0"
											src="<%=request.getContextPath()%>/cris/researchertabimage/${area.id}" alt="icon" />
										</c:if>
										<spring:message code="${entity.class.simpleName}.tab.${area.shortName}.label" text="${area.title}"></spring:message>
										<% if(showBadgeCount) { %>
										<c:set var="firstComponentFound" value="false"/>
										<c:forEach items="${area.mask}" var="box" varStatus="boxRowCounter">
										<c:if test="${!empty box.externalJSP && !firstComponentFound}">
										<%
										if(mapInfo!=null && !mapInfo.isEmpty()) {

											for(String key : mapInfo.keySet()) {
										%>
										<c:set var="key"><%= key %></c:set>
										<c:if test="${box.getShortName() eq key && !firstComponentFound}">
										<%
												ASolrConfigurerComponent iii = (ASolrConfigurerComponent)(mapInfo.get(key));
												String type = (String)iii.getType(request, entity.getId());
											    long count = iii.count(request, type, entity.getId());
												if(count>0) {
										%>
												<span class="badge badge-primary badge-pill"><%= count %></span>
												<c:set var="firstComponentFound" value="true"/>
									    <%
												} %>
										</c:if>
									    <%
											}
										}
										%>
										</c:if>
										</c:forEach>
										<% } %>
										</a>
									</li>
								</c:if>
							</c:forEach>
						</ul>
					</div>
				</div>
			</c:forEach>

		</ul>
	</div>
	<div class="${empty verticalSections ? 'col-lg-12': 'col-lg-9'}">
		<ul class="nav nav-pills">
			<c:forEach items="${tabList}" var="area" varStatus="rowCounter">
				<c:if test="${researcher:isVerticalTab(entity.getTypeText(),area.shortName) == false}">
					<c:set var="tablink">
						<c:choose>
							<c:when test="${rowCounter.count == 1}">${root}/cris/${specificPartPath}/${authority}</c:when>
							<c:otherwise>${root}/cris/${specificPartPath}/${authority}/${area.shortName}.html</c:otherwise>
						</c:choose>
					</c:set>

					<c:choose>
						<c:when test="${(researcher:isTabHidden(entity,area.shortName) == false)}">
							<c:set var="tabName" value="researcher-menu-item"/>
						</c:when>
						<c:otherwise>
							<c:set var="tabName" value="researcher-menu-item-hidden"/>
						</c:otherwise>
					</c:choose>

					<li data-tabname="${area.shortName}" class="${tabName}${area.id == tabId ? ' selected-' : ' '}horizontal-tab" id="bar-tab-${area.id}">
						<a href="${tablink}">
						<c:if test="${!empty area.ext}">
						<img style="width: 16px;vertical-align: middle;" border="0"
							src="<%=request.getContextPath()%>/cris/researchertabimage/${area.id}" alt="icon" />
						</c:if>
						<spring:message code="${entity.class.simpleName}.tab.${area.shortName}.label" text="${area.title}"></spring:message>
						<% if(showBadgeCount) { %>
						<c:set var="firstComponentFound" value="false"/>
						<c:forEach items="${area.mask}" var="box" varStatus="boxRowCounter">
						<c:if test="${!empty box.externalJSP && !firstComponentFound}">
						<%
						if(mapInfo!=null && !mapInfo.isEmpty()) {

							for(String key : mapInfo.keySet()) {
						%>
						<c:set var="key"><%= key %></c:set>
						<c:if test="${box.getShortName() eq key && !firstComponentFound}">
						<%
								ASolrConfigurerComponent iii = (ASolrConfigurerComponent)(mapInfo.get(key));
								String type = (String)iii.getType(request, entity.getId());
							    long count = iii.count(request, type, entity.getId());
								if(count>0) {
						%>
								<span class="badge badge-primary badge-pill"><%= count %></span>
								<c:set var="firstComponentFound" value="true"/>
					    <%
								} %>
						</c:if>
					    <%
							}
						}
						%>
						</c:if>
						</c:forEach>
						<% } %>
						</a>
					</li>
				</c:if>

			</c:forEach>
		</ul>
<c:forEach items="${tabList}" var="areaIter" varStatus="rowCounter">
	<c:if test="${areaIter.id == tabId}">
	<c:set var="area" scope="request" value="${areaIter}"></c:set>
	<c:set var="isVerticalTab" value="${researcher:isVerticalTab(entity.getTypeText(),area.shortName)}" scope="request"/>
	<jsp:include page="singleTabDetailsPage.jsp"></jsp:include>
	</c:if>
	
</c:forEach>

</div>
<div class="clearfix">&nbsp;</div>