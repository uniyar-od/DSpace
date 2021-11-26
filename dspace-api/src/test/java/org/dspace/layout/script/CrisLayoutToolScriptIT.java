/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.layout.script;

import static org.dspace.app.launcher.ScriptLauncher.handleScript;
import static org.dspace.layout.LayoutSecurity.OWNER_ONLY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

import org.dspace.AbstractIntegrationTestWithDatabase;
import org.dspace.app.launcher.ScriptLauncher;
import org.dspace.app.scripts.handler.impl.TestDSpaceRunnableHandler;
import org.dspace.authorize.AuthorizeException;
import org.dspace.builder.EntityTypeBuilder;
import org.dspace.layout.CrisLayoutBox;
import org.dspace.layout.CrisLayoutCell;
import org.dspace.layout.CrisLayoutField;
import org.dspace.layout.CrisLayoutFieldBitstream;
import org.dspace.layout.CrisLayoutRow;
import org.dspace.layout.CrisLayoutTab;
import org.dspace.layout.CrisMetadataGroup;
import org.dspace.layout.LayoutSecurity;
import org.dspace.layout.factory.CrisLayoutServiceFactory;
import org.dspace.layout.service.CrisLayoutTabService;
import org.junit.After;
import org.junit.Test;

/**
 * Integration tests for {@link CrisLayoutToolScript}.
 *
 * @author Luca Giamminonni (luca.giamminonni at 4science.it)
 *
 */
public class CrisLayoutToolScriptIT extends AbstractIntegrationTestWithDatabase {

    private static final String BASE_XLS_DIR_PATH = "./target/testing/dspace/assetstore/layout/script";

    private CrisLayoutTabService tabService = CrisLayoutServiceFactory.getInstance().getTabService();

    @After
    public void after() throws SQLException, AuthorizeException {
        context.turnOffAuthorisationSystem();
        List<CrisLayoutTab> tabs = tabService.findAll(context);
        for (CrisLayoutTab tab : tabs) {
            tabService.delete(context, tab);
        }
        context.restoreAuthSystemState();
    }

    @Test
    public void testConfigurationToolFile() throws Exception {

        context.turnOffAuthorisationSystem();
        List.of("Publication", "Person", "OrgUnit", "Patent", "Journal", "Event",
            "Equipment", "Funding", "Product", "Project").forEach(this::createEntityType);
        context.restoreAuthSystemState();

        assertThat(tabService.findAll(context), empty());

        String fileLocation = new File(getDspaceDir(), "etc/conftool/cris-layout-configuration.xls").getAbsolutePath();
        String[] args = new String[] { "cris-layout-tool", "-f", fileLocation };
        TestDSpaceRunnableHandler handler = new TestDSpaceRunnableHandler();
        handleScript(args, ScriptLauncher.getConfig(kernelImpl), handler, kernelImpl, eperson);
        assertThat(handler.getErrorMessages(), empty());

        assertThat(tabService.findAll(context), not(empty()));

    }

    @Test
    public void testWithEmptyFile() throws InstantiationException, IllegalAccessException {

        String fileLocation = getXlsFilePath("empty.xls");
        String[] args = new String[] { "cris-layout-tool", "-f", fileLocation };
        TestDSpaceRunnableHandler handler = new TestDSpaceRunnableHandler();

        handleScript(args, ScriptLauncher.getConfig(kernelImpl), handler, kernelImpl, eperson);
        assertThat(handler.getInfoMessages(), empty());
        assertThat(handler.getWarningMessages(), empty());

        List<String> errorMessages = handler.getErrorMessages();
        assertThat(errorMessages, hasSize(9));
        assertThat(errorMessages.get(0), containsString("The tab sheet is missing"));
        assertThat(errorMessages.get(1), containsString("The box sheet is missing"));
        assertThat(errorMessages.get(2), containsString("The tab2box sheet is missing"));
        assertThat(errorMessages.get(3), containsString("The box2metadata sheet is missing"));
        assertThat(errorMessages.get(4), containsString("The metadatagroups sheet is missing"));
        assertThat(errorMessages.get(5), containsString("The box2metrics sheet is missing"));
        assertThat(errorMessages.get(6), containsString("The boxpolicy sheet is missing"));
        assertThat(errorMessages.get(7), containsString("The tabpolicy sheet is missing"));
        assertThat(errorMessages.get(8), containsString("The given workbook is not valid. Import canceled"));
    }

