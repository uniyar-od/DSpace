package org.dspace.app.sitemap;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.dspace.core.Context;

public interface ISitemapGeneratorPlugin
{

    String addUrls(Context c, boolean makeHTMLMap, boolean makeSitemapOrg,
            List<String> includes, AbstractGenerator html,
            AbstractGenerator sitemapsOrg) throws SQLException, IOException;

    List<String> getListTypes();

}