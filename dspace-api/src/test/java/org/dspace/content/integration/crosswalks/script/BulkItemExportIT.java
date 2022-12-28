/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.integration.crosswalks.script;

import static org.dspace.app.launcher.ScriptLauncher.handleScript;
import static org.dspace.builder.CollectionBuilder.createCollection;
import static org.dspace.builder.CommunityBuilder.createCommunity;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.dspace.AbstractIntegrationTestWithDatabase;
import org.dspace.app.launcher.ScriptLauncher;
import org.dspace.app.scripts.handler.impl.TestDSpaceRunnableHandler;
import org.dspace.authorize.AuthorizeException;
import org.dspace.builder.EPersonBuilder;
import org.dspace.builder.ItemBuilder;
import org.dspace.builder.WorkflowItemBuilder;
import org.dspace.builder.WorkspaceItemBuilder;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.Item;
import org.dspace.content.WorkspaceItem;
import org.dspace.eperson.EPerson;
import org.dspace.services.ConfigurationService;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.dspace.workflow.WorkflowItem;
import org.junit.Before;
import org.junit.Test;

/**
 * Integration tests for {@link BulkItemExport}.
 *
 * @author Luca Giamminonni (luca.giamminonni at 4science.it)
 *
 */
public class BulkItemExportIT extends AbstractIntegrationTestWithDatabase {

    private Community community;

    private Collection collection;

    @Before
    @SuppressWarnings("deprecation")
    public void beforeTests() throws SQLException, AuthorizeException {
        context.turnOffAuthorisationSystem();
        community = createCommunity(context).build();
        collection = createCollection(context, community).withAdminGroup(eperson).withWorkflowGroup(1, eperson).build();
        context.restoreAuthSystemState();
    }

    @Test
    public void testBulkItemExport() throws Exception {

        context.turnOffAuthorisationSystem();
        createItem(collection, "Edward Red", "Science", "Person");
        createItem(collection, "My publication", "", "Publication");
        createItem(collection, "Walter White", "Science", "Person");
        createItem(collection, "John Smith", "Science", "Person");
        context.restoreAuthSystemState();
        context.commit();

        File xml = new File("person.xml");
        xml.deleteOnExit();

        String[] args = new String[] { "bulk-item-export", "-t", "Person", "-f", "person-xml" };
        TestDSpaceRunnableHandler handler = new TestDSpaceRunnableHandler();

        handleScript(args, ScriptLauncher.getConfig(kernelImpl), handler, kernelImpl, eperson);

        assertThat(handler.getErrorMessages(), empty());
        assertThat(handler.getInfoMessages(), hasItem("Found 3 items to export"));
        assertThat("The xml file should be created", xml.exists(), is(true));

        try (FileInputStream fis = new FileInputStream(xml)) {
            String content = IOUtils.toString(fis, Charset.defaultCharset());
            assertThat(content, containsString("<preferred-name>Edward Red</preferred-name>"));
            assertThat(content, containsString("<preferred-name>Walter White</preferred-name>"));
            assertThat(content, containsString("<preferred-name>John Smith</preferred-name>"));
            assertThat(content, not(containsString("<preferred-name>My publication</preferred-name>")));
        }
    }

