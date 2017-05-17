/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.common.service.dto;


import com.fasterxml.jackson.annotation.JsonIgnore;
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

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public Boolean getActivated() {
        return activated;
    }
    public void setActivated(Boolean activated) {
        this.activated = activated;
    }

    public UUID getUuid() {
        return uuid;
    }
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @JsonIgnore
    @Override
    public UUID getOrganisationUuid() {
        return uuid;
    }

    public Set<RequestType> getRequestTypes() { return requestTypes; }

    public void setRequestTypes(Set<RequestType> requestTypes) {
        this.requestTypes = requestTypes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        OrganisationDTO organisationDTO = (OrganisationDTO) o;

        if ( ! Objects.equals(id, organisationDTO.id)) { return false; }

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "OrganisationDTO{" +
            "id=" + id +
            ", name='" + name + "'" +
            ", shortName='" + shortName + "'" +
            ", activated='" + activated + "'" +
            ", uuid='" + uuid + "'" +
            '}';
    }

}

