package org.dspace.app.rest.model.step;

import java.util.List;

import org.dspace.app.cris.deduplication.model.DuplicateDecisionValue;
import org.dspace.app.rest.converter.ItemConverter;
import org.dspace.app.rest.model.ItemRest;
import org.dspace.app.rest.model.LinkRest;
import org.dspace.browse.BrowsableDSpaceObject;
import org.dspace.content.Item;
import org.dspace.util.ItemUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

/**
 * Java Bean to expose the section license during in progress submission.
 *
 * @author Giuseppe Digilio (giuseppe.digilio at 4science.it)
 */
public class DuplicateMatch implements SectionData {
	@Autowired
	ItemConverter converter;

    @JsonProperty(access = Access.READ_ONLY)
    private DuplicateDecisionValue submitterDecision;

	@JsonProperty(access = Access.READ_ONLY)
    private DuplicateDecisionValue workflowDecision;

	@JsonProperty(access = Access.READ_ONLY)
    private ItemRest matchObject;

    @LinkRest(linkClass = ItemRest.class)
    @JsonIgnore
    public ItemRest getMatchObject() {
        return matchObject;
    }     

	public void setMatchObject(ItemRest matchObject) {
		this.matchObject = matchObject;
	}

	public DuplicateDecisionValue getSubmitterDecision() {
		return submitterDecision;
	}

	public void setSubmitterDecision(DuplicateDecisionValue submitterDecision) {
		this.submitterDecision = submitterDecision;
	}

	public DuplicateDecisionValue getWorkflowDecision() {
		return workflowDecision;
	}

	public void setWorkflowDecision(DuplicateDecisionValue workflowDecision) {
		this.workflowDecision = workflowDecision;
	}

}