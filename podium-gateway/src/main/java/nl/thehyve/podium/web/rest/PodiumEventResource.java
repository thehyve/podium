/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

package nl.thehyve.podium.web.rest;

import com.codahale.metrics.annotation.Timed;
import nl.thehyve.podium.common.enumeration.RequestStatus;
import nl.thehyve.podium.common.enumeration.Status;
import nl.thehyve.podium.common.exceptions.ActionNotAllowedInStatus;
import nl.thehyve.podium.common.security.annotations.RequestUuidParameter;
import nl.thehyve.podium.common.security.annotations.SecuredByRequestOwner;
import nl.thehyve.podium.common.service.dto.AuditEventRepresentation;
import nl.thehyve.podium.service.PodiumEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URISyntaxException;
import java.util.UUID;

/**
 * REST controller for managing PodiumEvent.
 */
@RestController
@RequestMapping("/api")
public class PodiumEventResource {

    private final Logger log = LoggerFactory.getLogger(RequestResource.class);

    @Autowired
    private PodiumEventService podiumEventService;

    @GetMapping("/events/request/{uuid}/status/{status}")
    @SecuredByRequestOwner
    @Timed
    public ResponseEntity<AuditEventRepresentation> getLatestEventForRequest(
        @RequestUuidParameter @PathVariable("uuid") UUID uuid,
        @PathVariable("status") RequestStatus status
    )  throws URISyntaxException, ActionNotAllowedInStatus {
        log.debug("Gateway request {} for status {}", uuid, status);
        return podiumEventService.findLatestEventForRequestByStatus(uuid, status);
    }
}
