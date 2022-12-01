/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.integration.crosswalks.csl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;

import de.undercouch.citeproc.CSL;
import org.apache.commons.io.IOUtils;
import org.dspace.services.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implementation of {@link CSLGenerator} that uses a nested Citation processor
 * to generate the citations.
 *
 * @author Luca Giamminonni (luca.giamminonni at 4science.it)
 *
 */
public class CSLNestedGenerator implements CSLGenerator {

    @Autowired
    private ConfigurationService configurationService;

    @Override
    public String generate(DSpaceListItemDataProvider itemDataProvider, String style, String format) {
        CSL citeproc = createCitationProcessor(itemDataProvider, style, format);
        return citeproc.makeBibliography().makeString();
    }

    private CSL createCitationProcessor(DSpaceListItemDataProvider itemDataProvider, String style, String format) {
        try {
            CSL citeproc = new CSL(itemDataProvider, getStyle(style));
            citeproc.setOutputFormat(format);
            citeproc.registerCitationItems(itemDataProvider.getIds());
            return citeproc;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getStyle(String style) throws IOException {
        return CSL.supportsStyle(style) ? style : readXmlStyleContent(style);
    }

    private String readXmlStyleContent(String style) throws IOException {
        String parent = configurationService.getProperty("dspace.dir") + File.separator + "config" + File.separator;
        File styleFile = new File(parent, style);
        if (!styleFile.exists()) {
            throw new FileNotFoundException("Could not find style in " + styleFile.getAbsolutePath());
        }

        try (FileInputStream fis = new FileInputStream(styleFile)) {
            return IOUtils.toString(fis, Charset.defaultCharset());
        }
    }

}
