package org.dspace.app.cris.unpaywall;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.http.HttpException;
import org.dspace.app.cris.unpaywall.model.Unpaywall;
import org.dspace.core.ConfigurationManager;

public class UnpaywallTest
{

    public static void main(String[] args) throws HttpException
    {
        UnpaywallService service = new UnpaywallService();

        Unpaywall unpaywall = service.unpaywallCall("10.1038/nature12373");
        UnpaywallRecord rec = unpaywall.getUnpaywallRecord();
        UnpaywallBestOA unpaywallBestOA = rec.getUnpaywallBestOA();
        UnpaywallOA[] unpOAA = rec.getUnpaywallOA();
        System.out.println(rec.getDoi());
        System.out.println(rec.isOa());
        System.out.println(rec.getPublished_date());
        System.out.println(rec.getOa_status());
        System.out.println(rec.getUpdated());
        System.out.println(unpaywallBestOA.getEvidence());
        System.out.println(unpaywallBestOA.getHost_type());
        System.out.println(unpaywallBestOA.getLicense());
        System.out.println(unpaywallBestOA.getUrl_for_pdf());
        System.out.println(unpaywallBestOA.getVersion());
        System.out.println(rec.isInDoaj());
//        
//        for(UnpaywallOA unpo : unpOAA) {
//                System.out.println(unpo.getEndpoint_id() +  " Endpoint");
//                System.out.println(unpo.getEvidence() + " Evidence");
//                System.out.println(unpo.getHost_type()+ " Host Type");
//                System.out.println(unpo.getIs_best() + " Is_best");
//                System.out.println(unpo.getLicense() + " License");
//                System.out.println(unpo.getPmh_id() + " PMH_ID");
//                System.out.println(unpo.getUpdated() + " Updated");
//                System.out.println(unpo.getUrl() + " Url");
//                System.out.println(unpo.getUrl_for_landing_page() + " Url landing");
//                System.out.println(unpo.getUrl_for_pdf() + " Url pdf");
//                System.out.println(unpo.getVersion() + " Version");
                System.out.println(ConfigurationManager.getProperty("unpaywall",
                    "metadata.doi"));
//        }
//        Long currentDate = System.currentTimeMillis();
//        String pubDate = rec.getUnpaywallOA().get(0).get;
//
//
//        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
//        Long milliseconds = null;
//        try {
//            Date d = f.parse(pubDate);
//            milliseconds = d.getTime();
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//        Long a = Math.subtractExact(currentDate, milliseconds);
//        System.out.println(currentDate);
//        System.out.println(milliseconds);
//        
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(new Date());
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
//        sdf.setTimeZone(TimeZone.getDefault());
//      System.out.println(sdf.format(calendar.getTime()));
//      Long millis = calendar.getTimeInMillis();
//      System.out.println(millis);
        }
}