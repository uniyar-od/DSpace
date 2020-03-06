/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.webui.jsptag;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.jstl.fmt.LocaleSupport;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.dspace.app.webui.util.DateDisplayStrategy;
import org.dspace.app.webui.util.DefaultDisplayStrategy;
import org.dspace.app.webui.util.IDisplayMetadataValueStrategy;
import org.dspace.app.webui.util.LinkDisplayStrategy;
import org.dspace.app.webui.util.ResolverDisplayStrategy;
import org.dspace.app.webui.util.UIUtil;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.Item;
import org.dspace.content.Metadatum;
import org.dspace.content.authority.Choices;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.core.I18nUtil;
import org.dspace.core.PluginManager;
import org.dspace.discovery.DiscoverResult.DSpaceObjectHighlightResult;
import org.dspace.discovery.IGlobalSearchResult;
import org.dspace.discovery.configuration.DiscoveryMapConfiguration;
import org.dspace.discovery.configuration.DiscoveryViewConfiguration;
import org.dspace.discovery.configuration.DiscoveryViewFieldConfiguration;

/**
 * 
 */
public class MapArtifactTag extends BodyTagSupport {
	/** Artifact to display */
	private transient IGlobalSearchResult[] artifact;
	private transient DiscoveryMapConfiguration view;
	private transient String style = "";
	
	private transient List<String> contents;
	private transient List<String> labels;
	private transient List<String> locations;
	public MapArtifactTag() {
		super();
		
	}

	public int doStartTag() throws JspException {
		try {
			contents = new ArrayList<String>();
			labels = new ArrayList<String>();
			locations = new ArrayList<String>();
			JspWriter out = pageContext.getOut();
			for(IGlobalSearchResult obj:artifact) {
				String latLongField = view.getLatitudelongitude();
				String latitudeField = view.getLatitude();
				String longitudeField = view.getLongitude();
				List<String> latitude = new ArrayList<>();
				List<String> longitude= new ArrayList<>();
				if(StringUtils.isNotBlank(latLongField)){
					for(String value : obj.getMetadataValue(latLongField)){
						if(StringUtils.isNotBlank(value) && StringUtils.contains(value,",")){
							latitude.add( StringUtils.split(value,",")[0]);
							longitude.add( StringUtils.split(value,",")[1]);
						}
					}
					
				}else if(StringUtils.isNotBlank(latitudeField) && StringUtils.isNotBlank(longitudeField)){
					latitude = 	obj.getMetadataValue(latitudeField);
					longitude =	obj.getMetadataValue(latitudeField);
				}
				
				if(latitude.isEmpty() || longitude.isEmpty() || latitude.size()!=longitude.size()){
					continue;
				}
				
				for(int x=0; x<latitude.size();x++){
					String location =latitude.get(x) + ","+ longitude.get(x);
					locations.add("lat:"+latitude.get(x)+",lng:"+longitude.get(x));
					showPreview(obj,location);
				}
			}
			
		}catch (IOException | SQLException e) {
			throw new JspException(e);
		}
		return EVAL_BODY_BUFFERED;
	}

	@Override
	public int doEndTag() throws JspException {
		try {
		JspWriter out = pageContext.getOut();
		out.println("var locations = [" );
		for(String loc : locations) {
			out.println("{"+loc+"},");
		}
		out.println("];" );
		out.println("var contents = [" );
		for(String content : contents) {
			out.println("'"+content+"',");
		}
		out.println("];" );
		
		out.println("var labels = [" );
		for(String label : labels) {
			out.println("'"+label+"',");
		}
		out.println("];" );
		
		}catch (IOException e) {
			throw new JspException(e);
		}
		return EVAL_PAGE;
	}

	private void showPreview(IGlobalSearchResult iItem,String location) throws JspException, IOException, SQLException {
		JspWriter out = pageContext.getOut();
		HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
		Context context = UIUtil.obtainContext(request);
		boolean viewFull = false;
		
		String headings = "";
		String title = "";
		List<String> values =new ArrayList<String>();
		if (view != null) {
			for(DiscoveryViewFieldConfiguration dvfc: view.getMetadataHeadingFields()){
				String field = dvfc.getField();
				values = iItem.getMetadataValue(field);
				for(String val :values){
					title +=val + dvfc.getSeparator();
				}
				headings += printViewField(iItem, request, context, viewFull,dvfc);
			}
			String description = "";
			for(DiscoveryViewFieldConfiguration dvfc: view.getMetadataDescriptionFields()){
				String field = dvfc.getField();
				description += printViewField( iItem,request, context, false,dvfc);
			}
			labels.add(StringUtils.replace(title,"\'","\\\'"));
			contents.add(StringUtils.replace(headings,"\'","\\\'") + StringUtils.replace(description,"\'","\\\'"));
			
		}
	}

