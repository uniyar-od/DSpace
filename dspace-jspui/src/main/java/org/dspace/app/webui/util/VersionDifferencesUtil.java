/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.webui.util;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.authorize.ResourcePolicy;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.MetadataField;
import org.dspace.content.MetadataSchema;
import org.dspace.content.Metadatum;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.core.Utils;
import org.dspace.util.VersionUtil;
import org.dspace.versioning.Version;
import org.dspace.versioning.VersionHistory;
import org.dspace.workflow.WorkflowItem;

public class VersionDifferencesUtil {
	public static void addVersioningInformation(Context context, HttpServletRequest request, WorkflowItem workflowItem)
			throws SQLException, AuthorizeException, IOException {
		Item item = workflowItem.getItem();
		VersionHistory history = VersionUtil.retrieveVersionHistory(context,
		        item);

		if (history != null) {
			Version currentVersion = history.getVersion(item);
	
			Version previousVersion = history.getPrevious(currentVersion);
	
			Item previousItem = previousVersion.getItem();
			request.setAttribute("previous.item", previousItem);
			if (previousItem != null) {
				List<MetadataField> modifiedMetadata = getModifiedMetadata(context, item, previousItem);
	
				request.setAttribute("version.message", currentVersion.getSummary());
				request.setAttribute("modifiedMetadata", modifiedMetadata);
				request.setAttribute("modifiedFiles", getModifiedFiles(context, item, previousItem));
			}
		}
	}

    private static Map<String, BitstreamDifferencesDTO> getModifiedFiles(Context context,
            Item item, Item previousItem)
            throws SQLException, AuthorizeException, IOException
    {
    	List<String> bitSequence = new ArrayList<String>();
    	Map<String, BitstreamDifferencesDTO> bitDiffs = new HashMap<String, BitstreamDifferencesDTO>();
    	List<Bitstream> currBitstreams = new ArrayList<Bitstream>();
    	for (Bundle bnd : item.getBundles(Constants.CONTENT_BUNDLE_NAME)) {
    		for (Bitstream b : bnd.getBitstreams()) {
    			currBitstreams.add(b);
    			bitSequence.add(b.getInternalBitId());
    			BitstreamDifferencesDTO diff = new BitstreamDifferencesDTO();
    			diff.setNewBitstream(b);
    			bitDiffs.put(b.getInternalBitId(), diff);
    		}
    	}
    	
    	List<Bitstream> prevBitstreams = new ArrayList<Bitstream>();
    	int pos = 0;
    	for (Bundle bnd : previousItem.getBundles(Constants.CONTENT_BUNDLE_NAME)) {
    		for (Bitstream b : bnd.getBitstreams()) {
    			prevBitstreams.add(b);
    			if (bitSequence.contains(b.getInternalBitId())) {
    				BitstreamDifferencesDTO bitDiff = bitDiffs.get(b.getInternalBitId());
					bitDiff.setPrevious(b);
					bitDiff.setModifiedMetadata(getModifiedMetadata(context, bitDiff.getNewBitstream(), b));
					bitDiff.setPoliciesModified(areCustomPoliciesDifferent(context, bitDiff.getNewBitstream(), b));
    			}
    			else {
    				if (!(pos < bitSequence.size())) {
    					pos = bitSequence.size() - 1;
    				}
    				bitSequence.add(pos, b.getInternalBitId());
        			BitstreamDifferencesDTO diff = new BitstreamDifferencesDTO();
        			diff.setPrevious(b);
        			bitDiffs.put(b.getInternalBitId(), diff);
    			}
    			pos++;
    		}
    	}
    	
    	Map<String, BitstreamDifferencesDTO> orderedMap = new LinkedHashMap<String, BitstreamDifferencesDTO>();
    	for (String i : bitSequence) {
    		BitstreamDifferencesDTO bitDiff = bitDiffs.get(i);
    		if (bitDiff.getPrevious() == null || bitDiff.getNewBitstream() == null ||
    				bitDiff.getModifiedMetadata().size() > 0 || bitDiff.isPoliciesModified()) {
    			orderedMap.put(i, bitDiff);
    		}
    	}
    	return orderedMap;
    }
	
    private static boolean areCustomPoliciesDifferent(Context context, Bitstream newBitstream, Bitstream b) throws SQLException {
		List<ResourcePolicy> rpolicies = AuthorizeManager.findPoliciesByDSOAndType(context, newBitstream, ResourcePolicy.TYPE_CUSTOM);
		List<ResourcePolicy> rpoliciesOld = AuthorizeManager.findPoliciesByDSOAndType(context, b, ResourcePolicy.TYPE_CUSTOM);
		if (rpolicies.size() != rpoliciesOld.size()) {
			return true;
		}
		for (ResourcePolicy rp : rpoliciesOld) {
			int idx = 0;
			for (ResourcePolicy rpToCompare : rpolicies) {
				Date rpToCompStartDate = rpToCompare.getStartDate();
				Date rpStartDate = rp.getStartDate();
				if (rpToCompare.getGroupID() == rp.getGroupID() && (
						(rpToCompStartDate == null && rpStartDate == null) ||
						(rpToCompStartDate != null && rpStartDate != null && DateUtils.isSameDay(rpToCompStartDate, rpStartDate))
				)){
					break;
				}
				else {
					idx++;
				}
			}
			if (idx < rpolicies.size()) {
				rpolicies.remove(idx);
			}
			else {
				return true;
			}
		}
		return rpolicies.size() != 0;
	}

	private static List<MetadataField> getModifiedMetadata(Context context,
            DSpaceObject item, DSpaceObject previousItem)
            throws SQLException, AuthorizeException, IOException
    {
        String[] mdToIgnore = ConfigurationManager
                .getProperty("versioning", "differences.ignore-metadata")
                .split("\\s*,\\s*");


        List<MetadataField> modifiedMetadata = new ArrayList<MetadataField>();
        for (MetadataField md : MetadataField.findAll(context))
        {
            String schemaName = MetadataSchema.find(context, md.getSchemaID())
                    .getName();

            String metadata = Utils.standardize(schemaName, md.getElement(),
                    md.getQualifier(), ".");

            if (ArrayUtils.contains(mdToIgnore, metadata))
            {
                continue;
            }
            else
            {
                Metadatum[] mdPrevItem = previousItem
                        .getMetadataByMetadataString(metadata);

                Metadatum[] mdItem = item
                        .getMetadataByMetadataString(metadata);

                if (mdPrevItem.length != mdItem.length)
                {
                    modifiedMetadata.add(md);
                }
                else
                {
                    int length = mdItem.length > mdPrevItem.length
                            ? mdItem.length
                            : mdPrevItem.length;
                    for (int i = 0; i < length; i++)
                    {
                        if (!StringUtils.equalsIgnoreCase(mdItem[i].value,
                                mdPrevItem[i].value))
                        {
                            modifiedMetadata.add(md);
                        }
                    }
                }
            }
        }
        return modifiedMetadata;
    }

}
