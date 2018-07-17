/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.submit.step;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import gr.ekt.bte.core.TransformationEngine;
import gr.ekt.bte.core.TransformationSpec;
import gr.ekt.bte.exceptions.BadTransformationSpec;
import gr.ekt.bte.exceptions.MalformedSourceException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.app.rest.model.ErrorRest;
import org.dspace.app.rest.repository.WorkspaceItemRestRepository;
import org.dspace.app.rest.submit.SubmissionService;
import org.dspace.app.rest.submit.UploadableStep;
import org.dspace.app.rest.utils.Utils;
import org.dspace.app.util.SubmissionConfig;
import org.dspace.app.util.SubmissionConfigReader;
import org.dspace.app.util.SubmissionConfigReaderException;
import org.dspace.app.util.SubmissionStepConfig;
import org.dspace.content.Bundle;
import org.dspace.content.InProgressSubmission;
import org.dspace.content.Item;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.dspace.submit.extraction.MetadataExtractor;
import org.dspace.submit.lookup.ASubmissionLookupDataLoader;
import org.dspace.submit.lookup.DSpaceSingleItemOutputGenerator;
import org.dspace.submit.lookup.SubmissionItemDataLoader;
import org.dspace.submit.lookup.SubmissionLookupOutputGenerator;
import org.dspace.submit.step.ExtractionStep;
import org.dspace.submit.util.ItemSubmissionLookupDTO;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Luigi Andrea Pascarelli (luigiandrea.pascarelli at 4science.it)
 */
public class ExtractMetadataStep extends ExtractionStep implements UploadableStep {

    private static final Logger log = Logger.getLogger(ExtractMetadataStep.class);

    SubmissionConfigReader submissionConfigReader = null;

    public ExtractMetadataStep() throws SubmissionConfigReaderException {
        submissionConfigReader = new SubmissionConfigReader();
    }

    @Override
    public ErrorRest upload(Context context, SubmissionService submissionService, SubmissionStepConfig stepConfig,
            InProgressSubmission wsi, MultipartFile multipartFile, String extraField) throws IOException {

        Item item = wsi.getItem();

        SubmissionConfig submissionConfig = submissionConfigReader.
                getSubmissionConfigByCollection(wsi.getCollection().getHandle());

        boolean continueExtraction = false;

        // if file has just uploading no metadata extraction (only first uploaded file runs an extraction)
        List<Bundle> bundles;
        try {
            bundles = itemService.getBundles(item, Constants.CONTENT_BUNDLE_NAME);
            if (CollectionUtils.isEmpty(bundles)) {
                continueExtraction = true;
            } else {
                for (Bundle bundle : bundles) {
                    if (CollectionUtils.isEmpty(bundle.getBitstreams())) {
                        continueExtraction = true;
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }

        if (continueExtraction) {

            try {
                List<MetadataExtractor> extractors = DSpaceServicesFactory.getInstance().getServiceManager()
                        .getServicesByType(MetadataExtractor.class);
                File file = null;
                for (MetadataExtractor extractor : extractors) {
                    String dataloaderKey = extractor.getDataloadersKeyMap();
                    if (extractor.getExtensions()
                            .contains(FilenameUtils.getExtension(multipartFile.getOriginalFilename()))) {

                        if (file == null) {
                            file = Utils.getFile(multipartFile, "submissionlookup-loader", stepConfig.getId());
                        }

                        List<ItemSubmissionLookupDTO> tmpResult = new ArrayList<ItemSubmissionLookupDTO>();

                        if (StringUtils.isNotBlank(dataloaderKey)) {
                            TransformationEngine transformationEngine1 =
                                    metadataExtractorSubmissionLookupService.getPhase1TransformationEngine();
                            if (transformationEngine1 != null) {
                                ASubmissionLookupDataLoader dataLoader =
                                        (ASubmissionLookupDataLoader) transformationEngine1.getDataLoader();

                                List<String> fileDataLoaders =
                                        metadataExtractorSubmissionLookupService.getFileProviders();
                                for (String fileDataLoader : fileDataLoaders) {

                                    //Check if dataloader is configured
                                    if (!dataloaderKey.equals(fileDataLoader)) {
                                        continue;
                                    }
                                    dataLoader.setFile(file.getAbsolutePath(), fileDataLoader);

                                    try {
                                        SubmissionLookupOutputGenerator outputGenerator =
                                                (SubmissionLookupOutputGenerator) transformationEngine1
                                                .getOutputGenerator();
                                        outputGenerator.setDtoList(new ArrayList<ItemSubmissionLookupDTO>());
                                        log.debug("BTE transformation is about to start!");
                                        transformationEngine1.transform(new TransformationSpec());
                                        log.debug("BTE transformation finished!");
                                        tmpResult.addAll(outputGenerator.getDtoList());
                                        if (!tmpResult.isEmpty()) {
                                            // exit with the results founded on the first data provided
                                            break;
                                        }
                                    } catch (BadTransformationSpec e1) {
                                        log.error(e1.getMessage(), e1);
                                    } catch (MalformedSourceException e1) {
                                        log.error(e1.getMessage(), e1);
                                    }
                                }
                            }

                            //try to enrich item
                            if (!tmpResult.isEmpty()) {
                                TransformationEngine transformationEngine2 = metadataExtractorSubmissionLookupService
                                        .getPhase2TransformationEngine();
                                if (transformationEngine2 != null) {
                                    SubmissionItemDataLoader dataLoader =
                                            (SubmissionItemDataLoader) transformationEngine2
                                            .getDataLoader();
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
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                ErrorRest result = new ErrorRest();
                result.setMessage(e.getMessage());
                result.getPaths()
                        .add("/" + WorkspaceItemRestRepository.OPERATION_PATH_SECTIONS + "/" + stepConfig.getId());
                return result;
            }
        }
        return null;
    }
}
