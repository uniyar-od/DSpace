/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import org.dspace.app.rest.builder.CollectionBuilder;
import org.dspace.app.rest.builder.CommunityBuilder;
import org.dspace.app.rest.builder.EPersonBuilder;
import org.dspace.app.rest.builder.ItemBuilder;
import org.dspace.app.rest.builder.ResourcePolicyBuilder;
import org.dspace.app.rest.matcher.CollectionMatcher;
import org.dspace.app.rest.matcher.CommunityMatcher;
import org.dspace.app.rest.matcher.ItemMatcher;
import org.dspace.app.rest.test.AbstractControllerIntegrationTest;
import org.dspace.authorize.ResourcePolicy;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.Item;
import org.dspace.core.Constants;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;
import org.dspace.eperson.factory.EPersonServiceFactory;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Integration test that check access to subPath
 *
 * @author Andrea Bollini (andrea.bollini at 4science.it)
 */
public class TraverseRestControllerIT extends AbstractControllerIntegrationTest {

    @Autowired
    AuthorizeService authorizeService;
    
    @Test
    public void testTraverseItem() throws Exception {
        context.turnOffAuthorisationSystem();
        Community com = CommunityBuilder.createCommunity(context).withName("A community").build();
        Collection col = CollectionBuilder.createCollection(context, com).withName("Hidden collection").build();
        // make the collection visible only to administrators
        authorizeService.removePoliciesActionFilter(context, col, Constants.READ);
        // this item will get default anonymous READ
        Item item = ItemBuilder.createItem(context, col).withTitle("My item in the hidden collection").withIssueDate("2020-02-11").build();
        context.restoreAuthSystemState();
        
        // proof that we can access the item
        getClient().perform(get("/api/core/items/" + item.getID().toString()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", Matchers.is(
                                ItemMatcher.matchItemWithTitleAndDateIssued(item,
                                        "My item in the hidden collection", "2020-02-11"))));

        // proof that we cannot access the collection
        getClient().perform(get("/api/core/collections/" + col.getID().toString()))
            .andExpect(status().isUnauthorized());
        
