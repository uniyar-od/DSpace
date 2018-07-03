/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content;

import java.sql.SQLException;
import java.util.UUID;

import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;

public class EditItem implements InProgressSubmission<UUID> {

    private Item item;

    private Context context;

    public EditItem(Context context, Item item) {
        this.context = context;
        this.item = item;
    }

    @Override
    public UUID getID() {
        return item.getID();
    }

    @Override
    public void update() throws SQLException, AuthorizeException {
        item.getItemService().update(context, item);
    }

    @Override
    public Item getItem() {
        return item;
    }

    @Override
    public Collection getCollection() {
        try {
            return (Collection) item.getItemService().getParentObject(context, item);
        } catch (SQLException e) {
            throw new RuntimeException();
        }
    }

    @Override
    public EPerson getSubmitter() throws SQLException {
        return item.getSubmitter();
    }

    @Override
    public boolean hasMultipleFiles() {
        return true;
    }

    @Override
    public void setMultipleFiles(boolean b) {
    }

    @Override
    public boolean hasMultipleTitles() {
        return true;
    }

    @Override
    public void setMultipleTitles(boolean b) {
    }

    @Override
    public boolean isPublishedBefore() {
        return true;
    }

    @Override
    public void setPublishedBefore(boolean b) {
    }

    @Override
    public int getType() {
        return item.getType();
    }

    @Override
    public String getName() {
        return item.getName();
    }

    @Override
    public String getHandle() {
        return item.getHandle();
    }

    @Override
    public void setCollection(Collection collection) {
        item.setOwningCollection(collection);
    }
}
