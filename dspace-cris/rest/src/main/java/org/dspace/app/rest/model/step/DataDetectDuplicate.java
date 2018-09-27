package org.dspace.app.rest.model.step;

import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

/**
 * Java Bean to expose the section license during in progress submission.
 *
 * @author Giuseppe Digilio (giuseppe.digilio at 4science.it)
 */
public class DataDetectDuplicate implements SectionData {
    @JsonUnwrapped
    private Map<UUID, DuplicateMatch> matches = null;

    public Map<UUID, DuplicateMatch> getMatches() {
		return matches;
	}

	public void setMatches(Map<UUID, DuplicateMatch> matches) {
		this.matches = matches;
	}

}