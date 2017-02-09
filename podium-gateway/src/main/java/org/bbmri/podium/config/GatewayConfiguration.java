/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

package org.bbmri.podium.config;

import com.datastax.driver.core.Session;
import org.bbmri.podium.gateway.accesscontrol.AccessControlFilter;
import org.bbmri.podium.gateway.ratelimiting.RateLimitingFilter;
import org.bbmri.podium.gateway.ratelimiting.RateLimitingRepository;
import org.bbmri.podium.gateway.responserewriting.SwaggerBasePathRewritingFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfiguration {

    @Configuration
    public static class SwaggerBasePathRewritingConfiguration {

        @Bean
        public SwaggerBasePathRewritingFilter swaggerBasePathRewritingFilter(){
            return new SwaggerBasePathRewritingFilter();
        }
    }

    @Configuration
    public static class AccessControlFilterConfiguration {

        @Bean
        public AccessControlFilter accessControlFilter(RouteLocator routeLocator, PodiumProperties podiumProperties){
            return new AccessControlFilter(routeLocator, podiumProperties);
        }
    }

    /**
     * Configures the Zuul filter that limits the number of API calls per user.
     * <p>
     * For this filter to work, you need to have:
     * <ul>
     * <li>A working Cassandra cluster
     * <li>A schema with the Podium rate-limiting tables configured, using the
     * "create_keyspace.cql" and "create_tables.cql" scripts from the
     * "src/main/resources/config/cql" directory
     * <li>Your cluster configured in your application-*.yml files, using the
     * "spring.data.cassandra" keys
     * </ul>
     */
    @Configuration
    @ConditionalOnProperty("podium.gateway.rate-limiting.enabled")
    public static class RateLimitingConfiguration {

        private final PodiumProperties podiumProperties;

        public RateLimitingConfiguration(PodiumProperties podiumProperties) {
            this.podiumProperties = podiumProperties;
        }

        @Bean
        public RateLimitingRepository rateLimitingRepository(Session session) {
            return new RateLimitingRepository(session);
        }

        @Bean
        public RateLimitingFilter rateLimitingFilter(RateLimitingRepository rateLimitingRepository) {
            return new RateLimitingFilter(rateLimitingRepository, podiumProperties);
        }
    }
}