    @Test
    public void testWithEmptySheetsFile() throws InstantiationException, IllegalAccessException {

        String fileLocation = getXlsFilePath("empty-sheets.xls");
        String[] args = new String[] { "cris-layout-tool", "-f", fileLocation };
        TestDSpaceRunnableHandler handler = new TestDSpaceRunnableHandler();

        handleScript(args, ScriptLauncher.getConfig(kernelImpl), handler, kernelImpl, eperson);
        assertThat(handler.getInfoMessages(), empty());
        assertThat(handler.getWarningMessages(), empty());

        List<String> errorMessages = handler.getErrorMessages();
        assertThat(errorMessages, hasSize(41));
        assertThat(errorMessages.get(0), containsString("The sheet tab has no ENTITY column"));
        assertThat(errorMessages.get(1), containsString("The sheet tab has no LEADING column"));
        assertThat(errorMessages.get(2), containsString("The sheet tab has no PRIORITY column"));
        assertThat(errorMessages.get(3), containsString("The sheet tab has no SECURITY column"));
        assertThat(errorMessages.get(4), containsString("The sheet tab has no SHORTNAME column"));
        assertThat(errorMessages.get(5), containsString("The sheet tab has no LABEL column"));
        assertThat(errorMessages.get(6), containsString("The sheet box has no TYPE column"));
        assertThat(errorMessages.get(7), containsString("The sheet box has no ENTITY column"));
        assertThat(errorMessages.get(8), containsString("The sheet box has no COLLAPSED column"));
        assertThat(errorMessages.get(9), containsString("The sheet box has no CONTAINER column"));
        assertThat(errorMessages.get(10), containsString("The sheet box has no MINOR column"));
        assertThat(errorMessages.get(11), containsString("The sheet box has no SECURITY column"));
        assertThat(errorMessages.get(12), containsString("The sheet box has no SHORTNAME column"));
        assertThat(errorMessages.get(13), containsString("The sheet tab2box has no ENTITY column"));
        assertThat(errorMessages.get(14), containsString("The sheet tab2box has no TAB column"));
        assertThat(errorMessages.get(15), containsString("The sheet tab2box has no BOXES column"));
        assertThat(errorMessages.get(16), containsString("The sheet tab2box has no ROW_STYLE column"));
        assertThat(errorMessages.get(17), containsString("The sheet box2metadata has no ROW column"));
        assertThat(errorMessages.get(18), containsString("The sheet box2metadata has no CELL column"));
        assertThat(errorMessages.get(19), containsString("The sheet box2metadata has no LABEL_AS_HEADING column"));
        assertThat(errorMessages.get(20), containsString("The sheet box2metadata has no VALUES_INLINE column"));
        assertThat(errorMessages.get(21), containsString("The sheet box2metadata has no BUNDLE column"));
        assertThat(errorMessages.get(22), containsString("The sheet box2metadata has no VALUE column"));
        assertThat(errorMessages.get(23), containsString("The sheet box2metadata has no ROW_STYLE column"));
        assertThat(errorMessages.get(24), containsString("The sheet box2metadata has no FIELDTYPE column"));
        assertThat(errorMessages.get(25), containsString("The sheet box2metadata has no METADATA column"));
        assertThat(errorMessages.get(26), containsString("The sheet box2metadata has no ENTITY column"));
        assertThat(errorMessages.get(27), containsString("The sheet box2metadata has no BOX column"));
        assertThat(errorMessages.get(28), containsString("The sheet metadatagroups has no ENTITY column"));
        assertThat(errorMessages.get(29), containsString("The sheet metadatagroups has no METADATA column"));
        assertThat(errorMessages.get(30), containsString("The sheet metadatagroups has no PARENT column"));
        assertThat(errorMessages.get(31), containsString("The sheet box2metrics has no ENTITY column"));
        assertThat(errorMessages.get(32), containsString("The sheet box2metrics has no BOX column"));
        assertThat(errorMessages.get(33), containsString("The sheet box2metrics has no METRIC_TYPE column"));
        assertThat(errorMessages.get(34), containsString("The sheet boxpolicy has no METADATA column"));
        assertThat(errorMessages.get(35), containsString("The sheet boxpolicy has no ENTITY column"));
        assertThat(errorMessages.get(36), containsString("The sheet boxpolicy has no SHORTNAME column"));
        assertThat(errorMessages.get(37), containsString("The sheet tabpolicy has no METADATA column"));
        assertThat(errorMessages.get(38), containsString("The sheet tabpolicy has no ENTITY column"));
        assertThat(errorMessages.get(39), containsString("The sheet tabpolicy has no SHORTNAME column"));
        assertThat(errorMessages.get(40), containsString("The given workbook is not valid. Import canceled"));

    }

