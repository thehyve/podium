/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.web.rest;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.ApiParam;
import nl.thehyve.podium.common.enumeration.RequestStatus;
import nl.thehyve.podium.common.exceptions.ActionNotAllowedInStatus;
import nl.thehyve.podium.common.security.AuthenticatedUser;
import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.common.security.UserAuthenticationToken;
import nl.thehyve.podium.common.service.SecurityService;
import nl.thehyve.podium.service.RequestService;
import nl.thehyve.podium.service.representation.RequestRepresentation;
import nl.thehyve.podium.web.rest.util.HeaderUtil;
import nl.thehyve.podium.web.rest.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
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

    @Autowired
    private RequestService requestService;

    @Autowired
    private SecurityService securityService;

    /**
     * Fetch drafts for the current user
     *
     * @param pageable the pagination information
     * @throws URISyntaxException if the Location URI syntax is incorrect
     * @return A transformed list of RequestDTOs
     */
    @GetMapping("/requests/drafts")
    @Timed
    public ResponseEntity<List<RequestRepresentation>> getAllDraftsForUser(@ApiParam Pageable pageable) throws URISyntaxException {
        AuthenticatedUser user = securityService.getCurrentUser();
        log.debug("Get all request drafts for current user : {}", user);
        Page<RequestRepresentation> page = requestService.findAllRequestsForRequesterByStatus(user, RequestStatus.Draft, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/requests/drafts");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * Create a new request draft
     *
     * @throws URISyntaxException Thrown in case of a malformed URI syntax
     * @return The requestRepresentation of the initialized request
     */
    @PostMapping("/requests/drafts")
    @PreAuthorize("isAuthenticated()")
    @Secured(AuthorityConstants.RESEARCHER)
    @Timed
    public ResponseEntity<RequestRepresentation> createDraft() throws URISyntaxException {
        AuthenticatedUser user = securityService.getCurrentUser();
        log.debug("POST /requests/drafts (user: {})", user);
        RequestRepresentation result = requestService.createDraft(user);
        log.debug("Result: {}", result.getUuid());
        return ResponseEntity.created(new URI("/api/requests/drafts"))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * Fetch the request draft
     *
     * @param uuid of the request draft
     * @throws URISyntaxException Thrown in case of a malformed URI syntax
     * @throws ActionNotAllowedInStatus when a requested action is not available for the status of the Request
     * @return The list of requestDTOs generated
     */
    @GetMapping("/requests/drafts/{uuid}")
    @Timed
    public ResponseEntity<RequestRepresentation> getDraft(@PathVariable UUID uuid) throws URISyntaxException, ActionNotAllowedInStatus {
        AuthenticatedUser user = securityService.getCurrentUser();
        RequestRepresentation request = requestService.findRequestForRequester(user, uuid);
        return new ResponseEntity<>(request, HttpStatus.OK);
    }

    /**
     * Update a request draft
     *
     * @param request the request to be updated
     * @throws ActionNotAllowedInStatus when a requested action is not available for the status of the Request.
     * @throws URISyntaxException Thrown in case of a malformed URI syntax.
     * @return RequestRepresentation The updated request draft.
     */
    @PutMapping("/requests/drafts")
    @PreAuthorize("isAuthenticated()")
    @Secured(AuthorityConstants.RESEARCHER)
    @Timed
    public ResponseEntity<RequestRepresentation> updateDraft(@RequestBody RequestRepresentation request) throws URISyntaxException, ActionNotAllowedInStatus {
        AuthenticatedUser user = securityService.getCurrentUser();
        log.debug("PUT /requests/drafts (user: {})", user);
        RequestRepresentation result = requestService.updateDraft(user, request);
        log.debug("Result: {}", result.getUuid());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Validate the request draft
     *
     * @param request the request draft to validate.
     * @return ResponseEntity OK response when the request has been validated.
     */
    @PostMapping("/requests/drafts/validate")
    @Timed
    public ResponseEntity<Void> validateDraft(@RequestBody @Valid RequestRepresentation request) {
        if (request == null) {
            throw new IllegalArgumentException("Empty request body.");
        }
        return ResponseEntity.ok().build();
    }

    /**
     * Submit the request draft
     *
     * @param uuid of the request draft to be saved
     * @throws URISyntaxException Thrown in case of a malformed URI syntax
     * @throws ActionNotAllowedInStatus when a requested action is not available for the status of the Request.
     * @return The list of requestDTOs generated
     */
    @GetMapping("/requests/drafts/{uuid}/submit")
    @Timed
    public ResponseEntity<List<RequestRepresentation>> submitDraft(@PathVariable UUID uuid) throws URISyntaxException, ActionNotAllowedInStatus {
        AuthenticatedUser user = securityService.getCurrentUser();
        log.debug("GET /requests/drafts/{}/submit (user: {})", uuid, user);
        List<RequestRepresentation> requests = requestService.submitDraft(user, uuid);
        return new ResponseEntity<>(requests, HttpStatus.OK);
    }

    /**
     * GET  /requests/requester : get all the requests for which the current user is the requester.
     *
     * @param pageable the pagination information
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     * @return the ResponseEntity with status 200 (OK) and the list of requests in body
     */
    @GetMapping("/requests/requester")
    @Timed
    public ResponseEntity<List<RequestRepresentation>> getRequesterRequests(@ApiParam Pageable pageable)
        throws URISyntaxException {
        AuthenticatedUser user = securityService.getCurrentUser();
        log.debug("REST request to get a page of Requests for requester {}", user.getName());
        Page<RequestRepresentation> page = requestService.findAllForRequester(user, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/requests/requester");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /requests/status/:status/requester : get all the requests for a requester with the status.
     *
     * @param status the status to filter on
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of requests in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/requests/status/{status}/requester")
    @Timed
    public ResponseEntity<List<RequestRepresentation>> getAllRequestsByStatus(@PathVariable RequestStatus status, @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to requests with status {}", status);
        AuthenticatedUser user = securityService.getCurrentUser();
        Page<RequestRepresentation> page = requestService.findAllRequestsForRequesterByStatus(user, status, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page,
            "/api/requests/status/" + status.toString() + "/requester");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * Update a request
     *
     * @param request the request to be updated
     * @throws ActionNotAllowedInStatus when a requested action is not available for the status of the Request.
     * @throws URISyntaxException Thrown in case of a malformed URI syntax.
     * @return RequestRepresentation The updated request draft.
     */
    @PutMapping("/requests")
    @PreAuthorize("isAuthenticated()")
    @Secured(AuthorityConstants.RESEARCHER)
    @Timed
    public ResponseEntity<RequestRepresentation> updateRequest(@RequestBody RequestRepresentation request) throws URISyntaxException, ActionNotAllowedInStatus {
        AuthenticatedUser user = securityService.getCurrentUser();
        log.debug("PUT /requests (user: {})", user);
        RequestRepresentation result = requestService.updateRequest(user, request);
        log.debug("Result: {}", result.getUuid());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Submit the request
     *
     * @param uuid of the request to be saved
     * @throws URISyntaxException Thrown in case of a malformed URI syntax
     * @throws ActionNotAllowedInStatus when a requested action is not available for the status of the Request.
     */
    @GetMapping("/requests/{uuid}/submit")
    @PreAuthorize("isAuthenticated()")
    @Timed
    public ResponseEntity<RequestRepresentation> submitRequest(@PathVariable UUID uuid) throws URISyntaxException, ActionNotAllowedInStatus {
        AuthenticatedUser user = securityService.getCurrentUser();
        log.debug("GET /requests/{}/submit (user: {})", uuid, user);
        RequestRepresentation request = requestService.submitRequest(user, uuid);
        return new ResponseEntity<>(request, HttpStatus.OK);
    }

    /**
     * GET  /requests/status/:status/organisation : get all the organisation requests for the organisations where the current
     * user is a coordinator.
     *
     * @param status the status to filter on
     * @param pageable the pagination information
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     * @return the ResponseEntity with status 200 (OK) and the list of requests in body
     */
    @GetMapping("/requests/status/{status}/organisation")
    @Timed
    public ResponseEntity<List<RequestRepresentation>> getAllOrganisationRequests(@PathVariable RequestStatus status, @ApiParam Pageable pageable)
        throws URISyntaxException {
        AuthenticatedUser user = securityService.getCurrentUser();
        log.debug("REST request to get a page of organisation requests with status {} for user {}", status, user.getName());
        Page<RequestRepresentation> page = requestService.findAllCoordinatorRequestsInStatus(user, status, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page,
            "/api/requests/status/" + status.toString() + "/organisation");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /requests/status/:status/organisation/:uuid : get all the organisation requests for the organisation if the current
     * user is a coordinator.
     *
     * @param status the status to filter on
     * @param uuid the uuid of the organisation for which to fetch the requests
     * @param pageable the pagination information
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     * @return the ResponseEntity with status 200 (OK) and the list of requests in body
     */
    @GetMapping("/requests/status/{status}/organisation/{uuid}")
    @Timed
    public ResponseEntity<List<RequestRepresentation>> getOrganisationRequestsForOrganisation(
        @PathVariable RequestStatus status, @PathVariable UUID uuid, @ApiParam Pageable pageable)
        throws URISyntaxException {
        AuthenticatedUser user = securityService.getCurrentUser();
        log.debug("REST request to get a page of organisation requests with status {} for organisation {}, user {}",
            status, uuid, user.getName());
        Page<RequestRepresentation> page = requestService.findCoordinatorRequestsForOrganisationInStatus(
            user, status, uuid, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page,
            "/api/requests/status/" + status.toString() + "/organisation/" + uuid.toString());
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * Fetch the request
     *
     * FIXME: UNSECURED - Add new annotation that check whether an member (coordinator or reviewer) of an organisation
     *        has access to a different resource such as a Request.
     * @param uuid of the request
     * @throws URISyntaxException Thrown in case of a malformed URI syntax
     * @throws ActionNotAllowedInStatus when a requested action is not available for the status of the Request
     * @return The list of requestDTOs generated
     */
    @GetMapping("/requests/{uuid}")
    @Timed
    public ResponseEntity<RequestRepresentation> getRequest(@PathVariable UUID uuid) throws URISyntaxException, ActionNotAllowedInStatus {
        RequestRepresentation request = requestService.findRequest(uuid);
        return new ResponseEntity<>(request, HttpStatus.OK);
    }

    /**
     * DELETE  /requests/drafts/:uuid : delete the "uuid" draft request.
     *
     * @param uuid the uuid of the draft request to delete
     * @return the ResponseEntity with status 200 (OK)
     * @throws ActionNotAllowedInStatus when a requested action is not available for the status of the Request.
     */
    @DeleteMapping("/requests/drafts/{uuid}")
    @Timed
    public ResponseEntity<Void> deleteDraft(@PathVariable UUID uuid) throws ActionNotAllowedInStatus {
        AuthenticatedUser user = securityService.getCurrentUser();
        log.debug("REST request to delete Request : {}", uuid);
        requestService.deleteDraft(user, uuid);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, uuid.toString())).build();
    }

    /**
     * GET /requests/:uuid/validate : Validate a request with uuid.
     *
     * @param uuid the uuid of the request to validate
     * @return the ResponseEntity with the validated request representation
     *
     * @throws ActionNotAllowedInStatus when a requested action is not available for the status of the Request.
     */
    @GetMapping("/requests/{uuid}/validate")
    @Timed
    public ResponseEntity<RequestRepresentation> validateRequest(@PathVariable UUID uuid) throws ActionNotAllowedInStatus {
        log.debug("REST request to validate request process for : {} ", uuid);
        AuthenticatedUser user = securityService.getCurrentUser();
        RequestRepresentation requestRepresentation = requestService.validateRequest(user, uuid);
        return new ResponseEntity<>(requestRepresentation, HttpStatus.OK);
    }

    /**
     * GET /requests/:uuid/reject : Reject a request with uuid.
     *
     * @param uuid the uuid of the request to reject
     * @return the ResponseEntity with the rejected request representation
     *
     * @throws ActionNotAllowedInStatus when a requested action is not available for the status of the Request.
     */
    @GetMapping("/requests/{uuid}/reject")
    @Timed
    public ResponseEntity<RequestRepresentation> rejectRequest(@PathVariable UUID uuid) throws ActionNotAllowedInStatus {
        log.debug("REST request to reject request process for : {} ", uuid);
        AuthenticatedUser user = securityService.getCurrentUser();
        RequestRepresentation requestRepresentation = requestService.rejectRequest(user, uuid);
        return new ResponseEntity<>(requestRepresentation, HttpStatus.OK);
    }

    /**
     * GET /requests/:uuid/approve : Approve a request with uuid.
     *
     * @param uuid the uuid of the request to approve
     * @return the ResponseEntity with the approved request representation
     *
     * @throws ActionNotAllowedInStatus when a requested action is not available for the status of the Request.
     */
    @GetMapping("/requests/{uuid}/approve")
    @Timed
    public ResponseEntity<RequestRepresentation> approveRequest(@PathVariable UUID uuid) throws ActionNotAllowedInStatus {
        log.debug("REST request to approve request process for : {} ", uuid);
        AuthenticatedUser user = securityService.getCurrentUser();
        RequestRepresentation requestRepresentation = requestService.approveRequest(user, uuid);
        return new ResponseEntity<>(requestRepresentation, HttpStatus.OK);
    }

    /**
     * GET /requests/:uuid/revision : Request a revision for request with uuid.
     *
     * @param uuid the uuid of the request to request revision for
     * @return the ResponseEntity with the updated request representation
     *
     * @throws ActionNotAllowedInStatus when a requested action is not available for the status of the Request.
     */
    @GetMapping("/requests/{uuid}/revision")
    @Timed
    public ResponseEntity<RequestRepresentation> revisionRequest(@PathVariable UUID uuid) throws ActionNotAllowedInStatus {
        log.debug("REST request to apply revision to request details for : {} ", uuid);
        AuthenticatedUser user = securityService.getCurrentUser();
        RequestRepresentation requestRepresentation = requestService.reviseRequest(user, uuid);
        return new ResponseEntity<>(requestRepresentation, HttpStatus.OK);
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
    public ResponseEntity<List<RequestRepresentation>> searchRequests(@RequestParam String query, @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Requests for query {}", query);
        Page<RequestRepresentation> page = requestService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/requests");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
}
