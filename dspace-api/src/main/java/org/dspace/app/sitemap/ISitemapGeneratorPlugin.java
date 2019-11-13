/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.sitemap;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.dspace.core.Context;

public interface ISitemapGeneratorPlugin
{
    /**
     * The plugin must sent to the generators the URLs to include
     * 
     * @param c
     *            dspace context
     * @param makeHTMLMap
     *            true to generate the html sitemap
     * @param makeSitemapOrg
     *            true to generate the xml sitemapOrg
     * @param includes
     *            list of object types to includes in the sitemap (item,
     *            community, collection, etc.)
     * @param html
     *            generator of the html sitemap
     * @param sitemapsOrg
     *            generator of the xml sitemaporg
     * @return details about the objects sent to the generators
     * @throws SQLException
     * @throws IOException
     */
    String addUrls(Context c, boolean makeHTMLMap, boolean makeSitemapOrg,
            List<String> includes, AbstractGenerator html,
            AbstractGenerator sitemapsOrg) throws SQLException, IOException;

    /**
     * List of all object types supported by the plugin, such as item,
     * community, etc.
     * 
     * @return
     */
    List<String> getListTypes();

}
