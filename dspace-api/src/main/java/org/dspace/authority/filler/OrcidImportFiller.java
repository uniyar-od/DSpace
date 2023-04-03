/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.authority.filler;

import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.removeStart;
import static org.apache.commons.lang3.StringUtils.startsWith;
import static org.dspace.content.Item.ANY;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dspace.authority.CrisConsumer;
import org.dspace.authority.service.AuthorityValueService;
import org.dspace.content.Item;
import org.dspace.content.MetadataValue;
import org.dspace.content.dto.MetadataValueDTO;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.dspace.core.exception.SQLRuntimeException;
import org.dspace.external.model.ExternalDataObject;
import org.dspace.external.provider.impl.OrcidV3AuthorDataProvider;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implementation of {@link AuthorityImportFiller} which enriches the item with
 * the data present on ORCID.
 *
 * @author Luca Giamminonni (luca.giamminonni at 4Science)
 *
 */
public class OrcidImportFiller implements AuthorityImportFiller {

    private static final Logger LOGGER = LogManager.getLogger(OrcidImportFiller.class);

    @Autowired
    private OrcidV3AuthorDataProvider orcidV3AuthorDataProvider;

    @Autowired
    private ItemService itemService;

    @Override
    public boolean allowsUpdate(Context context, MetadataValue sourceMetadata, Item itemToFill) {
        return false;
    }

    @Override
    public List<MetadataValueDTO> getMetadataListByRelatedItemAndMetadata(Context context, Item relatedItem,
        MetadataValue metadata) {
        return List.of();
    }

    @Override
    public void fillItem(Context context, MetadataValue sourceMetadata, Item item) throws SQLException {

        try {

            getOrcidFromMetadataValue(sourceMetadata)
                .flatMap(orcid -> orcidV3AuthorDataProvider.getExternalDataObject(orcid))
                .ifPresent(externalData -> enrichItemWithOrcidExternalData(context, item, externalData));

        } catch (Exception ex) {
            LOGGER.error("An error occurs trying to enrich item with ORCID data", ex);
        }

        if (isTitleNotSet(item)) {
            setTitle(context, item, sourceMetadata.getValue());
        }

    }

    private Optional<String> getOrcidFromMetadataValue(MetadataValue metadataValue) {
        return Optional.ofNullable(metadataValue.getAuthority())
            .filter(this::isOrcidWillBeGeneratedAuthority)
            .map(authority -> removeOrcidWillBeGeneratedPrefix(authority));
    }

    private boolean isOrcidWillBeGeneratedAuthority(String authority) {
        return startsWith(authority, getOrcidWillBeGeneratedAuthority());
    }

    private String removeOrcidWillBeGeneratedPrefix(String authority) {
        return removeStart(authority, getOrcidWillBeGeneratedAuthority());
    }

    private String getOrcidWillBeGeneratedAuthority() {
        return AuthorityValueService.GENERATE + "ORCID" + AuthorityValueService.SPLIT;
    }

    private void enrichItemWithOrcidExternalData(Context context, Item item, ExternalDataObject externalData) {

        externalData.getMetadata().stream()
            .filter(metadataValue -> notAlreadyPresent(item, metadataValue))
            .forEach(metadataValue -> addMetadata(context, item, metadataValue));

    }

    private boolean notAlreadyPresent(Item item, MetadataValueDTO value) {
        return isEmpty(itemService.getMetadata(item, value.getSchema(), value.getElement(), value.getQualifier(), ANY));
    }

    private boolean isTitleNotSet(Item item) {
        return isBlank(itemService.getMetadataFirstValue(item, "dc", "title", null, Item.ANY));
    }

    private void addMetadata(Context context, Item item, MetadataValueDTO value) {
        try {
            itemService.addMetadata(context, item, value.getSchema(), value.getElement(),
                value.getQualifier(), value.getLanguage(), value.getValue());
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    private void setTitle(Context context, Item item, String title) {
        try {
            itemService.setMetadataSingleValue(context, item, "dc", "title", null, null, title);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    public OrcidV3AuthorDataProvider getOrcidV3AuthorDataProvider() {
        return orcidV3AuthorDataProvider;
    }

    public void setOrcidV3AuthorDataProvider(OrcidV3AuthorDataProvider orcidV3AuthorDataProvider) {
        this.orcidV3AuthorDataProvider = orcidV3AuthorDataProvider;
    }

}
