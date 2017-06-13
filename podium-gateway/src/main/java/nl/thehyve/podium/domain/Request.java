/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.domain;

import nl.thehyve.podium.common.IdentifiableRequest;
import nl.thehyve.podium.common.IdentifiableUser;
import nl.thehyve.podium.common.domain.AbstractAuditingEntity;
import nl.thehyve.podium.common.enumeration.RequestOutcome;
import nl.thehyve.podium.common.enumeration.RequestStatus;
import org.hibernate.annotations.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.Parameter;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.*;

/**
 * A Request.
 */
@Entity
@Table(name = "request")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "request")
public class Request extends AbstractAuditingEntity implements Serializable, IdentifiableUser, IdentifiableRequest {

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

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", nullable = false)
    private RequestOutcome outcome = RequestOutcome.None;

    @ElementCollection(targetClass = java.util.UUID.class)
    @CollectionTable(
        name="request_organisations",
        joinColumns=@JoinColumn(name="request_id")
    )
    @Column(name = "organisation_uuid")
    private Set<UUID> organisations = new HashSet<>();

    @OneToOne(cascade = {CascadeType.ALL})
    @JoinColumn(unique = true, name = "revision_detail")
    private RequestDetail revisionDetail;

    @OneToOne(cascade = {CascadeType.ALL})
    @JoinColumn(unique = true, name = "request_detail")
    private RequestDetail requestDetail;

    @OneToOne
    @JoinColumn(unique = true, name = "request_review_process")
    private RequestReviewProcess requestReviewProcess;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Fetch(FetchMode.JOIN)
    @BatchSize(size = 1000)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @OrderColumn(name="delivery_process_order")
    @JoinTable(name = "request_delivery_processes",
        joinColumns = @JoinColumn(name="request_id", referencedColumnName="id"),
        inverseJoinColumns = @JoinColumn(name="delivery_process_id", referencedColumnName="id"))
    private List<DeliveryProcess> deliveryProcesses;

    @Column(nullable = false)
    private UUID requester;

    @ManyToMany
    @Fetch(FetchMode.JOIN)
    @BatchSize(size = 1000)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JoinTable(name = "request_attachments",
               joinColumns = @JoinColumn(name="request_id", referencedColumnName="id"),
               inverseJoinColumns = @JoinColumn(name="attachment_id", referencedColumnName="id"))
    private Set<Attachment> attachments = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Fetch(FetchMode.JOIN)
    @BatchSize(size = 1000)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @OrderColumn(name="event_order")
    @JoinTable(name = "request_historic_events",
        joinColumns = @JoinColumn(name="request_id", referencedColumnName="id"),
        inverseJoinColumns = @JoinColumn(name="event_id", referencedColumnName="event_id"))
    private List<PodiumEvent> historicEvents = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Fetch(FetchMode.JOIN)
    @BatchSize(size = 1000)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @OrderColumn(name="review_round_order")
    @JoinTable(name = "request_review_rounds",
        joinColumns = @JoinColumn(name="request_id", referencedColumnName="id"),
        inverseJoinColumns = @JoinColumn(name="review_round_id", referencedColumnName="review_round_id"))
    private List<ReviewRound> reviewRounds;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getUuid() {
        return uuid;
    }

    @Override
    public UUID getRequestUuid() {
        return uuid;
    }

    /**
     * Only the database can return the UUID from the stored entity
     * Pre-persist will add a {@link UUID} to the entity
     * This setter is only added to satisfy mapstruct e.g.
     *
     * @param uuid is ignored.
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

    public RequestOutcome getOutcome() {
        return outcome;
    }

    public void setOutcome(RequestOutcome outcome) {
        this.outcome = outcome;
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

    public RequestDetail getRevisionDetail() { return revisionDetail; }

    public void setRevisionDetail(RequestDetail revisionDetail) { this.revisionDetail = revisionDetail; }

    public Request revisionDetail(RequestDetail revisionDetail) {
        this.revisionDetail = revisionDetail;
        return this;
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

    public List<DeliveryProcess> getDeliveryProcesses() {
        return deliveryProcesses;
    }

    public Request addDeliveryProcess(DeliveryProcess deliveryProcess) {
        this.deliveryProcesses.add(deliveryProcess);
        return this;
    }

    public void setDeliveryProcesses(List<DeliveryProcess> deliveryProcesses) {
        this.deliveryProcesses = deliveryProcesses;
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

    public List<PodiumEvent> getHistoricEvents() {
        return historicEvents;
    }

    public Request addHistoricEvent(PodiumEvent event) {
        this.historicEvents.add(event);
        return this;
    }

    public void setHistoricEvents(List<PodiumEvent> historicEvents) {
        this.historicEvents = historicEvents;
    }

    public List<ReviewRound> getReviewRounds() {
        return reviewRounds;
    }

    public void setReviewRounds(List<ReviewRound> reviewRounds) {
        this.reviewRounds = reviewRounds;
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
