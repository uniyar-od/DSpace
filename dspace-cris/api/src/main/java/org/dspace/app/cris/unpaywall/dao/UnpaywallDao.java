/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.cris.unpaywall.dao;

import org.dspace.app.cris.unpaywall.model.Unpaywall;

import it.cilea.osd.common.dao.PaginableObjectDao;

public interface UnpaywallDao extends PaginableObjectDao<Unpaywall, Integer> {
    
    public Unpaywall uniqueByDOI(String DOI);
    
    public Unpaywall uniqueByDOIAndItemID(String DOI, Integer ItemID);

}
