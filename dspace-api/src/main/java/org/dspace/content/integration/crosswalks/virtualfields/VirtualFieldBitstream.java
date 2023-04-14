/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.integration.crosswalks.virtualfields;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static org.apache.commons.lang.StringUtils.startsWith;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.tika.Tika;
import org.dspace.content.Bitstream;
import org.dspace.content.BitstreamFormat;
import org.dspace.content.Bundle;
import org.dspace.content.Item;
import org.dspace.content.service.BitstreamService;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.dspace.services.ConfigurationService;
import org.dspace.storage.bitstore.service.BitstreamStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link VirtualField} to retrieve a bitstream from an item.
 *
 * @author Luca Giamminonni (luca.giamminonni at 4science.it)
 *
 */
public class VirtualFieldBitstream implements VirtualField {

    private final static Logger LOGGER = LoggerFactory.getLogger(VirtualFieldBitstream.class);

    private final static String PREVIEW_BUNDLE = "PREVIEW";

    private final static String IMAGE_MIME_TYPE_PREFIX = "image";

    private final static String JPEG_MIME_TYPE = IMAGE_MIME_TYPE_PREFIX + "/jpeg";

    private final static String JPEG_FORMAT = "jpg";

    private final ItemService itemService;

    private final BitstreamStorageService bitstreamStorageService;

    private final BitstreamService bitstreamService;

    private final ConfigurationService configurationService;

    private final Map<String, String> bitstreamTypeMap;

    private final Map<String, Set<String>> bitstreamMIMETypeMap;

    public VirtualFieldBitstream(ItemService itemService, BitstreamStorageService bitstreamStorageService,
                                 BitstreamService bitstreamService, ConfigurationService configurationService,
                                 Map<String, String> bitstreamTypeMap, Map<String, Set<String>> bitstreamMIMETypeMap) {
        this.itemService = itemService;
        this.bitstreamStorageService = bitstreamStorageService;
        this.bitstreamService = bitstreamService;
        this.configurationService = configurationService;
        this.bitstreamTypeMap = bitstreamTypeMap;
        this.bitstreamMIMETypeMap = bitstreamMIMETypeMap;
    }

    @Override
    public String[] getMetadata(Context context, Item item, String fieldName) {
        String[] virtualFieldName = fieldName.split("\\.");
        int fieldNameLength = virtualFieldName.length;

        if (fieldNameLength < 3 || fieldNameLength > 6
            || (fieldNameLength == 6 && !NumberUtils.isCreatable(virtualFieldName[5]))) {
            LOGGER.warn("Invalid bitstream virtual field: " + fieldName);
            return new String[] {};
        }

        String bundleName = virtualFieldName[2].toUpperCase();

        try {

            List<Bitstream> bitstreams = findBitstreams(item, bundleName);

            if (CollectionUtils.isEmpty(bitstreams)) {
                return new String[] {};
            }

            if (fieldNameLength == 3) {
                return getMetadata(context, item, bitstreams);
            } else if (fieldNameLength == 4) {
                if (!isFindAllTypes(virtualFieldName)) {
                    bitstreams = filterBitstreamsByType(bitstreams, virtualFieldName[3]);
                }
                return getMetadata(context, item, bitstreams);
            } else if (fieldNameLength == 5) {
                if (!isFindAllTypes(virtualFieldName)) {
                    bitstreams = filterBitstreamsByTypeAndMIMEType(context,bitstreams,
                        virtualFieldName[3], virtualFieldName[4]);
                } else {
                    bitstreams = filterBitstreamsByMIMEType(context, bitstreams, virtualFieldName[4]);
                }
                return getMetadata(context, item, bitstreams);
            } else if (fieldNameLength == 6) {
                bitstreams = limitBitstreams(context, bitstreams,
                    virtualFieldName,Integer.parseInt(virtualFieldName[5]));

                return getMetadata(context, item, bitstreams);
            }

        } catch (Exception e) {
            LOGGER.error("Error retrieving bitstream of item {} from virtual field {}", item.getID(), fieldName, e);
            return new String[] {};
        }

        return new String[] {};
    }

    private List<Bitstream> findBitstreams(Item item, String bundleName) throws Exception {

        List<Bundle> bundles = itemService.getBundles(item, bundleName);

        if (CollectionUtils.isEmpty(bundles)) {
            return new ArrayList<>();
        }

        return bundles.stream()
                      .flatMap(bundle ->
                          bundle.getBitstreams().stream())
                      .collect(Collectors.toList());
    }

    private List<Bitstream> limitBitstreams(Context context, List<Bitstream> bitstreams,
                                            String[] virtualFieldName, int limit) {

        if (!isFindAllTypes(virtualFieldName) && !isFindAllMIMETypes(virtualFieldName)) {
            bitstreams = filterBitstreamsByTypeAndMIMEType(context,bitstreams,
                virtualFieldName[3], virtualFieldName[4]);
        } else if (isFindAllTypes(virtualFieldName) && !isFindAllMIMETypes(virtualFieldName)) {
            bitstreams = filterBitstreamsByMIMEType(context, bitstreams, virtualFieldName[4]);
        } else if (!isFindAllTypes(virtualFieldName) && isFindAllMIMETypes(virtualFieldName)) {
            bitstreams = filterBitstreamsByType(bitstreams, virtualFieldName[3]);
        }

        return bitstreams.stream()
                         .limit(limit)
                         .collect(Collectors.toList());
    }

