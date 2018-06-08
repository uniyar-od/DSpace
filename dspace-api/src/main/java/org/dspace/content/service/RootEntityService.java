/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.service;

import java.io.Serializable;
import java.sql.SQLException;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.RootObject;
import org.dspace.core.Context;

public interface RootEntityService<T extends RootObject, PK extends Serializable> {

    void updateLastModified(Context context, T dSpaceObject) throws SQLException, AuthorizeException;

    boolean isSupportsTypeConstant(int type);

    /**
     * Generic find for when the precise type of a DSO is not known, just the
     * a pair of type number and database ID.
     *
     * @param context - the context
     * @param id      - id within table of type'd objects
     * @return the object found, or null if it does not exist.
     * @throws SQLException only upon failure accessing the database.
     */
    public T find(Context context, PK id) throws SQLException;
}
