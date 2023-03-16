/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.dspace.app.rest.test.AbstractControllerIntegrationTest;
import org.dspace.builder.CollectionBuilder;
import org.dspace.builder.CommunityBuilder;
import org.dspace.builder.ItemBuilder;
import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * @author Mykhaylo Boychuk (mykhaylo.boychuk at 4science.com)
 */
public class ItemAuthorityRestRepositoryIT extends AbstractControllerIntegrationTest {

    @Test
    public void deletePersonItemTest() throws Exception {
        context.turnOffAuthorisationSystem();

        parentCommunity = CommunityBuilder.createCommunity(context)
                                          .withName("Parent Community")
                                          .build();

        Collection publications = CollectionBuilder.createCollection(context, parentCommunity)
                                                   .withName("Collection of Publications")
                                                   .withEntityType("Publication")
                                                   .build();

        Collection persons = CollectionBuilder.createCollection(context, parentCommunity)
                                              .withName("Collection of Persons")
                                              .withEntityType("Person")
                                              .build();

        Collection orgUnits = CollectionBuilder.createCollection(context, parentCommunity)
                                               .withName("Collection of OrgUnits")
                                               .withEntityType("OrgUnit")
                                               .build();

        Item orgUnitItem = ItemBuilder.createItem(context, orgUnits)
                                      .withTitle("4Science")
                                      .withEntityType("OrgUnit")
                                      .build();

        Item personItem = ItemBuilder.createItem(context, persons)
                                     .withTitle("Boychuk, Mykhaylo")
                                     .withOrcidIdentifier("0001-002-0003-0001")
                                     .withScopusAuthorIdentifier("12345678001")
                                     .withAffiliation("4Science", orgUnitItem.getID().toString())
                                     .withEntityType("Person")
                                     .build();

        Item personItem2 = ItemBuilder.createItem(context, persons)
                                      .withTitle("Giamminonni, Luca")
                                      .withOrcidIdentifier("0000-0002-8310-6788")
                                      .withScopusAuthorIdentifier("12345678002")
                                      .withAffiliation("4Science", orgUnitItem.getID().toString())
                                      .withEntityType("Person")
                                      .build();

        Item publicationItem = ItemBuilder.createItem(context, publications)
                                          .withTitle("New functionalities of the DSpace7")
                                          .withAuthor("Boychuk, Mykhaylo", personItem.getID().toString())
                                          .withAuthor("Giamminonni, Luca", personItem2.getID().toString())
                                          .withIssueDate("2023-02-14")
                                          .withSubject("ExtraEntry")
                                          .withEntityType("Publication")
                                          .build();


        context.restoreAuthSystemState();

        String tokenAdmin = getAuthToken(admin.getEmail(), password);

        getClient(tokenAdmin).perform(get("/api/core/items/" + personItem.getID()))
                 .andExpect(status().isOk())
                 .andExpect(jsonPath("$", Matchers.allOf(
                  hasJsonPath("$.name", is(personItem.getName())),
                  hasJsonPath("$.metadata['person.affiliation.name'][0].value", is(orgUnitItem.getName())),
                  hasJsonPath("$.metadata['person.affiliation.name'][0].authority", is(orgUnitItem.getID().toString())),
                  hasJsonPath("$.metadata['person.affiliation.name'][0].confidence", is(600))
                  )));

        getClient(tokenAdmin).perform(get("/api/core/items/" + publicationItem.getID()))
                 .andExpect(status().isOk())
                 .andExpect(jsonPath("$", Matchers.allOf(
                  // check Boychuk, Mykhaylo
                  hasJsonPath("$.metadata['dc.contributor.author'][0].value", is(personItem.getName())),
                  hasJsonPath("$.metadata['dc.contributor.author'][0].authority", is(personItem.getID().toString())),
                  hasJsonPath("$.metadata['dc.contributor.author'][0].confidence", is(600)),
                  // Giamminonni, Luca
                  hasJsonPath("$.metadata['dc.contributor.author'][1].value", is(personItem2.getName())),
                  hasJsonPath("$.metadata['dc.contributor.author'][1].authority", is(personItem2.getID().toString())),
                  hasJsonPath("$.metadata['dc.contributor.author'][1].confidence", is(600))
                  )));

        System.out.println("-------------> Person UUID:" + personItem.getID());
        // delete
        getClient(tokenAdmin).perform(delete("/api/core/items/" + personItem.getID()))
                             .andExpect(status().isNoContent());

        getClient(tokenAdmin).perform(get("/api/core/items/" + personItem.getID()))
                             .andExpect(status().isNotFound());

        getClient(tokenAdmin).perform(get("/api/core/items/" + publicationItem.getID()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.allOf(
                 // check Boychuk, Mykhaylo
                 hasJsonPath("$.metadata['dc.contributor.author'][0].value", is(personItem.getName())),
                 hasJsonPath("$.metadata['dc.contributor.author'][0].authority", is(personItem.getID().toString())),
                 hasJsonPath("$.metadata['dc.contributor.author'][0].confidence", is(600)),
                 // check Giamminonni, Luca
                 hasJsonPath("$.metadata['dc.contributor.author'][1].value", is(personItem2.getName())),
                 hasJsonPath("$.metadata['dc.contributor.author'][1].authority", is(personItem2.getID().toString())),
                 hasJsonPath("$.metadata['dc.contributor.author'][1].confidence", is(600))
                 )));
    }

}
