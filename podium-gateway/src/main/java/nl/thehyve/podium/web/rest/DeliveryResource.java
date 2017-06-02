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
import nl.thehyve.podium.common.service.dto.DeliveryProcessRepresentation;
import nl.thehyve.podium.common.service.dto.DeliveryReferenceRepresentation;
import nl.thehyve.podium.common.service.dto.MessageRepresentation;
import nl.thehyve.podium.common.service.dto.RequestRepresentation;
import nl.thehyve.podium.service.DeliveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing deliveries.
 */
@RestController
@RequestMapping("/api")
public class DeliveryResource {

    private final Logger log = LoggerFactory.getLogger(DeliveryResource.class);

    private static final String ENTITY_NAME = "delivery";

    @Autowired
    private DeliveryService deliveryService;

    @Autowired
    private SecurityService securityService;

    /**
     * GET /requests/:requestUuid/deliveries
     * Fetch delivery processes for a request
     *
     * @param requestUuid of the request to fetch the delivery processes for
     * @return the list of delivery process representations
     * @throws URISyntaxException Thrown in case of a malformed URI syntax
     * @throws ActionNotAllowed when a requested action is not available for the status of the Request.
     */
    @GetMapping("/requests/{requestUuid}/deliveries")
    @SecuredByRequestOwner
    @SecuredByRequestOrganisationCoordinator
    @Timed
    public ResponseEntity<List<DeliveryProcessRepresentation>> getDeliveryProcesses(
        @RequestUuidParameter @PathVariable("requestUuid") UUID requestUuid
    ) throws URISyntaxException, ActionNotAllowed {
        AuthenticatedUser user = securityService.getCurrentUser();
        log.debug("GET /requests/{}/deliveries (user: {})", requestUuid, user);
        List<DeliveryProcessRepresentation> deliveryProcesses = deliveryService.getDeliveriesForRequest(requestUuid);
        return new ResponseEntity<>(deliveryProcesses, HttpStatus.OK);
    }

    /**
     * GET /requests/:requestUuid/startDelivery
     * Start delivery processes for a request
     *
     * @param requestUuid of the request to start delivery processes for
     * @return the list of created delivery process representations
     * @throws URISyntaxException Thrown in case of a malformed URI syntax
     * @throws ActionNotAllowed when a requested action is not available for the status of the Request.
     */
    @GetMapping("/requests/{requestUuid}/startDelivery")
    @SecuredByRequestOrganisationCoordinator
    @Timed
    public ResponseEntity<RequestRepresentation> startDelivery(
        @RequestUuidParameter @PathVariable("requestUuid") UUID requestUuid
    ) throws URISyntaxException, ActionNotAllowed {
        AuthenticatedUser user = securityService.getCurrentUser();
        log.debug("GET /requests/{}/startDelivery (user: {})", requestUuid, user);
        RequestRepresentation request = deliveryService.startDelivery(user, requestUuid);
        return new ResponseEntity<>(request, HttpStatus.OK);
    }

    /**
     * POST /requests/:requestUuid/deliveries/:deliveryProcessUuid/release
     * Release the delivery with uuid deliveryProcessUuid.
     *
     * @param requestUuid the uuid of the request the delivery process belongs to
     * @param deliveryProcessUuid the uuid of delivery process
     * @param reference the reference to the released delivery (e.g., URL, track and trace number)
     * @return the ResponseEntity with the delivery process representation
     *
     * @throws ActionNotAllowed when a requested action is not available for the status of the delivery.
     */
    @PostMapping("/requests/{requestUuid}/deliveries/{deliveryProcessUuid}/release")
    @SecuredByRequestOrganisationCoordinator
    @Timed
    public ResponseEntity<DeliveryProcessRepresentation> releaseDelivery(
        @RequestUuidParameter @PathVariable("requestUuid") UUID requestUuid,
        @PathVariable("deliveryProcessUuid") UUID deliveryProcessUuid,
        @RequestBody @Valid DeliveryReferenceRepresentation reference
    ) throws ActionNotAllowed {
        log.info("REST request to release delivery process for request {}, delivery {}", requestUuid, deliveryProcessUuid);
        AuthenticatedUser user = securityService.getCurrentUser();
        DeliveryProcessRepresentation deliveryProcess = deliveryService.release(user, requestUuid, deliveryProcessUuid, reference);
        return new ResponseEntity<>(deliveryProcess, HttpStatus.OK);
    }

    /**
     * POST /requests/:requestUuid/deliveries/:deliveryProcessUuid/cancel
     * Cancel a already released delivery with uuid deliveryProcessUuid.
     *
     * @param requestUuid the uuid of the request the delivery process belongs to
     * @param deliveryProcessUuid the uuid of delivery process
     * @param message the podium event message representation
     * @return the ResponseEntity with the delivery process representation
     *
     * @throws ActionNotAllowed when a requested action is not available for the status of the delivery.
     */
    @PostMapping("/requests/{requestUuid}/deliveries/{deliveryProcessUuid}/cancel")
    @SecuredByRequestOrganisationCoordinator
    @Timed
    public ResponseEntity<DeliveryProcessRepresentation> cancelDelivery(
        @RequestUuidParameter @PathVariable("requestUuid") UUID requestUuid,
        @PathVariable("deliveryProcessUuid") UUID deliveryProcessUuid,
        @RequestBody MessageRepresentation message
    ) throws ActionNotAllowed {
        log.debug("REST request to cancel delivery process for request {}, delivery {}", requestUuid, deliveryProcessUuid);
        AuthenticatedUser user = securityService.getCurrentUser();
        DeliveryProcessRepresentation deliveryProcess = deliveryService.cancel(user, requestUuid, deliveryProcessUuid, message);
        return new ResponseEntity<>(deliveryProcess, HttpStatus.OK);
    }

    /**
     * GET /requests/:requestUuid/deliveries/:deliveryProcessUuid/received
     * Mark the delivery with uuid deliveryProcessUuid as received.
     *
     * @param requestUuid the uuid of the request the delivery process belongs to
     * @param deliveryProcessUuid the uuid of delivery process
     * @return the ResponseEntity with the delivery process representation
     *
     * @throws ActionNotAllowed when a requested action is not available for the status of the delivery.
     */
    @GetMapping("/requests/{requestUuid}/deliveries/{deliveryProcessUuid}/received")
    @SecuredByRequestOwner
    @SecuredByRequestOrganisationCoordinator
    @Timed
    public ResponseEntity<DeliveryProcessRepresentation> deliveryReceived(
        @RequestUuidParameter @PathVariable("requestUuid") UUID requestUuid,
        @PathVariable("deliveryProcessUuid") UUID deliveryProcessUuid,
        @RequestBody @Valid DeliveryReferenceRepresentation reference
    ) throws ActionNotAllowed {
        log.debug("REST request to mark delivery process as received for request {}, delivery {}", requestUuid, deliveryProcessUuid);
        AuthenticatedUser user = securityService.getCurrentUser();
        DeliveryProcessRepresentation deliveryProcess = deliveryService.received(user, requestUuid, deliveryProcessUuid);
        return new ResponseEntity<>(deliveryProcess, HttpStatus.OK);
    }

}
