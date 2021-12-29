/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.statistics;

import org.dspace.eperson.EPerson;

public class LoginStatistics {

    private EPerson user;

    private int count;

    public EPerson getUser() {
        return user;
    }

    public void setUser(EPerson user) {
        this.user = user;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

}
