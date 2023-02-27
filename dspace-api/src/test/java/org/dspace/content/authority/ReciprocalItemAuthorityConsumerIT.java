/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.authority;

import java.sql.SQLException;
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
    }

    @Test
    public void testShouldConsumeIfItemMentionsProduct() throws Exception {
        initItem("publication");

        itemService.addMetadata(context, testItem, MetadataSchemaEnum.DC.getName(), "relation",
                "product", null, testItem.getName(), testItem.getID().toString(), Choices.CF_ACCEPTED);

        Event testEvent = new Event(Event.MODIFY_METADATA, Constants.ITEM, this.testItem.getID(), "test");

        context.addEvent(testEvent);
        context.commit();

        List<MetadataValue> metadataValues = itemService.getMetadataByMetadataString(
                (Item) testEvent.getSubject(context), "dc.relation.publication");

        Assert.assertFalse(metadataValues.isEmpty());

        Assert.assertNotNull(metadataValues.get(0));
        Assert.assertEquals(testEvent.getSubjectID().toString(), metadataValues.get(0).getAuthority());
        Assert.assertTrue(metadataValues.get(0).getValue().equalsIgnoreCase(testEvent.getSubject(context).getName()));
    }

    @Test
    public void testShouldConsumeIfItemMentionsPublication() throws Exception {
        initItem("product");

        itemService.addMetadata(context, testItem, MetadataSchemaEnum.DC.getName(), "relation",
                "publication", null, testItem.getName(), testItem.getID().toString(), Choices.CF_ACCEPTED);

        Event testEvent = new Event(Event.MODIFY_METADATA, Constants.ITEM, this.testItem.getID(), "test");

        context.addEvent(testEvent);
        context.commit();

        List<MetadataValue> metadataValues = itemService.getMetadataByMetadataString(
                (Item) testEvent.getSubject(context), "dc.relation.product");

        Assert.assertFalse(metadataValues.isEmpty());

        Assert.assertNotNull(metadataValues.get(0));
        Assert.assertEquals(testEvent.getSubjectID().toString(), metadataValues.get(0).getAuthority());
        Assert.assertTrue(metadataValues.get(0).getValue().equalsIgnoreCase(testEvent.getSubject(context).getName()));
    }

    @Test
    public void testShouldConsumeIfReciprocalMetadataAlreadyExists() throws Exception {
        initItem("publication");

        itemService.addMetadata(context, testItem, MetadataSchemaEnum.DC.getName(), "relation",
                "product", null, testItem.getName(), testItem.getID().toString(), Choices.CF_ACCEPTED);


        Event testEvent = new Event(Constants.ADD, Constants.ITEM, this.testItem.getID(), "test");

        itemService.addMetadata(context, testItem, MetadataSchemaEnum.DC.getName(), "relation",
                "publication", null, testItem.getName(), testItem.getID().toString(), Choices.CF_ACCEPTED);

        context.addEvent(testEvent);
        context.commit();

        List<MetadataValue> metadataValues = itemService.getMetadataByMetadataString(
                (Item) testEvent.getSubject(context), "dc.relation.publication");

        Assert.assertFalse(metadataValues.isEmpty());

        Assert.assertEquals(1, metadataValues.size());
        Assert.assertEquals("dc_relation_publication", metadataValues.get(0).getMetadataField().toString());
        Assert.assertEquals(0, metadataValues.get(0).getPlace());
        Assert.assertEquals(testEvent.getSubject(context).getName(), metadataValues.get(0).getValue());
        Assert.assertEquals(testEvent.getSubjectID().toString(), metadataValues.get(0).getAuthority());

    }

    @Test
    public void testShouldConsumeWithoutExceptionIfMetadataAuthorityDoesntExist()
            throws SQLException {
        initItem("publication");

        itemService.addMetadata(context, testItem, MetadataSchemaEnum.DC.getName(), "relation",
                "product", null, testItem.getName());

        Event testEvent = new Event(Event.MODIFY, Constants.ITEM, testItem.getID(), "test");

        context.addEvent(testEvent);
        context.commit();
    }

    @Test
    public void testShouldNotCreateItemIfItemIsNotAssociatedWithAnyMetadata() throws Exception {
        initItem(null);

        Event testEvent = new Event(Event.MODIFY, Constants.ITEM, testItem.getID(), "test");

        context.addEvent(testEvent);
        context.commit();

        Assert.assertNotNull(itemService.find(context, testItem.getID()));
        Assert.assertEquals(testItem, itemService.find(context, testItem.getID()));
    }

    @Test
    public void testShouldNotCreateReciprocalMetadataWithNonexistentAuthorityId() throws Exception {
        initItem("publication");

        UUID nonexistentItemId = UUID.fromString("803762b5-6f73-4870-b941-adf3c5626f04");
        itemService.addMetadata(context, testItem, MetadataSchemaEnum.DC.getName(), "relation",
                "product", null, testItem.getName(), nonexistentItemId.toString(), Choices.CF_ACCEPTED);

        Event testEvent = new Event(Event.MODIFY, Constants.ITEM, testItem.getID(), "test");

        context.addEvent(testEvent);
        context.commit();

        Assert.assertTrue(itemService.getMetadata(testItem, "dc.relation.publication",
                testItem.getID().toString()).isEmpty()
        );
    }

    @Test
    public void testShouldNotCreateReciprocalMetadataWithInvalidAuthorityId() throws SQLException {
        initItem("publication");

        String invalidAuthorityId = "12345asd12345";
        itemService.addMetadata(context, testItem, MetadataSchemaEnum.DC.getName(), "relation",
                "product", null, testItem.getName(), invalidAuthorityId, Choices.CF_ACCEPTED);

        Event testEvent = new Event(Event.MODIFY, Constants.ITEM, testItem.getID(), "test");

        context.addEvent(testEvent);
        context.commit();

        Assert.assertTrue(itemService.getMetadata(testItem, "dc.relation.publication",
                testItem.getID().toString()).isEmpty()
        );
    }

    private void initItem(String colName) {
        Collection collection = CollectionBuilder.createCollection(context, parentCommunity)
                .withEntityType(colName)
                .withName("test_collection").build();

        this.testItem = ItemBuilder.createItem(context, collection)
                .withPersonIdentifierFirstName("test_first_name")
                .withPersonIdentifierLastName("test_second_name")
                .withScopusAuthorIdentifier("test_author_identifier")
                .withMetadata(MetadataSchemaEnum.DC.getName(), "title", null, "test_item")
                .build();
    }

}