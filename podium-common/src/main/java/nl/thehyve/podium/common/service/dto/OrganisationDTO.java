/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.common.service.dto;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import nl.thehyve.podium.common.IdentifiableOrganisation;
import nl.thehyve.podium.common.enumeration.RequestType;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * A DTO for the Organisation entity.
 */
@Data
public class OrganisationDTO implements IdentifiableOrganisation, Serializable {

    private Long id;

    @NotNull
    private String name;

    @NotNull
    @Size(max = 50)
    private String shortName;

    private Boolean activated;

    private UUID uuid;

    private Set<RequestType> requestTypes;

    @JsonIgnore
    @Override
    public UUID getOrganisationUuid() {
        return uuid;
    }
}

