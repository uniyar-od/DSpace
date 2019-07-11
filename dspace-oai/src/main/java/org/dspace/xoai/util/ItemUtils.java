/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.xoai.util;

import com.lyncode.xoai.dataprovider.xml.xoai.Element;
import com.lyncode.xoai.dataprovider.xml.xoai.Metadata;
import com.lyncode.xoai.util.Base64Utils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.dspace.app.cris.integration.CRISAuthority;
import org.dspace.app.cris.model.ACrisObject;
import org.dspace.app.cris.service.ApplicationService;
import org.dspace.app.cris.util.MetadatumAuthorityDecorator;
import org.dspace.app.cris.util.UtilsCrisMetadata;
import org.dspace.app.util.MetadataExposure;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.Metadatum;
import org.dspace.content.Item;
import org.dspace.content.authority.ChoiceAuthority;
import org.dspace.content.authority.ChoiceAuthorityManager;
import org.dspace.content.authority.Choices;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.core.Utils;
import org.dspace.utils.DSpace;
import org.dspace.xoai.data.DSpaceItem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * 
 * @author Lyncode Development Team <dspace@lyncode.com>
 */
public class ItemUtils
{
    private static Logger log = LogManager
            .getLogger(ItemUtils.class);
    
    public static Integer MAX_DEEP = 2;

    private static Element getElement(List<Element> list, String name)
    {
        for (Element e : list)
            if (name.equals(e.getName()))
                return e;

        return null;
    }
    private static Element create(String name)
    {
        Element e = new Element();
        e.setName(name);
        return e;
    }

    private static Element.Field createValue(
            String name, String value)
    {
        Element.Field e = new Element.Field();
        e.setValue(value);
        e.setName(name);
        return e;
    }
    
