/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

package nl.thehyve.podium.common.service.dto;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public class ReviewRoundRepresentation {
    private Long id;

    private RequestDetailRepresentation requestDetail;

    private List<ReviewFeedbackRepresentation> reviewFeedback;

    private ZonedDateTime startDate;

    private ZonedDateTime endDate;

    private UUID initiatedBy;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RequestDetailRepresentation getRequestDetail() {
        return requestDetail;
    }

    public void setRequestDetail(RequestDetailRepresentation requestDetail) {
        this.requestDetail = requestDetail;
    }

    public List<ReviewFeedbackRepresentation> getReviewFeedback() {
        return reviewFeedback;
    }

    public void setReviewFeedback(List<ReviewFeedbackRepresentation> reviewFeedback) {
        this.reviewFeedback = reviewFeedback;
    }

    public ZonedDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(ZonedDateTime startDate) {
        this.startDate = startDate;
    }

    public ZonedDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(ZonedDateTime endDate) {
        this.endDate = endDate;
    }

    public UUID getInitiatedBy() {
        return initiatedBy;
    }

    public void setInitiatedBy(UUID initiatedBy) {
        this.initiatedBy = initiatedBy;
    }
}
