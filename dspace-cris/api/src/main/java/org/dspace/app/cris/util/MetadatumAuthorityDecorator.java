package org.dspace.app.cris.util;

import org.dspace.app.cris.model.OrganizationUnit;
import org.dspace.app.cris.model.Project;
import org.dspace.app.cris.model.ResearchObject;
import org.dspace.app.cris.model.ResearcherPage;
import org.dspace.app.cris.model.jdyna.widget.WidgetPointerDO;
import org.dspace.app.cris.model.jdyna.widget.WidgetPointerOU;
import org.dspace.app.cris.model.jdyna.widget.WidgetPointerPJ;
import org.dspace.app.cris.model.jdyna.widget.WidgetPointerRP;
import org.dspace.content.IMetadataValue;

import it.cilea.osd.jdyna.model.PropertiesDefinition;
import it.cilea.osd.jdyna.widget.WidgetPointer;

/***
 * The calls is used to decorate Metadatum with authority information
 */
public class MetadatumAuthorityDecorator {
    
	private IMetadataValue metadatum = null;
	
	@SuppressWarnings("rawtypes")
	private Class classname = null;
	
	public MetadatumAuthorityDecorator(IMetadataValue metadatum) {
		init(metadatum);
	}
	
	@SuppressWarnings("rawtypes")
	public MetadatumAuthorityDecorator(IMetadataValue metadatum, PropertiesDefinition npd) {
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
	public boolean isPointer() {
		return classname != null;
	}
	
	/**
	 * Return the model class
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public Class getClassname() {
		return classname;
	}

	private void init(IMetadataValue metadatum) {
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
		else if (w instanceof WidgetPointerDO)	
			classname = ResearchObject.class;
	}
	
	/***
	 * Return the embedded metadatum.
	 * 
	 * @return
	 */
	public IMetadataValue getMetadatum() {
		return metadatum;
	}
	
	/***
	 * Update the metadatum value
	 * 
	 * @param metadatum
	 */
	public void setMetadatum(IMetadataValue metadatum) {
		this.metadatum = metadatum;
	}
}
