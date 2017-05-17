/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.common.security;

import nl.thehyve.podium.common.IdentifiableUser;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class UserAuthenticationToken extends AbstractAuthenticationToken implements IdentifiableUser {

    private final AuthenticatedUser user;
    private final Map<UUID, Collection<String>> organisationRoles;

    static Set<GrantedAuthority> getAuthorities(AuthenticatedUser user) {
        Set<GrantedAuthority> result = user.getAuthorityNames().stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toSet());
        return result;
    }

    static Map<UUID, Collection<String>> getOrganisationRoles(AuthenticatedUser user) {
        return Collections.unmodifiableMap(user.getOrganisationAuthorities());
    }

    public UserAuthenticationToken(AuthenticatedUser user) {
        super(getAuthorities(user));
        this.user = user;
        this.organisationRoles = getOrganisationRoles(user);
    }

    public AuthenticatedUser getUser() {
        return user;
    }

    public UUID getUuid() {
        return user.getUuid();
    }

    public UUID getUserUuid() { return user.getUuid(); }

    public Map<UUID, Collection<String>> getOrganisationRoles() {
        return organisationRoles;
    }

    @Override
    public Object getCredentials() {
        return user.getPassword();
    }

    @Override
    public Object getPrincipal() {
        return user;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName()).append(": ");
        sb.append("Username: ").append(this.getName()).append("; ");
        sb.append("UUID: ").append(this.getUserUuid().toString()).append("; ");
        sb.append("Password: [PROTECTED]; ");
        sb.append("Authenticated: ").append(this.isAuthenticated()).append("; ");

        Collection<GrantedAuthority> authorities = getAuthorities();
        if (!authorities.isEmpty()) {
            sb.append("Granted authorities: ");
            int i = 0;
            for (GrantedAuthority authority : authorities) {
                if (i++ > 0) {
                    sb.append(", ");
                }
                sb.append(authority);
            }
        } else {
            sb.append("Not granted any authorities");
        }
        sb.append("; ");

        if (!organisationRoles.isEmpty()) {
            sb.append("Organisation roles: ");

            int i = 0;
            for (Map.Entry<UUID, Collection<String>> roles : organisationRoles.entrySet()) {
                if (i++ > 0) {
                    sb.append(", ");
                }

                sb.append(roles.getKey().toString());
                sb.append(": [");
                int j = 0;
                for (String role: roles.getValue()) {
                    sb.append(role);
                    if (j++ > 0) {
                        sb.append(", ");
                    }
                }
                sb.append("]");
            }
        }
        else {
            sb.append("Not granted any organisation roles");
        }
        sb.append(".");
        return sb.toString();
    }

}
