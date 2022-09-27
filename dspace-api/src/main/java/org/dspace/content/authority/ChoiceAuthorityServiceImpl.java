/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.authority;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.dspace.app.util.DCInput;
import org.dspace.app.util.DCInputSet;
import org.dspace.app.util.DCInputsReader;
import org.dspace.app.util.DCInputsReaderException;
import org.dspace.app.util.SubmissionConfig;
import org.dspace.app.util.SubmissionConfigReader;
import org.dspace.app.util.SubmissionConfigReaderException;
import org.dspace.content.Collection;
import org.dspace.content.MetadataValue;
import org.dspace.content.authority.service.ChoiceAuthorityService;
import org.dspace.core.Constants;
import org.dspace.core.Utils;
import org.dspace.core.service.PluginService;
import org.dspace.services.ConfigurationService;
import org.dspace.submit.model.UploadConfiguration;
import org.dspace.submit.model.UploadConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Broker for ChoiceAuthority plugins, and for other information configured
 * about the choice aspect of authority control for a metadata field.
 *
 * Configuration keys, per metadata field (e.g. "dc.contributer.author")
 *
 * {@code
 * # names the ChoiceAuthority plugin called for this field
 * choices.plugin.<FIELD> = name-of-plugin
 *
 * # mode of UI presentation desired in submission UI:
 * #  "select" is dropdown menu, "lookup" is popup with selector, "suggest" is autocomplete/suggest
 * choices.presentation.<FIELD> = "select" | "suggest"
 *
 * # is value "closed" to the set of these choices or are non-authority values permitted?
 * choices.closed.<FIELD> = true | false
 * }
 *
 * @author Larry Stone
 * @see ChoiceAuthority
 */
public final class ChoiceAuthorityServiceImpl implements ChoiceAuthorityService {
    private Logger log = org.apache.logging.log4j.LogManager.getLogger(ChoiceAuthorityServiceImpl.class);

    // map of field key to authority plugin
    protected Map<String, ChoiceAuthority> controller = new HashMap<String, ChoiceAuthority>();

    // map of field key, dsoType, form definition to authority plugin
    protected Map<String, Map<Integer, Map<String, ChoiceAuthority>>> controllerFormDefinitions =
            new HashMap<String, Map<Integer, Map<String, ChoiceAuthority>>>();

    // map of field key to presentation type
    protected Map<String, String> presentation = new HashMap<String, String>();

    // map of field key to closed value
    protected Map<String, Boolean> closed = new HashMap<String, Boolean>();

    // flag to track the initialization status of the service
    private boolean initialized = false;

    // map of authority name to field keys (the same authority can be configured over multiple metadata)
    // the keys used by this cache are unique across all the dso type so there is
    // no need to add a dsoType (int key) level
    protected Map<String, List<String>> authorities = new HashMap<String, List<String>>();

    // map of authority name to form definition and field keys
    // the keys used by this cache are unique across all the dso type so there is
    // no need to add a dsoType (int key) level
    protected Map<String, Map<String, List<String>>> authoritiesFormDefinitions =
            new HashMap<String, Map<String, List<String>>>();

    // the item submission reader
    private SubmissionConfigReader itemSubmissionConfigReader;

    @Autowired(required = true)
    protected ConfigurationService configurationService;
    @Autowired(required = true)
    protected PluginService pluginService;
    @Autowired(required = true)
    protected UploadConfigurationService uploadConfigurationService;
    @Autowired(required = true)
    protected AuthorityServiceUtils authorityServiceUtils;

    final static String CHOICES_PLUGIN_PREFIX = "choices.plugin.";
    final static String CHOICES_PRESENTATION_PREFIX = "choices.presentation.";
    final static String CHOICES_CLOSED_PREFIX = "choices.closed.";

    protected ChoiceAuthorityServiceImpl() {
    }

    // translate tail of configuration key (supposed to be schema.element.qual)
    // into field key
    protected String config2fkey(String field) {
        // field is expected to be "schema.element.qualifier"
        int dot = field.indexOf('.');
        if (dot < 0) {
            return null;
        }
        String schema = field.substring(0, dot);
        String element = field.substring(dot + 1);
        String qualifier = null;
        dot = element.indexOf('.');
        if (dot >= 0) {
            qualifier = element.substring(dot + 1);
            element = element.substring(0, dot);
        }
        return makeFieldKey(schema, element, qualifier);
    }

    @Override
    public Set<String> getChoiceAuthoritiesNames() {
        init();
        Set<String> authoritiesNames = new HashSet<String>();
        authoritiesNames.addAll(authorities.keySet());
        authoritiesNames.addAll(authoritiesFormDefinitions.keySet());
        return authoritiesNames;
    }

