/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.web.rest;

import com.codahale.metrics.annotation.Timed;
import nl.thehyve.podium.common.resource.InternalRoleResource;
import nl.thehyve.podium.common.security.annotations.AnyAuthorisedUser;
import nl.thehyve.podium.common.security.annotations.OrganisationUuidParameter;
import nl.thehyve.podium.common.service.dto.UserRepresentation;
import nl.thehyve.podium.domain.Organisation;
import nl.thehyve.podium.domain.Role;
import nl.thehyve.podium.domain.User;
import nl.thehyve.podium.service.OrganisationService;
import nl.thehyve.podium.service.RoleService;
import nl.thehyve.podium.service.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for managing Role.
 */
@RestController
@AnyAuthorisedUser
public class InternalRoleServer implements InternalRoleResource {

    private final Logger log = LoggerFactory.getLogger(InternalRoleServer.class);

    @Autowired
    private RoleService roleService;

    @Autowired
    private OrganisationService organisationService;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Timed
    public ResponseEntity<List<UserRepresentation>> getOrganisationRoleUsers(
        @OrganisationUuidParameter @PathVariable("uuid") UUID uuid,
        @PathVariable("authority") String authority) {
        Organisation organisation = organisationService.findByUuid(uuid);
        Role role = roleService.findRoleByOrganisationAndAuthorityName(organisation, authority);
        List<User> users = new ArrayList<>(role.getUsers());
        return ResponseEntity.ok(userMapper.usersToUserDTOs(users));
    }
}
