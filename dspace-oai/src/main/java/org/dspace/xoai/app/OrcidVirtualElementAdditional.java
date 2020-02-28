/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.xoai.app;

import org.apache.commons.lang.StringUtils;
import org.dspace.content.Item;
import org.dspace.content.Metadatum;
import org.dspace.core.Context;
import org.dspace.xoai.util.ItemUtils;

import com.lyncode.xoai.dataprovider.xml.xoai.Element;
import com.lyncode.xoai.dataprovider.xml.xoai.Metadata;

public class OrcidVirtualElementAdditional implements XOAIItemCompilePlugin {

	@Override
	public Metadata additionalMetadata(Context context, Metadata metadata, Item item) {
		System.out.println("##############HERE#################");
        Element personElement = ItemUtils.create("person");
        Element identifierElement = ItemUtils.create("identifier");
        Element orcidElement = ItemUtils.create("orcid");
        Element noneElement = ItemUtils.create("none");
        
        Metadatum[] values= item.getMetadata("dc", "contributor", "author", Item.ANY);
        for(Metadatum val : values) {
        	if(StringUtils.isNotBlank(val.authority)) {
        		String orcid = "ABC-DEFG-HILM-NOPQ"; //TODO retrieved from "authority" core
        		noneElement.getField().add(ItemUtils.createValue("value", orcid));
        		noneElement.getField().add(ItemUtils.createValue("authority", val.authority));
        		noneElement.getField().add(ItemUtils.createValue("confidence", ""+val.confidence));
        	}
            else {
            	noneElement.getField().add(ItemUtils.createValue("value", val.value));            	
            }
        }
        orcidElement.getElement().add(noneElement);
        identifierElement.getElement().add(orcidElement);
        personElement.getElement().add(identifierElement);
        metadata.getElement().add(personElement);
		return metadata;
	}

}
