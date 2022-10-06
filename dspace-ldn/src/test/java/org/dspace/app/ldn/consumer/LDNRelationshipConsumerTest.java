/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.ldn.consumer;

import static org.dspace.app.ldn.LDNMetadataFields.ELEMENT;
import static org.dspace.app.ldn.LDNMetadataFields.RELATIONSHIP;
import static org.dspace.app.ldn.LDNMetadataFields.SCHEMA;
import static org.dspace.content.Item.ANY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.dspace.app.ldn.factory.LDNBusinessDelegateFactory;
import org.dspace.app.ldn.service.LDNBusinessDelegate;
import org.dspace.content.Item;
import org.dspace.content.MetadataValue;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.dspace.event.Event;
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
 * LDN relationship consumer tests.
 */
@RunWith(Parameterized.class)
public class LDNRelationshipConsumerTest {

    private MockedStatic<ContentServiceFactory> contentServiceFactoryMock;
    private MockedStatic<LDNBusinessDelegateFactory> ldnBusinessDelegateFactoryMock;

    @Mock
    private ContentServiceFactory contentServiceFactory;

    @Mock
    private LDNBusinessDelegateFactory ldnBusinessDelegateFactory;

    @Mock
    private ItemService itemService;

    @Mock
    private LDNBusinessDelegate ldnBusinessDelegate;

    @Mock
    private Context ctx;

    @Mock
    private Event event;

    @Mock
    private Item item;

    @InjectMocks
    private LDNRelationshipConsumer ldnRelationshipConsumer;

    @Parameter(0)
    public int subjectType;

    @Parameter(1)
    public int eventType;

    @Parameter(2)
    public String eventDetail;

    @Parameter(3)
    public boolean itemIsArchived;

    @Parameter(4)
    public List<MetadataValue> relationshipMetadata;

    @Parameter(5)
    public List<MetadataValue> researchMetadata;

    @Parameter(6)
    public int handledRequests;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        contentServiceFactoryMock = Mockito.mockStatic(ContentServiceFactory.class);
        ldnBusinessDelegateFactoryMock = Mockito.mockStatic(LDNBusinessDelegateFactory.class);

        when(contentServiceFactory.getItemService()).thenReturn(itemService);

        contentServiceFactoryMock.when(() -> ContentServiceFactory.getInstance())
            .thenReturn(contentServiceFactory);

        when(ldnBusinessDelegateFactory.getLDNBusinessDelegate()).thenReturn(ldnBusinessDelegate);

        ldnBusinessDelegateFactoryMock.when(() -> LDNBusinessDelegateFactory.getInstance())
            .thenReturn(ldnBusinessDelegateFactory);
    }

    @After
    public void tearDown() {
        contentServiceFactoryMock.close();
        ldnBusinessDelegateFactoryMock.close();
    }

    @Test
    public void testConsumeAndEnd() throws Exception {
        when(event.getSubjectType()).thenReturn(subjectType);
        when(event.getEventType()).thenReturn(eventType);
        when(event.getDetail()).thenReturn(eventDetail);

        when(item.isArchived()).thenReturn(itemIsArchived);

        when(event.getSubject(any(Context.class))).thenReturn(item);

        when(itemService.getMetadata(any(Item.class), eq(SCHEMA), eq(ELEMENT), eq(RELATIONSHIP), eq(ANY)))
            .thenReturn(relationshipMetadata);

        when(itemService.getMetadata(any(Item.class), eq("dc"), eq("data"), eq("uri"), eq(ANY)))
            .thenReturn(researchMetadata);

        ldnRelationshipConsumer.consume(ctx, event);

        ldnRelationshipConsumer.end(ctx);

        verify(ldnBusinessDelegate, times(handledRequests))
            .handleRequest("Announce:RelationshipAction", ctx, item);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> parameters() {

        MetadataValue coarNotifyAnnounceMetadatum = mock(MetadataValue.class);
        MetadataValue dataUriMetadatum = mock(MetadataValue.class);

        when(coarNotifyAnnounceMetadatum.getValue()).thenReturn("2022-04-04T20:36:30Z||https://doi.org/10.5072/FK2/NUB975||repository.org/dataset.xhtml?persistentId=doi:10.5072/FK2/NUB975");
        when(coarNotifyAnnounceMetadatum.getID()).thenReturn(1);

        when(dataUriMetadatum.getValue()).thenReturn("https://doi.org/10.5072/FK2/NUB975");
        when(dataUriMetadatum.getID()).thenReturn(2);

        return new ArrayList<>(Arrays.asList(new Object[][] {
            // subject type, event type, event details, archived, coar metadata, research metadata, handled requests
            { 1, 2, "", true, new ArrayList<>(), new ArrayList<>(), 0, },
            { 2, 1, "", true, new ArrayList<>(), new ArrayList<>(), 0, },

            // test item deposit with research data
            {
                2,
                2,
                null,
                true,
                new ArrayList<>(),
                new ArrayList<>() {
                    {
                        add(dataUriMetadatum);
                    }
                },
                1,
            },
            {
                2,
                2,
                "ARCHIVED: true",
                false,
                new ArrayList<>(),
                new ArrayList<>() {
                    {
                        add(dataUriMetadatum);
                    }
                },
                1,
            },
            // test item update with research data
            {
                2,
                4,
                null,
                true,
                new ArrayList<>(),
                new ArrayList<>() {
                    {
                        add(dataUriMetadatum);
                    }
                },
                1,
            },
            {
                2,
                4,
                "ARCHIVED: true",
                false,
                new ArrayList<>(),
                new ArrayList<>() {
                    {
                        add(dataUriMetadatum);
                    }
                },
                1,
            },

            // test update item with COAR Notify metadata
            {
                2,
                4,
                null,
                true,
                new ArrayList<>() {
                    {
                        add(coarNotifyAnnounceMetadatum);
                    }
                },
                new ArrayList<>() {
                    {
                        add(dataUriMetadatum);
                    }
                },
                0,
            },
            {
                2,
                4,
                "ARCHIVED: true",
                false,
                new ArrayList<>() {
                    {
                        add(coarNotifyAnnounceMetadatum);
                    }
                },
                new ArrayList<>() {
                    {
                        add(dataUriMetadatum);
                    }
                },
                0,
            },
        }));
    }

}
