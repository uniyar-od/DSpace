/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest;

import static org.dspace.app.rest.matcher.WorkflowStepStatisticsMatcher.match;
import static org.dspace.app.rest.matcher.WorkflowStepStatisticsMatcher.matchActionCount;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.dspace.app.rest.repository.WorkflowStepStatisticsRestRepository;
import org.dspace.app.rest.test.AbstractControllerIntegrationTest;
import org.dspace.authorize.AuthorizeException;
import org.dspace.builder.CollectionBuilder;
import org.dspace.builder.CommunityBuilder;
import org.dspace.builder.EPersonBuilder;
import org.dspace.builder.EntityTypeBuilder;
import org.dspace.builder.RelationshipTypeBuilder;
import org.dspace.builder.WorkflowItemBuilder;
import org.dspace.content.Collection;
import org.dspace.content.EntityType;
import org.dspace.eperson.EPerson;
import org.dspace.services.ConfigurationService;
import org.dspace.xmlworkflow.storedcomponents.ClaimedTask;
import org.dspace.xmlworkflow.storedcomponents.PoolTask;
import org.dspace.xmlworkflow.storedcomponents.XmlWorkflowItem;
import org.dspace.xmlworkflow.storedcomponents.service.ClaimedTaskService;
import org.dspace.xmlworkflow.storedcomponents.service.PoolTaskService;
import org.dspace.xmlworkflow.storedcomponents.service.XmlWorkflowItemService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RestMediaTypes;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class WorkflowStepStatisticsRestRepositoryIT extends AbstractControllerIntegrationTest {

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private ClaimedTaskService claimedTaskService;

    @Autowired
    private PoolTaskService poolTaskService;

    @Autowired
    private XmlWorkflowItemService workflowItemService;

    private Collection collection;

    private EPerson user;

    @Before
    public void setup() throws Exception {

        configurationService.setProperty("solr-statistics.autoCommit", false);

        context.turnOffAuthorisationSystem();

        parentCommunity = CommunityBuilder.createCommunity(context)
            .withName("Parent Community")
            .build();

        user = createEPerson("user1@email.com", "First", "User");

        collection = CollectionBuilder.createCollection(context, parentCommunity)
            .withWorkflow("defaultWorkflow")
            .withName("Publications")
            .withEntityType("Publication")
            .withWorkflowGroup("reviewer", user)
            .withWorkflowGroup("editor", user)
            .build();

        EntityType publicationType = EntityTypeBuilder.createEntityTypeBuilder(context, "Publication").build();

        RelationshipTypeBuilder.createRelationshipTypeBuilder(context, publicationType, publicationType,
            "isCorrectionOfItem", "isCorrectedByItem", 0, 1, 0, 1);

        context.setCurrentUser(eperson);

        context.restoreAuthSystemState();

    }

    @After
    public void after() throws SQLException, IOException, AuthorizeException {
        context.turnOffAuthorisationSystem();
        workflowItemService.deleteByCollection(context, collection);
        context.restoreAuthSystemState();
    }

    @Test
    public void testSearchCurrentWorkflows() throws Exception {

        context.turnOffAuthorisationSystem();

        XmlWorkflowItem firstWorkflowItem = createWorkflowItem(collection);
        XmlWorkflowItem secondWorkflowItem = createWorkflowItem(collection);
        XmlWorkflowItem thirdWorkflowItem = createWorkflowItem(collection);

        context.restoreAuthSystemState();

        String adminToken = getAuthToken(admin.getEmail(), password);

        getClient(adminToken).perform(get("/api/statistics/workflowSteps/search/current"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.workflowSteps", hasSize(1)))
            .andExpect(jsonPath("$._embedded.workflowSteps", contains(match("reviewstep", "reviewstep", 3))))
            .andExpect(jsonPath("$._embedded.workflowSteps[0]", matchActionCount("claimaction", 3)));

        ClaimedTask claimedTask = claimTask(firstWorkflowItem, user);

        getClient(adminToken).perform(get("/api/statistics/workflowSteps/search/current"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.workflowSteps", hasSize(1)))
            .andExpect(jsonPath("$._embedded.workflowSteps", contains(match("reviewstep", "reviewstep", 3))))
            .andExpect(jsonPath("$._embedded.workflowSteps[0]", allOf(
                matchActionCount("claimaction", 2),
                matchActionCount("reviewaction", 1))));

        approveClaimedTaskViaRest(user, claimedTask);

        getClient(adminToken).perform(get("/api/statistics/workflowSteps/search/current"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.workflowSteps", hasSize(2)))
            .andExpect(jsonPath("$._embedded.workflowSteps", contains(
                match("reviewstep", "reviewstep", 2),
                match("editstep", "editstep", 1))))
            .andExpect(jsonPath("$._embedded.workflowSteps[0]", matchActionCount("claimaction", 2)))
            .andExpect(jsonPath("$._embedded.workflowSteps[1]", matchActionCount("claimaction", 1)));

        claimTaskAndReject(secondWorkflowItem, user, "Bad item");

        getClient(adminToken).perform(get("/api/statistics/workflowSteps/search/current"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.workflowSteps", hasSize(2)))
            .andExpect(jsonPath("$._embedded.workflowSteps", containsInAnyOrder(
                match("reviewstep", "reviewstep", 1),
                match("editstep", "editstep", 1))))
            .andExpect(jsonPath("$._embedded.workflowSteps[0]", matchActionCount("claimaction", 1)))
            .andExpect(jsonPath("$._embedded.workflowSteps[1]", matchActionCount("claimaction", 1)));

        claimTaskAndApprove(thirdWorkflowItem, user);

        getClient(adminToken).perform(get("/api/statistics/workflowSteps/search/current"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.workflowSteps", hasSize(1)))
            .andExpect(jsonPath("$._embedded.workflowSteps", contains(match("editstep", "editstep", 2))))
            .andExpect(jsonPath("$._embedded.workflowSteps[0]", matchActionCount("claimaction", 2)));

        claimedTask = claimTask(firstWorkflowItem, user);

        getClient(adminToken).perform(get("/api/statistics/workflowSteps/search/current"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.workflowSteps", hasSize(1)))
            .andExpect(jsonPath("$._embedded.workflowSteps", contains(match("editstep", "editstep", 2))))
            .andExpect(jsonPath("$._embedded.workflowSteps[0]", allOf(
                matchActionCount("claimaction", 1),
                matchActionCount("editaction", 1))));

        rejectClaimedTaskViaRest(user, claimedTask, "Bad item");

        getClient(adminToken).perform(get("/api/statistics/workflowSteps/search/current"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.workflowSteps", hasSize(1)))
            .andExpect(jsonPath("$._embedded.workflowSteps", contains(match("editstep", "editstep", 1))))
            .andExpect(jsonPath("$._embedded.workflowSteps[0]", matchActionCount("claimaction", 1)));

        claimTaskAndApprove(thirdWorkflowItem, user);

        getClient(adminToken).perform(get("/api/statistics/workflowSteps/search/current"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.workflowSteps").doesNotExist());

    }

    @Test
    public void testSearchCurrentWorkflowsWithSizeParameter() throws Exception {

        context.turnOffAuthorisationSystem();

        XmlWorkflowItem firstWorkflowItem = createWorkflowItem(collection);
        XmlWorkflowItem secondWorkflowItem = createWorkflowItem(collection);
        XmlWorkflowItem thirdWorkflowItem = createWorkflowItem(collection);

        claimTaskAndApprove(firstWorkflowItem, user);

        claimTaskAndApprove(thirdWorkflowItem, user);
        claimTask(thirdWorkflowItem, user);

        claimTask(secondWorkflowItem, user);

        context.restoreAuthSystemState();

        String adminToken = getAuthToken(admin.getEmail(), password);

        getClient(adminToken).perform(get("/api/statistics/workflowSteps/search/current")
            .param("size", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.workflowSteps", hasSize(1)))
            .andExpect(jsonPath("$._embedded.workflowSteps", contains(match("editstep", "editstep", 2))))
            .andExpect(jsonPath("$._embedded.workflowSteps[0]", allOf(
                matchActionCount("claimaction", 1),
                matchActionCount("editaction", 1))));

    }

    private String formatDate(Date date) {
        return WorkflowStepStatisticsRestRepository.DATE_FORMATTER.format(date);
    }

    private void claimTaskAndApprove(XmlWorkflowItem workflowItem, EPerson user) throws Exception {
        ClaimedTask claimedTask = claimTask(workflowItem, user);
        approveClaimedTaskViaRest(user, claimedTask);
    }

    private void claimTaskAndReject(XmlWorkflowItem workflowItem, EPerson user, String reason) throws Exception {
        ClaimedTask claimedTask = claimTask(workflowItem, user);
        rejectClaimedTaskViaRest(user, claimedTask, reason);
    }

    private ClaimedTask claimTask(XmlWorkflowItem workflowItem, EPerson user)
        throws SQLException, Exception {
        List<PoolTask> poolTasks = poolTaskService.find(context, workflowItem);
        assertThat(poolTasks, hasSize(1));

        PoolTask poolTask = poolTasks.get(0);
        performActionOnPoolTaskViaRest(user, poolTask);

        ClaimedTask claimedTask = claimedTaskService.findByWorkflowIdAndEPerson(context, workflowItem, user);
        assertThat(claimedTask, notNullValue());

        return claimedTask;
    }

    private void rejectClaimedTaskViaRest(EPerson user, ClaimedTask task, String reason) throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add("submit_reject", "submit_reject");
        params.add("reason", reason);
        performActionOnClaimedTaskViaRest(user, task, params);
    }

    private void approveClaimedTaskViaRest(EPerson user, ClaimedTask task) throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add("submit_approve", "submit_approve");
        performActionOnClaimedTaskViaRest(user, task, params);
    }

    private void performActionOnClaimedTaskViaRest(EPerson user, ClaimedTask task, MultiValueMap<String, String> params)
        throws Exception {

        getClient(getAuthToken(user.getEmail(), password))
            .perform(post(BASE_REST_SERVER_URL + "/api/workflow/claimedtasks/{id}", task.getID()).params(params)
                .contentType("application/x-www-form-urlencoded"))
            .andExpect(status().isNoContent());

    }

    private void performActionOnPoolTaskViaRest(EPerson user, PoolTask task) throws Exception {
        getClient(getAuthToken(user.getEmail(), password)).perform(post("/api/workflow/claimedtasks")
            .contentType(RestMediaTypes.TEXT_URI_LIST)
            .content("/api/workflow/pooltasks/" + task.getID()))
            .andExpect(status().isCreated());
    }

    private XmlWorkflowItem createWorkflowItem(Collection collection) throws IOException {
        return WorkflowItemBuilder.createWorkflowItem(context, collection)
            .withTitle("My Publication")
            .withIssueDate("2017-10-17")
            .withAuthor("Mario Rossi")
            .grantLicense()
            .build();
    }

    private EPerson createEPerson(String email, String firstName, String lastName) throws SQLException {
        return EPersonBuilder.createEPerson(context)
            .withEmail(email)
            .withNameInMetadata(firstName, lastName)
            .withPassword(password)
            .build();
    }
}
