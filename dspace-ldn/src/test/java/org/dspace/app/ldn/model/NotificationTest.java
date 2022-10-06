/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.ldn.model;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.dspace.app.ldn.MockNotificationUtility;
import org.junit.Test;

public class NotificationTest {

    @Test
    public void testInboundNotification() throws IllegalArgumentException, IOException {
        Notification notification = MockNotificationUtility.read("src/test/resources/mocks/inbound.json");
        String[] context = notification.getC();
        assertEquals(2, context.length);
        assertEquals("https://purl.org/coar/notify", context[0]);
        assertEquals("https://www.w3.org/ns/activitystreams", context[1]);
    }

    @Test
    public void testOutboundNotification() throws IllegalArgumentException, IOException {
        Notification notification = MockNotificationUtility.read("src/test/resources/mocks/outbound.json");
        String[] context = notification.getC();
        assertEquals(2, context.length);
        assertEquals("https://purl.org/coar/notify", context[0]);
        assertEquals("https://www.w3.org/ns/activitystreams", context[1]);
    }

}
