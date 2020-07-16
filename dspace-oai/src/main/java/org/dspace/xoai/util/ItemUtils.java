/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.xoai.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import com.lyncode.xoai.dataprovider.xml.xoai.Element;
import com.lyncode.xoai.dataprovider.xml.xoai.Metadata;
import com.lyncode.xoai.util.Base64Utils;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.dspace.app.cris.integration.CRISAuthority;
import org.dspace.app.cris.model.ACrisObject;
import org.dspace.app.cris.service.ApplicationService;
import org.dspace.app.cris.util.MetadatumAuthorityDecorator;
import org.dspace.app.cris.util.UtilsCrisMetadata;
import org.dspace.app.util.factory.UtilServiceFactory;
import org.dspace.app.util.service.MetadataExposureService;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.ResourcePolicy;
import org.dspace.authorize.factory.AuthorizeServiceFactory;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.authorize.service.ResourcePolicyService;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.DSpaceObject;
import org.dspace.content.IMetadataValue;
import org.dspace.content.Item;
import org.dspace.content.authority.ChoiceAuthority;
import org.dspace.content.authority.Choices;
import org.dspace.content.authority.factory.ContentAuthorityServiceFactory;
import org.dspace.content.authority.service.ChoiceAuthorityService;
import org.dspace.content.authority.service.MetadataAuthorityService;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.BitstreamService;
import org.dspace.content.service.ItemService;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.core.Utils;
import org.dspace.eperson.Group;
import org.dspace.handle.factory.HandleServiceFactory;
import org.dspace.handle.service.HandleService;
import org.dspace.utils.DSpace;
import org.dspace.xoai.app.XOAI;
import org.dspace.xoai.data.DSpaceItem;

import java.text.SimpleDateFormat;

/**
 * 
 * @author Lyncode Development Team <dspace@lyncode.com>
 */
@SuppressWarnings("deprecation")
public class ItemUtils
{
    private static final Logger log = LogManager.getLogger(ItemUtils.class);

    public static final String RESTRICTED_ACCESS = "restricted access";

    public static final String EMBARGOED_ACCESS = "embargoed access";

    public static final String OPEN_ACCESS = "open access";

    public static final String METADATA_ONLY_ACCESS = "metadata only access";

    private static final MetadataExposureService metadataExposureService
            = UtilServiceFactory.getInstance().getMetadataExposureService();

    private static final ItemService itemService
            = ContentServiceFactory.getInstance().getItemService();

    private static final BitstreamService bitstreamService
            = ContentServiceFactory.getInstance().getBitstreamService();
    
    private static final MetadataAuthorityService mam = ContentAuthorityServiceFactory
            .getInstance().getMetadataAuthorityService();    
    
    private static final ChoiceAuthorityService choicheAuthManager = ContentAuthorityServiceFactory
            .getInstance().getChoiceAuthorityService();
    
    private static final HandleService handleService = HandleServiceFactory
            .getInstance().getHandleService();

    private static final AuthorizeService authorizeService = AuthorizeServiceFactory
            .getInstance().getAuthorizeService();

    private static final ResourcePolicyService resourcePolicyService = AuthorizeServiceFactory.getInstance().getResourcePolicyService();

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    public static Integer MAX_DEEP = 2;
    public static String AUTHORITY = "authority";

    public static Element getElement(List<Element> list, String name)
    {
        for (Element e : list)
            if (name.equals(e.getName()))
                return e;

        return null;
    }
    public static Element create(String name)
    {
        Element e = new Element();
        e.setName(name);
        return e;
    }

    public static Element.Field createValue(
            String name, String value)
    {
        Element.Field e = new Element.Field();
        e.setValue(value);
        e.setName(name);
        return e;
    }
    
    private static Element.Field getField(List<Element.Field> list, String name)
    {
        for (Element.Field f : list)
            if (name.equals(f.getName()))
                return f;

        return null;
    }
    
