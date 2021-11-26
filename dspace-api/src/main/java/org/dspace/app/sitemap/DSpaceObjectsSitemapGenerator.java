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
import java.util.Iterator;
import java.util.List;

import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.Item;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;

public class DSpaceObjectsSitemapGenerator implements ISitemapGeneratorPlugin {

	@Override
	public String addUrls(Context c, boolean makeHTMLMap, boolean makeSitemapOrg, List<String> includes,
			AbstractGenerator html, AbstractGenerator sitemapsOrg) throws SQLException, IOException {
		String handleURLStem = ConfigurationManager.getProperty("dspace.url") + "/handle/";
		StringBuffer objectDetails = new StringBuffer();
		if (includes.contains("community")) {

			List<Community> communities = ContentServiceFactory.getInstance().getCommunityService().findAll(c);

			for (Community com : communities) {
				String url = handleURLStem + com.getHandle();

				if (makeHTMLMap) {
					html.addURL(url, null);
				}
				if (makeSitemapOrg) {
					sitemapsOrg.addURL(url, null);
				}
			}

			objectDetails.append(",communities=").append(communities.size());
		}

		if (includes.contains("collection")) {
			List<Collection> collections = ContentServiceFactory.getInstance().getCollectionService().findAll(c);
			for (Collection collection : collections) {
				String url = handleURLStem + collection.getHandle();

				if (makeHTMLMap) {
					html.addURL(url, null);
				}
				if (makeSitemapOrg) {
					sitemapsOrg.addURL(url, null);
				}
			}
			objectDetails.append(",collections=").append(collections.size());

		}

		if (includes.contains("item")) {
			int itemCount = 0;
			Iterator<Item> items = ContentServiceFactory.getInstance().getItemService().findAll(c);

			while (items.hasNext()) {
				Item i = items.next();
				String url = handleURLStem + i.getHandle();
				Date lastMod = i.getLastModified();

				if (makeHTMLMap) {
					html.addURL(url, lastMod);
				}
				if (makeSitemapOrg) {
					sitemapsOrg.addURL(url, lastMod);
				}

				itemCount++;
			}
			objectDetails.append(",items=").append(itemCount);

		}
		return objectDetails.toString();
	}

	@Override
	public List<String> getListTypes() {
		return Arrays.asList("item", "community", "collection");
	}

}
