/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

package nl.thehyve.podium.common.config;

import nl.thehyve.podium.common.aop.logging.LoggingAspect;
import nl.thehyve.podium.common.aop.security.AccessPolicyAspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Profile;

@Configuration
@EnableAspectJAutoProxy
public class AspectConfiguration {

    Logger log = LoggerFactory.getLogger(AspectConfiguration.class);

    @Bean
    @Profile(PodiumConstants.SPRING_PROFILE_DEVELOPMENT)
    public LoggingAspect loggingAspect() {
        return new LoggingAspect();
    }

    @Bean
    public AccessPolicyAspect accessPolicyAspect() {
        log.info("Initialising access policy aspect.");
        return new AccessPolicyAspect();
    }

}