    @Test
    public void testBulkItemExportWithQuery() throws Exception {

        context.turnOffAuthorisationSystem();
        createItem(collection, "Edward Red", "Science", "Person");
        createItem(collection, "Company", "", "OrgUnit");
        createItem(collection, "Edward Smith", "Science", "Person");
        createItem(collection, "John Smith", "Software", "Person");
        context.restoreAuthSystemState();
        context.commit();

        File xml = new File("person.xml");
        xml.deleteOnExit();

        String[] args = new String[] { "bulk-item-export", "-t", "Person", "-f", "person-xml", "-q", "Edward" };
        TestDSpaceRunnableHandler handler = new TestDSpaceRunnableHandler();

        handleScript(args, ScriptLauncher.getConfig(kernelImpl), handler, kernelImpl, eperson);

        assertThat(handler.getErrorMessages(), empty());
        assertThat(handler.getInfoMessages(), hasItem("Found 2 items to export"));
        assertThat("The xml file should be created", xml.exists(), is(true));

        try (FileInputStream fis = new FileInputStream(xml)) {
            String content = IOUtils.toString(fis, Charset.defaultCharset());
            assertThat(content, containsString("<preferred-name>Edward Red</preferred-name>"));
            assertThat(content, containsString("<preferred-name>Edward Smith</preferred-name>"));
            assertThat(content, not(containsString("<preferred-name>John Smith</preferred-name>")));
            assertThat(content, not(containsString("<preferred-name>Company</preferred-name>")));
        }
    }

    @Test
    public void testBulkItemExportWithSingleFilter() throws Exception {

        context.turnOffAuthorisationSystem();
        createItem(collection, "Edward Red", "Science", "Person");
        createItem(collection, "My publication", "", "Publication");
        createItem(collection, "Walter White", "Science", "Person");
        createItem(collection, "John Smith", "Software", "Person");
        context.restoreAuthSystemState();
        context.commit();

        File xml = new File("person.xml");
        xml.deleteOnExit();

        String[] args = new String[] { "bulk-item-export", "-t", "Person", "-f", "person-xml",
            "-sf", "subject=Science" };
        TestDSpaceRunnableHandler handler = new TestDSpaceRunnableHandler();

        handleScript(args, ScriptLauncher.getConfig(kernelImpl), handler, kernelImpl, eperson);

        assertThat(handler.getErrorMessages(), empty());
        assertThat(handler.getInfoMessages(), hasItem("Found 2 items to export"));
        assertThat("The xml file should be created", xml.exists(), is(true));

        try (FileInputStream fis = new FileInputStream(xml)) {
            String content = IOUtils.toString(fis, Charset.defaultCharset());
            assertThat(content, containsString("<preferred-name>Edward Red</preferred-name>"));
            assertThat(content, containsString("<preferred-name>Walter White</preferred-name>"));
            assertThat(content, not(containsString("<preferred-name>John Smith</preferred-name>")));
            assertThat(content, not(containsString("<preferred-name>My publication</preferred-name>")));
        }
    }

    @Test
    public void testBulkItemExportWithManyFilters() throws Exception {

        context.turnOffAuthorisationSystem();
        createItem(collection, "Edward Red", "Science", "Person");
        createItem(collection, "My publication", "", "Publication");
        createItem(collection, "Walter White", "Science", "Person");
        createItem(collection, "John Smith", "Software", "Person");
        context.restoreAuthSystemState();
        context.commit();

        File xml = new File("person.xml");
        xml.deleteOnExit();

        String[] args = new String[] { "bulk-item-export", "-t", "Person", "-f", "person-xml",
            "-sf", "subject=Science&title=Walter White" };
        TestDSpaceRunnableHandler handler = new TestDSpaceRunnableHandler();

        handleScript(args, ScriptLauncher.getConfig(kernelImpl), handler, kernelImpl, eperson);

        assertThat(handler.getErrorMessages(), empty());
        assertThat(handler.getInfoMessages(), hasItem("Found 1 items to export"));
        assertThat("The xml file should be created", xml.exists(), is(true));

        try (FileInputStream fis = new FileInputStream(xml)) {
            String content = IOUtils.toString(fis, Charset.defaultCharset());
            assertThat(content, containsString("<preferred-name>Walter White</preferred-name>"));
            assertThat(content, not(containsString("<preferred-name>Edward Red</preferred-name>")));
            assertThat(content, not(containsString("<preferred-name>John Smith</preferred-name>")));
            assertThat(content, not(containsString("<preferred-name>My publication</preferred-name>")));
        }
    }

