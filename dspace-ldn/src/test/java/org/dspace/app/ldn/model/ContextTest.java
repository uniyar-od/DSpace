/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.ldn.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

/**
 *
 */
public class ContextTest {

    @Test
    public void testContext() {

        Map<Set<String>, String> lookup = new HashMap<>();

        Set<String> key = new HashSet<>();
        key.add("Announce");
        key.add("coar-notify:RelationshipAction");

        lookup.put(key, "Success");

        Context context = new Context();

        context.setId("urn:uuid:a301c520-f790-4f3d-87b1-a18b2b617683");
        context.setUrl(new Url());
        context.setType(new HashSet<>());
        context.setIetfCiteAs("Test");

        assertFalse(lookup.containsKey(context.getType()));

        assertEquals("urn:uuid:a301c520-f790-4f3d-87b1-a18b2b617683", context.getId());
        assertEquals("Test", context.getIetfCiteAs());
        assertEquals(0, context.getType().size());

        context.addType("Announce");
        context.addType("coar-notify:RelationshipAction");

        assertEquals(2, context.getType().size());
        assertTrue(lookup.containsKey(context.getType()));
    }

}
