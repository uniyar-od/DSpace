/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.submit.lookup;

import java.util.ArrayList;
import java.util.List;

import gr.ekt.bte.core.Record;
import gr.ekt.bte.core.RecordSet;

import org.apache.log4j.Logger;
import org.dspace.content.Item;

/**
 * @author Andrea Bollini
 * @author Kostas Stamatis
 * @author Luigi Andrea Pascarelli
 * @author Panagiotis Koutsourakis
 */
public class DSpaceSingleItemOutputGenerator extends AItemOutputGenerator {

    private static Logger log = Logger.getLogger(DSpaceSingleItemOutputGenerator.class);


    protected Item item;

    @Override
    public List<String> generateOutput(RecordSet recordSet) {

        log.info("BTE OutputGenerator started. Records to output: " + recordSet.getRecords().size());

        // Printing debug message
        String totalString = "";
        for (Record record : recordSet.getRecords()) {
            totalString += SubmissionLookupUtils.getPrintableString(record) + "\n";
        }
        log.debug("Records to output:\n" + totalString);

        for (Record rec : recordSet.getRecords()) {
            merge(formName, getItem(), rec);

        }

        return new ArrayList<String>();
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }
}