    private synchronized void init() {
        if (!initialized) {
            try {
                itemSubmissionConfigReader = new SubmissionConfigReader();
            } catch (SubmissionConfigReaderException e) {
                // the system is in an illegal state as the submission definition is not valid
                throw new IllegalStateException("Error reading the item submission configuration: " + e.getMessage(),
                        e);
            }
            loadChoiceAuthorityConfigurations();
            initialized = true;
        }
    }

    @Override
    public Choices getBestMatch(String fieldKey, String query, int dsoType, Collection collection,
                                String locale) {
        ChoiceAuthority ma = getAuthorityByFieldKeyCollection(fieldKey, dsoType, collection);
        if (ma == null) {
            String errorMessage = "No choices plugin was configured for  field \"" + fieldKey + "\"";
            if (collection != null) {
                errorMessage = errorMessage + ", collection=" + collection.getID().toString();
            }
            throw new IllegalArgumentException(errorMessage);
        }
        return ma.getBestMatch(query, locale);
    }

    @Override
    public String getLabel(MetadataValue metadataValue, int dsoType, Collection collection, String locale) {
        return getLabel(metadataValue.getMetadataField().toString(), dsoType, collection, metadataValue.getAuthority(),
                locale);
    }

    @Override
    public String getLabel(String fieldKey, int dsoType, Collection collection, String authKey, String locale) {
        ChoiceAuthority ma = getAuthorityByFieldKeyCollection(fieldKey, dsoType, collection);
        if (ma == null) {
            throw new IllegalArgumentException(
                "No choices plugin was configured for  field \"" + fieldKey
                    + "\", collection=" + collection.getID().toString() + ".");
        }
        return ma.getLabel(authKey, locale);
    }

    @Override
    public boolean isChoicesConfigured(String fieldKey, int dsoType, Collection collection) {
        return getAuthorityByFieldKeyCollection(fieldKey, dsoType, collection) != null;
    }

    @Override
    public String getPresentation(String fieldKey) {
        return getPresentationMap().get(fieldKey);
    }

    @Override
    public boolean isClosed(String fieldKey) {
        return getClosedMap().containsKey(fieldKey) && getClosedMap().get(fieldKey);
    }

    @Override
    public List<String> getVariants(MetadataValue metadataValue, int dsoType, Collection collection) {
        String fieldKey = metadataValue.getMetadataField().toString();
        ChoiceAuthority ma = getAuthorityByFieldKeyCollection(fieldKey, dsoType, collection);
        if (ma == null) {
            throw new IllegalArgumentException(
                "No choices plugin was configured for  field \"" + fieldKey
                    + "\", collection=" + collection.getID().toString() + ".");
        }
        if (ma instanceof AuthorityVariantsSupport) {
            AuthorityVariantsSupport avs = (AuthorityVariantsSupport) ma;
            return avs.getVariants(metadataValue.getAuthority(), metadataValue.getLanguage());
        }
        return null;
    }


    @Override
    public String getChoiceAuthorityName(String schema, String element, String qualifier, int dsoType,
            Collection collection) {
        init();
        String fieldKey = makeFieldKey(schema, element, qualifier);
        // check if there is an authority configured for the metadata valid for all the collections
        if (controller.containsKey(fieldKey)) {
            for (Entry<String, List<String>> authority2md : authorities.entrySet()) {
                if (authority2md.getValue().contains(fieldKey)) {
                    return authority2md.getKey();
                }
            }
        } else if (collection != null && controllerFormDefinitions.containsKey(fieldKey)) {
            // there is an authority configured for the metadata valid for some collections,
            // check if it is the requested collection
            Map<Integer, Map<String, ChoiceAuthority>> controllerFormDefTypes = controllerFormDefinitions.get(fieldKey);
            Map<String, ChoiceAuthority> controllerFormDef = controllerFormDefTypes.get(dsoType);
            SubmissionConfig submissionConfig = itemSubmissionConfigReader.getSubmissionConfigByCollection(collection);
            String submissionName = submissionConfig.getSubmissionName();
            // check if the requested collection has a submission definition that use an authority for the metadata
            if (controllerFormDef.containsKey(submissionName)) {
                for (Entry<String, Map<String, List<String>>> authority2defs2md :
                        authoritiesFormDefinitions.entrySet()) {
                    List<String> mdByDefinition = authority2defs2md.getValue().get(submissionName);
                    if (mdByDefinition != null && mdByDefinition.contains(fieldKey)) {
                        return authority2defs2md.getKey();
                    }
                }
            }
        }
        return null;
    }

