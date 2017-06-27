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
import nl.thehyve.podium.common.enumeration.ReviewProcessOutcome;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
public class ReviewFeedbackRepresentation {
    private Long id;

    private UUID uuid;

    private UserRepresentation reviewer;

    private ReviewProcessOutcome advice;

    private ZonedDateTime date;

    private MessageRepresentation message;
}
