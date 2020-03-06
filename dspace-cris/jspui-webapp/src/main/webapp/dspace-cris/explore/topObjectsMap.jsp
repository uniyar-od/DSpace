<%--

    The contents of this file are subject to the license and copyright
    detailed in the LICENSE and NOTICE files at the root of the source
    tree and available online at

    https://github.com/CILEA/dspace-cris/wiki/License

--%>
<%@ page contentType="text/html;charset=UTF-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@ taglib uri="http://www.dspace.org/dspace-tags.tld" prefix="dspace" %>

<%@ page import="java.util.List"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.dspace.content.DSpaceObject" %>
<%@ page import="org.dspace.discovery.configuration.DiscoveryViewFieldConfiguration" %>
<%@ page import="org.dspace.discovery.configuration.DiscoveryMapConfiguration" %>

<c:set var="dspace.layout.head" scope="request">
<script src="//maps.googleapis.com/maps/api/js?key=<%= ConfigurationManager.getProperty("key.googleapi.maps") %>&sensor=true&v=3" type="text/javascript"></script>
</c:set>
<%
if (locations != null && locations.count() > 0)
{
	
%>
<div class="panel panel-default">
  <div class="panel-heading">
   <h6 class="panel-title"><i class="fa fa-map-marker"></i> <fmt:message key="view.stats.map.title" /></h6>
  </div>
  <div class="panel-body">
	<div id="mapExplore"></div>
  </div>
</div>

<script type="text/javascript">
<!--
    var myOptions = {
      zoom: 2,
      center: new google.maps.LatLng(40.400231, -3.682978),
      mapTypeId: google.maps.MapTypeId.ROADMAP
    };
	var map = new google.maps.Map(document.getElementById("mapExplore"),myOptions);
	
<%
DiscoveryMapConfiguration mapConf = (DiscoveryMapConfiguration) locations.getConfiguration();
for (IGlobalSearchResult obj : locations.getRecentSubmissions()) {
	String latLongField = mapConf.getLatitudelongitude();
	String latitudeField = mapConf.getLatitude();
	String longitudeField = mapConf.getLongitude();
	List<String> latitude = new ArrayList<>();
	List<String> longitude= new ArrayList<>();
	if(StringUtils.isNotBlank(latLongField)){
		for(String value : obj.getMetadataValue(latLongField)){
			if(StringUtils.isNotBlank(value) && StringUtils.contains(value,",")){
				latitude.add( StringUtils.split(value,",")[0]);
				longitude.add( StringUtils.split(value,",")[1]);
			}
		}
		
	}else if(StringUtils.isNotBlank(latitudeField) && StringUtils.isNotBlank(longitudeField)){
		latitude = 	obj.getMetadataValue(latitudeField);
		longitude =	obj.getMetadataValue(latitudeField);
	}
	
	if(latitude.isEmpty() || longitude.isEmpty() || latitude.size()!=longitude.size()){
		continue;
	}
	
	for(int x=0; x<latitude.size();x++){
		String location =latitude.get(x) + ","+ longitude.get(x);
		%>
		<dspace:map-artifact style="global" artifact="<%= obj %>" view="<%= mapConf %>" location="<%= location %>" />
	<%}
}
%>
-->
</script>
	
<%
}
%>