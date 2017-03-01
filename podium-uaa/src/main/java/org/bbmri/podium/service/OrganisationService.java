/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

package org.bbmri.podium.service;

import org.bbmri.podium.domain.Authority;
import org.bbmri.podium.domain.Organisation;
import org.bbmri.podium.domain.Role;
import org.bbmri.podium.search.SearchOrganisation;
import org.bbmri.podium.repository.AuthorityRepository;
import org.bbmri.podium.repository.OrganisationRepository;
import org.bbmri.podium.repository.search.OrganisationSearchRepository;
import org.bbmri.podium.security.AuthorityConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * Service Implementation for managing Organisation.
 */
@Service
@Transactional
public class OrganisationService {

    private final Logger log = LoggerFactory.getLogger(OrganisationService.class);

    @Autowired
    private OrganisationRepository organisationRepository;

    @Autowired
    private OrganisationSearchRepository organisationSearchRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private RoleService roleService;


    @Transactional(readOnly = true)
    public Set<Authority> findOrganisationAuthorities() {
        Set<Authority> result = new LinkedHashSet<>(3);
        result.add(authorityRepository.findOne(AuthorityConstants.ORGANISATION_ADMIN));
        result.add(authorityRepository.findOne(AuthorityConstants.ORGANISATION_COORDINATOR));
        result.add(authorityRepository.findOne(AuthorityConstants.REVIEWER));
        return result;
    }

    /**
     * Save a organisation.
     *
     * @param organisation the entity to save
     * @return the persisted entity
     */
    public Organisation save(Organisation organisation) {
        log.debug("Request to save Organisation : {}", organisation);
        if (organisation.getRoles() == null || organisation.getRoles().isEmpty()) {
            Set<Role> roles = findOrganisationAuthorities().stream()
                .map(authority -> roleService.save(new Role(authority, organisation)))
                .collect(Collectors.toSet());
            organisation.setRoles(roles);
        }
        Organisation result = organisationRepository.save(organisation);
        log.info("Saved organisation: {}", result);
        SearchOrganisation searchOrganisation = organisationSearchRepository.findOne(organisation.getId());
        if (searchOrganisation == null) {
            searchOrganisation = new SearchOrganisation(result);
        }
        searchOrganisation.copyProperties(result);
        organisationSearchRepository.save(searchOrganisation);
        log.info("Saved to elastic search: {}", result);
        return result;
    }

    /**
     *  Get the number of organisations.
     *
     *  @return the number of entities
     */
    @Transactional(readOnly = true)
    public Long count() {
        log.debug("Request to count all Organisations");
        return organisationRepository.countByDeletedFalse();
    }

    /**
     *  Get all the organisations.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<Organisation> findAll(Pageable pageable) {
        log.debug("Request to get all Organisations");
        Page<Organisation> result = organisationRepository.findAllByDeletedFalse(pageable);
        return result;
    }

    /**
     *  Get one organisation by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public Organisation findOne(Long id) {
        log.debug("Request to get Organisation : {}", id);
        Organisation organisation = organisationRepository.findByIdAndDeletedFalse(id);
        return organisation;
    }

    /**
     *  Get one organisation by uuid.
     *
     *  @param uuid the uuid of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public Organisation findByUuid(UUID uuid) {
        log.debug("Request to get Organisation : {}", uuid);
        Organisation organisation = organisationRepository.findByUuidAndDeletedFalse(uuid);
        return organisation;
    }

    /**
     *  Mark the organisation as deleted.
     *
     *  @param organisation the organisation to mark deleted.
     */
    public void delete(Organisation organisation) {
        log.debug("Request to delete Organisation : {}", organisation.getUuid());

        organisation.setDeleted(true);
        save(organisation);
        organisationSearchRepository.delete(organisation.getId());
    }

    /**
     * Search for the organisation corresponding to the query.
     *
     *  @param query the query of the search
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<SearchOrganisation> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Organisations for query {}", query);
        Page<SearchOrganisation> result = organisationSearchRepository.search(queryStringQuery(query), pageable);
        return result;
    }

}
