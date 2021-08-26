/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.submit.step;

import javax.servlet.http.HttpServletRequest;

import org.dspace.app.rest.exception.UnprocessableEntityException;
import org.dspace.app.rest.model.patch.Operation;
import org.dspace.app.rest.model.step.DataReserveDOI;
import org.dspace.app.rest.submit.AbstractProcessingStep;
import org.dspace.app.rest.submit.SubmissionService;
import org.dspace.app.rest.submit.factory.PatchOperationFactory;
import org.dspace.app.rest.submit.factory.impl.PatchOperation;
import org.dspace.app.util.SubmissionStepConfig;
import org.dspace.content.InProgressSubmission;
import org.dspace.core.Context;

/**
 * Reserve DOI step. Expose reserved DOI information about the in progress
 * submission and allow user to request a DOI.
 *
 * @author Andrea Bollini (andrea.bollin at 4science.it)
 */
public class ReserveDOIStep extends AbstractProcessingStep {

    @Override
    public DataReserveDOI getData(SubmissionService submissionService, InProgressSubmission obj,
            SubmissionStepConfig config)
        throws Exception {
        DataReserveDOI result = new DataReserveDOI();
        return result;
    }

    @Override
    public void doPatchProcessing(Context context, HttpServletRequest currentRequest, InProgressSubmission source,
            Operation op, SubmissionStepConfig stepConf) throws Exception {

        if (op.getPath().endsWith(RESERVEDOI_STEP_OPERATION_ENTRY)) {

            PatchOperation<String> patchOperation = new PatchOperationFactory()
                .instanceOf(RESERVEDOI_STEP_OPERATION_ENTRY, op.getOp());
            patchOperation.perform(context, currentRequest, source, op);

        } else {
            throw new UnprocessableEntityException("The path " + op.getPath() + " cannot be patched");
        }
    }
}
