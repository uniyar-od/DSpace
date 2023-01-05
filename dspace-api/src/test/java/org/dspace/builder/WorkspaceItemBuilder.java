/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.builder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Objects;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Bitstream;
import org.dspace.content.Collection;
import org.dspace.content.DCDate;
import org.dspace.content.Item;
import org.dspace.content.LicenseUtils;
import org.dspace.content.MetadataSchemaEnum;
import org.dspace.content.WorkspaceItem;
import org.dspace.content.service.WorkspaceItemService;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.xmlworkflow.storedcomponents.XmlWorkflowItem;

/**
 * Builder to construct WorkspaceItem objects
 *
 **/
public class WorkspaceItemBuilder extends AbstractBuilder<WorkspaceItem, WorkspaceItemService> {

    /** Keep a reference to the underlying Item for cleanup **/
    private Item item;

    private WorkspaceItem workspaceItem;

    protected WorkspaceItemBuilder(Context context) {
        super(context);
    }

    public static WorkspaceItemBuilder createWorkspaceItem(final Context context, final Collection col) {
        WorkspaceItemBuilder builder = new WorkspaceItemBuilder(context);
        return builder.create(context, col);
    }

    private WorkspaceItemBuilder create(final Context context, final Collection col) {
        this.context = context;

        try {
            workspaceItem = workspaceItemService.create(context, col, false);
            item = workspaceItem.getItem();
        } catch (Exception e) {
            return handleException(e);
        }

        return this;
    }

    @Override
    public WorkspaceItem build() {
        try {
            context.dispatchEvents();
            indexingService.commit();
            return workspaceItem;
        } catch (Exception e) {
            return handleException(e);
        }

    }

    private void deleteItem(Context c, Item dso) throws Exception {
        if (dso != null) {
            itemService.delete(c, dso);
        }
    }

    @Override
    public void delete( Context c, WorkspaceItem dso) throws Exception {
        if (dso != null) {
            getService().deleteAll(c, dso);
        }
    }

    /**
     * Delete the Test WorkspaceItem referred to by the given ID
     * @param id Integer of Test WorkspaceItem to delete
     * @throws SQLException
     * @throws IOException
     */
    public static void deleteWorkspaceItem(Integer id) throws SQLException, IOException {
        if (Objects.isNull(id)) {
            return;
        }
        try (Context c = new Context()) {
            c.turnOffAuthorisationSystem();
            WorkspaceItem workspaceItem = workspaceItemService.find(c, id);
            if (workspaceItem != null) {
                try {
                    workspaceItemService.deleteAll(c, workspaceItem);
                } catch (AuthorizeException e) {
                    throw new RuntimeException(e);
                }
            }
            c.complete();
        }
    }

    @Override
    public void cleanup() throws Exception {
        try (Context c = new Context()) {
            c.setDispatcher("noindex");
            c.turnOffAuthorisationSystem();
            // Ensure object and any related objects are reloaded before checking to see what needs cleanup
            workspaceItem = c.reloadEntity(workspaceItem);
            if (workspaceItem != null) {
                delete(c, workspaceItem);
            } else {
                item = c.reloadEntity(item);
                // check if the wsi has been pushed to the workflow
                XmlWorkflowItem wfi = workflowItemService.findByItem(c, item);
                if (wfi != null) {
                    workflowItemService.delete(c, wfi);
                }
            }
            item = c.reloadEntity(item);
            if (item != null) {
                deleteItem(c, item);
            }
            c.complete();
            indexingService.commit();
        }
    }

    @Override
    protected WorkspaceItemService getService() {
        return workspaceItemService;
    }

    protected WorkspaceItemBuilder addMetadataValue(String schema, String element, String qualifier, String value) {
        return addMetadataValue(schema, element, qualifier, null, value, null, -1);
    }

    protected WorkspaceItemBuilder addMetadataValue(String schema, String element, String qualifier, String language,
                                                    String value) {
        return addMetadataValue(schema, element, qualifier, language, value, null, -1);
    }

    protected WorkspaceItemBuilder addMetadataValue(String schema, String element, String qualifier, String language,
        String value, String authority, int confidence) {

        try {
            itemService.addMetadata(context, workspaceItem.getItem(), schema, element, qualifier, language,
                value, authority, confidence);
        } catch (Exception e) {
            return handleException(e);
        }

        return this;
    }

    public WorkspaceItemBuilder withSubmitter(EPerson ePerson) {
        workspaceItem.getItem().setSubmitter(ePerson);
        return this;
    }

    protected WorkspaceItemBuilder setMetadataSingleValue(final String schema,
            final String element, final String qualifier, final String value) {
        try {
            itemService.setMetadataSingleValue(context, workspaceItem.getItem(), schema, element, qualifier, null,
                    value);
        } catch (Exception e) {
            return handleException(e);
        }

        return this;
    }

