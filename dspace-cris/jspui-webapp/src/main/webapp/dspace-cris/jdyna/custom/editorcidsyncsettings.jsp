<%--

    The contents of this file are subject to the license and copyright
    detailed in the LICENSE and NOTICE files at the root of the source
    tree and available online at

    https://github.com/CILEA/dspace-cris/wiki/License

--%>
<%@ taglib uri="http://www.dspace.org/dspace-tags.tld" prefix="dspace" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="jdynatags" prefix="dyna"%>
<%@ taglib uri="researchertags" prefix="researcher"%>
<%@ page import="java.util.Locale"%>
<%@ page import="org.dspace.app.webui.util.UIUtil" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>

<%
Locale sessionLocale = UIUtil.getSessionLocale(request);
String currLocale = null;
if (sessionLocale != null) {
	currLocale = sessionLocale.toString();
}

%>
<script type="text/javascript">
<!--
j(document).ready(function() {
	j(".bottomTooltip").popover({
		placement: "bottom",
		trigger: "hover"
	});
});
//-->
</script>
<c:set var="root"><%=request.getContextPath()%></c:set>
<div class="panel-group" id="${holder.shortName}">
	<div class="panel panel-default">
    	<div class="panel-heading">
    		<h4 class="panel-title">
        		<a data-toggle="collapse" data-parent="#${holder.shortName}" href="#collapseOne${holder.shortName}">
          			${holder.title} 
        		</a></h4>
    	</div>
		<div id="collapseOne${holder.shortName}" class="panel-collapse collapse<c:if test="${holder.collapsed==false}"> in</c:if>">
			<div class="panel-body">
				<div class="col-md-12">	
					<div class="panel panel-default">
						<div class="panel-heading">
							<h3 class="panel-title">
								<fmt:message
									key="jsp.orcid.custom.box.label.preferences.pushmode.title" />
							</h3>
						</div>
						<div class="panel-body">
							<div class="container">
								<div class="alert alert-info" role="alert">
									<fmt:message
										key="jsp.orcid.custom.box.label.preferences.pushmode" />
								</div>
								<div class="col-md-12">
									<c:forEach
										items="${propertiesDefinitionsInHolder[holder.shortName]}"
										var="tipologiaDaVisualizzareNoI18n">
										<c:set var="tipologiaDaVisualizzare" value="${researcher:getPropertyDefinitionI18N(tipologiaDaVisualizzareNoI18n,currLocale)}" />
										<c:if
											test="${tipologiaDaVisualizzare.shortName eq 'orcid-push-manual'}">
											<dyna:edit tipologia="${tipologiaDaVisualizzare.object}"
												disabled="${disabled}"
												propertyPath="anagraficadto.anagraficaProperties[${tipologiaDaVisualizzare.shortName}]"
												ajaxValidation="validateAnagraficaProperties"
												hideLabel="${hideLabel}" validationParams="${parameters}"
												visibility="${visibility}" lock="true" />
										</c:if>
									</c:forEach>
								</div>
							</div>
						</div>
					</div>
				</div>
			<div class="clearfix">&nbsp;</div>
			<div class="dynaClear">&nbsp;</div>
            <div class="dynaClear">&nbsp;</div>
            <div class="dynaClear">&nbsp;</div>
			<div class="dynaField"></div>								
					
					<div class="col-md-4"><div class="panel panel-default">
  						<div class="panel-heading">
    						<h3 class="panel-title"><fmt:message key="jsp.orcid.custom.box.label.preferences.publications"/></h3>
  						</div>
  						<div class="panel-body">
    						<div class="container">
    							<div class="label label-info"><fmt:message key="jsp.orcid.custom.box.label.preferences.publications.tips"/></div>
    							<div class="clearfix"></div>
								<c:forEach
									items="${propertiesDefinitionsInHolder[holder.shortName]}"
									var="tipologiaDaVisualizzareNoI18n">			
									<c:set var="tipologiaDaVisualizzare" value="${researcher:getPropertyDefinitionI18N(tipologiaDaVisualizzareNoI18n,currLocale)}" />						
									<c:if test="${tipologiaDaVisualizzare.shortName eq 'orcid-publications-prefs'}">
										<c:set var="propertyPath" value="anagraficadto.anagraficaProperties[${tipologiaDaVisualizzare.shortName}]" />
										<c:set var="option4row" value="${tipologiaDaVisualizzare.object.rendering.option4row}" />
										<spring:bind path="${propertyPath}[0]">
											<c:set var="value" value="${status.value}" />
											<c:set var="inputName"><c:out value="${status.expression}" escapeXml="false"></c:out></c:set>
										</spring:bind>

										<c:forEach var="option" items="${dyna:getResultsFromWidgetCheckRadio(tipologiaDaVisualizzare.object.rendering.staticValues)}" varStatus="optionStatus">
											<c:set var="checked" value="" />
											<c:if test="${option.identifyingValue == value}">
												<c:set var="checked" value=" checked=\"checked\"" />
											</c:if>
											<c:if test="${empty value && optionStatus.count == 1}">
												<c:set var="checked" value=" checked=\"checked\"" />
											</c:if>

											<c:set var="parametersValidation" value="${dyna:extractParameters(parameters)}"/>
											<c:set var="functionValidation" value="${validateAnagraficaProperties}('${inputName}',${parametersValidation})" />
											<input id="${inputName}" name="${inputName}" type="radio" value="${option.identifyingValue}" ${checked} onchange="${functionValidation};${onchange}">
												<span class="bottomTooltip" data-toggle="popover" data-container="body" data-content="<fmt:message key="jsp.orcid.custom.box.label.preferences.publications.${option.identifyingValue}.tooltip"/>">
													${option.displayValue}
												</span>
											</input>
											<input name="_${inputName}" id="_${inputName}" value="true" type="hidden" />
											<c:if test="${optionStatus.count mod option4row == 0}">
												<br/>
											</c:if>
										</c:forEach>
										<dyna:validation propertyPath="${propertyPath}" />
									</c:if>
								</c:forEach>
							</div>
						</div>   
					</div></div>

					<div class="col-md-4"><div class="panel panel-default">
  						<div class="panel-heading">
    						<h3 class="panel-title"><fmt:message key="jsp.orcid.custom.box.label.preferences.grant"/></h3>
  						</div>
  						<div class="panel-body">
    						<div class="container">
    							<div class="label label-info"><fmt:message key="jsp.orcid.custom.box.label.preferences.projects.tips"/></div>
    							<div class="clearfix"></div>
								<c:forEach
									items="${propertiesDefinitionsInHolder[holder.shortName]}"
									var="tipologiaDaVisualizzareNoI18n">
									<c:set var="tipologiaDaVisualizzare" value="${researcher:getPropertyDefinitionI18N(tipologiaDaVisualizzareNoI18n,currLocale)}" />
									<c:if test="${tipologiaDaVisualizzare.shortName eq 'orcid-projects-prefs'}">
										<c:set var="propertyPath" value="anagraficadto.anagraficaProperties[${tipologiaDaVisualizzare.shortName}]" />
										<c:set var="option4row" value="${tipologiaDaVisualizzare.object.rendering.option4row}" />
										<spring:bind path="${propertyPath}[0]">
											<c:set var="value" value="${status.value}" />
											<c:set var="inputName"><c:out value="${status.expression}" escapeXml="false"></c:out></c:set>
										</spring:bind>

										<c:forEach var="option" items="${dyna:getResultsFromWidgetCheckRadio(tipologiaDaVisualizzare.object.rendering.staticValues)}" varStatus="optionStatus">
											<c:set var="checked" value="" />
											<c:if test="${option.identifyingValue == value}">
												<c:set var="checked" value=" checked=\"checked\"" />
											</c:if>
											<c:if test="${empty value && optionStatus.count == 1}">
												<c:set var="checked" value=" checked=\"checked\"" />
											</c:if>

											<c:set var="parametersValidation" value="${dyna:extractParameters(parameters)}"/>
											<c:set var="functionValidation" value="${validateAnagraficaProperties}('${inputName}',${parametersValidation})" />
											<input id="${inputName}" name="${inputName}" type="radio" value="${option.identifyingValue}" ${checked} onchange="${functionValidation};${onchange}">
												<span class="bottomTooltip" data-toggle="popover" data-container="body" data-content="<fmt:message key="jsp.orcid.custom.box.label.preferences.projects.${option.identifyingValue}.tooltip"/>">
													${option.displayValue}
												</span>
											</input>
											<input name="_${inputName}" id="_${inputName}" value="true" type="hidden" />
											<c:if test="${optionStatus.count mod option4row == 0}">
												<br/>
											</c:if>
										</c:forEach>
										<dyna:validation propertyPath="${propertyPath}" />
									</c:if>
								</c:forEach>
								  </div>  
    						</div>
						</div>   
					</div>


					<div class="col-md-4"><div class="panel panel-default">
  						<div class="panel-heading">
    						<h3 class="panel-title"><fmt:message key="jsp.orcid.custom.box.label.preferences.profile"/></h3>
  						</div>
  						<div class="panel-body">
    						<div class="container">
    							<div class="label label-info"><fmt:message key="jsp.orcid.custom.box.label.preferences.profile.tips"/></div>
    							<div class="clearfix"></div>		
								<c:forEach
									items="${propertiesDefinitionsInHolder[holder.shortName]}"
									var="tipologiaDaVisualizzareNoI18n">
									<c:set var="tipologiaDaVisualizzare" value="${researcher:getPropertyDefinitionI18N(tipologiaDaVisualizzareNoI18n,currLocale)}" />
									<c:if test="${fn:startsWith(tipologiaDaVisualizzare.shortName, 'orcid-profile-pref-')}">
										<c:set var="validationParams" value="${parameters}" />
										<c:set var="propertyPath" value="anagraficadto.anagraficaProperties[${tipologiaDaVisualizzare.shortName}]" />
										<c:set var="checkedAsDefault" value="${tipologiaDaVisualizzare.object.rendering.checked}" />

										<div class="dynaField">
											<span class="dynaLabel bottomTooltip" data-toggle="popover" data-container="body" data-content="${fn:replace(tipologiaDaVisualizzare.shortName, 'orcid-profile-pref-', '')}" data-original-title="" title="">
												${tipologiaDaVisualizzare.object.label}
											</span>
											<c:set var="checked" value="" />
											<spring:bind path="${propertyPath}[0]">
												<c:set var="inputValue" ><c:out value="${status.value}" escapeXml="true"></c:out></c:set>
												<c:set var="inputName"><c:out value="${status.expression}" escapeXml="false"></c:out></c:set>
												<c:if test="${inputValue or checkedAsDefault}">
													<c:set var="checked" value="checked=\"checked\"" />
												</c:if>
											</spring:bind>
											<c:set var="validation" value="${propertyPath}[0]" />
											<c:set var="parametersValidation" value="${dyna:extractParameters(parameters)}"/>
											<c:set var="functionValidation" value="" />
											<c:if test="${!empty validateAnagraficaProperties}">
												<c:set var="functionValidation" value="validateAnagraficaProperties('${inputName}'${!empty parametersValidation?',':''}${!empty parametersValidation?parametersValidation:''});" />
											</c:if>

											<input id="_${inputName}" name="_${inputName}" type="hidden"  />
											<input id="${inputName}" name="${inputName}" type="hidden" value="${empty inputValue?'false':inputValue}"  />

											<c:set var="onchangeJS" value="cambiaBoolean('${inputName}');${functionValidation};${onchange}" />
											<input id="check${inputName}" type="checkbox" value="${inputValue}" ${checked} <dyna:javascriptEvents onchange="${onchangeJS}"/> <dyna:javascriptEvents onclick="${onclick}"/>/>
											<dyna:validation propertyPath="${validation}" />
										</div>
									</c:if>
		
								</c:forEach>
								</div>
							</div>
		
   						 </div>							
						</div>   
					</div></div></div>
</div>