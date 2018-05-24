/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.repository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.dspace.app.rest.SearchRestMethod;
import org.dspace.app.rest.converter.EditItemConverter;
import org.dspace.app.rest.exception.PatchBadRequestException;
import org.dspace.app.rest.model.EditItemRest;
import org.dspace.app.rest.model.ErrorRest;
import org.dspace.app.rest.model.hateoas.EditItemResource;
import org.dspace.app.rest.model.patch.Operation;
import org.dspace.app.rest.model.patch.Patch;
import org.dspace.app.rest.submit.AbstractRestProcessingStep;
import org.dspace.app.rest.submit.SubmissionService;
import org.dspace.app.rest.submit.UploadableStep;
import org.dspace.app.util.SubmissionConfig;
import org.dspace.app.util.SubmissionConfigReader;
import org.dspace.app.util.SubmissionConfigReaderException;
import org.dspace.app.util.SubmissionStepConfig;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.content.EditItem;
import org.dspace.content.service.EditItemService;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.EPersonServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * This is the repository responsible to manage EditItem Rest object
 *
 * @author Luigi Andrea Pascarelli (luigiandrea.pascarelli at 4science.it)
 */

@Component(EditItemRest.CATEGORY + "." + EditItemRest.NAME)
public class EditItemRestRepository extends DSpaceRestRepository<EditItemRest, UUID> {

    public static final String OPERATION_PATH_SECTIONS = "sections";

    private static final Logger log = Logger.getLogger(EditItemRestRepository.class);

    @Autowired
    EditItemService is;

    @Autowired
    EditItemConverter converter;

    @Autowired
    EPersonServiceImpl epersonService;

    @Autowired
    SubmissionService submissionService;

    @Autowired
    AuthorizeService authorizeService;

    private SubmissionConfigReader submissionConfigReader;

    public EditItemRestRepository() throws SubmissionConfigReaderException {
        submissionConfigReader = new SubmissionConfigReader();
    }

