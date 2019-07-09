package org.dspace.xoai.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.dspace.content.Metadatum;
import org.dspace.core.ConfigurationManager;

public class MetadataMapper {

	private static Logger log = LogManager.getLogger(MetadataMapper.class);
	
	public static String MAP_PREFIX = "oai.map";
	private String module;
	
	public static String SEPARATOR = ",";
    public static String VALUE_INDEX = "oai.value";
	
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

	/***
	 * Mappings item properties to virtual property
	 * 
	 * @param name The object type
	 * @return The array of fixed value virtual metadata
	 */
	public List<Metadatum> fixedValues(String type) {
		List<Metadatum> metadatumList = new ArrayList<Metadatum>();
		String index = VALUE_INDEX + "." + type;

		String objectIndex = ConfigurationManager.getProperty(module, index);
		if (objectIndex == null || objectIndex.trim().length() <= 0)
			return metadatumList;

		objectIndex = objectIndex.trim();
		String[] props = objectIndex.split(SEPARATOR);
		for (String p : props) {
			String meta = p;
			// remove prefix
			if (meta.length() > index.length()) {
				meta = meta.substring(index.length() + 1);
			}
			String value = ConfigurationManager.getProperty(module, p);

			Metadatum metadatum = new Metadatum();
			String[] m = meta.split("\\.");
			if (m.length < 2)
				continue;

			if (m.length > 0)
				metadatum.schema = m[0];
			if (m.length > 1)
				metadatum.element = m[1];
			if (m.length > 2)
				metadatum.qualifier = m[2];
			if (m.length == 4)
				metadatum.language = m[3];
			else {
				for (int i = 3; i < m.length; i++)
					metadatum.qualifier += '.' + m[i];
			}

			metadatum.authority = null;
			metadatum.value = value;

			metadatumList.add(metadatum);
		}

		return metadatumList;
	}
}
