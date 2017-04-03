/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.web.rest;

import com.codahale.metrics.annotation.Timed;

import nl.thehyve.podium.common.service.dto.OrganisationDTO;

import nl.thehyve.podium.common.exceptions.ResourceNotFound;

import nl.thehyve.podium.domain.Organisation;
import nl.thehyve.podium.search.SearchOrganisation;
import nl.thehyve.podium.service.OrganisationService;
import nl.thehyve.podium.service.mapper.OrganisationMapper;
import nl.thehyve.podium.web.rest.util.HeaderUtil;
import nl.thehyve.podium.web.rest.util.PaginationUtil;
import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.common.security.annotations.OrganisationParameter;
import nl.thehyve.podium.common.security.annotations.OrganisationUuidParameter;
import nl.thehyve.podium.common.security.annotations.SecuredByAuthority;
import nl.thehyve.podium.common.security.annotations.SecuredByOrganisation;
import io.swagger.annotations.ApiParam;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing Organisation.
 */
@SecuredByAuthority({AuthorityConstants.PODIUM_ADMIN, AuthorityConstants.BBMRI_ADMIN})
@RestController
@RequestMapping("/api")
public class OrganisationResource {

    private final Logger log = LoggerFactory.getLogger(OrganisationResource.class);

    private static final String ENTITY_NAME = "organisation";

    @Autowired
    private OrganisationService organisationService;

    private void copyProperties(Organisation source, Organisation target) {
        target.setName(source.getName());
        target.setShortName(source.getShortName());
        target.setActivated(source.isActivated());
    }

    /**
     * POST  /organisations : Create a new organisation.
     *
     * @param organisationDTO the organisation to create
     * @return the ResponseEntity with status 201 (Created) and with body the new organisation, or with status 400 (Bad Request) if the organisation has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/organisations")
    @Timed
    public ResponseEntity<OrganisationDTO> createOrganisation(@Valid @RequestBody OrganisationDTO organisationDTO) throws URISyntaxException {
        log.debug("REST request to save Organisation : {}", organisationDTO);
        if (organisationDTO.getId() != null) {
            return ResponseEntity.badRequest().headers(
                HeaderUtil.createFailureAlert(ENTITY_NAME,
                    "idexists",
                    "A new organisation cannot already have an ID")
            ).body(null);
        }

        OrganisationDTO result = organisationService.create(organisationDTO);

        return ResponseEntity.created(new URI("/api/organisations/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(organisationDTO);
    }

    /**
     * PUT  /organisations : Updates an existing organisation.
     *
     * @param organisationDTO the organisation to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated organisation,
     * or with status 400 (Bad Request) if the organisation is not valid,
     * or with status 500 (Internal Server Error) if the organisation couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @SecuredByAuthority({AuthorityConstants.PODIUM_ADMIN, AuthorityConstants.BBMRI_ADMIN})
    @SecuredByOrganisation(authorities = AuthorityConstants.ORGANISATION_ADMIN)
    @PutMapping("/organisations")
    @Timed
    public ResponseEntity<OrganisationDTO> updateOrganisation(@OrganisationParameter @Valid @RequestBody OrganisationDTO organisationDTO)
        throws ResourceNotFound, URISyntaxException {
            log.debug("REST request to update Organisation : {}", organisationDTO);
            if (organisationDTO.getId() == null) {
                throw new ResourceNotFound("ID not defined for organisation.");
            }

            OrganisationDTO updatedOrganisation = organisationService.update(organisationDTO);

            return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, updatedOrganisation.getId().toString()))
                .body(organisationDTO);
    }

    /**
     * GET  /organisations : get all the organisations.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of organisations in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @SecuredByAuthority({AuthorityConstants.PODIUM_ADMIN, AuthorityConstants.BBMRI_ADMIN})
    @GetMapping("/organisations")
    @Timed
    public ResponseEntity<List<OrganisationDTO>> getAllOrganisations(@ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Organisations");
        Page<OrganisationDTO> page = organisationService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/organisations");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /organisations/:id : get the "id" organisation.
     *
     * @param id the id of the organisation to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the organisation, or with status 404 (Not Found)
     */
    @SecuredByAuthority({AuthorityConstants.PODIUM_ADMIN, AuthorityConstants.BBMRI_ADMIN})
    @GetMapping("/organisations/{id}")
    @Timed
    @Deprecated
    public ResponseEntity<OrganisationDTO> getOrganisation(@PathVariable Long id) {
        log.debug("REST request to get Organisation : {}", id);
        OrganisationDTO organisationDTO = organisationService.findOneDTO(id);
        if (organisationDTO == null) {
            throw new ResourceNotFound(String.format("Organisation not found with id: %s.", id));
        }
        return ResponseEntity.ok(organisationDTO);
    }

    /**
     * GET  /organisations/uuid/:uuid : get the "uuid" organisation.
     *
     * @param uuid the uuid of the organisation to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the organisation, or with status 404 (Not Found)
     */
    @SecuredByAuthority({AuthorityConstants.PODIUM_ADMIN, AuthorityConstants.BBMRI_ADMIN})
    @SecuredByOrganisation
    @GetMapping("/organisations/uuid/{uuid}")
    @Timed
    public ResponseEntity<OrganisationDTO> getOrganisation(@OrganisationUuidParameter @PathVariable UUID uuid) {
        log.debug("REST request to get Organisation : {}", uuid);
        OrganisationDTO organisationDTO = organisationService.findByUuid(uuid);
        if (organisationDTO == null) {
            throw new ResourceNotFound(String.format("Organisation not found with uuid: %s.", uuid));
        }

        return ResponseEntity.ok(organisationDTO);
    }

    /**
     * PUT /organisations/:id/activation?value=:activation : activate or deactivate the "id" organisation
     *
     * @param id the id of the organisation to be activated/deactivated
     * @param activation boolean activation flag (true or false)
     * @return the ResponseEntity with status 200 (OK) and with body the updated organisation,
     * or with status 400 (Bad Request) if the organisation is not valid,
     * or with status 500 (Internal Server Error) if the organisation couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @SecuredByAuthority({AuthorityConstants.PODIUM_ADMIN, AuthorityConstants.BBMRI_ADMIN})
    @PutMapping("/organisations/{id}/activation")
    @Timed
    public ResponseEntity<OrganisationDTO> setOrganisationActivation(
        @PathVariable Long id,  @RequestParam (value = "value",required = true) boolean activation) throws
        URISyntaxException {

        // FIXME: Use UUID as unique id argument
            log.debug("REST request to activate/deactivate Organisation : {}", id, activation);
            OrganisationDTO updatedOrganisationDTO = organisationService.activation(id, activation);

            return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, updatedOrganisationDTO.getId().toString()))
                .body(updatedOrganisationDTO);
    }

    /**
     * DELETE  /organisations/:id : delete the "id" organisation.
     *
     * @param uuid the id of the organisation to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/organisations/{uuid}")
    @Timed
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
    @Timed
    public ResponseEntity<List<SearchOrganisation>> searchOrganisations(@RequestParam String query, @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Organisations for query {}", query);
        Page<SearchOrganisation> page = organisationService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/organisations");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }


}
