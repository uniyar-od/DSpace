package org.dspace.app.cris.batch;

import java.util.List;

public abstract class AFeedQueryGenerator implements IFeedQueryGenerator {

    private List<String> extraFilters;
    private Integer defaultSubmitter;

    public List<String> getExtraFilters() {
        return extraFilters;
    }

    public void setExtraFilters(List<String> extraFilters) {
        this.extraFilters = extraFilters;
    }

    public Integer getDefaultSubmitter() {
        return defaultSubmitter;
    }

    public void setDefaultSubmitter(Integer defaultSubmitter) {
        this.defaultSubmitter = defaultSubmitter;
    }
}
