/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.submit.extraction;

import java.util.List;

/**
 * @author Luigi Andrea Pascarelli (luigiandrea.pascarelli at 4science.it)
 */
public class MetadataExtractor {

    private List<String> extensions;

    private String dataloadersKeyMap;

    public List<String> getExtensions() {
        return extensions;
    }

    public void setExtensions(List<String> mime) {
        this.extensions = mime;
    }

    public String getDataloadersKeyMap() {
        return dataloadersKeyMap;
    }

    public void setDataloadersKeyMap(String dataloadersKeyMap) {
        this.dataloadersKeyMap = dataloadersKeyMap;
    }


}
