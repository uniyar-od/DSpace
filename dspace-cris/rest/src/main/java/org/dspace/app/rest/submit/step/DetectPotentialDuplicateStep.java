package org.dspace.app.rest.submit.step;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.dspace.app.cris.deduplication.model.DuplicateDecisionType;
import org.dspace.app.cris.deduplication.utils.DedupUtils;
import org.dspace.app.cris.deduplication.utils.DuplicateItemInfo;
import org.dspace.app.rest.converter.ItemConverter;
import org.dspace.app.rest.model.MetadataValueRest;
import org.dspace.app.rest.model.patch.Operation;
import org.dspace.app.rest.model.step.DataDetectDuplicate;
import org.dspace.app.rest.model.step.DuplicateMatch;
import org.dspace.app.rest.submit.AbstractRestProcessingStep;
import org.dspace.app.rest.submit.SubmissionService;
import org.dspace.app.rest.submit.factory.PatchOperationFactory;
import org.dspace.app.rest.submit.factory.impl.PatchOperation;
import org.dspace.app.util.SubmissionStepConfig;
import org.dspace.browse.BrowsableDSpaceObject;
import org.dspace.content.InProgressSubmission;
import org.dspace.content.Item;
import org.dspace.content.WorkspaceItem;
import org.dspace.core.Context;
import org.dspace.services.model.Request;
import org.dspace.submit.AbstractProcessingStep;
import org.dspace.utils.DSpace;

/**
 * Describe step for DSpace Spring Rest. Handle the exposition of metadata own by the in progress submission.
 *
 * @author Giuseppe Digilio (giuseppe.digilio at 4science.it)
 */
public class DetectPotentialDuplicateStep extends AbstractProcessingStep implements AbstractRestProcessingStep {

	public static final String DETECT_DUPLICATE_STEP_ADD_OPERATION_ENTRY = "detectduplicateadd";
	
	ItemConverter converter2 = new DSpace().getServiceManager()
            .getServiceByName("itemConverter", ItemConverter.class);
	
	@Override
    public DataDetectDuplicate getData(Context context, ItemConverter converter, SubmissionService submissionService, InProgressSubmission obj,
                               SubmissionStepConfig config) throws Exception {

        DedupUtils dedupUtils = new DSpace().getServiceManager()
                .getServiceByName("dedupUtils", DedupUtils.class);
        
        UUID itemID = obj.getItem().getID();
        int typeID = obj.getItem().getType();
        boolean check = !(obj instanceof WorkspaceItem);

        System.out.println(obj.getType());
        System.out.println(itemID);
        
        List<DuplicateItemInfo> potentialDuplicates = dedupUtils
                .getDuplicateByIDandType(context, itemID, typeID, check);

        Map<UUID, DuplicateMatch> matches = processPotentialDuplicates(context, converter, itemID, check, potentialDuplicates);
        DataDetectDuplicate result = new DataDetectDuplicate();
        if (matches.size() != 0) {
        	result.setMatches(processPotentialDuplicates(context, converter, itemID, check, potentialDuplicates));
        }
        
        return result;
    }

	private Map<UUID, DuplicateMatch> processPotentialDuplicates(Context context, ItemConverter converter, UUID itemID,
			boolean check, List<DuplicateItemInfo> potentialDuplicates) {
        DedupUtils dedupUtils = new DSpace().getServiceManager()
                .getServiceByName("dedupUtils", DedupUtils.class);
        Map<UUID, DuplicateMatch> matches = new HashMap<UUID, DuplicateMatch>();
        System.out.println("lenght " + potentialDuplicates.toArray().length);
		for (DuplicateItemInfo itemInfo : potentialDuplicates) {
			DuplicateMatch match = new DuplicateMatch();
			BrowsableDSpaceObject duplicateItem = itemInfo.getDuplicateItem();

//			match.setMatchObject(converter2.convert((Item) duplicateItem));
			match.setMatchObject(converter.convert((Item) duplicateItem));
			match.setSubmitterDecision(itemInfo.getDecision(DuplicateDecisionType.WORKSPACE));
			match.setWorkflowDecision(itemInfo.getDecision(DuplicateDecisionType.WORKFLOW));
			
			matches.put((UUID) duplicateItem.getID(), match);

			System.out.println(itemInfo.getDuplicateItemType());
			System.out.println(duplicateItem.getID());
			System.out.println(itemInfo.getDecision(DuplicateDecisionType.WORKSPACE));
//			System.out.println(itemInfo.isNotDuplicate());
//			System.out.println(itemInfo.isRejected());
//			System.out.println(itemInfo.isToFix());
//			System.out.println(itemInfo.getRejectDate());
//				duplicateItem.put("status", status);
//				duplicateItem.get;
//			try {
//				dedupUtils.rejectDups(context, itemID, duplicateItemID, type, true, null, check);
//			} catch (SQLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
		
//        DuplicateMatch match = new DuplicateMatch();
//        match.setSubmitterDecision(DuplicateDecisionType.REJECT);
//        match.setWorkflowDecision(DuplicateDecisionType.VERIFY);
//        
//        List<DuplicateMatch> matches = Arrays.asList(match);
        
        return matches;
	}
	
	@Override
	public void doPreProcessing(Context context, InProgressSubmission wsi) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doPostProcessing(Context context, InProgressSubmission wsi) {
		// TODO Auto-generated method stub
		
	}

    @Override
    public void doPatchProcessing(Context context, Request currentRequest, InProgressSubmission source, Operation op)
        throws Exception {

        PatchOperation<MetadataValueRest> patchOperation =
            new PatchOperationFactory().instanceOf(DETECT_DUPLICATE_STEP_ADD_OPERATION_ENTRY, op.getOp());
        patchOperation.perform(context, currentRequest, source, op);

    }
}