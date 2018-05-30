/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.repository.patch;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.dspace.app.rest.exception.PatchBadRequestException;
import org.dspace.app.rest.exception.UnprocessableEntityException;
import org.dspace.app.rest.model.patch.Operation;
import org.dspace.app.rest.model.patch.Patch;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;

/**
 * The base class for resource PATCH requests.
 *
 * @author Michael Spalti
 */
public abstract class AbstractResourcePatch {

    /**
     * Handles the patch operations, delegating actions to sub-class implementations. If no sub-class method
     * is provided, the default method throws a PatchUnprocessableEntityException.
     *
     * @param context
     * @param apiCategory
     * @param model
     * @param uuid
     * @param patch
     * @throws UnprocessableEntityException
     * @throws PatchBadRequestException
     * @throws SQLException
     * @throws AuthorizeException
     */
    public void patch(Context context, String apiCategory, String model, UUID uuid, Patch patch)
        throws UnprocessableEntityException, PatchBadRequestException, SQLException, AuthorizeException {

        List<Operation> operations = patch.getOperations();

        // Note: the list of possible operations here is taken from JsonPatchConverter class.
        ops: for (Operation op : operations) {
            switch (op.getOp()) {
                case "add":
                    add(context, apiCategory, model, uuid, patch, op);
                    continue ops;
                case "replace":
                    replace(context, apiCategory, model, uuid, patch, op);
                    continue ops;
                case "remove":
                    remove(context, apiCategory, model, uuid, patch, op);
                    continue ops;
                case "copy":
                    copy(context, apiCategory, model, uuid, patch, op);
                    continue ops;
                case "move":
                    move(context, apiCategory, model, uuid, patch, op);
                    continue ops;
                default:
                    // JsonPatchConverter should have thrown error before this point.
                    throw new PatchBadRequestException("Missing or illegal patch operation: " + op.getOp());
            }
        }
    }

    // The default patch methods throw an error when no sub-class implementation is provided.

    protected void add(Context context, String apiCategory, String model, UUID uuid, Patch patch, Operation operation)
        throws UnprocessableEntityException, PatchBadRequestException, SQLException, AuthorizeException {
        throw new UnprocessableEntityException(
            "The add operation is not supported for " + apiCategory + "." + model
        );
    }

    protected void replace(Context context, String apiCategory, String model,
                           UUID uuid, Patch patch, Operation operation)
        throws UnprocessableEntityException, PatchBadRequestException, SQLException, AuthorizeException {
        throw new UnprocessableEntityException(
            "The replace operation is not supported for " + apiCategory + "." + model
        );
    }

    protected void remove(Context context, String apiCategory, String model,
                          UUID uuid, Patch patch, Operation operation)
        throws UnprocessableEntityException, PatchBadRequestException, SQLException, AuthorizeException {
        throw new UnprocessableEntityException(
            "The remove operation is not supported for " + apiCategory + "." + model
        );
    }

    protected void copy(Context context, String apiCategory, String model,
                          UUID uuid, Patch patch, Operation operation)
        throws UnprocessableEntityException, PatchBadRequestException, SQLException, AuthorizeException {
        throw new UnprocessableEntityException(
            "The copy operation is not supported for " + apiCategory + "." + model
        );
    }

    protected void move(Context context, String apiCategory, String model,
                        UUID uuid, Patch patch, Operation operation)
        throws UnprocessableEntityException, PatchBadRequestException, SQLException, AuthorizeException {
        throw new UnprocessableEntityException(
            "The move operation is not supported for " + apiCategory + "." + model
        );
    }

}
