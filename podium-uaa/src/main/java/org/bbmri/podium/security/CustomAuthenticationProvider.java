/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package org.bbmri.podium.security;

import org.bbmri.podium.config.UaaProperties;
import org.bbmri.podium.domain.User;
import org.bbmri.podium.exceptions.AccountNotVerifiedException;
import org.bbmri.podium.exceptions.EmailNotVerifiedException;
import org.bbmri.podium.exceptions.UserAccountLockedException;
import org.bbmri.podium.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.Optional;

/**
 * Spring authentication provider that supports account locking after
 * too many failed login attempts and prevents login for unverified accounts.
 */
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final Logger log = LoggerFactory.getLogger(CustomAuthenticationProvider.class);

    @Autowired
    UaaProperties uaaProperties;

    @Autowired
    UserService userService;

    @Autowired
    PasswordEncoder passwordEncoder;

    /**
     * Authenticates a user based on a {@link UsernamePasswordAuthenticationToken} token.
     * The login succeeds if a user with the provided username exists and
     * - the user is verified (both email address is verified and the account is verified
     * by an administrator) and
     * - the user account is not locked and
     * - the provided password matches the (encrypted) password of the user.
     *
     * If the wrong password is provided for too many times (configured in
     * {@link UaaProperties.Security#maxFailedLoginAttempts}), the user accounts is blocked.
     * If the setting {@link UaaProperties.Security#timeBasedUnlockingEnabled} is true (default is false),
     * the account will be automatically unlocked after {@link UaaProperties.Security#accountLockingPeriodSeconds}
     * seconds.
     *
     * @param authentication the {@link UsernamePasswordAuthenticationToken} object.
     * @return a {@link UserAuthenticationToken} object if the authentication is successful.
     * @throws BadCredentialsException if the user account does not exists or the password is incorrect.
     * @throws EmailNotVerifiedException if the user email address has not been verified.
     * @throws AccountNotVerifiedException if the user account has not been verified.
     * @throws UserAccountLockedException if the user account is locked.
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (authentication == null || authentication.getName() == null) {
            throw new BadCredentialsException("Invalid credentials.");
        }
        String username = authentication.getName().toLowerCase(Locale.ENGLISH);
        log.info("username: " + username);
        Optional<User> userOptional = userService.getUserWithAuthoritiesByLogin(username);
        if (!userOptional.isPresent()) {
            throw new BadCredentialsException("Invalid credentials.");
        }
        User user = userOptional.get();
        if (!user.isEmailVerified()) {
            throw new EmailNotVerifiedException("Email address has not been verified yet.");
        }
        if (!user.isAdminVerified()) {
            throw new AccountNotVerifiedException("The user account has not been verified yet.");
        }
        if (user.isAccountLocked()) {
            if (!uaaProperties.getSecurity().isTimeBasedUnlockingEnabled()) {
                // account is llocked, deny access.
                log.info("Account still locked for user " + user.getLogin() + ". Access denied.");
                throw new UserAccountLockedException("The user account is locked.");
            } else {
                long intervalSeconds = Duration.between(user.getAccountLockDate(), ZonedDateTime.now()).abs().getSeconds();
                log.info("Account locked. interval = {} seconds (locking period is {} seconds)",
                    intervalSeconds,
                    uaaProperties.getSecurity().getAccountLockingPeriodSeconds());
                if (intervalSeconds > uaaProperties.getSecurity().getAccountLockingPeriodSeconds()) {
                    // unblock account
                    log.info("Unlocking locked account for user " + user.getLogin());
                    user.resetFailedLoginAttempts();
                    user.setAccountLocked(false);
                    user = userService.save(user);
                } else {
                    // account is temporarily locked, deny access.
                    log.info("Account still locked for user " + user.getLogin() + ". Access denied.");
                    throw new UserAccountLockedException("The user account is locked.");
                }
            }
        }
        // if oauth2 authentication
        if (authentication instanceof OAuth2Authentication) {
            log.info("Checking OAuth2 authentication.");
            if (authentication.isAuthenticated()) {
                UserAuthenticationToken token = new UserAuthenticationToken(user);
                token.setAuthenticated(true);
                log.info("Token: " + token);
                return token;
            }
            throw new BadCredentialsException("Invalid credentials.");
        }
        // if username and password authentication
        if (passwordEncoder.matches(authentication.getCredentials().toString(), user.getPassword())) {
            log.info("Authentication manager: OK");
            if (user.getFailedLoginAttempts() > 0) {
                user.resetFailedLoginAttempts();
                user = userService.save(user);
            }
            UserAuthenticationToken token = new UserAuthenticationToken(user);
            token.setAuthenticated(true);
            log.info("Token: " + token);
            return token;
        }
        // failed login attempt
        user.increaseFailedLoginAttempts();
        log.info("Login failed for user " + user.getLogin() + ". Failed attempt number " + user.getFailedLoginAttempts() + ".");
        if (user.getFailedLoginAttempts() >= uaaProperties.getSecurity().getMaxFailedLoginAttempts()) {
            // block account
            user.setAccountLocked(true);
            user.setAccountLockDate(ZonedDateTime.now());
            userService.save(user);
            throw new UserAccountLockedException("The user account is locked.");
        }
        userService.save(user);
        throw new BadCredentialsException("Invalid credentials.");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication == UsernamePasswordAuthenticationToken.class ||
            authentication == OAuth2Authentication.class;
    }

}
