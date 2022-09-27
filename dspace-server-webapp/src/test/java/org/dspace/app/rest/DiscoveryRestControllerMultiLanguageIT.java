/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Locale;

import org.dspace.app.rest.matcher.FacetValueMatcher;
import org.dspace.app.rest.matcher.PageMatcher;
import org.dspace.app.rest.matcher.SearchResultMatcher;
import org.dspace.app.rest.test.AbstractControllerIntegrationTest;
import org.dspace.builder.CollectionBuilder;
import org.dspace.builder.CommunityBuilder;
import org.dspace.builder.ItemBuilder;
import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.dspace.content.authority.DCInputAuthority;
import org.dspace.content.authority.service.ChoiceAuthorityService;
import org.dspace.content.authority.service.MetadataAuthorityService;
import org.dspace.services.ConfigurationService;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Integration tests for {@link DiscoveryRestController} related to multi
 * language support.
 *
 * @author Luca Giamminonni (luca.giamminonni at 4science.it)
 *
 */
public class DiscoveryRestControllerMultiLanguageIT extends AbstractControllerIntegrationTest {

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private MetadataAuthorityService metadataAuthorityService;

    @Autowired
    private ChoiceAuthorityService choiceAuthorityService;

    @After
    public void after() {
        DSpaceServicesFactory.getInstance().getConfigurationService().reloadConfig();
        metadataAuthorityService.clearCache();
        choiceAuthorityService.clearCache();
        // the DCInputAuthority has an internal cache of the DCInputReader
        DCInputAuthority.reset();
        DCInputAuthority.getPluginNames();
    }

