/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.integration.crosswalks.virtualfields.postprocessors;

import static org.apache.commons.lang3.StringUtils.capitalize;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dspace.content.Item;
import org.dspace.content.integration.crosswalks.csl.CSLResult;
import org.dspace.core.Context;

public class GroupByTypeCitationsPostProcessor implements VirtualFieldCitationsPostProcessor {

    private final Pattern typePattern;

    private final boolean removeTypePattern;

    private final String defaultType;

    public GroupByTypeCitationsPostProcessor(String typeRegex) {
        this(typeRegex, true, "Other");
    }

    public GroupByTypeCitationsPostProcessor(String typeRegex, boolean removeTypePattern, String defaultType) {
        this.typePattern = Pattern.compile(typeRegex);
        this.removeTypePattern = removeTypePattern;
        this.defaultType = defaultType;
    }

    @Override
    public CSLResult process(Context context, Item item, CSLResult cslResult) {

        if (!cslResult.getFormat().equals("fo")) {
            throw new IllegalArgumentException("Only CSLResult related to fo format is supported");
        }

        String[] citationEntries = cslResult.getCitationEntries();
        String[] newCitationEntries = new String[citationEntries.length];

        String lastType = null;

        for (int i = 0; i < citationEntries.length; i++) {

            String citationEntry = citationEntries[i];

            String type = getTypeOrDefault(citationEntry);

            if (removeTypePattern) {
                citationEntry = citationEntry.replaceFirst(typePattern.pattern(), "");
            }

            if (!type.equalsIgnoreCase(lastType)) {
                citationEntry = addTypeHeaderToCitationEntry(citationEntry, type);
            }

            lastType = type;
            newCitationEntries[i] = citationEntry;
        }

        return new CSLResult(cslResult.getFormat(), cslResult.getItemIds(), newCitationEntries);
    }

    private String addTypeHeaderToCitationEntry(String citationEntry, String type) {
        return "<fo:block font-size=\"12pt\" font-weight=\"bold\" margin-top=\"4mm\" >"
            + capitalize(type.toLowerCase()) + "</fo:block>" + citationEntry;
    }

    @Override
    public String getName() {
        return "group-by-type";
    }

    private String getTypeOrDefault(String citationEntry) {
        Matcher matcher = typePattern.matcher(citationEntry);
        return matcher.find() ? matcher.group(1) : defaultType;
    }

}
