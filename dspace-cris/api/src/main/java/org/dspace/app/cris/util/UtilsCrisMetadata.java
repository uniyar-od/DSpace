package org.dspace.app.cris.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.dspace.app.cris.model.ACrisObject;
import org.dspace.app.cris.model.CrisConstants;
import org.dspace.app.cris.model.jdyna.ACrisNestedObject;
import org.dspace.app.cris.model.jdyna.DynamicObjectType;
import org.dspace.app.cris.model.jdyna.DynamicPropertiesDefinition;
import org.dspace.app.cris.model.jdyna.DynamicTypeNestedObject;
import org.dspace.app.cris.service.ApplicationService;
import org.dspace.content.Metadatum;
import org.dspace.core.ConfigurationManager;
import org.dspace.utils.DSpace;
import org.hibernate.LazyInitializationException;

import it.cilea.osd.jdyna.model.ADecoratorPropertiesDefinition;
import it.cilea.osd.jdyna.model.ANestedPropertiesDefinition;
import it.cilea.osd.jdyna.model.ANestedProperty;
import it.cilea.osd.jdyna.model.ATypeNestedObject;
import it.cilea.osd.jdyna.model.IContainable;
import it.cilea.osd.jdyna.model.PropertiesDefinition;
import it.cilea.osd.jdyna.model.Property;

public class UtilsCrisMetadata {
	/** log4j logger */
	private static Logger log = Logger.getLogger(UtilsCrisMetadata.class);
	
	 /***
     * Cris application service
     * @return
     */
    private static ApplicationService getApplicationService()
    {
        return new DSpace().getServiceManager().getServiceByName(
                "applicationService", ApplicationService.class);
    }
    
	/***
	 * Retrieve all metadata.
	 * 
	 * @param item The cris object
	 * @param onlyPub if onlyPub is true retrieve only public properties.
	 * @param filterProperty if filterProperty is true property are filtered using module file
	 * @param module The module file name
	 * @return The read metadatum.
	 */
	public static <ACO extends ACrisObject<P, TP, NP, NTP, ACNO, ATNO>, P extends Property<TP>, TP extends PropertiesDefinition, NP extends ANestedProperty<NTP>, NTP extends ANestedPropertiesDefinition, ACNO extends ACrisNestedObject<NP, NTP, P, TP>, ATNO extends ATypeNestedObject<NTP>> 
	Metadatum[] getAllMetadata(ACO item, boolean onlyPub, boolean filterProperty, String module) {
		List<IContainable> metadataFirstLevel = new ArrayList<IContainable>();
        List<IContainable> metadataNestedLevel = new LinkedList<IContainable>();
        
        List<Metadatum> metadatum = null;
		int entity = -1;
		try {
			if (entity > CrisConstants.CRIS_DYNAMIC_TYPE_ID_START) {
	            DynamicObjectType type = getApplicationService().get(DynamicObjectType.class, CrisConstants.CRIS_DYNAMIC_TYPE_ID_START);
	            List<DynamicPropertiesDefinition> tps = type.getMask();            
	            for (DynamicPropertiesDefinition tp : tps) {
	                IContainable ic = getApplicationService().findContainableByDecorable(tp.getDecoratorClass(), tp.getId());
	                if (ic != null) {
	                    metadataFirstLevel.add(ic);
	                }
	            }
	            List<DynamicTypeNestedObject> ttps = type.getTypeNestedDefinitionMask();
	            for (DynamicTypeNestedObject ttp : ttps) {
	                IContainable ic = getApplicationService().findContainableByDecorable(
	                        ttp.getDecoratorClass(), ttp.getId());
	                if (ic != null){
	                    metadataNestedLevel.add(ic);
	                }
	            }
	        } else {
	        	metadataFirstLevel = getApplicationService().findAllContainables(item.getClassPropertiesDefinition());
	            List<ATNO> ttps = getApplicationService().getList(item.getClassTypeNested());
	            
	            for (ATNO ttp : ttps){
	                IContainable ic = getApplicationService().findContainableByDecorable(ttp.getDecoratorClass(), ttp.getId());
	                if (ic != null){
	                    metadataNestedLevel.add(ic);
	                }
	            }
	        }
			
			metadatum = getAllMetadata(item, metadataFirstLevel,
	                metadataNestedLevel, onlyPub, filterProperty, module);
			
			return metadatum.toArray(new Metadatum[metadatum.size()]);
		}
		catch (Exception e) {
			log.error("error in reading metadata of object " + item.getPublicPath() + " with id " + item.getID(), e);
			return null;
		}
	}
	
