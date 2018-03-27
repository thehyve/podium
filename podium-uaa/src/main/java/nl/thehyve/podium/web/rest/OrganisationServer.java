/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.web.rest;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.ApiParam;
import nl.thehyve.podium.common.exceptions.InvalidRequest;
import nl.thehyve.podium.common.exceptions.ResourceNotFound;
import nl.thehyve.podium.common.resource.OrganisationResource;
import nl.thehyve.podium.common.security.AuthenticatedUser;
import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.common.security.annotations.AnyAuthorisedUser;
import nl.thehyve.podium.common.security.annotations.OrganisationParameter;
import nl.thehyve.podium.common.security.annotations.OrganisationUuidParameter;
import nl.thehyve.podium.common.security.annotations.SecuredByAuthority;
import nl.thehyve.podium.common.security.annotations.SecuredByOrganisation;
import nl.thehyve.podium.common.service.SecurityService;
import nl.thehyve.podium.common.service.dto.OrganisationRepresentation;
import nl.thehyve.podium.common.web.rest.util.HeaderUtil;
import nl.thehyve.podium.search.SearchOrganisation;
import nl.thehyve.podium.service.OrganisationService;
import nl.thehyve.podium.common.web.rest.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for managing Organisation.
 */
@SecuredByAuthority({AuthorityConstants.BBMRI_ADMIN})
@Timed
@RestController
public class OrganisationServer implements OrganisationResource {

    private final Logger log = LoggerFactory.getLogger(OrganisationServer.class);

    private static final String ENTITY_NAME = "organisation";

    @Autowired
    private OrganisationService organisationService;

    @Autowired
    private SecurityService securityService;

    /**
     * POST  /organisations : Create a new organisation.
     *
     * @param organisationRepresentation the organisation to create
     * @return the ResponseEntity with status 201 (Created) and with body the new organisation, or with status 400 (Bad Request) if the organisation has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/organisations")
    public ResponseEntity<OrganisationRepresentation> createOrganisation(@Valid @RequestBody OrganisationRepresentation organisationRepresentation) throws URISyntaxException {
        log.debug("REST request to save Organisation : {}", organisationRepresentation);
        if (organisationRepresentation.getId() != null) {
            throw new InvalidRequest("A new organisation cannot already have an ID");
        }

        OrganisationRepresentation result = organisationService.create(organisationRepresentation);

        return ResponseEntity.created(new URI("/api/organisations/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /organisations : Updates an existing organisation.
     *
     * @param organisationRepresentation the organisation to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated organisation,
     * or with status 400 (Bad Request) if the organisation is not valid,
     * or with status 500 (Internal Server Error) if the organisation couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @SecuredByOrganisation(authorities = AuthorityConstants.ORGANISATION_ADMIN)
    @SecuredByAuthority({AuthorityConstants.BBMRI_ADMIN})
    @PutMapping("/organisations")
    public ResponseEntity<OrganisationRepresentation> updateOrganisation(@OrganisationParameter @Valid @RequestBody OrganisationRepresentation organisationRepresentation)
        throws ResourceNotFound, URISyntaxException {
            log.debug("REST request to update Organisation : {}", organisationRepresentation);
            if (organisationRepresentation.getId() == null) {
                throw new ResourceNotFound("ID not defined for organisation.");
            }

            OrganisationRepresentation updatedOrganisation = organisationService.update(organisationRepresentation);

            return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, updatedOrganisation.getId().toString()))
                .body(organisationRepresentation);
    }

    /**
     * GET  /organisations : get paginated organisations.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of organisations in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/organisations")
    public ResponseEntity<List<OrganisationRepresentation>> getOrganisations(@ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Organisations");
        Page<OrganisationRepresentation> page = organisationService.findAll(pageable);
        List<OrganisationRepresentation> result = page.getContent();
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/organisations");
        return new ResponseEntity<>(result, headers, HttpStatus.OK);
    }

    /**
     * GET  /organisations/all : get all the organisations.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of organisations in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @Override
    public ResponseEntity<List<OrganisationRepresentation>> getAllOrganisations()
        throws URISyntaxException {
        log.debug("REST request to get all Organisations");
        Page<OrganisationRepresentation> page = organisationService.findAll(null);
        List<OrganisationRepresentation> result = page.getContent();
        return ResponseEntity.ok(result);
    }

    /**
     * GET  /organisations/:id : get the "id" organisation.
     *
     * @param id the id of the organisation to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the organisation, or with status 404 (Not Found)
     */
    @GetMapping("/organisations/{id}")
    @Deprecated
    public ResponseEntity<OrganisationRepresentation> getOrganisationById(@PathVariable Long id) {
        log.debug("REST request to get Organisation : {}", id);
        OrganisationRepresentation organisationRepresentation = organisationService.findOneDTO(id);
        if (organisationRepresentation == null) {
            throw new ResourceNotFound(String.format("Organisation not found with id: %s.", id));
        }
        return ResponseEntity.ok(organisationRepresentation);
    }

