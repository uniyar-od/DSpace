/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.authorize.relationship;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.dspace.AbstractIntegrationTestWithDatabase;
import org.dspace.app.profile.service.ResearcherProfileService;
import org.dspace.builder.CollectionBuilder;
import org.dspace.builder.CommunityBuilder;
import org.dspace.builder.ItemBuilder;
import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.dspace.utils.DSpace;
import org.junit.Before;
import org.junit.Test;

/**
 * Integration tests for {@link RelationshipItemOwnerAuthorizer}.
 *
 * @author Luca Giamminonni (luca.giamminonni at 4science.it)
 *
 */
public class RelationshipItemOwnerAuthorizerIT extends AbstractIntegrationTestWithDatabase {

    private Collection collection;

    private RelationshipItemOwnerAuthorizer authorizer;

    @Before
    public void before() {
        context.turnOffAuthorisationSystem();
        parentCommunity = CommunityBuilder.createCommunity(context)
            .withName("Parent community")
            .build();
        collection = CollectionBuilder.createCollection(context, parentCommunity)
            .withName("Test collection")
            .build();
        context.restoreAuthSystemState();

        authorizer = new RelationshipItemOwnerAuthorizer();
        authorizer.setResearcherProfileService(getResearcherProfileService());
    }

    @Test
    public void testWithoutCurrentUser() {
        context.turnOffAuthorisationSystem();

        Item item = ItemBuilder.createItem(context, collection)
            .withTitle("Test item")
            .build();

        context.restoreAuthSystemState();
        context.setCurrentUser(null);

        assertThat(authorizer.isRelationshipCreatableOnItem(context, item), is(false));
    }

    @Test
    public void testWithAdminUserNotOwner() {
        context.turnOffAuthorisationSystem();

        Item item = ItemBuilder.createItem(context, collection)
            .withTitle("Test item")
            .build();

        context.restoreAuthSystemState();
        context.setCurrentUser(admin);

        assertThat(authorizer.isRelationshipCreatableOnItem(context, item), is(false));
    }

    @Test
    public void testWithNotOwner() {
        context.turnOffAuthorisationSystem();

        Item item = ItemBuilder.createItem(context, collection)
            .withTitle("Test item")
            .build();

        context.restoreAuthSystemState();
        context.setCurrentUser(eperson);

        assertThat(authorizer.isRelationshipCreatableOnItem(context, item), is(false));
    }

    @Test
    public void testWithOwnerUser() {
        context.turnOffAuthorisationSystem();

        Item item = ItemBuilder.createItem(context, collection)
            .withTitle("Test item")
            .withCrisOwner(eperson)
            .build();

        context.restoreAuthSystemState();
        context.setCurrentUser(eperson);

        assertThat(authorizer.isRelationshipCreatableOnItem(context, item), is(true));
    }

    private ResearcherProfileService getResearcherProfileService() {
        return new DSpace().getServiceManager()
            .getServiceByName("org.dspace.app.profile.ResearcherProfileServiceImpl", ResearcherProfileService.class);
    }

}
