/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.common.service.dto;


import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * A DTO for the Organisation entity.
 */
public class OrganisationDTO implements Serializable {

    private Long id;

    @NotNull
    private String name;

    @NotNull
    @Size(max = 50)
    private String shortName;

    private Boolean deleted;

    private Boolean activated;

    private UUID uuid;

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

    public Boolean getDeleted() {
        return deleted;
    }
    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
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
            ", deleted='" + deleted + "'" +
            ", activated='" + activated + "'" +
            ", uuid='" + uuid + "'" +
            '}';
    }
}
