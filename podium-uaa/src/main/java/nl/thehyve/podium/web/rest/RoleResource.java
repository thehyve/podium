/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.web.rest;

import io.swagger.annotations.ApiParam;
import nl.thehyve.podium.common.exceptions.ResourceNotFound;
import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.common.security.annotations.OrganisationParameter;
import nl.thehyve.podium.common.security.annotations.OrganisationUuidParameter;
import nl.thehyve.podium.common.security.annotations.SecuredByAuthority;
import nl.thehyve.podium.common.security.annotations.SecuredByOrganisation;
import nl.thehyve.podium.common.service.dto.RoleRepresentation;
import nl.thehyve.podium.common.web.rest.util.HeaderUtil;
import nl.thehyve.podium.service.RoleService;
import nl.thehyve.podium.common.web.rest.util.PaginationUtil;
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
import org.springframework.web.bind.annotation.RestController;

import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

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


    /**
     * PUT  /roles : Updates an existing role.
     *
     * @param roleRepresentation the role to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated role,
     * or with status 400 (Bad Request) if the role is not valid,
     * or with status 500 (Internal Server Error) if the role couldn't be updated
     */
    @SecuredByAuthority({AuthorityConstants.PODIUM_ADMIN, AuthorityConstants.BBMRI_ADMIN})
    @SecuredByOrganisation(authorities= {AuthorityConstants.ORGANISATION_ADMIN})
    @PutMapping("/roles")
    public ResponseEntity<RoleRepresentation> updateRole(@OrganisationParameter @RequestBody RoleRepresentation roleRepresentation) {
        log.debug("REST request to update Role : {}", roleRepresentation);
        if (roleRepresentation.getId() == null) {
            throw new ResourceNotFound(String.format("Role not found with id: %s.", roleRepresentation.getId()));
        }
        RoleRepresentation updatedRole = roleService.updateRole(roleRepresentation);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, updatedRole.getId().toString()))
            .body(updatedRole);
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
    public ResponseEntity<List<RoleRepresentation>> getAllRoles(@ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Roles");
        Page<RoleRepresentation> page = roleService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/roles");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
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
    public ResponseEntity<RoleRepresentation> getRole(@PathVariable Long id) {
        log.debug("REST request to get Role : {}", id);
        return ResponseEntity.ok(roleService.findOne(id));
    }
}
