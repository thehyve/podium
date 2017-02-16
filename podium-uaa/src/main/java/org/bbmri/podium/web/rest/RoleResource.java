/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

package org.bbmri.podium.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.Sets;
import org.bbmri.podium.domain.Organisation;
import org.bbmri.podium.domain.Role;
import org.bbmri.podium.domain.User;
import org.bbmri.podium.exceptions.ResourceNotFoundException;
import org.bbmri.podium.service.OrganisationService;
import org.bbmri.podium.service.RoleService;
import org.bbmri.podium.service.UserService;
import org.bbmri.podium.service.representation.RoleRepresentation;
import org.bbmri.podium.web.rest.util.HeaderUtil;
import org.bbmri.podium.web.rest.util.PaginationUtil;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for managing Role.
 */
@RestController
@RequestMapping("/api")
public class RoleResource {

    private final Logger log = LoggerFactory.getLogger(RoleResource.class);

    private static final String ENTITY_NAME = "role";

    private final RoleService roleService;
    private final UserService userService;
    private final OrganisationService organisationService;

    public RoleResource(RoleService roleService, UserService userService, OrganisationService organisationService) {
        this.roleService = roleService;
        this.userService = userService;
        this.organisationService = organisationService;
    }

    private void copyProperties(RoleRepresentation source, Role target) {
        Set<UUID> currentUsers = target.getUsers().stream().map(User::getUuid).collect(Collectors.toSet());
        Set<UUID> desiredUsers = source.getUsers();
        Set<UUID> deleteUsers = Sets.difference(currentUsers, desiredUsers);
        Set<UUID> addUsers = Sets.difference(desiredUsers, currentUsers);
        Set<User> result = target.getUsers().stream().filter( u ->
            !deleteUsers.contains(u.getUuid())
        ).collect(Collectors.toSet());
        for (UUID userUuid: addUsers) {
            Optional<User> user = userService.getUserByUuid(userUuid);
            if (user.isPresent()) {
                result.add(user.get());
            } else {
                throw new ResourceNotFoundException(String.format("Could not find user with uuid %s", userUuid));
            }
        }
        target.setUsers(result);
    }

    /**
     * PUT  /roles : Updates an existing role.
     *
     * @param role the role to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated role,
     * or with status 400 (Bad Request) if the role is not valid,
     * or with status 500 (Internal Server Error) if the role couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/roles")
    @Timed
    public ResponseEntity<RoleRepresentation> updateRole(@RequestBody RoleRepresentation role) throws URISyntaxException {
        log.debug("REST request to update Role : {}", role);
        if (role.getId() == null) {
            throw new ResourceNotFoundException(String.format("Role not found with id: %s.", role.getId()));
        }
        Role result = roleService.findOne(role.getId());
        if (result == null) {
            throw new ResourceNotFoundException(String.format("Role not found with id: %s.", role.getId()));
        }
        copyProperties(role, result);
        roleService.save(result);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, role.getId().toString()))
            .body(new RoleRepresentation(result));
    }

    /**
     * GET  /roles : get all the roles.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of roles in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/roles")
    @Timed
    public ResponseEntity<List<RoleRepresentation>> getAllRoles(@ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Roles");
        Page<Role> page = roleService.findAll(pageable);
        List<RoleRepresentation> roles = page.getContent().stream()
            .map(RoleRepresentation::new)
            .collect(Collectors.toList());
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/roles");
        return new ResponseEntity<>(roles, headers, HttpStatus.OK);
    }

    /**
     * GET  /roles : get all the roles for an organisation.
     *
     * @param uuid the uuid of the organisation
     * @return the ResponseEntity with status 200 (OK) and the list of roles in body
     */
    @GetMapping("/roles/organisation/{uuid}")
    @Timed
    public ResponseEntity<List<RoleRepresentation>> getOrganisationRoles(@PathVariable UUID uuid) {
        log.debug("REST request to get all Roles of Organisation {}", uuid);
        Organisation organisation = organisationService.findByUuid(uuid);
        List<RoleRepresentation> roles = roleService.findAllByOrganisation(organisation).stream()
            .map(RoleRepresentation::new)
            .collect(Collectors.toList());
        return new ResponseEntity<>(roles, HttpStatus.OK);
    }

    /**
     * GET  /roles/:id : get the "id" role.
     *
     * @param id the id of the role to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the role, or with status 404 (Not Found)
     */
    @GetMapping("/roles/{id}")
    @Timed
    public ResponseEntity<RoleRepresentation> getRole(@PathVariable Long id) {
        log.debug("REST request to get Role : {}", id);
        Role role = roleService.findOne(id);
        if (role == null) {
            throw new ResourceNotFoundException(String.format("Role not found with id: %s.", id));
        }
        return ResponseEntity.ok(new RoleRepresentation(role));
    }

    /**
     * SEARCH  /_search/roles?query=:query : search for the role corresponding
     * to the query.
     *
     * @param query the query of the role search
     * @param pageable the pagination information
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/_search/roles")
    @Timed
    public ResponseEntity<List<Role>> searchRoles(@RequestParam String query, @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Roles for query {}", query);
        Page<Role> page = roleService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/roles");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }


}
