/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.bulkedit;

import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.split;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;
import static org.dspace.core.CrisConstants.PLACEHOLDER_PARENT_METADATA_VALUE;
import static org.dspace.util.WorkbookUtils.getCellValue;
import static org.dspace.util.WorkbookUtils.getRows;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.cli.ParseException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.dspace.app.bulkimport.exception.BulkImportException;
import org.dspace.app.bulkimport.model.EntityRow;
import org.dspace.app.bulkimport.model.ImportAction;
import org.dspace.app.bulkimport.model.MetadataGroup;
import org.dspace.app.bulkimport.model.UploadDetails;
import org.dspace.app.bulkimport.util.BulkImportFileUtil;
import org.dspace.app.util.DCInputsReader;
import org.dspace.app.util.DCInputsReaderException;
import org.dspace.authority.service.ItemSearchService;
import org.dspace.authority.service.ItemSearcherMapper;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.factory.AuthorizeServiceFactory;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.content.Bitstream;
import org.dspace.content.BitstreamFormat;
import org.dspace.content.Bundle;
import org.dspace.content.Collection;
import org.dspace.content.InProgressSubmission;
import org.dspace.content.Item;
import org.dspace.content.MetadataField;
import org.dspace.content.MetadataValue;
import org.dspace.content.WorkspaceItem;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.packager.PackageUtils;
import org.dspace.content.service.BitstreamFormatService;
import org.dspace.content.service.BitstreamService;
import org.dspace.content.service.BundleService;
import org.dspace.content.service.CollectionService;
import org.dspace.content.service.InstallItemService;
import org.dspace.content.service.ItemService;
import org.dspace.content.service.MetadataFieldService;
import org.dspace.content.service.WorkspaceItemService;
import org.dspace.content.vo.MetadataValueVO;
import org.dspace.core.Context;
import org.dspace.core.exception.SQLRuntimeException;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.factory.EPersonServiceFactory;
import org.dspace.scripts.DSpaceRunnable;
import org.dspace.util.UUIDUtils;
import org.dspace.util.WorkbookUtils;
import org.dspace.utils.DSpace;
import org.dspace.validation.service.ValidationService;
import org.dspace.validation.service.factory.ValidationServiceFactory;
import org.dspace.workflow.WorkflowException;
import org.dspace.workflow.WorkflowItemService;
import org.dspace.workflow.WorkflowService;
import org.dspace.workflow.factory.WorkflowServiceFactory;
import org.dspace.xmlworkflow.storedcomponents.XmlWorkflowItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link DSpaceRunnable} to perfom a bulk import via excel
 * file.
 *
 * @author Luca Giamminonni (luca.giamminonni at 4science.it)
 *
 */
