/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.service.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

/**
 * A DTO for the External Request Template entity.
 */
@Data
public class ExternalRequestTemplateRepresentation {
    Long id;

    UUID uuid;

    String url;
    String humanReadable;

    List<String> organizationIds;

    String nToken;
}
