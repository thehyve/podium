/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.web.rest;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.ApiParam;
import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.common.security.annotations.SecuredByAuthority;
import nl.thehyve.podium.common.web.rest.util.PaginationUtil;
import nl.thehyve.podium.domain.RequestDetail;
import nl.thehyve.podium.service.RequestDetailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URISyntaxException;
import java.util.List;

/**
 * REST controller for managing RequestDetail.
 */
@RestController
@RequestMapping("/api")
public class RequestDetailResource {

    private final Logger log = LoggerFactory.getLogger(RequestDetailResource.class);

    private static final String ENTITY_NAME = "requestdetail";

    private final RequestDetailService requestDetailService;

    public RequestDetailResource(RequestDetailService requestDetailService) {
        this.requestDetailService = requestDetailService;
    }

    /**
     * SEARCH  /_search/requestdetails?query=:query : search for the requestdetail corresponding
     * to the query.
     *
     * @param query the query of the requestdetail search
     * @param pageable the pagination information
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/_search/requestdetails")
    @SecuredByAuthority({ AuthorityConstants.ORGANISATION_COORDINATOR, AuthorityConstants.PODIUM_ADMIN })
    @Timed
    public ResponseEntity<List<RequestDetail>> searchRequestdetails(@RequestParam String query, @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Requestdetails for query {}", query);
        Page<RequestDetail> page = requestDetailService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/requestdetails");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
}
