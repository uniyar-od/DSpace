/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.dspace.app.util.DCInputsReaderException;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.service.EditItemService;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Service implementation for the EditItem object.
 * This class is responsible for all business logic calls for the Item object and is autowired by spring.
 *
 * @author Pascarelli Luigi Andrea (luigiandrea.pascarelli at 4science dot it)
 */
public class EditItemServiceImpl implements EditItemService {

    @Autowired(required = true)
    private ItemService itemService;

    @Override
    public void deleteWrapper(Context context, EditItem inProgressSubmission) throws SQLException, AuthorizeException {
        try {
            getItemService().delete(context, inProgressSubmission.getItem());
        } catch (IOException e) {
            throw new SQLException(e);
        }
    }

    @Override
    public void update(Context context, EditItem inProgressSubmission) throws SQLException, AuthorizeException {
        getItemService().update(context, inProgressSubmission.getItem());
    }

    public ItemService getItemService() {
        return itemService;
    }

    @Override
    public void move(Context context, EditItem inProgressSubmission, Collection fromCollection, Collection toCollection)
        throws DCInputsReaderException {
        // TODO Auto-generated method stub

    }

    @Override
    public EditItem find(Context context, UUID id) throws SQLException {
        return new EditItem(context, getItemService().find(context, id));
    }

    @Override
    public int countTotal(Context context) throws SQLException {
        return itemService.countTotal(context);
    }

    @Override
    public Iterator<EditItem> findAll(Context context, int pageSize, int offset) throws SQLException {
        Iterator<Item> items = itemService.findAll(context, pageSize, offset);
        Iterable<Item> iterable = () -> items;
        Stream<Item> targetStream = StreamSupport.stream(iterable.spliterator(), false);
        return targetStream.map(x -> new EditItem(context, x)).iterator();
    }

    @Override
    public List<EditItem> findBySubmitter(Context context, EPerson ep, int pageSize, int offset) throws SQLException {
        List<Item> items = itemService.findBySubmitter(context, ep, pageSize, offset);
        return items.stream().map(x -> new EditItem(context, x)).collect(Collectors.toList());
    }

    @Override
    public int countBySubmitter(Context context, EPerson ep) throws SQLException {
        return itemService.countBySubmitter(context, ep);
    }

}
