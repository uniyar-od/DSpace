/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.ldn.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 *
 */
public class ObjectTest {

    @Test
    public void testObject() {
        Object object = new Object();

        object.setTitle("Test");
        assertEquals("Test", object.getTitle());

        String itemId = "https://research-organisation.org/dspace/item/35759679-5df3-4633-b7e5-4cf24b4d0614";

        object.setSubject(itemId);
        assertEquals(itemId, object.getSubject());

        object.setRelationship("http://purl.org/vocab/frbr/core#supplementOf");
        assertEquals("http://purl.org/vocab/frbr/core#supplementOf", object.getRelationship());

        object.setObject("https://research-organisation.org/repository/201203/421");
        assertEquals("https://research-organisation.org/repository/201203/421", object.getObject());
    }

}
