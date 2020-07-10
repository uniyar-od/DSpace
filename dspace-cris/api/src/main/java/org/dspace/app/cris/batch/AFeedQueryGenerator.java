package org.dspace.app.cris.batch;

import java.sql.SQLException;
import java.util.List;

public abstract class AFeedQueryGenerator implements IFeedQueryGenerator {

    private List<String> extraFilters;
    private String defaultSubmitter;

    public List<String> getExtraFilters() {
        return extraFilters;
    }

    public void setExtraFilters(List<String> extraFilters) {
        this.extraFilters = extraFilters;
    }

    public String getDefaultSubmitter() throws SQLException {
        return defaultSubmitter;
    }

    public void setDefaultSubmitter(String defaultSubmitter) {
        this.defaultSubmitter = defaultSubmitter;
    }
}
