/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.common.service.dto;


import com.fasterxml.jackson.annotation.JsonIgnore;
import nl.thehyve.podium.common.IdentifiableRequest;
import nl.thehyve.podium.common.enumeration.RequestStatus;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * A DTO for the Request entity.
 */
public class RequestRepresentation implements IdentifiableRequest, Serializable {

    private Long id;

    private UUID uuid;

    private UUID requester;

    @NotNull
    private RequestStatus status;

    private List<OrganisationDTO> organisations = new ArrayList<>();

    private RequestReviewRepresentation requestReview;

    private RequestDetailRepresentation revisionDetail;

    @Valid
    private RequestDetailRepresentation requestDetail;

    private ZonedDateTime createdDate;

    private ZonedDateTime lastModifiedDate;

    private List<PodiumEventRepresentation> historicEvents;

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
    @JsonIgnore
    public UUID getRequestUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getRequester() {
        return requester;
    }

    public void setRequester(UUID requester) {
        this.requester = requester;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public List<OrganisationDTO> getOrganisations() {
        return organisations;
    }

    public void setOrganisations(List<OrganisationDTO> organisations) {
        this.organisations = organisations;
    }

    public RequestDetailRepresentation getRevisionDetail() { return revisionDetail; }

    public void setRevisionDetail(RequestDetailRepresentation revisionDetail) { this.revisionDetail = revisionDetail; }

    public RequestDetailRepresentation getRequestDetail() {
        return requestDetail;
    }

    public void setRequestDetail(RequestDetailRepresentation requestDetail) {
        this.requestDetail = requestDetail;
    }

    public RequestReviewRepresentation getRequestReview() { return requestReview; }

    public void setRequestReview(RequestReviewRepresentation requestReview) { this.requestReview = requestReview; }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RequestRepresentation requestDTO = (RequestRepresentation) o;

        if ( ! Objects.equals(id, requestDTO.id)) { return false; }

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "RequestDTO{" +
            "id=" + id +
            ", status=" + status +
            ", organisations=" + organisations +
            ", requestDetail=" + requestDetail +
            '}';
    }
}
