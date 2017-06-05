/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.service;

import nl.thehyve.podium.domain.RequestDetail;
import nl.thehyve.podium.repository.RequestDetailRepository;
import nl.thehyve.podium.repository.search.RequestdetailSearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

/**
 * Service Implementation for managing RequestDetail.
 */
@Service
@Transactional
public class RequestDetailService {

    private final Logger log = LoggerFactory.getLogger(RequestDetailService.class);

    @Autowired
    private RequestDetailRepository requestDetailRepository;

    @Autowired
    private RequestdetailSearchRepository requestdetailSearchRepository;

    public RequestDetailService() {
    }

    /**
     * Save a requestDetail.
     *
     * @param requestDetail the entity to save
     * @return the persisted entity
     */
    public RequestDetail save(RequestDetail requestDetail) {
        log.debug("Request to save RequestDetail : {}", requestDetail);
        RequestDetail result = requestDetailRepository.save(requestDetail);
        requestdetailSearchRepository.save(result);
        return result;
    }

    /**
     * Search for the requestdetail corresponding to the query.
     *
     * @param query the query of the search
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<RequestDetail> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Requestdetails for query {}", query);
        return requestdetailSearchRepository.search(queryStringQuery(query), pageable);
    }
}
