/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.service;

import nl.thehyve.podium.common.exceptions.ResourceNotFound;
import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.common.service.dto.OrganisationDTO;
import nl.thehyve.podium.domain.Authority;
import nl.thehyve.podium.domain.Organisation;
import nl.thehyve.podium.domain.Role;
import nl.thehyve.podium.repository.AuthorityRepository;
import nl.thehyve.podium.repository.OrganisationRepository;
import nl.thehyve.podium.repository.search.OrganisationSearchRepository;
import nl.thehyve.podium.search.SearchOrganisation;
import nl.thehyve.podium.service.mapper.OrganisationMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

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

    @Autowired
    private OrganisationMapper organisationMapper;

    @Transactional(readOnly = true)
    public Set<Authority> findOrganisationAuthorities() {
        Set<Authority> result = new LinkedHashSet<>(3);
        result.add(authorityRepository.findOne(AuthorityConstants.ORGANISATION_ADMIN));
        result.add(authorityRepository.findOne(AuthorityConstants.ORGANISATION_COORDINATOR));
        result.add(authorityRepository.findOne(AuthorityConstants.REVIEWER));
        return result;
    }

    /**
     * Create an organisation
     *
     * @param organisationDTO the organisation to create
     * @return the created organisation
     */
    public OrganisationDTO create(OrganisationDTO organisationDTO) {
        Organisation organisation = organisationMapper.createOrganisationFromOrganisationDTO(organisationDTO);
        organisation = save(organisation);

        return organisationMapper.organisationToOrganisationDTO(organisation);
    }

    /**
     * Update an organisation
     *
     * @param organisationDTO The organisation to update
     * @return the updated organisation
     */
    public OrganisationDTO update(OrganisationDTO organisationDTO) {

        Organisation organisation = findOne(organisationDTO.getId());
        if (organisation == null) {
            throw new ResourceNotFound(String.format("Organisation not found with id: %d", organisationDTO.getId()));
        }

        organisation = organisationMapper.updateOrganisationFromOrganisationDTO(organisationDTO, organisation);

        save(organisation);

        return organisationMapper.organisationToOrganisationDTO(organisation);
    }

    /**
     * Save a organisation.
     *
     * @param organisation the entity to save
     * @return the persisted entity
     */
    public Organisation save(Organisation organisation) {
        log.debug("Request to save Organisation : {}", organisation);

        // FIXME: Check cascase ALL
        organisationRepository.save(organisation);

        if(!roleService.organisationHasAnyRole(organisation)) {
            Set<Role> roles = findOrganisationAuthorities().stream()
                .map(authority -> roleService.save(new Role(authority, organisation)))
                .collect(Collectors.toSet());
            organisation.setRoles(roles);
        }

        SearchOrganisation searchOrganisation = organisationMapper.organisationToSearchOrganisation(organisation);
        organisationSearchRepository.save(searchOrganisation);
        return organisation;
    }

    /**
     * Get the number of organisations.
     *
     * @return the number of entities
     */
    @Transactional(readOnly = true)
    public Long count() {
        log.debug("Request to count all Organisations");
        return organisationRepository.countByDeletedFalse();
    }

    /**
     * Get all the organisations.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<OrganisationDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Organisations");
        Page<Organisation> result = organisationRepository.findAllByDeletedFalse(pageable);
        return result.map(organisationMapper::organisationToOrganisationDTO);
    }

    /**
     * Get active organisations.
     *
     * @param pageable the pagination information
     * @return list of entities
     */
    @Transactional(readOnly = true)
    public Page<OrganisationDTO> findAllAvailable(Pageable pageable) {
        log.debug("Request to get all active organisations");
        Page<Organisation> result = organisationRepository.findAllByActivatedTrueAndDeletedFalse(pageable);
        return result.map(organisationMapper::organisationToOrganisationDTO);
    }

    /**
     * Get active organisations by their UUIDs.
     *
     * @param organisationUuids the UUIDs of the organisations to fetch.
     * @param pageable the pagination information
     * @return list of entities
     */
    @Transactional(readOnly = true)
    public Page<OrganisationDTO> findAvailableOrganisationsByUuids(Collection<UUID> organisationUuids, Pageable pageable) {
        log.debug("Request to get active organisations by their UUIDs");
        Page<Organisation> result = organisationRepository.findAllByActivatedTrueAndDeletedFalseAndUuidIn(
            organisationUuids, pageable);
        return result.map(organisationMapper::organisationToOrganisationDTO);
    }

    /**
     * Get one organisationDTO by id.
     *
     * @param id the id of the entity
     * @return the entity DTO
     */
    @Transactional(readOnly = true)
    public OrganisationDTO findOneDTO(Long id) {
        log.debug("Request to get Organisation DTO: {}", id);
        Organisation organisation = organisationRepository.findByIdAndDeletedFalse(id);
        return organisationMapper.organisationToOrganisationDTO(organisation);
    }

    /**
     *  Get one organisation by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public Organisation findOne(Long id) {
        log.debug("Request to get Organisation : {}", id);
        return organisationRepository.findByIdAndDeletedFalse(id);
    }

    /**
     *  Get an organisationDTO by uuid.
     *
     * @param uuid the uuid of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public OrganisationDTO findDTOByUuid(UUID uuid) {
        log.debug("Request to get Organisation : {}", uuid);
        Organisation organisation = organisationRepository.findByUuidAndDeletedFalse(uuid);
        return organisationMapper.organisationToOrganisationDTO(organisation);
    }

    /**
     *  Get one organisation by uuid.
     *
     * @param uuid the uuid of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public Organisation findByUuid(UUID uuid) {
        log.debug("Request to get Organisation : {}", uuid);
        return organisationRepository.findByUuidAndDeletedFalse(uuid);
    }

    /**
     * (De-)activate the organisation
     *
     *  @param uuid The uuid of the organisation to be activated.
     *  @param activated Boolean indicating if the organisation is to be activated or not.
     *
     *  @return OrganisationDTO of the updated Organisation
     */
    public OrganisationDTO activation(UUID uuid, boolean activated) {
        Organisation organisation = organisationRepository.findByUuidAndDeletedFalse(uuid);

        if (organisation == null) {
            throw new ResourceNotFound(String.format("Organisation not found with uuid: %s", uuid));
        }

        organisation.setActivated(activated);
        organisation = save(organisation);
        return organisationMapper.organisationToOrganisationDTO(organisation);
    }

    /**
     *  Get one organisation by short name.
     *
     *  @param shortName the short name of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public Organisation findByShortName(String shortName) {
        log.debug("Request to get Organisation : {}", shortName);
        Organisation organisation = organisationRepository.findByShortNameAndDeletedFalse(shortName);
        return organisation;
    }

    /**
     * Mark the organisation as deleted.
     *
     * @param uuid the organisation to mark deleted.
     */
    public void delete(UUID uuid) {
        log.debug("Request to delete Organisation : {}", uuid);

        Organisation organisation = organisationRepository.findByUuidAndDeletedFalse(uuid);
        if (organisation == null) {
            throw new ResourceNotFound(String.format("Organisation not found with id: %d", organisation.getId()));
        }

        organisation.setDeleted(true);
        organisationRepository.save(organisation);
        organisationSearchRepository.delete(organisation.getId());
    }

    /**
     * Search for the organisation corresponding to the query.
     *
     * @param query the query of the search
     * @param pageable Pagination object of the requested page
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<SearchOrganisation> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Organisations for query {}", query);
        Page<SearchOrganisation> result = organisationSearchRepository.search(queryStringQuery(query), pageable);
        return result;
    }

}
