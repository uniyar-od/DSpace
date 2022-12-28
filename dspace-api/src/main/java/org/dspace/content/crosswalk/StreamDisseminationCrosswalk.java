/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.crosswalk;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Iterator;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.DSpaceObject;
import org.dspace.core.Context;


/**
 * A class implementing this interface crosswalk metadata directly
 * from a DSpace Object to an output stream, in a specific format.
 * <p>
 * Stream-oriented crosswalks are intended to be used for metadata
 * formats which are either (a) not XML-based, or (b) too bulky for the
 * DOM-ish in-memory model developed for the METS and IMSCP packagers.
 * The METS packagers (all subclasses of AbstractMETSDisseminator / AbstractMETSIngester
 * are equipped to call these crosswalks as well as the XML-based ones,
 * just refer to the desired crosswalk by its plugin name.
 *
 * @author Larry Stone
 * @version $Revision$
 */
public interface StreamDisseminationCrosswalk {

    /**
     * Predicate: Can this disseminator crosswalk the given object.
     *
     * @param context context
     * @param dso     dspace object, e.g. an <code>Item</code>.
     * @return true when disseminator is capable of producing metadata.
     */
    public boolean canDisseminate(Context context, DSpaceObject dso);

    /**
     * Execute crosswalk on the given object, sending output to the stream.
     *
     * @param context the DSpace context
     * @param dso     the  DSpace Object whose metadata to export.
     * @param out     output stream to write to
     * @throws CrosswalkInternalException  (<code>CrosswalkException</code>) failure of the crosswalk itself.
     * @throws CrosswalkObjectNotSupported (<code>CrosswalkException</code>) Cannot crosswalk this kind of DSpace
     *                                     object.
     * @throws IOException                 I/O failure in services this calls
     * @throws SQLException                Database failure in services this calls
     * @throws AuthorizeException          current user not authorized for this operation.
     */
    public void disseminate(Context context, DSpaceObject dso, OutputStream out)
        throws CrosswalkException, IOException, SQLException, AuthorizeException;

    /**
     * Execute crosswalk on the given objects, sending output to the stream.
     *
     * @param context the DSpace context
     * @param dsoIterator  an iterator over the DSpace object to export
     * @param out     output stream to write to
     * @throws CrosswalkInternalException  (<code>CrosswalkException</code>) failure of the crosswalk itself.
     * @throws CrosswalkObjectNotSupported (<code>CrosswalkException</code>) Cannot crosswalk this kind of DSpace
     *                                     object.
     * @throws IOException                 I/O failure in services this calls
     * @throws SQLException                Database failure in services this calls
     * @throws AuthorizeException          current user not authorized for this operation.
     */
    public default void disseminate(Context context, Iterator<? extends DSpaceObject> dsoIterator, OutputStream out)
        throws CrosswalkException, IOException, SQLException, AuthorizeException {
        throw new UnsupportedOperationException("Crosswalk on multiple DSpace object not supported");
    }

    /**
     * Returns the MIME type of the output written in the given stream.
     *
     * @return the output MIME type.
     */
    public String getMIMEType();

    /**
     * Returns true if the output will be publicly readable
     *
     * @return publicly readable state
     */
    public default boolean isPubliclyReadable() {
        return false;
    }
}
