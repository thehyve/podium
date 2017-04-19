/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.service.representation;

import nl.thehyve.podium.common.enumeration.DecisionOutcome;
import nl.thehyve.podium.common.enumeration.RequestReviewStatus;
import nl.thehyve.podium.domain.RequestReviewProcess;

import java.io.Serializable;

/**
 * Representation class for instances of the request review process,
 * defined as a BPMN 2.0 process.
 */
public class RequestReviewRepresentation implements Serializable {

    private Long id;

    private String processInstanceId;

    private RequestReviewStatus status = RequestReviewStatus.None;

    private DecisionOutcome decision = DecisionOutcome.None;

    public RequestReviewRepresentation() {

    }

    public RequestReviewRepresentation(RequestReviewProcess requestReview) {
        this.id = requestReview.getId();
        this.processInstanceId = requestReview.getProcessInstanceId();
        this.status = requestReview.getStatus();
        this.decision = requestReview.getDecision();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public RequestReviewStatus getStatus() {
        return status;
    }

    public void setStatus(RequestReviewStatus status) {
        this.status = status;
    }

    public DecisionOutcome getDecision() {
        return decision;
    }

    public void setDecision(DecisionOutcome decision) {
        this.decision = decision;
    }

}
