package org.dspace.app.cris.util;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.dspace.app.cris.model.ACrisObject;
import org.dspace.app.cris.model.jdyna.VisibilityTabConstant;
import org.dspace.app.cris.service.ApplicationService;
import org.dspace.authorize.factory.AuthorizeServiceFactory;
import org.dspace.content.RootObject;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;
import org.dspace.eperson.factory.EPersonServiceFactory;
import org.dspace.services.ConfigurationService;
import org.dspace.utils.DSpace;

import it.cilea.osd.jdyna.model.AuthorizationContext;
import it.cilea.osd.jdyna.model.PropertiesDefinition;
import it.cilea.osd.jdyna.web.ITabService;
import it.cilea.osd.jdyna.web.AbstractEditTab;

public class CrisAuthorizeManager
{
	static ConfigurationService confService = new DSpace().getConfigurationService();

    public static <A extends AuthorizationContext, T extends ACrisObject, PD extends PropertiesDefinition> boolean authorize(
            Context context, ITabService applicationService, Class<T> clazz, Class<PD> classPD,
            Integer id, A authorizedObject) throws SQLException
    {
        Integer visibility = authorizedObject.getVisibility();
        if (!(authorizedObject instanceof AbstractEditTab) 
        		&& VisibilityTabConstant.HIGH.equals(visibility))
        {
            return true;
        }

        boolean result = false;
        T object = null;
        try
        {
            object = ((ApplicationService) applicationService).get(clazz, id);
        }
        catch (NumberFormatException e)
        {
            throw new RuntimeException(e);
        }
        
        // check admin authorization
        if (isAdmin(context, object) && !VisibilityTabConstant.LOW.equals(visibility))
        {
        	// admin can see everything except what is reserved to the object owner (LOW)
        	return true;
        }
        
        EPerson currUser = context.getCurrentUser();

        boolean isOwner = false;
        
        if (currUser != null)
        {
        	isOwner = object.isOwner(currUser);
        }
        
        if (visibility == VisibilityTabConstant.LOW || visibility == VisibilityTabConstant.STANDARD
        		|| visibility == VisibilityTabConstant.HIGH) {
        	// if visibility is standard the admin case has been already checked on line 49
        	// visibility == HIGH here only for edit tab, it is assumed to be "standard" as nobody want public edit
        	return isOwner;
        }

        // last case... policy
        if (currUser != null)
        {
            List<PD> listPolicySingle = authorizedObject
                    .getAuthorizedSingle();

            if (listPolicySingle != null && !listPolicySingle.isEmpty())
            {
                for (PD policy : listPolicySingle)
                {
                    String data = object.getMetadata(policy.getShortName());
                    if (StringUtils.isNotBlank(data))
                    {
                        if (currUser.getID() == UUID.fromString(data))
                        {
                            return true;
                        }
                    }
                }
            }
        }

        List<PD> listPolicyGroup = authorizedObject.getAuthorizedGroup();

        if (listPolicyGroup != null && !listPolicyGroup.isEmpty())
        {
            for (PD policy : listPolicyGroup)
            {
                List<String> policies = object.getMetadataValue(policy.getShortName());
                for (String data : policies)
                {
                    if (StringUtils.isNotBlank(data))
                    {
                        Group group = EPersonServiceFactory.getInstance().getGroupService().find(context,
                                UUID.fromString(data));
                        if (group != null)
                        {
                            if (currUser == null)
                            {
                                return false;
                            }
                            else if (EPersonServiceFactory.getInstance().getGroupService().isMember(context, group))
                            {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    public static <T extends RootObject> boolean isAdmin(
            Context context, T crisObject) throws SQLException 
    {
        // check admin authorization
        if (AuthorizeServiceFactory.getInstance().getAuthorizeService().isAdmin(context))
        {
            return true;
        }

        String crisObjectTypeText = crisObject.getTypeText();
        EPerson currUser = context.getCurrentUser();        
        String groupName = confService.getProperty("cris." + crisObjectTypeText + ".admin");
        if(StringUtils.isBlank(groupName)) {
            groupName = "Administrator "+crisObjectTypeText;
        }
        Group group = EPersonServiceFactory.getInstance().getGroupService().findByName(context, groupName);
        if (group != null)
        {
            if (currUser == null)
            {
                boolean isMember = EPersonServiceFactory.getInstance().getGroupService().isMember(context, Group.ADMIN);
                if (isMember)
                {
                    return true;
                }
            }
            if (EPersonServiceFactory.getInstance().getGroupService().isMember(context, group))
            {
                return true;
            }
        }
        return false;
    }
    
    public static <T extends ACrisObject> boolean canEdit(
            Context context, ITabService as, Class<? extends AbstractEditTab> classT, T crisObject) throws SQLException 
    {
        EPerson currUser = context.getCurrentUser();
        if(currUser==null) 
        {
            return false;
        }

        // check admin authorization
        if (isAdmin(context, crisObject))
        {
            return true;
        }

        List<? extends AbstractEditTab> list = as.getList(classT);
		for (AbstractEditTab t : list) {
        	if(CrisAuthorizeManager.authorize(context, as, crisObject.getCRISTargetClass(), crisObject.getClassPropertiesDefinition(), crisObject.getId(), t)) {
                return true;
            }
        }
        return false;
    }
    
}