    public WorkspaceItemBuilder withTitle(final String title) {
        return setMetadataSingleValue(MetadataSchemaEnum.DC.getName(), "title", null, title);
    }

    public WorkspaceItemBuilder withTitleForLanguage(final String title, final String language) {
        return addMetadataValue(MetadataSchemaEnum.DC.getName(), "title", null, language, title);
    }

    public WorkspaceItemBuilder withIssueDate(final String issueDate) {
        return addMetadataValue(MetadataSchemaEnum.DC.getName(), "date", "issued", new DCDate(issueDate).toString());
    }

    public WorkspaceItemBuilder withType(final String type) {
        return addMetadataValue(MetadataSchemaEnum.DC.getName(), "type", null, type);
    }

    public WorkspaceItemBuilder withAuthor(final String authorName) {
        return addMetadataValue(MetadataSchemaEnum.DC.getName(), "contributor", "author", authorName);
    }

    public WorkspaceItemBuilder withAuthor(String authorName, String authority) {
        return addMetadataValue(MetadataSchemaEnum.DC.getName(), "contributor", "author", null, authorName, authority,
            600);
    }

    public WorkspaceItemBuilder withAuthorAffilitation(final String affilation) {
        return addMetadataValue(MetadataSchemaEnum.OAIRECERIF.getName(), "author", "affiliation", affilation);
    }

    public WorkspaceItemBuilder withEditor(final String editorName) {
        return addMetadataValue(MetadataSchemaEnum.DC.getName(), "contributor", "editor", editorName);
    }

    public WorkspaceItemBuilder withEditor(String editorName, String authority) {
        return addMetadataValue(MetadataSchemaEnum.DC.getName(), "contributor", "editor", null, editorName, authority,
            600);
    }

    public WorkspaceItemBuilder withProject(final String projectName) {
        return addMetadataValue(MetadataSchemaEnum.DC.getName(), "relation", "project", projectName);
    }

    public WorkspaceItemBuilder withSubject(final String subject) {
        return addMetadataValue(MetadataSchemaEnum.DC.getName(), "subject", null, subject);
    }

    public WorkspaceItemBuilder withIssn(String issn) {
        return addMetadataValue(MetadataSchemaEnum.DC.getName(), "identifier", "issn", issn);
    }

    public WorkspaceItemBuilder withSubjectForLanguage(final String subject, final String language) {
        return addMetadataValue(MetadataSchemaEnum.DC.getName(), "subject", null, language, subject);
    }

    public WorkspaceItemBuilder withEntityType(final String entityType) {
        return addMetadataValue("dspace", "entity", "type", entityType);
    }

    public WorkspaceItemBuilder withCrisSourceId(String sourceId) {
        return addMetadataValue("cris", "sourceId", null, sourceId);
    }

    public WorkspaceItemBuilder withDoiIdentifier(String doi) {
        return addMetadataValue("dc", "identifier", "doi", doi);
    }

    public WorkspaceItemBuilder withIsniIdentifier(String isni) {
        return addMetadataValue("person", "identifier", "isni", isni);
    }

    public WorkspaceItemBuilder withOrcidIdentifier(String orcid) {
        return addMetadataValue("person", "identifier", "orcid", orcid);
    }

    public WorkspaceItemBuilder withPatentNo(String patentNo) {
        return addMetadataValue("dc", "identifier", "patentno", patentNo);
    }

    public WorkspaceItemBuilder withCustomUrl(String url) {
        return setMetadataSingleValue("cris", "customurl", null, url);
    }

    public WorkspaceItemBuilder withOldCustomUrl(String url) {
        return addMetadataValue("cris", "customurl", "old", url);

    }
    public WorkspaceItemBuilder withAbstract(final String subject) {
        return addMetadataValue(MetadataSchemaEnum.DC.getName(),"description", "abstract", subject);
    }

    public WorkspaceItemBuilder grantLicense() {
        Item item = workspaceItem.getItem();
        String license;
        try {
            EPerson submitter = workspaceItem.getSubmitter();
            submitter = context.reloadEntity(submitter);
            license = LicenseUtils.getLicenseText(context.getCurrentLocale(), workspaceItem.getCollection(), item,
                    submitter);
            LicenseUtils.grantLicense(context, item, license, null);
        } catch (Exception e) {
            handleException(e);
        }
        return this;
    }

    public WorkspaceItemBuilder withFulltext(String name, String source, byte[] content) {
        return withFulltext(name, source, new ByteArrayInputStream(content));
    }

    public WorkspaceItemBuilder withFulltext(String name, String source, InputStream is) {
        try {
            Item item = workspaceItem.getItem();
            Bitstream b = itemService.createSingleBitstream(context, is, item);
            b.setName(context, name);
            b.setSource(context, source);
        } catch (Exception e) {
            handleException(e);
        }
        return this;
    }
}
