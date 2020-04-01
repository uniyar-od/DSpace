/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * https://github.com/CILEA/dspace-cris/wiki/License
 */
package org.dspace.app.webui.cris.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dspace.app.cris.unpaywall.UnpaywallBestOA;
import org.dspace.app.cris.unpaywall.UnpaywallRecord;
import org.dspace.app.cris.unpaywall.UnpaywallService;
import org.dspace.app.cris.unpaywall.UnpaywallUtils;
import org.dspace.app.cris.unpaywall.model.Unpaywall;
import org.dspace.app.cris.unpaywall.services.UnpaywallPersistenceService;
import org.dspace.app.webui.json.JSONRequest;
import org.dspace.app.webui.util.UIUtil;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;
import org.dspace.utils.DSpace;

import flexjson.JSONSerializer;

public class UnpaywallJSONRequest extends JSONRequest {

	@Override
	public void doJSONRequest(Context context, HttpServletRequest req, HttpServletResponse resp)
			throws IOException, AuthorizeException {
		UnpaywallService unpaywallService = new DSpace().getServiceManager().getServiceByName("unpaywallService",
				UnpaywallService.class);
		
		UnpaywallItemInfoJSONResponse jsonresp = new UnpaywallItemInfoJSONResponse();
		
		int itemID = UIUtil.getIntParameter(req, "itemid");
		Unpaywall unpaywall = unpaywallService.getUnpaywallPersistenceService().uniqueByDOIAndItemID("*", itemID);
		UnpaywallRecord rec = UnpaywallUtils.convertStringToUnpaywallRecord(unpaywall.getUnpaywallJsonString());
		UnpaywallBestOA unpaywallBestOA = rec.getUnpaywallBestOA();
		
		jsonresp.setUrlJSON(unpaywallBestOA.getUrl_for_pdf());
		jsonresp.setFullJSON(unpaywall.getUnpaywallJsonString());

		if (unpaywall != null) {
			JSONSerializer serializer = new JSONSerializer();
			serializer.deepSerialize(jsonresp, resp.getWriter());
		}
	}
}

class UnpaywallItemInfoJSONResponse {

	String fullJSON;
	String urlJSON;
	
	public String getFullJSON() {
		return fullJSON;
	}
	public void setFullJSON(String fullJSON) {
		this.fullJSON = fullJSON;
	}
	public String getUrlJSON() {
		return urlJSON;
	}
	public void setUrlJSON(String urlJSON) {
		this.urlJSON = urlJSON;
	}

}