	private String printViewField( IGlobalSearchResult iItem,HttpServletRequest request,
			Context context, boolean viewFull,
			DiscoveryViewFieldConfiguration dvfc) throws JspException,
			IOException {
		String field = dvfc.getField();

		StringTokenizer dcf = new StringTokenizer(field, ".");

		String[] tokens = { "", "", "" };
		int i = 0;
		while (dcf.hasMoreTokens()) {
			tokens[i] = dcf.nextToken().trim();
			i++;
		}
		String schema = tokens[0];
		String element = tokens[1];
		String qualifier = tokens[2];

		String displayStrategyName = null;

		if (dvfc.getDecorator() != null) {
			displayStrategyName = dvfc.getDecorator();
		}

		String label = null;
		try {
			label = I18nUtil.getMessage("metadata." + ("default".equals(this.style) ? "" : this.style + ".")
					+ field, context);
		} catch (MissingResourceException e) {
			// if there is not a specific translation for the style we
			// use the default one
			label = LocaleSupport.getLocalizedMessage(pageContext, "metadata." + field);
		}

		boolean unescapeHtml = false;
		List<String> metadataValue = new ArrayList<String>();		
		List<Metadatum> dcMetadataValue = new ArrayList<Metadatum>();
		String metadata = "";
        Metadatum[] arrayDcMetadataValue = iItem
                .getMetadataValueInDCFormat(field);
        boolean found = false; 
		if (dvfc.getDecorator() != null) {
      
            if (arrayDcMetadataValue == null || arrayDcMetadataValue.length == 0) {
            	return "";
            }
			if (StringUtils.isNotBlank(displayStrategyName)) {
				found = true;
				IDisplayMetadataValueStrategy strategy = (IDisplayMetadataValueStrategy) PluginManager
						.getNamedPlugin(IDisplayMetadataValueStrategy.class, displayStrategyName);

				if (strategy == null) {
					if (displayStrategyName.equalsIgnoreCase("link")) {
						strategy = new LinkDisplayStrategy();
					} else if (displayStrategyName.equalsIgnoreCase("date")) {
						strategy = new DateDisplayStrategy();
					} else if (displayStrategyName.equalsIgnoreCase("resolver")) {
						strategy = new ResolverDisplayStrategy();
					} else {
						strategy = new DefaultDisplayStrategy();
					}
				}
				metadata = strategy.getMetadataDisplay(request, -1, viewFull,
                        "", 0, field,
                        arrayDcMetadataValue, iItem,
                        false, false);
			}
			
		}
		if(!found) {
            metadataValue = iItem.getMetadataValue(field);
			for (String vl : metadataValue) {
				metadata += vl;
				if (arrayDcMetadataValue.length > 1) {
					metadata += dvfc.getSeparator();
				}
			}
		}

		StringBuffer value= new StringBuffer();
		if (StringUtils.isNotBlank(metadata)) {
			if (StringUtils.isNotBlank(label)) {
				value.append("<span class=\"label label-default\">"+label +"</span> ");
			}
			metadata = unescapeHtml ? unescape(metadata) : metadata;
			value.append(dvfc.getPreHtml()).append( metadata ).append( dvfc.getPostHtml()) ;
		}
		return value.toString();
	}

	private String unescape(String input) {
		if (input == null)
			return null;
		String output = input;
		output = output.replaceAll("&#x09;", "\t");

		output = output.replaceAll("&#x0A;", "\n");
		output = output.replaceAll("&#x0C;", "\f");

		output = output.replaceAll("&#x0D;", "\r");

		// Chars that have a meaning for HTML
		output = output.replaceAll("&#39;", "'");

		output = output.replaceAll("&#x5C;", "\\\\");

		output = output.replaceAll("&#x20;", " ");

		output = output.replaceAll("&#x2F;", "/");

		output = output.replaceAll("&quot;", "\"");

		output = output.replaceAll("&lt;", "<");

		output = output.replaceAll("&gt;", ">");

		output = output.replaceAll("&amp;", "&");

		// Unicode new lines
		output = output.replaceAll("&#x2028;", "\u2028");
		output = output.replaceAll("&#x2029;", "\u2029");
		return output;
	}

	private void printDefault(JspWriter out, HttpServletRequest request, Context context, String browseIndex,
			boolean viewFull, String displayStrategyConf, String field,IGlobalSearchResult iItem) throws JspException, IOException {
		IDisplayMetadataValueStrategy strategy = (IDisplayMetadataValueStrategy) PluginManager.getNamedPlugin(
				IDisplayMetadataValueStrategy.class, displayStrategyConf);
		if (strategy == null) {
			strategy = new DefaultDisplayStrategy();
		}

		String metadata = strategy.getMetadataDisplay(request, -1, viewFull, browseIndex, 0, field,
				iItem.getMetadataValueInDCFormat(field), iItem, false, false);

		String label = null;
		try {
			label = I18nUtil.getMessage("metadata." + ("default".equals(this.style) ? "" : this.style + ".") + field,
					context);
		} catch (MissingResourceException e) {
			// if there is not a specific translation for the style we
			// use the default one
			label = LocaleSupport.getLocalizedMessage(pageContext, "metadata." + field);
		}
		if (StringUtils.isNotBlank(metadata)) {
			out.println(label + 
					 metadata );
		}
	}

	public void release() {
		artifact = null;
		view = null;
		contents = null;
		labels = null;
		locations = null;
	}

	public IGlobalSearchResult[] getArtifact() {
		return artifact;
	}

	public void setArtifact(IGlobalSearchResult[] artifact) {
		this.artifact = artifact;
	}


	public void setView(DiscoveryMapConfiguration view) {
		this.view = view;
	}


	public void setStyle(String style) {
		this.style = style;
	}

}