    private static Element writeMetadata(Element  schema,Metadatum val) {
    	
        Element valueElem = null;
        valueElem = schema;

        // Has element.. with XOAI one could have only schema and value
        if (val.element != null && !val.element.equals(""))
        {
            Element element = getElement(schema.getElement(),
                    val.element);
            if (element == null)
            {
                element = create(val.element);
                schema.getElement().add(element);
            }
            valueElem = element;

            // Qualified element?
            if (val.qualifier != null && !val.qualifier.equals(""))
            {
                Element qualifier = getElement(element.getElement(),
                        val.qualifier);
                if (qualifier == null)
                {
                    qualifier = create(val.qualifier);
                    element.getElement().add(qualifier);
                }
                valueElem = qualifier;
            }
        }

        // Language?
        if (val.language != null && !val.language.equals(""))
        {
            Element language = getElement(valueElem.getElement(),
                    val.language);
            if (language == null)
            {
                language = create(val.language);
                valueElem.getElement().add(language);
            }
            valueElem = language;
        }
        else
        {
            Element language = getElement(valueElem.getElement(),
                    "none");
            if (language == null)
            {
                language = create("none");
                valueElem.getElement().add(language);
            }
            valueElem = language;
        }

        valueElem.getField().add(createValue("value", val.value));
        if (val.authority != null) {
            valueElem.getField().add(createValue("authority", val.authority));
            if (val.confidence != Choices.CF_NOVALUE)
                valueElem.getField().add(createValue("confidence", val.confidence + ""));
        }
        return valueElem;

    }
    public static Metadata retrieveMetadata (Context context, Item item) {
    	return retrieveMetadata(context, item, false);
    }
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static Metadata retrieveMetadata (Context context, Item item, boolean skipAutority) {
        Metadata metadata;
        
        // read all metadata into Metadata Object
        metadata = new Metadata();
        
        Metadatum[] vals = item.getMetadata(Item.ANY, Item.ANY, Item.ANY, Item.ANY);
        for (Metadatum val : vals)
        {
            // Don't expose fields that are hidden by configuration
            try {
                if (MetadataExposure.isHidden(context,
                        val.schema,
                        val.element,
                        val.qualifier))
                {
                    continue;
                }
            } catch(SQLException se) {
                throw new RuntimeException(se);
            }

            Element schema = getElement(metadata.getElement(), val.schema);
            if (schema == null)
            {
                schema = create(val.schema);
                metadata.getElement().add(schema);
            }
            Element element = writeMetadata(schema, val);
            metadata.getElement().add(element);
            
            if (!skipAutority && val.authority != null) {
            	String m = val.schema + "." + val.element;
            	
                if (val.qualifier != null && !val.qualifier.equals("")) {
                	m += "." + val.qualifier;
                }

                // add metadata of related cris object, using authority to get it
                boolean metadataAuth = ConfigurationManager.getBooleanProperty("oai", "oai.authority." + m);
                if (metadataAuth) {
                	try {
                		ChoiceAuthorityManager choicheAuthManager = ChoiceAuthorityManager.getManager();
                		ChoiceAuthority choicheAuth = choicheAuthManager.getChoiceAuthority(m);
                		if (choicheAuth != null && choicheAuth instanceof CRISAuthority) {
							CRISAuthority crisAuthoriy = (CRISAuthority) choicheAuth;
							ACrisObject cris = getApplicationService().getEntityByCrisId(val.authority, crisAuthoriy.getCRISTargetClass());
                			
                			Metadata crisMetadata = retrieveMetadata(context, cris, skipAutority, 0);
                			if (crisMetadata != null && !crisMetadata.getElement().isEmpty()) {
                				Element root = create(m);
                				metadata.getElement().add(root);
                				
                				for (Element crisElement : crisMetadata.getElement()) {
                					root.getElement().add(crisElement);
                				}
                			}
                		} else {
                			log.warn("No choices plugin (CRISAuthority plugin) was configured for field " + m);
                		}
            		} catch (Exception e) {
            			log.error("Error during retrieving choices plugin (CRISAuthority plugin) for field " + m + ". " + e.getMessage(), e);
            		}
                }
            }
        }

        // Done! Metadata has been read!
        // Now adding bitstream info
        Element bundles = create("bundles");
        metadata.getElement().add(bundles);

        Bundle[] bs;
        try
        {
            bs = item.getBundles();
            for (Bundle b : bs)
            {
                Element bundle = create("bundle");
                bundles.getElement().add(bundle);
                bundle.getField()
                        .add(createValue("name", b.getName()));

                Element bitstreams = create("bitstreams");
                bundle.getElement().add(bitstreams);
                Bitstream[] bits = b.getBitstreams();
                for (Bitstream bit : bits)
                {
                    Element bitstream = create("bitstream");
                    bitstreams.getElement().add(bitstream);
                    
                    Metadatum[] bVals = bit.getMetadata(Item.ANY, Item.ANY, Item.ANY, Item.ANY);
                    for(Metadatum bVal : bVals) {
                        Element bSchema = getElement(bitstream.getElement(), bVal.schema);
                        if (bSchema == null)
                        {
                            bSchema = create(bVal.schema);
                            bitstream.getElement().add(bSchema);
                        }
                    	Element bElement = writeMetadata(bSchema, bVal);
                        bitstream.getElement().add(bElement);
                    }
                    
                    String url = "";
                    String bsName = bit.getName();
                    String bitID= Integer.toString(bit.getID());
                    String sid = String.valueOf(bit.getSequenceID());
                    String baseUrl = ConfigurationManager.getProperty("oai",
                            "bitstream.baseUrl");
                    String handle = null;
                    // get handle of parent Item of this bitstream, if there
                    // is one:
                    Bundle[] bn = bit.getBundles();
                    if (bn.length > 0)
                    {
                        Item bi[] = bn[0].getItems();
                        if (bi.length > 0)
                        {
                            handle = bi[0].getHandle();
                        }
                    }
                    if (bsName == null)
                    {
                        String ext[] = bit.getFormat().getExtensions();
                        bsName = "bitstream_" + sid
                                + (ext.length > 0 ? ext[0] : "");
                    }
                    if (handle != null && baseUrl != null)
                    {
                        url = baseUrl + "/bitstream/"
                                + handle + "/"
                                + sid + "/"
                                + URLUtils.encode(bsName);
                    }
                    else
                    {
                        url = URLUtils.encode(bsName);
                    }

                    String cks = bit.getChecksum();
                    String cka = bit.getChecksumAlgorithm();
                    String oname = bit.getSource();
                    String name = bit.getName();
                    String description = bit.getDescription();

                    bitstream.getField().add(createValue("id", bitID));
                    if (name != null)
                        bitstream.getField().add(
                                createValue("name", name));
                    if (oname != null)
                        bitstream.getField().add(
                                createValue("originalName", name));
                    if (description != null)
                        bitstream.getField().add(
                                createValue("description", description));
                    bitstream.getField().add(
                            createValue("format", bit.getFormat()
                                    .getMIMEType()));
                    bitstream.getField().add(
                            createValue("size", "" + bit.getSize()));
                    bitstream.getField().add(createValue("url", url));
                    bitstream.getField().add(
                            createValue("checksum", cks));
                    bitstream.getField().add(
                            createValue("checksumAlgorithm", cka));
                    bitstream.getField().add(
                            createValue("sid", bit.getSequenceID()
                                    + ""));
                }
            }
        }
        catch (SQLException e1)
        {
            e1.printStackTrace();
        }
        

        // Other info
        Element other = create("others");

        other.getField().add(
                createValue("handle", item.getHandle()));
        other.getField().add(
                createValue("identifier", DSpaceItem.buildIdentifier(item.getHandle())));
        other.getField().add(
                createValue("lastModifyDate", item
                        .getLastModified().toString()));
        other.getField().add(
                createValue("type", "item"));
        metadata.getElement().add(other);

        // Repository Info
        Element repository = create("repository");
        repository.getField().add(
                createValue("name",
                        ConfigurationManager.getProperty("dspace.name")));
        repository.getField().add(
                createValue("mail",
                        ConfigurationManager.getProperty("mail.admin")));
        metadata.getElement().add(repository);

        // Licensing info
        Element license = create("license");
        Bundle[] licBundles;
        try
        {
            licBundles = item.getBundles(Constants.LICENSE_BUNDLE_NAME);
            if (licBundles.length > 0)
            {
                Bundle licBundle = licBundles[0];
                Bitstream[] licBits = licBundle.getBitstreams();
                if (licBits.length > 0)
                {
                    Bitstream licBit = licBits[0];
                    InputStream in;
                    try
                    {
                        in = licBit.retrieve();
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        Utils.bufferedCopy(in, out);
                        license.getField().add(
                                createValue("bin",
                                        Base64Utils.encode(out.toString())));
                        metadata.getElement().add(license);
                    }
                    catch (AuthorizeException e)
                    {
                        log.warn(e.getMessage(), e);
                    }
                    catch (IOException e)
                    {
                        log.warn(e.getMessage(), e);
                    }
                    catch (SQLException e)
                    {
                        log.warn(e.getMessage(), e);
                    }

                }
            }
        }
        catch (SQLException e1)
        {
            log.warn(e1.getMessage(), e1);
        }
        
        return metadata;
    }
    
