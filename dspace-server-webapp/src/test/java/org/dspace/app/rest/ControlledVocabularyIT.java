/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest;

import static com.jayway.jsonpath.JsonPath.read;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.ws.rs.core.MediaType;

import org.dspace.app.rest.model.patch.Operation;
import org.dspace.app.rest.model.patch.ReplaceOperation;
import org.dspace.app.rest.test.AbstractControllerIntegrationTest;
import org.dspace.builder.CollectionBuilder;
import org.dspace.builder.CommunityBuilder;
import org.dspace.builder.ItemBuilder;
import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.dspace.content.edit.EditItem;
import org.dspace.services.ConfigurationService;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Test suite for the ControlledVocabulary functionality
 *
 * @author Mykhaylo Boychuk (mykhaylo.boychuk at 4science.com)
 */
public class ControlledVocabularyIT extends AbstractControllerIntegrationTest {

    @Autowired
    private ConfigurationService configurationService;

    @Test
    public void controlledVocabularyWithHierarchyStoreSetTrueTest() throws Exception {
        context.turnOffAuthorisationSystem();
        String vocabularyName = "publication-coar-types";
        parentCommunity = CommunityBuilder.createCommunity(context)
                                          .withName("Root Community")
                                          .build();

        Collection col = CollectionBuilder.createCollection(context, parentCommunity)
                                          .withEntityType("Publication")
                                          .withName("Collection 1")
                                          .build();

        Item itemA = ItemBuilder.createItem(context, col)
                                .withTitle("Test Item A")
                                .withIssueDate("2023-04-04")
                                .withType("Resource Types::text::book::book part", vocabularyName + ":c_3248")
                                .build();

        EditItem editItem = new EditItem(context, itemA);

        context.restoreAuthSystemState();

        String tokenAdmin = getAuthToken(admin.getEmail(), password);

        getClient(tokenAdmin).perform(get("/api/core/items/" + itemA.getID()))
                             .andExpect(status().isOk())
                             .andExpect(jsonPath("$.metadata", Matchers.allOf(
                                     hasJsonPath("$['dc.title'][0].value", is("Test Item A")),
                                     hasJsonPath("$['dc.type'][0].value", is("Resource Types::text::book::book part")),
                                     hasJsonPath("$['dc.type'][0].authority", is(vocabularyName + ":c_3248")),
                                     hasJsonPath("$['dc.type'][0].confidence", is(600))
                                     )));

        AtomicReference<String> selectedLeafValue = new AtomicReference<>();
        AtomicReference<String> selectedLeafauthority = new AtomicReference<>();

        getClient(tokenAdmin).perform(get("/api/submission/vocabularies/" + vocabularyName + "/entries")
                             .param("metadata", "dc.type")
                             .param("entryID", vocabularyName + ":c_b239"))
                             .andExpect(status().isOk())
                             .andDo(result -> selectedLeafValue.set(read(result.getResponse().getContentAsString(),
                                                                    "$._embedded.entries[0].value")))
                             .andDo(result -> selectedLeafauthority.set(read(result.getResponse().getContentAsString(),
                                                                        "$._embedded.entries[0].authority")));

        List<Operation> operations = new ArrayList<Operation>();
        Map<String, String> value = new HashMap<String, String>();
        value.put("value", selectedLeafValue.get());
        value.put("authority", selectedLeafauthority.get());
        value.put("confidence", "600");
        operations.add(new ReplaceOperation("/sections/controlled-vocabulary-test/dc.type/0", value));

        String patchBody = getPatchContent(operations);
        getClient(tokenAdmin).perform(patch("/api/core/edititems/" + editItem.getID() + ":MODE-VOC")
                             .content(patchBody)
                             .contentType(MediaType.APPLICATION_JSON_PATCH_JSON))
                             .andExpect(status().isOk());

        getClient(tokenAdmin).perform(get("/api/core/items/" + itemA.getID()))
                             .andExpect(status().isOk())
                             .andExpect(jsonPath("$.metadata", Matchers.allOf(
                                   hasJsonPath("$['dc.title'][0].value", is("Test Item A")),
                                   hasJsonPath("$['dc.type'][0].value", is("Resource Types::text::journal::editorial")),
                                   hasJsonPath("$['dc.type'][0].authority", is(vocabularyName + ":c_b239")),
                                   hasJsonPath("$['dc.type'][0].confidence", is(600))
                                   )));
    }

