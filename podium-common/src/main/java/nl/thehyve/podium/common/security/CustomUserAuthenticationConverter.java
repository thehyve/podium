/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.common.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.thehyve.podium.common.IdentifiableUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.provider.token.UserAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component("userAuthenticationConverter")
public class CustomUserAuthenticationConverter implements UserAuthenticationConverter {

    private final Logger log = LoggerFactory.getLogger(CustomUserAuthenticationConverter.class);

    private static final String UUID_KEY = "uuid";
    private static final String ORGANISATION_ROLES_KEY = "organisation_roles";

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public Map<String, ?> convertUserAuthentication(Authentication authentication) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put(USERNAME, authentication.getName());
        if (authentication.getAuthorities() != null && !authentication.getAuthorities().isEmpty()) {
            response.put(AUTHORITIES, AuthorityUtils.authorityListToSet(authentication.getAuthorities()));
        }
        if (authentication instanceof IdentifiableUser) {
            response.put(UUID_KEY, ((IdentifiableUser) authentication).getUserUuid().toString());
        }
        log.debug("Authentication = {}", authentication);
        if (authentication instanceof AuthenticatedUser) {
            addOrganisationRoles(response, ((AuthenticatedUser) authentication).getOrganisationAuthorities());
        } else if (authentication instanceof UserAuthenticationToken) {
            addOrganisationRoles(response, ((UserAuthenticationToken) authentication).getOrganisationRoles());
        }
        return response;
    }

    private void addOrganisationRoles(Map<String, Object> response, Map<UUID, Collection<String>> organisationAuthorities) {
        try {
            String organisationRoles = mapper.writeValueAsString(organisationAuthorities);
            response.put(ORGANISATION_ROLES_KEY, organisationRoles);
        } catch (JsonProcessingException e) {
            log.error("Error serialising organisation roles.", e);
        }
    }

    @Override
    public Authentication extractAuthentication(Map<String, ?> map) {
        if (map.containsKey(USERNAME)) {
            String username = (String) map.get(USERNAME);
            UUID uuid = null;
            Object uuidObject = map.get(UUID_KEY);
            if (uuidObject != null) {
                if (uuidObject instanceof UUID) {
                    uuid = (UUID) uuidObject;
                } else if (uuidObject instanceof String) {
                    uuid = UUID.fromString((String)uuidObject);
                }
            }
            Collection<String> authorities = getAuthorities(map);
            Map<UUID, Collection<String>> organisationAuthorities = getOrganisationRoles(map);
            AuthenticatedUser user = new SerialisedUser(uuid, username, authorities, organisationAuthorities);
            UserAuthenticationToken authentication = new UserAuthenticationToken(user);
            authentication.setAuthenticated(true);
            return authentication;
        }
        return null;
    }

    private Map<UUID, Collection<String>> toOrganisationRolesMap(Map<?, ?> map) {
        Map<UUID, Collection<String>> result = new HashMap<>();
        for(Map.Entry entry: map.entrySet()) {
            Object key = entry.getKey();
            UUID organisationUuid;
            if (key instanceof UUID) {
                organisationUuid = (UUID)key;
            } else {
                organisationUuid = UUID.fromString(key.toString());
            }
            Object value = entry.getValue();
            if (value instanceof Collection) {
                Collection<String> roles =
                    ((Collection<?>)value).stream().map(Object::toString).collect(Collectors.toList());
                result.put(organisationUuid, roles);
            }
        }
        return result;
    }

    private Map<UUID, Collection<String>> getOrganisationRoles(Map<String, ?> map) {
        if (!map.containsKey(ORGANISATION_ROLES_KEY)) {
            log.debug("No key {}", ORGANISATION_ROLES_KEY);
            return Collections.emptyMap();
        }
        Object organisationRoles = map.get(ORGANISATION_ROLES_KEY);
        if (organisationRoles instanceof String) {
            try {
                Map roles = mapper.readValue((String)organisationRoles, Map.class);
                return toOrganisationRolesMap(roles);
            } catch (IOException e) {
                log.error("Error deserialising organisation roles.", e);
            }
        } else if (organisationRoles instanceof Map) {
            return toOrganisationRolesMap((Map)organisationRoles);
        }
        log.error("Organisation role of wrong type: {}", organisationRoles);
        return Collections.emptyMap();
    }

    private List<String> getAuthorities(Map<String, ?> map) {
        if (!map.containsKey(AUTHORITIES)) {
            return Collections.emptyList();
        }
        Object authorities = map.get(AUTHORITIES);
        if (authorities instanceof String) {
            return Arrays.asList(StringUtils.tokenizeToStringArray((String)authorities, ","));
        }
        if (authorities instanceof Collection) {
            return ((Collection<?>) authorities).stream().map(Object::toString).collect(Collectors.toList());
        }
        throw new IllegalArgumentException("Authorities must be either a String or a Collection");
    }
}