    @Test
    public void testBulkItemExportWithScope() throws Exception {
        context.turnOffAuthorisationSystem();
        Collection anotherCollection = createCollection(context, community).withAdminGroup(eperson).build();
        createItem(collection, "Edward Red", "Science", "Person");
        createItem(collection, "My project", "", "Project");
        createItem(anotherCollection, "Walter White", "Science", "Person");
        createItem(collection, "John Smith", "Software", "Person");
        context.restoreAuthSystemState();
        context.commit();

        File xml = new File("person.xml");
        xml.deleteOnExit();

        String[] args = new String[] { "bulk-item-export", "-t", "Person", "-f", "person-xml",
            "-s", collection.getID().toString() };
        TestDSpaceRunnableHandler handler = new TestDSpaceRunnableHandler();

        handleScript(args, ScriptLauncher.getConfig(kernelImpl), handler, kernelImpl, eperson);

        assertThat(handler.getErrorMessages(), empty());
        assertThat(handler.getInfoMessages(), hasItem("Found 2 items to export"));
        assertThat("The xml file should be created", xml.exists(), is(true));

        try (FileInputStream fis = new FileInputStream(xml)) {
            String content = IOUtils.toString(fis, Charset.defaultCharset());
            assertThat(content, containsString("<preferred-name>John Smith</preferred-name>"));
            assertThat(content, containsString("<preferred-name>Edward Red</preferred-name>"));
            assertThat(content, not(containsString("<preferred-name>Walter White</preferred-name>")));
            assertThat(content, not(containsString("<preferred-name>My project</preferred-name>")));
        }
    }

    @Test
    public void testBulkItemExportWithConfiguration() throws Exception {

        context.turnOffAuthorisationSystem();

        Item orgUnit = ItemBuilder.createItem(context, collection)
            .withTitle("4Science")
            .build();

        String orgUnitId = orgUnit.getID().toString();

        ItemBuilder.createItem(context, collection)
            .withTitle("Edward Red")
            .withEntityType("Person")
            .withPersonMainAffiliation("4Science", orgUnitId)
            .build();

        ItemBuilder.createItem(context, collection)
            .withTitle("John Smith")
            .withEntityType("Person")
            .withPersonMainAffiliation("4Science", orgUnitId)
            .build();

        ItemBuilder.createItem(context, collection)
            .withTitle("Walter White")
            .withEntityType("Person")
            .withPersonMainAffiliation("Company")
            .build();

        createItem(collection, "My project", "", "Project");

        context.restoreAuthSystemState();
        context.commit();

        File xml = new File("person.xml");
        xml.deleteOnExit();

        String[] args = new String[] { "bulk-item-export", "-t", "Person", "-f", "person-xml",
            "-s", orgUnitId, "-c", "RELATION.OrgUnit.people" };
        TestDSpaceRunnableHandler handler = new TestDSpaceRunnableHandler();

        handleScript(args, ScriptLauncher.getConfig(kernelImpl), handler, kernelImpl, eperson);

        assertThat(handler.getErrorMessages(), empty());
        assertThat(handler.getInfoMessages(), hasItem("Found 2 items to export"));
        assertThat("The xml file should be created", xml.exists(), is(true));

        try (FileInputStream fis = new FileInputStream(xml)) {
            String content = IOUtils.toString(fis, Charset.defaultCharset());
            assertThat(content, containsString("<preferred-name>John Smith</preferred-name>"));
            assertThat(content, containsString("<preferred-name>Edward Red</preferred-name>"));
            assertThat(content, not(containsString("<preferred-name>Walter White</preferred-name>")));
            assertThat(content, not(containsString("<preferred-name>My project</preferred-name>")));
        }

    }

