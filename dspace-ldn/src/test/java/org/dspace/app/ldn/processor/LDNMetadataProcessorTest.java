/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.ldn.processor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.dspace.app.ldn.MockNotificationUtility;
import org.dspace.app.ldn.action.LDNAction;
import org.dspace.app.ldn.model.Notification;
import org.dspace.content.Item;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.ItemService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.handle.factory.HandleServiceFactory;
import org.dspace.handle.service.HandleService;
import org.dspace.services.ConfigurationService;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.dspace.web.ContextUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * LDN metadata processor tests.
 */
@RunWith(Parameterized.class)
public class LDNMetadataProcessorTest {

    private MockedStatic<ContextUtil> contextMock;
    private MockedStatic<ContentServiceFactory> contentServiceFactoryMock;
    private MockedStatic<HandleServiceFactory> handleServiceFactoryMock;
    private MockedStatic<DSpaceServicesFactory> dspaceServiceFactoryMock;

    @Mock
    private ContentServiceFactory contentServiceFactory;

    @Mock
    private HandleServiceFactory handleServiceFactory;

    @Mock
    private DSpaceServicesFactory dspaceServicesFactory;

    @Mock
    private ItemService itemService;

    @Mock
    private HandleService handleService;

    @Mock
    private ConfigurationService configurationService;

    @Mock
    private Context context;

    @Mock
    private Item item;

    @InjectMocks
    private LDNMetadataProcessor ldnMetadataProcessor;

    @Parameter(0)
    public String mockNotificationPath;

    @Parameter(1)
    public List<LDNAction> mockActions;

    @Parameter(2)
    public List<LDNMetadataChange> mockChanges;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        contextMock = Mockito.mockStatic(ContextUtil.class);

        contentServiceFactoryMock = Mockito.mockStatic(ContentServiceFactory.class);
        when(contentServiceFactory.getItemService()).thenReturn(itemService);
        contentServiceFactoryMock.when(() -> ContentServiceFactory.getInstance())
            .thenReturn(contentServiceFactory);

        handleServiceFactoryMock = Mockito.mockStatic(HandleServiceFactory.class);
        when(handleServiceFactory.getHandleService()).thenReturn(handleService);
        handleServiceFactoryMock.when(() -> HandleServiceFactory.getInstance())
            .thenReturn(handleServiceFactory);

        dspaceServiceFactoryMock = Mockito.mockStatic(DSpaceServicesFactory.class);
        dspaceServiceFactoryMock.when(() -> DSpaceServicesFactory.getInstance())
            .thenReturn(dspaceServicesFactory);
        when(dspaceServicesFactory.getConfigurationService()).thenReturn(configurationService);

        contextMock.when(() -> ContextUtil.obtainCurrentRequestContext())
            .thenReturn(context);

        LDNContextRepeater repeater = new LDNContextRepeater();
        repeater.setRepeatOver("IsSupplementTo");
        ldnMetadataProcessor.setRepeater(repeater);

        when(configurationService.getProperty(eq("ldn.notify.allowed-external-resolver-urls"))).thenReturn("");
        when(configurationService.getProperty(eq("dspace.ui.url"))).thenReturn("http://localhost:4200");
        when(configurationService.getProperty(eq("ldn.metadata.delimiter"))).thenReturn("||");

        when(item.getType()).thenReturn(Constants.ITEM);
    }

    @After
    public void tearDown() {
        contextMock.close();
        contentServiceFactoryMock.close();
        handleServiceFactoryMock.close();
        dspaceServiceFactoryMock.close();
    }

    @Test
    public void testInitAndProcess() throws Exception {
        Notification notification = MockNotificationUtility.read(mockNotificationPath);

        ldnMetadataProcessor.setActions(mockActions);
        ldnMetadataProcessor.setChanges(mockChanges);

        when(handleService.resolveUrlToHandle(any(Context.class), eq("http://localhost:4200/handle/123456789/3"))).thenReturn("123456789/3");
        when(handleService.resolveToObject(any(Context.class), eq("123456789/3"))).thenReturn(item);

        ldnMetadataProcessor.init();

        ldnMetadataProcessor.process(notification);

        verify(context, times(1)).turnOffAuthorisationSystem();
        verify(itemService, times(1)).update(any(Context.class), eq(item));
        verify(context, times(1)).commit();
        verify(context, times(1)).restoreAuthSystemState();
    }

    @Parameterized.Parameters
    public static Collection<Object[]> parameters() {
        return new ArrayList<>(Arrays.asList(new Object[][] {
            // test not changes or actions
            {
                "src/test/resources/mocks/fromDataverse.json",
                new ArrayList<>(),
                new ArrayList<>(),
            }
        }));
    }

}
