/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.edit;

import java.util.ArrayList;
import java.util.List;

import org.dspace.content.logic.Filter;
import org.dspace.content.security.AccessItemMode;
import org.dspace.content.security.CrisSecurity;

/**
 * Implementation of {@link AccessItemMode} to configure the item correction
 * modes.
 *
 * @author Luca Giamminonni (luca.giamminonni at 4science.it)
 *
 */
public class CorrectItemMode implements AccessItemMode {

    /**
     * Defines the users enabled to use this correction configuration
     */
    private List<CrisSecurity> securities;

    /**
     * Contains the list of groups metadata for CUSTOM security or the groups
     * name/uuid for the GROUP security
     */
    private List<String> groups = new ArrayList<String>();

    /**
     * Contains the list of users metadata for CUSTOM security
     */
    private List<String> users = new ArrayList<String>();

    /**
     * Contains the list of users metadata for CUSTOM security
     */
    private List<String> items = new ArrayList<String>();
    private Filter additionalFilter;

    @Override
    public List<CrisSecurity> getSecurities() {
        return securities;
    }

    @Override
    public List<String> getGroupMetadataFields() {
        return groups;
    }

    @Override
    public List<String> getUserMetadataFields() {
        return users;
    }

    @Override
    public List<String> getItemMetadataFields() {
        return items;
    }

    public void setSecurity(CrisSecurity security) {
        this.securities = List.of(security);
    }

    public void setSecurities(List<CrisSecurity> securities) {
        this.securities = securities;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    public void setItems(List<String> items) {
        this.items = items;
    }

    @Override
    public List<String> getGroups() {
        return groups;
    }

    public void setAdditionalFilter(Filter additionalFilter) {
        this.additionalFilter = additionalFilter;
    }

    @Override
    public Filter getAdditionalFilter() {
        return additionalFilter;
    }
}
