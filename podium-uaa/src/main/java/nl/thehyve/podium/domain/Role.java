/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.thehyve.podium.common.IdentifiableOrganisation;
import nl.thehyve.podium.common.service.dto.RoleRepresentation;
import nl.thehyve.podium.repository.RoleRepository;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * A Role.
 */
@Entity
@Table(name = "podium_role")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "role")
public class Role implements Serializable, IdentifiableOrganisation {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "role_seq_gen")
    @GenericGenerator(
        name = "role_seq_gen",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
            @Parameter(name = "sequence_name", value = "role_seq"),
            @Parameter(name = "initial_value", value = "1000"),
            @Parameter(name = "increment_size", value = "50")
        }
    )
    private Long id;

    @ManyToMany
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JoinTable(name = "role_users",
               joinColumns = @JoinColumn(name="roles_id", referencedColumnName="id"),
               inverseJoinColumns = @JoinColumn(name="users_id", referencedColumnName="id"))
    private Set<User> users = new HashSet<>();

    @ManyToOne
    private Organisation organisation;

    @JsonProperty()
    @ManyToOne
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JoinColumn(name = "authority_name", referencedColumnName = "name")
    private Authority authority;

    public Role() {

    }

    public Role(Authority authority, Organisation organisation) {
        this.authority = authority;
        this.organisation = organisation;
    }

    public Role(Authority authority) {
        this.authority = authority;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<User> getUsers() {
        return users;
    }

    public Role users(Set<User> users) {
        this.users = users;
        return this;
    }

    public Role addUsers(User user) {
        this.users.add(user);
        user.getRoles().add(this);
        return this;
    }

    public Role removeUsers(User user) {
        this.users.remove(user);
        user.getRoles().remove(this);
        return this;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public Organisation getOrganisation() {
        return organisation;
    }

    public UUID getOrganisationUuid() {
        if (getOrganisation() != null) {
            return getOrganisation().getOrganisationUuid();
        }
        return null;
    }

    public Role organisation(Organisation organisation) {
        this.organisation = organisation;
        return this;
    }

    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
    }

    public Authority getAuthority() {
        return authority;
    }

    public Role authority(Authority authority) {
        this.authority = authority;
        return this;
    }

    public void setAuthority(Authority authority) {
        this.authority = authority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Role role = (Role) o;
        if (role.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, role.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Role{" +
            "id=" + id +
            '}';
    }

    // FIXME: Replace with mapper
    public RoleRepresentation toRepresentation() {
        RoleRepresentation role = new RoleRepresentation();
        role.setId(this.getId());
        role.setOrganisation(this.getOrganisation() != null ? this.getOrganisation().getUuid() : null);
        role.setAuthority(this.getAuthority().getName());
        role.setUsers(this.getUsers().stream().map(User::getUuid).collect(Collectors.toSet()));
        return role;
    }
}
