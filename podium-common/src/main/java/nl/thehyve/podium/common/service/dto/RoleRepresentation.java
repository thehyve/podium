/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.common.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import nl.thehyve.podium.common.IdentifiableOrganisation;

import java.util.Set;
import java.util.UUID;

public class RoleRepresentation implements IdentifiableOrganisation {

    private Long id;

    private UUID organisation;

    private String authority;

    private Set<UUID> users;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getOrganisation() {
        return organisation;
    }

    @JsonIgnore
    public UUID getOrganisationUuid() {
        return getOrganisation();
    }

    public void setOrganisation(UUID organisation) {
        this.organisation = organisation;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public Set<UUID> getUsers() {
        return users;
    }

    public void setUsers(Set<UUID> users) {
        this.users = users;
    }

    public RoleRepresentation() {
    }

}
