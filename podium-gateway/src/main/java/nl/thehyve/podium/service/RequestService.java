/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.service;

import nl.thehyve.podium.domain.Request;
import nl.thehyve.podium.domain.enumeration.RequestStatus;
import nl.thehyve.podium.repository.RequestRepository;
import nl.thehyve.podium.repository.search.RequestSearchRepository;
import nl.thehyve.podium.service.mapper.RequestMapper;
import nl.thehyve.podium.service.representation.RequestRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * Service Implementation for managing Request.
 */
@Service
@Transactional
public class RequestService {

    private final Logger log = LoggerFactory.getLogger(RequestService.class);

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private RequestMapper requestMapper;

    @Autowired
    private RequestSearchRepository requestSearchRepository;

    @Autowired
    private RequestDetailService requestDetailService;

    public RequestService() {
    }

    /**
     * Save a request.
     *
     * @param request the entity to save
     * @return the persisted entity
     */
    @Transactional
    public Request save(Request request) {
        return requestRepository.save(request);
    }

    /**
     *  Get all the requests.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<RequestRepresentation> findAll(Pageable pageable) {
        log.debug("Request to get all Requests");
        Page<Request> result = requestRepository.findAll(pageable);
        return result.map(requestMapper::requestToRequestDTO);
    }

    /**
     * Perform look up of request drafts by UUID
     *
     * @param uuid The UUID to perform the lookup for
     * @return the transformed DTO list of requests
     */
    @Transactional(readOnly = true)
    public List<RequestRepresentation> findAllRequestDraftsByUserUuid(UUID uuid) {
        List<Request> result = requestRepository.findAllByRequesterAndStatus(uuid, RequestStatus.Draft);
        return requestMapper.requestsToRequestDTOs(result);
    }

    @Transactional
    public RequestRepresentation initializeBaseRequest(UUID requester) {
        Request request = new Request();
        request.setStatus(RequestStatus.Draft);
        request.setRequester(requester);

        save(request);
        return requestMapper.requestToRequestDTO(request);
    }

    /**
     *  Delete the  request by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Request : {}", id);
        requestRepository.delete(id);
        requestSearchRepository.delete(id);
    }

    /**
     * Search for the request corresponding to the query.
     *
     *  @param query the query of the search
     *  @param pageable the pagination informatio
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<RequestRepresentation> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Requests for query {}", query);
        Page<Request> result = requestSearchRepository.search(queryStringQuery(query), pageable);
        return result.map(request -> requestMapper.requestToRequestDTO(request));
    }
}
