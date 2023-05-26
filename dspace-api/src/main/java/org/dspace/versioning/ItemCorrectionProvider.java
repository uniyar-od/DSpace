/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.versioning;


import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.ResourcePolicy;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.dspace.content.MetadataField;
import org.dspace.content.MetadataSchema;
import org.dspace.content.MetadataValue;
import org.dspace.content.RelationshipMetadataValue;
import org.dspace.content.WorkspaceItem;
import org.dspace.content.service.WorkspaceItemService;
import org.dspace.core.Context;
import org.dspace.xmlworkflow.storedcomponents.XmlWorkflowItem;
import org.dspace.xmlworkflow.storedcomponents.service.XmlWorkflowItemService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Giuseppe Digilio (giuseppe.digilio at 4science.it)
 */
//FIXME be sure AbstractVersionProvider is not implementing ItemVersionProvider
public class ItemCorrectionProvider extends AbstractVersionProvider {

    Logger log = org.apache.logging.log4j.LogManager.getLogger(ItemCorrectionProvider.class);

    private Set<String> ignoredMetadataFieldsOfCreation;

    private Set<String> ignoredMetadataFieldsOfMerging;

    private Set<String> bundles;

    @Autowired(required = true)
    protected WorkspaceItemService workspaceItemService;

    @Autowired(required = true)
    protected XmlWorkflowItemService workflowItemService;

    public WorkspaceItem createNewItemAndAddItInWorkspace(Context context, Collection collection, Item nativeItem)
            throws AuthorizeException, IOException, SQLException {

        Set<String> ignoredMetadataFieldsOfCreation =
            Sets.union(getIgnoredMetadataFields(), getIgnoredMetadataFieldsOfCreation());

        WorkspaceItem workspaceItem = workspaceItemService.create(context, collection, false);
        Item itemNew = workspaceItem.getItem();
        // copy metadata from native item to corrected item
        copyMetadata(context, itemNew, nativeItem, ignoredMetadataFieldsOfCreation);
        context.turnOffAuthorisationSystem();
        // copy bundles and bitstreams from native item
        createBundlesAndAddBitstreams(context, itemNew, nativeItem);
        context.restoreAuthSystemState();
        workspaceItem.setItem(itemNew);
        workspaceItemService.update(context, workspaceItem);

        log.info("Created new correction item " + workspaceItem.getItem().getID().toString()
                + "from item " + nativeItem.getID().toString());

        return workspaceItem;

    }

    public XmlWorkflowItem updateNativeItemWithCorrection(Context context, XmlWorkflowItem workflowItem,
            Item correctionItem, Item nativeItem) throws AuthorizeException, IOException, SQLException {

        Set<String> ignoredMetadataFieldsOfMerging =
            Sets.union(getIgnoredMetadataFields(), getIgnoredMetadataFieldsOfMerging());

        // save entity type
        MetadataValue entityType = itemService.getMetadata(nativeItem, "dspace", "entity", "type", Item.ANY).get(0);
        // clear all metadata entries from native item
        itemService.clearMetadata(context, nativeItem, Item.ANY, Item.ANY, Item.ANY, Item.ANY);
        // copy metadata from corrected item to native item
        copyMetadata(context, nativeItem, correctionItem, ignoredMetadataFieldsOfMerging);
        // restore entity type
        itemService.addMetadata(context, nativeItem, entityType.getMetadataField(), entityType.getLanguage(),
                entityType.getValue(), entityType.getAuthority(), entityType.getConfidence());

        context.turnOffAuthorisationSystem();
        // copy bundles and bitstreams of native item
        updateBundlesAndBitstreams(context, correctionItem, nativeItem);
        log.info("Updated new item " + nativeItem.getID().toString()
                + " with correction item " + correctionItem.getID().toString());

        workflowItem.setItem(nativeItem);
        workflowItemService.update(context, workflowItem);

        WorkspaceItem workspaceItem = workspaceItemService.findByItem(context, correctionItem);
        if (workspaceItem != null) {
            workspaceItemService.deleteWrapper(context, workspaceItem);
        }

        itemService.delete(context, correctionItem);
        log.info("Deleted correction item " + correctionItem.getID().toString());

        context.restoreAuthSystemState();




        return workflowItem;
    }

    private void copyMetadata(Context context, Item itemNew, Item nativeItem, Set<String> ignoredMetadataFields)
        throws SQLException {

        List<MetadataValue> md = itemService.getMetadata(nativeItem, Item.ANY, Item.ANY, Item.ANY, Item.ANY);
        for (MetadataValue aMd : md) {
            MetadataField metadataField = aMd.getMetadataField();
            MetadataSchema metadataSchema = metadataField.getMetadataSchema();
            String unqualifiedMetadataField = metadataSchema.getName() + "." + metadataField.getElement();
            if (ignoredMetadataFields.contains(metadataField.toString('.')) ||
                ignoredMetadataFields.contains(unqualifiedMetadataField + "." + Item.ANY) ||
                aMd instanceof RelationshipMetadataValue) {
                //Skip this metadata field (ignored and/or virtual)
                continue;
            }

            itemService.addMetadata(
                context,
                itemNew,
                metadataField.getMetadataSchema().getName(),
                metadataField.getElement(),
                metadataField.getQualifier(),
                aMd.getLanguage(),
                aMd.getValue(),
                aMd.getAuthority(),
                aMd.getConfidence(),
                aMd.getPlace()
            );
        }
    }