public class BulkImport extends DSpaceRunnable<BulkImportScriptConfiguration<BulkImport>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BulkImport.class);

    public static final String AUTHORITY_SEPARATOR = "$$";

    public static final String METADATA_SEPARATOR = "||";

    public static final String LANGUAGE_SEPARATOR_PREFIX = "[";

    public static final String LANGUAGE_SEPARATOR_SUFFIX = "]";

    public static final String ID_CELL = "ID";

    public static final String PARENT_ID_CELL = "PARENT-ID";

    public static final String ROW_ID = "ROW-ID";

    public static final String ID_SEPARATOR = "::";


    private static final int ID_CELL_INDEX = 0;

    private static final int ACTION_CELL_INDEX = 1;

    private static final String ACTION_CELL = "ACTION";

    private static final String BITSTREAM_METADATA = "bitstream-metadata";

    private static final int FILE_PATH_INDEX = 1;

    private static final String FILE_PATH_CELL = "FILE-PATH";

    private static final String ORIGINAL_BUNDLE = "ORIGINAL";

    private CollectionService collectionService;

    private ItemService itemService;

    private MetadataFieldService metadataFieldService;

    private WorkspaceItemService workspaceItemService;

    private WorkflowItemService<?> workflowItemService;

    private InstallItemService installItemService;

    private WorkflowService<XmlWorkflowItem> workflowService;

    private ItemSearcherMapper itemSearcherMapper;

    private ItemSearchService itemSearchService;

    private AuthorizeService authorizeService;

    private ValidationService validationService;

    private DCInputsReader reader;

    private BulkImportTransformerService bulkImportTransformerService;

    private String collectionId;

    private String filename;

    private boolean abortOnError;

    private Context context;

    private BulkImportFileUtil bulkImportFileUtil;

    private BundleService bundleService;

    private BitstreamService bitstreamService;

    private BitstreamFormatService bitstreamFormatService;

    @Override
    @SuppressWarnings("unchecked")
    public void setup() throws ParseException {

        this.collectionService = ContentServiceFactory.getInstance().getCollectionService();
        this.itemService = ContentServiceFactory.getInstance().getItemService();
        this.metadataFieldService = ContentServiceFactory.getInstance().getMetadataFieldService();
        this.workspaceItemService = ContentServiceFactory.getInstance().getWorkspaceItemService();
        this.installItemService = ContentServiceFactory.getInstance().getInstallItemService();
        this.workflowService = WorkflowServiceFactory.getInstance().getWorkflowService();
        this.authorizeService = AuthorizeServiceFactory.getInstance().getAuthorizeService();
        this.itemSearcherMapper = new DSpace().getSingletonService(ItemSearcherMapper.class);
        this.itemSearchService = new DSpace().getSingletonService(ItemSearchService.class);
        this.validationService = ValidationServiceFactory.getInstance().getValidationService();
        this.workflowItemService = WorkflowServiceFactory.getInstance().getWorkflowItemService();
        this.bulkImportTransformerService = new DSpace().getServiceManager().getServiceByName(
               BulkImportTransformerService.class.getName(), BulkImportTransformerService.class);
        this.bulkImportFileUtil = new BulkImportFileUtil(this.handler);
        this.bundleService = ContentServiceFactory.getInstance().getBundleService();
        this.bitstreamService = ContentServiceFactory.getInstance().getBitstreamService();
        this.bitstreamFormatService = ContentServiceFactory.getInstance().getBitstreamFormatService();

        try {
            this.reader = new DCInputsReader();
        } catch (DCInputsReaderException e) {
            throw new RuntimeException(e);
        }

        collectionId = commandLine.getOptionValue('c');
        filename = commandLine.getOptionValue('f');

        if (commandLine.hasOption('e')) {
            abortOnError = true;
        }
    }

    @Override
    public void internalRun() throws Exception {
        context = new Context(Context.Mode.BATCH_EDIT);
        assignCurrentUserInContext();
        assignSpecialGroupsInContext();

        context.turnOffAuthorisationSystem();

        InputStream inputStream = handler.getFileStream(context, filename)
            .orElseThrow(() -> new IllegalArgumentException("Error reading file, the file couldn't be "
                + "found for filename: " + filename));

        Collection collection = getCollection();
        if (collection == null) {
            throw new IllegalArgumentException("No collection found with id " + collectionId);
        }

        if (!this.authorizeService.isAdmin(context, collection)) {
            throw new IllegalArgumentException("The user is not an admin of the given collection");
        }

        try {
            performImport(inputStream);
            context.complete();
            context.restoreAuthSystemState();
        } catch (Exception e) {
            handler.handleException(e);
            context.abort();
        }
    }

    public void performImport(InputStream is) {
        Workbook workbook = createWorkbook(is);
        validateWorkbook(workbook);
        List<EntityRow> entityRows = getValidEntityRows(workbook);
        performImport(entityRows);
    }

    private Workbook createWorkbook(InputStream is) {
        try {
            return WorkbookFactory.create(is);
        } catch (EncryptedDocumentException | IOException e) {
            throw new BulkImportException("An error occurs during the workbook creation", e);
        }
    }

    private void validateWorkbook(Workbook workbook) {
        if (workbook.getNumberOfSheets() == 0) {
            throw new BulkImportException("The Workbook should have at least one sheet");
        }

        List<String> groups = getSubmissionFormMetadataGroups();

        for (Sheet sheet : workbook) {
            String name = sheet.getSheetName();

            if (isEntityUploadSheet(sheet)) {
                validateUploadSheet(sheet);
                continue;
            }

            if (WorkbookUtils.isSheetEmpty(sheet)) {
                throw new BulkImportException("The sheet " + name + " of the Workbook is empty");
            }

            if (WorkbookUtils.isRowEmpty(sheet.getRow(0))) {
                throw new BulkImportException("The header of sheet " + name + " of the Workbook is empty");
            }

            if (!isEntityRowSheet(sheet) && !groups.contains(name)) {
                throw new BulkImportException("The sheet name " + name + " is not a valid metadata group");
            }

            validateHeaders(sheet);
        }
    }

    private void validateUploadSheet(Sheet sheet) {
        String sheetName = sheet.getSheetName();
        List<String> headers = WorkbookUtils.getAllHeaders(sheet);

        if (headers.size() < 2) {
            throw new BulkImportException("At least the parent ID and file path are required for the upload sheet.");
        }

        String id = headers.get(ID_CELL_INDEX);
        if (!PARENT_ID_CELL.equals(id)) {
            throw new BulkImportException("Wrong " + PARENT_ID_CELL + " header on sheet " + sheetName + ": " + id);
        }

        String filePath = headers.get(FILE_PATH_INDEX);
        if (!FILE_PATH_CELL.equals(filePath)) {
            throw new BulkImportException("Wrong " + FILE_PATH_CELL + " header on sheet " + sheetName + ": " + id);
        }

        List<String> invalidMetadataMessages = new ArrayList<>();

        try {
            List<String> uploadMetadata = this.reader.getUploadMetadataFieldsFromCollection(getCollection());
            List<String> metadataFields = headers.subList(3, headers.size()); // Upload sheet metadata starts at index 3

            for (String metadataField : metadataFields) {
                String metadata = getMetadataField(metadataField);

                if (StringUtils.isBlank(metadata)) {
                    invalidMetadataMessages.add("Empty metadata");
                    continue;
                }

                if (!uploadMetadata.contains(metadata)) {
                    invalidMetadataMessages.add(metadata + " is not valid for the given collection");
                    continue;
                }

                if (metadataFieldService.findByString(context, metadata, '.') == null) {
                    invalidMetadataMessages.add(metadata + " not found");
                }
            }
        } catch (Exception e) {
            handler.logError(ExceptionUtils.getRootCauseMessage(e));
        }

        if (CollectionUtils.isNotEmpty(invalidMetadataMessages)) {
            throw new BulkImportException("The following metadata fields of the sheet named '" + sheetName
                                              + "' are invalid:" + invalidMetadataMessages);
        }
    }

    private void validateHeaders(Sheet sheet) {
        List<String> headers = WorkbookUtils.getAllHeaders(sheet);
        validateMainHeaders(sheet, headers);
        validateMetadataFields(sheet, headers);
    }

    private void validateMainHeaders(Sheet sheet, List<String> headers) {
        String sheetName = sheet.getSheetName();
        boolean isEntityRowSheet = isEntityRowSheet(sheet);

        if (isEntityRowSheet && headers.size() < 2) {
            throw new BulkImportException("At least the columns ID and ACTION are required for the entity sheet");
        }

        if (isEntityRowSheet) {
            String id = headers.get(ID_CELL_INDEX);
            if (!ID_CELL.equals(id)) {
                throw new BulkImportException("Wrong " + ID_CELL + " header on sheet " + sheetName + ": " + id);
            }
            String action = headers.get(ACTION_CELL_INDEX);
            if (!ACTION_CELL.equals(action)) {
                throw new BulkImportException("Wrong " + ACTION_CELL + " header on sheet " + sheetName + ": " + action);
            }
        } else {
            String id = headers.get(ID_CELL_INDEX);
            if (!PARENT_ID_CELL.equals(id)) {
                throw new BulkImportException("Wrong " + PARENT_ID_CELL + " header on sheet " + sheetName + ": " + id);
            }
        }
    }

    private void validateMetadataFields(Sheet sheet, List<String> headers) {
        String sheetName = sheet.getSheetName();
        boolean isEntityRowSheet = isEntityRowSheet(sheet);

        List<String> metadataFields = headers.subList(getFirstMetadataIndex(sheet), headers.size());
        List<String> invalidMetadataMessages = new ArrayList<>();

        List<String> submissionMetadata = isEntityRowSheet ? getSubmissionFormMetadata()
            : getSubmissionFormMetadataGroup(sheetName);

        for (String metadataField : metadataFields) {

            String metadata = getMetadataField(metadataField);

            if (StringUtils.isBlank(metadata)) {
                invalidMetadataMessages.add("Empty metadata");
                continue;
            }

            if (!submissionMetadata.contains(metadata)) {
                invalidMetadataMessages.add(metadata + " is not valid for the given collection");
                continue;
            }

            try {
                if (metadataFieldService.findByString(context, metadata, '.') == null) {
                    invalidMetadataMessages.add(metadata + " not found");
                }
            } catch (SQLException e) {
                handler.logError(ExceptionUtils.getRootCauseMessage(e));
                invalidMetadataMessages.add(metadata);
            }

        }

        if (CollectionUtils.isNotEmpty(invalidMetadataMessages)) {
            throw new BulkImportException("The following metadata fields of the sheet named '" + sheetName
                + "' are invalid:" + invalidMetadataMessages);
        }
    }

    private List<String> getSubmissionFormMetadataGroup(String groupName) {
        try {
            return this.reader.getAllNestedMetadataByGroupName(getCollection(), groupName);
        } catch (DCInputsReaderException e) {
            throw new BulkImportException("An error occurs reading the input configuration "
                + "by group name " + groupName, e);
        }
    }

    private List<String> getSubmissionFormMetadata() {
        try {
            return this.reader.getSubmissionFormMetadata(getCollection());
        } catch (DCInputsReaderException e) {
            throw new BulkImportException("An error occurs reading the input configuration by collection", e);
        }
    }

    private List<String> getSubmissionFormMetadataGroups() {
        try {
            return this.reader.getSubmissionFormMetadataGroups(getCollection());
        } catch (DCInputsReaderException e) {
            throw new BulkImportException("An error occurs reading the input configuration by collection", e);
        }
    }

    private List<EntityRow> getValidEntityRows(Workbook workbook) {
        Sheet entityRowSheet = workbook.getSheetAt(0);
        Map<String, Integer> headers = getHeaderMap(entityRowSheet);

        List<Sheet> metadataGroupSheets = getAllMetadataGroupSheets(workbook);

        handler.logInfo("Start reading all the metadata group rows");
        List<MetadataGroup> metadataGroups = getValidMetadataGroups(metadataGroupSheets);
        List<UploadDetails> uploadDetails = getUploadDetails(workbook);
        handler.logInfo("Found " + metadataGroups.size() + " metadata groups to process");

        return WorkbookUtils.getRows(entityRowSheet)
            .filter(WorkbookUtils::isNotFirstRow)
            .filter(WorkbookUtils::isNotEmptyRow)
            .filter(this::isEntityRowRowValid)
            .map(row -> buildEntityRow(row, headers, metadataGroups, uploadDetails))
            .collect(Collectors.toList());
    }

    private List<UploadDetails> getUploadDetails(Workbook workbook) {
        Sheet uploadSheet = workbook.getSheet(BITSTREAM_METADATA);

        if (uploadSheet == null) {
            return Collections.emptyList();
        }

        final List<MetadataGroup> metadataGroups = getValidMetadataGroup(uploadSheet, false)
            .collect(Collectors.toList());

        return getRows(uploadSheet)
            .filter(WorkbookUtils::isNotFirstRow)
            .filter(WorkbookUtils::isNotEmptyRow)
            .filter(this::isUploadRowValid)
            .map(row -> buildUploadDetails(row, metadataGroups))
            .collect(Collectors.toList());
    }

    private boolean isUploadRowValid(Row row) {
        return !(StringUtils.isEmpty(getCellValue(row.getCell(0))) ||
        StringUtils.isEmpty(getCellValue(row.getCell(1))));
    }

    private UploadDetails buildUploadDetails(Row row, List<MetadataGroup> metadataGroups) {
        return new UploadDetails(getCellValue(row.getCell(0)), getCellValue(row.getCell(1)),
                                 getCellValue(row.getCell(2)), metadataGroups.get(row.getRowNum() - 1));
    }

    private List<Sheet> getAllMetadataGroupSheets(Workbook workbook) {
        return StreamSupport.stream(workbook.spliterator(), false).skip(1)
            .collect(Collectors.toList());
    }

    /**
     * Read all the metadata groups from all the given sheets.
     *
     * @param metadataGroupSheets the metadata group sheets to read
     * @return a list of MetadataGroup
     */
    private List<MetadataGroup> getValidMetadataGroups(List<Sheet> metadataGroupSheets) {
        return metadataGroupSheets.stream()
            .flatMap(this::getValidMetadataGroups)
            .collect(Collectors.toList());
    }

    /**
     * Read all the metadata groups from a single sheet.
     *
     * @param metadataGroupSheet the metadata group sheet
     * @return a stream of MetadataGroup
     */
    private Stream<MetadataGroup> getValidMetadataGroups(Sheet metadataGroupSheet) {
        return getValidMetadataGroup(metadataGroupSheet, true);
    }

    private Stream<MetadataGroup> getValidMetadataGroup(Sheet metadataGroupSheet,
                                                        boolean filterUploadMtdGroup) {
        Map<String, Integer> headers = getHeaderMap(metadataGroupSheet);

        Stream<MetadataGroup> metadataGroupStream = WorkbookUtils.getRows(metadataGroupSheet)
            .filter(WorkbookUtils::isNotFirstRow)
            .filter(WorkbookUtils::isNotEmptyRow)
            .filter(this::isMetadataGroupRowValid)
            .map(row -> buildMetadataGroup(row, headers));

        if (filterUploadMtdGroup) {
            return metadataGroupStream
                .filter(metadataGroup -> !isUploadMetadataGroup(metadataGroup.getName()));
        }

        return metadataGroupStream;
    }

    private Map<String, Integer> getHeaderMap(Sheet sheet) {
        return WorkbookUtils.getCells(sheet.getRow(0))
            .filter(cell -> StringUtils.isNotBlank(getCellValue(cell)))
            .collect(toMap(cell -> getCellValue(cell), cell -> cell.getColumnIndex(), handleDuplication(sheet)));
    }

    private BinaryOperator<Integer> handleDuplication(Sheet sheet) {
        return (i1, i2) -> {
            throw new BulkImportException("Sheet " + sheet.getSheetName() + " - Duplicated headers found on cells "
                + (i1 + 1) + " and " + (i2 + 1));
        };
    }

    private MetadataGroup buildMetadataGroup(Row row, Map<String, Integer> headers) {
        String parentId = getIdFromRow(row);
        MultiValuedMap<String, MetadataValueVO> metadata = getMetadataFromRow(row, headers);
        return new MetadataGroup(parentId, row.getSheet().getSheetName(), metadata);
    }

    private EntityRow buildEntityRow(Row row, Map<String, Integer> headers, List<MetadataGroup> metadataGroups,
                                     List<UploadDetails> uploadDetails) {
        String id = getIdFromRow(row);
        String action = getActionFromRow(row);
        MultiValuedMap<String, MetadataValueVO> metadata = getMetadataFromRow(row, headers);
        List<MetadataGroup> ownMetadataGroup = getOwnMetadataGroups(row, metadataGroups);
        List<UploadDetails> ownUploadDetails = getOwnUploadDetails(row, uploadDetails);

        return new EntityRow(id, action, row.getRowNum(), metadata, ownMetadataGroup, ownUploadDetails);
    }

    private void performImport(List<EntityRow> entityRows) {
        handler.logInfo("Found " + entityRows.size() + " items to process");
        entityRows.forEach(entityRow -> performImport(entityRow));
    }

    private void performImport(EntityRow entityRow) {

        try {

            Item item = null;

            switch (entityRow.getAction()) {
                case ADD:
                case ADD_ARCHIVE:
                case ADD_WORKSPACE:
                    item = addItem(entityRow);
                    break;
                case UPDATE:
                case UPDATE_WORKFLOW:
                case UPDATE_ARCHIVE:
                    item = updateItem(entityRow);
                    break;
                case DELETE:
                    deleteItem(entityRow);
                    break;
                case NOT_SPECIFIED:
                default:
                    item = addOrUpdateItem(entityRow);
                    break;
            }

            if (item != null) {
                context.uncacheEntity(item);
            }

            context.commit();

        } catch (BulkImportException bie) {
            handleException(entityRow, bie);
        } catch (Exception e) {
            LOGGER.error("An unexpected error occurs during the import", e);
            handleException(entityRow, new BulkImportException(e));
        }

    }

    private Item addItem(EntityRow entityRow)
        throws AuthorizeException, SQLException, IOException, WorkflowException {

        WorkspaceItem workspaceItem = workspaceItemService.create(context, getCollection(), false);

        Item item = workspaceItem.getItem();

        PackageUtils.addDepositLicense(context, null, item, workspaceItem.getCollection());

        addMetadata(item, entityRow, false);
        addUploadsToItem(item, entityRow);

        String itemId = item.getID().toString();
        int row = entityRow.getRow();

        switch (entityRow.getAction()) {
            case ADD:
                startWorkflow(entityRow, workspaceItem);
                break;
            case ADD_ARCHIVE:
                installItem(entityRow, workspaceItem);
                break;
            case ADD_WORKSPACE:
                handler.logInfo("Row " + row + " - WorkspaceItem created successfully - ID: " + itemId);
                break;
            default:
                break;
        }

        return item;

    }

    private void addUploadsToItem(Item item, EntityRow entityRow) {
        boolean hasFilesForUpload = entityRow.getUploadDetails().size() != 0;
        if (hasFilesForUpload) {
            List<UploadDetails> uploadDetails = entityRow.getUploadDetails();
            uploadDetails.forEach(u -> {
                Optional<Bundle> optionalBundle = getBundleOfUpload(item, u);
                Optional<InputStream> optionalInputStream = bulkImportFileUtil.getInputStream(u.getFilePath());
                if (optionalBundle.isPresent() && optionalInputStream.isPresent()) {
                    Optional<Bitstream> bitstream =
                        createBitstream(optionalBundle.get(), optionalInputStream.get());
                    bitstream.ifPresent(value -> {
                        addMetadataToBitstream(value, u.getMetadataGroup());
                        setBitstreamFormat(value);
                    });
                } else {
                    handler.logError("Cannot create bundle or input stream for " +
                                         "bundle: " + u.getBundleName() + " with path: " + u.getFilePath() +
                                         " and parent id: " + u.getParentId());
                }
            });
        }
    }

    private void addMetadataToBitstream(Bitstream bitstream, MetadataGroup metadataGroup) {
        for (Map.Entry<String, MetadataValueVO> entry : metadataGroup.getMetadata().entries()) {
            Optional<MetadataField> metadataField = getMetadataFieldByString(entry.getKey());
            metadataField.ifPresent(field -> addMetadataToBitstream(bitstream, field, entry.getValue()));
        }
    }

    private void setBitstreamFormat(Bitstream bitstream) {
        try {
            BitstreamFormat bf = bitstreamFormatService.guessFormat(context, bitstream);
            bitstreamService.setFormat(context, bitstream, bf);
            bitstreamService.update(context, bitstream);
        } catch (SQLException | AuthorizeException e) {
            handler.logError(e.getMessage());
        }
    }

    private Optional<MetadataField> getMetadataFieldByString(String metadata) {
        try {
            return Optional.of(metadataFieldService.findByString(context, metadata, '.'));
        } catch (Exception e) {
            handler.logError(e.getMessage());
        }

        return Optional.ofNullable(null);
    }

    private void addMetadataToBitstream(Bitstream bitstream, MetadataField metadataField, MetadataValueVO metadata) {
        try {
            bitstreamService.addMetadata(context, bitstream, metadataField, "", metadata.getValue(),
                                         metadata.getAuthority(), metadata.getConfidence());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Optional<Bundle> getBundleOfUpload(Item item, UploadDetails uploadDetails) {
        String bundleName = uploadDetails.getBundleName();
        // If Bundle is not specified
        if (StringUtils.isEmpty(bundleName)) {
            return createBundle(item, ORIGINAL_BUNDLE);
        } else {
            // If item already has the bundle specified
            if (item.getBundles(bundleName).size() > 0) {
                return Optional.of(item.getBundles(bundleName).get(0));
            } else {
                // Create new bundle as specified in file
                return createBundle(item, bundleName);
            }
        }
    }

    private Optional<Bundle> createBundle(Item item, String bundleName) {
        try {
            return Optional.of(bundleService.create(context, item, bundleName));
        } catch (Exception e) {
            handler.logError("Cannot create bundle: [" + bundleName + "]" +
                                 "\n" + e.getMessage());
        }

        return Optional.ofNullable(null);
    }

    private Optional<Bitstream> createBitstream(Bundle bundle, InputStream inputStream) {
        try {
            return Optional.of(bitstreamService.create(context, bundle, inputStream));
        } catch (Exception e) {
            handler.logError("Cannot create bitstream.\n" + e.getMessage());
        }

        return Optional.ofNullable(null);
    }

    private void installItem(EntityRow entityRow, InProgressSubmission<?> inProgressItem)
        throws SQLException, AuthorizeException {

        String itemId = inProgressItem.getItem().getID().toString();
        int row = entityRow.getRow();

        if (authorizeService.isAdmin(context)) {
            installItemService.installItem(context, inProgressItem);
            handler.logInfo("Row " + row + " - Item archived successfully - ID: " + itemId);
        } else {
            handler.logWarning("Row " + row + " - Current user can't deposit an item directly bypassing the workflow");
        }

    }

    private void startWorkflow(EntityRow entityRow, WorkspaceItem workspaceItem)
        throws SQLException, AuthorizeException, IOException, WorkflowException {

        String itemId = workspaceItem.getItem().getID().toString();
        int row = entityRow.getRow();

        List<String> validationErrors = validateItem(workspaceItem);
        if (CollectionUtils.isEmpty(validationErrors)) {
            workflowService.start(context, workspaceItem);
            handler.logInfo("Row " + row + " - WorkflowItem created successfully - ID: " + itemId);
        } else {
            handler.logWarning("Row " + row + " - Invalid item left in workspace - ID: " + itemId
                + " - validation errors: " + validationErrors);
        }

    }

    private Item updateItem(EntityRow entityRow) throws Exception {
        Item item = findItem(entityRow);
        if (item == null) {
            throw new BulkImportException("No item to update found for entity with id " + entityRow.getId());
        }

        return updateItem(entityRow, item);
    }

    private Item updateItem(EntityRow entityRow, Item item)
        throws SQLException, AuthorizeException, IOException, WorkflowException {

        if (!isInSpecifiedCollection(item)) {
            throw new BulkImportException("The item related to the entity with id " + entityRow.getId()
                + " have a different collection");
        }

        addMetadata(item, entityRow, true);
        addUploadsToItem(item, entityRow);

        handler.logInfo("Row " + entityRow.getRow() + " - Item updated successfully - ID: " + item.getID());

        switch (entityRow.getAction()) {
            case UPDATE_WORKFLOW:
                startWorkflow(entityRow, item);
                break;
            case UPDATE_ARCHIVE:
                installItem(entityRow, item);
                break;
            default:
                break;
        }

        return item;

    }

    private void installItem(EntityRow entityRow, Item item) throws SQLException, AuthorizeException {

        InProgressSubmission<Integer> inProgressItem = findInProgressSubmission(item);
        if (inProgressItem != null) {
            installItem(entityRow, inProgressItem);
        } else {
            handler.logInfo("Row " + entityRow.getRow() + " - No workspace/workflow item to archive found");
        }

    }

    private void startWorkflow(EntityRow entityRow, Item item)
        throws SQLException, AuthorizeException, IOException, WorkflowException {

        WorkspaceItem workspaceItem = workspaceItemService.findByItem(context, item);
        if (workspaceItem != null) {
            startWorkflow(entityRow, workspaceItem);
        } else {
            handler.logInfo("Row " + entityRow.getRow() + " - No workspace item to start found");
        }
    }

    private void deleteItem(EntityRow entityRow) throws Exception {

        Item item = findItem(entityRow);
        if (item == null) {
            throw new BulkImportException("No item to delete found for entity with id " + entityRow.getId());
        }

        itemService.delete(context, item);
        handler.logInfo("Row " + entityRow.getRow() + " - Item deleted successfully");
    }

    private Item addOrUpdateItem(EntityRow entityRow) throws Exception {

        Item item = findItem(entityRow);
        if (item == null) {
            return addItem(entityRow);
        } else {
            return updateItem(entityRow, item);
        }

    }

    private Item findItem(EntityRow entityRow) throws Exception {
        return entityRow.getId() != null ? itemSearchService.search(context, entityRow.getId()) : null;
    }

    private List<String> validateItem(WorkspaceItem workspaceItem) {
        return validationService.validate(context, workspaceItem).stream()
            .map(error -> error.getMessage() + ": " + error.getPaths())
            .collect(Collectors.toList());
    }

    private void addMetadata(Item item, EntityRow entityRow, boolean replace) throws SQLException {

        if (replace) {
            removeMetadata(item, entityRow);
        }

        addMetadata(item, entityRow.getMetadata());

        List<MetadataGroup> metadataGroups = entityRow.getMetadataGroups();
        for (MetadataGroup metadataGroup : metadataGroups) {
            addMetadata(item, metadataGroup.getMetadata());
        }

    }

    private void addMetadata(Item item, MultiValuedMap<String, MetadataValueVO> metadata)
        throws SQLException {

        Iterable<String> metadataFields = metadata.keySet();
        for (String field : metadataFields) {
            String language = getMetadataLanguage(field);
            MetadataField metadataField = metadataFieldService.findByString(context, getMetadataField(field), '.');
            for (MetadataValueVO metadataValue : metadata.get(field)) {
                metadataValue = bulkImportTransformerService.converter(context, field, metadataValue);
                String authority = metadataValue.getAuthority();
                int confidence = metadataValue.getConfidence();
                String value = metadataValue.getValue();
                if (StringUtils.isNotEmpty(value)) {
                    itemService.addMetadata(context, item, metadataField, language, value, authority, confidence);
                }
            }
        }

    }

    private void removeMetadata(Item item, EntityRow entityRow) throws SQLException {

        removeMetadata(item, entityRow.getMetadata());

        List<MetadataGroup> metadataGroups = entityRow.getMetadataGroups();
        for (MetadataGroup metadataGroup : metadataGroups) {
            removeMetadata(item, metadataGroup.getMetadata());
        }
    }

    private void removeMetadata(Item item, MultiValuedMap<String, MetadataValueVO> metadata)
        throws SQLException {

        Iterable<String> fields = metadata.keySet();
        for (String field : fields) {
            String language = getMetadataLanguage(field);
            MetadataField metadataField = metadataFieldService.findByString(context, getMetadataField(field), '.');
            removeSingleMetadata(item, metadataField, language);
        }

    }

    private void removeSingleMetadata(Item item, MetadataField metadataField, String language)
        throws SQLException {
        List<MetadataValue> metadata = itemService.getMetadata(item, metadataField.getMetadataSchema().getName(),
            metadataField.getElement(), metadataField.getQualifier(), language);
        itemService.removeMetadataValues(context, item, metadata);
    }

    private String getMetadataField(String field) {
        return field.contains(LANGUAGE_SEPARATOR_PREFIX) ? split(field, LANGUAGE_SEPARATOR_PREFIX)[0] : field;
    }

    private String getMetadataLanguage(String field) {
        if (field.contains(LANGUAGE_SEPARATOR_PREFIX)) {
            return split(field, LANGUAGE_SEPARATOR_PREFIX)[1].replace(LANGUAGE_SEPARATOR_SUFFIX, "");
        }
        return null;
    }

    private String getIdFromRow(Row row) {
        return WorkbookUtils.getCellValue(row, ID_CELL_INDEX);
    }

    private String getActionFromRow(Row row) {
        return WorkbookUtils.getCellValue(row, ACTION_CELL_INDEX);
    }

    private MultiValuedMap<String, MetadataValueVO> getMetadataFromRow(Row row, Map<String, Integer> headers) {

        MultiValuedMap<String, MetadataValueVO> metadata = new ArrayListValuedHashMap<String, MetadataValueVO>();

        int firstMetadataIndex = getFirstMetadataIndex(row.getSheet());
        boolean isEntityRowSheet = isEntityRowSheet(row.getSheet());

        for (String header : headers.keySet()) {
            int index = headers.get(header);
            if (index >= firstMetadataIndex) {

                String cellValue = WorkbookUtils.getCellValue(row, index);
                String[] values = isNotBlank(cellValue) ? split(cellValue, METADATA_SEPARATOR) : new String[] { "" };

                List<MetadataValueVO> metadataValues = Arrays.stream(values)
                    .map(value -> buildMetadataValueVO(row, value, isEntityRowSheet))
                    .collect(Collectors.toList());

                metadata.putAll(header, metadataValues);
            }
        }

        return metadata;
    }

    private MetadataValueVO buildMetadataValueVO(Row row, String metadataValue, boolean isEntityRowSheet) {

        if (isBlank(metadataValue)) {
            return new MetadataValueVO(isEntityRowSheet ? metadataValue : PLACEHOLDER_PARENT_METADATA_VALUE, null, -1);
        }

        if (!metadataValue.contains(AUTHORITY_SEPARATOR)) {
            return new MetadataValueVO(metadataValue, null, -1);
        }

        String[] valueSections = StringUtils.split(metadataValue, AUTHORITY_SEPARATOR);

        String value = valueSections[0];
        String authority = valueSections[1];
        int confidence = 600;
        if (valueSections.length > 2) {
            String confidenceAsString = valueSections[2];
            confidence = Integer.valueOf(confidenceAsString);
        }

        return new MetadataValueVO(value, authority, confidence);
    }

    private boolean isEntityRowSheet(Sheet sheet) {
        return sheet.getWorkbook().getSheetIndex(sheet) == 0;
    }

    private int getFirstMetadataIndex(Sheet sheet) {
        String sheetName = sheet.getSheetName();

        if (BITSTREAM_METADATA.equalsIgnoreCase(sheetName)) {
            return 3; // In Bitstream sheet metadata row starts at third index
        }

        return isEntityRowSheet(sheet) ? 2 : 1;
    }

    private List<MetadataGroup> getOwnMetadataGroups(Row row, List<MetadataGroup> metadataGroups) {
        String id = getIdFromRow(row);
        int rowIndex = row.getRowNum() + 1;
        return metadataGroups.stream()
            .filter(g -> g.getParentId().equals(id) || g.getParentId().equals(ROW_ID + ID_SEPARATOR + rowIndex))
            .collect(Collectors.toList());
    }

    private List<UploadDetails> getOwnUploadDetails(Row row, List<UploadDetails> uploadDetails) {
        String id = getIdFromRow(row);
        int rowIndex = row.getRowNum() + 1;
        return uploadDetails.stream()
            .filter(ud -> ud.getParentId().equals(id) || ud.getParentId().equals(ROW_ID + ID_SEPARATOR + rowIndex))
            .collect(Collectors.toList());
    }

    private boolean isEntityRowRowValid(Row row) {
        String id = getIdFromRow(row);
        String action = getActionFromRow(row);

        if (!isValidId(id, false)) {
            handleValidationErrorOnRow(row, "Invalid ID " + id);
            return false;
        }

        return isNotBlank(action) ? isValidAction(id, action, row) : true;
    }

    private boolean isValidAction(String id, String action, Row row) {

        ImportAction[] actions = ImportAction.values();
        if (!ImportAction.isValid(action)) {
            handleValidationErrorOnRow(row,
                "Invalid action " + action + ": allowed values are " + Arrays.toString(actions));
            return false;
        }

        if (isBlank(id) && !ImportAction.valueOf(action).isAddAction()) {
            handleValidationErrorOnRow(row, "Only adding actions can have an empty ID");
            return false;
        }

        if (isNotBlank(id) && ImportAction.valueOf(action).isAddAction()) {
            handleValidationErrorOnRow(row, "Adding actions can not have an ID set");
            return false;
        }

        return true;
    }

    private boolean isMetadataGroupRowValid(Row row) {
        String parentId = getIdFromRow(row);

        if (StringUtils.isBlank(parentId)) {
            handleValidationErrorOnRow(row, "No PARENT-ID set");
            return false;
        }

        if (!isValidId(parentId, true)) {
            handleValidationErrorOnRow(row, "Invalid PARENT-ID " + parentId);
            return false;
        }

        int firstMetadataIndex = getFirstMetadataIndex(row.getSheet());
        for (int index = firstMetadataIndex; index < row.getLastCellNum(); index++) {

            String cellValue = WorkbookUtils.getCellValue(row, index);
            String[] values = isNotBlank(cellValue) ? split(cellValue, METADATA_SEPARATOR) : new String[] { "" };
            if (values.length > 1) {
                handleValidationErrorOnRow(row, "Multiple metadata value on the same cell not allowed "
                    + "in the metadata group sheets: " + cellValue);
                return false;
            }

            String value = values[0];
            if (value.contains(AUTHORITY_SEPARATOR)) {

                String[] valueSections = StringUtils.split(value, AUTHORITY_SEPARATOR);
                if (valueSections.length > 3) {
                    handleValidationErrorOnRow(row, "Invalid metadata value " + value + ": too many sections "
                        + "splitted by " + AUTHORITY_SEPARATOR);
                    return false;
                }

                if (valueSections.length > 2 && !NumberUtils.isCreatable(valueSections[2])) {
                    handleValidationErrorOnRow(row,
                        "Invalid metadata value " + value + ": invalid confidence value " + valueSections[2]);
                    return false;
                }

            }
        }

        return true;
    }

    private boolean isValidId(String id, boolean isMetadataGroup) {

        if (StringUtils.isBlank(id)) {
            return true;
        }

        if (UUIDUtils.fromString(id) != null) {
            return true;
        }

        String[] idSections = id.split(ID_SEPARATOR);
        if (idSections.length != 2) {
            return false;
        }

        java.util.Collection<String> validPrefixes = itemSearcherMapper.getAllowedSearchType();
        if (isMetadataGroup) {
            validPrefixes = new ArrayList<String>(validPrefixes);
            validPrefixes.add(ROW_ID);
        }
        return validPrefixes.contains(idSections[0]);
    }

    private boolean isInSpecifiedCollection(Item item) throws SQLException {
        Collection collection = getCollection();
        if (item.getOwningCollection() != null) {
            return item.getOwningCollection().equals(collection);
        }

        InProgressSubmission<Integer> inProgressSubmission = findInProgressSubmission(item);
        return collection.equals(inProgressSubmission.getCollection());
    }

    private InProgressSubmission<Integer> findInProgressSubmission(Item item) throws SQLException {
        WorkspaceItem workspaceItem = workspaceItemService.findByItem(context, item);
        return workspaceItem != null ? workspaceItem : workflowItemService.findByItem(context, item);
    }

    private void handleException(EntityRow entityRow, BulkImportException bie) {

        rollback();

        if (abortOnError) {
            throw bie;
        }

        String message = "Row " + entityRow.getRow() + " - " + getRootCauseMessage(bie);
        handler.logError(message);

    }

    private void handleValidationErrorOnRow(Row row, String message) {
        String sheetName = row.getSheet().getSheetName();
        String errorMessage = "Sheet " + sheetName + " - Row " + (row.getRowNum() + 1) + " - " + message;
        if (abortOnError) {
            throw new BulkImportException(errorMessage);
        } else {
            handler.logError(errorMessage);
        }
    }

    private void rollback() {
        try {
            context.rollback();
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    private void assignCurrentUserInContext() throws SQLException {
        UUID uuid = getEpersonIdentifier();
        if (uuid != null) {
            EPerson ePerson = EPersonServiceFactory.getInstance().getEPersonService().find(context, uuid);
            context.setCurrentUser(ePerson);
        }
    }

    private void assignSpecialGroupsInContext() throws SQLException {
        for (UUID uuid : handler.getSpecialGroups()) {
            context.setSpecialGroup(uuid);
        }
    }

    private Collection getCollection() {
        try {
            return collectionService.find(context, UUID.fromString(collectionId));
        } catch (SQLException e) {
            throw new BulkImportException(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public BulkImportScriptConfiguration<BulkImport> getScriptConfiguration() {
        return new DSpace().getServiceManager().getServiceByName("bulk-import", BulkImportScriptConfiguration.class);
    }

    public boolean isEntityUploadSheet(Sheet sheet) {
        return sheet.getSheetName().equalsIgnoreCase(BITSTREAM_METADATA);
    }

    public boolean isUploadMetadataGroup(String metadataGroupName) {
        return BITSTREAM_METADATA.equalsIgnoreCase(metadataGroupName);
    }
}
