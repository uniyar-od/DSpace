/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.ldn.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 */
public class Object extends Citation {

    @JsonProperty("sorg:name")
    private String title;

    @JsonProperty("subject")
    private String subject;

    @JsonProperty("relationship")
    private String relationship;

    @JsonProperty("object")
    private String object;

    /**
     * 
     */
    public Object() {
        super();
    }

    /**
     * @return String
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return String
     */
    public String getSubject() {
        return subject;
    }

    /**
     * @param subject
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * @return String
     */
    public String getRelationship() {
        return relationship;
    }

    /**
     * @param relationship
     */
    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    /**
     * @return String
     */
    public String getObject() {
        return object;
    }

    /**
     * @param object
     */
    public void setObject(String object) {
        this.object = object;
    }

}
