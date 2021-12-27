/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.submit.step;

import static java.util.Optional.of;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;

import org.dspace.app.rest.exception.UnprocessableEntityException;
import org.dspace.app.rest.model.patch.Operation;
import org.dspace.app.rest.model.step.CustomUrl;
import org.dspace.app.rest.submit.AbstractProcessingStep;
import org.dspace.app.rest.submit.SubmissionService;
import org.dspace.app.rest.submit.factory.PatchOperationFactory;
import org.dspace.app.rest.submit.factory.impl.PatchOperation;
import org.dspace.app.util.SubmissionStepConfig;
import org.dspace.content.InProgressSubmission;
import org.dspace.content.Item;
import org.dspace.content.MetadataValue;
import org.dspace.core.Context;

/**
 * Implementation of {@link DataProcessingStep} that expose and allow patching
 * the custom defined url.
 *
 * @author Luca Giamminonni (luca.giamminonni at 4science.it)
 *
 */
@SuppressWarnings("rawtypes")
public class CustomUrlStep extends AbstractProcessingStep {


    @Override
    @SuppressWarnings("unchecked")
    public CustomUrl getData(SubmissionService submissionService, InProgressSubmission obj,
        SubmissionStepConfig config) throws Exception {

        Item item = obj.getItem();

        CustomUrl customUrl = new CustomUrl();
        customUrl.setUrl(getUrl(item));
        customUrl.setRedirectedUrls(getRedirectedUrls(item));

        return customUrl;
    }

    @Override
    public void doPatchProcessing(Context context, HttpServletRequest currentRequest, InProgressSubmission source,
        Operation op, SubmissionStepConfig stepConf) throws Exception {

        String path = op.getPath();
        String stepId = stepConf.getId();

        PatchOperation<?> patchOperation = calculatePatchOperation(op)
            .orElseThrow(() -> new UnprocessableEntityException("Path " + path + " not supported by step " + stepId));

        patchOperation.perform(context, currentRequest, source, op);

    }

    private Optional<PatchOperation<?>> calculatePatchOperation(Operation operation) {

        PatchOperationFactory patchOperationFactory = new PatchOperationFactory();
        String operationName = operation.getOp();

        if (operation.getPath().contains("/" + CUSTOM_URL_STEP_URL_OPERATION_ENTRY)) {
            return of(patchOperationFactory.instanceOf(CUSTOM_URL_STEP_URL_OPERATION_ENTRY, operationName));
        }

        if (operation.getPath().contains("/" + CUSTOM_URL_STEP_REDIRECTED_URL_OPERATION_ENTRY)) {
            return of(patchOperationFactory.instanceOf(CUSTOM_URL_STEP_REDIRECTED_URL_OPERATION_ENTRY, operationName));
        }

        return Optional.empty();
    }

    private String getUrl(Item item) {
        return itemService.getMetadataFirstValue(item, "cris", "customurl", null, Item.ANY);
    }

    private List<String> getRedirectedUrls(Item item) {
        return itemService.getMetadataByMetadataString(item, "cris.customurl.old").stream()
            .map(MetadataValue::getValue)
            .collect(Collectors.toList());
    }

}
