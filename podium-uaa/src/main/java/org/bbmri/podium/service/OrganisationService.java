package org.bbmri.podium.service;

import org.bbmri.podium.domain.Authority;
import org.bbmri.podium.domain.Organisation;
import org.bbmri.podium.repository.AuthorityRepository;
import org.bbmri.podium.repository.OrganisationRepository;
import org.bbmri.podium.repository.search.OrganisationSearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * Service Implementation for managing Organisation.
 */
@Service
@Transactional
public class OrganisationService {

    private final Logger log = LoggerFactory.getLogger(OrganisationService.class);

    private final OrganisationRepository organisationRepository;

    private final OrganisationSearchRepository organisationSearchRepository;

    private final AuthorityRepository authorityRepository;

    public OrganisationService(OrganisationRepository organisationRepository,
                               OrganisationSearchRepository organisationSearchRepository,
                               AuthorityRepository authorityRepository) {
        this.organisationRepository = organisationRepository;
        this.organisationSearchRepository = organisationSearchRepository;
        this.authorityRepository = authorityRepository;
    }

    @Transactional(readOnly = true)
    public Set<Authority> findOrganisationAuthorities() {
        Set<Authority> result = new LinkedHashSet<>(2);
        result.add(authorityRepository.findOne(Authority.ORGANISATION_ADMIN));
        result.add(authorityRepository.findOne(Authority.ORGANISATION_COORDINATOR));
        result.add(authorityRepository.findOne(Authority.REVIEWER));
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
        Organisation result = organisationRepository.save(organisation);
        organisationSearchRepository.save(result);
        return result;
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
        Page<Organisation> result = organisationRepository.findAll(pageable);
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
        Organisation organisation = organisationRepository.findOne(id);
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
        Organisation organisation = organisationRepository.findByUuid(uuid);
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
    }

    /**
     * Search for the organisation corresponding to the query.
     *
     *  @param query the query of the search
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<Organisation> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Organisations for query {}", query);
        Page<Organisation> result = organisationSearchRepository.search(queryStringQuery(query), pageable);
        return result;
    }

}