    @Test
    public void controlledVocabularyWithHierarchyStoreSetFalseTest() throws Exception {
        context.turnOffAuthorisationSystem();
        String vocabularyName = "publication-coar-types";
        configurationService.setProperty("vocabulary.plugin." + vocabularyName + ".hierarchy.store", false);
        parentCommunity = CommunityBuilder.createCommunity(context)
                                          .withName("Root Community")
                                          .build();

        Collection col = CollectionBuilder.createCollection(context, parentCommunity)
                                          .withEntityType("Publication")
                                          .withName("Collection 1")
                                          .build();

        Item itemA = ItemBuilder.createItem(context, col)
                                .withTitle("Test Item A")
                                .withIssueDate("2023-04-04")
                                .withType("Resource Types::text::book::book part", vocabularyName + ":c_3248")
                                .build();

        EditItem editItem = new EditItem(context, itemA);

        context.restoreAuthSystemState();

        String tokenAdmin = getAuthToken(admin.getEmail(), password);

        getClient(tokenAdmin).perform(get("/api/core/items/" + itemA.getID()))
                             .andExpect(status().isOk())
                             .andExpect(jsonPath("$.metadata", Matchers.allOf(
                                     hasJsonPath("$['dc.title'][0].value", is("Test Item A")),
                                     hasJsonPath("$['dc.type'][0].value", is("Resource Types::text::book::book part")),
                                     hasJsonPath("$['dc.type'][0].authority", is(vocabularyName + ":c_3248")),
                                     hasJsonPath("$['dc.type'][0].confidence", is(600))
                                     )));

        AtomicReference<String> selectedLeafValue = new AtomicReference<>();
        AtomicReference<String> selectedLeafauthority = new AtomicReference<>();

        getClient(tokenAdmin).perform(get("/api/submission/vocabularies/" + vocabularyName + "/entries")
                             .param("metadata", "dc.type")
                             .param("entryID", vocabularyName + ":c_b239"))
                             .andExpect(status().isOk())
                             .andDo(result -> selectedLeafValue.set(read(result.getResponse().getContentAsString(),
                                                                    "$._embedded.entries[0].value")))
                             .andDo(result -> selectedLeafauthority.set(read(result.getResponse().getContentAsString(),
                                                                        "$._embedded.entries[0].authority")));

        List<Operation> operations = new ArrayList<Operation>();
        Map<String, String> value = new HashMap<String, String>();
        value.put("value", selectedLeafValue.get());
        value.put("authority", selectedLeafauthority.get());
        value.put("confidence", "600");
        operations.add(new ReplaceOperation("/sections/controlled-vocabulary-test/dc.type/0", value));

        String patchBody = getPatchContent(operations);
        getClient(tokenAdmin).perform(patch("/api/core/edititems/" + editItem.getID() + ":MODE-VOC")
                             .content(patchBody)
                             .contentType(MediaType.APPLICATION_JSON_PATCH_JSON))
                             .andExpect(status().isOk());

        getClient(tokenAdmin).perform(get("/api/core/items/" + itemA.getID()))
                             .andExpect(status().isOk())
                             .andExpect(jsonPath("$.metadata", Matchers.allOf(
                                     hasJsonPath("$['dc.title'][0].value", is("Test Item A")),
                                     hasJsonPath("$['dc.type'][0].value", is("editorial")),
                                     hasJsonPath("$['dc.type'][0].authority", is(vocabularyName + ":c_b239")),
                                     hasJsonPath("$['dc.type'][0].confidence", is(600))
                                     )));
    }

    @Test
    public void controlledVocabularyWithHierarchySuggestSetTrueTest() throws Exception {
        String vocabularyName = "publication-coar-types";

        String tokenAdmin = getAuthToken(admin.getEmail(), password);
        // default configuration
        getClient(tokenAdmin).perform(get("/api/submission/vocabularies/" + vocabularyName + "/entries")
                             .param("metadata", "dc.type")
                             .param("entryID", vocabularyName + ":c_b239"))
                             .andExpect(status().isOk())
                             .andExpect(jsonPath("$._embedded.entries[0]", Matchers.allOf(
                                     hasJsonPath("$.authority", is(vocabularyName + ":c_b239")),
                                     // the display value without suggestions
                                     hasJsonPath("$.display", is("editorial")),
                                     hasJsonPath("$.value", is("Resource Types::text::journal::editorial"))
                                     )));

        configurationService.setProperty("vocabulary.plugin." + vocabularyName + ".hierarchy.suggest", true);

        getClient(tokenAdmin).perform(get("/api/submission/vocabularies/" + vocabularyName + "/entries")
                .param("metadata", "dc.type")
                .param("entryID", vocabularyName + ":c_b239"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.entries[0]", Matchers.allOf(
                        hasJsonPath("$.authority", is(vocabularyName + ":c_b239")),
                        // now the display value with suggestions
                        hasJsonPath("$.display", is("Resource Types::text::journal::editorial")),
                        hasJsonPath("$.value", is("Resource Types::text::journal::editorial"))
                        )));
    }

}
