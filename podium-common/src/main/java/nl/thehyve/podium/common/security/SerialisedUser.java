/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.common.security;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

public class SerialisedUser implements AuthenticatedUser, Serializable {

    private UUID uuid;

    private String name;

    private String password = null;

    private Collection<String> authorityNames;

    private Map<UUID, Collection<String>> organisationAuthorities;

    public SerialisedUser(UUID uuid, String name,
                          Collection<String> authorityNames,
                          Map<UUID, Collection<String>> organisationAuthorities) {
        this.uuid = uuid;
        this.name = name;
        this.authorityNames = authorityNames == null ? Collections.emptyList() : authorityNames;
        this.organisationAuthorities = organisationAuthorities == null ? Collections.emptyMap() : organisationAuthorities;
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<String> getAuthorityNames() {
        return authorityNames;
    }

    @Override
    public Map<UUID, Collection<String>> getOrganisationAuthorities() {
        return organisationAuthorities;
    }

    @Override
    public UUID getUserUuid() {
        return getUuid();
    }

}
