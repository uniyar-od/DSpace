/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.submit.step;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gr.ekt.bte.core.TransformationEngine;
import gr.ekt.bte.core.TransformationSpec;
import gr.ekt.bte.exceptions.BadTransformationSpec;
import gr.ekt.bte.exceptions.MalformedSourceException;

import org.apache.log4j.Logger;
import org.dspace.app.util.SubmissionConfig;
import org.dspace.app.util.SubmissionConfigReader;
import org.dspace.app.util.SubmissionConfigReaderException;
import org.dspace.content.IMetadataValue;
import org.dspace.content.InProgressSubmission;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.core.Utils;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.dspace.submit.AbstractProcessingStep;
import org.dspace.submit.listener.MetadataListener;
import org.dspace.submit.lookup.ASubmissionLookupDataLoader;
import org.dspace.submit.lookup.DSpaceSingleItemOutputGenerator;
import org.dspace.submit.lookup.SubmissionItemDataLoader;
import org.dspace.submit.lookup.SubmissionLookupOutputGenerator;
import org.dspace.submit.util.ItemSubmissionLookupDTO;

/**
 * @author Luigi Andrea Pascarelli (luigiandrea.pascarelli at 4science.it)
 */
public class MetadataStep extends AbstractProcessingStep {
    /**
     * log4j logger
     */
    private static Logger log = Logger.getLogger(MetadataStep.class);

    protected List<MetadataListener> listeners = DSpaceServicesFactory.getInstance().getServiceManager()
            .getServicesByType(MetadataListener.class);

    protected Map<String, List<IMetadataValue>> metadataMap = new HashMap<String, List<IMetadataValue>>();
    private Map<String, Set<String>> results = new HashMap<String, Set<String>>();
    private Map<String, String> mappingIdentifier = new HashMap<String, String>();
    SubmissionConfigReader submissionConfigReader = null;

    public MetadataStep() throws SubmissionConfigReaderException {
        submissionConfigReader = new SubmissionConfigReader();
    }

    @Override
    public void doPreProcessing(Context context, InProgressSubmission wsi) {
        for (MetadataListener listener : listeners) {
            for (String metadata : listener.getMetadata().keySet()) {
                String[] tokenized = Utils.tokenize(metadata);
                List<IMetadataValue> mm = itemService.getMetadata(wsi.getItem(), tokenized[0], tokenized[1],
                        tokenized[2], Item.ANY);
                if (mm != null && !mm.isEmpty()) {
                    metadataMap.put(metadata, mm);
                } else {
                    metadataMap.put(metadata, new ArrayList<IMetadataValue>());
                }
                mappingIdentifier.put(metadata, listener.getMetadata().get(metadata));
            }
        }
    }

    @Override
    public void doPostProcessing(Context context, InProgressSubmission wsi) {
        external: for (String metadata : metadataMap.keySet()) {
            String[] tokenized = Utils.tokenize(metadata);
            List<IMetadataValue> currents = itemService.getMetadata(wsi.getItem(), tokenized[0], tokenized[1],
                    tokenized[2], Item.ANY);
            if (currents != null && !currents.isEmpty()) {
                List<IMetadataValue> olds = metadataMap.get(metadata);
                if (olds.isEmpty()) {
                    process(context, metadata, currents);
                    continue external;
                }
                internal: for (IMetadataValue current : currents) {

                    boolean found = false;
                    for (IMetadataValue old : olds) {
                        if (old.getValue().equals(current.getValue())) {
                            found = true;
                        }
                    }
                    if (!found) {
                        process(context, metadata, current);
                    }
                }
            }
        }

        List<ItemSubmissionLookupDTO> tmpResult = new ArrayList<ItemSubmissionLookupDTO>();
        SubmissionConfig submissionConfig = submissionConfigReader
                .getSubmissionConfigByCollection(wsi.getCollection().getHandle());

        if (!results.isEmpty()) {

            TransformationEngine transformationEngine1 =
                    identifierSubmissionLookupService.getPhase1TransformationEngine();
            if (transformationEngine1 != null) {
                ASubmissionLookupDataLoader dataLoader = (ASubmissionLookupDataLoader) transformationEngine1
                        .getDataLoader();
                dataLoader.setIdentifiers(results);
                try {
                    SubmissionLookupOutputGenerator outputGenerator =
                            (SubmissionLookupOutputGenerator) transformationEngine1
                            .getOutputGenerator();
                    outputGenerator.setDtoList(new ArrayList<ItemSubmissionLookupDTO>());
                    log.debug("BTE transformation is about to start!");
                    transformationEngine1.transform(new TransformationSpec());
                    log.debug("BTE transformation finished!");
                    tmpResult.addAll(outputGenerator.getDtoList());
                } catch (BadTransformationSpec e1) {
                    log.error(e1.getMessage(), e1);
                } catch (MalformedSourceException e1) {
                    log.error(e1.getMessage(), e1);
                }
            }
        }

        //try to enrich item
        if (!tmpResult.isEmpty()) {
            TransformationEngine transformationEngine2 =
                    identifierSubmissionLookupService.getPhase2TransformationEngine();
            if (transformationEngine2 != null) {
                SubmissionItemDataLoader dataLoader = (SubmissionItemDataLoader) transformationEngine2.getDataLoader();
                dataLoader.setDtoList(tmpResult);

                DSpaceSingleItemOutputGenerator outputGenerator =
                        (DSpaceSingleItemOutputGenerator) transformationEngine2
                        .getOutputGenerator();
                outputGenerator.setContext(context);
                outputGenerator.setItem(wsi.getItem());
                outputGenerator.setFormName(submissionConfig.getSubmissionName());

                try {
                    transformationEngine2.transform(new TransformationSpec());
                } catch (BadTransformationSpec e1) {
                    e1.printStackTrace();
                } catch (MalformedSourceException e1) {
                    e1.printStackTrace();
                }
            }
        }

    }

    private void process(Context context, String metadata, List<IMetadataValue> currents) {
        for (IMetadataValue current : currents) {
            process(context, metadata, current);
        }
    }

    private void process(Context context, String metadata, IMetadataValue current) {
        String key = mappingIdentifier.get(metadata);
        Set<String> identifiers = null;
        if (!results.containsKey(key)) {
            identifiers = new HashSet<String>();
        } else {
            identifiers = results.get(key);
        }
        identifiers.add(current.getValue());
        results.put(key, identifiers);
    }
}
