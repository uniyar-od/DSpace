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
import org.dspace.app.rest.exception.PatchUnprocessableEntityException;
import org.dspace.app.rest.model.patch.Operation;
import org.dspace.app.rest.model.patch.Patch;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Item;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This is the implementation for item resource patches.
 *
 * @author Michael Spalti
 */
@Component
public class ItemPatch extends AbstractResourcePatch {

    private static final String OPERATION_PATH_WITHDRAW = "/withdrawn";

    private static final String OPERATION_PATH_DISCOVERABLE = "/discoverable";

    private static final Logger log = Logger.getLogger(ItemPatch.class);

    @Autowired
    ItemService is;

    /**
     * Replace implementation for the Item PATCH operation.
     *
     * @param context
     * @param apiCategory
     * @param model
     * @param id
     * @param patch
     * @param operation
     * @throws PatchUnprocessableEntityException
     * @throws PatchBadRequestException
     * @throws SQLException
     * @throws AuthorizeException
     */
    @Override
    protected void replace(Context context, String apiCategory, String model, UUID id, Patch patch, Operation operation)
        throws PatchUnprocessableEntityException, PatchBadRequestException, SQLException, AuthorizeException {

        Item item;
        try {
            item = is.find(context, id);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw e;
        }

        switch (operation.getPath()) {
            case OPERATION_PATH_WITHDRAW:
                withdraw(context, item, (Boolean) operation.getValue());
                break;
            case OPERATION_PATH_DISCOVERABLE:
                discoverable(item, (Boolean) operation.getValue());
                break;
            default:
                throw new PatchUnprocessableEntityException(
                    "Unrecognized patch operation path: " + operation.getPath()
                );
        }
    }

    /**
     * Withdraws or reinstates the item based on boolean value provided in the patch request.
     *
     * @param context
     * @param item
     * @param withdrawItem the operation's value
     * @throws PatchBadRequestException
     * @throws SQLException
     * @throws AuthorizeException
     */
    private void withdraw(Context context, Item item, Boolean withdrawItem)
        throws PatchBadRequestException, SQLException, AuthorizeException {

        // TODO https://jira.duraspace.org/browse/DS-3909 requires 422 response when withdrawing a NOT archived item.

        try {
            if (withdrawItem == null) {
                throw new PatchBadRequestException("Boolean value not provided for withdrawal operation.");
            }
            if (withdrawItem) {
                if (item.isWithdrawn()) {
                    // Item is already withdrawn. No-op, 200 response.
                    // The operation is idempotent but there's no need to replace
                    // the current value if we know it will be unchanged.
                    return;
                }
                is.withdraw(context, item);

            } else {
                if (!item.isWithdrawn()) {
                    // No need to reinstate item if it is not withdrawn.
                    // No-op, 200 response.
                    return;
                }
                is.reinstate(context, item);

            }

        } catch (SQLException | AuthorizeException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Sets discoverable field on the item.
     *
     * @param item
     * @param isDiscoverable
     * @throws PatchBadRequestException
     */
    private void discoverable(Item item, Boolean isDiscoverable) throws PatchBadRequestException {

        if (isDiscoverable == null) {
            throw new PatchBadRequestException("Boolean value not provided for discoverable operation.");
        }
        item.setDiscoverable(isDiscoverable);
    }

}
