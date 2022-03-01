/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
function toggleTable(bundleID){
	console.log("show "+bundleID);
	
	hiddenDiv=document.getElementById("hidden-div-"+bundleID);
	tableContainer=document.getElementById("table-container-"+bundleID);
	
	console.log(tableContainer.innerHTML.trim()!=="");
	console.log(tableContainer.innerHTML.trim());
	
	if(tableContainer.innerHTML.trim()===""){
		tableContainer.innerHTML=hiddenDiv.innerHTML;
	}else{
		hiddenDiv.innerHTML=tableContainer.innerHTML;
		tableContainer.innerHTML="";
	}
}