/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package org.bbmri.podium.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties specific to the User Authentication component of Podium.
 *
 * <p>
 *     Properties are configured in the application.yml file.
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

        /**
         * Maximum number of failed login attempts before the user account is locked.
         *
         * Property: uaa.security.maxFailedLoginAttempts (default: 5)
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
         *
         * Property: uaa.security.timeBasedUnlockingEnabled (default: false)
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
         *
         * Property: uaa.security.accountBlockingPeriodSeconds (default: 300 = 5 minutes)
         */
        public long getAccountLockingPeriodSeconds() {
            return accountLockingPeriodSeconds;
        }

        public void setAccountLockingPeriodSeconds(long accountBlockingPeriodSeconds) {
            this.accountLockingPeriodSeconds = accountBlockingPeriodSeconds;
        }

    }

}
