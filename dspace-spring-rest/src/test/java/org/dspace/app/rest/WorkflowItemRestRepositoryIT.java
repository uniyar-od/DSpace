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

import org.dspace.app.rest.builder.CollectionBuilder;
import org.dspace.app.rest.builder.CommunityBuilder;
import org.dspace.app.rest.builder.WorkflowItemBuilder;
import org.dspace.app.rest.matcher.WorkflowItemMatcher;
import org.dspace.app.rest.test.AbstractControllerIntegrationTest;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.xmlworkflow.storedcomponents.XmlWorkflowItem;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test suite for the WorkflowItem endpoint
 * @author Andrea Bollini (andrea.bollini at 4science.it)
 *
 */
public class WorkflowItemRestRepositoryIT extends AbstractControllerIntegrationTest {

    @Test
    /**
     * All the workflowitems should be returned regardless of the collection where they were created
     * 
     * @throws Exception
     */
    public void findAllTest() throws Exception {
        context.setCurrentUser(admin);

        //** GIVEN **
        //1. A community-collection structure with one parent community with sub-community and two collections.
        parentCommunity = CommunityBuilder.createCommunity(context)
                                          .withName("Parent Community")
                                          .build();
        Community child1 = CommunityBuilder.createSubCommunity(context, parentCommunity)
                                           .withName("Sub Community")
                                           .build();
        Collection col1 = CollectionBuilder.createCollection(context, child1).withName("Collection 1")
                .withWorkflowGroup(1, admin).build();
        Collection col2 = CollectionBuilder.createCollection(context, child1).withName("Collection 2")
                .withWorkflowGroup(1, admin).build();


        //2. Three workflow items in two different collections
        XmlWorkflowItem workflowItem1 = WorkflowItemBuilder.createWorkflowItem(context, col1)
                                      .withTitle("Workflow Item 1")
                                      .withIssueDate("2017-10-17")
                                      .build();

        XmlWorkflowItem workflowItem2 = WorkflowItemBuilder.createWorkflowItem(context, col2)
                                      .withTitle("Workflow Item 2")
                                      .withIssueDate("2016-02-13")
                                      .build();

        XmlWorkflowItem workflowItem3 = WorkflowItemBuilder.createWorkflowItem(context, col2)
                                      .withTitle("Workflow Item 3")
                                      .withIssueDate("2016-02-13")
                                      .build();

        String token = getAuthToken(admin.getEmail(), password);

        getClient(token).perform(get("/api/workflow/workflowitems"))
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$._embedded.workflowitems", Matchers.containsInAnyOrder(
                        WorkflowItemMatcher.matchItemWithTitleAndDateIssued(workflowItem1, "Workflow Item 1",
                                "2017-10-17"),
                        WorkflowItemMatcher.matchItemWithTitleAndDateIssued(workflowItem2, "Workflow Item 2",
                                "2016-02-13"),
                        WorkflowItemMatcher.matchItemWithTitleAndDateIssued(workflowItem3, "Workflow Item 3",
                                "2016-02-13"))))
                   .andExpect(jsonPath("$._links.self.href", Matchers.containsString("/api/workflow/workflowitems")))
                   .andExpect(jsonPath("$.page.size", is(20)))
                   .andExpect(jsonPath("$.page.totalElements", is(3)));
    }

    @Test
    /**
     * The workflowitem endpoint must provide proper pagination
     * 
     * @throws Exception
     */
    public void findAllWithPaginationTest() throws Exception {
        context.turnOffAuthorisationSystem();

        //** GIVEN **
        //1. A community-collection structure with one parent community with sub-community and two collections.
        parentCommunity = CommunityBuilder.createCommunity(context)
                                          .withName("Parent Community")
                                          .build();
        Community child1 = CommunityBuilder.createSubCommunity(context, parentCommunity)
                                           .withName("Sub Community")
                                           .build();
        Collection col1 = CollectionBuilder.createCollection(context, child1).withName("Collection 1")
                .withWorkflowGroup(1, admin).build();
        Collection col2 = CollectionBuilder.createCollection(context, child1).withName("Collection 2")
                .withWorkflowGroup(1, admin).build();

        //2. Three workflow items in two different collections
        XmlWorkflowItem workflowItem1 = WorkflowItemBuilder.createWorkflowItem(context, col1)
                                      .withTitle("Workflow Item 1")
                                      .withIssueDate("2017-10-17")
                                      .build();

        XmlWorkflowItem workflowItem2 = WorkflowItemBuilder.createWorkflowItem(context, col2)
                                      .withTitle("Workflow Item 2")
                                      .withIssueDate("2016-02-13")
                                      .build();

        XmlWorkflowItem workflowItem3 = WorkflowItemBuilder.createWorkflowItem(context, col2)
                                      .withTitle("Workflow Item 3")
                                      .withIssueDate("2016-02-13")
                                      .build();

        String token = getAuthToken(admin.getEmail(), password);

        getClient(token).perform(get("/api/workflow/workflowitems").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.worklowitems",
                        Matchers.containsInAnyOrder(
                                WorkflowItemMatcher.matchItemWithTitleAndDateIssued(workflowItem1, "Workflow Item 1",
                                        "2017-10-17"),
                                WorkflowItemMatcher.matchItemWithTitleAndDateIssued(workflowItem2, "Workflow Item 2",
                                        "2016-02-13"))))
                .andExpect(jsonPath("$._embedded.workflowitems",
                        Matchers.not(Matchers.contains(WorkflowItemMatcher
                                .matchItemWithTitleAndDateIssued(workflowItem3, "Workflow Item 3", "2016-02-13")))));

        getClient(token).perform(get("/api/workflow/workflowitems").param("size", "2").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.workflowitems",
                        Matchers.contains(WorkflowItemMatcher.matchItemWithTitleAndDateIssued(workflowItem3,
                                "Workflow Item 3", "2016-02-13"))))
                .andExpect(jsonPath("$._embedded.workflowitems",
                        Matchers.not(Matchers.contains(
                                WorkflowItemMatcher.matchItemWithTitleAndDateIssued(workflowItem1, "Workflow Item 1",
                                        "2017-10-17"),
                                WorkflowItemMatcher.matchItemWithTitleAndDateIssued(workflowItem2, "Workflow Item 2",
                                        "2016-02-13")))))
                .andExpect(jsonPath("$.page.size", is(2))).andExpect(jsonPath("$.page.totalElements", is(3)));
    }

}
