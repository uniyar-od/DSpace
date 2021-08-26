/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.model.step;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

/**
 * Java Bean to expose the section to reserve doi during submission.
 *
 * @author Andrea Bollini (andrea.bollin at 4science.it)
 */
public class DataReserveDOI implements SectionData {

    @JsonProperty(access = Access.READ_ONLY)
    private String doi;

    @JsonProperty(access = Access.READ_ONLY)
    private boolean automaticMinted = false;

    private boolean minted = false;

    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }

    public boolean isAutomaticMinted() {
        return automaticMinted;
    }

    public void setAutomaticMinted(boolean automaticMinted) {
        this.automaticMinted = automaticMinted;
    }

    public boolean isMinted() {
        return minted;
    }

    public void setMinted(boolean minted) {
        this.minted = minted;
    }

}
