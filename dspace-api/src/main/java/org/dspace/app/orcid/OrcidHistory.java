/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.orcid;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.dspace.content.Item;
import org.dspace.core.ReloadableEntity;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "orcid_history")
public class OrcidHistory implements ReloadableEntity<Integer> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "orcid_history_id_seq")
    @SequenceGenerator(name = "orcid_history_id_seq", sequenceName = "orcid_history_id_seq", allocationSize = 1)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    protected Item owner;

    @ManyToOne
    @JoinColumn(name = "entity_id")
    private Item entity;

    @Column(name = "put_code")
    private String putCode;

    @Column(name = "record_type")
    private String recordType;

    @Column(name = "description")
    private String description;

    @Lob
    @Column(name = "metadata")
    @Type(type = "org.dspace.storage.rdbms.hibernate.DatabaseAwareLobType")
    private String metadata;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation")
    private OrcidOperation operation;

    @Lob
    @Column(name = "response_message")
    @Type(type = "org.dspace.storage.rdbms.hibernate.DatabaseAwareLobType")
    private String responseMessage;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "timestamp_last_attempt")
    private Date timestamp = new Date();

    @Column(name = "status")
    private Integer status;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public Integer getID() {
        return id;
    }

    public Item getOwner() {
        return owner;
    }

    public void setOwner(Item owner) {
        this.owner = owner;
    }

    public Item getEntity() {
        return entity;
    }

    public void setEntity(Item entity) {
        this.entity = entity;
    }

    public String getPutCode() {
        return putCode;
    }

    public void setPutCode(String putCode) {
        this.putCode = putCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public String getRecordType() {
        return recordType;
    }

    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public OrcidOperation getOperation() {
        return operation;
    }

    public void setOperation(OrcidOperation operation) {
        this.operation = operation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

}
