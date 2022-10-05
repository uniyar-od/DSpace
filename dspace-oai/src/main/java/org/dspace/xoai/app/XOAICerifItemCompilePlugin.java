/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.xoai.app;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.lyncode.xoai.dataprovider.xml.xoai.Element;
import com.lyncode.xoai.dataprovider.xml.xoai.Metadata;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dspace.content.Item;
import org.dspace.content.ItemServiceImpl;
import org.dspace.content.crosswalk.StreamDisseminationCrosswalk;
import org.dspace.content.integration.crosswalks.StreamDisseminationCrosswalkMapper;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.factory.EPersonServiceFactory;
import org.dspace.eperson.service.EPersonService;
import org.dspace.utils.DSpace;
import org.dspace.xoai.util.ItemUtils;



/**
 * Utility class to enrich content of 'item.compile' solr document field, which adds a metadata containing cerif
 * representation of a DSpace item.
 * Additional xml is written into a node named "cerif." + value of field name passed in plugin configuration
 *
 */
public class XOAICerifItemCompilePlugin implements XOAIExtensionItemCompilePlugin {

    private static Logger log = LogManager.getLogger(XOAICerifItemCompilePlugin.class);

    private EPersonService ePersonService = EPersonServiceFactory.getInstance().getEPersonService();

    private String generator;
    private String fieldName;
    private String ePersonName;

    public String getGenerator() {
        return generator;
    }

    public void setGenerator(String generator) {
        this.generator = generator;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public void setePersonName(String ePersonName) {
        this.ePersonName = ePersonName;
    }

    @Override
    public Metadata additionalMetadata(Context context, Metadata metadata, Item item) {
        EPerson currentUser = context.getCurrentUser();
        try {
            if (StringUtils.isNotBlank(ePersonName)) {
                EPerson ePerson = ePersonService.findByEmail(context, ePersonName);
                Optional.ofNullable(ePerson).ifPresent(context::setCurrentUser);
            }
            StreamDisseminationCrosswalkMapper crosswalkMapper = new DSpace().getSingletonService(
                                                                         StreamDisseminationCrosswalkMapper.class);
            ItemServiceImpl itemService = new DSpace().getSingletonService(ItemServiceImpl.class);
            String entityType = itemService.getEntityTypeLabel(item);
            final String crosswalkType = entityType.substring(0, 1).toLowerCase()
                                             + entityType.substring(1) + "-" + generator;
            StreamDisseminationCrosswalk crosswalk = crosswalkMapper.getByType(crosswalkType);
            if (crosswalk == null) {
                log.warn("No Crosswalk found with name " + crosswalkType);
            } else {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                crosswalk.disseminate(context, item, out);
                List<Element> elementList = metadata.getElement();
                elementList.add(ItemUtils.create("cerif"));
                Element cerif = ItemUtils.getElement(elementList, "cerif");
                if (Objects.isNull(cerif)) {
                    elementList.add(ItemUtils.create("cerif"));
                    cerif = ItemUtils.getElement(elementList, "cerif");
                }
                Element fieldname = ItemUtils.create(fieldName);
                cerif.getElement().add(fieldname);
                Element none = ItemUtils.create("none");
                fieldname.getElement().add(none);
                String xml_presentation = out.toString();
                // replace \n to avoid invalid element in the xml
                String toWrite = xml_presentation.replace("\n", "");
                none.getField().add(ItemUtils.createValue("value", xml_presentation));
                none.getField().add(ItemUtils.createValue("authority", ""));
                none.getField().add(ItemUtils.createValue("confidence", "-1"));
                return metadata;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            context.setCurrentUser(currentUser);
        }
        return metadata;
    }
}
