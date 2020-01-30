/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.webui.util;

import java.util.List;

import org.dspace.content.Bitstream;
import org.dspace.content.MetadataField;

public class BitstreamDifferencesDTO {
	private Bitstream previous;
	private Bitstream newBitstream;
	private List<MetadataField> modifiedMetadata;
	private boolean policiesModified;

	public Bitstream getPrevious() {
		return previous;
	}

	public void setPrevious(Bitstream previous) {
		this.previous = previous;
	}

	public Bitstream getNewBitstream() {
		return newBitstream;
	}

	public void setNewBitstream(Bitstream newBitstream) {
		this.newBitstream = newBitstream;
	}

	public List<MetadataField> getModifiedMetadata() {
		return modifiedMetadata;
	}

	public void setModifiedMetadata(List<MetadataField> modifiedMetadata) {
		this.modifiedMetadata = modifiedMetadata;
	}

	public boolean isPoliciesModified() {
		return policiesModified;
	}

	public void setPoliciesModified(boolean policiesModified) {
		this.policiesModified = policiesModified;
	}

}
