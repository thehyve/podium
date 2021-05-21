/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.common.service.dto;

import lombok.Data;
import nl.thehyve.podium.common.enumeration.DeliveryProcessOutcome;
import nl.thehyve.podium.common.enumeration.DeliveryStatus;
import nl.thehyve.podium.common.enumeration.RequestType;

import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Representation class for instances of the delivery process,
 * defined as a BPMN 2.0 process.
 */
@Data
public class DeliveryProcessRepresentation implements Serializable {

    private UUID uuid;

    private DeliveryStatus status = DeliveryStatus.None;

    private DeliveryProcessOutcome outcome = DeliveryProcessOutcome.None;

    private RequestType type;

    @Size(max = 2000)
    private String reference;

    private LocalDateTime createdDate;

    private LocalDateTime lastModifiedDate;

    private List<PodiumEventRepresentation> historicEvents;

}
