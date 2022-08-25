/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.repository;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.dspace.app.rest.Parameter;
import org.dspace.app.rest.SearchRestMethod;
import org.dspace.app.rest.exception.DSpaceBadRequestException;
import org.dspace.app.rest.exception.RepositoryMethodNotImplementedException;
import org.dspace.app.rest.exception.UnprocessableEntityException;
import org.dspace.app.rest.model.BitstreamRest;
import org.dspace.app.rest.model.BundleRest;
import org.dspace.app.rest.model.patch.Patch;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.MetadataValue;
import org.dspace.content.service.BitstreamService;
import org.dspace.content.service.BundleService;
import org.dspace.content.service.CollectionService;
import org.dspace.content.service.CommunityService;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.dspace.handle.service.HandleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;


/**
 * This is the repository responsible to manage Bitstream Rest object
 *
 * @author Andrea Bollini (andrea.bollini at 4science.it)
 */

@Component(BitstreamRest.CATEGORY + "." + BitstreamRest.NAME)
public class BitstreamRestRepository extends DSpaceObjectRestRepository<Bitstream, BitstreamRest> {

    private final BitstreamService bs;

    @Autowired
    BundleService bundleService;

    @Autowired
    AuthorizeService authorizeService;

    @Autowired
    private CollectionService collectionService;

    @Autowired
    private CommunityService communityService;

    @Autowired
    private HandleService handleService;

    @Autowired
    private ItemService itemService;

    @Autowired
    public BitstreamRestRepository(BitstreamService dsoService) {
        super(dsoService);
        this.bs = dsoService;
    }

