/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.statistics;

/**
 * Data structure for returning results from statistics pivot searches.
 *
 * @author Luca Giamminonni (luca.giamminonni at 4Science)
 */
public final class PivotObjectCount {

    private final long count;

    private final String value;

    private final PivotObjectCount[] pivot;

    public PivotObjectCount(long count, String value, PivotObjectCount[] pivot) {
        this.count = count;
        this.value = value;
        this.pivot = pivot;
    }

    public long getCount() {
        return count;
    }

    public String getValue() {
        return value;
    }

    public PivotObjectCount[] getPivot() {
        return pivot;
    }

}
