/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties specific to the User Authentication component of Podium.
 * <p>
 * <p>
 * Properties are configured in the application.yml file.
 * </p>
 */
@ConfigurationProperties(prefix = "uaa", ignoreUnknownFields = false)
public class UaaProperties {

    private final Security security = new Security();

    public Security getSecurity() {
        return security;
    }

    public static class Security {

        private long maxFailedLoginAttempts = 5;
        private boolean timeBasedUnlockingEnabled = false;
        private long accountLockingPeriodSeconds = 5 * 60; // 5 minutes
        private long activationKeyValiditySeconds = 60 * 60 * 24 * 7; // One week

        /**
         * Maximum number of failed login attempts before the user account is locked.
         * <p>
         * Property: uaa.security.maxFailedLoginAttempts (default: 5)
         *
         * @return The number of maximum failed login attempts allowed.
         */
        public long getMaxFailedLoginAttempts() {
            return maxFailedLoginAttempts;
        }

        public void setMaxFailedLoginAttempts(long maxFailedLoginAttempts) {
            this.maxFailedLoginAttempts = maxFailedLoginAttempts;
        }

        /**
         * If true, the account will only be temporarily locked, see {@link #accountLockingPeriodSeconds};
         * otherwise, an admin action is required to unlock the account.
         * <p>
         * Property: uaa.security.timeBasedUnlockingEnabled (default: false)
         *
         * @return boolean indicating whether time based account unlocking is enabled.
         */
        public boolean isTimeBasedUnlockingEnabled() {
            return timeBasedUnlockingEnabled;
        }

        public void setTimeBasedUnlockingEnabled(boolean timeBasedUnlockingEnabled) {
            this.timeBasedUnlockingEnabled = timeBasedUnlockingEnabled;
        }

        /**
         * The number of seconds an account is locked after too many failed login attempts.
         * Automatic unlocking is only active if {@link #timeBasedUnlockingEnabled}.
         * <p>
         * Property: uaa.security.accountBlockingPeriodSeconds (default: 300 = 5 minutes)
         *
         * @return Number of seconds indicating how long the account is locked after exceeding max login attempts.
         */
        public long getAccountLockingPeriodSeconds() {
            return accountLockingPeriodSeconds;
        }

        public void setAccountLockingPeriodSeconds(long accountBlockingPeriodSeconds) {
            this.accountLockingPeriodSeconds = accountBlockingPeriodSeconds;
        }

        /**
         * The number of seconds an activation key is valid.
         * <p>
         * Property: uaa.security.activationKeyValiditySeconds { default: 604800 }
         *
         * @return Number of seconds indicating how long an activation key is valid for.
         */
        public long getActivationKeyValiditySeconds() {
            return activationKeyValiditySeconds;
        }

        public void setActivationKeyValiditySeconds(long activationKeyValidity) {
            this.activationKeyValiditySeconds = activationKeyValidity;
        }

    }

}
