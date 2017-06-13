/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.common.service.dto;

import nl.thehyve.podium.common.enumeration.DeliveryProcessOutcome;
import nl.thehyve.podium.common.enumeration.DeliveryStatus;
import nl.thehyve.podium.common.enumeration.RequestType;

import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Representation class for instances of the delivery process,
 * defined as a BPMN 2.0 process.
 */
public class DeliveryProcessRepresentation implements Serializable {

    private UUID uuid;

    private DeliveryStatus status = DeliveryStatus.None;

    private DeliveryProcessOutcome outcome = DeliveryProcessOutcome.None;

    private RequestType type;

    @Size(max = 2000)
    private String reference;

    private ZonedDateTime createdDate;

    private ZonedDateTime lastModifiedDate;

    private List<PodiumEventRepresentation> historicEvents;

    public DeliveryProcessRepresentation() {

    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
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

    public ZonedDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(ZonedDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public ZonedDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(ZonedDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public List<PodiumEventRepresentation> getHistoricEvents() {
        return historicEvents;
    }

    public void setHistoricEvents(List<PodiumEventRepresentation> historicEvents) {
        this.historicEvents = historicEvents;
    }

}
