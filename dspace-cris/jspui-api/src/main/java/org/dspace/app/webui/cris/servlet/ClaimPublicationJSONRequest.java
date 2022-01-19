/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * https://github.com/CILEA/dspace-cris/wiki/License
 */
package org.dspace.app.webui.cris.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.dspace.app.cris.integration.BindItemToRP;
import org.dspace.app.webui.json.JSONRequest;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.IMetadataValue;
import org.dspace.content.Item;
import org.dspace.content.MetadataField;
import org.dspace.content.MetadataSchema;
import org.dspace.content.authority.Choices;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.ItemService;
import org.dspace.content.service.MetadataSchemaService;
import org.dspace.core.Context;
import org.dspace.core.I18nUtil;
import org.dspace.core.Utils;

import flexjson.JSONSerializer;

public class ClaimPublicationJSONRequest extends JSONRequest
{
	
	protected ItemService itemService = ContentServiceFactory.getInstance().getItemService();
	
	private MetadataSchemaService metadataSchemaService = ContentServiceFactory.getInstance().getMetadataSchemaService();

    @Override
    public void doJSONRequest(Context context, HttpServletRequest req,
            HttpServletResponse resp) throws IOException, AuthorizeException
    {
        
        String itemS = req.getParameter("item");
        String crisID = req.getParameter("crisid");
        
        List<CheckMetadataPublicationDTO> jsonresp = new ArrayList<CheckMetadataPublicationDTO>();
        
        if(StringUtils.isNotBlank(itemS)) {
            UUID itemId = UUID.fromString(itemS);
            try
            {
                Item item = itemService.find(context, itemId);
                List<MetadataField> metadataFields = BindItemToRP.metadataFieldWithAuthorityRP(context);
                for (MetadataField metadataField : metadataFields)
                {
                    MetadataSchema find = metadataSchemaService.find(context,
                            metadataField.getMetadataSchema().getID());
                    String underscoredField = Utils.standardize(find.getName(),
                            metadataField.getElement(),
                            metadataField.getQualifier(), "_");
                    String standardizeField = Utils.standardize(find.getName(),
                            metadataField.getElement(),
                            metadataField.getQualifier(), ".");
                    List<IMetadataValue> metadatum = item
                            .getMetadataValueInDCFormat(standardizeField);
                    boolean found = false;
                    internal : for (IMetadataValue meta : metadatum)
                    {
                        if(StringUtils.isNotBlank(meta.getAuthority())) {
                            if(StringUtils.isNotBlank(crisID)) {
                                if(crisID.equals(meta.getAuthority()) && meta.getConfidence() == Choices.CF_ACCEPTED) {
                                    CheckMetadataPublicationDTO json = new CheckMetadataPublicationDTO();
                                    json.setMessage(I18nUtil.getMessage("jsp.display-item.unclaim-publication", new String[]{I18nUtil.getMessage("metadata." + standardizeField)}, context.getCurrentLocale(), false));
                                    json.setMetadata(standardizeField);
                                    json.setAction("unclaim");
                                    jsonresp.add(json);
                                    found = true;
                                    break internal;
                                }
                                
                            }
                        }
                    }
                    if(!found) {
                        internal : for(IMetadataValue mm : item.getMetadataValueInDCFormat("local.message.claim")) {
                           if(mm.getValue().contains(underscoredField)) {
                               CheckMetadataPublicationDTO json = new CheckMetadataPublicationDTO();
                               json.setMessage(I18nUtil.getMessage("jsp.display-item.pending-publication", new String[]{I18nUtil.getMessage("metadata." + standardizeField)}, context.getCurrentLocale(), false));
                               json.setMetadata(standardizeField);
                               json.setAction("claim");
                               jsonresp.add(json);
                               found = true;
                               break internal;
                           }
                        }
                        if(!found) {
                            CheckMetadataPublicationDTO json = new CheckMetadataPublicationDTO();
                            json.setMessage(I18nUtil.getMessage("jsp.display-item.claim-publication", new String[]{I18nUtil.getMessage("metadata." + standardizeField)}, context.getCurrentLocale(), false));
                            json.setMetadata(standardizeField);
                            json.setAction("claim");
                            jsonresp.add(json);
                        }
                    }
                }
            }
            catch (SQLException e)
            {
                throw new IOException(e);
            }
        }
        
        JSONSerializer serializer = new JSONSerializer();
        serializer.deepSerialize(jsonresp, resp.getWriter());
    }
    
}

class CheckMetadataPublicationDTO
{
    String message;
    String metadata;
    String action;

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public String getMetadata()
    {
        return metadata;
    }

    public void setMetadata(String metadata)
    {
        this.metadata = metadata;
    }

    public String getAction()
    {
        return action;
    }

    public void setAction(String action)
    {
        this.action = action;
    }

    
}