    /***
     * Write metadata into a Element structure.
     * 
     * @param schema The reference schema
     * @param val The metadata value
     * @return
     */
    private static Element writeMetadata(Element  schema,IMetadataValue val) {
        return writeMetadata(schema, val, false);
    }
    
    /***
     * Write metadata into a Element structure.
     * 
     * @param schema The reference schema
     * @param val The metadata value
     * @param forceEmptyQualifier Set to true to create a qualifier element
     *              with value "none" when qualifier is empty. Otherwise the qualifier element is not created.
     * @return
     */
    private static Element writeMetadata(Element  schema,IMetadataValue val, boolean forceEmptyQualifier) {
        
        Element valueElem = null;
        valueElem = schema;

        // Has element.. with XOAI one could have only schema and value
        if (val.getElement() != null && !val.getElement().equals(""))
        {
            Element element = getElement(schema.getElement(),
                    val.getElement());
            if (element == null)
            {
                element = create(val.getElement());
                schema.getElement().add(element);
            }
            valueElem = element;

            // Qualified element?
            if (val.getQualifier() != null && !val.getQualifier().equals(""))
            {
                Element qualifier = getElement(element.getElement(),
                        val.getQualifier());
                if (qualifier == null)
                {
                    qualifier = create(val.getQualifier());
                    element.getElement().add(qualifier);
                }
                valueElem = qualifier;
            } else if (forceEmptyQualifier) {
                Element qualifier = getElement(element.getElement(),
                        "none");
                //if (qualifier == null)
                {
                    qualifier = create("none");
                    element.getElement().add(qualifier);
                }
                valueElem = qualifier;
            }
        }
        Element qualifier = valueElem;
        
        // Language?
        if (val.getLanguage() != null && !val.getLanguage().equals(""))
        {
            Element language = getElement(valueElem.getElement(),
                    val.getLanguage());
            // remove single language
            //if (language == null)
            {
                language = create(val.getLanguage());
                valueElem.getElement().add(language);
            }
            valueElem = language;
        }
        else
        {
            Element language = getElement(valueElem.getElement(),
                    "none");
            // remove single language
            //if (language == null)
            {
                language = create("none");
                valueElem.getElement().add(language);
            }
            valueElem = language;
        }
        
        valueElem.getField().add(createValue("value", val.getValue()));
        if (val.getAuthority() != null) {
            valueElem.getField().add(createValue("authority", val.getAuthority()));
            if (val.getConfidence() != Choices.CF_NOVALUE)
                valueElem.getField().add(createValue("confidence", val.getConfidence() + ""));
        }
        return valueElem;

    }

    public static Metadata retrieveMetadata (Context context, Item item, boolean specialIdentifier) {
    	return retrieveMetadata(context, item, false, 0, specialIdentifier);
    }
    
