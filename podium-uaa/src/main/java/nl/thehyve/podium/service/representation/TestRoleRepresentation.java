/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.service.representation;

import nl.thehyve.podium.domain.Role;
import nl.thehyve.podium.domain.User;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Same as {@link RoleRepresentation}, but using user login instead of uuid
 * and organisation short name instead of uuid.
 * For testing purposes.
 */
public class TestRoleRepresentation {

    private Long id;

    /**
     * Short name of the organisation
     */
    private String organisation;

    private String authority;

    /**
     * Set of user logins.
     */
    private Set<String> users;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrganisation() {
        return organisation;
    }

    public void setOrganisation(String organisation) {
        this.organisation = organisation;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public Set<String> getUsers() {
        return users;
    }

    public void setUsers(Set<String> users) {
        this.users = users;
    }

    public TestRoleRepresentation() {
    }

    public TestRoleRepresentation(Role role) {
        this.id = role.getId();
        this.organisation = role.getOrganisation() != null ? role.getOrganisation().getShortName() : null;
        this.authority = role.getAuthority().getName();
        this.users = role.getUsers().stream().map(User::getLogin).collect(Collectors.toSet());
    }

}
