/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.domain;

import nl.thehyve.podium.common.domain.AbstractAuditingEntity;
import nl.thehyve.podium.common.enumeration.DeliveryProcessOutcome;
import nl.thehyve.podium.common.enumeration.DeliveryStatus;
import nl.thehyve.podium.common.enumeration.RequestType;
import org.hibernate.annotations.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.Parameter;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "delivery_process")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "deliveryprocess")
public class DeliveryProcess extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "delivery_process_gen")
    @GenericGenerator(
        name = "delivery_process_gen",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
            @Parameter(name = "sequence_name", value = "delivery_process_seq"),
            @Parameter(name = "initial_value", value = "1000"),
            @Parameter(name = "increment_size", value = "50")
        }
    )
    private Long id;

    @Column(unique = true, nullable = false)
    private UUID uuid;

    @Column(name = "process_instance_id", nullable = false)
    private String processInstanceId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DeliveryStatus status = DeliveryStatus.None;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", nullable = false)
    private DeliveryProcessOutcome outcome = DeliveryProcessOutcome.None;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private RequestType type;

    @Column(name = "reference")
    private String reference;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Fetch(FetchMode.JOIN)
    @BatchSize(size = 1000)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @OrderColumn(name="event_order")
    @JoinTable(name = "delivery_process_historic_events",
        joinColumns = @JoinColumn(name="delivery_process_id", referencedColumnName="id"),
        inverseJoinColumns = @JoinColumn(name="event_id", referencedColumnName="event_id"))
    private List<PodiumEvent> historicEvents = new ArrayList<>();

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

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public DeliveryStatus getStatus() {
        return status;
    }

    public void setStatus(DeliveryStatus status) {
        this.status = status;
    }

    public DeliveryProcessOutcome getOutcome() {
        return outcome;
    }

    public void setOutcome(DeliveryProcessOutcome outcome) {
        this.outcome = outcome;
    }

    public RequestType getType() {
        return type;
    }

    public void setType(RequestType type) {
        this.type = type;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public List<PodiumEvent> getHistoricEvents() {
        return historicEvents;
    }

    public DeliveryProcess addHistoricEvent(PodiumEvent event) {
        this.historicEvents.add(event);
        return this;
    }

    public void setHistoricEvents(List<PodiumEvent> historicEvents) {
        this.historicEvents = historicEvents;
    }

}
