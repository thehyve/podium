/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.common.security;

import nl.thehyve.podium.common.config.PodiumConstants;
import nl.thehyve.podium.common.service.SecurityService;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;
import java.util.Optional;

/**
 * Implementation of AuditorAware based on Spring Security.
 */
@Component
public class SpringSecurityAuditorAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        String userName = SecurityService.getCurrentUserLogin();
        String result = userName != null ? userName : PodiumConstants.SYSTEM_ACCOUNT;
        return Optional.of(result);
    }
}
