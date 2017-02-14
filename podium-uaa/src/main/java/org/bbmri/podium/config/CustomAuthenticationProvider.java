package org.bbmri.podium.config;

import org.bbmri.podium.domain.User;
import org.bbmri.podium.exceptions.AccountNotVerifiedException;
import org.bbmri.podium.exceptions.EmailNotVerifiedException;
import org.bbmri.podium.exceptions.UserAccountBlockedException;
import org.bbmri.podium.security.UserAuthenticationToken;
import org.bbmri.podium.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.Optional;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    static final long MAX_FAILED_LOGIN_ATTEMPTS = 10;
    static final long ACCOUNT_BLOCKING_PERIOD_MINUTES = 5; // 5 minutes

    private final Logger log = LoggerFactory.getLogger(CustomAuthenticationProvider.class);

    @Autowired
    UserService userService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        log.info("username: " + authentication.getName());
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
            long intervalMinutes = Duration.between(ZonedDateTime.now(), user.getAccountLockDate()).toMinutes();
            if (intervalMinutes > ACCOUNT_BLOCKING_PERIOD_MINUTES) {
                // unblock account
                log.info("Unblocking blocked account for user " + user.getLogin());
                user.resetFailedLoginAttempts();
                user.setAccountLocked(false);
                user = userService.save(user);
            } else {
                // account is temporarily blocked, deny access.
                log.info("Account still blocked for user " + user.getLogin() + ". Access denied.");
                throw new UserAccountBlockedException("User account blocked.");
            }
        }
        if (passwordEncoder.matches(authentication.getCredentials().toString(), user.getPassword())) {
            log.info("Authentication manager: OK");
            if (user.getFailedLoginAttempts() > 0) {
                user.resetFailedLoginAttempts();
                user = userService.save(user);
            }
            UserAuthenticationToken token = new UserAuthenticationToken(user);
            log.info("Token: " + token);
            return token;
        }
        // failed login attempt
        user.increaseFailedLoginAttempts();
        log.info("Login failed for user " + user.getLogin() + ". Failed attempt number " + user.getFailedLoginAttempts() + ".");
        if (user.getFailedLoginAttempts() >= MAX_FAILED_LOGIN_ATTEMPTS) {
            // block account
            user.setAccountLocked(true);
            user.setAccountLockDate(ZonedDateTime.now());
            userService.save(user);
            throw new UserAccountBlockedException("The user account is blocked.");
        }
        userService.save(user);
        throw new BadCredentialsException("Invalid credentials.");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication == UserAuthenticationToken.class;
    }

}
