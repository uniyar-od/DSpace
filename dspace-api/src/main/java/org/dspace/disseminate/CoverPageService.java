/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.disseminate;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;

public class CoverPageService {
	
	private static Logger log = Logger.getLogger(CoverPageService.class);
	private boolean includeMappedCollections;
	private HashMap<String,String> configurationFileMap;
	private String defaultConfigFile; 

	private List<String> validTypes;
    
	public HashMap<String, String> getConfigurationFileMap() {
		return configurationFileMap;
	}

	public void setConfigurationFileMap(HashMap<String, String> configurationFileMap) {
		this.configurationFileMap = configurationFileMap;
	}
	
	public String getDefaultConfigFile() {
		return defaultConfigFile;
	}

	public void setDefaultConfigFile(String defualtConfigFile) {
		this.defaultConfigFile = defualtConfigFile;
	}
	
	public List<String> getValidTypes() {
		return validTypes;
	}

	public void setValidTypes(List<String> validTypes) {
		this.validTypes = validTypes;
	}

	public String getConfigFile(String collHandle) {
		String configFile = null;
		if( ( configurationFileMap.containsKey(collHandle) && includeMappedCollections )
				|| ( !configurationFileMap.containsKey(collHandle) && !includeMappedCollections )) {
			
			configFile = configurationFileMap.get(collHandle);
			if(StringUtils.isBlank(configFile) ) {
				configFile = defaultConfigFile;
			}
		}
		return configFile;
	}
	
	public boolean canCreateCover(Bitstream bitstream) {
		try {
			if(isValidType(bitstream)) {
				for(Bundle bndl:bitstream.getBundles()) {
					if(StringUtils.equals(bndl.getName(),"ORIGINAL")) {
						return true;
					}
				}
			}
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
		}
		return false;
	}
	
	public boolean isValidType(String type) {
		return validTypes.contains(type);
	}
	
	public boolean isValidType(Bitstream bitstream) {
		return isValidType(bitstream.getFormat().getMIMEType());
	}

	public boolean isIncludeMappedCollections() {
		return includeMappedCollections;
	}

	public void setIncludeMappedCollections(boolean includeMappedCollections) {
		this.includeMappedCollections = includeMappedCollections;
	}
}
