/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
function toggleTable(bundleID){	
	hiddenDiv=document.getElementById("hidden-div-"+bundleID);
	tableContainer=document.getElementById("table-container-"+bundleID);	
	if(tableContainer.innerHTML.trim()===""){
		tableContainer.innerHTML=hiddenDiv.innerHTML;
	}else{
		hiddenDiv.innerHTML=tableContainer.innerHTML;
		tableContainer.innerHTML="";
	}
}