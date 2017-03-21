/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.domain;

import nl.thehyve.podium.common.IdentifiableUser;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Cache;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;
import java.util.UUID;

import nl.thehyve.podium.domain.enumeration.RequestStatus;

/**
 * A Request.
 */
@Entity
@Table(name = "request")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "request")
public class Request extends AbstractAuditingEntity implements Serializable, IdentifiableUser {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "request_seq_gen")
    @GenericGenerator(
        name = "request_seq_gen",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
            @Parameter(name = "sequence_name", value = "request_seq"),
            @Parameter(name = "initial_value", value = "1000"),
            @Parameter(name = "increment_size", value = "50")
        }
    )
    private Long id;

    @Column(unique = true, nullable = false)
    private UUID uuid;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RequestStatus status;

    @ElementCollection(targetClass = java.util.UUID.class)
    @CollectionTable(
        name="request_organisations",
        joinColumns=@JoinColumn(name="request_id")
    )
    @Column(name = "organisation_uuid")
    private Set<UUID> organisations = new HashSet<>();

    @ManyToOne
    private Request parentRequest;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(unique = true, name = "request_detail")
    private RequestDetail requestDetail;

    @OneToOne
    @JoinColumn(unique = true, name = "request_review_process")
    private RequestReviewProcess requestReviewProcess;

    @Column(nullable = false)
    private UUID requester;

    @ManyToMany
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JoinTable(name = "request_attachments",
               joinColumns = @JoinColumn(name="request_id", referencedColumnName="id"),
               inverseJoinColumns = @JoinColumn(name="attachment_id", referencedColumnName="id"))
    private Set<Attachment> attachments = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getUuid() {
        return uuid;
    }

    /**
     * Void.
     */
    public void setUuid(UUID uuid) {
        // pass
    }

    @PrePersist
    public void generateUuid() {
        if (this.uuid == null) {
            this.uuid = UUID.randomUUID();
        }
    }

    public RequestStatus getStatus() {
        return status;
    }

    public Request status(RequestStatus status) {
        this.status = status;
        return this;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public Set<UUID> getOrganisations() {
        return organisations;
    }

    public Request organisations(Set<UUID> organisations) {
        this.organisations = organisations;
        return this;
    }

    public Request addOrganisations(UUID organisation) {
        this.organisations.add(organisation);
        return this;
    }

    public Request removeOrganisations(UUID organisation) {
        this.organisations.remove(organisation);
        return this;
    }

    public void setOrganisations(Set<UUID> organisations) {
        this.organisations = organisations;
    }

    public Request getParentRequest() {
        return parentRequest;
    }

    public Request parentRequest(Request request) {
        this.parentRequest = request;
        return this;
    }

    public void setParentRequest(Request request) {
        this.parentRequest = request;
    }

    public RequestDetail getRequestDetail() {
        return requestDetail;
    }

    public Request requestDetail(RequestDetail requestDetail) {
        this.requestDetail = requestDetail;
        return this;
    }

    public void setRequestDetail(RequestDetail requestDetail) {
        this.requestDetail = requestDetail;
    }

    public RequestReviewProcess getRequestReviewProcess() {
        return requestReviewProcess;
    }

    public void setRequestReviewProcess(RequestReviewProcess requestReviewProcess) {
        this.requestReviewProcess = requestReviewProcess;
    }

    public Set<Attachment> getAttachments() {
        return attachments;
    }

    public Request attachments(Set<Attachment> attachments) {
        this.attachments = attachments;
        return this;
    }

    public Request addAttachments(Attachment attachment) {
        this.attachments.add(attachment);
        return this;
    }

    public Request removeAttachments(Attachment attachment) {
        this.attachments.remove(attachment);
        return this;
    }

    public void setAttachments(Set<Attachment> attachments) {
        this.attachments = attachments;
    }

    public UUID getRequester() {
        return requester;
    }

    public void setRequester(UUID requester) {
        this.requester = requester;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Request request = (Request) o;
        if (request.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, request.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Request{" +
            "id=" + id +
            ", status='" + status + "'" +
            '}';
    }

    @Override
    public UUID getUserUuid() {
        return requester;
    }
}
