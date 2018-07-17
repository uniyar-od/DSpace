/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.submit.lookup;

import java.util.Map;
import java.util.Set;

import gr.ekt.bte.core.DataLoader;
import gr.ekt.bte.core.DataLoadingSpec;
import gr.ekt.bte.core.RecordSet;
import gr.ekt.bte.dataloader.FileDataLoader;
import gr.ekt.bte.exceptions.MalformedSourceException;

import org.apache.log4j.Logger;

/**
 * @author Andrea Bollini
 * @author Kostas Stamatis
 * @author Luigi Andrea Pascarelli
 * @author Panagiotis Koutsourakis
 */
public abstract class ASubmissionLookupDataLoader implements DataLoader {

    private static Logger log = Logger
        .getLogger(ASubmissionLookupDataLoader.class);

    protected final String NOT_FOUND_DOI = "NOT-FOUND-DOI";

    Map<String, DataLoader> dataloadersMap;

    // Depending on these values, the multiple data loader loads data from the
    // appropriate providers
    Map<String, Set<String>> identifiers = null; // Searching by identifiers
    // (DOI ...)

    Map<String, Set<String>> searchTerms = null; // Searching by author, title,
    // date

    String filename = null; // Uploading file

    String type = null; // the type of the upload file (bibtex, etc.)

    /*
     * (non-Javadoc)
     *
     * @see
     * gr.ekt.bte.core.DataLoader#getRecords(gr.ekt.bte.core.DataLoadingSpec)
     */
    @Override
    public RecordSet getRecords(DataLoadingSpec loadingSpec)
        throws MalformedSourceException {

        // Identify the end of loading
        if (loadingSpec.getOffset() > 0) {
            return new RecordSet();
        }

        return getRecords();
    }

    public Map<String, DataLoader> getProvidersMap() {
        return dataloadersMap;
    }

    public void setDataloadersMap(Map<String, DataLoader> providersMap) {
        this.dataloadersMap = providersMap;
    }

    public void setIdentifiers(Map<String, Set<String>> identifiers) {
        this.identifiers = identifiers;
        this.filename = null;
        this.searchTerms = null;

        if (dataloadersMap != null) {
            for (String providerName : dataloadersMap.keySet()) {
                DataLoader provider = dataloadersMap.get(providerName);
                if (provider instanceof NetworkSubmissionLookupDataLoader) {
                    ((NetworkSubmissionLookupDataLoader) provider)
                        .setIdentifiers(identifiers);
                }

            }
        }
    }

    public void setSearchTerms(Map<String, Set<String>> searchTerms) {
        this.searchTerms = searchTerms;
        this.identifiers = null;
        this.filename = null;

        if (dataloadersMap != null) {
            for (String providerName : dataloadersMap.keySet()) {
                DataLoader provider = dataloadersMap.get(providerName);
                if (provider instanceof NetworkSubmissionLookupDataLoader) {
                    ((NetworkSubmissionLookupDataLoader) provider)
                        .setSearchTerms(searchTerms);
                }
            }
        }
    }

    public void setFile(String filename, String type) {
        this.filename = filename;
        this.type = type;
        this.identifiers = null;
        this.searchTerms = null;

        if (dataloadersMap != null) {
            for (String providerName : dataloadersMap.keySet()) {
                DataLoader provider = dataloadersMap.get(providerName);
                if (provider instanceof FileDataLoader) {
                    ((FileDataLoader) provider).setFilename(filename);
                }
            }
        }
    }

}