    @Override
    public EditItemRest findOne(Context context, UUID id) {
        EditItem editItem = null;
        try {
            editItem = is.find(context, id);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        if (editItem == null) {
            return null;
        }
        return converter.fromModel(editItem);
    }

    @Override
    public Page<EditItemRest> findAll(Context context, Pageable pageable) {
        Iterator<EditItem> it = null;
        List<EditItem> items = new ArrayList<EditItem>();
        int total = 0;
        try {
            total = is.countTotal(context);
            it = is.findAll(context, pageable.getPageSize(), pageable.getOffset());
            while (it.hasNext()) {
                EditItem i = it.next();
                items.add(i);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        Page<EditItemRest> page = new PageImpl<EditItem>(items, pageable, total).map(converter);
        return page;
    }

    @SearchRestMethod(name = "findBySubmitter")
    public Page<EditItemRest> findBySubmitter(@Param(value = "uuid") UUID submitterID, Pageable pageable) {
        List<EditItem> items = null;
        int total = 0;
        try {
            Context context = obtainContext();
            EPerson ep = epersonService.find(context, submitterID);
            items = is.findBySubmitter(context, ep, pageable.getPageSize(), pageable.getOffset());
            total = is.countBySubmitter(context, ep);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        Page<EditItemRest> page = new PageImpl<EditItem>(items, pageable, total).map(converter);
        return page;
    }

    @Override
    public Class<EditItemRest> getDomainClass() {
        return EditItemRest.class;
    }

    @Override
    public EditItemResource wrapResource(EditItemRest item, String... rels) {
        return new EditItemResource(item, utils, rels);
    }

    @Override
    public EditItemRest upload(HttpServletRequest request, String apiCategory, String model, UUID id, String extraField,
                               MultipartFile file) throws Exception {

        Context context = obtainContext();
        EditItem source = is.find(context, id);
        if (!authorizeService.isAdmin(context) && !is.getItemService().canEdit(context, source.getItem())) {
            throw new AuthorizeException("Unauthorized attempt to edit ItemID " + source.getItem().getID());
        }
        context.turnOffAuthorisationSystem();

        EditItemRest wsi = findOne(id);
        List<ErrorRest> errors = new ArrayList<ErrorRest>();
        SubmissionConfig submissionConfig =
            submissionConfigReader.getSubmissionConfigByName(wsi.getSubmissionDefinition().getName());
        for (int i = 0; i < submissionConfig.getNumberOfSteps(); i++) {
            SubmissionStepConfig stepConfig = submissionConfig.getStep(i);

            /*
             * First, load the step processing class (using the current class loader)
             */
            ClassLoader loader = this.getClass().getClassLoader();
            Class stepClass;
            try {
                stepClass = loader.loadClass(stepConfig.getProcessingClassName());

                Object stepInstance = stepClass.newInstance();
                if (UploadableStep.class.isAssignableFrom(stepClass)) {
                    UploadableStep uploadableStep = (UploadableStep) stepInstance;
                    uploadableStep.doPreProcessing(context, source);
                    ErrorRest err =
                        uploadableStep.upload(context, submissionService, stepConfig, source, file, extraField);
                    uploadableStep.doPostProcessing(context, source);
                    if (err != null) {
                        errors.add(err);
                    }
                }

            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

        }
        wsi = converter.convert(source);

        if (errors.isEmpty()) {
            wsi.setStatus(true);
        } else {
            wsi.setStatus(false);
            wsi.getErrors().addAll(errors);
        }

        context.commit();
        return wsi;
    }

    @Override
    public void patch(Context context, HttpServletRequest request, String apiCategory, String model, UUID id,
                      Patch patch) throws SQLException, AuthorizeException {
        List<Operation> operations = patch.getOperations();

        EditItem source = is.find(context, id);
        if (!authorizeService.isAdmin(context) && !is.getItemService().canEdit(context, source.getItem())) {
            throw new AuthorizeException("Unauthorized attempt to edit ItemID " + source.getItem().getID());
        }
        context.turnOffAuthorisationSystem();

        EditItemRest wsi = findOne(id);
        for (Operation op : operations) {
            // the value in the position 0 is a null value
            String[] path = op.getPath().substring(1).split("/", 3);
            if (OPERATION_PATH_SECTIONS.equals(path[0])) {
                String section = path[1];
                evaluatePatch(context, request, source, wsi, section, op);
            } else {
                throw new PatchBadRequestException(
                    "Patch path operation need to starts with '" + OPERATION_PATH_SECTIONS + "'");
            }
        }
        is.update(context, source);
    }

    private void evaluatePatch(Context context, HttpServletRequest request, EditItem source, EditItemRest wsi,
                               String section, Operation op) {
        SubmissionConfig submissionConfig =
            submissionConfigReader.getSubmissionConfigByName(wsi.getSubmissionDefinition().getName());
        for (int stepNum = 0; stepNum < submissionConfig.getNumberOfSteps(); stepNum++) {

            SubmissionStepConfig stepConfig = submissionConfig.getStep(stepNum);

            if (section.equals(stepConfig.getId())) {
                /*
                 * First, load the step processing class (using the current class loader)
                 */
                ClassLoader loader = this.getClass().getClassLoader();
                Class stepClass;
                try {
                    stepClass = loader.loadClass(stepConfig.getProcessingClassName());

                    Object stepInstance = stepClass.newInstance();

                    if (stepInstance instanceof AbstractRestProcessingStep) {
                        // load the JSPStep interface for this step
                        AbstractRestProcessingStep stepProcessing =
                            (AbstractRestProcessingStep) stepClass.newInstance();
                        stepProcessing.doPreProcessing(context, source);
                        stepProcessing.doPatchProcessing(context, getRequestService().getCurrentRequest(), source, op);
                        stepProcessing.doPostProcessing(context, source);
                    } else {
                        throw new PatchBadRequestException(
                            "The submission step class specified by '" + stepConfig.getProcessingClassName() +
                            "' does not extend the class org.dspace.submit.AbstractProcessingStep!" +
                            " Therefore it cannot be used by the Configurable Submission as the <processing-class>!");
                    }

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    @Override
    protected void delete(Context context, UUID id) {
        EditItem source = null;
        try {
            source = is.find(context, id);
            if (!authorizeService.isAdmin(context) && !is.getItemService().canEdit(context, source.getItem())) {
                throw new AuthorizeException("Unauthorized attempt to edit ItemID " + source.getItem().getID());
            }
            context.turnOffAuthorisationSystem();
            is.deleteWrapper(context, source);
        } catch (SQLException | AuthorizeException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}