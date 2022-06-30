/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.bulkedit;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.SQLException;
import java.util.UUID;

import org.apache.commons.cli.ParseException;
import org.dspace.app.bulkimport.exception.BulkImportException;
import org.dspace.authorize.factory.AuthorizeServiceFactory;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.content.Collection;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.integration.crosswalks.XlsCollectionCrosswalk;
import org.dspace.content.service.CollectionService;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.factory.EPersonServiceFactory;
import org.dspace.scripts.DSpaceRunnable;
import org.dspace.utils.DSpace;

/**
 * Implementation of {@link DSpaceRunnable} to export all the archived items of
 * the given collection.
 *
 * @author Luca Giamminonni (luca.giamminonni at 4science.it)
 *
 */
public class CollectionExport extends DSpaceRunnable<CollectionExportScriptConfiguration<CollectionExport>> {

    private CollectionService collectionService;

    private AuthorizeService authorizeService;

    private XlsCollectionCrosswalk xlsCollectionCrosswalk;

    private String collectionId;

    private Context context;

    @Override
    public void setup() throws ParseException {
        this.collectionService = ContentServiceFactory.getInstance().getCollectionService();
        this.authorizeService = AuthorizeServiceFactory.getInstance().getAuthorizeService();
        this.xlsCollectionCrosswalk = new DSpace().getServiceManager().getServiceByName("xlsCrosswalkCollection",
            XlsCollectionCrosswalk.class);
        collectionId = commandLine.getOptionValue('c');

    }

    @Override
    public void internalRun() throws Exception {

        context = new Context(Context.Mode.BATCH_EDIT);
        assignCurrentUserInContext();
        assignSpecialGroupsInContext();

        context.turnOffAuthorisationSystem();

        Collection collection = getCollection();
        if (collection == null) {
            throw new IllegalArgumentException("No collection found with id " + collectionId);
        }

        if (!this.authorizeService.isAdmin(context, collection)) {
            throw new IllegalArgumentException("The user is not an admin of the given collection");
        }

        try {
            performExport(collection);
            context.complete();
            context.restoreAuthSystemState();
        } catch (Exception e) {
            handler.handleException(e);
            context.abort();
        }

    }

    private void performExport(Collection collection) throws Exception {

        String fileName = xlsCollectionCrosswalk.getFileName();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        xlsCollectionCrosswalk.disseminate(context, collection, out);

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        handler.writeFilestream(context, fileName, in, xlsCollectionCrosswalk.getMIMEType());

        handler.logInfo("Items exported successfully into file named " + fileName);

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
    public CollectionExportScriptConfiguration<CollectionExport> getScriptConfiguration() {
        return new DSpace().getServiceManager().getServiceByName("collection-export",
            CollectionExportScriptConfiguration.class);
    }

}
