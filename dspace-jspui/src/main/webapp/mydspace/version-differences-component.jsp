<%--

    The contents of this file are subject to the license and copyright
    detailed in the LICENSE and NOTICE files at the root of the source
    tree and available online at

    http://www.dspace.org/license/

--%>
<%
	if (previousItem != null) {
%>
		<p class="alert alert-info">
			<fmt:message key="jsp.mydspace.preview-task.version">
				<fmt:param><%= versionMessage %></fmt:param>
				<fmt:param><%= request.getContextPath()  %>/handle/<%= previousItem.getHandle() %></fmt:param>
			</fmt:message>
		</p>
		<p><fmt:message key="jsp.mydspace.preview-task.version.changes" />
		<% if(modifiedMetadata != null && modifiedMetadata.size() > 0) { %>
		<button type="button" class="btn btn-warn" data-toggle="collapse" data-target="#metadata">
			<fmt:message key="jsp.mydspace.preview-task.version.modified-metadata">
				<fmt:param><%= modifiedMetadata.size() %></fmt:param>
			</fmt:message>
		</button>
		<% } else { %>
		<b><fmt:message key="jsp.mydspace.preview-task.version.no-modified-metadata" /></b>    
		<% } %>
		 |
		<% if(modifiedFiles != null && modifiedFiles.size() > 0) { %>
		<button type="button" class="btn btn-warn" data-toggle="collapse" data-target="#files">
			<fmt:message key="jsp.mydspace.preview-task.version.modified-files" />
		</button>
		<% } else { %>
		<b><fmt:message key="jsp.mydspace.preview-task.version.no-modified-files" /></b>    
		<% } %>
		</p>
		<% if(modifiedMetadata != null && modifiedMetadata.size() > 0) { %>
		  <div id="metadata" class="well collapse">
		      
		  <table class="table">
		  <thead>
					<th><fmt:message key="jsp.mydspace.preview-task.version.modified-metadata.metadata" /></th>
					<th><fmt:message key="jsp.mydspace.preview-task.version.modified-metadata.old-values" /></th>
					<th><fmt:message key="jsp.mydspace.preview-task.version.modified-metadata.new-values" /></th>
				</thead>
			<%
				if(modifiedMetadata != null) {
				    for(MetadataField md :  modifiedMetadata){
				        String schemaName = MetadataSchema.find(context, md.getSchemaID())
				                .getName();
					String completeField = "metadata." + schemaName + "." +md.getElement()+ (md.getQualifier()!= null? "." + md.getQualifier():""); 
			%>
					<tr>
						<td><fmt:message key="<%= completeField%>" /></td>
						<td>
						<%
				    for(Metadatum m :  previousItem.getMetadata(schemaName, md.getElement(), md.getQualifier(), Item.ANY)) {
				        %><%= m.value %> <%= m.authority != null?"("+m.authority+")":"" %><br /><%
				    } %>
						</td>
						<td>
						<%
				    for(Metadatum m :  item.getMetadata(schemaName, md.getElement(), md.getQualifier(), Item.ANY)) {
				        %><%= m.value %> <%= m.authority != null?"("+m.authority+")":"" %><br /><%
				    } %>
						</td>
					</tr>
					<% } %> 
			<% } %>
		    </table>
		    </div>
		<% } %>
		
		<% if(modifiedFiles != null && modifiedFiles.size() > 0) { %>
		<div id="files" class="well collapse">
		      
		  <table class="table">
		  <thead>
					<th><fmt:message key="jsp.mydspace.preview-task.version.modified-files.title" /></th>
					<th><fmt:message key="jsp.mydspace.preview-task.version.modified-files.details" /></th>
				</thead>
			<%
				for (BitstreamDifferencesDTO bitDiff : modifiedFiles.values()) {
					if (bitDiff.getPrevious() == null) {
					%>
					<tr><td colspan="2">
					<fmt:message key="jsp.mydspace.preview-task.version.modified-files.added">
						<fmt:param><%= request.getContextPath()%>/retrieve/<%= bitDiff.getNewBitstream().getID() %></fmt:param>
						<fmt:param><%= bitDiff.getNewBitstream().getName() %></fmt:param>
					</fmt:message> 
					<% } else if (bitDiff.getNewBitstream() == null) {%>
					<tr><td colspan="2">
					<fmt:message key="jsp.mydspace.preview-task.version.modified-files.removed">
						<fmt:param><%= request.getContextPath()%>/retrieve/<%= bitDiff.getPrevious().getID() %></fmt:param>
						<fmt:param><%= bitDiff.getPrevious().getName() %></fmt:param>
					</fmt:message> 
					<% } else { %>
						<%
							if (bitDiff.getModifiedMetadata().size() > 0) {
								%>
								<tr><td>
								<fmt:message key="jsp.mydspace.preview-task.version.modified-files.modified-metadata">
									<fmt:param><%= request.getContextPath()%>/retrieve/<%= bitDiff.getNewBitstream().getID() %></fmt:param>
									<fmt:param><%= bitDiff.getNewBitstream().getName() %></fmt:param>
								</fmt:message>
								</td>
								<td>
									<table class="table">
									<thead>
										<th><fmt:message key="jsp.mydspace.preview-task.version.modified-files.details.metadata" /></th>
										<th><fmt:message key="jsp.mydspace.preview-task.version.modified-files.details.metadata.old" /></th>
										<th><fmt:message key="jsp.mydspace.preview-task.version.modified-files.details.metadata.new" /></th>
									</thead>
									<%
										for(MetadataField md :  bitDiff.getModifiedMetadata()){
									        String schemaName = MetadataSchema.find(context, md.getSchemaID())
									                .getName();
										String completeField = "metadata.bitstream." + schemaName + "." +md.getElement()+ (md.getQualifier()!= null? "." + md.getQualifier():""); 
						  			%>
										<tr>
											<td><fmt:message key="<%= completeField%>" /></td>
											<td>
											<%
									    for(Metadatum m :  bitDiff.getPrevious().getMetadata(schemaName, md.getElement(), md.getQualifier(), Item.ANY)) {
									        %><%= m.value %> <%= m.authority != null?"("+m.authority+")":"" %><br /><%
									    } %>
											</td>
											<td>
											<%
									    for(Metadatum m :  bitDiff.getNewBitstream().getMetadata(schemaName, md.getElement(), md.getQualifier(), Item.ANY)) {
									        %><%= m.value %> <%= m.authority != null?"("+m.authority+")":"" %><br /><%
									    } %>
										</td>
								<%  } %>
									</tr>
								  </table>
								</td>
							</tr>	  
						<% }
						   if (bitDiff.isPoliciesModified()) { %>
						   <tr><td>
								<fmt:message key="jsp.mydspace.preview-task.version.modified-files.modified-policies">
									<fmt:param><%= request.getContextPath()%>/retrieve/<%= bitDiff.getNewBitstream().getID() %></fmt:param>
									<fmt:param><%= bitDiff.getNewBitstream().getName() %></fmt:param>
								</fmt:message></td>
								<td>
								<table class="table">
								<thead>
									<th><fmt:message key="jsp.mydspace.preview-task.version.modified-files.details.policies.old" /></th>
									<th><fmt:message key="jsp.mydspace.preview-task.version.modified-files.details.policies.new" /></th>
								</thead>
								<tr>
									<td>
									<% 
										List<ResourcePolicy> rpolicies = AuthorizeManager.findPoliciesByDSOAndType(context, 
											bitDiff.getPrevious(), ResourcePolicy.TYPE_CUSTOM); 
										if (rpolicies != null && !rpolicies.isEmpty()) {
											for(ResourcePolicy rpolicy : rpolicies) { 
												if(rpolicy.getStartDate()!=null) {
												%>
													<c:set var="policyStartDate" value="<%= rpolicy.getStartDate() %>" target="java.util.Date"/>
													<fmt:message key="jsp.mydspace.preview-task.version.modified-files.details.policies.embargoed"><fmt:param value="${policyStartDate}"/></fmt:message>				    
												<%
												}
												else { 
												%>
													<fmt:message key="jsp.mydspace.preview-task.version.modified-files.details.policies.openaccess"/>										    
											    <%
												}
											}
										} else {
									%><fmt:message key="jsp.mydspace.preview-task.version.modified-files.details.policies.default" /><%		
										}
									%>	
									</td>
									<td>
									<% 
										rpolicies = AuthorizeManager.findPoliciesByDSOAndType(context, 
											bitDiff.getNewBitstream(), ResourcePolicy.TYPE_CUSTOM);
										if (rpolicies != null && !rpolicies.isEmpty()) {
											for(ResourcePolicy rpolicy : rpolicies) { 
												if(rpolicy.getStartDate()!=null) {
												%>
													<c:set var="policyStartDate" value="<%= rpolicy.getStartDate() %>" target="java.util.Date"/>
													<fmt:message key="jsp.mydspace.preview-task.version.modified-files.details.policies.embargoed"><fmt:param value="${policyStartDate}"/></fmt:message>				    
												<%
												}
												else { 
												%>
													<fmt:message key="jsp.mydspace.preview-task.version.modified-files.details.policies.openaccess"/>														    
											    <%
												}
											}
										} else {
									%><fmt:message key="jsp.mydspace.preview-task.version.modified-files.details.policies.default" /><%		
										}
									%>	
									</td>
								</tr>
								</table>
							</td>
						</tr>		
						<% }
					}
			 } %>
		    </table>
		    </div>
		<% } %>
	<% } %>