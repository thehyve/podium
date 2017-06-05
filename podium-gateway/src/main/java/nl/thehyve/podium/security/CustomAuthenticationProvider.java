/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.security;

import nl.thehyve.podium.common.security.UserAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Spring authentication provider.
 */
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final Logger log = LoggerFactory.getLogger(CustomAuthenticationProvider.class);

    /**
     * Authenticates a user based on a {@link OAuth2Authentication} token.
     * The login succeeds if the token contains a valid {@link UserAuthenticationToken}.
     *
     * @param authentication the {@link OAuth2Authentication} object.
     * @return a {@link UserAuthenticationToken} object if the authentication is successful.
     * @throws BadCredentialsException if the user account does not exists or the password is incorrect.
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (authentication == null) {
            throw new InvalidTokenException("Invalid token (token not found)");
        }
        OAuth2Authentication auth = (OAuth2Authentication) authentication;
        if (auth.getName() == null) {
            throw new InvalidTokenException("Invalid token: " + auth);
        }
        String username = auth.getName().toLowerCase(Locale.ENGLISH);
        log.debug("username: " + username);

        Authentication userAuthentication = auth.getUserAuthentication();
        log.debug("User authentication: {}", userAuthentication == null ? null : userAuthentication.toString());
        if (userAuthentication != null && userAuthentication instanceof UserAuthenticationToken) {
            log.debug("Authentication OK", userAuthentication);
            userAuthentication.setAuthenticated(true);
            UserAuthenticationToken token = new UserAuthenticationToken(((UserAuthenticationToken) userAuthentication).getUser());
            token.setAuthenticated(true);
            return token;
        }
        log.error("Authentication failed.");
        throw new InvalidTokenException("Invalid token.");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication == OAuth2Authentication.class;
    }

}
