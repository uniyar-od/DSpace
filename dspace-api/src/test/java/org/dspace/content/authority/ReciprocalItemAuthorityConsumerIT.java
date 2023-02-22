/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.authority;

import java.util.List;
import java.util.UUID;

import org.dspace.AbstractIntegrationTestWithDatabase;
import org.dspace.builder.CollectionBuilder;
import org.dspace.builder.CommunityBuilder;
import org.dspace.builder.ItemBuilder;
import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.dspace.content.MetadataSchemaEnum;
import org.dspace.content.MetadataValue;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.ItemService;
import org.dspace.core.Constants;
import org.dspace.event.Event;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ReciprocalItemAuthorityConsumerIT extends AbstractIntegrationTestWithDatabase {
    private final ReciprocalItemAuthorityConsumer reciprocalItemAuthorityConsumer =
            new ReciprocalItemAuthorityConsumer();

    private final ItemService itemService = ContentServiceFactory.getInstance().getItemService();

    private Item testItem;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        context.turnOffAuthorisationSystem();

        parentCommunity = CommunityBuilder.createCommunity(context)
                .withName("Parent Community")
                .build();

        Collection collection = CollectionBuilder.createCollection(context, parentCommunity)
                .withEntityType("publication")
                .withName("test_collection").build();

        this.testItem = ItemBuilder.createItem(context, collection)
                .withPersonIdentifierFirstName("test_first_name")
                .withPersonIdentifierLastName("test_second_name")
                .withScopusAuthorIdentifier("test_author_identifier")
                .build();

        itemService.addMetadata(context, testItem, MetadataSchemaEnum.DC.getName(), "title", null,
                null, "test_item");

        itemService.addMetadata(context, testItem, MetadataSchemaEnum.DC.getName(), "relation",
                "product", null, testItem.getName(), testItem.getID().toString(), Choices.CF_ACCEPTED);
    }

    @Test
    public void testReciprocalItemAuthorityConsumeHappyPath() throws Exception {
        Event testEvent = new Event(Constants.ADD, Constants.ITEM, this.testItem.getID(), "test");

        reciprocalItemAuthorityConsumer.consume(context, testEvent);

        MetadataValue metadataValueResult = itemService.getMetadataByMetadataString(
                (Item) testEvent.getSubject(context), "dc.relation.publication").get(0);

        Assert.assertNotNull(metadataValueResult);
        Assert.assertEquals(testEvent.getSubjectID().toString(), metadataValueResult.getAuthority());
        Assert.assertTrue(metadataValueResult.getValue().equalsIgnoreCase(testEvent.getSubject(context).getName()));
    }

    @Test
    public void testReciprocalItemAuthorityConsumeIfMetadataExistsHappyPath() throws Exception {
        Event testEvent = new Event(Constants.ADD, Constants.ITEM, this.testItem.getID(), "test");

        itemService.addMetadata(context, testItem, MetadataSchemaEnum.DC.getName(), "relation",
                "publication", null, testItem.getName(), testItem.getID().toString(), Choices.CF_ACCEPTED);

        reciprocalItemAuthorityConsumer.consume(context, testEvent);

        List<MetadataValue> metadataValues = itemService.getMetadataByMetadataString(
                (Item) testEvent.getSubject(context), "dc.relation.publication");

        Assert.assertEquals(1, metadataValues.size());
        Assert.assertEquals("dc_relation_publication", metadataValues.get(0).getMetadataField().toString());
        Assert.assertEquals(0, metadataValues.get(0).getPlace());
    }

    @Test
    public void testReciprocalItemAuthorityConsumeWithNonexistentItemIdFailPath() throws Exception {
        UUID failPathItemId = UUID.fromString("803762b5-6f73-4870-b941-adf3c5626f04");

        Event testEvent = new Event(Constants.ADD, Constants.ITEM, failPathItemId, "test");

        reciprocalItemAuthorityConsumer.consume(context, testEvent);

        Assert.assertNull(itemService.find(context, testEvent.getSubjectID()));
    }

    @Test
    public void testReciprocalItemAuthorityConsumeWithNonexistentItemMetadataFailPath() throws Exception {
        Collection failPathCollection = CollectionBuilder.createCollection(context, parentCommunity)
                .withName("TEST_COLLECTION").build();

        Item failPathItem = ItemBuilder.createItem(context, failPathCollection)
                .withPersonIdentifierFirstName("test_first_name")
                .withPersonIdentifierLastName("test_second_name")
                .withScopusAuthorIdentifier("test_author_identifier")
                .build();

        Event testEvent = new Event(Constants.ADD, Constants.ITEM, failPathItem.getID(), "test");

        reciprocalItemAuthorityConsumer.consume(context, testEvent);

        List<MetadataValue> productMetadataValuesResult = itemService.getMetadataByMetadataString(
                (Item) testEvent.getSubject(context), "dc.relation.product");
        List<MetadataValue> publicationMetadataValuesResult = itemService.getMetadataByMetadataString(
                (Item) testEvent.getSubject(context), "dc.relation.publication");

        Assert.assertTrue(productMetadataValuesResult.isEmpty());
        Assert.assertTrue(publicationMetadataValuesResult.isEmpty());
    }

}