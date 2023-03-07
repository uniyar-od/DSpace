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
import org.dspace.core.Context;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ReciprocalItemAuthorityConsumerIT extends AbstractIntegrationTestWithDatabase {

    private final ItemService itemService = ContentServiceFactory.getInstance().getItemService();

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
    public void testShouldCreatePublicationMetadataForProductItem() {
        String productTitle = "productTitle";
        Item productItem = initItem("product", productTitle).build();
        Item publicationItem = initItem("publication", "publicationTitle")
                .withSecuredMetadataValue(MetadataSchemaEnum.DC.getName(), "relation",
                        "product", null, productTitle, productItem.getID().toString(), Choices.CF_ACCEPTED, null)
                .build();

        List<MetadataValue> metadataValues = itemService.getMetadataByMetadataString(
                productItem, "dc.relation.publication");

        Assert.assertEquals(1, metadataValues.size());
        Assert.assertNotNull(metadataValues.get(0));
        Assert.assertEquals(publicationItem.getID().toString(), metadataValues.get(0).getAuthority());
        Assert.assertEquals(publicationItem.getName(), metadataValues.get(0).getValue());
    }

    @Test
    public void testShouldCreateProductMetadataForPublicationItem() {
        String publicationTitle = "publicationTitle";
        Item publicationItem = initItem("publication", publicationTitle).build();
        Item productItem = initItem("product", "productTitle")
                .withSecuredMetadataValue(MetadataSchemaEnum.DC.getName(), "relation", "publication",
                        null, publicationTitle, publicationItem.getID().toString(), Choices.CF_ACCEPTED, null)
                .build();

        List<MetadataValue> metadataValues = itemService.getMetadataByMetadataString(
                publicationItem, "dc.relation.product");

        Assert.assertEquals(1, metadataValues.size());
        Assert.assertNotNull(metadataValues.get(0));
        Assert.assertEquals(productItem.getID().toString(), metadataValues.get(0).getAuthority());
        Assert.assertEquals(productItem.getName(), metadataValues.get(0).getValue());
    }

    @Test
    public void testItemMentioningNotExistingAuthorityIsCreated() throws Exception {
        UUID notExistingItemId = UUID.fromString("803762b5-6f73-4870-b941-adf3c5626f04");
        Item publicationItem = initItem("publication", "publicationTitle").build();
        Item productItem = initItem("product", "productTitle")
                .withSecuredMetadataValue(MetadataSchemaEnum.DC.getName(), "relation", "product",
                        null, "notExistingPublicationTitle", notExistingItemId.toString(), Choices.CF_ACCEPTED, null)
                .build();

        List<MetadataValue> metadataValues = itemService.getMetadataByMetadataString(
                publicationItem, "dc.relation.product");
        Assert.assertEquals(0, metadataValues.size());

        Item foundProductItem = itemService.findByIdOrLegacyId(new Context(), productItem.getID().toString());
        Assert.assertEquals(productItem.getID(), foundProductItem.getID());
    }

    @Test
    public void testItemMentioningInvalidAuthorityIsCreated() throws Exception {
        Item publicationItem = initItem("publication", "publicationTitle").build();
        Item productItem = initItem("product", "productTitle")
                .withSecuredMetadataValue(MetadataSchemaEnum.DC.getName(), "relation", "product",
                        null, "notExistingPublicationTitle", "invalidAuthorityUUID", Choices.CF_ACCEPTED, null)
                .build();

        List<MetadataValue> metadataValues = itemService.getMetadataByMetadataString(
                publicationItem, "dc.relation.product");
        Assert.assertEquals(0, metadataValues.size());

        Item foundProductItem = itemService.findByIdOrLegacyId(new Context(), productItem.getID().toString());
        Assert.assertEquals(productItem.getID(), foundProductItem.getID());
    }

    @Test
    public void testItemWithoutAuthorityIsCreated() throws Exception {
        String publicationTitle = "publicationTitle";
        Item publicatoinItem = initItem("publication", publicationTitle).build();
        Item productItem = initItem("product", "productTitle")
                .withMetadata(MetadataSchemaEnum.DC.getName(), "relation", "publication", publicationTitle)
                .build();

        List<MetadataValue> metadataValues = itemService.getMetadataByMetadataString(
                publicatoinItem, "dc.relation.product");
        Assert.assertEquals(0, metadataValues.size());

        Item foundProductItem = itemService.findByIdOrLegacyId(new Context(), productItem.getID().toString());
        Assert.assertEquals(productItem.getID(), foundProductItem.getID());
    }

    @Test
    public void testItemWithoutPublicationMetadataIsCreated() throws Exception {
        Item publicationItem = initItem("publication", "publicationTitle").build();
        Item productItem = initItem("product", "productTitle").build();

        List<MetadataValue> publicationItemMetadataValues = itemService.getMetadataByMetadataString(
                publicationItem, "dc.relation.product");
        Assert.assertEquals(0, publicationItemMetadataValues.size());

        List<MetadataValue> productItemMetadataValues = itemService.getMetadataByMetadataString(
                productItem, "dc.relation.publication");
        Assert.assertEquals(0, productItemMetadataValues.size());

        Item foundProductItem = itemService.findByIdOrLegacyId(new Context(), productItem.getID().toString());
        Assert.assertEquals(productItem.getID(), foundProductItem.getID());
    }

    private ItemBuilder initItem(String itemType, String itemTitle) {
        Collection collection = CollectionBuilder.createCollection(context, parentCommunity)
                .withEntityType(itemType)
                .withName("test_collection").build();

        return ItemBuilder.createItem(context, collection)
                .withPersonIdentifierFirstName("test_first_name")
                .withPersonIdentifierLastName("test_second_name")
                .withScopusAuthorIdentifier("test_author_identifier")
                .withMetadata(MetadataSchemaEnum.DC.getName(), "title", null, itemTitle)
                .withType(itemType);
    }

}