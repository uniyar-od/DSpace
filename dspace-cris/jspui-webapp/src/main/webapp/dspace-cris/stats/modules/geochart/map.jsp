<%--

    The contents of this file are subject to the license and copyright
    detailed in the LICENSE and NOTICE files at the root of the source
    tree and available online at

    http://www.dspace.org/license/

--%>
<%--  http://www.beginningspatial.com/creating_proportional_symbol_maps_google_maps  --%>

<c:set var="pieType">location</c:set>
<c:set var="targetDiv" scope="page" >div_${data.jspKey}_${statType}_${objectName}_${pieType}</c:set>

<c:set var="jsDataObjectName" scope="page"> data_${statType}_${objectName}_${pieType}_${pieType}</c:set>
<c:if test="${fn:length(data.resultBean.dataBeans[statType][objectName][pieType].dataTable) gt 0}">
<div class="panel panel-default">
  <div class="panel-heading">
   <h6 class="panel-title"><i class="fa fa-map-marker"></i> <fmt:message key="view.stats.map.title" /></h6>
  </div>
  <div class="panel-body">
	<div id="${targetDiv}"></div>
  </div>
</div>
<script type='text/javascript' src='https://www.gstatic.com/charts/loader.js'></script>
<script type="text/javascript">

	var ${jsDataObjectName} = new Array (${fn:length(data.resultBean.dataBeans[statType][objectName][pieType].dataTable)});

function initialize_${jsDataObjectName}() {
	if (${jsDataObjectName}.length==0) return;
    google.charts.load('current', {
        'packages':['geochart'],
    });
	google.charts.setOnLoadCallback(drawRegionsMap);

    function drawRegionsMap() {
    	var data = new google.visualization.DataTable();
    	data.addColumn('number', '<fmt:message key="view.${data.jspKey}.data.${statType}.${objectName}.${pieType}.latitude" />');
    	data.addColumn('number', '<fmt:message key="view.${data.jspKey}.data.${statType}.${objectName}.${pieType}.longitude" />');
    	data.addColumn('number', '<fmt:message key="view.${data.jspKey}.data.${statType}.${objectName}.${pieType}.views" />');
    	data.addColumn('number', '<fmt:message key="view.${data.jspKey}.data.${statType}.${objectName}.${pieType}.percentage" />');
       	
    	var tmpData = '<c:forEach items="${data.resultBean.dataBeans[statType][objectName][pieType].dataTable}" var="row" varStatus="status">[<c:out value="${row.latitude}"/>,<c:out value="${row.longitude}"/>,<c:out value="${row.value}"/>,<c:out value="${row.percentage}"/>],</c:forEach> ';
    	tmpData = tmpData.substring(0,tmpData.lastIndexOf(","));
    	data.addRows(JSON.parse("[" + tmpData + "]"));

        var options = {colorAxis: {colors: ['green', 'blue']}};

        var chart = new google.visualization.GeoChart(document.getElementById("${targetDiv}"));

        chart.draw(data, options);
        
	}
      
  }

initialize_${jsDataObjectName}();

</script>
</c:if>