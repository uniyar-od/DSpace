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
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.Item;
import org.dspace.content.ItemIterator;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;

public class DSpaceObjectsSitemapGenerator implements ISitemapGeneratorPlugin {

    @Override
    public String addUrls(Context c, boolean makeHTMLMap, boolean makeSitemapOrg, List<String> includes,
			AbstractGenerator html, AbstractGenerator sitemapsOrg) throws SQLException, IOException {
		String handleURLStem = ConfigurationManager.getProperty("dspace.url") + "/handle/";
		StringBuffer objectDetails = new StringBuffer();
		if (includes.contains("community")) {

			Community[] comms = Community.findAll(c);
			for (int i = 0; i < comms.length; i++) {
				String url = handleURLStem + comms[i].getHandle();

				if (makeHTMLMap) {
					html.addURL(url, null);
				}
				if (makeSitemapOrg) {
					sitemapsOrg.addURL(url, null);
				}
			}

			objectDetails.append(",communities=").append(comms.length);
		}

		if (includes.contains("collection")) {
			Collection[] colls = Collection.findAll(c);
			for (int i = 0; i < colls.length; i++) {
				String url = handleURLStem + colls[i].getHandle();

				if (makeHTMLMap) {
					html.addURL(url, null);
				}
				if (makeSitemapOrg) {
					sitemapsOrg.addURL(url, null);
				}
			}
			objectDetails.append(",collections=").append(colls.length);

		}

		ItemIterator allItems = null;
		try {

			int itemCount = 0;
			if (includes.contains("item")) {
				allItems = Item.findAll(c);

				while (allItems.hasNext()) {
					Item i = allItems.next();
					String url = handleURLStem + i.getHandle();
					Date lastMod = i.getLastModified();

					if (makeHTMLMap) {
						html.addURL(url, lastMod);
					}
					if (makeSitemapOrg) {
						sitemapsOrg.addURL(url, lastMod);
					}
					i.decache();

					itemCount++;
				}
				objectDetails.append(",items=").append(itemCount);

			}
		} finally {
			if (allItems != null) {
				allItems.close();
			}
		}
		return objectDetails.toString();
	}

    @Override
    public List<String> getListTypes()
    {
        return Arrays.asList("item", "community", "collection");
    }

}
