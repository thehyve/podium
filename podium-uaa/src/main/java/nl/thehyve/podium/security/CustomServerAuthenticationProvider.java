/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.security;

import nl.thehyve.podium.common.security.UserAuthenticationToken;
import nl.thehyve.podium.config.UaaProperties;
import nl.thehyve.podium.config.UaaProperties.Security;
import nl.thehyve.podium.domain.User;
import nl.thehyve.podium.exceptions.AccountNotVerifiedException;
import nl.thehyve.podium.exceptions.EmailNotVerifiedException;
import nl.thehyve.podium.exceptions.UserAccountLockedException;
import nl.thehyve.podium.service.MailService;
import nl.thehyve.podium.service.UserService;
import nl.thehyve.podium.service.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.Optional;

/**
 * Spring authentication provider that supports account locking after
 * too many failed login attempts and prevents login for unverified accounts.
 */
@Component
public class CustomServerAuthenticationProvider implements AuthenticationProvider {

    private final Logger log = LoggerFactory.getLogger(CustomServerAuthenticationProvider.class);

    @Autowired
    UaaProperties uaaProperties;

    @Autowired
    UserService userService;

    @Autowired
    MailService mailService;

    @Autowired
    UserMapper userMapper;

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Returns a {@link UserAuthenticationToken} for the user if the email address and account
     * have been verified. Throws an {@link AuthenticationException} otherwise.
     *
     * @param user the user to create the token for.
     * @return a {@link UserAuthenticationToken} for the user.
     * @throws AuthenticationException if the email address or account have not been verified.
     */
    private Authentication getToken(@NotNull User user) throws AuthenticationException {
        if (!user.isEmailVerified()) {
            throw new EmailNotVerifiedException("Email address has not been verified yet.");
        }
        if (!user.isAdminVerified()) {
            throw new AccountNotVerifiedException("The user account has not been verified yet.");
        }
        UserAuthenticationToken token = new UserAuthenticationToken(user);
        token.setAuthenticated(true);
        log.info("Token: " + token);
        return token;
    }

    /**
     * Authenticates a user based on a {@link UsernamePasswordAuthenticationToken} token.
     * The login succeeds if a user with the provided username exists and
     * - the user is verified (both email address is verified and the account is verified
     * by an administrator) and
     * - the user account is not locked and
     * - the provided password matches the (encrypted) password of the user.
     *
     * If the wrong password is provided for too many times (configured in
     * {@link Security#maxFailedLoginAttempts}), the user accounts is blocked.
     * If the setting {@link Security#timeBasedUnlockingEnabled} is true (default is false),
     * the account will be automatically unlocked after {@link Security#accountLockingPeriodSeconds}
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
        log.debug("Username: " + username);
        Optional<User> userOptional = userService.getDomainUserWithAuthoritiesByLogin(username);
        if (!userOptional.isPresent()) {
            throw new BadCredentialsException("Invalid credentials.");
        }
        User user = userOptional.get();
        if (user.isAccountLocked()) {
            if (!uaaProperties.getSecurity().isTimeBasedUnlockingEnabled()) {
                // account is locked, deny access.
                log.warn("Account still locked for user {}. Access denied.", user.getLogin());
                throw new UserAccountLockedException("The user account is locked.");
            } else {
                long intervalSeconds = Duration.between(user.getAccountLockDate(), ZonedDateTime.now()).abs().getSeconds();
                log.debug("Account locked. interval = {} seconds (locking period is {} seconds)",
                    intervalSeconds,
                    uaaProperties.getSecurity().getAccountLockingPeriodSeconds());
                if (intervalSeconds > uaaProperties.getSecurity().getAccountLockingPeriodSeconds()) {
                    // unblock account
                    log.info("Unlocking locked account for user {}.", user.getLogin());
                    user.resetFailedLoginAttempts();
                    user.setAccountLocked(false);
                    user = userService.save(user);
                } else {
                    // account is temporarily locked, deny access.
                    log.warn("Account still locked for user {}. Access denied.", user.getLogin());
                    throw new UserAccountLockedException("The user account is locked.");
                }
            }
        }
        // if oauth2 authentication
        if (authentication instanceof OAuth2Authentication) {
            log.debug("Authenticated with OAuth2. Returning user authentication token.");
            return getToken(user);
        }
        // if username and password authentication
        if (passwordEncoder.matches(authentication.getCredentials().toString(), user.getPassword())) {
            log.debug("Credentials correct.");
            if (user.getFailedLoginAttempts() > 0) {
                user.resetFailedLoginAttempts();
                user = userService.save(user);
            }
            return getToken(user);
        }
        // failed login attempt
        user.increaseFailedLoginAttempts();
        log.warn("Login failed for user {}. Failed attempt number {}.", user.getLogin(), user.getFailedLoginAttempts());
        if (user.getFailedLoginAttempts() >= uaaProperties.getSecurity().getMaxFailedLoginAttempts()) {
            // block account
            user.setAccountLocked(true);
            user.setAccountLockDate(ZonedDateTime.now());
            userService.save(user);
            mailService.sendAccountLockedMail(userMapper.userToUserDTO(user));
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
