/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.repository.patch;

import java.sql.SQLException;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.dspace.app.rest.exception.PatchBadRequestException;
import org.dspace.app.rest.exception.UnprocessableEntityException;
import org.dspace.app.rest.model.patch.Operation;
import org.dspace.app.rest.model.patch.Patch;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.service.EPersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This is an example to show a draft implementation for eperson resource patches based on the Michael Spalti proposal.
 *
 * @author Andrea Bollini (andrea.bollini at 4science.it)
 */
@Component
public class EPersonPatch extends AbstractResourcePatch {

    private static final String OPERATION_PATH_WITHDRAW = "/canLogin";

    private static final String OPERATION_PATH_DISCOVERABLE = "/password";

    private static final Logger log = Logger.getLogger(EPersonPatch.class);

    @Autowired
    EPersonService es;

    /**
     * Replace implementation
     *
     * @param context
     * @param apiCategory
     * @param model
     * @param id
     * @param patch
     * @param operation
     * @throws UnprocessableEntityException
     * @throws PatchBadRequestException
     * @throws SQLException
     * @throws AuthorizeException
     */
    @Override
    protected void replace(Context context, String apiCategory, String model, UUID id, Patch patch, Operation operation)
            throws UnprocessableEntityException, PatchBadRequestException, SQLException, AuthorizeException {
        // FIXME the method signature should be simplified we already know that it is a replace
        // we could also pass the object instead than the apiCategory/model and id
        EPerson eperson = es.find(context, id);
        switch (operation.getPath()) {
            case OPERATION_PATH_WITHDRAW:
                canLogin(context, eperson, (Boolean) operation.getValue());
                break;
            case OPERATION_PATH_DISCOVERABLE:
                changePassword(eperson, (String) operation.getValue());
                break;
            default:
                throw new UnprocessableEntityException("Unrecognized patch operation path: " + operation.getPath());
        }
    }

    private void canLogin(Context context, EPerson eperson, Boolean canLogin)
            throws PatchBadRequestException, SQLException, AuthorizeException {
        if (canLogin == null) {
            // this check can be probably moved in the AbstractResourcePatch class as it is mandate by the json+patch
            // specification
            throw new PatchBadRequestException("Boolean value not provided for canLogin operation.");
        }
        eperson.setCanLogIn(canLogin);
    }

    /**
     * Sets discoverable field on the item.
     *
     * @param item
     * @param isDiscoverable
     * @throws PatchBadRequestException
     */
    private void changePassword(EPerson eperson, String newPassword) throws PatchBadRequestException {

        if (newPassword == null) {
            // again here we should have more domain specific check, i.e. the new password respect
            // minimum security level?
            throw new PatchBadRequestException("You must supply a new password");
        }
        es.setPassword(eperson, newPassword);
    }

}
