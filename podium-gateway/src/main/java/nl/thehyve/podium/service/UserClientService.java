/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.service;

import com.codahale.metrics.annotation.Timed;
import feign.FeignException;
import nl.thehyve.podium.client.InternalRoleClient;
import nl.thehyve.podium.client.InternalUserClient;
import nl.thehyve.podium.client.OrganisationClient;
import nl.thehyve.podium.common.service.dto.OrganisationDTO;
import nl.thehyve.podium.common.service.dto.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

@Service
public class UserClientService {

    private final Logger log = LoggerFactory.getLogger(UserClientService.class);

    @Autowired
    InternalUserClient internalUserClient;

    @Timed
    public UserRepresentation findUserByUuid(UUID userUuid) throws URISyntaxException, FeignException {
        return internalUserClient.getUser(userUuid).getBody();
    }

}