    protected String makeFieldKey(String schema, String element, String qualifier) {
        return Utils.standardize(schema, element, qualifier, "_");
    }

    @Override
    public void clearCache() {
        controller.clear();
        authorities.clear();
        presentation.clear();
        closed.clear();
        controllerFormDefinitions.clear();
        authoritiesFormDefinitions.clear();
        itemSubmissionConfigReader = null;
        initialized = false;
    }

    private void loadChoiceAuthorityConfigurations() {
        // Get all configuration keys starting with a given prefix
        List<String> propKeys = configurationService.getPropertyKeys(CHOICES_PLUGIN_PREFIX);
        Iterator<String> keyIterator = propKeys.iterator();
        while (keyIterator.hasNext()) {
            String key = keyIterator.next();
            String fkey = config2fkey(key.substring(CHOICES_PLUGIN_PREFIX.length()));
            if (fkey == null) {
                log.warn(
                    "Skipping invalid ChoiceAuthority configuration property: " + key + ": does not have schema" +
                        ".element.qualifier");
                continue;
            }

            // XXX FIXME maybe add sanity check, call
            // MetadataField.findByElement to make sure it's a real field.
            String authorityName = configurationService.getProperty(key);
            ChoiceAuthority ma = (ChoiceAuthority)
                pluginService.getNamedPlugin(ChoiceAuthority.class, authorityName);
            if (ma == null) {
                log.warn(
                    "Skipping invalid configuration for " + key + " because named plugin not found: " + authorityName);
                continue;
            }

            controller.put(fkey, ma);
            List<String> fkeys;
            if (authorities.containsKey(authorityName)) {
                fkeys = authorities.get(authorityName);
            } else {
                fkeys = new ArrayList<String>();
            }
            fkeys.add(fkey);
            authorities.put(authorityName, fkeys);
            log.debug("Choice Control: For field=" + fkey + ", Plugin=" + ma);
        }
        autoRegisterChoiceAuthorityFromInputReader();
    }

    /**
     * This method will register all the authorities that are required due to the
     * submission forms configuration. This includes authorities for value pairs and
     * xml vocabularies
     */
    private void autoRegisterChoiceAuthorityFromInputReader() {
        try {
            List<SubmissionConfig> submissionConfigs = itemSubmissionConfigReader
                    .getAllSubmissionConfigs(Integer.MAX_VALUE, 0);
            DCInputsReader dcInputsReader = new DCInputsReader();

            // loop over all the defined item submission configuration
            for (SubmissionConfig subCfg : submissionConfigs) {
                String submissionName = subCfg.getSubmissionName();
                List<DCInputSet> inputsBySubmissionName = dcInputsReader.getInputsBySubmissionName(submissionName);
                autoRegisterChoiceAuthorityFromSubmissionForms(Constants.ITEM, submissionName,
                        inputsBySubmissionName);
            }
            // loop over all the defined bitstream metadata submission configuration
            for (UploadConfiguration uploadCfg : uploadConfigurationService.getMap().values()) {
                String formName = uploadCfg.getMetadata();
                DCInputSet inputByFormName = dcInputsReader.getInputsByFormName(formName);
                autoRegisterChoiceAuthorityFromSubmissionForms(Constants.BITSTREAM, formName,
                        List.of(inputByFormName));
            }
        } catch (DCInputsReaderException e) {
            // the system is in an illegal state as the submission definition is not valid
            throw new IllegalStateException("Error reading the item submission configuration: " + e.getMessage(),
                    e);
        }
    }

    private void autoRegisterChoiceAuthorityFromSubmissionForms(int dsoType, String submissionName,
            List<DCInputSet> inputsBySubmissionName) {
        // loop over the submission forms configuration eventually associated with the
        // submission panel
        for (DCInputSet dcinputSet : inputsBySubmissionName) {
            DCInput[][] dcinputs = dcinputSet.getFields();
            for (DCInput[] dcrows : dcinputs) {
                for (DCInput dcinput : dcrows) {
                    // for each input in the form check if it is associated with a real value pairs
                    // or an xml vocabulary
                    String authorityName = null;
                    if (StringUtils.isNotBlank(dcinput.getPairsType())
                            && !StringUtils.equals(dcinput.getInputType(), "qualdrop_value")) {
                        authorityName = dcinput.getPairsType();
                    } else if (StringUtils.isNotBlank(dcinput.getVocabulary())) {
                        authorityName = dcinput.getVocabulary();
                    }

                    // do we have an authority?
                    if (StringUtils.isNotBlank(authorityName)) {
                        String fieldKey = makeFieldKey(dcinput.getSchema(), dcinput.getElement(),
                                dcinput.getQualifier());
                        ChoiceAuthority ca = controller.get(authorityName);
                        if (ca == null) {
                            ca = (ChoiceAuthority) pluginService.getNamedPlugin(ChoiceAuthority.class, authorityName);
                            if (ca == null) {
                                throw new IllegalStateException("Invalid configuration for " + fieldKey
                                        + " in submission definition " + submissionName + ", form definition "
                                        + dcinputSet.getFormName() + " no named plugin found: " + authorityName);
                            }
                        }

                        addAuthorityToFormCacheMap(dsoType, submissionName, fieldKey, ca);
                        addFormDetailsToAuthorityCacheMap(submissionName, authorityName, fieldKey);
                    }
                }
            }
        }
    }

