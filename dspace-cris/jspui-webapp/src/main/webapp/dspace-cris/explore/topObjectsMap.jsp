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


<%
if (locations != null && locations.count() > 0)
{
	
%>
<c:set var="dspace.layout.head" scope="request">
<script src="//maps.googleapis.com/maps/api/js?key=<%= ConfigurationManager.getProperty("key.googleapi.maps") %>&sensor=true&v=3" type="text/javascript"></script>
<script src="https://unpkg.com/@google/markerclustererplus@4.0.1/dist/markerclustererplus.min.js"></script>
</c:set>
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
%>
<dspace:map-artifact style="global" artifact="<%= locations.getRecentSubmissions() %>" view="<%= mapConf %>" />

var markers = locations.map(function(location, i) {
    var marker = new google.maps.Marker({
      position: location,
      title: labels[i % labels.length]
    });
    var infowindow = new google.maps.InfoWindow({ 
    	content:contents[i % contents.length]
    });
    marker.addListener('mouseover',function(){	infowindow.open(map,marker);});
    return marker;
  });

  // Add a marker clusterer to manage the markers.
  var markerCluster = new MarkerClusterer(map, markers,
      {imagePath: '<%= request.getContextPath()%>/image/m'});
-->
</script>
	
<%
}
%>