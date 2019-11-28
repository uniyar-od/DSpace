package org.dspace.app.cris.util;

import java.beans.PropertyEditor;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.dspace.app.cris.model.ACrisObject;
import org.dspace.app.cris.model.CrisConstants;
import org.dspace.app.cris.model.VisibilityConstants;
import org.dspace.app.cris.model.jdyna.ACrisNestedObject;
import org.dspace.app.cris.model.jdyna.DynamicObjectType;
import org.dspace.app.cris.model.jdyna.DynamicPropertiesDefinition;
import org.dspace.app.cris.model.jdyna.DynamicTypeNestedObject;
import org.dspace.app.cris.service.ApplicationService;
import org.dspace.content.IMetadataValue;
import org.dspace.content.MetadataValue;
import org.dspace.content.MetadataValueVolatile;
import org.dspace.content.authority.Choices;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Utils;
import org.dspace.utils.DSpace;

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
	MetadatumAuthorityDecorator[] getAllMetadata(ACO item, boolean onlyPub, boolean filterProperty, String module) {
		List<IContainable> metadataFirstLevel = new ArrayList<IContainable>();
        List<IContainable> metadataNestedLevel = new LinkedList<IContainable>();
        
        List<MetadatumAuthorityDecorator> metadatumAuthDec = null;
		int entity = -1;
		try {
			if (entity > CrisConstants.CRIS_DYNAMIC_TYPE_ID_START) {
	            DynamicObjectType type = getApplicationService().get(DynamicObjectType.class, entity);
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
			
			metadatumAuthDec = getAllMetadata(item, metadataFirstLevel,
	                metadataNestedLevel, onlyPub, filterProperty, module);
			
			return metadatumAuthDec.toArray(new MetadatumAuthorityDecorator[metadatumAuthDec.size()]);
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
	List<MetadatumAuthorityDecorator> getAllMetadata(ACO item, List<IContainable> metadata, List<IContainable> metadataNestedLevel, boolean onlyPub, boolean filterProperty, String module)  {
		
		List<MetadatumAuthorityDecorator> metadatumAuthDec = new ArrayList<MetadatumAuthorityDecorator>();
		
		for (IContainable containable : metadata) {
			if (containable instanceof ADecoratorPropertiesDefinition) {
				@SuppressWarnings("rawtypes")
				List<MetadatumAuthorityDecorator> m = getMetadata(item, (ADecoratorPropertiesDefinition) containable, onlyPub);
				metadatumAuthDec.addAll(m);
			}
		}

        for (IContainable nestedContainable : metadataNestedLevel)
        {
        	ATNO typo = getApplicationService().findTypoByShortName(
        			item.getClassTypeNested(), nestedContainable.getShortName());
        	
        	List<ACNO> acno = getApplicationService().getNestedObjectsByParentIDAndShortname(item.getLegacyId(), typo.getShortName(), item.getClassNested());
        	
        	if(acno != null && !acno.isEmpty()) {
            	List<NTP> ntps = null;
            	try {
            	    ntps = getApplicationService().findMaskByShortName(typo.getClass(), typo.getShortName());
                }
                catch (Exception e) {
                    log.error(e.getMessage());
                }
            	if (ntps != null) {
            		for (NTP ntp : ntps) {
            		    
                        for (ACNO nestedObject : acno) {
                            
                            List<NP> nProprieties = nestedObject.getAnagrafica4view().get(ntp.getShortName());

                            if (nProprieties != null && !nProprieties.isEmpty()) {
                                for (NP prop : nProprieties) {
                                    if (onlyPub && prop.getVisibility() != VisibilityConstants.PUBLIC)
                                        continue;
                                    
                                    MetadataValueVolatile copy = new MetadataValueVolatile();
                                    
                                    copy.element = ntp.getShortName();
                                    copy.schema = typo.getShortName();
                                    
                                    Object val = prop.getObject();
                                    if (val instanceof ACrisObject) {
                                        String authority = ResearcherPageUtils.getPersistentIdentifier((ACrisObject) val);
                                        String value = ((ACrisObject) val).getName();
                                        copy.setAuthority(authority);;
                                        copy.setConfidence(Choices.CF_ACCEPTED);
                                        copy.setValue(value);
                                    } else {
                                        PropertyEditor propertyEditor = prop.getTypo().getRendering()
                                                .getPropertyEditor(null);
                                        propertyEditor.setValue(val);
                                        String value = propertyEditor.getAsText();
                                        copy.setAuthority(null);;
                                        copy.setConfidence(Choices.CF_UNSET);
                                        copy.setValue(value);                                        
                                    }
                                    
                                    metadatumAuthDec.add(new MetadatumAuthorityDecorator(copy, ntp));
                                }
                            }
                            else {
                                MetadataValueVolatile copy = new MetadataValueVolatile(typo.getShortName(), ntp.getShortName(), null, null, null);
                                copy.setValue(MetadataValue.PARENT_PLACEHOLDER_VALUE);
                                copy.setAuthority(null);
                                copy.setConfidence(Choices.CF_UNSET);
                                metadatumAuthDec.add(new MetadatumAuthorityDecorator(copy, ntp));
                            }
                        }
                        
            		}
            	}
        	}
		}
        
        if (filterProperty) {
        	// Filter all metadatum
            List<MetadatumAuthorityDecorator> filteredMetadatumAD = new ArrayList<MetadatumAuthorityDecorator>();
            
        	for (MetadatumAuthorityDecorator mad : metadatumAuthDec) {
        		IMetadataValue m = mad.getMetadatum();
        	    String meta = Utils.standardize(m.getSchema(), m.getElement(), m.getQualifier(), ".");
        		boolean filtered = ConfigurationManager.getBooleanProperty(module, module + ".filtered." + meta);
        		if (!filtered)
        			filteredMetadatumAD.add(mad);
        	}
        	
        	return filteredMetadatumAD;
        }
        else
        	return metadatumAuthDec;
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
	List<MetadatumAuthorityDecorator> getMetadata(ACO item, ADecoratorPropertiesDefinition decorator, boolean onlyPub) {
		
		List<MetadatumAuthorityDecorator> metadatum = new ArrayList<MetadatumAuthorityDecorator>();
		String schema = "cris" + item.getPublicPath();
		for (IMetadataValue m : item.getMetadata(schema, decorator.getShortName(), "", "", onlyPub))
			metadatum.add(new MetadatumAuthorityDecorator(m, (PropertiesDefinition)decorator.getObject()));
		
		return metadatum;
	}
}
