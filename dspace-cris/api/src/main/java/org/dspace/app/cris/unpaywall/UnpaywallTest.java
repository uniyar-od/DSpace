/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.cris.unpaywall;

import org.apache.http.HttpException;
import org.dspace.app.cris.unpaywall.model.Unpaywall;
import org.dspace.core.ConfigurationManager;

public class UnpaywallTest
{

    public static void main(String[] args) throws HttpException
    {
        UnpaywallService service = new UnpaywallService();

//        Unpaywall unpaywall = service.searchByDOI("10.1038/nature12373", 3, false);
        Unpaywall unpaywall = service.searchByDOI("10.1038/nature12373", 3);
        UnpaywallRecord rec = UnpaywallUtils.convertStringToUnpaywallRecord(unpaywall.getJsonRecord());
        UnpaywallBestOA unpaywallBestOA = rec.getUnpaywallBestOA();
        UnpaywallOA[] unpOAA = rec.getUnpaywallOA();
        System.out.println("------------------------------------------------------------");
        System.out.println("Record");
        System.out.println("------------------------------------------------------------");
        System.out.println(rec.getDoi() + " Doi");
        System.out.println(rec.isOa() + " isOa");
        System.out.println(rec.getPublished_date() + " Published Date");
        System.out.println(rec.getOa_status() + " Status");
        System.out.println(rec.getUpdated() + " Updated");
        System.out.println("------------------------------------------------------------");
        System.out.println("Best Oa");
        System.out.println("------------------------------------------------------------");
        System.out.println(unpaywallBestOA.getEvidence() + " Evidence");
        System.out.println(unpaywallBestOA.getHost_type() + " Host type");
        System.out.println(unpaywallBestOA.getLicense() + " License");
        System.out.println(unpaywallBestOA.getUrl_for_pdf() + " pdf url");
        System.out.println(unpaywallBestOA.getVersion() + " Version");
        System.out.println(rec.isInDoaj() + " isInDoaj");
        
        for(UnpaywallOA unpo : unpOAA) {
        		System.out.println("------------------------------------------------------------");
        		System.out.println("Oa");
        		System.out.println("------------------------------------------------------------");
                System.out.println(unpo.getEndpoint_id() +  " Endpoint");
                System.out.println(unpo.getEvidence() + " Evidence");
                System.out.println(unpo.getHost_type()+ " Host Type");
                System.out.println(unpo.getIs_best() + " Is_best");
                System.out.println(unpo.getLicense() + " License");
                System.out.println(unpo.getPmh_id() + " PMH_ID");
                System.out.println(unpo.getUpdated() + " Updated");
                System.out.println(unpo.getUrl() + " Url");
                System.out.println(unpo.getUrl_for_landing_page() + " Url landing");
                System.out.println(unpo.getUrl_for_pdf() + " Url pdf");
                System.out.println(unpo.getVersion() + " Version");
                System.out.println(ConfigurationManager.getProperty("unpaywall",
                    "metadata.doi"));
        }
    }
}