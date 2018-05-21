/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.authority.rest;

<<<<<<< HEAD
import org.dspace.authority.AuthorityValue;

import java.io.IOException;
import java.util.List;
=======
import org.dspace.authority.SolrAuthorityInterface;
>>>>>>> refs/heads/dspace-6-rs

/**
 *
 * @author Antoine Snyers (antoine at atmire.com)
 * @author Kevin Van de Velde (kevin at atmire dot com)
 * @author Ben Bosman (ben at atmire dot com)
 * @author Mark Diggory (markd at atmire dot com)
 */
public abstract class RestSource implements SolrAuthorityInterface {

    protected RESTConnector restConnector;

    public RestSource(String url) {
        this.restConnector = new RESTConnector(url);
    }

<<<<<<< HEAD
    public abstract List<AuthorityValue> queryAuthorities(String field, String text, int start, int max) throws IOException;
    public abstract List<AuthorityValue> queryAuthorities(String text, int max) throws IOException;

    public abstract AuthorityValue queryAuthorityID(String id);
=======
>>>>>>> refs/heads/dspace-6-rs
}