    @Test
    public void testBulkItemExportWithQueryAndScope() throws Exception {

        context.turnOffAuthorisationSystem();
        Collection anotherCollection = createCollection(context, community).withAdminGroup(eperson).build();
        createItem(collection, "Edward Red", "Science", "Person");
        createItem(collection, "Edward Mason", "Software", "Person");
        createItem(collection, "My publication", "", "Publication");
        createItem(anotherCollection, "Edward White", "Science", "Person");
        createItem(collection, "John Smith", "Science", "Person");
        context.restoreAuthSystemState();
        context.commit();

        File xml = new File("person.xml");
        xml.deleteOnExit();

        String[] args = new String[] { "bulk-item-export", "-t", "Person", "-f", "person-xml",
            "-s", collection.getID().toString(), "-sf", "subject=Science", "-q", "Edward" };
        TestDSpaceRunnableHandler handler = new TestDSpaceRunnableHandler();

        handleScript(args, ScriptLauncher.getConfig(kernelImpl), handler, kernelImpl, eperson);

        assertThat(handler.getErrorMessages(), empty());
        assertThat(handler.getInfoMessages(), hasItem("Found 1 items to export"));
        assertThat("The xml file should be created", xml.exists(), is(true));

        try (FileInputStream fis = new FileInputStream(xml)) {
            String content = IOUtils.toString(fis, Charset.defaultCharset());
            assertThat(content, containsString("<preferred-name>Edward Red</preferred-name>"));
            assertThat(content, not(containsString("<preferred-name>Edward Mason</preferred-name>")));
            assertThat(content, not(containsString("<preferred-name>Edward White</preferred-name>")));
            assertThat(content, not(containsString("<preferred-name>John Smith</preferred-name>")));
            assertThat(content, not(containsString("<preferred-name>My publication</preferred-name>")));
        }
    }

    @Test
    public void testBulkItemExportWithSortAscending() throws Exception {

        context.turnOffAuthorisationSystem();
        createItem(collection, "Edward Red", "Science", "Person");
        createItem(collection, "Walter White", "Science", "Person");
        createItem(collection, "John Smith", "Science", "Person");
        context.restoreAuthSystemState();
        context.commit();

        File xml = new File("person.xml");
        xml.deleteOnExit();

        String[] args = new String[] { "bulk-item-export", "-t", "Person", "-f", "person-xml", "-so", "dc.title,ASC" };
        TestDSpaceRunnableHandler handler = new TestDSpaceRunnableHandler();

        handleScript(args, ScriptLauncher.getConfig(kernelImpl), handler, kernelImpl, eperson);

        assertThat(handler.getErrorMessages(), empty());
        assertThat(handler.getInfoMessages(), hasItem("Found 3 items to export"));
        assertThat("The xml file should be created", xml.exists(), is(true));

        try (FileInputStream fis = new FileInputStream(xml)) {
            String content = IOUtils.toString(fis, Charset.defaultCharset());
            assertThat(content, containsString("<preferred-name>Edward Red</preferred-name>"));
            assertThat(content, containsString("<preferred-name>Walter White</preferred-name>"));
            assertThat(content, containsString("<preferred-name>John Smith</preferred-name>"));
            assertThat(content.indexOf("Edward Red"), lessThan(content.indexOf("John Smith")));
            assertThat(content.indexOf("John Smith"), lessThan(content.indexOf("Walter White")));
        }
    }

    @Test
    public void testBulkItemExportWithSortDescending() throws Exception {

        context.turnOffAuthorisationSystem();
        createItem(collection, "Edward Red", "Science", "Person");
        createItem(collection, "Walter White", "Science", "Person");
        createItem(collection, "John Smith", "Science", "Person");
        context.restoreAuthSystemState();
        context.commit();

        File xml = new File("person.xml");
        xml.deleteOnExit();

        String[] args = new String[] { "bulk-item-export", "-t", "Person", "-f", "person-xml", "-so", "dc.title,DESC" };
        TestDSpaceRunnableHandler handler = new TestDSpaceRunnableHandler();

        handleScript(args, ScriptLauncher.getConfig(kernelImpl), handler, kernelImpl, eperson);

        assertThat(handler.getErrorMessages(), empty());
        assertThat(handler.getInfoMessages(), hasItem("Found 3 items to export"));
        assertThat("The xml file should be created", xml.exists(), is(true));

        try (FileInputStream fis = new FileInputStream(xml)) {
            String content = IOUtils.toString(fis, Charset.defaultCharset());
            assertThat(content, containsString("<preferred-name>Edward Red</preferred-name>"));
            assertThat(content, containsString("<preferred-name>Walter White</preferred-name>"));
            assertThat(content, containsString("<preferred-name>John Smith</preferred-name>"));
            assertThat(content.indexOf("Walter White"), lessThan(content.indexOf("John Smith")));
            assertThat(content.indexOf("John Smith"), lessThan(content.indexOf("Edward Red")));
        }
    }

