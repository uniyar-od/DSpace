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

import java.io.File;
import java.util.List;

import org.dspace.AbstractIntegrationTestWithDatabase;
import org.dspace.app.launcher.ScriptLauncher;
import org.dspace.app.scripts.handler.impl.TestDSpaceRunnableHandler;
import org.dspace.builder.EntityTypeBuilder;
import org.junit.Test;

/**
 * Integration tests for {@link CrisLayoutToolScript}.
 *
 * @author Luca Giamminonni (luca.giamminonni at 4science.it)
 *
 */
public class CrisLayoutToolScriptIT extends AbstractIntegrationTestWithDatabase {

    private static final String BASE_XLS_DIR_PATH = "./target/testing/dspace/assetstore/layout/script";

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
        assertThat(handler.getWarningMessages(), empty());

    }

    @Test
    public void testWithInvalidFile() throws InstantiationException, IllegalAccessException {

        String fileLocation = getXlsFilePath("invalid.xls");
        String[] args = new String[] { "cris-layout-tool", "-f", fileLocation };
        TestDSpaceRunnableHandler handler = new TestDSpaceRunnableHandler();

        handleScript(args, ScriptLauncher.getConfig(kernelImpl), handler, kernelImpl, eperson);
        assertThat("Expected no infos", handler.getInfoMessages(), empty());
        assertThat("Expected no warnings", handler.getWarningMessages(), empty());

        List<String> errorMessages = handler.getErrorMessages();
        assertThat("Expected 1 error message", errorMessages, hasSize(1));
        assertThat(errorMessages.get(0), containsString("The sheet Main Entity of the Workbook is empty"));
    }

    private void createEntityType(String entityType) {
        EntityTypeBuilder.createEntityTypeBuilder(context, entityType).build();
    }

    private String getXlsFilePath(String name) {
        return new File(BASE_XLS_DIR_PATH, name).getAbsolutePath();
    }
}
