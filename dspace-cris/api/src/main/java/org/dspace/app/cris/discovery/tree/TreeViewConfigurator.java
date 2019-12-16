package org.dspace.app.cris.discovery.tree;

import java.util.List;
import java.util.Map;

import org.dspace.app.cris.configuration.RelationConfiguration;

public class TreeViewConfigurator
{
    private Map<String, List<TreeViewResourceConfigurator>> profile;
    
    private Map<String, Boolean> enabled;

    private Map<String, List<RelationConfiguration>> relations;
    
    private Map<String, Boolean> showRelationOnLeaf;
    
    private Map<String, Boolean> showRelationCount;
    
    private Map<String, String> icons;
    
    private Map<String, List<String>> parents;
    
    private Map<String, List<String>> leafs;
    
    private Map<String, Map<String, String>> closed;
    
    public Map<String, Boolean> getEnabled()
    {
        return enabled;
    }

    public void setEnabled(Map<String, Boolean> enabled)
    {
        this.enabled = enabled;
    }

    public Map<String, List<TreeViewResourceConfigurator>> getProfile()
    {
        return profile;
    }

    public void setProfile(Map<String, List<TreeViewResourceConfigurator>> profile)
    {
        this.profile = profile;
    }

    public Map<String, List<String>> getParents()
    {
        return parents;
    }

    public void setParents(Map<String, List<String>> parents)
    {
        this.parents = parents;
    }

    public Map<String, List<String>> getLeafs()
    {
        return leafs;
    }

    public void setLeafs(Map<String, List<String>> leafs)
    {
        this.leafs = leafs;
    }

    public Map<String, Boolean> getShowRelationOnLeaf()
    {
        return showRelationOnLeaf;
    }

    public void setShowRelationOnLeaf(Map<String, Boolean> showRelation)
    {
        this.showRelationOnLeaf = showRelation;
    }

    public Map<String, String> getIcons()
    {
        return icons;
    }

    public void setIcons(Map<String, String> icons)
    {
        this.icons = icons;
    }

    public Map<String, List<RelationConfiguration>> getRelations()
    {
        return relations;
    }

    public void setRelations(Map<String, List<RelationConfiguration>> relations)
    {
        this.relations = relations;
    }

    public Map<String, Boolean> getShowRelationCount()
    {
        return showRelationCount;
    }

    public void setShowRelationCount(Map<String, Boolean> showRelationCount)
    {
        this.showRelationCount = showRelationCount;
    }

    public Map<String, Map<String, String>> getClosed()
    {
        return closed;
    }

    public void setClosed(Map<String, Map<String, String>> closed)
    {
        this.closed = closed;
    }

}
