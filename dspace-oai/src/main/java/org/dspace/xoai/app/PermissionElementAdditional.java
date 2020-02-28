/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.xoai.app;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.authorize.ResourcePolicy;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.Item;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.xoai.util.ItemUtils;

import com.lyncode.xoai.dataprovider.xml.xoai.Element;
import com.lyncode.xoai.dataprovider.xml.xoai.Metadata;

public class PermissionElementAdditional implements XOAIItemCompilePlugin {

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	private static Logger log = LogManager.getLogger(PermissionElementAdditional.class);

	@Override
	public Metadata additionalMetadata(Context context, Metadata metadata, Item item) {
		Element other;
		if(ItemUtils.getElement(metadata.getElement(),"others") != null){
			other = ItemUtils.getElement(metadata.getElement(),"others");
		}else {
			other = ItemUtils.create("others");
		}

		String drm = null;

		try {
			drm = buildPermission(context, item);
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
		}

		other.getField().add(
				ItemUtils.createValue("drm", drm));
		metadata.getElement().add(other);

		return metadata;
	}

	private static String buildPermission(Context context, Item item) throws SQLException {

		String values = "metadata only access";
		Bundle[] bnds;
		try {
			bnds = item.getBundles(Constants.DEFAULT_BUNDLE_NAME);
		} catch (SQLException e) {
			throw new RuntimeException(e.getMessage(), e);
		}

		for (Bundle bnd : bnds) {

			Bitstream bitstream = Bitstream.find(context, bnd.getPrimaryBitstreamID());
			if (bitstream == null) {
				for (Bitstream b : bnd.getBitstreams()) {
					bitstream = b;
					break;
				}
			}

			if (bitstream == null) {
				return "metadata only access";
			}
			values = getDRM(AuthorizeManager.getPoliciesActionFilter(context, bitstream, Constants.READ));
		}
		return values;
	}

	public static String getDRM(List<ResourcePolicy> rps) {
		Date now = new Date();
		Date embargoEndDate = null;
		boolean openAccess = false;
		boolean groupRestricted = false;
		boolean withEmbargo = false;

		if (rps != null) {
			for (ResourcePolicy rp : rps) {
				if (rp.getGroupID() == 0) {
					if (rp.isDateValid()) {
						openAccess = true;
					} else if (rp.getStartDate() != null && rp.getStartDate().after(now)) {
						withEmbargo = true;
						embargoEndDate = rp.getStartDate();
					}
				} else if (rp.getGroupID() != 1) {
					if (rp.isDateValid()) {
						groupRestricted = true;
					} else if (rp.getStartDate() == null || rp.getStartDate().after(now)) {
						withEmbargo = true;
						embargoEndDate = rp.getStartDate();
					}
				}
			}
		}
		String values = "metadata only access";
		// if there are fulltext build the values
		if (openAccess) {
			// open access
			values = "open access";
		} else if (withEmbargo) {
			// all embargoed
			values = "embargoed access" + "|||" + sdf.format(embargoEndDate);
		} else if (groupRestricted) {
			// all restricted
			values = "restricted access";
		}
		return values;
	}

}