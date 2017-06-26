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
import nl.thehyve.podium.common.event.EventType;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

@Data
public class PodiumEventRepresentation implements Serializable {
    private Long id;

    private String principal;

    private Date eventDate;
    private EventType eventType;

    private Map<String, String> data;

}
