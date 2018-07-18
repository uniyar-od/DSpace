/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.submit.lookup;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import gr.ekt.bte.core.Record;
import gr.ekt.bte.core.RecordSet;

import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.WorkspaceItem;

/**
 * @author Andrea Bollini
 * @author Kostas Stamatis
 * @author Luigi Andrea Pascarelli
 * @author Panagiotis Koutsourakis
 */
public class DSpaceWorkspaceItemOutputGenerator extends AItemOutputGenerator {

    private static Logger log = Logger
        .getLogger(DSpaceWorkspaceItemOutputGenerator.class);

    protected List<WorkspaceItem> witems;

    private Collection collection;

    @Override
    public List<String> generateOutput(RecordSet recordSet) {

        log.info("BTE OutputGenerator started. Records to output: "
                     + recordSet.getRecords().size());

        // Printing debug message
        String totalString = "";
        for (Record record : recordSet.getRecords()) {
            totalString += SubmissionLookupUtils.getPrintableString(record)
                + "\n";
        }
        log.debug("Records to output:\n" + totalString);

        witems = new ArrayList<WorkspaceItem>();

        for (Record rec : recordSet.getRecords()) {
            try {
                WorkspaceItem wi = workspaceItemService.create(context, collection,
                                                               true);
                merge(formName, wi.getItem(), rec);

                witems.add(wi);

            } catch (AuthorizeException e) {
                log.error(e.getMessage(), e);
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
            }

        }

        return new ArrayList<String>();
    }

    public List<WorkspaceItem> getWitems() {
        return witems;
    }

    public Collection getCollection() {
        return collection;
    }

    public void setCollection(Collection collection) {
        this.collection = collection;
    }

}
