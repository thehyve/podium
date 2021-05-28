/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.service;

import feign.FeignException;
import nl.thehyve.podium.client.InternalUserClient;
import nl.thehyve.podium.common.service.dto.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserClientService {

    private final Logger log = LoggerFactory.getLogger(UserClientService.class);

    @Autowired
    InternalUserClient internalUserClient;

    public UserRepresentation findUserByUuid(UUID userUuid) throws FeignException {
        log.info("Fetching user through Feign ...");
        return internalUserClient.getUser(userUuid).getBody();
    }

    @Cacheable("remoteUsers")
    public UserRepresentation findUserByUuidCached(UUID userUuid) throws FeignException {
        log.info("Fetching user through Feign ...");
        return findUserByUuid(userUuid);
    }

}
