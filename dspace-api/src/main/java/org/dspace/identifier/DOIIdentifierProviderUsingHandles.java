/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.identifier;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Metadatum;
import org.dspace.content.DSpaceObject;
import org.dspace.content.FormatIdentifier;
import org.dspace.content.Item;
import org.dspace.content.logic.Filter;
import org.dspace.content.logic.LogicalStatementException;
import org.dspace.core.Context;
import org.dspace.identifier.doi.DOIConnector;
import org.dspace.identifier.doi.DOIIdentifierException;
import org.dspace.identifier.doi.DOIIdentifierNotApplicableException;
import org.dspace.storage.rdbms.DatabaseManager;
import org.dspace.storage.rdbms.TableRow;
import org.dspace.utils.DSpace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

/**
 * Provide service for DOIs using DataCite. Create DOIs reusing the handle suffix
 * of the DSpaceObjects they belong to.
 * 
 * @see org.dspace.identifier.DOIIdentifierProvider 
 * 
 * @author Pascal-Nicolas Becker
 */
public class DOIIdentifierProviderUsingHandles extends DOIIdentifierProvider {
    private static final Logger log = LoggerFactory.getLogger(DOIIdentifierProviderUsingHandles.class);

    /*
     * We need to distinguish several cases. LoadOrCreate can be called with a specifid identifier to load or create.
     * It can also be used to create a new unspecified identifier. In the latter case doiIdentifier is set null.
     * If doiIdentifier is set, we know which doi we should try to load or create, but even in sucha situation
     * we might be able to find it in the database or might have to create it.
     * This method defines how new DOIs will look like. We changed it compared to
     * {@link DOIIdentifierProvider#loadOrCreateDOI(Context, DSpaceObject, STring, Boolean)}.
     */
    @Override
    protected TableRow loadOrCreateDOI(Context context, DSpaceObject dso, String doiIdentifier, Boolean skipFilter)
        throws SQLException, DOIIdentifierException {

        TableRow doiRow = null;

        // Was an identifier specified that we shall try to load or create if it is not existing yet?
        if (null != doiIdentifier)
        {
            // we expect DOIs to have the DOI-Scheme except inside the doi table:
            doiIdentifier = doiIdentifier.substring(DOI.SCHEME.length());
            
            // check if DOI is already in Database
            doiRow = DatabaseManager.findByUnique(context, "Doi", "doi", doiIdentifier);
            if (null != doiRow)
            {
                // check if DOI already belongs to dso
                if (doiRow.getIntColumn("resource_id") == dso.getID() &&
                        doiRow.getIntColumn("resource_type_id") == dso.getType())
                {
                    return doiRow;
                }
                else
                {
                    throw new DOIIdentifierException("Trying to create a DOI " +
                            "that is already reserved for another object.",
                            DOIIdentifierException.DOI_ALREADY_EXISTS);
                }
            }

            // check prefix
            if (!doiIdentifier.startsWith(this.getPrefix() + "/"))
            {
                throw new DOIIdentifierException("Trying to create a DOI " +
                        "that's not part of our Namespace!",
                        DOIIdentifierException.FOREIGN_DOI);
            }
            // prepare new doiRow
            doiRow = DatabaseManager.create(context, "Doi");
        }
        else
        {
            // We need to generate a new DOI. Before doing so, we should check if a
            // filter is in place to prevent the creation of new DOIs for certain items.
            if(skipFilter) {
                log.warn("loadOrCreateDOI: Skipping default item filter");
            }
            else {
                // Find out if we're allowed to create a DOI
                // throws an exception if creation of a new DOI is prohibeted by a filter
                boolean canMintDOI = canMint(context, dso);
                log.debug("Called canMint(), result was " + canMintDOI + " (and presumably an exception was not thrown)");
            }

            // We need to generate a new DOI.
            doiRow = DatabaseManager.create(context, "Doi");
    
            /*
             * By default the doi_id is used to generate the DOI. We want to use the handle suffix instead
             * doiIdentifier = this.getPrefix() + "/" + this.getNamespaceSeparator() +
             *      doiRow.getIntColumn("doi_id");
             */
            // Check if the item already got a handle
            String handle = null;
            try {
                handle = dso.getHandle();
    
                if (handle == null) {
                    IdentifierProvider handleProvider = this.parentService.getProvider(Handle.class);
                    handle = handleProvider.register(context, dso);
                }
            } catch (IdentifierException iex) {
                log.error("Error while retrieving DOI.", iex);
                throw new DOIIdentifierException(iex.getMessage(), iex, DOIIdentifierException.INTERNAL_ERROR);
            } catch (Exception ex) {
                log.error("Error while creating or loading the handle to create a DOI with the same value.", ex);
                throw ex;
            }
            if (handle == null) {
                log.error("We were not able to create or load a handle for " + dso.getTypeText() + " " + dso.getID()
                        + ". Unable to crate DOI if there is no handle.");
                throw new DOIIdentifierException("Unable to load or create handle. Unable to reuse handle for DOI registration.",
                        DOIIdentifierException.INTERNAL_ERROR);
            }
            doiIdentifier=handle;
        }
                    
        doiRow.setColumn("doi", doiIdentifier);
        doiRow.setColumn("resource_type_id", dso.getType());
        doiRow.setColumn("resource_id", dso.getID());
        doiRow.setColumnNull("status");
        if (0 == DatabaseManager.update(context, doiRow))
        {
            throw new RuntimeException("Cannot save DOI to databse for unkown reason.");
        }
        
        return doiRow;
    }
}
