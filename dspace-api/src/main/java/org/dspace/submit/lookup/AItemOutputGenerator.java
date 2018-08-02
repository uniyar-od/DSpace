/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.submit.lookup;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gr.ekt.bte.core.DataOutputSpec;
import gr.ekt.bte.core.OutputGenerator;
import gr.ekt.bte.core.Record;
import gr.ekt.bte.core.RecordSet;
import gr.ekt.bte.core.Value;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.AdditionalMetadataUpdateProcessPlugin;
import org.dspace.content.Item;
import org.dspace.content.service.ItemService;
import org.dspace.content.service.MetadataFieldService;
import org.dspace.content.service.MetadataSchemaService;
import org.dspace.content.service.WorkspaceItemService;
import org.dspace.core.Context;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.dspace.submit.util.ItemSubmissionLookupDTO;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Andrea Bollini
 * @author Kostas Stamatis
 * @author Luigi Andrea Pascarelli
 * @author Panagiotis Koutsourakis
 */
public abstract class AItemOutputGenerator implements OutputGenerator {

    private static Logger log = Logger
        .getLogger(AItemOutputGenerator.class);

    protected Context context;

    protected String formName;

    protected ItemSubmissionLookupDTO dto;

    Map<String, String> outputMap;

    protected List<String> extraMetadataToKeep;

    @Autowired(required = true)
    protected ItemService itemService;
    @Autowired(required = true)
    protected MetadataFieldService metadataFieldService;
    @Autowired(required = true)
    protected MetadataSchemaService metadataSchemaService;
    @Autowired(required = true)
    protected WorkspaceItemService workspaceItemService;

    @Override
    public List<String> generateOutput(RecordSet records, DataOutputSpec spec) {
        return generateOutput(records);
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setFormName(String formName) {
        this.formName = formName;
    }

    public void setDto(ItemSubmissionLookupDTO dto) {
        this.dto = dto;
    }

    public void setOutputMap(Map<String, String> outputMap) {
        this.outputMap = outputMap;
    }

    public void setExtraMetadataToKeep(List<String> extraMetadataToKeep) {
        this.extraMetadataToKeep = extraMetadataToKeep;
    }

    // Methods
    public void merge(String formName, Item item, Record record) {
        try {
            Record itemLookup = record;

            Set<String> addedMetadata = new HashSet<String>();
            for (String field : itemLookup.getFields()) {
                String metadata = getMetadata(formName, itemLookup, field);
                if (StringUtils.isBlank(metadata)) {
                    continue;
                }
                if (itemService.getMetadataByMetadataString(item, metadata).size() == 0
                        || addedMetadata.contains(metadata)) {
                    addedMetadata.add(metadata);
                    String[] md = splitMetadata(metadata);

                    List<Value> values = itemLookup.getValues(field);
                    if (values != null && values.size() > 0) {
                        for (Value value : values) {
                            String[] splitValue = splitValue(value.getAsString());
                            try {
                                if (splitValue[3] != null) {
                                    itemService.addMetadata(context, item, md[0], md[1], md[2], md[3], splitValue[0],
                                            splitValue[1], Integer.parseInt(splitValue[2]));
                                } else {
                                    itemService.addMetadata(context, item, md[0], md[1], md[2], md[3],
                                            value.getAsString());
                                }
                            } catch (SQLException ex) {
                                log.warn(ex.getMessage(), ex);
                            }
                        }
                    }
                }
            }
            itemService.update(context, item);

            String providerName = "";
            List<Value> providerNames = itemLookup.getValues("provider_name_field");
            if (providerNames != null && providerNames.size() > 0) {
                providerName = providerNames.get(0).getAsString();
            }
            List<AdditionalMetadataUpdateProcessPlugin> additionalMetadataUpdateProcessPlugins =
                (List<AdditionalMetadataUpdateProcessPlugin>) DSpaceServicesFactory
                    .getInstance()
                    .getServiceManager().getServicesByType(AdditionalMetadataUpdateProcessPlugin.class);
            for (AdditionalMetadataUpdateProcessPlugin additionalMetadataUpdateProcessPlugin :
                additionalMetadataUpdateProcessPlugins) {
                additionalMetadataUpdateProcessPlugin.process(this.context, item, providerName);
            }

            itemService.update(context, item);
        } catch (SQLException | NullPointerException e) {
            log.error(e.getMessage(), e);
        } catch (AuthorizeException e) {
            log.error(e.getMessage(), e);
        }

    }

    protected String getMetadata(String formName, Record itemLookup, String name) {
        String type = SubmissionLookupService.getType(itemLookup);

        String md = outputMap.get(type + "." + name);
        if (StringUtils.isBlank(md)) {
            md = outputMap.get(formName + "." + name);
            if (StringUtils.isBlank(md)) {
                md = outputMap.get(name);
            }
        }

        // KSTA:ToDo: Make this a modifier
        if (md != null && md.contains("|")) {
            String[] cond = md.trim().split("\\|");
            for (int idx = 1; idx < cond.length; idx++) {
                boolean temp = itemLookup.getFields().contains(cond[idx]);
                if (temp) {
                    return null;
                }
            }
            return cond[0];
        }
        return md;
    }

    protected String[] splitMetadata(String metadata) {
        String[] mdSplit = new String[3];
        if (StringUtils.isNotBlank(metadata)) {
            String tmpSplit[] = metadata.split("\\.");
            if (tmpSplit.length == 4) {
                mdSplit = new String[4];
                mdSplit[0] = tmpSplit[0];
                mdSplit[1] = tmpSplit[1];
                mdSplit[2] = tmpSplit[2];
                mdSplit[3] = tmpSplit[3];
            } else if (tmpSplit.length == 3) {
                mdSplit = new String[4];
                mdSplit[0] = tmpSplit[0];
                mdSplit[1] = tmpSplit[1];
                mdSplit[2] = tmpSplit[2];
                mdSplit[3] = null;
            } else if (tmpSplit.length == 2) {
                mdSplit = new String[4];
                mdSplit[0] = tmpSplit[0];
                mdSplit[1] = tmpSplit[1];
                mdSplit[2] = null;
                mdSplit[3] = null;
            }
        }
        return mdSplit;
    }

    protected String[] splitValue(String value) {
        String[] splitted = value
            .split(SubmissionLookupService.SEPARATOR_VALUE_REGEX);
        String[] result = new String[6];
        result[0] = splitted[0];
        result[2] = "-1";
        result[3] = "-1";
        result[4] = "-1";
        if (splitted.length > 1) {
            result[5] = "splitted";
            if (StringUtils.isNotBlank(splitted[1])) {
                result[1] = splitted[1];
            }
            if (splitted.length > 2) {
                result[2] = String.valueOf(Integer.parseInt(splitted[2]));
                if (splitted.length > 3) {
                    result[3] = String.valueOf(Integer.parseInt(splitted[3]));
                    if (splitted.length > 4) {
                        result[4] = String.valueOf(Integer
                                                       .parseInt(splitted[4]));
                    }
                }
            }
        }
        return result;
    }

}
