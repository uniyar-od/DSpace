/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;

import org.dspace.app.rest.matcher.ItemAuthorityMatcher;
import org.dspace.app.rest.test.AbstractControllerIntegrationTest;
import org.dspace.builder.CollectionBuilder;
import org.dspace.builder.CommunityBuilder;
import org.dspace.builder.EPersonBuilder;
import org.dspace.builder.GroupBuilder;
import org.dspace.builder.ItemBuilder;
import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.dspace.content.authority.service.ChoiceAuthorityService;
import org.dspace.core.service.PluginService;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;
import org.dspace.orcid.client.OrcidClient;
import org.dspace.orcid.factory.OrcidServiceFactory;
import org.dspace.orcid.factory.OrcidServiceFactoryImpl;
import org.dspace.services.ConfigurationService;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.orcid.jaxb.model.v3.release.search.expanded.ExpandedSearch;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class handles ItemAuthority related IT.
 *
 * @author Mykhaylo Boychuk (4Science.it)
 */
public class ItemAuthorityIT extends AbstractControllerIntegrationTest {

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private PluginService pluginService;

    @Autowired
    private ChoiceAuthorityService choiceAuthorityService;

    @Autowired
    private OrcidClient orcidClient;

    private OrcidClient orcidClientMock = mock(OrcidClient.class);

    @Before
    public void setup() {
        ((OrcidServiceFactoryImpl) OrcidServiceFactory.getInstance()).setOrcidClient(orcidClientMock);
        when(orcidClientMock.expandedSearch(any(), anyInt(), anyInt())).thenReturn(new ExpandedSearch());
    }

    @After
    public void after() {
        ((OrcidServiceFactoryImpl) OrcidServiceFactory.getInstance()).setOrcidClient(orcidClient);
    }

