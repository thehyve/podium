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

import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class ReviewRoundRepresentation {
    private Long id;

    private UUID uuid;

    private RequestDetailRepresentation requestDetail;

    private List<ReviewFeedbackRepresentation> reviewFeedback;

    private ZonedDateTime startDate;

    private ZonedDateTime endDate;

    private UUID initiatedBy;
}