	/***
	 * Retrieve all metadata
	 * 
	 * @param item The item
	 * @param metadata The list of IContainable related to the first level
	 * @param metadataNestedLevel The list of IContainable related to the nested level
	 * @param onlyPub Set to true if only public metadata have to be retrieved
	 * @param filterProperty Set to true if metadata can be filtered
	 * @param module The module configuration file name
	 * @return The list of metadata
	 */
	@SuppressWarnings("unchecked")
	private static <ACO extends ACrisObject<P, TP, NP, NTP, ACNO, ATNO>, P extends Property<TP>, TP extends PropertiesDefinition, NP extends ANestedProperty<NTP>, NTP extends ANestedPropertiesDefinition, ACNO extends ACrisNestedObject<NP, NTP, P, TP>, ATNO extends ATypeNestedObject<NTP>>
	List<Metadatum> getAllMetadata(ACO item, List<IContainable> metadata, List<IContainable> metadataNestedLevel, boolean onlyPub, boolean filterProperty, String module)  {
		
		List<Metadatum> metadatum = new ArrayList<Metadatum>();
		
		for (IContainable containable : metadata) {
			if (containable instanceof ADecoratorPropertiesDefinition) {
				@SuppressWarnings("rawtypes")
				List<Metadatum> m = getMetadata(item, (ADecoratorPropertiesDefinition) containable, onlyPub);
				
				metadatum.addAll(m);
			}
		}

        for (IContainable nestedContainable : metadataNestedLevel)
        {
        	ATNO typo = getApplicationService().findTypoByShortName(
        			item.getClassTypeNested(), nestedContainable.getShortName());
        	List<NTP> ntps = null;
        	try {
                ntps = typo.getMask();
            }
            catch (LazyInitializationException e) {
                ntps = getApplicationService().findMaskByShortName(typo.getClass(), typo.getShortName());
            }
        	if (ntps != null) {
        		for (NTP ntp : ntps) {
        			item.getMetadata(typo.getShortName(), ntp.getShortName(), null, null, onlyPub);
        		}
        	}
		}
        
        if (filterProperty) {
        	// Filter all metadatum
            List<Metadatum> filteredMetadatum = new ArrayList<Metadatum>();
            
        	for (Metadatum m : metadatum) {
        		String meta = m.schema + "." + m.element;
        		if (m.qualifier != null && m.qualifier.trim().length() > 0)
        			meta += "." + m.qualifier;
        		boolean filtered = ConfigurationManager.getBooleanProperty(module, module + ".filtered." + meta);
        		if (!filtered)
        			filteredMetadatum.add(m);
        	}
        	
        	return filteredMetadatum;
        }
        else
        	return metadatum;
	}

	/***
	 * Retrieve all metadata
	 * 
	 * @param decorator
	 * @param item The item
	 * @param onlyPub Set to true if only public metadata have to be retrieved
	 * @return The list of metadata
	 */
	@SuppressWarnings("rawtypes")
	private static <ACO extends ACrisObject<P, TP, NP, NTP, ACNO, ATNO>, P extends Property<TP>, TP extends PropertiesDefinition, NP extends ANestedProperty<NTP>, NTP extends ANestedPropertiesDefinition, ACNO extends ACrisNestedObject<NP, NTP, P, TP>, ATNO extends ATypeNestedObject<NTP>>
	List<Metadatum> getMetadata(ACO item, ADecoratorPropertiesDefinition decorator, boolean onlyPub) {
		
		List<Metadatum> metadatum = new ArrayList<Metadatum>();
		PropertiesDefinition real = (PropertiesDefinition)decorator.getReal();
		String shortName = real.getShortName();

		String schema = "cris" + item.getPublicPath();
		for (Metadatum m : item.getMetadata(schema, shortName, "", "", onlyPub))
			metadatum.add(m);
		
		return metadatum;
	}
}
