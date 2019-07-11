package org.dspace.app.cris.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dspace.app.cris.model.OrganizationUnit;
import org.dspace.app.cris.model.Project;
import org.dspace.app.cris.model.ResearcherPage;
import org.dspace.app.cris.model.jdyna.RPNestedPropertiesDefinition;
import org.dspace.app.cris.model.jdyna.widget.WidgetPointerDO;
import org.dspace.app.cris.model.jdyna.widget.WidgetPointerOU;
import org.dspace.app.cris.model.jdyna.widget.WidgetPointerPJ;
import org.dspace.app.cris.model.jdyna.widget.WidgetPointerRP;
import org.dspace.content.Metadatum;

import it.cilea.osd.jdyna.widget.WidgetPointer;

/***
 * The calls is used to decorate Metadatum with authority information
 */
public class MetadatumAuthorityDecorator {
	private Metadatum metadatum = null;
	
	@SuppressWarnings("rawtypes")
	private Class classname = null;
	
	public MetadatumAuthorityDecorator(Metadatum metadatum) {
		init(metadatum);
	}
	
	@SuppressWarnings("rawtypes")
	public MetadatumAuthorityDecorator(Metadatum metadatum, RPNestedPropertiesDefinition npd) {
		init(metadatum);
		
		if (npd.getRendering() instanceof WidgetPointer) {
			WidgetPointer w = (WidgetPointer)npd.getRendering();
			setClassname(w);
		}
	}
	
	/***
	 * Check if classname is assigned
	 * 
	 * @return true if classname is null.
	 */
	public boolean isClassNameNull() {
		return classname == null;
	}
	
	/***
	 * Check if classname is assigned using the authority value
	 * 
	 * @param authority The authority value
	 * @return true if classname is null.
	 */
	public boolean isClassNameNull(String authority) {
		Pattern pattern = Pattern.compile("(ou|proj|rp)([0-9]+)");
		Matcher matcher = pattern.matcher(authority);
		return !matcher.matches();
	}
	
	/**
	 * Return the model class
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public Class className() {
		return classname;
	}
	
	/**
	 * Return the model class
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public Class className(String authority) {
		Pattern pattern = Pattern.compile("(ou|proj|rp)([0-9]+)");
		Matcher matcher = pattern.matcher(authority);
		
		if (matcher.matches()) {
			switch (matcher.group(1)) {
				case "ou": return OrganizationUnit.class;
				case "rp": return ResearcherPage.class;
				case "proj": return Project.class;
				default: return null;
			}
		}
		return null;
	}
	
	private void init(Metadatum metadatum) {
		this.metadatum = metadatum;
	}
	
	/***
	 * Set the internal class using the wodget pointer.
	 * 
	 * @param w The widget pointer
	 */
	@SuppressWarnings({ "rawtypes" })
	private void setClassname(WidgetPointer w) {
		if (w instanceof WidgetPointerOU)
			classname = OrganizationUnit.class;
		else if (w instanceof WidgetPointerPJ)
			classname = Project.class;
		else if (w instanceof WidgetPointerRP)
			classname = ResearcherPage.class;
		else if (w instanceof WidgetPointerDO)	// TODO: which value?
			classname = null;
	}
	
	/***
	 * Return the embedded metadatum.
	 * 
	 * @return
	 */
	public Metadatum getMetadatum() {
		return metadatum;
	}
}
