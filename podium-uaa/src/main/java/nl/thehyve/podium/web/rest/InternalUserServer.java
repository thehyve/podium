/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.web.rest;

import nl.thehyve.podium.common.exceptions.ResourceNotFound;
import nl.thehyve.podium.common.resource.InternalUserResource;
import nl.thehyve.podium.common.security.SerialisedUser;
import nl.thehyve.podium.common.security.annotations.AnyAuthorisedUser;
import nl.thehyve.podium.common.security.annotations.OrganisationUuidParameter;
import nl.thehyve.podium.common.service.dto.UserRepresentation;
import nl.thehyve.podium.service.UserService;
import nl.thehyve.podium.service.mapper.*;
import nl.thehyve.podium.web.rest.dto.ManagedUserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

/**
 * REST controller for serving user information.
 */
@RestController
@AnyAuthorisedUser
public class InternalUserServer implements InternalUserResource {

    private final Logger log = LoggerFactory.getLogger(InternalUserServer.class);

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Override
    public ResponseEntity<UserRepresentation> getUser(
        @OrganisationUuidParameter @PathVariable("uuid") UUID uuid) {
        Optional<ManagedUserRepresentation> userOptional = userService.getUserByUuid(uuid);
        if (!userOptional.isPresent()) {
            throw new ResourceNotFound("User not found with uuid " + uuid.toString());
        }
        return ResponseEntity.ok(userMapper.managedUserVMToUserDTO(userOptional.get()));
    }

    @Override
    public ResponseEntity<SerialisedUser> getAuthenticatedUserByLogin(@PathVariable("login") String login) {
        Optional<ManagedUserRepresentation> userOptional = userService.getUserWithAuthoritiesByLogin(login);
        if (!userOptional.isPresent()) {
            throw new ResourceNotFound("User not found with login " + login);
        }
        ManagedUserRepresentation user = userOptional.get();
        return ResponseEntity.ok(
            new SerialisedUser(
                user.getUuid(), user.getLogin(), user.getAuthorities(), user.getOrganisationAuthorities())
        );
    }
}
