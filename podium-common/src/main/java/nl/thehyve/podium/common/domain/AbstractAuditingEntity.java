/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.common.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * Base abstract class for entities which will hold definitions for created, last modified by and created,
 * last modified by date.
 */
@MappedSuperclass
@Audited
@EntityListeners(AuditingEntityListener.class)
public abstract class AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @CreatedBy
    @Column(name = "created_by", nullable = false, length = 50, updatable = false)
    @JsonIgnore
    private String createdBy;

    @CreatedDate
    @Column(name = "created_date", nullable = false)
    @JsonIgnore
    // FIXME:
    // ZonedDateTime is no longer supported;
    // LocalDateTime is not supported yet;
    // Switch to a better type matching "TIMESTAMP WITHOUT TIME ZONE" when possible
    private java.util.Date createdDate = new java.util.Date();

    @LastModifiedBy
    @Column(name = "last_modified_by", length = 50)
    @JsonIgnore
    private String lastModifiedBy;

    @LastModifiedDate
    @Column(name = "last_modified_date")
    @JsonIgnore
    // FIXME:
    // ZonedDateTime is no longer supported;
    // LocalDateTime is not supported yet;
    // Switch to a better type matching "TIMESTAMP WITHOUT TIME ZONE" when possible
    private java.util.Date lastModifiedDate = new java.util.Date();

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public ZonedDateTime getCreatedDate() {
        if (createdDate == null) {
            return null;
        }
        return createdDate.toInstant().atZone(java.time.ZoneId.systemDefault());
    }

    public void setCreatedDate(ZonedDateTime createdDate) {
        if (createdDate == null) {
            this.createdDate = null;
            return;
        }
        this.createdDate = java.util.Date.from(createdDate.toInstant());
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public ZonedDateTime getLastModifiedDate() {
        if (lastModifiedDate == null) {
            return null;
        }
        return lastModifiedDate.toInstant().atZone(java.time.ZoneId.systemDefault());
    }

    public void setLastModifiedDate(ZonedDateTime lastModifiedDate) {
        if (lastModifiedDate == null) {
            this.lastModifiedBy = null;
            return;
        }
        this.lastModifiedDate = java.util.Date.from(lastModifiedDate.toInstant());
    }
}
