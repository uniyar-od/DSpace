package org.dspace.app.cris.batch;

public abstract class AFeedVariantsQueryGenerator extends AFeedQueryGenerator {

    private String solrExtraFilter;
    private boolean useVariants;

    public String getSolrExtraFilter()
    {
        return solrExtraFilter;
    }

    public void setSolrExtraFilter(String solrExtraFilter)
    {
        this.solrExtraFilter = solrExtraFilter;
    }

    public void setUseVariants(boolean useVariants)
    {
        this.useVariants = useVariants;
    }

    public boolean isUseVariants()
    {
        return useVariants;
    }
}