    protected void updateBundlesAndBitstreams(Context c, Item itemNew, Item nativeItem)
            throws SQLException, AuthorizeException, IOException {

        for (String bundleName : bundles) {
            List<Bundle> nativeBundles = nativeItem.getBundles(bundleName);
            List<Bundle> correctedBundles = itemNew.getBundles(bundleName);

            if (CollectionUtils.isEmpty(nativeBundles) || CollectionUtils.isEmpty(correctedBundles)) {
                continue;
            }
            updateBundleAndBitstreams(c, nativeBundles.get(0), correctedBundles.get(0));
        }
    }

    protected void updateBundleAndBitstreams(Context c, Bundle nativeBundle, Bundle correctedBundle)
            throws SQLException, AuthorizeException, IOException {

        for (Bitstream bitstreamCorrected : correctedBundle.getBitstreams()) {
            // check if new bitstream exists in native bundle
            Bitstream nativeBitstream = findBitstreamByChecksum(nativeBundle, bitstreamCorrected.getChecksum());
            if (nativeBitstream != null) {
                // update native bitstream metadata
                bitstreamService.clearMetadata(c, nativeBitstream, Item.ANY, Item.ANY, Item.ANY, Item.ANY);
                List<MetadataValue> metadataValues = bitstreamService
                        .getMetadata(bitstreamCorrected, Item.ANY, Item.ANY, Item.ANY, Item.ANY);

                for (MetadataValue metadataValue : metadataValues) {
                    bitstreamService.addMetadata(c, nativeBitstream, metadataValue.getMetadataField(),
                        metadataValue.getLanguage(), metadataValue.getValue(), metadataValue.getAuthority(),
                        metadataValue.getConfidence());
                }
                bitstreamService.update(c, nativeBitstream);
            } else {
                // Add new bitstram to native bundle
                // Metadata and additional information like internal identifier,
                // file size, checksum, and checksum algorithm are set by the bitstreamStorageService.clone(...)
                // and respectively bitstreamService.clone(...) method.
                Bitstream bitstreamNew =  bitstreamStorageService.clone(c, bitstreamCorrected);

                bundleService.addBitstream(c, nativeBundle, bitstreamNew);

                // NOTE: bundle.addBitstream() causes Bundle policies to be inherited by default.
                // So, we need to REMOVE any inherited TYPE_CUSTOM policies before copying over the correct ones.
                authorizeService.removeAllPoliciesByDSOAndType(c, bitstreamNew, ResourcePolicy.TYPE_CUSTOM);

                // Now, we need to copy the TYPE_CUSTOM resource policies from old bitstream
                // to the new bitstream, like we did above for bundles
                List<ResourcePolicy> bitstreamPolicies =
                    authorizeService.findPoliciesByDSOAndType(c, nativeBitstream, ResourcePolicy.TYPE_CUSTOM);
                authorizeService.addPolicies(c, bitstreamPolicies, bitstreamNew);

                if (correctedBundle.getPrimaryBitstream() != null && correctedBundle.getPrimaryBitstream()
                                                                              .equals(nativeBitstream)) {
                    nativeBundle.setPrimaryBitstreamID(bitstreamNew);
                }

                bitstreamService.update(c, bitstreamNew);
            }

        }
        bundleService.update(c, nativeBundle);
    }

    protected Bitstream findBitstreamByChecksum(Bundle bundle, String bitstreamChecksum) {
        List<Bitstream> bitstreams = bundle.getBitstreams();
        for (Bitstream bitstream : bitstreams) {
            if (bitstream.getChecksum().equals(bitstreamChecksum)) {
                return bitstream;
            }
        }

        return null;
    }

    public Set<String> getIgnoredMetadataFieldsOfCreation() {
        if (ignoredMetadataFieldsOfCreation == null) {
            return new HashSet<>();
        }
        return ignoredMetadataFieldsOfCreation;
    }

    public void setIgnoredMetadataFieldsOfCreation(Set<String> ignoredMetadataFieldsOfCreation) {
        this.ignoredMetadataFieldsOfCreation = ignoredMetadataFieldsOfCreation;
    }

    public Set<String> getIgnoredMetadataFieldsOfMerging() {
        if (ignoredMetadataFieldsOfMerging == null) {
            return new HashSet<>();
        }
        return ignoredMetadataFieldsOfMerging;
    }

    public void setIgnoredMetadataFieldsOfMerging(Set<String> ignoredMetadataFieldsOfMerging) {
        this.ignoredMetadataFieldsOfMerging = ignoredMetadataFieldsOfMerging;
    }

    public Set<String> getBundles() {
        return bundles;
    }

    public void setBundles(Set<String> bundles) {
        this.bundles = bundles;
    }
}
