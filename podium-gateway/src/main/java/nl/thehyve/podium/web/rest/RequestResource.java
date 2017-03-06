/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.web.rest;

import com.codahale.metrics.annotation.Timed;
import nl.thehyve.podium.web.rest.util.PaginationUtil;
import nl.thehyve.podium.service.RequestService;
import nl.thehyve.podium.web.rest.util.HeaderUtil;
import nl.thehyve.podium.service.dto.RequestDTO;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing Request.
 */
@RestController
@RequestMapping("/api")
public class RequestResource {

    private final Logger log = LoggerFactory.getLogger(RequestResource.class);

    private static final String ENTITY_NAME = "request";

    private final RequestService requestService;

    public RequestResource(RequestService requestService) {
        this.requestService = requestService;
    }

    /**
     * Fetch drafts for user
     *
     * @param uuid The UUID to perform the lookup for
     * @return A transformed list of RequestDTOs
     */
    @GetMapping("/requests/drafts/{uuid}")
    @Timed
    public ResponseEntity<List<RequestDTO>> getAllDraftsForUser(@PathVariable String uuid) {
        log.debug("Get all request drafts for uuid");
        List<RequestDTO> requests = requestService.findAllRequestDraftsByUserUuid(UUID.fromString(uuid));
        return new ResponseEntity<>(requests, HttpStatus.OK);
    }

    /**
     * Setup an initial request
     *
     * @param uuid The UUID to create the request by
     * @return The requestDTO of the initialized request
     * @throws URISyntaxException Thrown in case of a malformed URI syntax
     */
    @GetMapping("/requests/initialize/{uuid}")
    @Timed
    public ResponseEntity<RequestDTO> initializeRequest(@PathVariable String uuid) throws URISyntaxException {
        RequestDTO result = requestService.initializeBaseRequest(UUID.fromString(uuid));
        return ResponseEntity.created(new URI("/api/requests/initialize/"+uuid))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * GET  /requests : get all the requests.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of requests in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/requests")
    @Timed
    public ResponseEntity<List<RequestDTO>> getAllRequests(@ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Requests");
        Page<RequestDTO> page = requestService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/requests");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * DELETE  /requests/:id : delete the "id" request.
     *
     * @param id the id of the requestDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/requests/{id}")
    @Timed
    public ResponseEntity<Void> deleteRequest(@PathVariable Long id) {
        log.debug("REST request to delete Request : {}", id);
        requestService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    /**
     * SEARCH  /_search/requests?query=:query : search for the request corresponding
     * to the query.
     *
     * @param query the query of the request search
     * @param pageable the pagination information
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/_search/requests")
    @Timed
    public ResponseEntity<List<RequestDTO>> searchRequests(@RequestParam String query, @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Requests for query {}", query);
        Page<RequestDTO> page = requestService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/requests");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }


}
