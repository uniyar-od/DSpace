/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.content.license.FormattableArgument;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

/**
 * Utility class to manage generation and storing of the license text that the
 * submitter has to grant/granted for archiving the item
 * 
 * @author bollini
 * 
 */
public class LicenseUtils
{
    private static Logger log = Logger.getLogger(LicenseUtils.class);

    /**
     * Return the text of the license that the user has granted/must grant
     * before for submit the item. The license text is build using the template
     * defined for the collection if any or the wide site configuration. In the
     * license text the following substitutions can be used.<br>
     * {0} the eperson firstname<br>
     * {1} the eperson lastname<br>
     * {2} the eperson email<br>
     * {3} the current date<br>
     * {4} the collection object that will be formatted using the appropriate
     * LicenseArgumentFormatter plugin (if defined)<br>
     * {5} the item object that will be formatted using the appropriate
     * LicenseArgumentFormatter plugin (if defined)<br>
     * {6} the eperson object that will be formatted using the appropriate
     * LicenseArgumentFormatter plugin (if defined)<br>
     * {x} any addition argument supplied wrapped in the
     * LicenseArgumentFormatter based on his type (map key)
     * 
     * @see license.LicenseArgumentFormatter
     * @param locale
     * @param collection
     * @param item
     * @param eperson
     * @param additionalInfo
     * @return the license text obtained substituting the provided argument in
     *         the license template
     */
    public static String getLicenseText(Locale locale, Collection collection,
            Item item, EPerson eperson, Map<String, Object> additionalInfo)
    {
        Formatter formatter = new Formatter(locale);

        // EPerson firstname, lastname, email and the current date
        // will be available as separate arguments to make more simple produce
        // "tradition" text license
        // collection, item and eperson object will be also available
        int numArgs = 7 + (additionalInfo != null ? additionalInfo.size() : 0);
        Object[] args = new Object[numArgs];
        args[0] = eperson.getFirstName();
        args[1] = eperson.getLastName();
        args[2] = eperson.getEmail();
        args[3] = new java.util.Date();
        args[4] = new FormattableArgument("collection", collection);
        args[5] = new FormattableArgument("item", item);
        args[6] = new FormattableArgument("eperson", eperson);

        if (additionalInfo != null)
        {
            int i = 7; // Start is next index after previous args
            for (Map.Entry<String, Object> info : additionalInfo.entrySet())
            {
                args[i] = new FormattableArgument(info.getKey(), info.getValue());
                i++;
            }
        }

        String licenseTemplate = collection.getLicense();

        return formatter.format(licenseTemplate, args).toString();
    }

    /**
     * Utility method if no additional arguments are to be supplied to the
     * license template. (equivalent to calling the full getLicenseText
     * supplying {@code null} for the additionalInfo argument)
     *
     * @param locale
     * @param collection
     * @param item
     * @param eperson
     * @return the license text, with no custom substitutions.
     */
    public static String getLicenseText(Locale locale, Collection collection,
            Item item, EPerson eperson)
    {
        return getLicenseText(locale, collection, item, eperson, null);
    }

    /**
     * Store a copy of the license a user granted in the item.
     * 
     * @param context
     *            the dspace context
     * @param item
     *            the item object of the license
     * @param licenseText
     *            the license the user granted
     * @throws SQLException
     * @throws IOException
     * @throws AuthorizeException
     */
    public static void grantLicense(Context context, Item item,
            String licenseText) throws SQLException, IOException,
            AuthorizeException
    {
        // Put together text to store
        // String licenseText = "License granted by " + eperson.getFullName()
        // + " (" + eperson.getEmail() + ") on "
        // + DCDate.getCurrent().toString() + " (GMT):\n\n" + license;

        // Store text as a bitstream
        byte[] licenseBytes = licenseText.getBytes("UTF-8");
        ByteArrayInputStream bais = new ByteArrayInputStream(licenseBytes);
        Bitstream b = item.createSingleBitstream(bais, "LICENSE");

        // Now set the format and name of the bitstream
        b.setName("license.txt");
        b.setSource("Written by org.dspace.content.LicenseUtils");

        // Find the License format
        BitstreamFormat bf = BitstreamFormat.findByShortDescription(context,
                "License");
        b.setFormat(bf);

        b.update();
    }
	public static void getLicensePDF(Context context, Item item, String choiceLicense) {
		String reportName = ConfigurationManager.getProperty("jasper", choiceLicense);
        String reportFolderPath = ConfigurationManager.getProperty("jasper","report.folder");
        String reportExtension = ConfigurationManager.getProperty("jasper","report.extension");
		String reportPath = reportFolderPath+reportName+reportExtension;
		try {
			fill(context, item, reportPath,reportName);
		} catch (SQLException e) {
			log.error(e.getMessage(),e);
		}

		
	}