    @Test
    public void discoverSearchByKnowsLanguageTest() throws Exception {
        context.turnOffAuthorisationSystem();

        String[] supportedLanguage = { "it", "uk" };
        configurationService.setProperty("webui.supported.locales", supportedLanguage);
        metadataAuthorityService.clearCache();
        choiceAuthorityService.clearCache();
        // the DCInputAuthority has an internal cache of the DCInputReader
        DCInputAuthority.reset();
        DCInputAuthority.getPluginNames();

        parentCommunity = CommunityBuilder.createCommunity(context)
            .withName("Parent Community")
            .build();

        Collection col1 = CollectionBuilder.createCollection(context, parentCommunity, "123456789/language-test-1")
            .withName("Collection 1")
            .withEntityType("Publication")
            .build();

        Item item1 = ItemBuilder.createItem(context, col1)
            .withTitle("Test 1")
            .withIssueDate("2010-10-17")
            .withAuthor("Testing, Works")
            .withLanguage("it")
            .build();

        Item item2 = ItemBuilder.createItem(context, col1)
            .withTitle("Test 2")
            .withIssueDate("2010-10-17")
            .withAuthor("Testing, Works")
            .withLanguage("uk")
            .build();

        context.restoreAuthSystemState();

        getClient().perform(get("/api/discover/search/objects")
            .param("sort", "dc.date.accessioned, ASC")
            .param("query", "language:(uk)"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type", is("discover")))
            .andExpect(jsonPath("$._embedded.searchResult._embedded.objects", Matchers.hasItem(
                SearchResultMatcher.matchOnItemName("item", "items", item2.getName()))))
            .andExpect(jsonPath("$._embedded.searchResult.page", is(
                PageMatcher.pageEntryWithTotalPagesAndElements(0, 20, 1, 1))))
            .andExpect(jsonPath("$._embedded.searchResult._embedded.objects", Matchers.hasItem(
                SearchResultMatcher.match("core", "item", "items"))));

        getClient().perform(get("/api/discover/search/objects")
            .param("sort", "dc.date.accessioned, ASC")
            .param("query", "language_keyword:\"it\""))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type", is("discover")))
            .andExpect(jsonPath("$._embedded.searchResult._embedded.objects", Matchers.hasItem(
                SearchResultMatcher.matchOnItemName("item", "items", item1.getName()))))
            .andExpect(jsonPath("$._embedded.searchResult.page", is(
                PageMatcher.pageEntryWithTotalPagesAndElements(0, 20, 1, 1))))
            .andExpect(jsonPath("$._embedded.searchResult._embedded.objects", Matchers.hasItem(
                SearchResultMatcher.match("core", "item", "items"))));

        getClient().perform(get("/api/discover/search/objects")
            .param("sort", "dc.date.accessioned, ASC")
            .param("query", "Italiano"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type", is("discover")))
            .andExpect(jsonPath("$._embedded.searchResult._embedded.objects", Matchers.hasItem(
                SearchResultMatcher.matchOnItemName("item", "items", item1.getName()))))
            .andExpect(jsonPath("$._embedded.searchResult.page", is(
                PageMatcher.pageEntryWithTotalPagesAndElements(0, 20, 1, 1))))
            .andExpect(jsonPath("$._embedded.searchResult._embedded.objects", Matchers.hasItem(
                SearchResultMatcher.match("core", "item", "items"))));

        getClient().perform(get("/api/discover/search/objects")
            .param("sort", "dc.date.accessioned, ASC")
            .param("query", "Український"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type", is("discover")))
            .andExpect(jsonPath("$._embedded.searchResult._embedded.objects", Matchers.hasItem(
                SearchResultMatcher.matchOnItemName("item", "items", item2.getName()))))
            .andExpect(jsonPath("$._embedded.searchResult.page", is(
                PageMatcher.pageEntryWithTotalPagesAndElements(0, 20, 1, 1))))
            .andExpect(jsonPath("$._embedded.searchResult._embedded.objects", Matchers.hasItem(
                SearchResultMatcher.match("core", "item", "items"))));

        getClient().perform(get("/api/discover/search/objects")
            .param("sort", "dc.date.accessioned, ASC")
            .param("f.language", "Italiano,equals")
            .header("Accept-Language", Locale.ITALIAN.getLanguage()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type", is("discover")))
            .andExpect(jsonPath("$._embedded.searchResult._embedded.objects", Matchers.hasItem(
                SearchResultMatcher.matchOnItemName("item", "items", item1.getName()))))
            .andExpect(jsonPath("$._embedded.searchResult.page", is(
                PageMatcher.pageEntryWithTotalPagesAndElements(0, 20, 1, 1))))
            .andExpect(jsonPath("$._embedded.searchResult._embedded.objects", Matchers.hasItem(
                SearchResultMatcher.match("core", "item", "items"))));

    }

    @Test
    public void discoverFacetsLanguageTest() throws Exception {
        context.turnOffAuthorisationSystem();

        String[] supportedLanguage = { "it", "uk" };
        configurationService.setProperty("webui.supported.locales", supportedLanguage);
        metadataAuthorityService.clearCache();
        choiceAuthorityService.clearCache();
        // the DCInputAuthority has an internal cache of the DCInputReader
        DCInputAuthority.reset();
        DCInputAuthority.getPluginNames();

        parentCommunity = CommunityBuilder.createCommunity(context)
            .withName("Parent Community")
            .build();

        Collection col1 = CollectionBuilder.createCollection(context, parentCommunity, "123456789/language-test-1")
            .withName("Collection 1")
            .withEntityType("Publication")
            .build();

        ItemBuilder.createItem(context, col1)
            .withTitle("Test 1")
            .withIssueDate("2010-10-17")
            .withAuthor("Testing, Works")
            .withLanguage("it")
            .build();

        ItemBuilder.createItem(context, col1)
            .withTitle("Test 2")
            .withIssueDate("2006-01-23")
            .withAuthor("Smith, Maria")
            .withLanguage("uk")
            .withSubject("TestingForMore")
            .withSubject("ExtraEntry")
            .build();

        context.restoreAuthSystemState();

        getClient().perform(get("/api/discover/facets/language")
            .header("Accept-Language", "uk"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type", is("discover")))
            .andExpect(jsonPath("$.name", is("language")))
            .andExpect(jsonPath("$.facetType", is("text")))
            .andExpect(jsonPath("$._links.self.href", containsString("api/discover/facets/language")))
            .andExpect(jsonPath("$._embedded.values", containsInAnyOrder(
                FacetValueMatcher.entryLanguage("Iталiйська"),
                FacetValueMatcher.entryLanguage("Український"))));

        getClient().perform(get("/api/discover/facets/language")
            .header("Accept-Language", Locale.ITALIAN.getLanguage()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type", is("discover")))
            .andExpect(jsonPath("$.name", is("language")))
            .andExpect(jsonPath("$.facetType", is("text")))
            .andExpect(jsonPath("$._links.self.href", containsString("api/discover/facets/language")))
            .andExpect(jsonPath("$._embedded.values", containsInAnyOrder(
                FacetValueMatcher.entryLanguage("Italiano"),
                FacetValueMatcher.entryLanguage("Ucraino"))));
    }

    @Test
    public void discoverFacetsLanguageWithPrefixTest() throws Exception {
        context.turnOffAuthorisationSystem();

        String[] supportedLanguage = { "it", "uk" };
        configurationService.setProperty("webui.supported.locales", supportedLanguage);
        metadataAuthorityService.clearCache();
        choiceAuthorityService.clearCache();
        // the DCInputAuthority has an internal cache of the DCInputReader
        DCInputAuthority.reset();
        DCInputAuthority.getPluginNames();

        parentCommunity = CommunityBuilder.createCommunity(context)
            .withName("Parent Community")
            .build();

        Collection col1 = CollectionBuilder.createCollection(context, parentCommunity, "123456789/language-test-1")
            .withName("Collection 1")
            .withEntityType("Publication")
            .build();

        ItemBuilder.createItem(context, col1)
            .withTitle("Test 1")
            .withIssueDate("2010-10-17")
            .withAuthor("Testing, Works")
            .withLanguage("it")
            .build();

        ItemBuilder.createItem(context, col1)
            .withTitle("Test 2")
            .withIssueDate("2006-01-23")
            .withAuthor("Smith, Maria")
            .withLanguage("uk")
            .withSubject("TestingForMore")
            .withSubject("ExtraEntry")
            .build();

        context.restoreAuthSystemState();

        getClient().perform(get("/api/discover/facets/language")
            .header("Accept-Language", Locale.ITALIAN.getLanguage())
            .param("prefix", "ucra"))
            .andExpect(jsonPath("$.type", is("discover")))
            .andExpect(jsonPath("$.name", is("language")))
            .andExpect(jsonPath("$.facetType", is("text")))
            .andExpect(jsonPath("$._links.self.href", containsString("api/discover/facets/language")))
            .andExpect(jsonPath("$._embedded.values", containsInAnyOrder(
                FacetValueMatcher.entryLanguage("Ucraino"))));

    }

    @Test
    public void discoverFacetsTypesTest() throws Exception {
        context.turnOffAuthorisationSystem();

        String[] supportedLanguage = { "en","uk", "it" };
        configurationService.setProperty("webui.supported.locales", supportedLanguage);
        metadataAuthorityService.clearCache();
        choiceAuthorityService.clearCache();

        parentCommunity = CommunityBuilder.createCommunity(context)
                                          .withName("Parent Community")
                                          .build();

        Collection col1 = CollectionBuilder.createCollection(context, parentCommunity, "123456789/language-test-1")
                                           .withName("Collection 1")
                                           .withEntityType("Publication")
                                           .build();

        ItemBuilder.createItem(context, col1)
                   .withTitle("Test 1")
                   .withIssueDate("2010-10-17")
                   .withAuthor("Testing, Works")
                   .withType("Research Subject Categories::MATEMATICA", "srsc:SCB14")
                   .build();

        context.restoreAuthSystemState();

        getClient().perform(get("/api/discover/facets/types")
                   .header("Accept-Language", Locale.ITALIAN.getLanguage())
                   .param("configuration", "multilanguage-types")
                   .param("prefix", "matem"))
                   .andExpect(jsonPath("$.type", is("discover")))
                   .andExpect(jsonPath("$.name", is("types")))
                   .andExpect(jsonPath("$.facetType", is("text")))
                   .andExpect(jsonPath("$._links.self.href", containsString("api/discover/facets/types")))
                   .andExpect(jsonPath("$._embedded.values", containsInAnyOrder(
                              FacetValueMatcher.entryTypes("MATEMATICA","srsc:SCB14"))));

        getClient().perform(get("/api/discover/facets/types")
                   .header("Accept-Language", "uk")
                   .param("configuration", "multilanguage-types")
                   .param("prefix", "мат"))
                   .andExpect(jsonPath("$.type", is("discover")))
                   .andExpect(jsonPath("$.name", is("types")))
                   .andExpect(jsonPath("$.facetType", is("text")))
                   .andExpect(jsonPath("$._links.self.href", containsString("api/discover/facets/types")))
                   .andExpect(jsonPath("$._embedded.values", containsInAnyOrder(
                              FacetValueMatcher.entryTypes("МАТЕМАТИКА","srsc:SCB14"))));

    }

    @Test
    public void discoverFacetsTypesTestWithoutAuthority() throws Exception {
        context.turnOffAuthorisationSystem();
        String[] supportedLanguage = { "en","uk", "it" };
        configurationService.setProperty("webui.supported.locales", supportedLanguage);
        metadataAuthorityService.clearCache();
        choiceAuthorityService.clearCache();

        parentCommunity = CommunityBuilder.createCommunity(context)
            .withName("Parent Community")
            .build();

        Collection col1 = CollectionBuilder.createCollection(context, parentCommunity, "123456789/language-test-1")
            .withName("Collection 1")
            .withEntityType("Publication")
            .build();

        ItemBuilder.createItem(context, col1)
            .withTitle("Test 1")
            .withIssueDate("2010-10-17")
            .withAuthor("Testing, Works")
            .withType("Research Subject Categories::MATEMATICA")
            .build();

        context.restoreAuthSystemState();

        getClient().perform(get("/api/discover/facets/types")
            .header("Accept-Language", Locale.ITALIAN.getLanguage())
            .param("configuration", "multilanguage-types")
            .param("prefix", "research"))
            .andExpect(jsonPath("$.type", is("discover")))
            .andExpect(jsonPath("$.name", is("types")))
            .andExpect(jsonPath("$.facetType", is("text")))
            .andExpect(jsonPath("$._links.self.href", containsString("api/discover/facets/types")))
            .andExpect(jsonPath("$._embedded.values", containsInAnyOrder(
                FacetValueMatcher.entryTypes("Research Subject Categories::MATEMATICA"))));

        getClient().perform(get("/api/discover/facets/types")
            .header("Accept-Language", "uk")
            .param("configuration", "multilanguage-types")
            .param("prefix", "research"))
            .andExpect(jsonPath("$.type", is("discover")))
            .andExpect(jsonPath("$.name", is("types")))
            .andExpect(jsonPath("$.facetType", is("text")))
            .andExpect(jsonPath("$._links.self.href", containsString("api/discover/facets/types")))
            .andExpect(jsonPath("$._embedded.values", containsInAnyOrder(
                FacetValueMatcher.entryTypes("Research Subject Categories::MATEMATICA"))));
    }

    @Test
    public void discoverFacetsTypesTestWithUnknownAuthority() throws Exception {
        context.turnOffAuthorisationSystem();
        String[] supportedLanguage = { "en","uk", "it" };
        configurationService.setProperty("webui.supported.locales", supportedLanguage);
        metadataAuthorityService.clearCache();
        choiceAuthorityService.clearCache();

        parentCommunity = CommunityBuilder.createCommunity(context)
            .withName("Parent Community")
            .build();

        Collection col1 = CollectionBuilder.createCollection(context, parentCommunity, "123456789/language-test-1")
            .withName("Collection 1")
            .withEntityType("Publication")
            .build();

        ItemBuilder.createItem(context, col1)
            .withTitle("Test 1")
            .withIssueDate("2010-10-17")
            .withAuthor("Testing, Works")
            .withType("Research Subject Categories::MATEMATICA", "srsc:UNKNOWN")
            .build();

        context.restoreAuthSystemState();

        getClient().perform(get("/api/discover/facets/types")
            .header("Accept-Language", Locale.ITALIAN.getLanguage())
            .param("configuration", "multilanguage-types")
            .param("prefix", "research"))
            .andExpect(jsonPath("$.type", is("discover")))
            .andExpect(jsonPath("$.name", is("types")))
            .andExpect(jsonPath("$.facetType", is("text")))
            .andExpect(jsonPath("$._links.self.href", containsString("api/discover/facets/types")))
            .andExpect(jsonPath("$._embedded.values", containsInAnyOrder(
                FacetValueMatcher.entryTypes("Research Subject Categories::MATEMATICA", "srsc:UNKNOWN"))));

        getClient().perform(get("/api/discover/facets/types")
            .header("Accept-Language", "uk")
            .param("configuration", "multilanguage-types")
            .param("prefix", "research"))
            .andExpect(jsonPath("$.type", is("discover")))
            .andExpect(jsonPath("$.name", is("types")))
            .andExpect(jsonPath("$.facetType", is("text")))
            .andExpect(jsonPath("$._links.self.href", containsString("api/discover/facets/types")))
            .andExpect(jsonPath("$._embedded.values", containsInAnyOrder(
                FacetValueMatcher.entryTypes("Research Subject Categories::MATEMATICA", "srsc:UNKNOWN"))));
    }

    @Test
    public void discoverFacetsTypesTestWithUnknownAuthorityName() throws Exception {
        context.turnOffAuthorisationSystem();
        String[] supportedLanguage = { "en","uk", "it" };
        configurationService.setProperty("webui.supported.locales", supportedLanguage);
        metadataAuthorityService.clearCache();
        choiceAuthorityService.clearCache();

        parentCommunity = CommunityBuilder.createCommunity(context)
            .withName("Parent Community")
            .build();

        Collection col1 = CollectionBuilder.createCollection(context, parentCommunity, "123456789/language-test-1")
            .withName("Collection 1")
            .withEntityType("Publication")
            .build();

        ItemBuilder.createItem(context, col1)
            .withTitle("Test 1")
            .withIssueDate("2010-10-17")
            .withAuthor("Testing, Works")
            .withType("Research Subject Categories::MATEMATICA", "UNKNOWN:VALUE")
            .build();

        context.restoreAuthSystemState();

        getClient().perform(get("/api/discover/facets/types")
            .header("Accept-Language", Locale.ITALIAN.getLanguage())
            .param("configuration", "multilanguage-types")
            .param("prefix", "research"))
            .andExpect(jsonPath("$.type", is("discover")))
            .andExpect(jsonPath("$.name", is("types")))
            .andExpect(jsonPath("$.facetType", is("text")))
            .andExpect(jsonPath("$._links.self.href", containsString("api/discover/facets/types")))
            .andExpect(jsonPath("$._embedded.values", containsInAnyOrder(
                FacetValueMatcher.entryTypes("Research Subject Categories::MATEMATICA", "UNKNOWN:VALUE"))));

        getClient().perform(get("/api/discover/facets/types")
            .header("Accept-Language", "uk")
            .param("configuration", "multilanguage-types")
            .param("prefix", "research"))
            .andExpect(jsonPath("$.type", is("discover")))
            .andExpect(jsonPath("$.name", is("types")))
            .andExpect(jsonPath("$.facetType", is("text")))
            .andExpect(jsonPath("$._links.self.href", containsString("api/discover/facets/types")))
            .andExpect(jsonPath("$._embedded.values", containsInAnyOrder(
                FacetValueMatcher.entryTypes("Research Subject Categories::MATEMATICA", "UNKNOWN:VALUE"))));
    }

    @Test
    public void discoverFacetsTypesTestWithWrongAuthorityFormat() throws Exception {
        context.turnOffAuthorisationSystem();
        String[] supportedLanguage = { "en", "uk", "it" };
        configurationService.setProperty("webui.supported.locales", supportedLanguage);
        metadataAuthorityService.clearCache();
        choiceAuthorityService.clearCache();

        parentCommunity = CommunityBuilder.createCommunity(context)
            .withName("Parent Community")
            .build();

        Collection col1 = CollectionBuilder.createCollection(context, parentCommunity, "123456789/language-test-1")
            .withName("Collection 1")
            .withEntityType("Publication")
            .build();

        ItemBuilder.createItem(context, col1)
            .withTitle("Test 1")
            .withIssueDate("2010-10-17")
            .withAuthor("Testing, Works")
            .withType("Research Subject Categories::MATEMATICA", "authority")
            .build();

        context.restoreAuthSystemState();

        getClient().perform(get("/api/discover/facets/types")
            .header("Accept-Language", Locale.ITALIAN.getLanguage())
            .param("configuration", "multilanguage-types")
            .param("prefix", "research"))
            .andExpect(jsonPath("$.type", is("discover")))
            .andExpect(jsonPath("$.name", is("types")))
            .andExpect(jsonPath("$.facetType", is("text")))
            .andExpect(jsonPath("$._links.self.href", containsString("api/discover/facets/types")))
            .andExpect(jsonPath("$._embedded.values", containsInAnyOrder(
                FacetValueMatcher.entryTypes("Research Subject Categories::MATEMATICA", "authority"))));

        getClient().perform(get("/api/discover/facets/types")
            .header("Accept-Language", "uk")
            .param("configuration", "multilanguage-types")
            .param("prefix", "research"))
            .andExpect(jsonPath("$.type", is("discover")))
            .andExpect(jsonPath("$.name", is("types")))
            .andExpect(jsonPath("$.facetType", is("text")))
            .andExpect(jsonPath("$._links.self.href", containsString("api/discover/facets/types")))
            .andExpect(jsonPath("$._embedded.values", containsInAnyOrder(
                FacetValueMatcher.entryTypes("Research Subject Categories::MATEMATICA", "authority"))));
    }

}
