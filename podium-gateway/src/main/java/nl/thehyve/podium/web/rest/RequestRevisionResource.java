/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.web.rest;

import com.codahale.metrics.annotation.Timed;
import nl.thehyve.podium.common.exceptions.ActionNotAllowed;
import nl.thehyve.podium.common.security.AuthenticatedUser;
import nl.thehyve.podium.common.security.annotations.*;
import nl.thehyve.podium.common.service.SecurityService;
import nl.thehyve.podium.common.service.dto.RequestRepresentation;
import nl.thehyve.podium.service.DraftService;
import nl.thehyve.podium.service.RequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for managing request revisions.
 */
@RestController
@RequestMapping("/api")
public class RequestRevisionResource {

    private final Logger log = LoggerFactory.getLogger(RequestRevisionResource.class);

    @Autowired
    private DraftService draftService;

    @Autowired
    private RequestService requestService;

    @Autowired
    private SecurityService securityService;


    /**
     * PUT  /requests : Update a request in revision
     *
     * @param request the request to be updated
     * @throws ActionNotAllowed when a requested action is not available for the status of the Request.
     * @return RequestRepresentation The updated request draft.
     */
    @PutMapping("/requests")
    @SecuredByRequestOwner
    @Timed
    public ResponseEntity<RequestRepresentation> updateRevisionRequest(
        @RequestParameter @RequestBody RequestRepresentation request) throws ActionNotAllowed {
        AuthenticatedUser user = securityService.getCurrentUser();
        log.debug("PUT /requests (user: {})", user);
        RequestRepresentation result = draftService.updateRevision(user, request);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * GET  /requests/:uuid/submit : Submit the request
     *
     * @param uuid of the request to be saved
     * @return the updated request representation
     * @throws ActionNotAllowed when a requested action is not available for the status of the Request.
     */
    @GetMapping("/requests/{uuid}/submit")
    @SecuredByRequestOwner
    @Timed
    public ResponseEntity<RequestRepresentation> submitRevisedRequest(
        @RequestUuidParameter @PathVariable("uuid") UUID uuid
    ) throws ActionNotAllowed {
        AuthenticatedUser user = securityService.getCurrentUser();
        log.debug("GET /requests/{}/submit (user: {})", uuid, user);
        RequestRepresentation request = requestService.submitRevision(user, uuid);
        return new ResponseEntity<>(request, HttpStatus.OK);
    }

}