    /**
     * Add the form/field to the cache map keeping track of which form/field are
     * associated with the specific authority name
     *
     * @param submissionName the form definition name
     * @param authorityName  the name of the authority plugin
     * @param fieldKey       the field key that use the authority
     */
    private void addFormDetailsToAuthorityCacheMap(String submissionName, String authorityName,
            String fieldKey) {
        Map<String, List<String>> submissionDefinitionNames2fieldKeys;
        if (authoritiesFormDefinitions.containsKey(authorityName)) {
            submissionDefinitionNames2fieldKeys = authoritiesFormDefinitions.get(authorityName);
        } else {
            submissionDefinitionNames2fieldKeys = new HashMap<String, List<String>>();
        }

        List<String> fields;
        if (submissionDefinitionNames2fieldKeys.containsKey(submissionName)) {
            fields = submissionDefinitionNames2fieldKeys.get(submissionName);
        } else {
            fields = new ArrayList<String>();
        }
        fields.add(fieldKey);
        submissionDefinitionNames2fieldKeys.put(submissionName, fields);
        authoritiesFormDefinitions.put(authorityName, submissionDefinitionNames2fieldKeys);
    }

    /**
     * Add the authority plugin to the cache map keeping track of which authority is
     * used by a specific form/field
     * 
     * @param dsoType        the DSpace Object Type
     * @param submissionName the submission definition name
     * @param fieldKey       the field key that require the authority
     * @param ca             the authority plugin
     */
    private void addAuthorityToFormCacheMap(int dsoType, String submissionName, String fieldKey, ChoiceAuthority ca) {
        Map<Integer, Map<String, ChoiceAuthority>> definition2type2authority;
        Map<String, ChoiceAuthority> type2authority;
        if (controllerFormDefinitions.containsKey(fieldKey)) {
            definition2type2authority = controllerFormDefinitions.get(fieldKey);
        } else {
            definition2type2authority = new HashMap<Integer, Map<String, ChoiceAuthority>>();
        }
        if (definition2type2authority.containsKey(dsoType)) {
            type2authority = definition2type2authority.get(dsoType);
        } else {
            type2authority = new HashMap<String, ChoiceAuthority>();
        }

        type2authority.put(submissionName, ca);
        definition2type2authority.put(dsoType, type2authority);
        controllerFormDefinitions.put(fieldKey, definition2type2authority);
    }

    /**
     * Return map of key to presentation
     *
     * @return
     */
    private Map<String, String> getPresentationMap() {
        // If empty, load from configuration
        if (presentation.isEmpty()) {
            // Get all configuration keys starting with a given prefix
            List<String> propKeys = configurationService.getPropertyKeys(CHOICES_PRESENTATION_PREFIX);
            Iterator<String> keyIterator = propKeys.iterator();
            while (keyIterator.hasNext()) {
                String key = keyIterator.next();

                String fkey = config2fkey(key.substring(CHOICES_PRESENTATION_PREFIX.length()));
                if (fkey == null) {
                    log.warn(
                        "Skipping invalid ChoiceAuthority configuration property: " + key + ": does not have schema" +
                            ".element.qualifier");
                    continue;
                }
                presentation.put(fkey, configurationService.getProperty(key));
            }
        }

        return presentation;
    }

    /**
     * Return map of key to closed setting
     *
     * @return
     */
    private Map<String, Boolean> getClosedMap() {
        // If empty, load from configuration
        if (closed.isEmpty()) {
            // Get all configuration keys starting with a given prefix
            List<String> propKeys = configurationService.getPropertyKeys(CHOICES_CLOSED_PREFIX);
            Iterator<String> keyIterator = propKeys.iterator();
            while (keyIterator.hasNext()) {
                String key = keyIterator.next();

                String fkey = config2fkey(key.substring(CHOICES_CLOSED_PREFIX.length()));
                if (fkey == null) {
                    log.warn(
                        "Skipping invalid ChoiceAuthority configuration property: " + key + ": does not have schema" +
                            ".element.qualifier");
                    continue;
                }
                closed.put(fkey, configurationService.getBooleanProperty(key));
            }
        }

        return closed;
    }

