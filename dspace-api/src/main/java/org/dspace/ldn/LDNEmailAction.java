package org.dspace.ldn;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.dspace.content.DCDate;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.core.Email;
import org.dspace.core.I18nUtil;
import org.dspace.handle.HandleManager;

public class LDNEmailAction extends LDNAction {
	/*
	 * supported for actionSendFilter are:
	 *   - <single email>
	 *   - GROUP:<group_name>
	 *   - SUBMITTER
	 */
	private String actionSendFilter;
	//The file name for the requested email
	private String actionSendEmailTextFile;

	public String getActionSendFilter() {
		return actionSendFilter;
	}

	public void setActionSendFilter(String actionSendFilter) {
		this.actionSendFilter = actionSendFilter;
	}
	
	public String getActionSendEmailTextFile() {
		return actionSendEmailTextFile;
	}

	public void setActionSendEmailTextFile(String actionSendEmailTextFile) {
		this.actionSendEmailTextFile = actionSendEmailTextFile;
	}

	@Override
	public ActionStatus executeAction(Context context, NotifyLDNDTO ldnRequestDTO) {

        try
        {
    		String itemHandle = LDNUtils.getHandleFromURL(ldnRequestDTO.getContext().getId());
    		DSpaceObject dso = HandleManager.resolveToObject(context, itemHandle);
    		Item item = (Item) dso;
        	
            Locale supportedLocale = I18nUtil.getEPersonLocale(context.getCurrentUser());
            Email email = Email.getEmail(I18nUtil.getEmailFilename(supportedLocale, actionSendEmailTextFile));
            

            //Setting recipients email
            List<String> recipients=retrieveRecipientsEmail(item);
            for(String recipient:recipients) {
                email.addRecipient(recipient);
            }
            
//            # Parameters: {0} Service Name
//            #             {1} Item Name
//            #             {2} Service URL
//            #             {3} Item URL
//            #             {4} Submitter's Name
//            #			    {5} Date of the received LDN notification
            
            email.addArgument(ldnRequestDTO.getActor().getName());
            email.addArgument(item.getName());
            email.addArgument(ldnRequestDTO.getActor().getId());
            email.addArgument(ldnRequestDTO.getContext().getId());
            email.addArgument(item.getSubmitter().getFullName());
            email.addArgument(new DCDate(Calendar.getInstance().getTime()).toString());

            email.send();
        }
        catch (Exception e)
        {
            logger.error("An Error Occurred while sending a notification email", e);
        }

		return ActionStatus.CONTINUE;
	}
	
	public List<String> retrieveRecipientsEmail(Item item) throws SQLException{
		List<String> recipients=new LinkedList<String>();
		
		if(actionSendFilter.startsWith("SUBMITTER")) {
			recipients.add(item.getSubmitter().getEmail());
		}else if (actionSendFilter.startsWith("GROUP:")) {
			String groupName=actionSendFilter.replace("GROUP:", "");
			String[] groupEmails=ConfigurationManager.getProperty("ldn-coar-notify", "email." + groupName + ".list").split(",");
			recipients=Arrays.asList(groupEmails);
		}else {
			recipients.add(actionSendFilter);
		}
		
		return recipients;
	}

}