    private String[] getMetadata( Context context, Item item, List<Bitstream> bitstreams) throws Exception {
        List<String> metadata = new ArrayList<>();

        if (shouldUsePreview()) {
            bitstreams = findPreviews(item, bitstreams);
        }

        for (Bitstream bitstream : bitstreams) {
            metadata.addAll(Arrays.asList(writeTemporaryFile(context, bitstream)));
        }

        return metadata.toArray(new String[metadata.size()]);
    }

    private List<Bitstream> findPreviews(Item item, List<Bitstream> bitstreams) throws SQLException {
        List<Bundle> bundles = itemService.getBundles(item, PREVIEW_BUNDLE);
        if (CollectionUtils.isEmpty(bundles)) {
            return bitstreams;
        }

        return bitstreams.stream()
                         .map(bitstream ->
                             findBitstream(
                                 bundles,
                                 bs -> startsWith(bs.getName(), bitstream.getName())
                             ).orElse(bitstream)
                         ).collect(Collectors.toList());
    }

    private List<Bitstream> filterBitstreamsByType(List<Bitstream> bitstreams, String bitstreamType) {
        return findBitstreams(bitstreams, bs -> hasTypeEqualsTo(bs, getBitstreamType(bitstreamType)));
    }

    private List<Bitstream> filterBitstreamsByMIMEType(Context context, List<Bitstream> bitstreams, String mimeType) {
        Set<String> mimeTypes = getMIMETypes(mimeType);

        return findBitstreams(bitstreams,
            bs -> mimeTypes.contains(getBitstreamMimeType(context, bs)));
    }

    private List<Bitstream> filterBitstreamsByTypeAndMIMEType(Context context,
                                                              List<Bitstream> bitstreams,
                                                              String type, String mimeType) {

        List<Bitstream> filteredBitstreams = filterBitstreamsByType(bitstreams, type);
        return filterBitstreamsByMIMEType(context, filteredBitstreams, mimeType);
    }

    private Set<String> getMIMETypes(String mimeType) {
        Set<String> mimeTypes = new HashSet<>();
        if (bitstreamMIMETypeMap.containsKey(mimeType)) {
            mimeTypes.addAll(bitstreamMIMETypeMap.get(mimeType));
        }
        return mimeTypes;
    }

    private String getBitstreamType(String type) {
        if (bitstreamTypeMap.containsKey(type)) {
            return bitstreamTypeMap.get(type);
        }
        return type;
    }

    private List<Bitstream> findBitstreams(List<Bitstream> bitstreams, Predicate<Bitstream> predicate) {
        return bitstreams.stream()
                         .filter(predicate)
                         .collect(Collectors.toList());
    }

    private boolean hasTypeEqualsTo(Bitstream bitstream, String type) {
        return type.equals(bitstreamService.getMetadataFirstValue(bitstream, "dc", "type", null, Item.ANY));
    }

    private Optional<Bitstream> findBitstream(List<Bundle> bundles, Predicate<Bitstream> predicate) {
        return bundles.stream()
            .flatMap(bundle -> bundle.getBitstreams().stream())
            .filter(predicate)
            .findFirst();
    }

    private String[] writeTemporaryFile(Context context, Bitstream bitstream) throws Exception {

        InputStream inputStream = bitstreamStorageService.retrieve(context, bitstream);
        if (inputStream == null) {
            return new String[] {};
        }

        String format = getBitstreamMimeType(context, bitstream);

        if (format != null && format.startsWith(IMAGE_MIME_TYPE_PREFIX) && !format.equals(JPEG_MIME_TYPE)) {
            inputStream = convertToJpeg(inputStream);
        }

        File tempFile = createEmptyTemporaryFile(bitstream);

        try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
            IOUtils.copy(inputStream, outputStream);
            return new String[] { tempFile.getName() };
        }

    }

    private String getBitstreamMimeType(Context context, Bitstream bitstream) {

        try {
            BitstreamFormat format = bitstream.getFormat(context);
            if (format != null && format.getSupportLevel() != BitstreamFormat.UNKNOWN) {
                return format.getMIMEType();
            } else {
                return new Tika().detect(bitstreamStorageService.retrieve(context, bitstream));
            }
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }

    }

    private File createEmptyTemporaryFile(Bitstream bitstream) throws IOException {
        String tempFileName = bitstream.getID().toString();
        File tempFile = new File(getTempExportDir(), tempFileName);
        tempFile.createNewFile();
        tempFile.deleteOnExit();
        return tempFile;
    }

    private InputStream convertToJpeg(InputStream inputStream) throws IOException {

        BufferedImage image = ImageIO.read(inputStream);

        if (image == null) {
            return inputStream;
        }

        BufferedImage convertedImage = new BufferedImage(image.getWidth(), image.getHeight(), TYPE_INT_RGB);
        convertedImage.createGraphics().drawImage(image, 0, 0, Color.WHITE, null);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(convertedImage, JPEG_FORMAT, outputStream);

        return new ByteArrayInputStream(outputStream.toByteArray());

    }

    private File getTempExportDir() {
        String tempDir = configurationService.getProperty("crosswalk.virtualfield.bitstream.tempdir", " export");
        File tempExportDir = new File(System.getProperty("java.io.tmpdir"), tempDir);
        if (!tempExportDir.exists()) {
            tempExportDir.mkdirs();
            tempExportDir.deleteOnExit();
        }
        return tempExportDir;
    }

    private boolean isFindAllTypes(String[] virtualFieldName) {
        return virtualFieldName.length > 3 && virtualFieldName[3].equals("*");
    }

    private boolean isFindAllMIMETypes(String[] virtualFieldName) {
        return virtualFieldName.length > 4 && virtualFieldName[4].equals("*");
    }

    private boolean shouldUsePreview() {
        return configurationService.getBooleanProperty("crosswalk.virtualfield.bitstream.use-preview", true);
    }

}
