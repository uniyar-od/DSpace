/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.cris.submit.step;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Paths;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpException;
import org.dspace.app.cris.unpaywall.UnpaywallRecord;
import org.dspace.app.cris.unpaywall.UnpaywallService;
import org.dspace.app.cris.unpaywall.UnpaywallUtils;
import org.dspace.app.cris.unpaywall.model.Unpaywall;
import org.dspace.app.util.SubmissionInfo;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Bitstream;
import org.dspace.content.BitstreamFormat;
import org.dspace.content.Bundle;
import org.dspace.content.FormatIdentifier;
import org.dspace.content.Item;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.submit.AbstractProcessingStep;
import org.dspace.utils.DSpace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnpaywallStep extends AbstractProcessingStep
{

    private static Logger log = LoggerFactory.getLogger(UnpaywallStep.class);

    /***************************************************************************
     * STATUS / ERROR FLAGS (returned by doProcessing() if an error occurs or
     * additional user interaction may be required)
     * 
     * (Do NOT use status of 0, since it corresponds to STATUS_COMPLETE flag
     * defined in the JSPStepManager class)
     **************************************************************************/
    /**
     * @param context
     *            current DSpace context
     * @param request
     *            current servlet request object
     * @param response
     *            current servlet response object
     * @param subInfo
     *            submission info object
     * @return Status or error flag which will be processed by
     *         doPostProcessing() below! (if STATUS_COMPLETE or 0 is returned,
     *         no errors occurred!)
     */
    public int doProcessing(Context context, HttpServletRequest request,
            HttpServletResponse response, SubmissionInfo subInfo)
            throws ServletException, IOException, SQLException,
            AuthorizeException
    {
        if (subInfo != null)
        {
            UnpaywallService unpaywallService = new DSpace().getServiceManager().getServiceByName(
                    "unpaywallService", UnpaywallService.class);
            String metadataDOI = ConfigurationManager.getProperty("unpaywall",
                    "metadata.doi");
            Item item = subInfo.getSubmissionItem().getItem();

            try
            {
                if (StringUtils.isNotBlank(item.getMetadata(metadataDOI)))
                {
                    Bundle[] bundles = item.getBundles(Constants.DEFAULT_BUNDLE_NAME);
                    if (bundles == null || bundles.length == 0) {
                        Unpaywall unpaywall = unpaywallService.searchByDOI(item.getMetadata(metadataDOI), item.getID());
                        if (unpaywall != null) {
                            Bundle bundle = item.createBundle("ORIGINAL");

                            UnpaywallRecord record = UnpaywallUtils.convertStringToUnpaywallRecord(unpaywall.getUnpaywallJsonString());
                            InputStream is = new URL(record.getUnpaywallBestOA().getUrl_for_pdf()).openStream();

                            Bitstream bs = bundle.createBitstream(is);
                            bs.setName(URLDecoder.decode(
                                Paths.get(record.getUnpaywallBestOA().getUrl_for_pdf()).getFileName().toString(),
                                "UTF-8"));
                            BitstreamFormat bf = FormatIdentifier.guessFormat(context, bs);
                            bs.setFormat(bf);

                            item.addBundle(bundle);
                            item.update();
                        }
                    }
                }
            }
            catch (HttpException e)
            {
                log.error(e.getMessage(), e);
            }
        }
        return 0;
    }

    @Override
    public void doClear(SubmissionInfo subInfo) throws ServletException,
            IOException, SQLException, AuthorizeException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public int getNumberOfPages(HttpServletRequest request,
            SubmissionInfo subInfo) throws ServletException
    {
        // TODO Auto-generated method stub
        return 0;
    }
}
