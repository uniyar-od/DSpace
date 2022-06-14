/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.ldn.utility;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.dspace.services.ConfigurationService;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * LDN utilities tests.
 */
@RunWith(MockitoJUnitRunner.class)
public class LDNUtilsTest {

    private MockedStatic<DSpaceServicesFactory> dspaceServiceFactoryMock;

    @Mock
    private DSpaceServicesFactory dspaceServicesFactory;

    @Mock
    private ConfigurationService configurationService;

    @Before
    public void setUp() {
        when(dspaceServicesFactory.getConfigurationService()).thenReturn(configurationService);
        when(configurationService.getProperty(eq("ldn.metadata.delimiter"))).thenReturn("||");

        dspaceServiceFactoryMock = Mockito.mockStatic(DSpaceServicesFactory.class);

        dspaceServiceFactoryMock.when(() -> DSpaceServicesFactory.getInstance())
              .thenReturn(dspaceServicesFactory);
    }

    @After
    public void tearDown() {
        dspaceServiceFactoryMock.close();
    }

    @Test
    public void testHasUUIDInURL() {
        assertTrue(LDNUtils.hasUUIDInURL("http://localhost:4200/item/0fb9284e-6be3-4c38-8833-917048c8acb3"));
        assertFalse(LDNUtils.hasUUIDInURL("http://localhost:8080/handle/123456789/3"));
    }

    @Test
    public void testGetUUIDFromURL() {
        UUID id = LDNUtils.getUUIDFromURL("http://localhost:4200/item/0fb9284e-6be3-4c38-8833-917048c8acb3");
        assertEquals("0fb9284e-6be3-4c38-8833-917048c8acb3", id.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetUUIDFromURLException() {
        LDNUtils.getUUIDFromURL("http://localhost:8080/handle/123456789/3");
    }

    @Test
    public void testRemovedProtocol() {
        assertEquals("localhost:8080", LDNUtils.removedProtocol("https://localhost:8080"));
    }

    @Test
    public void testProcessContextResolverId() {
        String doi = LDNUtils.processContextResolverId("https://doi.org/10.5072/FK2/NUB975");
        assertEquals("doi:10.5072/FK2/NUB975", doi);
    }

}