    @Test
    public void testWithValidLayout() throws InstantiationException, IllegalAccessException, SQLException {

        context.turnOffAuthorisationSystem();
        createEntityType("Publication");
        createEntityType("Person");
        context.restoreAuthSystemState();

        String fileLocation = getXlsFilePath("valid-layout-with-3-tabs.xls");
        String[] args = new String[] { "cris-layout-tool", "-f", fileLocation };
        TestDSpaceRunnableHandler handler = new TestDSpaceRunnableHandler();

        handleScript(args, ScriptLauncher.getConfig(kernelImpl), handler, kernelImpl, eperson);
        assertThat(handler.getErrorMessages(), empty());
        assertThat(handler.getWarningMessages(), empty());

        List<String> infoMessages = handler.getInfoMessages();
        assertThat(infoMessages, hasSize(6));
        assertThat(infoMessages.get(0), containsString("The given workbook is valid. Proceed with the import"));
        assertThat(infoMessages.get(1), containsString("The workbook has been parsed correctly, "
            + "found 3 tabs to import"));
        assertThat(infoMessages.get(2), containsString("Proceed with the clearing of the previous layout"));
        assertThat(infoMessages.get(3), containsString("Found 0 tabs to delete"));
        assertThat(infoMessages.get(4), containsString("The previous layout has been deleted, "
            + "proceed with the import of the new configuration"));
        assertThat(infoMessages.get(5), containsString("Import completed successfully"));

        assertThat(tabService.findAll(context), hasSize(3));

        List<CrisLayoutTab> personTabs = tabService.findByEntityType(context, "Person");
        assertThat(personTabs, hasSize(2));

        CrisLayoutTab firstPersonTab = personTabs.get(0);
        assertThatTabHas(firstPersonTab, "details", "Person", "Profile", 1, 0, false, 0, LayoutSecurity.PUBLIC);

        CrisLayoutRow firstPersonTabRow = firstPersonTab.getRows().get(0);
        assertThat(firstPersonTabRow.getStyle(), is("person-details-style"));
        assertThat(firstPersonTabRow.getCells(), hasSize(1));

        CrisLayoutCell firstPersonTabCell = firstPersonTabRow.getCells().get(0);
        assertThat(firstPersonTabCell.getStyle(), nullValue());
        assertThat(firstPersonTabCell.getBoxes(), hasSize(2));

        CrisLayoutBox profileBox = firstPersonTabCell.getBoxes().get(0);
        assertThatBoxHas(profileBox, "researcherprofile", "METADATA", "Person", "Profile", 6,
            0, 0, false, false, false, "profile-style", LayoutSecurity.PUBLIC);

        List<CrisLayoutField> profileFields = profileBox.getLayoutFields();

        CrisLayoutField profilePicture = profileFields.get(0);
        assertThatBitstreamFieldHas(profilePicture, null, "row", null, 1, 1, 0, "thumbnail", 0,
            "dc.type", "font-weight-bold col-4", null, false, false, "ORIGINAL", "personal picture");

        CrisLayoutField profileTitle = profileFields.get(1);
        assertThatMetadataFieldHas(profileTitle, "Preferred name", "row", "title-cell-style", 1, 2, 1, null, 0,
            "dc.title", "font-weight-bold", "bold", false, false);

        CrisLayoutField profileName = profileFields.get(2);
        assertThatMetadataFieldHas(profileName, "Official Name", "row", null, 1, 2, 2, null, 0,
            "crisrp.name", null, null, false, true);

        CrisLayoutField profileEmail = profileFields.get(3);
        assertThatMetadataFieldHas(profileEmail, "Email", "row", null, 1, 2, 3, "crisref.email", 0,
            "person.email", null, null, true, true);

        CrisLayoutField profileDescription = profileFields.get(4);
        assertThatMetadataFieldHas(profileDescription, "Biography", "row", null, 2, 1, 4, "longtext", 0,
            "dc.description.abstract", null, null, false, false);

        CrisLayoutField profileAffiliation = profileFields.get(5);
        assertThatMetadataFieldHas(profileAffiliation, "Affiliation", "row", null, 3, 1, 5, "table", 2,
            "oairecerif.person.affiliation", null, null, false, false);

        CrisMetadataGroup profileNestedAffiliationRole = profileAffiliation.getCrisMetadataGroupList().get(0);
        assertThat(profileNestedAffiliationRole.getLabel(), is("Role"));
        assertThat(profileNestedAffiliationRole.getMetadataField().toString('.'), is("oairecerif.affiliation.role"));
        assertThat(profileNestedAffiliationRole.getPriority(), is(0));
        assertThat(profileNestedAffiliationRole.getRendering(), is("text"));
        assertThat(profileNestedAffiliationRole.getStyleLabel(), is("col"));
        assertThat(profileNestedAffiliationRole.getStyleValue(), is("col"));

        CrisMetadataGroup profileNestedAffiliation = profileAffiliation.getCrisMetadataGroupList().get(1);
        assertThat(profileNestedAffiliation.getLabel(), is("Organisation"));
        assertThat(profileNestedAffiliation.getMetadataField().toString('.'), is("oairecerif.person.affiliation"));
        assertThat(profileNestedAffiliation.getPriority(), is(1));
        assertThat(profileNestedAffiliation.getRendering(), is("crisref"));
        assertThat(profileNestedAffiliation.getStyleLabel(), is("label-style"));
        assertThat(profileNestedAffiliation.getStyleValue(), is("value-style"));

        CrisLayoutBox profileSecuredBox = firstPersonTabCell.getBoxes().get(1);
        assertThatBoxHas(profileSecuredBox, "secured", "METADATA", "Person", "Secured infos", 1,
            0, 0, false, false, true, null, LayoutSecurity.OWNER_ONLY);
        assertThat(profileSecuredBox.getLayoutFields().get(0).getMetadataField().toString('.'),
            is("oairecerif.person.gender"));

        CrisLayoutTab secondPersonTab = personTabs.get(1);
        assertThatTabHas(secondPersonTab, "publications", "Person", "Publications", 2, 0, false, 2, OWNER_ONLY);

        CrisLayoutRow secondPersonTabFirstRow = secondPersonTab.getRows().get(0);
        assertThat(secondPersonTabFirstRow.getStyle(), nullValue());
        assertThat(secondPersonTabFirstRow.getCells(), hasSize(1));
        assertThat(secondPersonTabFirstRow.getCells().get(0).getStyle(), nullValue());
        assertThat(secondPersonTabFirstRow.getCells().get(0).getBoxes(), hasSize(1));

        CrisLayoutBox profileNameCardBox = secondPersonTabFirstRow.getCells().get(0).getBoxes().get(0);
        assertThatBoxHas(profileNameCardBox, "namecard", "METADATA", "Person", "Person", 2,
            0, 0, true, false, false, null, LayoutSecurity.PUBLIC);

        CrisLayoutRow secondPersonTabSecondRow = secondPersonTab.getRows().get(1);
        assertThat(secondPersonTabSecondRow.getStyle(), is("person-pub-style"));
        assertThat(secondPersonTabSecondRow.getCells(), hasSize(1));

        CrisLayoutCell secondPersonTabSecondRowCell = secondPersonTabSecondRow.getCells().get(0);
        assertThat(secondPersonTabSecondRowCell.getStyle(), nullValue());
        assertThat(secondPersonTabSecondRowCell.getBoxes(), hasSize(1));

        CrisLayoutBox profileResearchoutputsBox = secondPersonTabSecondRowCell.getBoxes().get(0);
        assertThatBoxHas(profileResearchoutputsBox, "researchoutputs", "RELATION", "Person", "Publications", 0,
            0, 0, false, false, true, "researchoutputs-style", LayoutSecurity.PUBLIC);

        List<CrisLayoutTab> publicationTabs = tabService.findByEntityType(context, "Publication");
        assertThat(publicationTabs, hasSize(1));

        CrisLayoutTab publicationTab = publicationTabs.get(0);

        // TODO continue....

    }