    @Override
    @PreAuthorize("hasPermission(#id, 'BITSTREAM', 'METADATA_READ')")
    public BitstreamRest findOne(Context context, UUID id) {
        Bitstream bit = null;
        try {
            bit = bs.find(context, id);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        if (bit == null) {
            return null;
        }
        try {
            if (bit.isDeleted() == true) {
                throw new ResourceNotFoundException();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return converter.toRest(bit, utils.obtainProjection());
    }

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    public Page<BitstreamRest> findAll(Context context, Pageable pageable) {
        throw new RepositoryMethodNotImplementedException(BitstreamRest.NAME, "findAll");
    }

    @Override
    @PreAuthorize("hasPermission(#id, 'BITSTREAM', 'WRITE')")
    protected void patch(Context context, HttpServletRequest request, String apiCategory, String model, UUID id,
                         Patch patch) throws AuthorizeException, SQLException {
        patchDSpaceObject(apiCategory, model, id, patch);
    }

    @Override
    public Class<BitstreamRest> getDomainClass() {
        return BitstreamRest.class;
    }

    @Override
    protected void delete(Context context, UUID id) throws AuthorizeException {
        Bitstream bit = null;
        try {
            bit = bs.find(context, id);
            if (bit == null) {
                throw new ResourceNotFoundException("The bitstream with uuid " + id + " could not be found");
            }
            if (bit.isDeleted()) {
                throw new ResourceNotFoundException("The bitstream with uuid " + id + " was already deleted");
            }
            Community community = bit.getCommunity();
            if (community != null) {
                communityService.setLogo(context, community, null);
            }
            Collection collection = bit.getCollection();
            if (collection != null) {
                collectionService.setLogo(context, collection, null);
            }
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        try {
            bs.delete(context, bit);
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Find the bitstream for the provided handle and sequence or filename.
     * When a bitstream can be found with the sequence ID it will be returned if the user has "METADATA_READ" access.
     *
     * @param handle    The handle of the item
     * @param sequence  The sequence ID of the bitstream
     * @param filename  The filename of the bitstream
     *
     * @return a Page of BitstreamRest instance matching the user query
     */
    @SearchRestMethod(name = "byItemHandle")
    public BitstreamRest findByItemHandle(@Parameter(value = "handle", required = true) String handle,
                                          @Parameter(value = "sequence") Integer sequence,
                                          @Parameter(value = "filename") String filename) {
        if (StringUtils.isBlank(filename) && sequence == null) {
            throw new IllegalArgumentException("The request should include a sequence or a filename");
        }

        try {
            Context context = obtainContext();
            DSpaceObject dSpaceObject = handleService.resolveToObject(context, handle);

            if (!(dSpaceObject instanceof Item)) {
                throw new UnprocessableEntityException("The provided handle does not correspond to an existing item");
            }
            Item item = (Item) dSpaceObject;

            Bitstream matchedBitstream = getFirstMatchedBitstream(item, sequence, filename);

            if (matchedBitstream == null) {
                return null;
            } else {
                return converter.toRest(matchedBitstream, utils.obtainProjection());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Find the bitstream for the provided uuid of an item, the name of bundle, the metadata field
     * and the value of this metadata.
     *
     * @param uuid The uuid of the item
     * @param name The bundle name
     * @param metadata The filter metadata field
     * @param value The filter metadata value
     *
     * @return a Page of BitstreamRest instance matching the user query
     */
    @SearchRestMethod(name = "byItemId")
    public Page<BitstreamRest> findByItemId(@Parameter(value = "uuid", required = true) UUID uuid,
                                            @Parameter(value = "name", required = true) String name,
                                            @Parameter(value = "filterMetadata") String metadata,
                                            @Parameter(value = "filterMetadataValue") String value,
                                            Pageable pageable) {

        validateParameters(metadata, value);

        try {

            List<Bitstream> bitstreams = new ArrayList<>();
            List<Bitstream> matchedBitstreams;
            Context context = obtainContext();

            Item item = itemService.find(context, uuid);

            if (item == null) {
                throw new UnprocessableEntityException("The provided uuid:" + uuid + " does not correspond to an " +
                    "existing item");
            }

            bs.getItemBitstreams(context, item).forEachRemaining(bitstreams::add);

            matchedBitstreams = filterByBundleName(bitstreams, name);

            if (metadata != null && value != null) {
                matchedBitstreams = filterByMetadataAndValue(matchedBitstreams, metadata, value);
            }

            if (matchedBitstreams.isEmpty()) {
                throw new ResourceNotFoundException("no matched bitstreams found");
            }

            return converter.toRestPage(matchedBitstreams, pageable, matchedBitstreams.size(),
                utils.obtainProjection());
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void validateParameters(String metadata, String value) {
        if ((metadata != null && value == null) || (value != null && metadata == null)) {
            throw new IllegalArgumentException("The request must include a filterMetadata " +
                "and a filterMetadataValue together");
        }
    }

    private Bitstream getFirstMatchedBitstream(Item item, Integer sequence, String filename) {
        List<Bundle> bundles = item.getBundles();
        List<Bitstream> bitstreams = new LinkedList<>();
        bundles.forEach(bundle -> bitstreams.addAll(bundle.getBitstreams()));

        if (sequence != null) {
            for (Bitstream bitstream : bitstreams) {
                if (bitstream.getSequenceID() == sequence) {
                    return bitstream;
                }
            }
        }
        if (StringUtils.isNotBlank(filename)) {
            for (Bitstream bitstream : bitstreams) {
                if (StringUtils.equals(bitstream.getName(), filename)) {
                    return bitstream;
                }
            }
        }
        return null;
    }

    public InputStream retrieve(UUID uuid) {
        Context context = obtainContext();
        Bitstream bit = null;
        try {
            bit = bs.find(context, uuid);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        if (bit == null) {
            return null;
        }
        InputStream is;
        try {
            is = bs.retrieve(context, bit);
        } catch (IOException | SQLException | AuthorizeException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        context.abort();
        return is;
    }

    /**
     * Method that will move the bitstream corresponding to the uuid to the target bundle
     *
     * @param context      The context
     * @param bitstream    The bitstream to be moved
     * @param targetBundle The target bundle
     * @return The target bundle with the bitstream attached
     */
    public BundleRest performBitstreamMove(Context context, Bitstream bitstream, Bundle targetBundle)
            throws SQLException, IOException, AuthorizeException {

        if (bitstream.getBundles().contains(targetBundle)) {
            throw new DSpaceBadRequestException("The provided bitstream is already in the target bundle");
        }

        bundleService.moveBitstreamToBundle(context, targetBundle, bitstream);

        return converter.toRest(targetBundle, utils.obtainProjection());
    }

    private List<Bitstream> filterByBundleName(List<Bitstream> bitstreams, String name) throws SQLException {
        List<Bitstream> matchedBitstreams = new ArrayList<>();
        for (Bitstream bitstream : bitstreams) {
            for (Bundle bundle : bitstream.getBundles()) {
                if (bundle.getName().equals(name)) {
                    matchedBitstreams.add(bitstream);
                }
            }
        }
        return matchedBitstreams;
    }

    private List<Bitstream> filterByMetadataAndValue(List<Bitstream> bitstreams, String metadata, String value) {
        List<Bitstream> matchedBitstreams = new ArrayList<>();
        for (Bitstream bitstream : bitstreams) {
            for (MetadataValue metadataValue : bitstream.getMetadata()) {
                if (isMatched(metadataValue, metadata, value)) {
                    matchedBitstreams.add(bitstream);
                }
            }
        }

        return matchedBitstreams;
    }

    private boolean isMatched(MetadataValue metadataValue, String metadata, String value) {

        boolean metadataMatched = metadataValue.getMetadataField().toString('.').equals(metadata);

        if (value.startsWith("(") && value.endsWith(")")) {
            value = value.substring(1, value.length() - 1);
            return metadataMatched && metadataValue.getValue().matches(value);
        } else {
            return metadataMatched && metadataValue.getValue().equals(value);
        }
    }

}
