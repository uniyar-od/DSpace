package org.dspace.app.cris.content.generator;

import java.sql.SQLException;

import org.apache.commons.lang.StringUtils;
import org.dspace.app.cris.model.ResearcherPage;
import org.dspace.app.cris.service.ApplicationService;
import org.dspace.content.Item;
import org.dspace.content.Metadatum;
import org.dspace.content.generator.SubmitterValueGenerator;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.utils.DSpace;

public class ResearcherValueGenerator extends SubmitterValueGenerator {
	
	ApplicationService applicationService = new DSpace().getServiceManager().getServiceByName("applicationService", ApplicationService.class);
	@Override
	public Metadatum[] generator(Context context,Item targetItem, Item templateItem, Metadatum metadatum, String extraParams) {
		Metadatum[] m = new Metadatum[1];
		m[0] = metadatum;
		try {
			EPerson eperson = targetItem.getSubmitter();
			ResearcherPage rp = applicationService.getResearcherPageByEPersonId(eperson.getID());
			if(rp != null){
					metadatum.value = rp.getMetadata(extraParams); 
			}else{
				m = super.generator(context, targetItem, templateItem, metadatum, extraParams);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (StringUtils.isNotBlank(m[0].value)){
			return m;
		}
		else {
			return new Metadatum[0];
		}
	}

}
