/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.webui.util;

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;
import org.dspace.content.Bitstream;
import org.dspace.content.BitstreamFormat;
import org.dspace.content.Bundle;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.I18nUtil;
import org.dspace.core.Utils;

public class EditItemUtil {
	private static boolean collapsedByDefaultBundles = ConfigurationManager.getBooleanProperty("edit.admin.collapsed.bundle.default",false);

	
	public static String generateHiddenHTMLTable(String contextPath, Bundle[] bundles, int index, boolean breOrderBitstreams, boolean bRemoveBits) {
		String row="even";
		
		StringBuilder builder=new StringBuilder();
		if(collapsedByDefaultBundles) {
			builder.append("<div id=\"hidden-div-"+bundles[index].getID()+"\" >");
		}
		builder.append("<table id=\"bitstream-edit-form-table-"+index+"\" class=\"table\" summary=\"Bitstream data table\">");
		builder.append("<tr>");
		builder.append("<th id=\"t10\" class=\"oddRowEvenCol\">&nbsp;</th>");
		builder.append("<th id=\"t11\" class=\"oddRowOddCol\"><strong>"+I18nUtil.getMessage("jsp.tools.edit-item-form.elem5")+"</strong></th>");
		builder.append("<th id=\"t12\" class=\"oddRowEvenCol\"><strong>"+I18nUtil.getMessage("jsp.tools.edit-item-form.elem7")+"</strong></th>");
		builder.append("<th id=\"t13\" class=\"oddRowOddCol\"><strong>"+I18nUtil.getMessage("jsp.tools.edit-item-form.elem8")+"</strong></th>");
		builder.append("<th id=\"t14\" class=\"oddRowEvenCol\"><strong>"+I18nUtil.getMessage("jsp.tools.edit-item-form.elem9")+"</strong></th>");
		builder.append("<th id=\"t15\" class=\"oddRowOddCol\"><strong>"+I18nUtil.getMessage("jsp.tools.edit-item-form.elem10")+"</strong></th>");
		builder.append("<th id=\"t16\" class=\"oddRowEvenCol\"><strong>"+I18nUtil.getMessage("jsp.tools.edit-item-form.elem11")+"</strong></th>");
		builder.append("<th id=\"t17\" class=\"oddRowOddCol\"><strong>"+I18nUtil.getMessage("jsp.tools.edit-item-form.elem12")+"</strong></th>");
		builder.append("<th id=\"t18\" class=\"oddRowEvenCol\">&nbsp;</th>");
		builder.append("</tr>");

		Bitstream[] bitstreams = bundles[index].getBitstreams();
		for (int j = 0; j < bitstreams.length; j++)
		{
			ArrayList<Integer> bitstreamIdOrder = new ArrayList<Integer>();
			for (Bitstream bitstream : bitstreams) {
			bitstreamIdOrder.add(bitstream.getID());
			}
			
			// Parameter names will include the bundle and bitstream ID
			// e.g. "bitstream_14_18_desc" is the description of bitstream 18 in bundle 14
			String key = bundles[index].getID() + "_" + bitstreams[j].getID();
            BitstreamFormat bf = bitstreams[j].getFormat();
			
			builder.append("<tr id=\"row_" + bundles[index].getName() + "_" + bitstreams[j].getID()+"\">");
			builder.append("<td headers=\"t10\" class=\""+row+"RowEvenCol\">");
			builder.append("<a class=\"btn btn-info\" target=\"_blank\" href=\""+contextPath+"/retrieve/"+bitstreams[j].getID()+"\">"+I18nUtil.getMessage("jsp.tools.general.view")+"</a>&nbsp;");
			builder.append("<a class=\"btn btn-warning\" target=\"_blank\" href=\""+contextPath+"/tools/edit-dso?resource_type=0&resource_id="+bitstreams[j].getID()+"\">"+I18nUtil.getMessage("jsp.tools.general.edit")+"</a>");
			builder.append("</td>");
			if (bundles[index].getName().equals("ORIGINAL")){ 
				builder.append("<td headers=\"t11\" class=\""+row+"RowEvenCol\" >");
				builder.append("<span class=\"form-control\">");
				builder.append("<input type=\"radio\" name=\""+ bundles[index].getID() +"_primary_bitstream_id\" value=\""+bitstreams[j].getID()+"\"");
				if (bundles[index].getPrimaryBitstreamID() == bitstreams[j].getID()) { 
					builder.append("checked=\"checked\"");
				} 
				builder.append("/></span>");
				builder.append("</td>");
			 } else { 
				builder.append("<td headers=\"t11\"> </td>");
			 } 
				builder.append("<td headers=\"t12\" class=\""+row+"RowOddCol\">");
				builder.append("<input class=\"form-control\" type=\"text\" name=\"bitstream_name_"+key+"\" value=\""+ (bitstreams[j].getName() == null ? "" : Utils.addEntities(bitstreams[j].getName())) +"\"/>");
				builder.append("</td>");
				builder.append("<td headers=\"t13\" class=\""+row+"RowEvenCol\">");
				builder.append("<input class=\"form-control\" type=\"text\" name=\"bitstream_source_"+key+"\" value=\""+ (bitstreams[j].getSource() == null ? "" : bitstreams[j].getSource()) +"\"/>");
				builder.append("</td>");
				builder.append("<td headers=\"t14\" class=\""+row+"RowOddCol\">");
				builder.append("<input class=\"form-control\" type=\"text\" name=\"bitstream_description_"+key+"\" value=\""+ (bitstreams[j].getDescription() == null ? "" : Utils.addEntities(bitstreams[j].getDescription())) +"\"/>");
				builder.append("</td>");
				builder.append("<td headers=\"t15\" class=\""+row+"RowEvenCol\">");
				builder.append("<input class=\"form-control\" type=\"text\" name=\"bitstream_format_id_"+key+"\" value=\""+ bf.getID() +"\" size=\"4\"/> ("+ Utils.addEntities(bf.getShortDescription()) +")");
				builder.append("</td>");
				builder.append("<td headers=\"t16\" class=\""+row+"RowOddCol\">");
				builder.append("<input class=\"form-control\" type=\"text\" name=\"bitstream_user_format_description_"+key+"\" value=\""+ (bitstreams[j].getUserFormatDescription() == null ? "" : Utils.addEntities(bitstreams[j].getUserFormatDescription())) +"\"/>");
				builder.append("</td>");
	
			if (bundles[index].getName().equals("ORIGINAL") && breOrderBitstreams)
			{
				 //This strings are only used in case the user has javascript disabled
				String upButtonValue = null;
				String downButtonValue = null;
				if(0 != j){
					ArrayList<Integer> temp = (ArrayList<Integer>) bitstreamIdOrder.clone();
					//We don't have the first button, so create a value where the current bitstreamId moves one up
					Integer tempInt = temp.get(j);
					temp.set(j, temp.get(j - 1));
					temp.set(j - 1, tempInt);
					upButtonValue = StringUtils.join(temp.toArray(new Integer[temp.size()]), ",");
				}
				if(j < (bitstreams.length -1)){
					//We don't have the first button, so create a value where the current bitstreamId moves one up
					ArrayList<Integer> temp = (ArrayList<Integer>) bitstreamIdOrder.clone();
					Integer tempInt = temp.get(j);
					temp.set(j, temp.get(j + 1));
					temp.set(j + 1, tempInt);
					downButtonValue = StringUtils.join(temp.toArray(new Integer[temp.size()]), ",");
				}
							
					builder.append("<td headers=\"t17\" class=\""+row+"RowEvenCol\">");
					builder.append("<input type=\"hidden\" value=\""+(j+1)+"\" name=\"order_"+bitstreams[j].getID()+"\">");
					builder.append("<input type=\"hidden\" value=\""+upButtonValue+"\" name=\""+bundles[index].getID()+"_"+bitstreams[j].getID()+"_up_value\">");
					builder.append("<input type=\"hidden\" value=\""+downButtonValue+"\" name=\""+bundles[index].getID()+"_"+bitstreams[j].getID()+"_down_value\">");
					builder.append("<div>");
					builder.append("<button class=\"btn btn-default\" name=\"submit_order_"+key+"_up\" value=\""+I18nUtil.getMessage("jsp.tools.edit-item-form.move-up")+"\" "+ (j==0 ? "disabled=\"disabled\"" : "")+" >");
					builder.append("<span class=\"glyphicon glyphicon-arrow-up\"></span>");
					builder.append("</button>");
					builder.append("</div>");
					builder.append("<div>");
					builder.append("<button class=\"btn btn-default\" name=\"submit_order_"+key+"_down\" value=\""+I18nUtil.getMessage("jsp.tools.edit-item-form.move-down")+"\" "+ (j==(bitstreams.length-1) ? "disabled=\"disabled\"" : "")+" >");
					builder.append("<span class=\"glyphicon glyphicon-arrow-down\"></span>");
					builder.append("</button>");
					builder.append("</div>");
					builder.append("</td>");
			
			}else{
			
				builder.append("<td>");
				builder.append(j+1);
				builder.append("</td>");
			
			}
			
			builder.append("<td headers=\"t18\" class=\""+row+"RowEvenCol\">");
			
			if (bRemoveBits) { 
				builder.append("<button class=\"btn btn-danger\" name=\"submit_delete_bitstream_"+key+"\" value=\""+I18nUtil.getMessage("jsp.tools.general.remove")+"\">");
				builder.append("<span class=\"glyphicon glyphicon-trash\"></span>");
				builder.append("</button>");
			} 
			builder.append("</td>");
			builder.append("</tr>");
			
			row = (row.equals("odd") ? "even" : "odd");
		}
		 
		builder.append("</table>");
		if(collapsedByDefaultBundles) {
			builder.append("</div>");
		}
		return builder.toString();
	}
}
