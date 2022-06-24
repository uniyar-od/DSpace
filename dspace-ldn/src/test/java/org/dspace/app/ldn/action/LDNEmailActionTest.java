/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.ldn.action;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

import org.dspace.app.ldn.MockNotificationUtility;
import org.dspace.app.ldn.model.Notification;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.core.Email;
import org.dspace.core.I18nUtil;
import org.dspace.eperson.EPerson;
import org.dspace.services.ConfigurationService;
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
 * LDN email action tests.
 */
@RunWith(Parameterized.class)
public class LDNEmailActionTest {

    private MockedStatic<ContextUtil> contextMock;
    private MockedStatic<I18nUtil> i18nMock;
    private MockedStatic<Email> emailMock;

    @Mock
    private ConfigurationService configurationService;

    @Mock
    private Context context;

    @Mock
    private EPerson ePerson;

    @Mock
    private Email email;

    @Mock
    private Item item;

    @InjectMocks
    private LDNEmailAction emailAction;

    @Parameter(0)
    public String actionSendFilter;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        contextMock = Mockito.mockStatic(ContextUtil.class);
        i18nMock = Mockito.mockStatic(I18nUtil.class);
        emailMock = Mockito.mockStatic(Email.class);
    }

    @After
    public void tearDown() {
        contextMock.close();
        i18nMock.close();
        emailMock.close();
    }

    @Test
    public void testExecute() throws Exception {
        Locale locale = Locale.ENGLISH;

        emailAction.setActionSendFilter(actionSendFilter);

        String emailTemplate = Files.readString(Path.of("src/test/resources/mocks/coar_notify_released"));

        emailAction.setActionSendEmailTextFile(emailTemplate);

        Notification notification = MockNotificationUtility.read("src/test/resources/mocks/fromDataverse.json");

        when(context.getCurrentUser()).thenReturn(ePerson);

        when(ePerson.getFullName()).thenReturn("Bob Boring");
        when(ePerson.getEmail()).thenReturn("bboring@dspace.org");

        when(item.getName()).thenReturn("Test Item");
        when(item.getSubmitter()).thenReturn(ePerson);

        when(configurationService.getArrayProperty(anyString())).thenReturn(new String[] {
            "eexciting@dspace.org"
        });

        contextMock.when(() -> ContextUtil.obtainCurrentRequestContext())
            .thenReturn(context);

        i18nMock.when(() -> I18nUtil.getEPersonLocale(any(EPerson.class)))
            .thenReturn(locale);

        i18nMock.when(() -> I18nUtil.getEmailFilename(any(Locale.class), anyString()))
            .thenReturn("coar_notify_released");

        emailMock.when(() -> Email.getEmail(anyString()))
            .thenReturn(email);

        ActionStatus status = emailAction.execute(notification, item);

        assertEquals(ActionStatus.CONTINUE, status);

        verify(email, times(1)).send();
    }

    @Parameterized.Parameters
    public static Collection<Object[]> parameters() {
        return new ArrayList<>(Arrays.asList(new Object[][] {
            { "SUBMITTER" },
            { "GROUP:test" },
            { "bboring@dspace.org" },
        }));
    }

}
