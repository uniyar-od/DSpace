/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.metrics;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.UUID;

import org.dspace.authorize.service.AuthorizeService;
import org.dspace.content.Item;
import org.dspace.content.service.ItemService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Unit tests for CrisItemMetricsServiceImplTest.
 *
 * @author Alessandro Martelli (alessandro.martelli at 4science.it)
 */

@RunWith(MockitoJUnitRunner.class)
public class CrisItemMetricsAuthorizationServiceImplTest {

    @Mock
    private AuthorizeService authorizeService;

    @Mock
    private ItemService itemService;

    @Mock
    Context context;

    @Mock
    Item item;

    @Mock
    EPerson eperson;

    UUID itemUUID;

    CrisItemMetricsAuthorizationServiceImpl crisItemMetricsAuthorizationService;

    @Before
    public void setUp() throws Exception {

        crisItemMetricsAuthorizationService = new CrisItemMetricsAuthorizationServiceImpl();
        crisItemMetricsAuthorizationService.itemService = itemService;
        crisItemMetricsAuthorizationService.authorizeService = authorizeService;

        itemUUID = UUID.randomUUID();
    }

    @Test
    public void isAuthorizedForItemUUIDWhenAnonymousUserShouldReturnFalse() {

        when(context.getCurrentUser()).thenReturn(null);

        boolean result = crisItemMetricsAuthorizationService.isAuthorized(context, itemUUID);

        assertFalse(result);

    }

    @Test
    public void isAuthorizedForItemUUIDWhenTheItemCantBeRetrievedShouldReturnFalse() throws SQLException {


        when(context.getCurrentUser()).thenReturn(eperson);
        when(itemService.find(context, itemUUID)).thenReturn(null);

        boolean result = crisItemMetricsAuthorizationService.isAuthorized(context, itemUUID);

        assertFalse(result);

    }

    @Test
    public void isAuthorizedForItemUUIDWhenTheAuthorizeServiceReturnsFalseShouldReturnFalse() throws SQLException {

        when(context.getCurrentUser()).thenReturn(eperson);
        when(itemService.find(context, itemUUID)).thenReturn(item);
        when(authorizeService.authorizeActionBoolean(context, item, Constants.READ)).thenReturn(false);

        boolean result = crisItemMetricsAuthorizationService.isAuthorized(context, itemUUID);

        assertFalse(result);

    }

    @Test
    public void isAuthorizedForItemUUIDWhenTheAuthorizeServiceReturnsTrueShouldReturnTrue() throws SQLException {


        when(context.getCurrentUser()).thenReturn(eperson);
        when(itemService.find(context, itemUUID)).thenReturn(item);
        when(authorizeService.authorizeActionBoolean(context, item, Constants.READ)).thenReturn(true);

        boolean result = crisItemMetricsAuthorizationService.isAuthorized(context, itemUUID);

        assertTrue(result);

    }

    @Test
    public void isAuthorizedForItemWhenAnonymousUserShouldReturnFalse() {

        when(context.getCurrentUser()).thenReturn(null);

        boolean result = crisItemMetricsAuthorizationService.isAuthorized(context, item);

        assertFalse(result);

    }

    @Test
    public void isAuthorizedForItemWhenTheAuthorizeServiceReturnsFalseShouldReturnFalse() throws SQLException {

        when(context.getCurrentUser()).thenReturn(eperson);
        when(authorizeService.authorizeActionBoolean(context, item, Constants.READ)).thenReturn(false);

        boolean result = crisItemMetricsAuthorizationService.isAuthorized(context, item);

        assertFalse(result);

    }

    @Test
    public void isAuthorizedForItemWhenTheAuthorizeServiceReturnsTrueShouldReturnTrue() throws SQLException {

        when(context.getCurrentUser()).thenReturn(eperson);
        when(authorizeService.authorizeActionBoolean(context, item, Constants.READ)).thenReturn(true);

        boolean result = crisItemMetricsAuthorizationService.isAuthorized(context, item);

        assertTrue(result);

    }


}