    /**
     * GET  /organisations/admin : get a paginated list organisations for which the current user
     * is an admin.
     * If the user is a {@link AuthorityConstants#BBMRI_ADMIN}, all organisations are fetched.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of organisations in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @SecuredByAuthority({AuthorityConstants.ORGANISATION_ADMIN, AuthorityConstants.BBMRI_ADMIN})
    @GetMapping("/organisations/admin")
    public ResponseEntity<List<OrganisationRepresentation>> getAdminOrganisations(@ApiParam Pageable pageable)
        throws URISyntaxException {
        AuthenticatedUser user = securityService.getCurrentUser();
        log.debug("REST request to get a page of Organisations for admin {}", user.getName());
        Page<OrganisationRepresentation> page;
        if (user.getAuthorityNames().contains(AuthorityConstants.BBMRI_ADMIN)) {
            // Fetch all organisations for the BBMRI_ADMIN user
            log.debug("Fetching all organisations for the BBMRI admin.");
            page = organisationService.findAll(pageable);
        } else {
            // Get the uuids of the organisations for which the user is admin
            log.debug("Fetching organisations for the organisation admin.");
            Collection<UUID> organisationUuids = user.getOrganisationAuthorities().entrySet().stream()
                .filter(entry -> entry.getValue().contains(AuthorityConstants.ORGANISATION_ADMIN))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
            // Fetch the organisation entities
            page = organisationService.findAvailableOrganisationsByUuids(organisationUuids, pageable);
        }
        List<OrganisationRepresentation> result = page.getContent();
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/organisations/admin");
        return new ResponseEntity<>(result, headers, HttpStatus.OK);
    }

    /**
     * GET  /organisations/available : get all the organisations.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of organisations in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @AnyAuthorisedUser
    @GetMapping("/organisations/available")
    public ResponseEntity<List<OrganisationRepresentation>> getActiveOrganisations(@ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Organisations");
        Page<OrganisationRepresentation> page = organisationService.findAllAvailable(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/organisations");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /organisations/uuid/:uuid : get the "uuid" organisation.
     *
     * @param uuid the uuid of the organisation to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the organisation, or with status 404 (Not Found)
     */
    @AnyAuthorisedUser
    @Override
    public ResponseEntity<OrganisationRepresentation> getOrganisation(@OrganisationUuidParameter @PathVariable("uuid") UUID uuid) {
        log.debug("REST request to get Organisation : {}", uuid);
        OrganisationRepresentation organisationRepresentation = organisationService.findDTOByUuid(uuid);
        if (organisationRepresentation == null) {
            throw new ResourceNotFound(String.format("Organisation not found with uuid: %s.", uuid));
        }
        return ResponseEntity.ok(organisationRepresentation);
    }

    /**
     * PUT /organisations/:uuid/activation?value=:activation : activate or deactivate the "uuid" organisation
     *
     * @param uuid the uuid of the organisation to be activated/deactivated
     * @param activation boolean activation flag (true or false)
     * @return the ResponseEntity with status 200 (OK) and with body the updated organisation,
     * or with status 400 (Bad Request) if the organisation is not valid,
     * or with status 500 (Internal Server Error) if the organisation couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/organisations/{uuid}/activation")
    public ResponseEntity<OrganisationRepresentation> setOrganisationActivation(
        @PathVariable UUID uuid,  @RequestParam(value = "value", required = true) boolean activation) throws
        URISyntaxException {

        log.debug("REST request to activate/deactivate Organisation : {}", uuid, activation);
        OrganisationRepresentation updatedOrganisationRepresentation = organisationService.activation(uuid, activation);

        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, uuid.toString()))
            .body(updatedOrganisationRepresentation);
    }

    /**
     * DELETE  /organisations/:id : delete the "id" organisation.
     *
     * @param uuid the id of the organisation to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/organisations/{uuid}")
    public ResponseEntity<Void> deleteOrganisation(@PathVariable UUID uuid) {
        log.debug("REST request to delete Organisation : {}", uuid);
        organisationService.delete(uuid);

        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, uuid.toString())).build();
    }

    /**
     * SEARCH  /_search/organisations?query=:query : search for the organisation corresponding
     * to the query.
     *
     * @param query the query of the organisation search
     * @param pageable the pagination information
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/_search/organisations")
    public ResponseEntity<List<SearchOrganisation>> searchOrganisations(@RequestParam String query, @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Organisations for query {}", query);
        Page<SearchOrganisation> page = organisationService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/organisations");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

}

