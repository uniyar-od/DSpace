/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.layout.script;

import static org.dspace.app.launcher.ScriptLauncher.handleScript;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

import org.dspace.AbstractIntegrationTestWithDatabase;
import org.dspace.app.launcher.ScriptLauncher;
import org.dspace.app.scripts.handler.impl.TestDSpaceRunnableHandler;
import org.dspace.authorize.AuthorizeException;
import org.dspace.builder.EntityTypeBuilder;
import org.dspace.layout.CrisLayoutTab;
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
        assertThat(errorMessages, hasSize(40));
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
        assertThat(errorMessages.get(23), containsString("The sheet box2metadata has no FIELDTYPE column"));
        assertThat(errorMessages.get(24), containsString("The sheet box2metadata has no METADATA column"));
        assertThat(errorMessages.get(25), containsString("The sheet box2metadata has no ENTITY column"));
        assertThat(errorMessages.get(26), containsString("The sheet box2metadata has no BOX column"));
        assertThat(errorMessages.get(27), containsString("The sheet metadatagroups has no ENTITY column"));
        assertThat(errorMessages.get(28), containsString("The sheet metadatagroups has no METADATA column"));
        assertThat(errorMessages.get(29), containsString("The sheet metadatagroups has no PARENT column"));
        assertThat(errorMessages.get(30), containsString("The sheet box2metrics has no ENTITY column"));
        assertThat(errorMessages.get(31), containsString("The sheet box2metrics has no BOX column"));
        assertThat(errorMessages.get(32), containsString("The sheet box2metrics has no METRIC_TYPE column"));
        assertThat(errorMessages.get(33), containsString("The sheet boxpolicy has no METADATA column"));
        assertThat(errorMessages.get(34), containsString("The sheet boxpolicy has no ENTITY column"));
        assertThat(errorMessages.get(35), containsString("The sheet boxpolicy has no SHORTNAME column"));
        assertThat(errorMessages.get(36), containsString("The sheet tabpolicy has no METADATA column"));
        assertThat(errorMessages.get(37), containsString("The sheet tabpolicy has no ENTITY column"));
        assertThat(errorMessages.get(38), containsString("The sheet tabpolicy has no SHORTNAME column"));
        assertThat(errorMessages.get(39), containsString("The given workbook is not valid. Import canceled"));

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

        List<CrisLayoutTab> personTabs = tabService.findByEntityType(context, "Person");
        assertThat(personTabs, hasSize(2));

        CrisLayoutTab firstPersonTab = personTabs.get(0);
        assertThat(firstPersonTab.getEntity().getLabel(), is("Person"));
        assertThat(firstPersonTab.getHeader(), is("Profile"));
        assertThat(firstPersonTab.getMetadataSecurityFields(), empty());
        assertThat(firstPersonTab.getPriority(), is(0));
        assertThat(firstPersonTab.getRows(), hasSize(1));
        assertThat(firstPersonTab.getSecurity(), is(LayoutSecurity.PUBLIC.getValue()));
        assertThat(firstPersonTab.getShortName(), is("details"));
        assertThat(firstPersonTab.isLeading(), is(false));

        // TODO to be continued...

    }

    private void createEntityType(String entityType) {
        EntityTypeBuilder.createEntityTypeBuilder(context, entityType).build();
    }

    private String getXlsFilePath(String name) {
        return new File(BASE_XLS_DIR_PATH, name).getAbsolutePath();
    }
}