    @Test
    public void testBulkItemExportWithoutExportFormat() throws Exception {

        String[] args = new String[] { "bulk-item-export", "-t", "Person" };
        TestDSpaceRunnableHandler handler = new TestDSpaceRunnableHandler();

        handleScript(args, ScriptLauncher.getConfig(kernelImpl), handler, kernelImpl, eperson);

        List<String> errorMessages = handler.getErrorMessages();
        assertThat(errorMessages, hasSize(1));
        assertThat(errorMessages.get(0), containsString("The export format must be provided"));
    }

    @Test
    public void testBulkItemExportWithInvalidFormat() throws Exception {

        String[] args = new String[] { "bulk-item-export", "-t", "Person", "-f", "invalid" };
        TestDSpaceRunnableHandler handler = new TestDSpaceRunnableHandler();

        handleScript(args, ScriptLauncher.getConfig(kernelImpl), handler, kernelImpl, eperson);

        List<String> errorMessages = handler.getErrorMessages();
        assertThat(errorMessages, hasSize(1));
        assertThat(errorMessages.get(0), containsString("No dissemination configured for format invalid"));
    }

    @Test
    public void testBulkItemExportWithInvalidFilter() throws Exception {

        String[] args = new String[] { "bulk-item-export", "-t", "Person", "-f", "person-xml", "-sf", "testFilter" };
        TestDSpaceRunnableHandler handler = new TestDSpaceRunnableHandler();

        handleScript(args, ScriptLauncher.getConfig(kernelImpl), handler, kernelImpl, eperson);

        List<String> errorMessages = handler.getErrorMessages();
        assertThat(errorMessages, hasSize(1));
        assertThat(errorMessages.get(0), containsString("Invalid filter: testFilter"));
    }

    @Test
    public void testBulkItemExportWithoutEntityType() throws Exception {

        String[] args = new String[] { "bulk-item-export", "-f", "person-xml" };
        TestDSpaceRunnableHandler handler = new TestDSpaceRunnableHandler();

        handleScript(args, ScriptLauncher.getConfig(kernelImpl), handler, kernelImpl, eperson);

        assertThat(handler.getErrorMessages(), empty());
        assertThat(handler.getInfoMessages(), contains("Found 0 items to export",
            "Items exported successfully into file named person.xml"));
    }

    @Test
    public void testBulkItemExportWithWorkspaceAndWorkflowItemsAndDefaultConfiguration() throws Exception {

        context.turnOffAuthorisationSystem();
        createItem(collection, "Edward Red", "Science", "Person");
        createItem(collection, "Company", "", "OrgUnit");
        createWorkspaceItem(collection, "Edward Smith", "Science", "Person");
        createItem(collection, "John Smith", "Software", "Person");
        createWorkflowItem(collection, "Edward White", "Science", "Person");
        context.restoreAuthSystemState();
        context.commit();

        File xml = new File("person.xml");
        xml.deleteOnExit();

        String[] args = new String[] { "bulk-item-export", "-t", "Person", "-f", "person-xml", "-q", "Edward" };
        TestDSpaceRunnableHandler handler = new TestDSpaceRunnableHandler();

        handleScript(args, ScriptLauncher.getConfig(kernelImpl), handler, kernelImpl, eperson);

        assertThat(handler.getErrorMessages(), empty());
        assertThat(handler.getInfoMessages(), hasItem("Found 1 items to export"));
        assertThat("The xml file should be created", xml.exists(), is(true));

        try (FileInputStream fis = new FileInputStream(xml)) {
            String content = IOUtils.toString(fis, Charset.defaultCharset());
            assertThat(content, containsString("<preferred-name>Edward Red</preferred-name>"));
            assertThat(content, not(containsString("<preferred-name>Edward Smith</preferred-name>")));
            assertThat(content, not(containsString("<preferred-name>Edward White</preferred-name>")));
            assertThat(content, not(containsString("<preferred-name>John Smith</preferred-name>")));
            assertThat(content, not(containsString("<preferred-name>Company</preferred-name>")));
        }
    }