    /***
     * Retrieve all metadata
     * 
     * @param context The context
     * @param item The cris item
     * @return
     */
    @SuppressWarnings("rawtypes")
	public static Metadata retrieveMetadata (Context context, ACrisObject item) {
    	return retrieveMetadata(context, item, false, 0);
    }
    
    /***
     * Retrieve all metadata
     * 
     * @param context The context
     * @param item The cris item
     * @param skipAutority Usually set to false.
     * @param deep 
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static Metadata retrieveMetadata (Context context, ACrisObject item, boolean skipAutority, int deep) {
        Metadata metadata;
        
        // read all metadata into Metadata Object
        metadata = new Metadata();
        
        MetadatumAuthorityDecorator[] vals = UtilsCrisMetadata.getAllMetadata(item, true, true, "oai");
        if (vals != null)
        {
            for (MetadatumAuthorityDecorator valAuthDec : vals)
            {
            	Metadatum val = valAuthDec.getMetadatum();
                Element schema = getElement(metadata.getElement(), val.schema);
                if (schema == null)
                {
                    schema = create(val.schema);
                    metadata.getElement().add(schema);
                }
                Element element = writeMetadata(schema, val);
                metadata.getElement().add(element);
                
                // follow relations:
                if (!skipAutority && val.authority != null) {
                	String m = val.schema + "." + val.element;
                	
                    if (val.qualifier != null && !val.qualifier.equals("")) {
                    	m += "." + val.qualifier;
                    }

                    int authorityDeep = ConfigurationManager.getIntProperty("oai", "oai.authority." + m + ".deep");
                    if (authorityDeep <= 0) {
                    	authorityDeep = ConfigurationManager.getIntProperty("oai", "oai.authority.deep");
                    	
                    	if (authorityDeep <= 0)
                    		authorityDeep = MAX_DEEP;
                    }
                    
                    // add metadata of related cris object, using authority to get it
                    boolean metadataAuth = ConfigurationManager.getBooleanProperty("oai", "oai.authority." + m);
	                if (metadataAuth && (deep < authorityDeep) && (!valAuthDec.isClassNameNull() || !valAuthDec.isClassNameNull(val.authority))) {
	                	try {
	                		ACrisObject cris = null;
	                		
	                		if (!valAuthDec.isClassNameNull())
	                			cris = getApplicationService().getEntityByCrisId(val.authority, valAuthDec.className());
	                		else
	                			cris = getApplicationService().getEntityByCrisId(val.authority, valAuthDec.className(val.authority));
	                			
                			Metadata crisMetadata = retrieveMetadata(context, cris, skipAutority, deep + 1);
                			if (crisMetadata != null && !crisMetadata.getElement().isEmpty()) {
                				Element root = create(m);
                				metadata.getElement().add(root);
                				
                				for (Element crisElement : crisMetadata.getElement()) {
                					root.getElement().add(crisElement);
                				}
                			}
	            		} catch (Exception e) {
	            			log.error("Error during retrieving choices plugin (CRISAuthority plugin) for field " + m + ". " + e.getMessage(), e);
	            		}
	                }
                }
            }
        }

        // Other info
        Element other = create("others");

        other.getField().add(
                createValue("handle", item.getHandle()));
        other.getField().add(
                createValue("identifier", DSpaceItem.buildIdentifier(item.getHandle())));
        Date m = new Date(item
                .getTimeStampInfo().getLastModificationTime().getTime());
        other.getField().add(
                createValue("lastModifyDate", m.toString()));
        other.getField().add(
                createValue("type", item.getPublicPath()));
        metadata.getElement().add(other);

        // Repository Info
        Element repository = create("repository");
        repository.getField().add(
                createValue("name",
                        ConfigurationManager.getProperty("dspace.name")));
        repository.getField().add(
                createValue("mail",
                        ConfigurationManager.getProperty("mail.admin")));
        metadata.getElement().add(repository);
        
        return metadata;
    }
    
    /***
     * Cris application service
     * @return
     */
    private static ApplicationService getApplicationService()
    {
    	return new DSpace().getServiceManager().getServiceByName(
    			"applicationService", ApplicationService.class);
    }
}
