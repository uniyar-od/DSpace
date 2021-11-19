/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.dspace.app.launcher.ScriptLauncher;
import org.dspace.app.rest.test.AbstractControllerIntegrationTest;
import org.dspace.app.scripts.handler.impl.TestDSpaceRunnableHandler;
import org.dspace.builder.CollectionBuilder;
import org.dspace.builder.CommunityBuilder;
import org.dspace.builder.ItemBuilder;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.Item;
import org.dspace.content.MetadataValue;
import org.dspace.content.service.ItemService;
import org.dspace.event.factory.EventServiceFactory;
import org.dspace.event.service.EventService;
import org.dspace.services.ConfigurationService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
* @author Mykhaylo Boychuk (mykhaylo.boychuk at 4Science.it)
*/
public class UpdateItemReferenceIT extends AbstractControllerIntegrationTest {

    @Autowired
    private ItemService itemService;
    @Autowired
    private ConfigurationService configService;

    @Test
    public void updateItemReferenceTest() throws Exception {
        context.turnOffAuthorisationSystem();
        Set<String> consumers = new HashSet<String>(
                               Arrays.asList(configService.getArrayProperty("event.dispatcher.default.consumers")));
        consumers.remove("referenceresolver");
        consumers.remove("crisconsumer");
        configService.setProperty("event.dispatcher.default.consumers", consumers.toArray());
        EventService eventService = EventServiceFactory.getInstance().getEventService();
        eventService.reloadConfiguration();

        Community rootCommunity = CommunityBuilder.createCommunity(context)
                                                  .withName("My Root Community")
                                                  .build();
        Collection collection = CollectionBuilder.createCollection(context, rootCommunity)
                                                 .withName("My collection")
                                                 .build();

        Item publication1 = ItemBuilder.createItem(context, collection)
                                       .withEntityType("Publication")
                                       .withTitle("Title Publication 1")
                                       .withAuthor("M.Boychuk", "will be referenced::ORCID::0000-0000-0012-3456")
                                       .withAuthor("V.Stus", "will be referenced::ORCID::0000-0000-0078-9101")
                                       .build();

        Item publication2 = ItemBuilder.createItem(context, collection)
                                       .withEntityType("Publication")
                                       .withTitle("Title Publication 2")
                                       .withAuthor("M.Boychuk", "will be referenced::ORCID::0000-0000-0012-3456")
                                       .build();

        Item publication3 = ItemBuilder.createItem(context, collection)
                                       .withEntityType("Publication")
                                       .withTitle("Title Publication 3")
                                       .withAuthor("O.Beketov", "will be referenced::ORCID::0000-0000-5612-5555")
                                       .build();

        Item misha = ItemBuilder.createItem(context, collection)
                                .withEntityType("Person")
                                .withFullName("Misha Boychuk")
                                .withTitle("Misha Boychuk")
                                .withOrcidIdentifier("0000-0000-0012-3456")
                                .build();

        Item viktor = ItemBuilder.createItem(context, collection)
                                 .withEntityType("Person")
                                 .withFullName("Viktor Stus")
                                 .withTitle("Viktor Stus")
                                 .withOrcidIdentifier("0000-0000-0078-9101")
                                 .build();

        System.out.println("p1 " + publication1.getID());
        System.out.println("p2 " + publication2.getID());
        System.out.println("p3 " + publication3.getID());
        System.out.println("m " + misha.getID());
        System.out.println("v " + viktor.getID());

        context.restoreAuthSystemState();

        //verify that the authority values was not resolved yet
        publication1 = context.reloadEntity(publication1);
        publication2 = context.reloadEntity(publication2);
        publication3 = context.reloadEntity(publication3);
        List<MetadataValue> values = itemService.getMetadata(publication1, "dc", "contributor", "author", Item.ANY);
        List<MetadataValue> values2 = itemService.getMetadata(publication2, "dc", "contributor", "author", Item.ANY);
        List<MetadataValue> values3 = itemService.getMetadata(publication3, "dc", "contributor", "author", Item.ANY);

        assertEquals(values.size(), 2);
        assertTrue(StringUtils.equalsAny("will be referenced::ORCID::0000-0000-0012-3456", values.get(0).getAuthority(),
                values.get(1).getAuthority()));
        assertTrue(StringUtils.equalsAny("will be referenced::ORCID::0000-0000-0078-9101", values.get(0).getAuthority(),
                values.get(1).getAuthority()));

        assertEquals(values2.size(), 1);
        assertEquals(values2.get(0).getAuthority(), "will be referenced::ORCID::0000-0000-0012-3456");

        assertEquals(values3.size(), 1);
        assertEquals(values3.get(0).getAuthority(), "will be referenced::ORCID::0000-0000-5612-5555");

        TestDSpaceRunnableHandler testDSpaceRunnableHandler = new TestDSpaceRunnableHandler();
        String[] args = new String[] { "update-item-references" };
        ScriptLauncher.handleScript(args, ScriptLauncher.getConfig(kernelImpl), testDSpaceRunnableHandler, kernelImpl);

        //verify that the authority values was not resolved yet
        publication1 = context.reloadEntity(publication1);
        publication2 = context.reloadEntity(publication2);
        publication3 = context.reloadEntity(publication3);
        values = itemService.getMetadata(publication1, "dc", "contributor", "author", Item.ANY);
        values2 = itemService.getMetadata(publication2, "dc", "contributor", "author", Item.ANY);
        values3 = itemService.getMetadata(publication3, "dc", "contributor", "author", Item.ANY);

        assertEquals(values.size(), 2);
        assertTrue(StringUtils.equalsAny(misha.getID().toString(), values.get(0).getAuthority(),
                values.get(1).getAuthority()));
        assertTrue(StringUtils.equalsAny(viktor.getID().toString(), values.get(0).getAuthority(),
                values.get(1).getAuthority()));

        assertEquals(values2.size(), 1);
        assertEquals(values2.get(0).getAuthority(), misha.getID().toString());

        // publication3 was not resolved
        assertEquals(values3.size(), 1);
        assertEquals(values3.get(0).getAuthority(), "will be referenced::ORCID::0000-0000-5612-5555");

    }

}