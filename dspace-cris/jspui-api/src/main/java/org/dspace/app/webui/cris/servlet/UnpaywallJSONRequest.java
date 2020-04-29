/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.webui.cris.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dspace.app.cris.service.ApplicationService;
import org.dspace.app.cris.unpaywall.UnpaywallBestOA;
import org.dspace.app.cris.unpaywall.UnpaywallRecord;
import org.dspace.app.cris.unpaywall.UnpaywallUtils;
import org.dspace.app.cris.unpaywall.model.Unpaywall;
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
		ApplicationService applicationService = new DSpace().getServiceManager().getServiceByName(
				"applicationService", ApplicationService.class);
		
		UnpaywallItemInfoJSONResponse jsonresp = new UnpaywallItemInfoJSONResponse();
		boolean isFounded = false;

		int itemID = UIUtil.getIntParameter(req, "itemid");
		String doi = UnpaywallUtils.resolveDoi(req.getParameter("doi"));
		Unpaywall unpaywall = applicationService.uniqueByDOIAndItemID(doi, itemID);
		if (unpaywall != null && unpaywall.getUnpaywallJsonString() != null) {
			UnpaywallRecord rec = UnpaywallUtils.convertStringToUnpaywallRecord(unpaywall.getUnpaywallJsonString());
			if (rec != null) {
				UnpaywallBestOA unpaywallBestOA = rec.getUnpaywallBestOA();
				if (unpaywallBestOA != null) {
					jsonresp.setUrlJSON(unpaywallBestOA.getUrl_for_pdf());
					jsonresp.setFullJSON(unpaywall.getUnpaywallJsonString());
					isFounded = true;
				}
			}
		}

		jsonresp.setIsFounded(isFounded);

		JSONSerializer serializer = new JSONSerializer();
		serializer.deepSerialize(jsonresp, resp.getWriter());
	}
}

class UnpaywallItemInfoJSONResponse {

	String fullJSON;
	String urlJSON;
	boolean isFounded;
	
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
	public boolean getIsFounded() {
		return isFounded;
	}
	public void setIsFounded(boolean isFounded) {
		this.isFounded = isFounded;
	}

}
