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

import nl.thehyve.podium.common.enumeration.DecisionOutcome;

import java.time.ZonedDateTime;
import java.util.UUID;

public class ReviewFeedbackRepresentation {
    private Long id;

    private UUID reviewer;

    private DecisionOutcome advice;

    private ZonedDateTime date;

    private MessageRepresentation message;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getReviewer() {
        return reviewer;
    }

    public void setReviewer(UUID reviewer) {
        this.reviewer = reviewer;
    }

    public DecisionOutcome getAdvice() {
        return advice;
    }

    public void setAdvice(DecisionOutcome advice) {
        this.advice = advice;
    }

    public ZonedDateTime getDate() {
        return date;
    }

    public void setDate(ZonedDateTime date) {
        this.date = date;
    }

    public MessageRepresentation getMessage() {
        return message;
    }

    public void setMessage(MessageRepresentation message) {
        this.message = message;
    }
}
