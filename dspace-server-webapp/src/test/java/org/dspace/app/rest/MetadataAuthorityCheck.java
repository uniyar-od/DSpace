/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.app.rest;

import org.dspace.app.rest.test.AbstractControllerIntegrationTest;
import org.dspace.builder.CollectionBuilder;
import org.dspace.builder.CommunityBuilder;
import org.dspace.builder.ItemBuilder;
import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.dspace.content.service.ItemService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;

/*
 * @author Jurgen Mamani
 */
public class MetadataAuthorityCheck extends AbstractControllerIntegrationTest {

    @Autowired
    private ItemService itemService;

    private Item item;

    @Before
    public void setup() {
        context.turnOffAuthorisationSystem();

        parentCommunity = CommunityBuilder.createCommunity(context)
                .withName("Parent Community")
                .build();

        Collection col1 = CollectionBuilder.createCollection(context, parentCommunity)
                .withEntityType("Publication")
                .withName("Collection 1")
                .build();

        item = ItemBuilder.createItem(context, col1)
                .withIssueDate("2022-02-01")
                .build();

        context.restoreAuthSystemState();
    }

    @Test
    public void addAuthorityControlledMetadataWithAuthorities() throws Exception {
        itemService.addMetadata(context, item, "dc", "contributor", "author", null, "Smith, Maria");
        String metadataValue = itemService.getMetadataFirstValue(item, "dc", "contributor", "author", null);
        assertThat(metadataValue, is("Smith, Maria"));
    }

    @Test
    public void addAuthorityControlledMetadataWithoutAuthorities() throws Exception {
        itemService.addMetadata(context, item, "dc", "contributor","author", null,
                List.of("Smith, Maria"), null, null);
        String metadataValue = itemService.getMetadataFirstValue(item, "dc", "contributor", "author", null);
        assertThat(metadataValue, is("Smith, Maria"));
    }

    @Test
    public void addNonAuthorityControlledMetadataWithAuthorities() throws Exception {
        Throwable throwable = assertThrows(IllegalArgumentException.class,
                () -> itemService.addMetadata(context, item, "dc", "title",null, null,
                        List.of("Public Item A"), List.of("test_authority"), null));

        assertThat(throwable.getMessage(),
                is("The metadata field \"dc_title\" is not authority controlled but authorities were provided. Values:\"[test_authority]\""));
    }

    @Test
    public void addNonAuthorityControlledMetadataWithoutAuthorities() throws Exception {
        itemService.addMetadata(context, item, "dc", "title", null, null, "Public Item A");
        String metadataValue = itemService.getMetadataFirstValue(item, "dc", "title", null, null);
        assertThat(metadataValue, is("Public Item A"));
    }

}