    @Override
    public ChoiceAuthority getChoiceAuthorityByAuthorityName(String authorityName) throws IllegalArgumentException {
        ChoiceAuthority ma = (ChoiceAuthority)
            pluginService.getNamedPlugin(ChoiceAuthority.class, authorityName);
        if (ma == null) {
            throw new IllegalArgumentException(
                "No choices plugin was configured for authorityName \"" + authorityName
                    + "\".");
        }
        return ma;
    }

    @Override
    public ChoiceAuthority getAuthorityByFieldKeyCollection(String fieldKey, int dsoType, Collection collection) {
        init();
        ChoiceAuthority ma = controller.get(fieldKey);
        if (ma == null && collection != null) {
            String submissionName = authorityServiceUtils.getSubmissionOrFormName(itemSubmissionConfigReader,
                    dsoType, collection);
            if (submissionName == null) {
                log.warn("No submission name was found for object type " + dsoType + " in collection "
                        + collection.getHandle());
                return null;
            }
            Map<Integer, Map<String, ChoiceAuthority>> mapType2SubAuth = controllerFormDefinitions.get(fieldKey);
            if (mapType2SubAuth != null) {
                Map<String, ChoiceAuthority> mapSubAuth = mapType2SubAuth.get(dsoType);
                if (mapSubAuth != null) {
                    ma = mapSubAuth.get(submissionName);
                }
            }
        }
        return ma;
    }

    /**
     * Wrapper that calls getChoicesByParent method of the plugin.
     *
     * @param authorityName authority name
     * @param parentId      parent Id
     * @param start         choice at which to start, 0 is first.
     * @param limit         maximum number of choices to return, 0 for no limit.
     * @param locale        explicit localization key if available, or null
     * @return a Choices object (never null).
     * @see org.dspace.content.authority.ChoiceAuthority#getChoicesByParent(java.lang.String, java.lang.String,
     *  int, int, java.lang.String)
     */
    @Override
    public Choices getChoicesByParent(String authorityName, String parentId, int start, int limit, String locale) {
        HierarchicalAuthority ma = (HierarchicalAuthority) getChoiceAuthorityByAuthorityName(authorityName);
        return ma.getChoicesByParent(authorityName, parentId, start, limit, locale);
    }

    /**
     * Wrapper that calls getTopChoices method of the plugin.
     *
     * @param authorityName authority name
     * @param start         choice at which to start, 0 is first.
     * @param limit         maximum number of choices to return, 0 for no limit.
     * @param locale        explicit localization key if available, or null
     * @return a Choices object (never null).
     * @see org.dspace.content.authority.ChoiceAuthority#getTopChoices(java.lang.String, int, int, java.lang.String)
     */
    @Override
    public Choices getTopChoices(String authorityName, int start, int limit, String locale) {
        HierarchicalAuthority ma = (HierarchicalAuthority) getChoiceAuthorityByAuthorityName(authorityName);
        return ma.getTopChoices(authorityName, start, limit, locale);
    }

    @Override
    public String getLinkedEntityType(String fieldKey) {
        ChoiceAuthority ma = getAuthorityByFieldKeyCollection(fieldKey, Constants.ITEM, null);
        if (ma == null) {
            throw new IllegalArgumentException("No choices plugin was configured for  field \"" + fieldKey + "\".");
        }
        if (ma instanceof LinkableEntityAuthority) {
            return ((LinkableEntityAuthority) ma).getLinkedEntityType();
        }
        return null;
    }

    public Choice getParentChoice(String authorityName, String vocabularyId, String locale) {
        HierarchicalAuthority ma = (HierarchicalAuthority) getChoiceAuthorityByAuthorityName(authorityName);
        return ma.getParentChoice(authorityName, vocabularyId, locale);
    }

    @Override
    public List<String> getAuthorityControlledFieldsByEntityType(String entityType) {
        init();

        if (StringUtils.isEmpty(entityType)) {
            return new ArrayList<String>(controller.keySet());
        }

        return controller.keySet().stream()
            .filter(field -> isLinkableToAnEntityWithEntityType(controller.get(field), entityType))
            .collect(Collectors.toList());
    }

    private boolean isLinkableToAnEntityWithEntityType(ChoiceAuthority choiceAuthority, String entityType) {

        return choiceAuthority instanceof LinkableEntityAuthority
            && entityType.equals(((LinkableEntityAuthority) choiceAuthority).getLinkedEntityType());
    }
}