    /***
     * Retrieve all metadata in a XML fragment.
     * 
     * @param context The context
     * @param item The cris item
     * @param skipAutority is used to disable relation metadata inclusion.
     * @param deep the recursive dept
     * @param specialIdentifier TODO
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static Metadata retrieveMetadata (Context context, Item item, boolean skipAutority, int deep, boolean specialIdentifier) {
        Metadata metadata;
        
        // read all metadata into Metadata Object
        metadata = new Metadata();
        
        List<IMetadataValue> vals = item.getMetadata(Item.ANY, Item.ANY, Item.ANY, Item.ANY);
        for (IMetadataValue val : vals)
        {
            // Don't expose fields that are hidden by configuration
            try {
                if (metadataExposureService.isHidden(context,
                        val.getSchema(),
                        val.getElement(),
                        val.getQualifier()))
                {
                    continue;
                }
            } catch(SQLException se) {
                throw new RuntimeException(se);
            }

            // mapping metadata in index only            
            Element schema = getElement(metadata.getElement(), val.getSchema());

            if (schema == null)
            {
                schema = create(val.getSchema());
                metadata.getElement().add(schema);
            }
            Element element = writeMetadata(schema, val);
            // backward compatibility
            {
                //metadata.getElement().add(element);
                Element elementCopy = create(element.getName());
                for (Element.Field f : element.getField()) {
                    elementCopy.getField().add(f);
                }
                metadata.getElement().add(elementCopy);
            }

            if (!skipAutority && StringUtils.isNotBlank(val.getAuthority())) {
                String m = Utils.standardize(val.getSchema(), val.getElement(), val.getQualifier(), ".");
                // compute deep
                int authorityDeep = ConfigurationManager.getIntProperty("oai", "oai.authority." + m + ".deep");
                if (authorityDeep <= 0) {
                    authorityDeep = ConfigurationManager.getIntProperty("oai", "oai.authority.deep");
                    
                    if (authorityDeep <= 0)
                        authorityDeep = MAX_DEEP;
                }
                
                // add metadata of related cris object, using authority to get it                
                boolean metadataAuth = mam.isAuthorityControlled(val.getMetadataField());
                if (metadataAuth && (deep < authorityDeep)) {
                	try {
                		ChoiceAuthority choicheAuth = choicheAuthManager.getChoiceAuthority(m);
                		if (choicheAuth != null && choicheAuth instanceof CRISAuthority) {
							CRISAuthority crisAuthoriy = (CRISAuthority) choicheAuth;
							ACrisObject o = getApplicationService().getEntityByCrisId(val.getAuthority(), crisAuthoriy.getCRISTargetClass());
							Metadata crisMetadata = null;
                			if (o != null) 
                			{								
                				crisMetadata = retrieveMetadata(context, o, skipAutority, /*m, ((ACrisObject) o).getUuid(), Integer.toString(item.getID()),*/ 0);
							}else if (log.isDebugEnabled()) 
							{
								log.debug("WARNING: the item with id \"" + item.getID() + "\" contains the authority value \"" + val.getAuthority() + "\"  NOT IN the database - field " + val.getSchema() + "."+ val.getElement() + "."+ val.getQualifier());
							}
                			if (crisMetadata != null && !crisMetadata.getElement().isEmpty()) {
                				Element root = create(AUTHORITY);
                				element.getElement().add(root);
                				for (Element crisElement : crisMetadata.getElement()) {
                					root.getElement().add(crisElement);
                				}
                			}
                		} else if (choicheAuth != null) {
                			DSpaceObject dso = handleService.resolveToObject(context, val.getAuthority());
                			
                			if (dso != null && dso instanceof Item) {
                				Metadata itemMetadata = retrieveMetadata(context, (Item)dso, skipAutority, deep + 1, specialIdentifier);
                				if (itemMetadata != null && !itemMetadata.getElement().isEmpty()) {
                					Element root = create(AUTHORITY);
                    				element.getElement().add(root);
                    				for (Element crisElement : itemMetadata.getElement()) {
                    					root.getElement().add(crisElement);
                    				}
                				}
                			}
                		}
                		else {
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

        List<Bundle> bs;
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
                List<Bitstream> bits = b.getBitstreams();
                for (Bitstream bit : bits)
                {
                    Element bitstream = create("bitstream");
                    bitstreams.getElement().add(bitstream);
                    
                    List<IMetadataValue> bVals = bit.getMetadata(Item.ANY, Item.ANY, Item.ANY, Item.ANY);
                    for(IMetadataValue bVal : bVals) {
                        Element bSchema = getElement(bitstream.getElement(), bVal.getSchema());
                        if (bSchema == null)
                        {
                            bSchema = create(bVal.getSchema());
                            bitstream.getElement().add(bSchema);
                        }
                    	Element bElement = writeMetadata(bSchema, bVal);
                        bitstream.getElement().add(bElement);
                    }
                    
                    String url = "";
                    String bsName = bit.getName();
                    String bitID= bit.getID().toString();
                    String sid = String.valueOf(bit.getSequenceID());
                    String baseUrl = ConfigurationManager.getProperty("oai",
                            "bitstream.baseUrl");
                    String handle = null;
                    // get handle of parent Item of this bitstream, if there
                    // is one:
                    List<Bundle> bn = bit.getBundles();
                    if (!bn.isEmpty())
                    {
                        List<Item> bi = bn.get(0).getItems();
                        if (!bi.isEmpty())
                        {
                            handle = bi.get(0).getHandle();
                        }
                    }
                    if (bsName == null)
                    {
                        List<String> ext = bit.getFormat(context).getExtensions();
                        bsName = "bitstream_" + sid
                                + (ext.isEmpty() ? "" : ext.get(0));
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

                    String drm = ItemUtils.getAccessRightsValue(context, authorizeService.getPoliciesActionFilter(context, bit,  Constants.READ));

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
                            createValue("format", bit.getFormat(context)
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
                    bitstream.getField().add(
                            createValue("drm", drm));
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
        
        String type = (String)item.getExtraInfo().get("item.cerifentitytype");
        if(StringUtils.isNotBlank(type) && specialIdentifier) {
            other.getField().add(
                    createValue("identifier", DSpaceItem.buildIdentifier(item.getHandle(), type)));
            other.getField().add(
                    createValue("type", XOAI.ITEMTYPE_SPECIAL));
        }
        else {
            other.getField().add(
                    createValue("identifier", DSpaceItem.buildIdentifier(item.getHandle(), null)));
            other.getField().add(
                    createValue("type", XOAI.ITEMTYPE_DEFAULT));
        }
        other.getField().add(
                createValue("lastModifyDate", item
                        .getLastModified().toString()));
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
        boolean exposeLicense = ConfigurationManager.getBooleanProperty("oai", "license.expose", true);
        if(exposeLicense) {
	        Element license = create("license");
	        List<Bundle> licBundles;
	        try
	        {
	            licBundles = itemService.getBundles(item, Constants.LICENSE_BUNDLE_NAME);
	            if (!licBundles.isEmpty())
	            {
	                Bundle licBundle = licBundles.get(0);
	                List<Bitstream> licBits = licBundle.getBitstreams();
	                if (!licBits.isEmpty())
	                {
	                    Bitstream licBit = licBits.get(0);
	                    InputStream in;
	                    try
	                    {
	                        in = bitstreamService.retrieve(context, licBit);
	                        ByteArrayOutputStream out = new ByteArrayOutputStream();
	                        Utils.bufferedCopy(in, out);
	                        license.getField().add(
	                                createValue("bin",
	                                        Base64Utils.encode(out.toString())));
	                        metadata.getElement().add(license);
	                    }
	                    catch (AuthorizeException | IOException | SQLException e)
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
        }        
        return metadata;
    }
    
    @SuppressWarnings("rawtypes")
	public static Metadata retrieveMetadata (Context context, ACrisObject item) {
    	return retrieveMetadata(context, item, false, 0);
    }
    
    /***
     * Retrieve all metadata in a XML fragment.
     * 
     * @param context The context
     * @param item The cris item
     * @param deep 
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static Metadata retrieveMetadata (Context context, ACrisObject item, boolean skipAutority, int deep) {
        Metadata metadata;
        
        // read all metadata into Metadata Object
        metadata = new Metadata();
        
        if(!item.getStatus()) {
            Element schema = create("cris" + item.getAuthorityPrefix());
            metadata.getElement().add(schema);
            Element element = create(item.getMetadataFieldName(null));
            element.getElement().add(element);
            Element qualifier = create("none");
            element.getElement().add(qualifier);
            Element lang = create("none");
            element.getElement().add(lang);
            element.getField().add(createValue("value", item.getName()));
        }
        else {
    		MetadatumAuthorityDecorator[] vals = UtilsCrisMetadata.getAllMetadata(item, true, true, "oai");
            if (vals != null)
            {
                for (MetadatumAuthorityDecorator valAuthDec : vals)
                {
                    IMetadataValue val = valAuthDec.getMetadatum();
    
                	// mapping metadata in index only
                    Element schema = getElement(metadata.getElement(), val.getSchema());
                    
                    if (schema == null)
                    {
                        schema = create(val.getSchema());
                        metadata.getElement().add(schema);
                    }
                    Element element = writeMetadata(schema, val, true);
                    //metadata.getElement().add(element);
                    
                    // create relation (use full metadata value as relation name)
                    if (!skipAutority && StringUtils.isNotBlank(val.getAuthority())) {
                        String m = Utils.standardize(val.getSchema(), val.getElement(), val.getQualifier(), ".");
                        
                        // compute deep
                        int authorityDeep = ConfigurationManager.getIntProperty("oai", "oai.authority." + m + ".deep");
                        if (authorityDeep <= 0) {
                        	authorityDeep = ConfigurationManager.getIntProperty("oai", "oai.authority.deep");
                        	
                        	if (authorityDeep <= 0)
                        		authorityDeep = MAX_DEEP;
                        }
                        
                        // add metadata of related cris object, using authority to get it
    	                if (valAuthDec.isPointer() && (deep < authorityDeep)) {
    	                	try {
    	                		ACrisObject o = getApplicationService().getEntityByCrisId(val.getAuthority(), valAuthDec.getClassname());
    	                			
                    			Metadata crisMetadata = retrieveMetadata(context, o, skipAutority, deep + 1);
                    			if (crisMetadata != null && !crisMetadata.getElement().isEmpty()) {
                    				Element root = create(AUTHORITY);
                    				element.getElement().add(root);
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
            
            String type = ConfigurationManager.getProperty("oai", "identifier.cerifentitytype." + item.getPublicPath());
            other.getField().add(
                    createValue("identifier", DSpaceItem.buildIdentifier(item.getHandle(), type)));
            
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
        }
        
        return metadata;
    }

	/**
	 * Method to return a default value text to identify access rights:
	 * 'open access','embargoed access','restricted access','metadata only access'
	 *
	 * NOTE: embargoed access contains also embargo end date in the form "embargoed access|||yyyy-MM-dd"
	 *
	 * @param rps
	 * @return
	 */
	public static String getAccessRightsValue(Context context, List<ResourcePolicy> rps)
			throws SQLException {
		Date now = new Date();
		Date embargoEndDate = null;
		boolean openAccess = false;
		boolean groupRestricted = false;
		boolean withEmbargo = false;

		if (rps != null) {
			for (ResourcePolicy rp : rps) {
				if (rp.getGroup() != null && Group.ANONYMOUS.equals(rp.getGroup().getName())) {
					if (resourcePolicyService.isDateValid(rp)) {
						openAccess = true;
					} else if (rp.getStartDate() != null && rp.getStartDate().after(now)) {
						withEmbargo = true;
						embargoEndDate = rp.getStartDate();
					}
				} else if (rp.getGroup() != null && !Group.ADMIN.equals(rp.getGroup().getName())) {
					if (resourcePolicyService.isDateValid(rp)) {
						groupRestricted = true;
					} else if (rp.getStartDate() == null || rp.getStartDate().after(now)) {
						withEmbargo = true;
						embargoEndDate = rp.getStartDate();
					}
				}
				context.uncacheEntity(rp);
			}
		}
		String value = METADATA_ONLY_ACCESS;
		// if there are fulltext build the values
		if (openAccess) {
			// open access
			value = OPEN_ACCESS;
		} else if (withEmbargo) {
			// all embargoed
			value = EMBARGOED_ACCESS + "|||" + sdf.format(embargoEndDate);
		} else if (groupRestricted) {
			// all restricted
			value = RESTRICTED_ACCESS;
		}
		return value;
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