    @Test
    public void singleItemAuthorityTest() throws Exception {
        context.turnOffAuthorisationSystem();

        parentCommunity = CommunityBuilder.createCommunity(context).build();
        Collection col1 = CollectionBuilder.createCollection(context, parentCommunity)
                .withName("Test collection")
                .build();

        Item orgUnit_1 = ItemBuilder.createItem(context, col1)
                .withTitle("OrgUnit_1")
                .withEntityType("orgunit")
                .build();

        Item orgUnit_2 = ItemBuilder.createItem(context, col1)
                .withTitle("OrgUnit_2")
                .withEntityType("orgunit")
                .build();

        Item author_1 = ItemBuilder.createItem(context, col1)
                .withTitle("Author 1")
                .withEntityType("person")
                .withPersonMainAffiliation(orgUnit_1.getName(), orgUnit_1.getID().toString())
                .build();

        Item author_2 = ItemBuilder.createItem(context, col1)
                .withTitle("Author 2")
                .withPersonMainAffiliation(orgUnit_1.getName(), orgUnit_1.getID().toString())
                .withEntityType("person")
                .build();

        Item author_3 = ItemBuilder.createItem(context, col1)
                .withTitle("Author 3")
                .withPersonMainAffiliation(orgUnit_2.getName(), orgUnit_2.getID().toString())
                .withEntityType("person")
                .build();

        context.restoreAuthSystemState();

        String token = getAuthToken(eperson.getEmail(), password);
        getClient(token).perform(get("/api/submission/vocabularies/AuthorAuthority/entries")
                        .param("metadata", "dc.contributor.author")
                        .param("collection", col1.getID().toString())
                        .param("filter", "author"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$._embedded.entries", Matchers.containsInAnyOrder(
                            ItemAuthorityMatcher.matchItemAuthorityWithOtherInformations(author_1.getID().toString(),
                                "Author 1", "Author 1", "vocabularyEntry",
                                Map.of("data-oairecerif_author_affiliation", "OrgUnit_1::"
                                    + orgUnit_1.getID(),
                                    "oairecerif_author_affiliation", "OrgUnit_1::"
                                        + orgUnit_1.getID())),
                            ItemAuthorityMatcher.matchItemAuthorityWithOtherInformations(author_2.getID().toString(),
                                "Author 2", "Author 2", "vocabularyEntry",
                                Map.of("data-oairecerif_author_affiliation", "OrgUnit_1::"
                                    + orgUnit_1.getID(),
                                    "oairecerif_author_affiliation", "OrgUnit_1::"
                                        + orgUnit_1.getID())),
                            ItemAuthorityMatcher.matchItemAuthorityWithOtherInformations(author_3.getID().toString(),
                                "Author 3", "Author 3", "vocabularyEntry",
                                Map.of("data-oairecerif_author_affiliation", "OrgUnit_2::"
                                    + orgUnit_2.getID(),
                                    "oairecerif_author_affiliation", "OrgUnit_2::"
                                        + orgUnit_2.getID()))
                        )))
                        .andExpect(jsonPath("$.page.totalElements", Matchers.is(3)));
    }

    @Test
    public void multiItemAuthorityTest() throws Exception {
       context.turnOffAuthorisationSystem();

       configurationService.setProperty("plugin.named.org.dspace.content.authority.ChoiceAuthority",
                new String[] {
                        "org.dspace.content.authority.ItemMultiAuthority = AuthorAuthority",
                        "org.dspace.content.authority.ItemAuthority = OrgUnitAuthority"
                });

       configurationService.setProperty("solr.authority.server", "${solr.server}/authority");
       configurationService.setProperty("choices.plugin.dc.contributor.author", "AuthorAuthority");
       configurationService.setProperty("choices.presentation.dc.contributor.author", "authorLookup");
       configurationService.setProperty("authority.controlled.dc.contributor.author", "true");

       configurationService.setProperty("choices.plugin.person.affiliation.name", "OrgUnitAuthority");
       configurationService.setProperty("choices.presentation.person.affiliation.name", "authorLookup");
       configurationService.setProperty("authority.controlled.person.affiliation.name", "true");

       // These clears have to happen so that the config is actually reloaded in those classes. This is needed for
       // the properties that we're altering above and this is only used within the tests
       pluginService.clearNamedPluginClasses();
       choiceAuthorityService.clearCache();

       parentCommunity = CommunityBuilder.createCommunity(context).build();
       Collection col1 = CollectionBuilder.createCollection(context, parentCommunity).build();

       Item orgUnit_1 = ItemBuilder.createItem(context, col1)
                                   .withTitle("OrgUnit_1")
                                   .withEntityType("orgunit")
                                   .build();

       Item orgUnit_2 = ItemBuilder.createItem(context, col1)
                                   .withTitle("OrgUnit_2")
                                   .withEntityType("orgunit")
                                   .build();

       Item author_1 = ItemBuilder.createItem(context, col1)
                                  .withTitle("Author 1")
                                  .withPersonMainAffiliation(orgUnit_1.getName(), orgUnit_1.getID().toString())
                                  .withPersonMainAffiliation(orgUnit_2.getName(), orgUnit_2.getID().toString())
                                  .withEntityType("person")
                                  .build();

       Item author_2 = ItemBuilder.createItem(context, col1)
                                  .withTitle("Author 2")
                                  .withPersonMainAffiliation(orgUnit_2.getName(), orgUnit_2.getID().toString())
                                  .withEntityType("person")
                                  .build();

       context.restoreAuthSystemState();

       String token = getAuthToken(eperson.getEmail(), password);
       getClient(token).perform(get("/api/submission/vocabularies/AuthorAuthority/entries")
                       .param("metadata", "dc.contributor.author")
                       .param("collection", col1.getID().toString())
                       .param("filter", "author"))
                       .andExpect(status().isOk())
                       .andExpect(jsonPath("$._embedded.entries", Matchers.containsInAnyOrder(
                               // filled with AuthorAuthority extra metadata generator
                               ItemAuthorityMatcher.matchItemAuthorityWithOtherInformations(author_1.getID().toString(),
                               "Author 1(OrgUnit_1)", "Author 1", "vocabularyEntry",
                               Map.of("data-oairecerif_author_affiliation", "OrgUnit_1::" + orgUnit_1.getID(),
                                   "oairecerif_author_affiliation", "OrgUnit_1::" + orgUnit_1.getID())),
                               ItemAuthorityMatcher.matchItemAuthorityWithOtherInformations(author_1.getID().toString(),
                               "Author 1(OrgUnit_2)", "Author 1", "vocabularyEntry",
                               Map.of("data-oairecerif_author_affiliation", "OrgUnit_2::" + orgUnit_2.getID(),
                                   "oairecerif_author_affiliation", "OrgUnit_2::" + orgUnit_2.getID())),
                               ItemAuthorityMatcher.matchItemAuthorityWithOtherInformations(author_2.getID().toString(),
                               "Author 2(OrgUnit_2)", "Author 2", "vocabularyEntry",
                               Map.of("data-oairecerif_author_affiliation", "OrgUnit_2::" + orgUnit_2.getID(),
                                   "oairecerif_author_affiliation", "OrgUnit_2::" + orgUnit_2.getID())),
                               // filled with EditorAuthority extra metadata generator
                               ItemAuthorityMatcher.matchItemAuthorityProperties(author_1.getID().toString(),
                               "Author 1", "Author 1", "vocabularyEntry"),
                               ItemAuthorityMatcher.matchItemAuthorityProperties(author_2.getID().toString(),
                               "Author 2", "Author 2", "vocabularyEntry")
                               )))
                       .andExpect(jsonPath("$.page.totalElements", Matchers.is(5)));
    }

    @Test
    public void singleItemAuthorityWithoutOrgUnitTest() throws Exception {
       context.turnOffAuthorisationSystem();

       parentCommunity = CommunityBuilder.createCommunity(context).build();
       Collection col1 = CollectionBuilder.createCollection(context, parentCommunity)
                                          .withName("Test collection")
                                          .build();

       Item author_1 = ItemBuilder.createItem(context, col1)
                                  .withTitle("Author 1")
                                  .withEntityType("person")
                                  .build();

       context.restoreAuthSystemState();

       String token = getAuthToken(eperson.getEmail(), password);
       getClient(token).perform(get("/api/submission/vocabularies/AuthorAuthority/entries")
                       .param("metadata", "dc.contributor.author")
                       .param("collection", col1.getID().toString())
                       .param("filter", "author"))
                       .andExpect(status().isOk())
                       .andExpect(jsonPath("$._embedded.entries", Matchers.contains(
                           ItemAuthorityMatcher.matchItemAuthorityWithOtherInformations(author_1.getID().toString(),
                                "Author 1", "Author 1", "vocabularyEntry",
                                Map.of("data-oairecerif_author_affiliation", "", "oairecerif_author_affiliation", ""))
                       )))
                       .andExpect(jsonPath("$.page.totalElements", Matchers.is(1)));
    }

    @Test
    public void ePersonAuthorityTest() throws Exception {
       context.turnOffAuthorisationSystem();

       EPerson ePerson1 = EPersonBuilder.createEPerson(context)
                                        .withNameInMetadata("Andrea", "Bollini")
                                        .withEmail("Andrea.Bollini@example.com")
                                        .build();

       EPerson ePerson2 = EPersonBuilder.createEPerson(context)
                                        .withNameInMetadata("Mykhaylo", "Boychuk")
                                        .withEmail("Mykhaylo.Boychuk@example.com")
                                        .build();

       EPerson ePerson3 = EPersonBuilder.createEPerson(context)
                                        .withNameInMetadata("Luca", "Giamminonni")
                                        .withEmail("Luca.Giamminonni@example.com")
                                        .build();

       EPerson ePerson4 = EPersonBuilder.createEPerson(context)
                                        .withNameInMetadata("Andrea", "Pascarelli")
                                        .withEmail("Andrea.Pascarelli@example.com")
                                        .build();

       context.restoreAuthSystemState();

       String token = getAuthToken(admin.getEmail(), password);
       getClient(token).perform(get("/api/submission/vocabularies/EPersonAuthority/entries")
                       .param("filter", "Andrea"))
                       .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.entries", Matchers.containsInAnyOrder(
                                ItemAuthorityMatcher.matchItemAuthorityProperties(ePerson1.getID().toString(),
                                        ePerson1.getFullName(), ePerson1.getFullName(), "vocabularyEntry"),
                                ItemAuthorityMatcher.matchItemAuthorityProperties(ePerson4.getID().toString(),
                                        ePerson4.getFullName(), ePerson4.getFullName(), "vocabularyEntry"))))
                .andExpect(jsonPath("$._embedded.entries", Matchers.not(
                        ItemAuthorityMatcher.matchItemAuthorityProperties(ePerson2.getID().toString(),
                                ePerson2.getFullName(), ePerson2.getFullName(), "vocabularyEntry"))))
                .andExpect(jsonPath("$._embedded.entries", Matchers.not(
                        ItemAuthorityMatcher.matchItemAuthorityProperties(ePerson3.getID().toString(),
                                ePerson3.getFullName(), ePerson3.getFullName(), "vocabularyEntry"))))
                .andExpect(jsonPath("$.page.totalElements", Matchers.is(2)));
    }

    @Test
    public void ePersonAuthorityNoComparisonTest() throws Exception {
       context.turnOffAuthorisationSystem();

       EPersonBuilder.createEPerson(context)
            .withNameInMetadata("Andrea", "Bollini")
            .withEmail("Andrea.Bollini@example.com")
            .build();

       EPersonBuilder.createEPerson(context)
            .withNameInMetadata("Mykhaylo", "Boychuk")
            .withEmail("Mykhaylo.Boychuk@example.com")
            .build();

       context.restoreAuthSystemState();

       String token = getAuthToken(eperson.getEmail(), password);
       getClient(token).perform(get("/api/submission/vocabularies/EPersonAuthority/entries")
                       .param("filter", "wrong text"))
                       .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements", Matchers.is(0)));
    }

    @Test
    public void ePersonAuthorityEmptyQueryTest() throws Exception {
       context.turnOffAuthorisationSystem();

       EPersonBuilder.createEPerson(context)
           .withNameInMetadata("Andrea", "Bollini")
           .withEmail("Andrea.Bollini@example.com")
           .build();

       EPersonBuilder.createEPerson(context)
           .withNameInMetadata("Mykhaylo", "Boychuk")
           .withEmail("Mykhaylo.Boychuk@example.com")
           .build();

       context.restoreAuthSystemState();

       String token = getAuthToken(eperson.getEmail(), password);
       getClient(token).perform(get("/api/submission/vocabularies/EPersonAuthority/entries")
                       .param("filter", ""))
                       .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void ePersonAuthorityUnauthorizedTest() throws Exception {

       getClient().perform(get("/api/submission/vocabularies/EPersonAuthority/entries")
                  .param("filter", "wrong text"))
                  .andExpect(status().isUnauthorized());
    }

    @Test
    public void groupAuthorityTest() throws Exception {
       context.turnOffAuthorisationSystem();

       Group simpleGroup = GroupBuilder.createGroup(context)
                                 .withName("Simple Group")
                                 .build();

       Group groupA = GroupBuilder.createGroup(context)
                                  .withName("Group A")
                                  .build();

       Group admins = GroupBuilder.createGroup(context)
                                  .withName("Admins")
                                  .build();

       context.restoreAuthSystemState();

       String token = getAuthToken(eperson.getEmail(), password);
       getClient(token).perform(get("/api/submission/vocabularies/GroupAuthority/entries")
                       .param("filter", "Group"))
                       .andExpect(status().isOk())
                       .andExpect(jsonPath("$._embedded.entries", Matchers.containsInAnyOrder(
                               ItemAuthorityMatcher.matchItemAuthorityProperties(simpleGroup.getID().toString(),
                                       simpleGroup.getName(), simpleGroup.getName(), "vocabularyEntry"),
                               ItemAuthorityMatcher.matchItemAuthorityProperties(groupA.getID().toString(),
                                       groupA.getName(), groupA.getName(), "vocabularyEntry"))))
                       .andExpect(jsonPath("$._embedded.entries", Matchers.not(ItemAuthorityMatcher
                               .matchItemAuthorityProperties(admins.getID().toString(),admins.getName(),
                                                             admins.getName(), "vocabularyEntry"))))
                       .andExpect(jsonPath("$.page.totalElements", Matchers.is(2)));
    }

    @Test
    public void groupAuthorityEmptyQueryTest() throws Exception {
       context.turnOffAuthorisationSystem();

       GroupBuilder.createGroup(context)
           .withName("Simple Group")
           .build();

       GroupBuilder.createGroup(context)
           .withName("Group A")
           .build();

       context.restoreAuthSystemState();

       String token = getAuthToken(eperson.getEmail(), password);
       getClient(token).perform(get("/api/submission/vocabularies/GroupAuthority/entries")
                       .param("filter", ""))
                       .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void groupAuthorityUnauthorizedTest() throws Exception {

       getClient().perform(get("/api/submission/vocabularies/GroupAuthority/entries")
                  .param("filter", "wrong text"))
                  .andExpect(status().isUnauthorized());
    }

    @Test
    public void itemAuthorityWithValidExternalSourceTest() throws Exception {
        Map<String, String> exptectedMap = new HashMap<String, String>(
                Map.of("dc.contributor.author", "authorAuthority"));
       context.turnOffAuthorisationSystem();

       configurationService.setProperty("choises.externalsource.dc.contributor.author", "authorAuthority");

       // These clears have to happen so that the config is actually reloaded in those classes. This is needed for
       // the properties that we're altering above and this is only used within the tests
       pluginService.clearNamedPluginClasses();
       choiceAuthorityService.clearCache();

       context.restoreAuthSystemState();

       String token = getAuthToken(eperson.getEmail(), password);
       getClient(token).perform(get("/api/submission/vocabularies/AuthorAuthority"))
                       .andExpect(status().isOk())
                       .andExpect(jsonPath("$.entity", Matchers.is("Person")))
                       .andExpect(jsonPath("$.externalSource", Matchers.is(exptectedMap)));
    }

    @Test
    public void itemAuthorityWithNotValidExternalSourceTest() throws Exception {
        Map<String, String> exptectedMap = new HashMap<String, String>();
       context.turnOffAuthorisationSystem();

       configurationService.setProperty("choises.externalsource.dc.contributor.author", "fakeAuthorAuthority");

       // These clears have to happen so that the config is actually reloaded in those classes. This is needed for
       // the properties that we're altering above and this is only used within the tests
       pluginService.clearNamedPluginClasses();
       choiceAuthorityService.clearCache();

       context.restoreAuthSystemState();

       String token = getAuthToken(eperson.getEmail(), password);
       getClient(token).perform(get("/api/submission/vocabularies/AuthorAuthority"))
                       .andExpect(status().isOk())
                       .andExpect(jsonPath("$.entity", Matchers.is("Person")))
                       .andExpect(jsonPath("$.externalSource", Matchers.is(exptectedMap)));
    }

    @Test
    public void entityTypeAuthorityFilters() throws Exception {
        context.turnOffAuthorisationSystem();

        configurationService.setProperty("plugin.named.org.dspace.content.authority.ChoiceAuthority",
            new String[] { "org.dspace.content.authority.ItemAuthority = PersonAuthority" });

        configurationService.setProperty("choices.presentation.dc.contributor.author", "suggest");
        configurationService.setProperty("authority.controlled.dc.contributor.author", "true");

        configurationService.setProperty("cris.ItemAuthority.PersonAuthority.entityType", "EntityPerson");

        // These clears have to happen so that the config is actually reloaded in those
        // classes. This is needed for
        // the properties that we're altering above and this is only used within the
        // tests
        pluginService.clearNamedPluginClasses();
        choiceAuthorityService.clearCache();

        parentCommunity = CommunityBuilder.createCommunity(context).build();
        Collection col1 = CollectionBuilder.createCollection(context, parentCommunity).build();

        Item person1 = ItemBuilder.createItem(context, col1)
            .withTitle("Person 1")
            .withType("mytype")
            .withEntityType("EntityPerson").build();

        ItemBuilder.createItem(context, col1)
            .withTitle("Person 2")
            .withEntityType("EntityPerson")
            .build();

        ItemBuilder.createItem(context, col1)
            .withTitle("Person 3")
            .withType("anotherType")
            .withEntityType("EntityPerson").build();

        context.restoreAuthSystemState();

        String token = getAuthToken(eperson.getEmail(), password);
        getClient(token)
            .perform(get("/api/submission/vocabularies/PersonAuthority/entries").param("filter", "Person"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.page.totalElements", Matchers.is(1)))
            .andExpect(jsonPath("$._embedded.entries",
                Matchers.containsInAnyOrder(ItemAuthorityMatcher.matchItemAuthorityProperties(
                    person1.getID().toString(), "Person 1", "Person 1", "vocabularyEntry"))));

    }

    @Test
    public void authorityNameAuthorityFilters() throws Exception {
        context.turnOffAuthorisationSystem();

        configurationService.setProperty("plugin.named.org.dspace.content.authority.ChoiceAuthority",
            new String[] { "org.dspace.content.authority.ItemAuthority = EntityPersonAuthority",
                "org.dspace.content.authority.ItemAuthority = PersonAuthority" });

        configurationService.setProperty("choices.plugin.dc.contributor.author", "EntityPersonAuthority");
        configurationService.setProperty("choices.presentation.dc.contributor.author", "suggest");
        configurationService.setProperty("authority.controlled.dc.contributor.author", "true");
        configurationService.setProperty("cris.ItemAuthority.EntityPersonAuthority.entityType", "Person");
        configurationService.setProperty("cris.ItemAuthority.PersonAuthority.entityType", "Person");

        // These clears have to happen so that the config is actually reloaded in those
        // classes. This is needed for
        // the properties that we're altering above and this is only used within the
        // tests
        pluginService.clearNamedPluginClasses();
        choiceAuthorityService.clearCache();

        parentCommunity = CommunityBuilder.createCommunity(context).build();
        Collection col1 = CollectionBuilder.createCollection(context, parentCommunity).build();

        Item person1 = ItemBuilder.createItem(context, col1)
            .withTitle("Author 1")
            .withType("mytype")
            .withEntityType("Person").build();

        Item person2 = ItemBuilder.createItem(context, col1)
            .withTitle("Author 2")
            .withEntityType("Person")
            .build();

        Item person3 = ItemBuilder.createItem(context, col1)
            .withTitle("Author 3")
            .withType("anotherType")
            .withEntityType("Person").build();

        context.restoreAuthSystemState();

        String token = getAuthToken(eperson.getEmail(), password);
        getClient(token)
            .perform(get("/api/submission/vocabularies/EntityPersonAuthority/entries").param("filter", "Author"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.page.totalElements", Matchers.is(1)))
            .andExpect(jsonPath("$._embedded.entries",
                Matchers.containsInAnyOrder(ItemAuthorityMatcher.matchItemAuthorityProperties(
                    person1.getID().toString(), "Author 1", "Author 1", "vocabularyEntry"))));

        getClient(token).perform(get("/api/submission/vocabularies/PersonAuthority/entries").param("filter", "Author"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.page.totalElements", Matchers.is(3)))
            .andExpect(jsonPath("$._embedded.entries",
                Matchers.containsInAnyOrder(
                    ItemAuthorityMatcher.matchItemAuthorityProperties(person1.getID().toString(),
                        "Author 1", "Author 1", "vocabularyEntry"),
                    ItemAuthorityMatcher.matchItemAuthorityProperties(person2.getID().toString(),
                        "Author 2", "Author 2", "vocabularyEntry"),
                    ItemAuthorityMatcher.matchItemAuthorityProperties(person3.getID().toString(),
                        "Author 3", "Author 3", "vocabularyEntry"))));

    }

    @Override
    @After
    // We need to cleanup the authorities cache once than the configuration has been restored
    public void destroy() throws Exception {
        super.destroy();
        pluginService.clearNamedPluginClasses();
        choiceAuthorityService.clearCache();
    }
}
