/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.common.service.dto;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import nl.thehyve.podium.common.IdentifiableRequest;
import nl.thehyve.podium.common.enumeration.OverviewStatus;
import nl.thehyve.podium.common.enumeration.RequestOutcome;
import nl.thehyve.podium.common.enumeration.RequestStatus;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * A DTO for the Request entity.
 */
@Data
public class RequestRepresentation implements IdentifiableRequest, Serializable {

    private Long id;

    private UUID uuid;

    private UserRepresentation requester;

    private OverviewStatus status;

    private List<OrganisationRepresentation> organisations = new ArrayList<>();

    private RequestReviewRepresentation requestReview;

    private RequestDetailRepresentation revisionDetail;

    @Valid
    private RequestDetailRepresentation requestDetail;

    private ZonedDateTime createdDate;

    private ZonedDateTime lastModifiedDate;

    private PodiumEventRepresentation latestEvent;

    private ReviewRoundRepresentation reviewRound;

    private Set<RequestRepresentation> relatedRequests;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", nullable = false)
    private RequestOutcome outcome = RequestOutcome.None;

    @Override
    @JsonIgnore
    public UUID getRequestUuid() {
        return uuid;
    }

}
