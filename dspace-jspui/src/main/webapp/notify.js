function selectMetadataAndSubmit(status) {
	document.getElementById("selected_status").value = status;
	document.getElementById("choose_status_for_details").submit();
}

function goToPage(offset, pageSize, action) {
	if (action === "next") {
		offset += pageSize;
	} else if (action === "previous") {
		offset -= pageSize;
	}
	document.getElementById("offset").value = offset*1;
	console.log("OFFSET value: "+document.getElementById("offset").value);
	document.getElementById("notify_report_pagination_form").submit();
}
