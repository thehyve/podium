package org.bbmri.podium.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.*;
import java.util.stream.Collectors;

public class UserAuthenticationToken extends AbstractAuthenticationToken {

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

}