    private void assertThatBitstreamFieldHas(CrisLayoutField field, String label, String rowStyle, String cellStyle,
        int row, int cell, int priority, String rendering, int metadataGroupSize, String metadataField,
        String labelStyle, String valueStyle, boolean labelAsHeading, boolean valuesInline, String bundle,
        String value) {

        assertThat(field, instanceOf(CrisLayoutFieldBitstream.class));
        assertThat(((CrisLayoutFieldBitstream) field).getBundle(), is(bundle));
        assertThat(((CrisLayoutFieldBitstream) field).getMetadataValue(), is(value));
        assertThatFieldHas(field, label, rowStyle, cellStyle, row, cell, priority, rendering, metadataGroupSize,
            metadataField, labelStyle, valueStyle, labelAsHeading, valuesInline);

    }

    private void assertThatMetadataFieldHas(CrisLayoutField field, String label, String rowStyle, String cellStyle,
        int row, int cell, int priority, String rendering, int metadataGroupSize, String metadataField,
        String labelStyle, String valueStyle, boolean labelAsHeading, boolean valuesInline) {

        assertThatFieldHas(field, label, rowStyle, cellStyle, row, cell, priority, rendering, metadataGroupSize,
            metadataField, labelStyle, valueStyle, labelAsHeading, valuesInline);

    }

