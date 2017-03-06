/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.security;

import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.common.security.UserAuthenticationToken;
import nl.thehyve.podium.domain.User;
import nl.thehyve.podium.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class SecurityService {

    private final Logger log = LoggerFactory.getLogger(SecurityService.class);

    @Autowired
    private UserService userService;

    /**
     * Check if a user is authenticated.
     *
     * @return true if the user is authenticated, false otherwise
     */
    public static boolean isAuthenticated() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        if (authentication != null) {
            return authentication.getAuthorities().stream()
                .noneMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(AuthorityConstants.ANONYMOUS));
        }
        return false;
    }

    public static String getCurrentUserLogin() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        String userName = null;
        if (authentication != null) {
            if (authentication.getPrincipal() instanceof UserDetails) {
                UserDetails springSecurityUser = (UserDetails) authentication.getPrincipal();
                userName = springSecurityUser.getUsername();
            } else if (authentication.getPrincipal() instanceof String) {
                userName = (String) authentication.getPrincipal();
            }
        }
        return userName;
    }

    public UserAuthenticationToken getUserAuthenticationToken() {
        if (!isAuthenticated()) {
            log.warn("User not authenticated.");
            return null;
        }
        String login = getCurrentUserLogin();
        if (login != null) {
            Optional<User> userOptional = userService.getUserWithAuthoritiesByLogin(login);
            if (!userOptional.isPresent()) {
                log.warn("User not found with login: {}.", login);
                return null;
            }
            User user = userOptional.get();
            return new UserAuthenticationToken(user);
        }
        return null;
    }

    /**
     * Get the uuid of the current user.
     *
     * @return the uuid of the current user
     */
    public UUID getCurrentUserUuid() {
        UserAuthenticationToken token = getUserAuthenticationToken();
        if (token == null) {
            return null;
        }
        return token.getUuid();
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
                .anyMatch(grantedAuthority -> {
                    return Arrays.stream(authorities).anyMatch(authority -> {
                        return grantedAuthority.getAuthority().equals(authority);
                    });
                });
        }
        return false;
    }

    /**
     * If the current user has a specific authority (security role).
     *
     * @param authority the authority to check
     * @return true if the current user has the authority, false otherwise
     */
    public boolean isCurrentUserInOrganisationRole(UUID organisationUuid, String authority) {
        return isCurrentUserInAnyOrganisationRole(organisationUuid, authority);
    }

    /**
     * If the current user has any of the specified authorities (security roles).
     *
     * @param authorities the authorities to check
     * @return true if the current user has any of the authorities, false otherwise
     */
    public boolean isCurrentUserInAnyOrganisationRole(UUID organisationUuid, String ... authorities) {
        log.info("Checking access for organisation {}", organisationUuid);
        UserAuthenticationToken token = getUserAuthenticationToken();
        if (token == null) {
            return false;
        }
        Collection<String> organisationRoles = token.getOrganisationRoles().get(organisationUuid);
        log.info("Organisation roles: {}", organisationRoles);
        return organisationRoles != null &&
            organisationRoles.stream().anyMatch(grantedAuthority ->
                    Arrays.stream(authorities).anyMatch(authority ->
                        grantedAuthority.equals(authority)
                    )
            );
    }

}