	private static void fill(Context context, Item item, String reportPath, String reportName) throws SQLException {
        HashMap<String, Object> reportMap = new HashMap<String, Object>();
        String key = "";
        String value = "";
        EPerson ep = item.getSubmitter();
        List<Metadatum> listEPMetadata = ep.getMetadata();
        Metadatum[] listItemMetadata = item.getMetadataWithoutPlaceholder(Item.ANY, Item.ANY, Item.ANY, Item.ANY);
        for (Metadatum m: listItemMetadata) {
        	key = m.getField().replace('.', '_');
        	String oldValue = (String) reportMap.get(key);
        	value = m.value;
        	if (oldValue != null) {
        		value = oldValue+", "+value;
        	}
    		reportMap.put(key, value);
        }
        for (Metadatum m: listEPMetadata) {
        	key = m.getField().replace('.', '_');
        	String oldValue = (String) reportMap.get(key);
        	value = m.value;
        	if (oldValue != null) {
        		value = oldValue+", "+value;
        	}
    		reportMap.put(key, value);
        }
        String logo = ConfigurationManager.getProperty("jasper","parameter.logo");
        String pathLogoImage = ConfigurationManager.getProperty("jasper","parameter.logo.path");
        String submitterEmail = ConfigurationManager.getProperty("jasper","parameter.submitter.email");
        String submitterFullName = ConfigurationManager.getProperty("jasper","parameter.submitter.fullname");
        String currentDateLabel = ConfigurationManager.getProperty("jasper","parameter.current.date");
        
		reportMap.put(logo, pathLogoImage);
		String email = item.getSubmitter().getEmail();
		String fullname = item.getSubmitter().getFullName();
		Date date = new Date();
		String currentDate = date.toString();
		reportMap.put(submitterEmail, email);
		reportMap.put(submitterFullName, fullname);
		reportMap.put(currentDateLabel, currentDate);
        try {
        	// Give the path of report jrxml file path for complie.
        	JasperReport jasperReport = JasperCompileManager.compileReport(reportPath);
        	//pass data HashMap with compiled jrxml file and a empty data source .
        	JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, reportMap,new JREmptyDataSource() );

        	ByteArrayOutputStream baos = new ByteArrayOutputStream();
        	JasperExportManager.exportReportToPdfStream(jasperPrint, baos);
        	ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            Bitstream b = item.createSingleBitstream(bais, "LICENSE");
            // Now set the format and name of the bitstream
            b.setName("license.pdf");
            b.setSource("Written by org.dspace.content.LicenseUtils");

            // Find the License format
            BitstreamFormat bf = BitstreamFormat.findByShortDescription(context,
                    "License PDF");
            b.setFormat(bf);
            b.update();
            context.turnOffAuthorisationSystem();
            // grant access to the license to the submitter
            AuthorizeManager.addPolicy(context, b, org.dspace.core.Constants.READ, context.getCurrentUser());
            context.restoreAuthSystemState();
        } catch (Exception e) {
        	log.error(e.getMessage(),e);
        	throw new RuntimeException(e.getMessage(), e);
        }
    }
}
