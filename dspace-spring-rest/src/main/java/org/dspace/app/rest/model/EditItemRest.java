/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.dspace.app.rest.RestResourceController;

/**
 * The EditItem REST Resource
 *
 * @author Luigi Andrea Pascarelli (luigiandrea.pascarelli at 4science.it)
 */
public class EditItemRest extends BaseObjectRest<UUID> {

    public static final String NAME = "edititem";
    public static final String CATEGORY = RestAddressableModel.SUBMISSION;

    private Date lastModified = new Date();

    private Map<String, Serializable> sections;

    @JsonIgnore
    private CollectionRest collection;

    @JsonIgnore
    private ItemRest item;

    @JsonIgnore
    private SubmissionDefinitionRest submissionDefinition;

    @JsonIgnore
    private EPersonRest submitter;

    @Override
    public String getCategory() {
        return CATEGORY;
    }

    @Override
    public String getType() {
        return NAME;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public ItemRest getItem() {
        return item;
    }

    public void setItem(ItemRest item) {
        this.item = item;
    }

    public SubmissionDefinitionRest getSubmissionDefinition() {
        return submissionDefinition;
    }

    public void setSubmissionDefinition(SubmissionDefinitionRest submissionDefinition) {
        this.submissionDefinition = submissionDefinition;
    }

    public EPersonRest getSubmitter() {
        return submitter;
    }

    public void setSubmitter(EPersonRest submitter) {
        this.submitter = submitter;
    }

    @Override
    public Class getController() {
        return RestResourceController.class;
    }

    public Map<String, Serializable> getSections() {
        if (sections == null) {
            sections = new HashMap<String, Serializable>();
        }
        return sections;
    }

    public void setSections(Map<String, Serializable> sections) {
        this.sections = sections;
    }

    public CollectionRest getCollection() {
        return collection;
    }

    public void setCollection(CollectionRest collection) {
        this.collection = collection;
    }
}