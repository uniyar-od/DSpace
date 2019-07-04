package org.dspace.xoai.util;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.dspace.content.Metadatum;
import org.dspace.core.ConfigurationManager;

public class MetadataMapper {

	private static Logger log = LogManager.getLogger(MetadataMapper.class);
	
	public static String MAP_PREFIX = "oai.map";
	private String module;
	
	/***
	 * Initialize the mapper with the configuration file (module name).
	 * 
	 * @param module The module name
	 */
	public MetadataMapper(String module) {
		this.module = module;
	}
	
	/***
	 * Mappings cris property to virtual property.
	 * 
	 * Sample of virtual metadata used inside oai_cerif.xsl are:
	 * 		cris object journal:
	 * 			crisitem.crisvprop.journalsname    (the name of the journal)
	 * 			crisitem.crisvprop.issn            (the issn of the journal)
	 * 
	 * @param metadatum The current metadata value
	 * @return The mapped metadada or the original metadata value.
	 */
	public Metadatum map(Metadatum metadatum) {
		String metadata = metadatum.schema + "." + metadatum.element;
		if (metadatum.qualifier != null && metadatum.qualifier.trim().length() > 0) {
			metadata += "." + metadatum.qualifier;
//			if (metadatum.language != null && metadatum.language.trim().length() > 0)
//				metadata += "." + metadatum.language;
		}
		
		String mapping = ConfigurationManager.getProperty(module, MAP_PREFIX + "." + metadata);
		if (mapping != null && mapping.trim().length() > 0) {
			mapping = mapping.trim();
			
			String m[] = mapping.split("\\.");
			if (m.length < 3) {
				log.error("Error in metadata mapping. The metadata has no element or qualifier: " + metadata);
				return metadatum;
			} else {
				metadatum.schema = m[0];
				metadatum.element = m[1];
	        	metadatum.qualifier = m[2];
	        	metadatum.language = null;
	        
	        	if (m.length == 4) {
	        		metadatum.language = m[4];
	        	} else {
	        		for (int i = 4; i < m.length; i++)
	        			metadatum.qualifier += m[i];
	        	}
	        }
		}
		return metadatum;
	}

}
