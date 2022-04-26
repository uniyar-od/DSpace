/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.dspace.app.rest.matcher.VocabularyEntryDetailsMatcher;
import org.dspace.app.rest.test.AbstractControllerIntegrationTest;
import org.dspace.builder.CollectionBuilder;
import org.dspace.builder.CommunityBuilder;
import org.dspace.builder.ItemBuilder;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.Item;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * 
 * @author Mykhaylo Boychuk (4science.it)
 */
public class VocabularyEntryDetailsIT extends AbstractControllerIntegrationTest {

    private Community community;

    private Collection collection;

    @Before
    public void setup() {
        context.turnOffAuthorisationSystem();

        community = CommunityBuilder.createCommunity(context)
            .withName("Test community")
            .build();

        collection = CollectionBuilder
            .createCollection(context, community)
            .withName("Test collection")
            .withEntityType("OrgUnit")
            .build();

        context.restoreAuthSystemState();
    }

    @Test
    public void discoverableNestedLinkTest() throws Exception {
        String token = getAuthToken(eperson.getEmail(), password);
        getClient(token).perform(get("/api"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$._links",Matchers.allOf(
                                hasJsonPath("$.vocabularyEntryDetails.href",
                                         is("http://localhost/api/submission/vocabularyEntryDetails")),
                                hasJsonPath("$.vocabularyEntryDetails-search.href",
                                         is("http://localhost/api/submission/vocabularyEntryDetails/search"))
                        )));
    }

    @Test
    public void findAllTest() throws Exception {
        String authToken = getAuthToken(admin.getEmail(), password);
        getClient(authToken).perform(get("/api/submission/vocabularyEntryDetails"))
                            .andExpect(status()
                            .isMethodNotAllowed());
    }

    @Test
    public void findOneTest() throws Exception {
        String idAuthority = "srsc:SCB110";
        String token = getAuthToken(eperson.getEmail(), password);
        getClient(token).perform(get("/api/submission/vocabularyEntryDetails/" + idAuthority))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$",
                                VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:SCB110", "Religion/Theology",
                                        "Research Subject Categories::HUMANITIES and RELIGION::Religion/Theology")))
                        .andExpect(jsonPath("$.selectable", is(true)))
                        .andExpect(jsonPath("$.otherInformation.id", is("SCB110")))
                        .andExpect(jsonPath("$.otherInformation.note", is("Religionsvetenskap/Teologi")))
                        .andExpect(jsonPath("$.otherInformation.parent",
                                is("Research Subject Categories::HUMANITIES and RELIGION")))
                        .andExpect(jsonPath("$._links.parent.href",
                                endsWith("api/submission/vocabularyEntryDetails/srsc:SCB110/parent")))
                        .andExpect(jsonPath("$._links.children.href",
                                endsWith("api/submission/vocabularyEntryDetails/srsc:SCB110/children")));
    }

    @Test
    public void findOneUnauthorizedTest() throws Exception {
        String idAuthority = "srsc:SCB110";
        getClient().perform(get("/api/submission/vocabularyEntryDetails/" + idAuthority))
                   .andExpect(status().isUnauthorized());
    }

    @Test
    public void findOneNotFoundTest() throws Exception {
        String idAuthority = "srsc:not-existing";
        String token = getAuthToken(eperson.getEmail(), password);
        getClient(token).perform(get("/api/submission/vocabularyEntryDetails/" + idAuthority))
                   .andExpect(status().isNotFound());

        // try with a special id missing only the entry-id part
        getClient(token).perform(get("/api/submission/vocabularyEntryDetails/srsc:"))
                   .andExpect(status().isNotFound());

        // try to retrieve the xml root that is not a entry itself
        getClient(token).perform(get("/api/submission/vocabularyEntryDetails/srsc:ResearchSubjectCategories"))
                   .andExpect(status().isNotFound());

    }

    @Test
    public void srscSearchTopTest() throws Exception {
        String tokenAdmin = getAuthToken(admin.getEmail(), password);
        String tokenEPerson = getAuthToken(eperson.getEmail(), password);
        getClient(tokenAdmin).perform(get("/api/submission/vocabularyEntryDetails/search/top")
          .param("vocabulary", "srsc"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$._embedded.vocabularyEntryDetails", Matchers.containsInAnyOrder(
          VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:SCB11", "HUMANITIES and RELIGION",
                  "Research Subject Categories::HUMANITIES and RELIGION"),
          VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:SCB12", "LAW/JURISPRUDENCE",
                  "Research Subject Categories::LAW/JURISPRUDENCE"),
          VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:SCB13", "SOCIAL SCIENCES",
                  "Research Subject Categories::SOCIAL SCIENCES"),
          VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:SCB14", "MATHEMATICS",
                  "Research Subject Categories::MATHEMATICS"),
          VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:SCB15", "NATURAL SCIENCES",
                  "Research Subject Categories::NATURAL SCIENCES"),
          VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:SCB16", "TECHNOLOGY",
                  "Research Subject Categories::TECHNOLOGY"),
          VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:SCB17",
                   "FORESTRY, AGRICULTURAL SCIENCES and LANDSCAPE PLANNING",
                  "Research Subject Categories::FORESTRY, AGRICULTURAL SCIENCES and LANDSCAPE PLANNING"),
          VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:SCB18", "MEDICINE",
                  "Research Subject Categories::MEDICINE"),
          VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:SCB19", "ODONTOLOGY",
                  "Research Subject Categories::ODONTOLOGY"),
          VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:SCB21", "PHARMACY",
                  "Research Subject Categories::PHARMACY"),
          VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:SCB22", "VETERINARY MEDICINE",
                  "Research Subject Categories::VETERINARY MEDICINE"),
          VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:SCB23", "INTERDISCIPLINARY RESEARCH AREAS",
                  "Research Subject Categories::INTERDISCIPLINARY RESEARCH AREAS")
          )))
          .andExpect(jsonPath("$.page.totalElements", Matchers.is(12)));

        getClient(tokenEPerson).perform(get("/api/submission/vocabularyEntryDetails/search/top")
         .param("vocabulary", "srsc"))
         .andExpect(status().isOk())
         .andExpect(jsonPath("$._embedded.vocabularyEntryDetails", Matchers.containsInAnyOrder(
                 VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:SCB11", "HUMANITIES and RELIGION",
                         "Research Subject Categories::HUMANITIES and RELIGION"),
                 VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:SCB12", "LAW/JURISPRUDENCE",
                         "Research Subject Categories::LAW/JURISPRUDENCE"),
                 VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:SCB13", "SOCIAL SCIENCES",
                         "Research Subject Categories::SOCIAL SCIENCES"),
                 VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:SCB14", "MATHEMATICS",
                         "Research Subject Categories::MATHEMATICS"),
                 VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:SCB15", "NATURAL SCIENCES",
                         "Research Subject Categories::NATURAL SCIENCES"),
                 VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:SCB16", "TECHNOLOGY",
                         "Research Subject Categories::TECHNOLOGY"),
                 VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:SCB17",
                          "FORESTRY, AGRICULTURAL SCIENCES and LANDSCAPE PLANNING",
                         "Research Subject Categories::FORESTRY, AGRICULTURAL SCIENCES and LANDSCAPE PLANNING"),
                 VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:SCB18", "MEDICINE",
                         "Research Subject Categories::MEDICINE"),
                 VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:SCB19", "ODONTOLOGY",
                         "Research Subject Categories::ODONTOLOGY"),
                 VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:SCB21", "PHARMACY",
                         "Research Subject Categories::PHARMACY"),
                 VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:SCB22", "VETERINARY MEDICINE",
                         "Research Subject Categories::VETERINARY MEDICINE"),
                 VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:SCB23", "INTERDISCIPLINARY RESEARCH AREAS",
                         "Research Subject Categories::INTERDISCIPLINARY RESEARCH AREAS")
          )))
         .andExpect(jsonPath("$.page.totalElements", Matchers.is(12)));
    }

    @Test
    public void srscSearchFirstLevel_MATHEMATICS_Test() throws Exception {
        String tokenAdmin = getAuthToken(admin.getEmail(), password);
        getClient(tokenAdmin).perform(get("/api/submission/vocabularyEntryDetails/srsc:SCB14/children"))
                 .andExpect(status().isOk())
                 .andExpect(jsonPath("$._embedded.children", Matchers.containsInAnyOrder(
                   VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:SCB1401",
                           "Algebra, geometry and mathematical analysis",
                           "Research Subject Categories::MATHEMATICS::Algebra, geometry and mathematical analysis"),
                   VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:SCB1402", "Applied mathematics",
                           "Research Subject Categories::MATHEMATICS::Applied mathematics"),
                   VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:SCB1409", "Other mathematics",
                           "Research Subject Categories::MATHEMATICS::Other mathematics")
                  )))
                 .andExpect(jsonPath("$._embedded.children[*].otherInformation.parent",
                         Matchers.everyItem(is("Research Subject Categories::MATHEMATICS"))))
                 .andExpect(jsonPath("$.page.totalElements", Matchers.is(3)));
    }

    @Test
    public void srscSearchTopPaginationTest() throws Exception {
        String tokenAdmin = getAuthToken(admin.getEmail(), password);
        getClient(tokenAdmin).perform(get("/api/submission/vocabularyEntryDetails/search/top")
                             .param("vocabulary", "srsc")
                             .param("page", "0")
                             .param("size", "5"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$._embedded.vocabularyEntryDetails", Matchers.containsInAnyOrder(
                  VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:SCB11", "HUMANITIES and RELIGION",
                          "Research Subject Categories::HUMANITIES and RELIGION"),
                  VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:SCB12", "LAW/JURISPRUDENCE",
                          "Research Subject Categories::LAW/JURISPRUDENCE"),
                  VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:SCB13", "SOCIAL SCIENCES",
                          "Research Subject Categories::SOCIAL SCIENCES"),
                  VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:SCB14", "MATHEMATICS",
                          "Research Subject Categories::MATHEMATICS"),
                  VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:SCB15", "NATURAL SCIENCES",
                          "Research Subject Categories::NATURAL SCIENCES")
              )))
          .andExpect(jsonPath("$.page.totalElements", is(12)))
          .andExpect(jsonPath("$.page.totalPages", is(3)))
          .andExpect(jsonPath("$.page.number", is(0)));

        //second page
        getClient(tokenAdmin).perform(get("/api/submission/vocabularyEntryDetails/search/top")
                 .param("vocabulary", "srsc")
                 .param("page", "1")
                 .param("size", "5"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$._embedded.vocabularyEntryDetails", Matchers.containsInAnyOrder(
           VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:SCB16", "TECHNOLOGY",
                   "Research Subject Categories::TECHNOLOGY"),
           VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:SCB17",
                        "FORESTRY, AGRICULTURAL SCIENCES and LANDSCAPE PLANNING",
                   "Research Subject Categories::FORESTRY, AGRICULTURAL SCIENCES and LANDSCAPE PLANNING"),
           VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:SCB18", "MEDICINE",
                   "Research Subject Categories::MEDICINE"),
           VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:SCB19", "ODONTOLOGY",
                   "Research Subject Categories::ODONTOLOGY"),
           VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:SCB21", "PHARMACY",
                   "Research Subject Categories::PHARMACY")
               )))
           .andExpect(jsonPath("$.page.totalElements", is(12)))
           .andExpect(jsonPath("$.page.totalPages", is(3)))
           .andExpect(jsonPath("$.page.number", is(1)));

        // third page
        getClient(tokenAdmin).perform(get("/api/submission/vocabularyEntryDetails/search/top")
                 .param("vocabulary", "srsc")
                 .param("page", "2")
                 .param("size", "5"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$._embedded.vocabularyEntryDetails", Matchers.containsInAnyOrder(
                   VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:SCB22", "VETERINARY MEDICINE",
                           "Research Subject Categories::VETERINARY MEDICINE"),
                   VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:SCB23", "INTERDISCIPLINARY RESEARCH AREAS",
                           "Research Subject Categories::INTERDISCIPLINARY RESEARCH AREAS")
               )))
           .andExpect(jsonPath("$.page.totalElements", is(12)))
           .andExpect(jsonPath("$.page.totalPages", is(3)))
           .andExpect(jsonPath("$.page.number", is(2)));
    }

    @Test
    public void searchTopBadRequestTest() throws Exception {
        String tokenAdmin = getAuthToken(admin.getEmail(), password);
        getClient(tokenAdmin).perform(get("/api/submission/vocabularyEntryDetails/search/top")
                             .param("vocabulary", UUID.randomUUID().toString()))
                             .andExpect(status().isBadRequest());
    }

    @Test
    public void searchTopUnauthorizedTest() throws Exception {
        getClient().perform(get("/api/submission/vocabularyEntryDetails/search/top")
                   .param("vocabulary", "srsc:SCB16"))
                   .andExpect(status().isUnauthorized());
    }

    @Test
    public void srscSearchByParentFirstLevelPaginationTest() throws Exception {
        String token = getAuthToken(eperson.getEmail(), password);
        // first page
        getClient(token).perform(get("/api/submission/vocabularyEntryDetails/srsc:SCB14/children")
                 .param("page", "0")
                 .param("size", "2"))
                 .andExpect(status().isOk())
                 .andExpect(jsonPath("$._embedded.children", Matchers.containsInAnyOrder(
                     VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:SCB1401",
                             "Algebra, geometry and mathematical analysis",
                             "Research Subject Categories::MATHEMATICS::Algebra, geometry and mathematical analysis"),
                     VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:SCB1402", "Applied mathematics",
                             "Research Subject Categories::MATHEMATICS::Applied mathematics")
                     )))
                 .andExpect(jsonPath("$.page.totalElements", is(3)))
                 .andExpect(jsonPath("$.page.totalPages", is(2)))
                 .andExpect(jsonPath("$.page.number", is(0)));

        // second page
        getClient(token).perform(get("/api/submission/vocabularyEntryDetails/srsc:SCB14/children")
                .param("page", "1")
                .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.children", Matchers.contains(
                    VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:SCB1409", "Other mathematics",
                            "Research Subject Categories::MATHEMATICS::Other mathematics")
                    )))
                .andExpect(jsonPath("$.page.totalElements", is(3)))
                .andExpect(jsonPath("$.page.totalPages", is(2)))
                .andExpect(jsonPath("$.page.number", is(1)));
    }

    @Test
    public void srscSearchByParentSecondLevel_Applied_mathematics_Test() throws Exception {
        String token = getAuthToken(eperson.getEmail(), password);
        getClient(token).perform(get("/api/submission/vocabularyEntryDetails/srsc:SCB1402/children"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.children", Matchers.containsInAnyOrder(
              VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:VR140202", "Numerical analysis",
                      "Research Subject Categories::MATHEMATICS::Applied mathematics::Numerical analysis"),
              VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:VR140203", "Mathematical statistics",
                      "Research Subject Categories::MATHEMATICS::Applied mathematics::Mathematical statistics"),
              VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:VR140204", "Optimization, systems theory",
                      "Research Subject Categories::MATHEMATICS::Applied mathematics::Optimization, systems theory"),
              VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:VR140205", "Theoretical computer science",
                      "Research Subject Categories::MATHEMATICS::Applied mathematics::Theoretical computer science")
             )))
            .andExpect(jsonPath("$.page.totalElements", Matchers.is(4)));
    }

    @Test
    public void srscSearchByParentEmptyTest() throws Exception {
        String tokenAdmin = getAuthToken(admin.getEmail(), password);
        getClient(tokenAdmin).perform(get("/api/submission/vocabularyEntryDetails/srsc:VR140202/children"))
                             .andExpect(status().isOk())
                             .andExpect(jsonPath("$.page.totalElements", Matchers.is(0)));
    }

    @Test
    public void srscSearchByParentWrongIdTest() throws Exception {
        String tokenAdmin = getAuthToken(admin.getEmail(), password);
        getClient(tokenAdmin).perform(get("/api/submission/vocabularyEntryDetails/"
                                                          + UUID.randomUUID() + "/children"))
                             .andExpect(status().isBadRequest());
    }

    @Test
    public void srscSearchTopUnauthorizedTest() throws Exception {
        getClient().perform(get("/api/submission/vocabularyEntryDetails/search/top")
                   .param("vocabulary", "srsc"))
                   .andExpect(status().isUnauthorized());
    }

    @Test
    public void findParentByChildTest() throws Exception {
        String tokenEperson = getAuthToken(eperson.getEmail(), password);
        getClient(tokenEperson).perform(get("/api/submission/vocabularyEntryDetails/srsc:SCB180/parent"))
                 .andExpect(status().isOk())
                 .andExpect(jsonPath("$", is(
                         VocabularyEntryDetailsMatcher.matchAuthorityEntry(
                                 "srsc:SCB18", "MEDICINE","Research Subject Categories::MEDICINE")
                  )));
    }

    @Test
    public void findParentByChildBadRequestTest() throws Exception {
        String tokenEperson = getAuthToken(eperson.getEmail(), password);
        getClient(tokenEperson).perform(get("/api/submission/vocabularyEntryDetails/" + UUID.randomUUID() + "/parent"))
                               .andExpect(status().isBadRequest());
    }

    @Test
    public void findParentByChildUnauthorizedTest() throws Exception {
        getClient().perform(get("/api/submission/vocabularyEntryDetails/srsc:SCB180/parent"))
                   .andExpect(status().isUnauthorized());
    }

    @Test
    public void findParentTopTest() throws Exception {
        String tokenEperson = getAuthToken(eperson.getEmail(), password);
        getClient(tokenEperson)
            .perform(get("/api/submission/vocabularyEntryDetails/srsc:SCB11/parent"))
            .andExpect(status().isNoContent());
    }

    @Test
    public void srscProjectionTest() throws Exception {
        String tokenAdmin = getAuthToken(admin.getEmail(), password);
        getClient(tokenAdmin).perform(
                get("/api/submission/vocabularyEntryDetails/srsc:SCB110").param("projection", "full"))
                 .andExpect(status().isOk())
                 .andExpect(jsonPath("$._embedded.parent",
                         VocabularyEntryDetailsMatcher.matchAuthorityEntry(
                                 "srsc:SCB11", "HUMANITIES and RELIGION",
                                 "Research Subject Categories::HUMANITIES and RELIGION")))
                 .andExpect(jsonPath("$._embedded.children._embedded.children", matchAllSrscSC110Children()))
                 .andExpect(jsonPath("$._embedded.children._embedded.children[*].otherInformation.parent",
                         Matchers.everyItem(
                                 is("Research Subject Categories::HUMANITIES and RELIGION::Religion/Theology"))));

        getClient(tokenAdmin).perform(
                get("/api/submission/vocabularyEntryDetails/srsc:SCB110").param("embed", "children"))
                 .andExpect(status().isOk())
                 .andExpect(jsonPath("$._embedded.children._embedded.children", matchAllSrscSC110Children()))
                 .andExpect(jsonPath("$._embedded.children._embedded.children[*].otherInformation.parent",
                         Matchers.everyItem(
                                 is("Research Subject Categories::HUMANITIES and RELIGION::Religion/Theology"))));

        getClient(tokenAdmin).perform(
                get("/api/submission/vocabularyEntryDetails/srsc:SCB110").param("embed", "parent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.parent",
                        VocabularyEntryDetailsMatcher.matchAuthorityEntry(
                                "srsc:SCB11", "HUMANITIES and RELIGION",
                                "Research Subject Categories::HUMANITIES and RELIGION")));
    }

    private Matcher<Iterable<? extends Object>> matchAllSrscSC110Children() {
        return Matchers.containsInAnyOrder(
           VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:VR110102",
                   "History of religion",
                   "Research Subject Categories::HUMANITIES and RELIGION::Religion/Theology::History of religion"),
           VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:VR110103",
                   "Church studies",
                   "Research Subject Categories::HUMANITIES and RELIGION::Religion/Theology::Church studies"),
           VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:VR110104",
                   "Missionary studies",
                   "Research Subject Categories::HUMANITIES and RELIGION::Religion/Theology::Missionary studies"),
           VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:VR110105",
                   "Systematic theology",
                   "Research Subject Categories::HUMANITIES and RELIGION::Religion/Theology::Systematic theology"),
           VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:VR110106",
                   "Islamology",
                   "Research Subject Categories::HUMANITIES and RELIGION::Religion/Theology::Islamology"),
           VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:VR110107",
                   "Faith and reason",
                   "Research Subject Categories::HUMANITIES and RELIGION::Religion/Theology::Faith and reason"),
           VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:VR110108",
                   "Sociology of religion",
                   "Research Subject Categories::HUMANITIES and RELIGION::Religion/Theology::Sociology of religion"),
           VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:VR110109",
                   "Psychology of religion",
                   "Research Subject Categories::HUMANITIES and RELIGION::Religion/Theology::Psychology of religion"),
           VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:VR110110",
                   "Philosophy of religion",
                   "Research Subject Categories::HUMANITIES and RELIGION::Religion/Theology::Philosophy of religion"),
           VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:VR110111",
                   "New Testament exegesis",
                   "Research Subject Categories::HUMANITIES and RELIGION::Religion/Theology::New Testament exegesis"),
           VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:VR110112",
                   "Old Testament exegesis",
                   "Research Subject Categories::HUMANITIES and RELIGION::Religion/Theology::Old Testament exegesis"),
           VocabularyEntryDetailsMatcher.matchAuthorityEntry("srsc:VR110113",
                   "Dogmatics with symbolics",
                   "Research Subject Categories::HUMANITIES and RELIGION::Religion/Theology::Dogmatics with symbolics")
          );
    }

    @Test
    public void itemVocabularyNoParentsTest() throws Exception {
        String tokenAdmin = getAuthToken(admin.getEmail(), password);
        getClient(tokenAdmin).perform(
                get("/api/submission/vocabularyEntryDetails/search/top").param("vocabulary", "orgunits"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.vocabularyEntryDetails").doesNotExist());
    }

    @Test
    public void itemVocabularyWithParentsAndNoChildrenTest() throws Exception {
        context.turnOffAuthorisationSystem();

        Item item1 = ItemBuilder.createItem(context, collection)
            .withTitle("Title1")
            .withDescriptionAbstract("Description1")
            .build();

        Item item2 = ItemBuilder.createItem(context, collection)
            .withTitle("Title2")
            .withDescriptionAbstract("Description2")
            .build();

        context.restoreAuthSystemState();

        String tokenAdmin = getAuthToken(admin.getEmail(), password);
        getClient(tokenAdmin).perform(
            get("/api/submission/vocabularyEntryDetails/search/top").param("vocabulary", "orgunits"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.vocabularyEntryDetails", hasSize(2)))
            .andExpect(jsonPath("$._embedded.vocabularyEntryDetails[0].display", is("Title1")))
            .andExpect(jsonPath("$._embedded.vocabularyEntryDetails[0].value", is(String.valueOf(item1.getID()))))
            .andExpect(jsonPath("$._embedded.vocabularyEntryDetails[0].otherInformation.note",
                                is("Description1")))
            .andExpect(jsonPath("$._embedded.vocabularyEntryDetails[0].otherInformation.display",
                                is("Title1")))
            .andExpect(jsonPath("$._embedded.vocabularyEntryDetails[0].otherInformation.hasChildren",
                                is("false")))
            .andExpect(jsonPath("$._embedded.vocabularyEntryDetails[1].display", is("Title2")))
            .andExpect(jsonPath("$._embedded.vocabularyEntryDetails[1].value", is(String.valueOf(item2.getID()))))
            .andExpect(jsonPath("$._embedded.vocabularyEntryDetails[1].otherInformation.note",
                                is("Description2")))
            .andExpect(jsonPath("$._embedded.vocabularyEntryDetails[1].otherInformation.display",
                                is("Title2")))
            .andExpect(jsonPath("$._embedded.vocabularyEntryDetails[1].otherInformation.hasChildren",
                                is("false")));
    }

    @Test
    public void itemVocabularyWithParentsAndChildrenTest() throws Exception {
        context.turnOffAuthorisationSystem();

        // This item has no children
        Item noChildrenParent = ItemBuilder.createItem(context, collection)
            .withTitle("NoChildrenItem")
            .withDescriptionAbstract("Description0")
            .build();

        Item parent1 = ItemBuilder.createItem(context, collection)
            .withTitle("Parent1")
            .withDescriptionAbstract("Description1")
            .build();

        Item parent2 = ItemBuilder.createItem(context, collection)
            .withTitle("Parent2")
            .withDescriptionAbstract("Description2")
            .build();

        // Create child for parent1
        Item child1 = ItemBuilder.createItem(context, collection)
            .withTitle("Child1")
            .withDescriptionAbstract("ChildDescription1")
            .withParentOrganization("Parent1", parent1.getID().toString())
            .build();

        // Create child for parent2
        Item child2 = ItemBuilder.createItem(context, collection)
            .withTitle("Child2")
            .withDescriptionAbstract("ChildDescription2")
            .withParentOrganization("Parent2", parent2.getID().toString())
            .build();

        context.restoreAuthSystemState();

        String tokenAdmin = getAuthToken(admin.getEmail(), password);
        getClient(tokenAdmin).perform(
                get("/api/submission/vocabularyEntryDetails/search/top").param("vocabulary", "orgunits"))
            .andExpect(status().isOk())
            // No children item
            .andExpect(jsonPath("$._embedded.vocabularyEntryDetails", hasSize(3)))
            .andExpect(jsonPath("$._embedded.vocabularyEntryDetails[0].display", is("NoChildrenItem")))
            .andExpect(jsonPath("$._embedded.vocabularyEntryDetails[0].value",
                                is(String.valueOf(noChildrenParent.getID()))))
            .andExpect(jsonPath("$._embedded.vocabularyEntryDetails[0].otherInformation.note",
                                is("Description0")))
            .andExpect(jsonPath("$._embedded.vocabularyEntryDetails[0].otherInformation.display",
                                is("NoChildrenItem")))
            .andExpect(jsonPath("$._embedded.vocabularyEntryDetails[0].otherInformation.hasChildren",
                                is("false")))
            // First parent
            .andExpect(jsonPath("$._embedded.vocabularyEntryDetails[1].display", is("Parent1")))
            .andExpect(jsonPath("$._embedded.vocabularyEntryDetails[1].value", is(String.valueOf(parent1.getID()))))
            .andExpect(jsonPath("$._embedded.vocabularyEntryDetails[1].otherInformation.note",
                                is("Description1")))
            .andExpect(jsonPath("$._embedded.vocabularyEntryDetails[1].otherInformation.display",
                                is("Parent1")))
            .andExpect(jsonPath("$._embedded.vocabularyEntryDetails[1].otherInformation.hasChildren",
                                is("true")))
            // Second parent
            .andExpect(jsonPath("$._embedded.vocabularyEntryDetails[2].display", is("Parent2")))
            .andExpect(jsonPath("$._embedded.vocabularyEntryDetails[2].value", is(String.valueOf(parent2.getID()))))
            .andExpect(jsonPath("$._embedded.vocabularyEntryDetails[2].otherInformation.note",
                                is("Description2")))
            .andExpect(jsonPath("$._embedded.vocabularyEntryDetails[2].otherInformation.display",
                                is("Parent2")))
            .andExpect(jsonPath("$._embedded.vocabularyEntryDetails[2].otherInformation.hasChildren",
                                is("true")));
    }


    @Test
    public void itemVocabularyWithChildrenTest() throws Exception {
        context.turnOffAuthorisationSystem();

        Item parent1 = ItemBuilder.createItem(context, collection)
            .withTitle("Parent1")
            .withDescriptionAbstract("Description1")
            .build();

        // Create child for parent1
        Item child1 = ItemBuilder.createItem(context, collection)
            .withTitle("Child1")
            .withDescriptionAbstract("ChildDescription1")
            .withParentOrganization("Parent1", parent1.getID().toString())
            .build();

        context.restoreAuthSystemState();

        // Verify that parent is returned and hasChildren is true
        String tokenAdmin = getAuthToken(admin.getEmail(), password);
        getClient(tokenAdmin).perform(
                get("/api/submission/vocabularyEntryDetails/search/top").param("vocabulary", "orgunits"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.vocabularyEntryDetails", hasSize(1)))
            .andExpect(jsonPath("$._embedded.vocabularyEntryDetails[0].display", is("Parent1")))
            .andExpect(jsonPath("$._embedded.vocabularyEntryDetails[0].value", is(String.valueOf(parent1.getID()))))
            .andExpect(jsonPath("$._embedded.vocabularyEntryDetails[0].otherInformation.note",
                                is("Description1")))
            .andExpect(jsonPath("$._embedded.vocabularyEntryDetails[0].otherInformation.display",
                                is("Parent1")))
            .andExpect(jsonPath("$._embedded.vocabularyEntryDetails[0].otherInformation.hasChildren",
                                is("true")));

        // Verify child
        getClient(tokenAdmin).perform(
                get("/api/submission/vocabularyEntryDetails/orgunits:" + parent1.getID() + "/children"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.children", hasSize(1)))
            .andExpect(jsonPath("$._embedded.children[0].display", is("Child1")))
            .andExpect(jsonPath("$._embedded.children[0].value", is(String.valueOf(child1.getID()))))
            .andExpect(jsonPath("$._embedded.children[0].otherInformation.note",
                                is("ChildDescription1")))
            .andExpect(jsonPath("$._embedded.children[0].otherInformation.display",
                                is("Child1")))
            .andExpect(jsonPath("$._embedded.children[0].otherInformation.hasChildren",
                                is("false")));
    }

    @Test
    public void itemVocabularyWithMultipleParentsAndChildrenTest() throws Exception {
        context.turnOffAuthorisationSystem();

        // This item has no children
        Item noChildrenParent = ItemBuilder.createItem(context, collection)
            .withTitle("NoChildrenItem")
            .withDescriptionAbstract("Description0")
            .build();

        Item parent1 = ItemBuilder.createItem(context, collection)
            .withTitle("Parent1")
            .withDescriptionAbstract("Description1")
            .build();

        Item parent2 = ItemBuilder.createItem(context, collection)
            .withTitle("Parent2")
            .withDescriptionAbstract("Description2")
            .build();

        // Create child for parent1
        Item child1 = ItemBuilder.createItem(context, collection)
            .withTitle("Child1")
            .withDescriptionAbstract("ChildDescription1")
            .withParentOrganization("Parent1", parent1.getID().toString())
            .build();

        // Create child for parent2
        Item child2 = ItemBuilder.createItem(context, collection)
            .withTitle("Child2")
            .withDescriptionAbstract("ChildDescription2")
            .withParentOrganization("Parent2", parent2.getID().toString())
            .build();

        context.restoreAuthSystemState();

        String tokenAdmin = getAuthToken(admin.getEmail(), password);
        getClient(tokenAdmin).perform(
                get("/api/submission/vocabularyEntryDetails/search/top").param("vocabulary", "orgunits"))
            .andExpect(status().isOk())
            // No children item
            .andExpect(jsonPath("$._embedded.vocabularyEntryDetails", hasSize(3)))
            .andExpect(jsonPath("$._embedded.vocabularyEntryDetails[0].display", is("NoChildrenItem")))
            .andExpect(jsonPath("$._embedded.vocabularyEntryDetails[0].value",
                                is(String.valueOf(noChildrenParent.getID()))))
            .andExpect(jsonPath("$._embedded.vocabularyEntryDetails[0].otherInformation.note",
                                is("Description0")))
            .andExpect(jsonPath("$._embedded.vocabularyEntryDetails[0].otherInformation.display",
                                is("NoChildrenItem")))
            .andExpect(jsonPath("$._embedded.vocabularyEntryDetails[0].otherInformation.hasChildren",
                                is("false")))
            // First parent
            .andExpect(jsonPath("$._embedded.vocabularyEntryDetails[1].display", is("Parent1")))
            .andExpect(jsonPath("$._embedded.vocabularyEntryDetails[1].value", is(String.valueOf(parent1.getID()))))
            .andExpect(jsonPath("$._embedded.vocabularyEntryDetails[1].otherInformation.note",
                                is("Description1")))
            .andExpect(jsonPath("$._embedded.vocabularyEntryDetails[1].otherInformation.display",
                                is("Parent1")))
            .andExpect(jsonPath("$._embedded.vocabularyEntryDetails[1].otherInformation.hasChildren",
                                is("true")))
            // Second parent
            .andExpect(jsonPath("$._embedded.vocabularyEntryDetails[2].display", is("Parent2")))
            .andExpect(jsonPath("$._embedded.vocabularyEntryDetails[2].value", is(String.valueOf(parent2.getID()))))
            .andExpect(jsonPath("$._embedded.vocabularyEntryDetails[2].otherInformation.note",
                                is("Description2")))
            .andExpect(jsonPath("$._embedded.vocabularyEntryDetails[2].otherInformation.display",
                                is("Parent2")))
            .andExpect(jsonPath("$._embedded.vocabularyEntryDetails[2].otherInformation.hasChildren",
                                is("true")));

        // Verify children
        getClient(tokenAdmin).perform(
                get("/api/submission/vocabularyEntryDetails/orgunits:"
                        + noChildrenParent.getID() + "/children"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.children", hasSize(0)));

        getClient(tokenAdmin).perform(
                get("/api/submission/vocabularyEntryDetails/orgunits:" + parent1.getID() + "/children"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.children", hasSize(1)))
            .andExpect(jsonPath("$._embedded.children[0].display", is("Child1")))
            .andExpect(jsonPath("$._embedded.children[0].value", is(String.valueOf(child1.getID()))))
            .andExpect(jsonPath("$._embedded.children[0].otherInformation.note",
                                is("ChildDescription1")))
            .andExpect(jsonPath("$._embedded.children[0].otherInformation.display",
                                is("Child1")))
            .andExpect(jsonPath("$._embedded.children[0].otherInformation.hasChildren",
                                is("false")));

        getClient(tokenAdmin).perform(
                get("/api/submission/vocabularyEntryDetails/orgunits:" + parent2.getID() + "/children"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.children", hasSize(1)))
            .andExpect(jsonPath("$._embedded.children[0].display", is("Child2")))
            .andExpect(jsonPath("$._embedded.children[0].value", is(String.valueOf(child2.getID()))))
            .andExpect(jsonPath("$._embedded.children[0].otherInformation.note",
                                is("ChildDescription2")))
            .andExpect(jsonPath("$._embedded.children[0].otherInformation.display",
                                is("Child2")))
            .andExpect(jsonPath("$._embedded.children[0].otherInformation.hasChildren",
                                is("false")));
    }

    @Test
    public void itemVocabularyGetSelfTest() throws Exception {
        context.turnOffAuthorisationSystem();

        Item item1 = ItemBuilder.createItem(context, collection)
            .withTitle("Title1")
            .withDescriptionAbstract("Description1")
            .build();

        context.restoreAuthSystemState();

        String tokenAdmin = getAuthToken(admin.getEmail(), password);
        getClient(tokenAdmin).perform(
                get("/api/submission/vocabularyEntryDetails/orgunits:" + item1.getID()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.display", is("Title1")))
            .andExpect(jsonPath("$.value", is(String.valueOf(item1.getID()))))
            .andExpect(jsonPath("$.otherInformation.note", is("Description1")))
            .andExpect(jsonPath("$.otherInformation.display", is("Title1")))
            .andExpect(jsonPath("$.otherInformation.hasChildren", is("false")));
    }

    @Test
    public void itemVocabularyGetParentTest() throws Exception {
        context.turnOffAuthorisationSystem();

        Item parent1 = ItemBuilder.createItem(context, collection)
            .withTitle("Parent1")
            .withDescriptionAbstract("Description1")
            .build();

        // Create child for parent1
        Item child1 = ItemBuilder.createItem(context, collection)
            .withTitle("Child1")
            .withDescriptionAbstract("ChildDescription1")
            .withParentOrganization("Parent1", parent1.getID().toString())
            .build();

        context.restoreAuthSystemState();

        // Get parent of child
        String tokenAdmin = getAuthToken(admin.getEmail(), password);
        getClient(tokenAdmin).perform(
                get("/api/submission/vocabularyEntryDetails/orgunits:" + child1.getID() + "/parent"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.display", is("Parent1")))
            .andExpect(jsonPath("$.value", is(String.valueOf(parent1.getID()))))
            .andExpect(jsonPath("$.otherInformation.note", is("Description1")))
            .andExpect(jsonPath("$.otherInformation.display", is("Parent1")))
            .andExpect(jsonPath("$.otherInformation.hasChildren", is("true")));

        // When item has no parent no content is expected
        getClient(tokenAdmin).perform(
                get("/api/submission/vocabularyEntryDetails/orgunits:" + parent1.getID() + "/parent"))
            .andExpect(status().isNoContent());
    }

    @Test
    public void vocabularyEntryDetailSerchMethodWithSingleModelTest() throws Exception {
        String tokenAdmin = getAuthToken(admin.getEmail(), password);
        getClient(tokenAdmin).perform(get("/api/submission/vocabularyEntryDetail/search"))
                             .andExpect(status().isNotFound());
    }

    @Test
    public void vocabularyEntryDetailSerchMethodWithPluralModelTest() throws Exception {
        String tokenAdmin = getAuthToken(admin.getEmail(), password);
        getClient(tokenAdmin).perform(get("/api/submission/vocabularyEntryDetails/search"))
                             .andExpect(status().isOk())
                             .andExpect(jsonPath("$._links.top.href", Matchers.allOf(
                                 Matchers.containsString("/api/submission/vocabularyEntryDetails/search/top"))));
    }
}
