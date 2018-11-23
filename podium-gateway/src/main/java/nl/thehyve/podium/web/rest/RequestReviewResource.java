/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.web.rest;

import com.codahale.metrics.annotation.Timed;
import nl.thehyve.podium.common.exceptions.AccessDenied;
import nl.thehyve.podium.common.exceptions.ActionNotAllowed;
import nl.thehyve.podium.common.security.AuthenticatedUser;
import nl.thehyve.podium.common.security.annotations.*;
import nl.thehyve.podium.common.service.SecurityService;
import nl.thehyve.podium.common.service.dto.MessageRepresentation;
import nl.thehyve.podium.common.service.dto.RequestRepresentation;
import nl.thehyve.podium.common.service.dto.ReviewFeedbackRepresentation;
import nl.thehyve.podium.service.ReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for managing reviews.
 */
@RestController
@RequestMapping("/api")
public class RequestReviewResource {

    private final Logger log = LoggerFactory.getLogger(RequestReviewResource.class);

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private SecurityService securityService;


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
        RequestRepresentation requestRepresentation = reviewService.validateRequest(user, uuid);
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
        RequestRepresentation requestRepresentation = reviewService.rejectRequest(user, uuid, message);
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
        RequestRepresentation requestRepresentation = reviewService.approveRequest(user, uuid);
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
        RequestRepresentation requestRepresentation = reviewService.requestRevision(user, uuid, message);
        return new ResponseEntity<>(requestRepresentation, HttpStatus.OK);
    }

    /**
     * PUT /requests/:uuid/review : Submit review feedback for a request in review status.
     *
     * @param uuid the uuid of the request to provide the review feedback for.
     * @param feedback the review feedback representation holding the advice and optional message.
     *
     * @throws AccessDenied if the current user is not the owner of the feedback.
     * @throws ActionNotAllowed when the request is not in status 'Review', the feedback is not part of the request, or
     * the feedback has already been saved before.
     */
    @PutMapping("/requests/{uuid}/review")
    @SecuredByRequestOrganisationReviewer
    @Timed
    public ResponseEntity<RequestRepresentation> submitReviewFeedback(
        @RequestUuidParameter @PathVariable("uuid") UUID uuid,
        @RequestBody ReviewFeedbackRepresentation feedback
    ) throws ActionNotAllowed {
        log.debug("REST request to provide review feedback advice for request : {}", uuid);
        AuthenticatedUser user = securityService.getCurrentUser();

        RequestRepresentation request = reviewService.saveReviewFeedback(user, uuid, feedback);

        return new ResponseEntity<>(request, HttpStatus.OK);
    }

}
