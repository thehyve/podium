/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.common.service.dto;

import lombok.Data;
import nl.thehyve.podium.common.enumeration.ReviewProcessOutcome;
import nl.thehyve.podium.common.enumeration.RequestReviewStatus;

import java.io.Serializable;

/**
 * Representation class for instances of the request review process,
 * defined as a BPMN 2.0 process.
 */
@Data
public class RequestReviewRepresentation implements Serializable {

    private Long id;

    private String processInstanceId;

    private RequestReviewStatus status = RequestReviewStatus.None;

    private ReviewProcessOutcome decision = ReviewProcessOutcome.None;

}