        // but what happen if we try to traverse the item
        getClient().perform(get("/api/core/items/" + item.getID().toString() + "/owningCollection"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$", Matchers.not(is(
                    CollectionMatcher.matchCollectionEntry(col.getName(), col.getID(), col.getHandle())
                ))));
    }

    @Test
    public void testSevereTraverseItem() throws Exception {
        context.turnOffAuthorisationSystem();
        Community com = CommunityBuilder.createCommunity(context).withName("A community").build();
        Collection hiddenCol = CollectionBuilder.createCollection(context, com).withName("Hidden collection").build();
        
        // make the collection visible only to administrators
        authorizeService.removePoliciesActionFilter(context, hiddenCol, Constants.READ);
        authorizeService.removePoliciesActionFilter(context, hiddenCol, Constants.DEFAULT_ITEM_READ);
        authorizeService.addPolicy(context, hiddenCol, Constants.DEFAULT_ITEM_READ, EPersonServiceFactory.getInstance().getGroupService().findByName(context, Group.ADMIN));
        
        Collection publicCol = CollectionBuilder.createCollection(context, com).withName("Public collection").build();
        authorizeService.removePoliciesActionFilter(context, publicCol, Constants.DEFAULT_ITEM_READ);
        authorizeService.addPolicy(context, publicCol, Constants.DEFAULT_ITEM_READ, EPersonServiceFactory.getInstance().getGroupService().findByName(context, Group.ADMIN));
        
        // these items will get default admin READ (they will be not public)
        Item hiddenItem = ItemBuilder.createItem(context, hiddenCol).withTitle("My hidden item in the hidden collection").withIssueDate("2020-02-11").build();
        Item hiddenItemInPublicCol = ItemBuilder.createItem(context, publicCol).withTitle("My hidden item in the public collection").withIssueDate("2020-02-12").build();
        context.restoreAuthSystemState();
        
        // proof that the items exist
        String adminToken = getAuthToken(admin.getEmail(), password);
        getClient(adminToken).perform(get("/api/core/items/" + hiddenItem.getID().toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", Matchers.is(
                ItemMatcher.matchItemWithTitleAndDateIssued(hiddenItem,
                        "My hidden item in the hidden collection", "2020-02-11"))));
        
        getClient(adminToken).perform(get("/api/core/items/" + hiddenItemInPublicCol.getID().toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", Matchers.is(
                ItemMatcher.matchItemWithTitleAndDateIssued(hiddenItemInPublicCol,
                        "My hidden item in the public collection", "2020-02-12"))));
        
        // proof that we cannot access the items anonymously
        getClient().perform(get("/api/core/items/" + hiddenItem.getID().toString()))
                        .andExpect(status().isUnauthorized());
        getClient().perform(get("/api/core/items/" + hiddenItemInPublicCol.getID().toString()))
        .andExpect(status().isUnauthorized());

        
        // proof that we cannot access the hidden collection
        getClient().perform(get("/api/core/collections/" + hiddenCol.getID().toString()))
            .andExpect(status().isUnauthorized());

        // proof that we can access the public collection
        getClient().perform(get("/api/core/collections/" + publicCol.getID().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", is(
                    CollectionMatcher.matchCollectionEntry(publicCol.getName(), publicCol.getID(), publicCol.getHandle())
                )));

        // but what happen if we try to traverse the items
        getClient().perform(get("/api/core/items/" + hiddenItem.getID().toString() + "/owningCollection"))
            .andExpect(status().isUnauthorized());
        getClient().perform(get("/api/core/items/" + hiddenItemInPublicCol.getID().toString() + "/owningCollection"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void testTraverseResourcePolicy() throws Exception {
        context.turnOffAuthorisationSystem();
        Community com = CommunityBuilder.createCommunity(context).withName("A reserved community").build();
        // make the community visible only to administrators
        authorizeService.removePoliciesActionFilter(context, com, Constants.READ);
        // add an ADD policy for the eperson
        ResourcePolicy addPolicy = ResourcePolicyBuilder.createResourcePolicy(context)
                .withDspaceObject(com).withAction(Constants.ADD).withUser(eperson).build();
        context.restoreAuthSystemState();

        String epersonToken = getAuthToken(eperson.getEmail(), password);
        // proof that we cannot access the restricted community
        getClient(epersonToken).perform(get("/api/core/communities/" + com.getID().toString()))
            .andExpect(status().isForbidden());
        // but what happen if we try to traverse the resourcepolicy
        getClient(epersonToken).perform(get("/api/authz/resourcepolicies/" + addPolicy.getID()))
            .andExpect(status().isOk());
        // should the subPath of a visible REST resource be forbidden?
        getClient(epersonToken).perform(get("/api/authz/resourcepolicies/" + addPolicy.getID() + "/resource"))
            .andExpect(status().isForbidden());
    }
    
    @Test
    public void testSevereTraverseResourcePolicy() throws Exception {
        context.turnOffAuthorisationSystem();
        Community com = CommunityBuilder.createCommunity(context).withName("A reserved community").build();
        EPerson anotherUSer = EPersonBuilder.createEPerson(context).withEmail("another@example.com").withPassword(password).build();
        // make the community visible only to administrators
        authorizeService.removePoliciesActionFilter(context, com, Constants.READ);
        // add an ADD policy for the eperson
        ResourcePolicy addPolicy = ResourcePolicyBuilder.createResourcePolicy(context)
                .withDspaceObject(com).withAction(Constants.ADD).withUser(eperson).build();
        context.restoreAuthSystemState();

        String anotherToken = getAuthToken(anotherUSer.getEmail(), password);
        // proof that we cannot access the restricted community
        getClient(anotherToken).perform(get("/api/core/communities/" + com.getID().toString()))
            .andExpect(status().isForbidden());
        // but what happen if we try to traverse the resourcepolicy that we cannot access directly?
        getClient(anotherToken).perform(get("/api/authz/resourcepolicies/" + addPolicy.getID()))
            .andExpect(status().isForbidden());
        getClient(anotherToken).perform(get("/api/authz/resourcepolicies/" + addPolicy.getID() + "/resource"))
            .andExpect(status().isForbidden());
    }        

}
