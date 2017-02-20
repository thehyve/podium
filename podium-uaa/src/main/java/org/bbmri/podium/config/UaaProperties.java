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
        private long accountBlockingPeriodMinutes = 5; // 5 minutes

        public long getMaxFailedLoginAttempts() {
            return maxFailedLoginAttempts;
        }

        public void setMaxFailedLoginAttempts(long maxFailedLoginAttempts) {
            this.maxFailedLoginAttempts = maxFailedLoginAttempts;
        }

        public long getAccountBlockingPeriodMinutes() {
            return accountBlockingPeriodMinutes;
        }

        public void setAccountBlockingPeriodMinutes(long accountBlockingPeriodMinutes) {
            this.accountBlockingPeriodMinutes = accountBlockingPeriodMinutes;
        }

    }

}
