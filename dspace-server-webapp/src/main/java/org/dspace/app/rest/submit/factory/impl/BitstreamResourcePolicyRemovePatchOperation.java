/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.submit.factory.impl;

import java.util.List;
import javax.servlet.http.HttpServletRequest;

import org.dspace.app.rest.model.AccessConditionDTO;
import org.dspace.authorize.ResourcePolicy;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.authorize.service.ResourcePolicyService;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.InProgressSubmission;
import org.dspace.content.Item;
import org.dspace.content.service.BitstreamService;
import org.dspace.content.service.ItemService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Submission "remove" operation to remove resource policies from the Bitstream
 *
 * @author Luigi Andrea Pascarelli (luigiandrea.pascarelli at 4science.it)
 */
public class BitstreamResourcePolicyRemovePatchOperation
             extends RemovePatchOperation<AccessConditionDTO> {

    @Autowired
    ItemService itemService;

    @Autowired
    ResourcePolicyService resourcePolicyService;

    @Autowired
    BitstreamService bitstreamService;

    @Autowired
    AuthorizeService authorizeService;

    @Override
    void remove(Context context, HttpServletRequest currentRequest, InProgressSubmission source, String path,
            Object value) throws Exception {
        // "path" : "/sections/upload/files/0/accessConditions/0"
        // "abspath" : "/files/0/accessConditions/0"
        String[] split = getAbsolutePath(path).split("/");
        String bitstreamIdx = split[1];

        Item item = source.getItem();

        List<Bundle> bundle = itemService.getBundles(item, Constants.CONTENT_BUNDLE_NAME);
        Bitstream bitstream = null;
        for (Bundle bb : bundle) {
            int idx = 0;
            for (Bitstream b : bb.getBitstreams()) {
                if (idx == Integer.parseInt(bitstreamIdx)) {
                    if (split.length == 3) {
                        resourcePolicyService.removePolicies(context, b,
                                                             ResourcePolicy.TYPE_CUSTOM);
                    } else {
                        String rpIdx = split[3];
                        List<ResourcePolicy> policies = resourcePolicyService.find(context, b,
                                                                                   ResourcePolicy.TYPE_CUSTOM);
                        int index = 0;
                        for (ResourcePolicy policy : policies) {
                            int toDelete = Integer.parseInt(rpIdx);
                            if (index == toDelete) {
                                b.getResourcePolicies().remove(policy);
                                resourcePolicyService.delete(context, policy);
                                bitstream = b;
                                break;
                            }
                            index++;
                        }
                    }
                }
                idx++;
            }
        }

        if (bitstream != null && item.isArchived() && noLongerHasCustomPolicies(context, bitstream)) {
            List<ResourcePolicy> defaultCollectionPolicies = authorizeService
                .getPoliciesActionFilter(context, item.getOwningCollection(), Constants.DEFAULT_BITSTREAM_READ);

            itemService.addDefaultPoliciesNotInPlace(context, bitstream, defaultCollectionPolicies);
        }

        if (bitstream != null) {
            bitstreamService.update(context, bitstream);
        }
    }

    private boolean noLongerHasCustomPolicies(Context context, Bitstream bitstream) {
        return bitstream.getResourcePolicies().stream()
            .noneMatch(policy -> ResourcePolicy.TYPE_CUSTOM.equals(policy.getRpType()));
    }

    @Override
    protected Class<AccessConditionDTO[]> getArrayClassForEvaluation() {
        return AccessConditionDTO[].class;
    }

    @Override
    protected Class<AccessConditionDTO> getClassForEvaluation() {
        return AccessConditionDTO.class;
    }
}
