/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.Sets;
import io.swagger.annotations.ApiParam;
import nl.thehyve.podium.common.exceptions.ResourceNotFound;
import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.common.security.annotations.OrganisationParameter;
import nl.thehyve.podium.common.security.annotations.OrganisationUuidParameter;
import nl.thehyve.podium.common.security.annotations.SecuredByAuthority;
import nl.thehyve.podium.common.security.annotations.SecuredByOrganisation;
import nl.thehyve.podium.common.service.dto.RoleRepresentation;
import nl.thehyve.podium.domain.Role;
import nl.thehyve.podium.domain.User;
import nl.thehyve.podium.service.OrganisationService;
import nl.thehyve.podium.service.RoleService;
import nl.thehyve.podium.service.UserService;
import nl.thehyve.podium.common.service.dto.RoleRepresentation;
import nl.thehyve.podium.service.mapper.RoleMapper;
import nl.thehyve.podium.web.rest.util.HeaderUtil;
import nl.thehyve.podium.web.rest.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @Autowired
    private RoleService roleService;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private OrganisationService organisationService;

    public RoleResource() {}

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
                throw new ResourceNotFound(String.format("Could not find user with uuid %s", userUuid));
            }
        }
        target.setUsers(result);
    }

    /**
     * PUT  /roles : Updates an existing role.
     *
     * @param roleRepresentation the role to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated role,
     * or with status 400 (Bad Request) if the role is not valid,
     * or with status 500 (Internal Server Error) if the role couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @SecuredByAuthority({AuthorityConstants.PODIUM_ADMIN, AuthorityConstants.BBMRI_ADMIN})
    @SecuredByOrganisation(authorities= {AuthorityConstants.ORGANISATION_ADMIN})
    @PutMapping("/roles")
    @Timed
    public ResponseEntity<RoleRepresentation> updateRole(@OrganisationParameter @RequestBody RoleRepresentation roleRepresentation) throws URISyntaxException {
        log.debug("REST request to update Role : {}", roleRepresentation);
        if (roleRepresentation.getId() == null) {
            throw new ResourceNotFound(String.format("Role not found with id: %s.", roleRepresentation.getId()));
        }
        Role role = roleService.findOne(roleRepresentation.getId());
        if (role == null) {
            throw new ResourceNotFound(String.format("Role not found with id: %s.", roleRepresentation.getId()));
        }
        copyProperties(roleRepresentation, role);
        roleService.save(role);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, role.getId().toString()))
            .body(roleMapper.roleToRoleDTO(role));
    }

    /**
     * GET  /roles : get all the roles.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of roles in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @SecuredByAuthority({AuthorityConstants.PODIUM_ADMIN, AuthorityConstants.BBMRI_ADMIN})
    @GetMapping("/roles")
    @Timed
    public ResponseEntity<List<RoleRepresentation>> getAllRoles(@ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Roles");
        Page<Role> page = roleService.findAll(pageable);
        List<Role> roles = page.getContent().stream()
            .collect(Collectors.toList());
        List<RoleRepresentation> roleDTOs = roleMapper.rolesToRoleDTOs(roles);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/roles");
        return new ResponseEntity<>(roleDTOs, headers, HttpStatus.OK);
    }

    /**
     * GET  /roles : get all the roles for an organisation.
     *
     * @param uuid the uuid of the organisation
     * @return the ResponseEntity with status 200 (OK) and the list of roles in body
     */
    @SecuredByAuthority({AuthorityConstants.PODIUM_ADMIN, AuthorityConstants.BBMRI_ADMIN})
    @SecuredByOrganisation
    @GetMapping("/roles/organisation/{uuid}")
    @Timed
    public ResponseEntity<List<RoleRepresentation>> getOrganisationRoles(@OrganisationUuidParameter @PathVariable UUID uuid) {
        log.debug("REST request to get all Roles of Organisation {}", uuid);
        List<RoleRepresentation> roles = roleService.findAllByOrganisationUUID(uuid);
        return new ResponseEntity<>(roles, HttpStatus.OK);
    }

    /**
     * GET  /roles/:id : get the "id" role.
     *
     * @param id the id of the role to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the role, or with status 404 (Not Found)
     */
    @SecuredByAuthority({AuthorityConstants.PODIUM_ADMIN, AuthorityConstants.BBMRI_ADMIN})
    @GetMapping("/roles/{id}")
    @Timed
    public ResponseEntity<RoleRepresentation> getRole(@PathVariable Long id) {
        log.debug("REST request to get Role : {}", id);
        Role role = roleService.findOne(id);
        if (role == null) {
            throw new ResourceNotFound(String.format("Role not found with id: %s.", id));
        }
        return ResponseEntity.ok(roleMapper.roleToRoleDTO(role));
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
    @SecuredByAuthority({AuthorityConstants.PODIUM_ADMIN, AuthorityConstants.BBMRI_ADMIN})
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