    @Test
    public void testBulkItemExportWithWorkspaceAndWorkflowItemsAndWorkspaceConfiguration() throws Exception {

        context.turnOffAuthorisationSystem();
        createItem(collection, "Edward Red", "Science", "Person");
        createItem(collection, "Company", "", "OrgUnit");
        createWorkspaceItem(collection, "Edward Smith", "Science", "Person");
        createItem(collection, "John Smith", "Software", "Person");
        createWorkflowItem(collection, "Edward White", "Science", "Person");
        context.restoreAuthSystemState();
        context.commit();

        File xml = new File("person.xml");
        xml.deleteOnExit();

        String[] args = new String[] { "bulk-item-export", "-t", "Person", "-f", "person-xml",
            "-q", "Edward", "-c", "workspace" };
        TestDSpaceRunnableHandler handler = new TestDSpaceRunnableHandler();

        handleScript(args, ScriptLauncher.getConfig(kernelImpl), handler, kernelImpl, eperson);

        assertThat(handler.getErrorMessages(), empty());
        assertThat(handler.getInfoMessages(), hasItem("Found 3 items to export"));
        assertThat("The xml file should be created", xml.exists(), is(true));

        try (FileInputStream fis = new FileInputStream(xml)) {
            String content = IOUtils.toString(fis, Charset.defaultCharset());
            assertThat(content, containsString("<preferred-name>Edward Red</preferred-name>"));
            assertThat(content, containsString("<preferred-name>Edward Smith</preferred-name>"));
            assertThat(content, containsString("<preferred-name>Edward White</preferred-name>"));
            assertThat(content, not(containsString("<preferred-name>John Smith</preferred-name>")));
            assertThat(content, not(containsString("<preferred-name>Company</preferred-name>")));
        }
    }

    @Test
    public void testSelectedItemsBulkItemExport() throws Exception {

        context.turnOffAuthorisationSystem();
        Item item1 = createItem(collection, "Edward Red", "Science", "Publication");
        createItem(collection, "My publication", "", "Publication");
        Item item2 = createItem(collection, "Walter White", "Science", "Publication");
        createItem(collection, "John Smith", "Science", "Publication");
        context.restoreAuthSystemState();
        context.commit();

        String items = item1.getID().toString() + ";" + item2.getID().toString();

        String[] args = new String[] { "bulk-item-export", "-si", items, "-f", "publication-chicago" };

        TestDSpaceRunnableHandler handler = new TestDSpaceRunnableHandler();
        File txt = new File("publications.txt");
        txt.deleteOnExit();

        handleScript(args, ScriptLauncher.getConfig(kernelImpl), handler, kernelImpl, eperson);

        try (FileInputStream fis = new FileInputStream(txt)) {
            String content = IOUtils.toString(fis, Charset.defaultCharset());
            assertThat(content,
                    containsString("“Edward Red,” n.d. http://localhost:4000/handle/" + item1.getHandle()));
            assertThat(content,
                    containsString("“Walter White,” n.d. http://localhost:4000/handle/" + item2.getHandle()));
        }
    }

