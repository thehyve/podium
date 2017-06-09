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
import nl.thehyve.podium.common.exceptions.ActionNotAllowed;
import nl.thehyve.podium.common.security.AuthenticatedUser;
import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.common.security.annotations.*;
import nl.thehyve.podium.common.service.SecurityService;
import nl.thehyve.podium.common.service.dto.MessageRepresentation;
import nl.thehyve.podium.common.service.dto.ReviewFeedbackRepresentation;
import nl.thehyve.podium.service.RequestService;
import nl.thehyve.podium.common.service.dto.RequestRepresentation;
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
import org.springframework.web.bind.annotation.*;

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
    @SecuredByAuthority(AuthorityConstants.RESEARCHER)
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
    @SecuredByAuthority(AuthorityConstants.RESEARCHER)
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
     * @throws ActionNotAllowed when a requested action is not available for the status of the Request
     * @return The list of requestDTOs generated
     */
    @GetMapping("/requests/drafts/{uuid}")
    @SecuredByRequestOwner
    @Timed
    public ResponseEntity<RequestRepresentation> getDraft(
        @RequestUuidParameter @PathVariable("uuid") UUID uuid) throws URISyntaxException, ActionNotAllowed {
        AuthenticatedUser user = securityService.getCurrentUser();
        RequestRepresentation request = requestService.findRequestForRequester(user, uuid);
        return new ResponseEntity<>(request, HttpStatus.OK);
    }

    /**
     * Update a request draft
     *
     * @param request the request to be updated
     * @throws ActionNotAllowed when a requested action is not available for the status of the Request.
     * @throws URISyntaxException Thrown in case of a malformed URI syntax.
     * @return RequestRepresentation The updated request draft.
     */
    @PutMapping("/requests/drafts")
    @SecuredByRequestOwner
    @Timed
    public ResponseEntity<RequestRepresentation> updateDraft(
        @RequestParameter @RequestBody RequestRepresentation request) throws URISyntaxException, ActionNotAllowed {
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
    @AnyAuthorisedUser
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
     * @throws ActionNotAllowed when a requested action is not available for the status of the Request.
     * @return The list of requestDTOs generated
     */
    @GetMapping("/requests/drafts/{uuid}/submit")
    @SecuredByRequestOwner
    @Timed
    public ResponseEntity<List<RequestRepresentation>> submitDraft(
        @RequestUuidParameter @PathVariable("uuid") UUID uuid) throws URISyntaxException, ActionNotAllowed {
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
    @SecuredByAuthority(AuthorityConstants.RESEARCHER)
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
    @SecuredByAuthority(AuthorityConstants.RESEARCHER)
    @Timed
    public ResponseEntity<List<RequestRepresentation>> getRequesterRequestsByStatus(@PathVariable("status") RequestStatus status, @ApiParam Pageable pageable)
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
     * @throws ActionNotAllowed when a requested action is not available for the status of the Request.
     * @throws URISyntaxException Thrown in case of a malformed URI syntax.
     * @return RequestRepresentation The updated request draft.
     */
    @PutMapping("/requests")
    @SecuredByRequestOwner
    @Timed
    public ResponseEntity<RequestRepresentation> updateRevisionRequest(
        @RequestParameter @RequestBody RequestRepresentation request) throws URISyntaxException, ActionNotAllowed {
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
     * @return the updated request representation
     * @throws URISyntaxException Thrown in case of a malformed URI syntax
     * @throws ActionNotAllowed when a requested action is not available for the status of the Request.
     */
    @GetMapping("/requests/{uuid}/submit")
    @SecuredByRequestOwner
    @Timed
    public ResponseEntity<RequestRepresentation> submitRevisedRequest(
        @RequestUuidParameter @PathVariable("uuid") UUID uuid
    ) throws URISyntaxException, ActionNotAllowed {
        AuthenticatedUser user = securityService.getCurrentUser();
        log.debug("GET /requests/{}/submit (user: {})", uuid, user);
        RequestRepresentation request = requestService.submitRevision(user, uuid);
        return new ResponseEntity<>(request, HttpStatus.OK);
    }

    /**
     * GET  /requests/reviewer : get all the organisation requests in review status for the organisations where the current
     * user is a reviewer.
     *
     * @param pageable the pagination information
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     * @return the ResponseEntity with status 200 (OK) and the list of requests in body
     */
    @GetMapping("/requests/reviewer")
    @SecuredByAuthority(AuthorityConstants.REVIEWER)
    @Timed
    public ResponseEntity<List<RequestRepresentation>> getReviewerRequests(@ApiParam Pageable pageable)
        throws URISyntaxException {
        AuthenticatedUser user = securityService.getCurrentUser();
        log.debug("REST request to get a page of requests in review status for reviewer {}", user.getName());
        Page<RequestRepresentation> page = requestService.findAllForReviewer(user, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page,
            "/api/requests/reviewer");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /requests/status/:status/coordinator : get all the organisation requests for the organisations where the current
     * user is a coordinator.
     *
     * @param status the status to filter on
     * @param pageable the pagination information
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     * @return the ResponseEntity with status 200 (OK) and the list of requests in body
     */
    @GetMapping("/requests/status/{status}/coordinator")
    @SecuredByAuthority(AuthorityConstants.ORGANISATION_COORDINATOR)
    @Timed
    public ResponseEntity<List<RequestRepresentation>> getCoordinatorRequests(@PathVariable("status") RequestStatus status, @ApiParam Pageable pageable)
        throws URISyntaxException {
        AuthenticatedUser user = securityService.getCurrentUser();
        log.debug("REST request to get a page of requests with status {} for coordinator {}", status, user.getName());
        Page<RequestRepresentation> page = requestService.findAllForCoordinatorInStatus(user, status, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page,
            "/api/requests/status/" + status.toString() + "/coordinator");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /requests/organisation/:uuid/reviewer : get all the organisation requests for the organisation if the current
     * user is a coordinator.
     *
     * @param uuid the uuid of the organisation for which to fetch the requests
     * @param pageable the pagination information
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     * @return the ResponseEntity with status 200 (OK) and the list of requests in body
     */
    @GetMapping("/requests/organisation/{uuid}/reviewer")
    @SecuredByOrganisation(authorities = AuthorityConstants.REVIEWER)
    @Timed
    public ResponseEntity<List<RequestRepresentation>> getReviewerRequestsForOrganisation(
        @OrganisationUuidParameter @PathVariable("uuid") UUID uuid, @ApiParam Pageable pageable)
        throws URISyntaxException {
        AuthenticatedUser user = securityService.getCurrentUser();
        log.debug("REST request to get a page of requests for reviewers of organisation {}, user {}", uuid, user.getName());
        Page<RequestRepresentation> page = requestService.findAllForReviewerByOrganisation(user, uuid, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page,
            "/api/requests/organisation/" + uuid.toString() + "/reviewer");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /requests/status/:status/organisation/:uuid/coordinator : get all the organisation requests for the organisation if the current
     * user is a coordinator.
     *
     * @param status the status to filter on
     * @param uuid the uuid of the organisation for which to fetch the requests
     * @param pageable the pagination information
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     * @return the ResponseEntity with status 200 (OK) and the list of requests in body
     */
    @GetMapping("/requests/status/{status}/organisation/{uuid}/coordinator")
    @SecuredByOrganisation(authorities = AuthorityConstants.ORGANISATION_COORDINATOR)
    @Timed
    public ResponseEntity<List<RequestRepresentation>> getCoordinatorRequestsForOrganisation(
        @PathVariable("status") RequestStatus status,
        @OrganisationUuidParameter @PathVariable("uuid") UUID uuid,
        @ApiParam Pageable pageable)
        throws URISyntaxException {
        AuthenticatedUser user = securityService.getCurrentUser();
        log.debug("REST request to get a page of requests with status {} for coordinators of organisation {}, user {}",
            status, uuid, user.getName());
        Page<RequestRepresentation> page = requestService.findAllForCoordinatorByOrganisationInStatus(
            user, status, uuid, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page,
            "/api/requests/status/" + status.toString() + "/organisation/" + uuid.toString()+ "/coordinator");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET /requests/:uuid : Fetch the request
     *
     * @param uuid of the request
     * @throws URISyntaxException Thrown in case of a malformed URI syntax
     * @return The list of requestDTOs
     */
    @RequestMapping(value = "/requests/{uuid}", method = RequestMethod.GET)
    @SecuredByRequestOwner
    @SecuredByRequestOrganisationCoordinator
    @SecuredByRequestOrganisationReviewer
    @Timed
    public ResponseEntity<RequestRepresentation> getRequest(
        @RequestUuidParameter @PathVariable("uuid") UUID uuid) throws URISyntaxException {
        RequestRepresentation request = requestService.findRequest(uuid);
        return new ResponseEntity<>(request, HttpStatus.OK);
    }

    /**
     * DELETE  /requests/drafts/:uuid : delete the "uuid" draft request.
     *
     * @param uuid the uuid of the draft request to delete
     * @return the ResponseEntity with status 200 (OK)
     * @throws ActionNotAllowed when a requested action is not available for the status of the Request.
     */
    @DeleteMapping("/requests/drafts/{uuid}")
    @SecuredByRequestOwner
    @Timed
    public ResponseEntity<Void> deleteDraft(
        @RequestUuidParameter @PathVariable("uuid") UUID uuid) throws ActionNotAllowed {
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
     * @throws ActionNotAllowed when a requested action is not available for the status of the Request.
     */
    @GetMapping("/requests/{uuid}/validate")
    @SecuredByRequestOrganisationCoordinator
    @Timed
    public ResponseEntity<RequestRepresentation> validateRequest(
        @RequestUuidParameter @PathVariable("uuid") UUID uuid) throws ActionNotAllowed {
        log.debug("REST request to validate request process for : {} ", uuid);
        AuthenticatedUser user = securityService.getCurrentUser();
        RequestRepresentation requestRepresentation = requestService.validateRequest(user, uuid);
        return new ResponseEntity<>(requestRepresentation, HttpStatus.OK);
    }

    /**
     * POST /requests/:uuid/reject : Reject a request with uuid.
     *
     * @param uuid the uuid of the request to reject
     * @param message the podium event message representation
     * @return the ResponseEntity with the rejected request representation
     *
     * @throws ActionNotAllowed when a requested action is not available for the status of the Request.
     */
    @PostMapping("/requests/{uuid}/reject")
    @SecuredByRequestOrganisationCoordinator
    @Timed
    public ResponseEntity<RequestRepresentation> rejectRequest(
        @RequestUuidParameter @PathVariable("uuid") UUID uuid, @RequestBody MessageRepresentation message
    ) throws ActionNotAllowed {
        log.debug("REST request to reject request process for : {} ", uuid);
        AuthenticatedUser user = securityService.getCurrentUser();
        RequestRepresentation requestRepresentation = requestService.rejectRequest(user, uuid, message);
        return new ResponseEntity<>(requestRepresentation, HttpStatus.OK);
    }

    /**
     * GET /requests/:uuid/approve : Approve a request with uuid.
     *
     * @param uuid the uuid of the request to approve
     * @return the ResponseEntity with the approved request representation
     *
     * @throws ActionNotAllowed when a requested action is not available for the status of the Request.
     */
    @GetMapping("/requests/{uuid}/approve")
    @SecuredByRequestOrganisationCoordinator
    @Timed
    public ResponseEntity<RequestRepresentation> approveRequest(
        @RequestUuidParameter @PathVariable("uuid") UUID uuid) throws ActionNotAllowed {
        log.debug("REST request to approve request process for : {} ", uuid);
        AuthenticatedUser user = securityService.getCurrentUser();
        RequestRepresentation requestRepresentation = requestService.approveRequest(user, uuid);
        return new ResponseEntity<>(requestRepresentation, HttpStatus.OK);
    }

    /**
     * POST /requests/:uuid/requestRevision : Request a revision for request with uuid.
     *
     * @param uuid the uuid of the request to request revision for
     * @param message the podium event message representation
     * @return the ResponseEntity with the updated request representation
     *
     * @throws ActionNotAllowed when a requested action is not available for the status of the Request.
     */
    @PostMapping("/requests/{uuid}/requestRevision")
    @SecuredByRequestOrganisationCoordinator
    @Timed
    public ResponseEntity<RequestRepresentation> requestRevision(
        @RequestUuidParameter @PathVariable("uuid") UUID uuid, @RequestBody MessageRepresentation message
    ) throws ActionNotAllowed {
        log.debug("REST request to apply revision to request details for : {} ", uuid);
        AuthenticatedUser user = securityService.getCurrentUser();
        RequestRepresentation requestRepresentation = requestService.requestRevision(user, uuid, message);
        return new ResponseEntity<>(requestRepresentation, HttpStatus.OK);
    }

    /**
     * PUT /requests/:uuid/review : Submit review feedback for a request in review
     *
     * @param uuid the uuid of the request to provide the review feedback for
     * @param feedback the review feedback representation holding the advice and optional message
     *
     * @throws ActionNotAllowed
     */
    @PutMapping("/requests/{uuid}/review")
    @SecuredByRequestOrganisationReviewer
    @Timed
    public ResponseEntity<RequestRepresentation> reviewRequest(
        @RequestUuidParameter @PathVariable("uuid") UUID uuid,
        @RequestBody ReviewFeedbackRepresentation feedback
    ) throws ActionNotAllowed {
        log.debug("REST request to provide review feedback advice for request : {}", uuid);
        AuthenticatedUser user = securityService.getCurrentUser();

        RequestRepresentation requestRepresentation = requestService.findRequest(uuid);
        requestService.provideReviewFeedback(user, requestRepresentation, feedback);

        return new ResponseEntity<>(requestRepresentation, HttpStatus.OK);
    }

    /**
     * POST /requests/:uuid/close : Close a request with uuid.
     *
     * @param uuid the uuid of the request to close
     * @param message the podium event message representation
     * @return the ResponseEntity with the closed request representation
     *
     * @throws ActionNotAllowed when a requested action is not available for the status of the Request.
     */
    @PostMapping("/requests/{uuid}/close")
    @SecuredByRequestOrganisationCoordinator
    @Timed
    public ResponseEntity<RequestRepresentation> closeRequest(
        @RequestUuidParameter @PathVariable("uuid") UUID uuid, @RequestBody(required = false) MessageRepresentation message
    ) throws ActionNotAllowed {
        log.debug("REST request to close request process for : {} ", uuid);
        AuthenticatedUser user = securityService.getCurrentUser();
        RequestRepresentation requestRepresentation = requestService.closeRequest(user, uuid, message);
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
    public ResponseEntity<List<RequestRepresentation>> searchRequests(@RequestParam("query") String query, @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Requests for query {}", query);
        Page<RequestRepresentation> page = requestService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/requests");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

}
