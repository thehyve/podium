/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.web.rest;

import com.codahale.metrics.annotation.Timed;
import nl.thehyve.podium.common.exceptions.ResourceNotFound;
import nl.thehyve.podium.common.resource.InternalUserResource;
import nl.thehyve.podium.common.security.SerialisedUser;
import nl.thehyve.podium.common.security.annotations.AnyAuthorisedUser;
import nl.thehyve.podium.common.security.annotations.OrganisationUuidParameter;
import nl.thehyve.podium.common.service.dto.UserRepresentation;
import nl.thehyve.podium.domain.User;
import nl.thehyve.podium.service.UserService;
import nl.thehyve.podium.service.mapper.UserMapper;
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
    @Timed
    public ResponseEntity<UserRepresentation> getUser(
        @OrganisationUuidParameter @PathVariable("uuid") UUID uuid) {
        Optional<User> userOptional = userService.getUserByUuid(uuid);
        if (!userOptional.isPresent()) {
            throw new ResourceNotFound("User not found with uuid " + uuid.toString());
        }
        return ResponseEntity.ok(userMapper.userToUserDTO(userOptional.get()));
    }

    @Override
    @Timed
    public ResponseEntity<SerialisedUser> getAuthenticatedUserByLogin(@PathVariable("login") String login) {
        Optional<User> userOptional = userService.getUserWithAuthoritiesByLogin(login);
        if (!userOptional.isPresent()) {
            throw new ResourceNotFound("User not found with login " + login);
        }
        User user = userOptional.get();
        return ResponseEntity.ok(
            new SerialisedUser(
                user.getUuid(), user.getLogin(), user.getAuthorityNames(), user.getOrganisationAuthorities())
        );
    }

}
