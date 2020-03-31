package org.dspace.app.cris.unpaywall.services;

import org.dspace.app.cris.unpaywall.dao.UnpaywallDAO;
import org.dspace.app.cris.unpaywall.model.Unpaywall;

import it.cilea.osd.common.model.Identifiable;
import it.cilea.osd.common.service.PersistenceService;

public class UnpaywallPersistenceService extends PersistenceService
{
    
    private UnpaywallDAO unpaywallDAO;
    
    public void init() 
    {
        unpaywallDAO = (UnpaywallDAO) getDaoByModel(Unpaywall.class);
    }
    
    public Unpaywall uniqueByDOI(String DOI)
    {
        return unpaywallDAO.uniqueByDOI(DOI);
    }

    public Unpaywall uniqueByDOIAndItemID(String DOI, Integer ID)
    {
        return unpaywallDAO.uniqueByDOIAndItemID(DOI, ID);
    }
    
    @Override
    public void evict(Identifiable identifiable)
    {
        // TODO Auto-generated method stub
        
    }
}
