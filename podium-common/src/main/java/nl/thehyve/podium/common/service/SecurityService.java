/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.common.service;

import nl.thehyve.podium.common.resource.InternalUserResource;
import nl.thehyve.podium.common.security.AuthenticatedUser;
import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.common.security.SerialisedUser;
import nl.thehyve.podium.common.security.UserAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@Service
@Transactional
public class SecurityService {

    private static final Logger log = LoggerFactory.getLogger(SecurityService.class);

    @Autowired
    private InternalUserResource internalUserResource;

    /**
     * Check if a user is authenticated.
     *
     * @return true if the user is authenticated, false otherwise
     */
    public static boolean isAuthenticated() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            if (authentication instanceof OAuth2Authentication) {
                if (((OAuth2Authentication) authentication).isClientOnly()) {
                    return false;
                }
            }
            return authentication.getAuthorities().stream()
                .noneMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(AuthorityConstants.ANONYMOUS));
        }
        return false;
    }

    public static String getCurrentUserLogin() {
        if (!isAuthenticated()) {
            log.debug("User not authenticated.");
            return null;
        }
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        String userName = null;
        if (authentication != null) {
            if (authentication.getPrincipal() instanceof UserDetails) {
                UserDetails springSecurityUser = (UserDetails) authentication.getPrincipal();
                userName = springSecurityUser.getUsername();
            } else if (authentication.getPrincipal() instanceof SerialisedUser) {
                userName = ((SerialisedUser) authentication.getPrincipal()).getName();
            } else if (authentication.getPrincipal() instanceof String) {
                userName = (String) authentication.getPrincipal();
            }
        }
        return userName;
    }

    public UserAuthenticationToken getUserAuthenticationToken() {
        if (!isAuthenticated()) {
            log.debug("User not authenticated.");
            return null;
        }
        String login = getCurrentUserLogin();
        if (login != null) {
            ResponseEntity<SerialisedUser> response = internalUserResource.getAuthenticatedUserByLogin(login);
            if (response.getStatusCode() != HttpStatus.OK) {
                log.warn("User not found with login: {}.", login);
                return null;
            }
            UserAuthenticationToken token = new UserAuthenticationToken(response.getBody());
            token.setAuthenticated(true);
            return token;
        }
        return null;
    }

    public AuthenticatedUser getCurrentUser() {
        // First check if the user object is available in the security context
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        if (authentication != null) {
            if (authentication.getPrincipal() instanceof AuthenticatedUser) {
                return ((AuthenticatedUser) authentication.getPrincipal());
            }
        }
        // Otherwise extract it from the authentication token
        UserAuthenticationToken token = getUserAuthenticationToken();
        if (token == null) {
            return null;
        }
        return token.getUser();
    }

    /**
     * Get the uuid of the current user.
     *
     * @return the uuid of the current user
     */
    public UUID getCurrentUserUuid() {
        AuthenticatedUser user = getCurrentUser();
        if (user == null) {
            return null;
        }
        return user.getUuid();
    }

    /**
     * If the current user has a specific authority (security role).
     *
     * <p>The name of this method comes from the isUserInRole() method in the Servlet API</p>
     *
     * @param authority the authority to check
     * @return true if the current user has the authority, false otherwise
     */
    public boolean isCurrentUserInRole(String authority) {
        return isCurrentUserInAnyRole(authority);
    }

    /**
     * If the current user has any of the specified authorities (security roles).
     *
     * @param authorities the authorities to check
     * @return true if the current user has any of the authorities, false otherwise
     */
    public boolean isCurrentUserInAnyRole(String ... authorities) {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        if (authentication != null) {
            return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority ->
                    Arrays.stream(authorities).anyMatch(authority ->
                        grantedAuthority.getAuthority().equals(authority)
                    )
                );
        }
        return false;
    }

    /**
     * If the current user has a specific authority (security role).
     *
     * @param organisationUuid The UUID of the organisation to check against.
     * @param authority the authority to check.
     * @return true if the current user has the authority, false otherwise.
     */
    public boolean isCurrentUserInOrganisationRole(UUID organisationUuid, String authority) {
        return isCurrentUserInAnyOrganisationRole(organisationUuid, authority);
    }

    /**
     * If the current user has any of the specified authorities (security roles).
     *
     * @param organisationUuid The UUID of the organisation to check against.
     * @param authorities the authorities to check
     * @return true if the current user has any of the authorities, false otherwise
     */
    public boolean isCurrentUserInAnyOrganisationRole(UUID organisationUuid, String ... authorities) {
        log.debug("Checking access for organisation {}", organisationUuid);
        return isCurrentUserInAnyOrganisationRole(Collections.singleton(organisationUuid), Arrays.asList(authorities));
    }

    /**
     * If the current user has any of the specified authorities (security roles).
     *
     * @param organisationUuids The UUID of the organisation to check against.
     * @param authority the authority to check
     * @return true if the current user has any of the authorities, false otherwise
     */
    public boolean isCurrentUserInAnyOrganisationRole(Collection<UUID> organisationUuids, String authority) {
        log.debug("Checking access for organisation {}", organisationUuids);
        return isCurrentUserInAnyOrganisationRole(organisationUuids, Collections.singleton(authority));
    }

    /**
     * If the current user has any of the specified authorities (security roles).
     *
     * @param organisationUuids The UUIDs of the organisations to check against.
     * @param authorities the authorities to check
     * @return true if the current user has any of the authorities, false otherwise
     */
    public boolean isCurrentUserInAnyOrganisationRole(Collection<UUID> organisationUuids, Collection<String> authorities) {
        log.debug("Checking access for organisations {}", organisationUuids);
        AuthenticatedUser user = getCurrentUser();
        if (user == null) {
            return false;
        }
        return organisationUuids.stream().anyMatch(organisationUuid -> {
            Collection<String> organisationRoles = user.getOrganisationAuthorities().get(organisationUuid);
            return organisationRoles != null &&
                organisationRoles.stream().anyMatch(grantedAuthority ->
                    authorities.stream().anyMatch(authority ->
                        grantedAuthority.equals(authority)
                    )
                );
        });
    }

}
