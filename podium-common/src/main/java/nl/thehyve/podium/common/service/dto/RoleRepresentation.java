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

import java.util.Set;
import java.util.UUID;

@Data
public class RoleRepresentation implements IdentifiableOrganisation {

    private Long id;

    private UUID organisation;

    private String authority;

    private Set<UUID> users;

    @JsonIgnore
    public UUID getOrganisationUuid() {
        return this.organisation;
    }

    public RoleRepresentation() {
    }

}