    private void assertThatFieldHas(CrisLayoutField field, String label, String rowStyle, String cellStyle,
        int row, int cell, int priority, String rendering, int metadataGroupSize, String metadataField,
        String labelStyle, String valueStyle, boolean labelAsHeading, boolean valuesInline) {

        assertThat(field.getLabel(), is(label));
        assertThat(field.getRow(), is(row));
        assertThat(field.getCell(), is(cell));
        assertThat(field.getRowStyle(), is(rowStyle));
        assertThat(field.getCellStyle(), is(cellStyle));
        assertThat(field.getCrisMetadataGroupList(), hasSize(metadataGroupSize));
        assertThat(field.getMetadataField().toString('.'), is(metadataField));
        assertThat(field.getPriority(), is(priority));
        assertThat(field.getRendering(), is(rendering));
        assertThat(field.getStyleLabel(), is(labelStyle));
        assertThat(field.getStyleValue(), is(valueStyle));
        assertThat(field.isLabelAsHeading(), is(labelAsHeading));
        assertThat(field.isValuesInline(), is(valuesInline));

    }

    private void assertThatTabHas(CrisLayoutTab tab, String shortname, String entityType, String header, int rowsSize,
        int securityFieldsSize, boolean isLeading, int priority, LayoutSecurity security) {

        assertThat(tab.getEntity().getLabel(), is(entityType));
        assertThat(tab.getHeader(), is(header));
        assertThat(tab.getMetadataSecurityFields(), hasSize(securityFieldsSize));
        assertThat(tab.getPriority(), is(priority));
        assertThat(tab.getRows(), hasSize(rowsSize));
        assertThat(tab.getSecurity(), is(security.getValue()));
        assertThat(tab.getShortName(), is(shortname));
        assertThat(tab.isLeading(), is(isLeading));

    }

    private void assertThatBoxHas(CrisLayoutBox box, String shortname, String type, String entityType,
        String header, int fieldsSize, int securityFieldsSize, int metricsSize, boolean minor, boolean collapsed,
        boolean container, String style, LayoutSecurity security) {

        assertThat(box.getCollapsed(), is(collapsed));
        assertThat(box.isContainer(), is(container));
        assertThat(box.getEntitytype().getLabel(), is(entityType));
        assertThat(box.getHeader(), is(header));
        assertThat(box.getLayoutFields(), hasSize(fieldsSize));
        assertThat(box.getMaxColumns(), nullValue());
        assertThat(box.getMetadataSecurityFields(), hasSize(securityFieldsSize));
        assertThat(box.getMetric2box(), hasSize(metricsSize));
        assertThat(box.getMinor(), is(minor));
        assertThat(box.getSecurity(), is(security.getValue()));
        assertThat(box.getStyle(), is(style));
        assertThat(box.getShortname(), is(shortname));
        assertThat(box.getType(), is(type));

    }

    private void createEntityType(String entityType) {
        EntityTypeBuilder.createEntityTypeBuilder(context, entityType).build();
    }

    private String getXlsFilePath(String name) {
        return new File(BASE_XLS_DIR_PATH, name).getAbsolutePath();
    }
}
