package org.dspace.app.cris.unpaywall.dao;

import org.dspace.app.cris.unpaywall.model.Unpaywall;

import it.cilea.osd.common.dao.PaginableObjectDao;

public interface UnpaywallDao extends PaginableObjectDao<Unpaywall, Integer> {
    
    public Unpaywall uniqueByDOI(String DOI);
    
    public Unpaywall uniqueByDOIAndItemID(String DOI, Integer ItemID);

}
