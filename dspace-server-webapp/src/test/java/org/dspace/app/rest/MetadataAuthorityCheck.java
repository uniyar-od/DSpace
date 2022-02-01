package org.dspace.app.rest;

import org.dspace.app.rest.test.AbstractControllerIntegrationTest;
import org.dspace.builder.CollectionBuilder;
import org.dspace.builder.CommunityBuilder;
import org.dspace.builder.ItemBuilder;
import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.dspace.content.service.ItemService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

public class MetadataAuthorityCheck extends AbstractControllerIntegrationTest {
    @Autowired
    private ItemService itemService;

    @Test
    public void addAuthorityControllerMetadata() throws Exception {
        context.turnOffAuthorisationSystem();
        parentCommunity = CommunityBuilder.createCommunity(context)
                                          .withName("Parent Community")
                                          .build();

        Collection col1 = CollectionBuilder.createCollection(context, parentCommunity)
                                           .withEntityType("Publication")
                                           .withName("Collection 1")
                                           .build();

        Item itemA = ItemBuilder.createItem(context, col1)
                                .withTitle("Public item A")
                                .withIssueDate("2022-02-01")
                                .build();

        itemService.addMetadata(context, itemA, "dc", "contributor", "author", null, "Smith, Maria");

        String metadataValue = itemService.getMetadataFirstValue(itemA, "dc", "contributor", "author", null);

        Assert.assertEquals("Smith, Maria", metadataValue);
    }

    @Test
    public void addNonAuthorityControlledMetadata() throws Exception {
        context.turnOffAuthorisationSystem();
        parentCommunity = CommunityBuilder.createCommunity(context)
                .withName("Parent Community")
                .build();

        Collection col1 = CollectionBuilder.createCollection(context, parentCommunity)
                .withEntityType("Publication")
                .withName("Collection 1")
                .build();

        Item itemA = ItemBuilder.createItem(context, col1)
                .withAuthor("Smith, Maria")
                .withIssueDate("2022-02-01")
                .build();

        itemService.addMetadata(context, itemA, "dc", "title", null, null, "Public Item A");

        String metadataValue = itemService.getMetadataFirstValue(itemA, "dc", "title", null, null);

        Assert.assertEquals("Public Item A", metadataValue);
    }

    @Test
    public void addNonAuthorityControlledMetadataWithAuthority() throws Exception {
        context.turnOffAuthorisationSystem();
        parentCommunity = CommunityBuilder.createCommunity(context)
                .withName("Parent Community")
                .build();

        Collection col1 = CollectionBuilder.createCollection(context, parentCommunity)
                .withEntityType("Publication")
                .withName("Collection 1")
                .build();

        Item itemA = ItemBuilder.createItem(context, col1)
                .withAuthor("Smith, Maria")
                .withIssueDate("2022-02-01")
                .build();

        Throwable throwable = Assert.assertThrows(IllegalArgumentException.class,
                            () -> itemService
                                    .addMetadata(context, itemA, "dc", "title",null, null,
                                            Arrays.asList("Public Item A"), Arrays.asList("test_authority"), null));

        Assert.assertEquals("The metadata field \"dc_title\" is not authority controlled but authorities were provided. Values:\"[test_authority]\"", throwable.getMessage());
    }

}
