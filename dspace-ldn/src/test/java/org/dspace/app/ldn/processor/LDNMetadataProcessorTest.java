/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.ldn.processor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.dspace.app.ldn.MockNotificationUtility;
import org.dspace.app.ldn.action.ActionStatus;
import org.dspace.app.ldn.action.LDNEmailAction;
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

    @Mock
    private LDNEmailAction ldnEmailAction;

    @Mock
    private LDNMetadataRemove ldnMetadataRemove;

    @Mock
    private LDNMetadataAdd ldnMetadataAdd;

    @InjectMocks
    private LDNMetadataProcessor ldnMetadataProcessor;

    @Parameter(0)
    public String mockNotificationPath;

    @Parameter(1)
    public int resolveUrlToHandle;

    @Parameter(2)
    public int resolveToObject;

    @Parameter(3)
    public int find;

    @Before
    public void setUp() throws Exception {
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

        when(configurationService.getProperty(eq("ldn.notify.allowed-external-resolver-urls"))).thenReturn("");
        when(configurationService.getProperty(eq("dspace.ui.url"))).thenReturn("http://localhost:4000");
        when(configurationService.getProperty(eq("ldn.metadata.delimiter"))).thenReturn("||");

        when(item.getType()).thenReturn(Constants.ITEM);


        when(ldnEmailAction.execute(
            any(Notification.class),
            any(Item.class)
        )).thenReturn(ActionStatus.CONTINUE);

        when(ldnMetadataRemove.renderTemplate(
            any(VelocityContext.class),
            any(VelocityEngine.class),
            eq("true")
        )).thenReturn("true");
        doNothing().when(ldnMetadataRemove).doAction(
            any(VelocityContext.class),
            any(VelocityEngine.class),
            any(Context.class),
            any(Item.class)
        );

        when(ldnMetadataRemove.getConditionTemplate()).thenReturn("true");

        when(ldnMetadataAdd.renderTemplate(
            any(VelocityContext.class),
            any(VelocityEngine.class),
            eq("true")
        )).thenReturn("true");
        doNothing().when(ldnMetadataAdd).doAction(
            any(VelocityContext.class),
            any(VelocityEngine.class),
            any(Context.class),
            any(Item.class)
        );

        when(ldnMetadataAdd.getConditionTemplate()).thenReturn("true");
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

        ldnMetadataProcessor.setActions(Arrays.asList(ldnEmailAction));
        ldnMetadataProcessor.setChanges(Arrays.asList(ldnMetadataRemove, ldnMetadataAdd));

        when(handleService.resolveUrlToHandle(any(Context.class), anyString())).thenReturn("123456789/1");
        when(handleService.resolveToObject(any(Context.class), anyString())).thenReturn(item);

        when(itemService.find(any(Context.class), any(UUID.class))).thenReturn(item);

        ldnMetadataProcessor.init();

        ldnMetadataProcessor.process(notification);

        verify(handleService, times(resolveUrlToHandle)).resolveUrlToHandle(
            any(Context.class),
             anyString()
        );

        verify(handleService, times(resolveToObject)).resolveToObject(
            any(Context.class),
            anyString()
        );

        verify(itemService, times(find)).find(
            any(Context.class),
            any(UUID.class)
        );

        verify(ldnEmailAction, times(1)).execute(
            any(Notification.class),
            any(Item.class)
        );

        verify(ldnMetadataRemove, times(1)).renderTemplate(
            any(VelocityContext.class),
            any(VelocityEngine.class),
            eq("true")
        );
        verify(ldnMetadataRemove, times(1)).doAction(
            any(VelocityContext.class),
            any(VelocityEngine.class),
            any(Context.class),
            any(Item.class)
        );

        verify(ldnMetadataAdd, times(1)).renderTemplate(
            any(VelocityContext.class),
            any(VelocityEngine.class),
            eq("true")
        );
        verify(ldnMetadataAdd, times(1)).doAction(
            any(VelocityContext.class),
            any(VelocityEngine.class),
            any(Context.class),
            any(Item.class)
        );

        verify(context, times(1)).turnOffAuthorisationSystem();
        verify(itemService, times(1)).update(any(Context.class), eq(item));
        verify(context, times(1)).commit();
        verify(context, times(1)).restoreAuthSystemState();
    }

    @Parameterized.Parameters
    public static Collection<Object[]> parameters() {
        return new ArrayList<>(Arrays.asList(new Object[][] {
            { "src/test/resources/mocks/inbound.json", 1, 1, 0 },
            { "src/test/resources/mocks/inboundUUID.json", 0, 0, 1 }
        }));
    }

}
