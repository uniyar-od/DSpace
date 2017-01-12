package org.dspace.app.webui.cris.servlet;

import it.cilea.osd.jdyna.value.DateValue;
import it.cilea.osd.jdyna.value.PointerValue;
import it.cilea.osd.jdyna.widget.WidgetDate;
import it.cilea.osd.jdyna.widget.WidgetPointer;
import it.cilea.osd.jdyna.widget.WidgetTesto;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.dspace.app.cris.integration.authority.AuthoritiesFillerConfig;
import org.dspace.app.cris.integration.authority.CrisConsumer;
import org.dspace.app.cris.integration.authority.ItemMetadataImportFiller;
import org.dspace.app.cris.integration.authority.ItemMetadataImportFillerConfiguration;
import org.dspace.app.cris.integration.authority.ItemMetadataImportFillerConfiguration.MappingDetails;
import org.dspace.app.cris.model.ACrisObject;
import org.dspace.app.cris.model.CrisConstants;
import org.dspace.app.cris.model.ResearchObject;
import org.dspace.app.cris.model.jdyna.DynamicPropertiesDefinition;
import org.dspace.app.cris.model.jdyna.DynamicProperty;
import org.dspace.app.cris.model.jdyna.value.DOPointer;
import org.dspace.app.cris.model.jdyna.value.OUPointer;
import org.dspace.app.cris.model.jdyna.value.ProjectPointer;
import org.dspace.app.cris.model.jdyna.value.RPPointer;
import org.dspace.app.cris.service.ApplicationService;
import org.dspace.app.util.DCInputSet;
import org.dspace.app.util.DCInputsReader;
import org.dspace.app.util.DCInputsReaderException;
import org.dspace.app.util.SubmissionInfo;
import org.dspace.app.webui.servlet.DSpaceServlet;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.dspace.content.WorkspaceItem;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.utils.DSpace;

public class EditSpecialEntity extends DSpaceServlet {
	
	Logger log = Logger.getLogger(EditSpecialEntity.class);
	
	private DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	
	ApplicationService applicationService = new DSpace().getServiceManager().getServiceByName("applicationService", ApplicationService.class);
	AuthoritiesFillerConfig fillerConfig = new DSpace().getSingletonService(AuthoritiesFillerConfig.class);
	ItemMetadataImportFiller imif = (ItemMetadataImportFiller) fillerConfig.getFiller(CrisConsumer.SOURCE_INTERNAL);

	@Override
	protected void doDSGet(Context context, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException,
			SQLException, AuthorizeException {


		String crisid = request.getParameter("entity_id");
		String prop = request.getParameter("entity_property");
		String collid = request.getParameter("collection_id");
		context.turnOffItemWrapper();
		Collection coll = Collection.find(context, Integer.parseInt(collid));
		AuthorizeManager.authorizeAction(context, coll, Constants.ADD);
		
		ResearchObject ro = applicationService.getEntityByCrisId(crisid,ResearchObject.class);
        try {
			DCInputSet dcinputset = new DCInputsReader().getInputs(coll.getHandle());
			WorkspaceItem wi = WorkspaceItem.create(context, coll,
                    true);
			Item item = wi.getItem();
		
			String roTypo = ro.getTypo().getShortName();
			
			item = fillWorkspaceMetadata(prop, dcinputset, item, ro, roTypo);
			item.update();
	        // commit changes to database
	        context.commit();
	
			response.sendRedirect(request.getContextPath() + "/submit?resume=" + wi.getID());
		
        } catch (DCInputsReaderException e) {
			log.error(e.getMessage(), e);
		}
		
	}

	private Item fillWorkspaceMetadata(String prop, DCInputSet dcinputset,Item i,ResearchObject ro, String roType){
		
    	ItemMetadataImportFillerConfiguration conf = imif.getConfigurations().get(prop);
		Map<String ,MappingDetails> metadata2details = conf.getMapping();

		for (String s : metadata2details.keySet())
		{
			if(!dcinputset.isFieldWithSchemaPresent(s)){
				continue;
			}
			MappingDetails mapping = metadata2details.get(s);
			String shortname = roType+mapping.getShortName();
			String[] metadatum = Item.getElements(s);

			DynamicPropertiesDefinition dyPD = applicationService.findPropertiesDefinitionByShortName(ro.getClassPropertiesDefinition(), shortname);
			List<DynamicProperty> dp = ro.getAnagrafica4view().get(shortname);
			for(DynamicProperty d: dp){
				String authority=null;
				String value ="";

				if(d.getValue() instanceof PointerValue ) {
					ACrisObject ao = (ACrisObject) d.getValue().getObject();
					authority = ao.getCrisID();
					value = ao.getName();
				}else if (d.getValue() instanceof DateValue){
					value = df.format(d.getValue().toString());
				}else{
					value =(String) d.getValue().toString();
				}
				int confidence = StringUtils.isNotBlank(authority) ? 600 : 0;
				
				i.addMetadata(metadatum[0], metadatum[1], metadatum[2], metadatum[3], value,authority,confidence);
			}
			
			if(imif.getConfigurations().get(s) != null){
				i = fillWorkspaceMetadata(s, dcinputset, i,ro,roType);
			}
		}

		return i;
    }
	
	

}