    @Test
    public void testBulkItemExportLimited() throws Exception {

        context.turnOffAuthorisationSystem();
        createItem(collection, "Edward Red", "Science", "Person");
        createItem(collection, "My publication", "", "Publication");
        createItem(collection, "Walter White", "Science", "Person");
        createItem(collection, "John Smith", "Science", "Person");
        EPerson member = createEPerson();
        context.restoreAuthSystemState();
        context.commit();

        ConfigurationService configurationService = DSpaceServicesFactory.getInstance().getConfigurationService();
        int loggedInLimit = configurationService.getIntProperty("bulk-export.limit.loggedIn");
        int notLoggedInLimit = configurationService.getIntProperty("bulk-export.limit.notLoggedIn");
        configurationService.setProperty("bulk-export.limit.loggedIn", 2);
        configurationService.setProperty("bulk-export.limit.notLoggedIn", 1);

        File xml = new File("person.xml");
        xml.deleteOnExit();

        String[] args = new String[] { "bulk-item-export", "-t", "Person", "-f", "person-xml", "-so", "dc.title,ASC" };
        TestDSpaceRunnableHandler handler = new TestDSpaceRunnableHandler();

        handleScript(args, ScriptLauncher.getConfig(kernelImpl), handler, kernelImpl, admin);

        assertThat(handler.getErrorMessages(), empty());
        assertThat(handler.getInfoMessages(), hasItem("Found 3 items to export"));
        assertThat("The xml file should be created", xml.exists(), is(true));

        // eperson is the collection admin, so the output will be the same as the admin.
        handleScript(args, ScriptLauncher.getConfig(kernelImpl), handler, kernelImpl, eperson);

        assertThat(handler.getErrorMessages(), empty());
        assertThat(handler.getInfoMessages(), hasItem("Found 3 items to export"));
        assertThat("The xml file should be created", xml.exists(), is(true));

        // member is a newly created eperson, so it will be treated as a generic logged user.
        handleScript(args, ScriptLauncher.getConfig(kernelImpl), handler, kernelImpl, member);

        assertThat(handler.getErrorMessages(), empty());
        assertThat(handler.getInfoMessages(), hasItem("Export will be limited to 2 items."));
        assertThat(handler.getInfoMessages(), hasItem("Found 2 items to export"));
        assertThat("The xml file should be created", xml.exists(), is(true));

        // null eperson, so it will be treated as there is no user logged in.
        handleScript(args, ScriptLauncher.getConfig(kernelImpl), handler, kernelImpl, null);

        assertThat(handler.getErrorMessages(), empty());
        assertThat(handler.getInfoMessages(), hasItem("Export will be limited to 1 items."));
        assertThat(handler.getInfoMessages(), hasItem("Found 1 items to export"));
        assertThat("The xml file should be created", xml.exists(), is(true));

        configurationService.setProperty("bulk-export.limit.loggedIn", loggedInLimit);
        configurationService.setProperty("bulk-export.limit.notLoggedIn", notLoggedInLimit);
    }

    private Item createItem(Collection collection, String title, String subject, String entityType) {
        return ItemBuilder.createItem(context, collection)
            .withTitle(title)
            .withSubject(subject)
            .withEntityType(entityType)
            .build();
    }

    private EPerson createEPerson() {
        return EPersonBuilder.createEPerson(context).build();
    }

    private WorkspaceItem createWorkspaceItem(Collection collection, String title, String subject, String entityType) {
        return WorkspaceItemBuilder.createWorkspaceItem(context, collection)
            .withTitle(title)
            .withSubject(subject)
            .withEntityType(entityType)
            .build();
    }

    private WorkflowItem createWorkflowItem(Collection collection, String title, String subject, String entityType) {
        return WorkflowItemBuilder.createWorkflowItem(context, collection)
            .withTitle(title)
            .withSubject(subject)
            .withEntityType(entityType)
            .build();
    }
}
