package org.dspace.app.rest.model.step;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.dspace.app.cris.deduplication.model.DuplicateDecisionType;
import org.dspace.app.cris.deduplication.model.DuplicateDecisionValue;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

/**
 * Java Bean to expose the section license during in progress submission.
 *
 * @author Giuseppe Digilio (giuseppe.digilio at 4science.it)
 */
public class DataDetectDuplicate implements SectionData {
    @JsonUnwrapped
//    private List<DuplicateMatch> matches;
    private Map<UUID, DuplicateMatch> matches = new HashMap<UUID, DuplicateMatch>();

    public Map<UUID, DuplicateMatch> getMatches() {
		return matches;
	}

	public void setMatches(Map<UUID, DuplicateMatch> matches) {
		this.matches = matches;
	}

}