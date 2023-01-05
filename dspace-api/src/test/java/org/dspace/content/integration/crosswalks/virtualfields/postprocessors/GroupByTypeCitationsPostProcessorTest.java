/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.integration.crosswalks.virtualfields.postprocessors;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;

import java.util.UUID;

import org.dspace.content.Item;
import org.dspace.content.integration.crosswalks.csl.CSLResult;
import org.dspace.core.Context;
import org.junit.Test;

public class GroupByTypeCitationsPostProcessorTest {

    private Context context = mock(Context.class);

    private Item item = mock(Item.class);

    @Test
    public void testWithFoFormat() {

        GroupByTypeCitationsPostProcessor processor = new GroupByTypeCitationsPostProcessor("%%%\\d(.*?)%%%");

        UUID[] itemIds = new UUID[] { randomUUID(),
            randomUUID(),
            randomUUID(),
            randomUUID(),
            randomUUID(),
            randomUUID(),
            randomUUID(),
            randomUUID() };

        String[] citations = new String[] {
            "<fo:block>%%%1article%%%Publication 1 title<fo:block/>",
            "<fo:block>%%%1article%%%Publication 2 title<fo:block/>",
            "<fo:block>%%%2book%%%Publication 3 title<fo:block/>",
            "<fo:block>%%%3conference%%%Publication 4 title<fo:block/>",
            "<fo:block>%%%3conference%%%Publication 5 title<fo:block/>",
            "<fo:block>%%%5speech%%%Publication 6 title<fo:block/>",
            "<fo:block>Publication 7 title<fo:block/>",
            "<fo:block>Publication 8 title<fo:block/>"
        };

        CSLResult result = processor.process(context, item, new CSLResult("fo", itemIds, citations));
        assertThat(result, notNullValue());
        assertThat(result.getFormat(), is("fo"));
        assertThat(result.getItemIds(), is(itemIds));
        assertThat(result.getCitationEntries(), arrayContaining(
            "<fo:block font-size=\"12pt\" font-weight=\"bold\" margin-top=\"4mm\" >Article</fo:block>"
                + "<fo:block>Publication 1 title<fo:block/>",
            "<fo:block>Publication 2 title<fo:block/>",
            "<fo:block font-size=\"12pt\" font-weight=\"bold\" margin-top=\"4mm\" >Book</fo:block>"
                + "<fo:block>Publication 3 title<fo:block/>",
            "<fo:block font-size=\"12pt\" font-weight=\"bold\" margin-top=\"4mm\" >Conference</fo:block>"
                + "<fo:block>Publication 4 title<fo:block/>",
            "<fo:block>Publication 5 title<fo:block/>",
            "<fo:block font-size=\"12pt\" font-weight=\"bold\" margin-top=\"4mm\" >Speech</fo:block>"
                + "<fo:block>Publication 6 title<fo:block/>",
            "<fo:block font-size=\"12pt\" font-weight=\"bold\" margin-top=\"4mm\" >Other</fo:block>"
                + "<fo:block>Publication 7 title<fo:block/>",
            "<fo:block>Publication 8 title<fo:block/>"));

    }
}
