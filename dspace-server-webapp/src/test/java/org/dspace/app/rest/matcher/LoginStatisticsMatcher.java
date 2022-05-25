/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.matcher;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

import org.hamcrest.Matcher;

/**
 * Utility class to provide convenient matchers for login statistics.
 */
public final class LoginStatisticsMatcher {

    private LoginStatisticsMatcher() {

    }

    /**
     * Gets a matcher to ensure a given value is present among all values for a
     * given metadata key.
     *
     * @param  key   the metadata key.
     * @param  value the value that must be present.
     * @return       the matcher.
     */
    public static Matcher<? super Object> match(String name, String email, int count) {
        return allOf(hasJsonPath("$.name", is(name)),
            hasJsonPath("$.email", is(email)),
            hasJsonPath("$.count", is(count)));
    }
}
