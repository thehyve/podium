package org.bbmri.podium.service;

import org.bbmri.podium.domain.Organisation;
import org.bbmri.podium.repository.OrganisationRepository;
import org.bbmri.podium.repository.search.OrganisationSearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
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

    public OrganisationService(OrganisationRepository organisationRepository, OrganisationSearchRepository organisationSearchRepository) {
        this.organisationRepository = organisationRepository;
        this.organisationSearchRepository = organisationSearchRepository;
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
     *  Delete the  organisation by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Organisation : {}", id);
        organisationRepository.delete(id);
        organisationSearchRepository.delete(id);
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
