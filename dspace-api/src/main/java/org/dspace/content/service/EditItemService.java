/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.service;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.dspace.content.EditItem;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;

/**
 * Service interface class for the Item object.
 * The implementation of this class is responsible for all business logic calls for the Item object and is autowired
 * by spring
 *
 * @author Pascarelli Luigi Andrea (luigiandrea.pascarelli at 4science dot it)
 */
public interface EditItemService extends InProgressSubmissionService<EditItem, UUID> {

    ItemService getItemService();

    int countTotal(Context context) throws SQLException;

    Iterator<EditItem> findAll(Context context, int pageSize, int offset) throws SQLException;

    List<EditItem> findBySubmitter(Context context, EPerson ep, int pageSize, int offset) throws SQLException;

    int countBySubmitter(Context context, EPerson ep) throws SQLException;

}
