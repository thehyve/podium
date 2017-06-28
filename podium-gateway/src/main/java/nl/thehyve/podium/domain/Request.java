/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.domain;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
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
@Data
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
    @Setter(AccessLevel.NONE)
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

    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @Fetch(FetchMode.JOIN)
    @BatchSize(size = 1000)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JoinTable(name = "request_related_requests",
        joinColumns = @JoinColumn(name="request_id", referencedColumnName="id"),
        inverseJoinColumns = @JoinColumn(name="related_request_id", referencedColumnName="id"))
    private Set<Request> relatedRequests = new HashSet<>();

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

    public Request status(RequestStatus status) {
        this.status = status;
        return this;
    }

    public Request organisations(Set<UUID> organisations) {
        this.organisations = organisations;
        return this;
    }

    public Request requestDetail(RequestDetail requestDetail) {
        this.requestDetail = requestDetail;
        return this;
    }

    public Request addDeliveryProcess(DeliveryProcess deliveryProcess) {
        this.deliveryProcesses.add(deliveryProcess);
        return this;
    }

    public Request addHistoricEvent(PodiumEvent event) {
        this.historicEvents.add(event);
        return this;
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
