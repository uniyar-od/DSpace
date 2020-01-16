package org.dspace.app.cris.integration.authority;

import org.dspace.core.Context;
import org.dspace.event.Consumer;
import org.dspace.event.Event;

public class UnpaywallConsumer implements Consumer
{
    
    
//    private UnpaywallPersistenceService unpaywallPersistenceServices = new DSpace().getServiceManager().getServiceByName("unpaywallPersistenceServices", UnpaywallPersistenceService.class);
    public void consume(Context ctx, Event event) throws Exception {
//    DSpaceObject dso = event.getSubject(ctx);
//    if (dso instanceof Item) {
//        Item item = (Item) dso;
//        if (item.isArchived()) {
//            // 1)check the internal contributors
//            Set<String> listAuthoritiesManager = ChoiceAuthorityManager.getManager().getAuthorities();
//            for (String crisAuthority : listAuthoritiesManager) {
//                List<String> listMetadata = ChoiceAuthorityManager.getManager()
//                        .getAuthorityMetadataForAuthority(crisAuthority);
//
//                for (String metadata : listMetadata) {
//                    ChoiceAuthority choiceAuthority = ChoiceAuthorityManager.getManager()
//                            .getChoiceAuthority(metadata);
//                    if (ORCIDAuthority.class.isAssignableFrom(choiceAuthority.getClass())) {
//                        // 2)check for each internal contributors if has
//                        // authority
//                        Metadatum[] Metadatums = item.getMetadataByMetadataString(metadata);
//                        for (Metadatum dcval : Metadatums) {
//                            String authority = dcval.authority;
//                            if (StringUtils.isNotBlank(authority)) {
//                                // 3)check the orcid preferences
//                                boolean isAPreferiteWork = unpaywallPersistenceServices.findByDOI(doi);
//                                        // 4)if the publications match the
//                                // preference add publication to queue
//                                if (isAPreferiteWork) {
//                                    unpaywallPersistenceServices.(authority, dso);
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
}

    @Override
    public void initialize() throws Exception
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void end(Context ctx) throws Exception
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void finish(Context ctx) throws Exception
    {
        // TODO Auto-generated method stub
        
    }
}
