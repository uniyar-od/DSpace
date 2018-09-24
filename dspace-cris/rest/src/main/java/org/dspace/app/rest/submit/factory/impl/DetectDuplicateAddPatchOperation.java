/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.submit.factory.impl;

import java.util.UUID;

import org.dspace.app.cris.deduplication.model.DuplicateDecisionObjectRest;
import org.dspace.app.cris.deduplication.model.DuplicateDecisionType;
import org.dspace.app.cris.deduplication.utils.DedupUtils;
import org.dspace.app.rest.exception.PatchUnprocessableEntityException;
import org.dspace.app.rest.model.patch.LateObjectEvaluator;
import org.dspace.content.InProgressSubmission;
import org.dspace.content.WorkspaceItem;
import org.dspace.core.Context;
import org.dspace.services.model.Request;
import org.dspace.utils.DSpace;

/**
 * Submission "add" PATCH operation.
 *
 * Path used to add a new value to an <b>existent metadata</b>:
 * "/sections/<:name-of-the-form>/<:metadata>/-"
 *
 * @author Giuseppe Digilio (giuseppe.digilio at 4science.it)
 */
public class DetectDuplicateAddPatchOperation extends AddPatchOperation<DuplicateDecisionObjectRest> {

	@Override
	void add(Context context, Request currentRequest, InProgressSubmission source, String path, Object value)
			throws Exception {
		String[] split = getAbsolutePath(path).split("/");
        if (split.length == 1) {
            throw new IllegalArgumentException(
            		"The specified path is not valid");
        }

        DedupUtils dedupUtils = new DSpace().getServiceManager()
                .getServiceByName("dedupUtils", DedupUtils.class);
        
        DuplicateDecisionObjectRest decisionObject = evaluateSingleObject((LateObjectEvaluator) value);
        UUID currentItemID = source.getItem().getID();
        UUID duplicateItemID = UUID.fromString(split[0]);
        boolean isInWorkflow = !(source instanceof WorkspaceItem);
        String subPath = split[1];
        Integer resourceType = source.getItem().getType();
        
        switch (subPath) {
        	case "submitterDecision": 
        		decisionObject.setType(DuplicateDecisionType.WORKSPACE);
        		break;
        	case "workflowDecision":
        		decisionObject.setType(DuplicateDecisionType.WORKFLOW);
	    		break;
        	case "adminDecision":
        		decisionObject.setType(DuplicateDecisionType.ADMIN);
	    		break;
	    	default:
	    		throw new IllegalArgumentException(String.format("The specified path %s is not valid", subPath));
        }
        
        if (!dedupUtils.validateDecision(decisionObject)) {
        	throw new IllegalArgumentException(String.format("The specified decision %s is not valid", decisionObject.getValue()));
        }
        
        if (!dedupUtils.matchExist(context, currentItemID, duplicateItemID, resourceType, null, isInWorkflow)) {
        	throw new PatchUnprocessableEntityException(String.format("Cannot find any duplicate match relato to Item %s", duplicateItemID));
        }
        // PatchUnprocessableEntityException
        dedupUtils.setDuplicateDecision(context, source.getItem().getID(), duplicateItemID, source.getItem().getType(), decisionObject);
		System.out.println("add patch dedup" + source.getItem().getID());
		System.out.println("add patch dedup" + split);
		System.out.println("add patch dedup" + decisionObject.getValue().toString());
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected Class<DuplicateDecisionObjectRest[]> getArrayClassForEvaluation() {
		return DuplicateDecisionObjectRest[].class;
	}

	@Override
	protected Class<DuplicateDecisionObjectRest> getClassForEvaluation() {
		return DuplicateDecisionObjectRest.class;
	}

}