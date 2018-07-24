/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.converter;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.dspace.app.rest.model.MetadataEntryRest;
import org.dspace.app.util.factory.UtilServiceFactory;
import org.dspace.content.DSpaceObject;
import org.dspace.content.IMetadataValue;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.core.Context.Mode;

/**
 * This is the base converter from/to objects in the DSpace API data model and
 * the REST data model
 *
 * @param <M> the Class in the DSpace API data model
 * @param <R> the Class in the DSpace REST data model
 * @author Andrea Bollini (andrea.bollini at 4science.it)
 */
public abstract class DSpaceObjectConverter<M extends DSpaceObject, R extends org.dspace.app.rest.model
    .DSpaceObjectRest>
    extends DSpaceConverter<M, R> implements BrowsableDSpaceObjectConverter<M, R> {

    private static final Logger log = Logger.getLogger(DSpaceObjectConverter.class);

    @Override
    public R fromModel(M obj) {
        R resource = newInstance();
        Context context = null;
        try {
            context = new Context(Mode.READ_ONLY);
            resource.setHandle(obj.getHandle());
            if (obj.getID() != null) {
                resource.setUuid(obj.getID().toString());
            }
            resource.setName(obj.getName());
            List<MetadataEntryRest> metadata = new ArrayList<MetadataEntryRest>();
            List<IMetadataValue> values = obj.getMetadataWithoutPlaceholder(Item.ANY, Item.ANY, Item.ANY, Item.ANY);
            for (IMetadataValue mv : values) {

                // check for hidden field, even if it's configured..
                if (UtilServiceFactory.getInstance().getMetadataExposureService()
                        .isHidden(context, mv.getSchema(), mv.getElement(), mv.getQualifier())) {
                    continue;
                }

                MetadataEntryRest me = new MetadataEntryRest();
                me.setKey(mv.getMetadataField().toString('.'));
                me.setValue(mv.getValue());
                me.setLanguage(mv.getLanguage());
                me.setAuthority(mv.getAuthority());
                me.setConfidence(mv.getConfidence());
                metadata.add(me);
            }
            resource.setMetadata(metadata);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        return resource;
    }

    @Override
    public M toModel(R obj) {
        return null;
    }

    public boolean supportsModel(Object object) {
        return object != null && object.getClass().equals(getModelClass());
    }

    protected abstract R newInstance();

    protected abstract Class<M> getModelClass();

}