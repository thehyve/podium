/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.service;

import com.codahale.metrics.annotation.Timed;
import nl.thehyve.podium.common.enumeration.DeliveryStatus;
import nl.thehyve.podium.common.enumeration.RequestStatus;
import nl.thehyve.podium.common.enumeration.RequestType;
import nl.thehyve.podium.common.enumeration.Status;
import nl.thehyve.podium.common.event.StatusUpdateEvent;
import nl.thehyve.podium.common.exceptions.ActionNotAllowedInStatus;
import nl.thehyve.podium.common.exceptions.ResourceNotFound;
import nl.thehyve.podium.common.security.AuthenticatedUser;
import nl.thehyve.podium.common.service.dto.DeliveryProcessRepresentation;
import nl.thehyve.podium.common.service.dto.DeliveryReferenceRepresentation;
import nl.thehyve.podium.common.service.dto.MessageRepresentation;
import nl.thehyve.podium.domain.*;
import nl.thehyve.podium.repository.RequestRepository;
import nl.thehyve.podium.service.mapper.DeliveryProcessMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Service implementation for managing deliveries.
 */
@Service
@Transactional
@Timed
public class DeliveryService {

    private final Logger log = LoggerFactory.getLogger(DeliveryService.class);

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private RequestService requestService;

    @Autowired
    private DeliveryProcessService deliveryProcessService;

    @Autowired
    private DeliveryProcessMapper deliveryProcessMapper;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private EntityManager entityManager;


    @Transactional
    private void persistAndPublishDeliveryEvent(DeliveryProcess deliveryProcess, StatusUpdateEvent event) {
        PodiumEvent historicEvent = new PodiumEvent(event);
        entityManager.persist(historicEvent);
        deliveryProcess.addHistoricEvent(historicEvent);
        entityManager.persist(deliveryProcess);
        log.info("About to publish delivery event: {}", event);
        publisher.publishEvent(event);
    }

    private void publishDeliveryStatusUpdate(AuthenticatedUser user, Status sourceStatus, Request request, DeliveryProcess deliveryProcess, MessageRepresentation message) {
        StatusUpdateEvent event =
            new StatusUpdateEvent(user, sourceStatus, deliveryProcess.getStatus(), request.getUuid(), deliveryProcess.getUuid(), message);
        persistAndPublishDeliveryEvent(deliveryProcess, event);
    }

    /**
     * Checks if the request has the required status.
     * @param request the request object.
     * @param status the required status.
     * @throws ActionNotAllowedInStatus iff the request does not have the required status.
     */
    private void checkStatus(Request request, RequestStatus status) throws ActionNotAllowedInStatus {
        if (request.getStatus() != status) {
            throw ActionNotAllowedInStatus.forStatus(request.getStatus());
        }
    }

    /**
     * Checks if the delivery process has the required status.
     * @param deliveryProcess the delivery process object.
     * @param status the required status.
     * @throws ActionNotAllowedInStatus iff the delivery process does not have the required status.
     */
    private void checkDeliveryStatus(DeliveryProcess deliveryProcess, DeliveryStatus status) throws ActionNotAllowedInStatus {
        if (deliveryProcess.getStatus() != status) {
            throw ActionNotAllowedInStatus.forStatus(deliveryProcess.getStatus());
        }
    }

    /**
     * Gets the delivery process with the provided uuid from the request.
     * @param request the request to get the delivery process for.
     * @param deliveryProcessUuid the uuid of the delivery process.
     * @return the delivery process object.
     * @throws ResourceNotFound iff no delivery process with the uuid exists for the request.
     */
    private DeliveryProcess getDeliveryProcess(Request request, UUID deliveryProcessUuid) {
        Optional<DeliveryProcess> deliveryProcessOptional = request.getDeliveryProcesses().stream()
            .filter(process -> process.getUuid().equals(deliveryProcessUuid)).findAny();
        if (deliveryProcessOptional.isPresent()) {
            return deliveryProcessOptional.get();
        }
        throw new ResourceNotFound("Could not find delivery process with uuid " + deliveryProcessUuid.toString());
    }

    /**
     *
     * @param uuid
     * @return
     * @throws ActionNotAllowedInStatus
     */
    public List<DeliveryProcessRepresentation> getDeliveriesForRequest(UUID uuid) throws ActionNotAllowedInStatus {
        Request request = requestRepository.findOneByUuid(uuid);
        checkStatus(request, RequestStatus.Delivery);
        return request.getDeliveryProcesses().stream()
            .map(deliveryProcessMapper::deliveryProcessToDeliveryProcessRepresentation)
            .collect(Collectors.toList());
    }

    /**
     * Start delivery processes for each of the selected request types for a request.
     * @param user the current user.
     * @param uuid the uuid of the request.
     * @return the list of generated delivery process instances.
     * @throws ActionNotAllowedInStatus iff the request is not in status Approved.
     */
    public List<DeliveryProcessRepresentation> startDelivery(AuthenticatedUser user, UUID uuid) throws ActionNotAllowedInStatus {
        Request request = requestRepository.findOneByUuid(uuid);
        checkStatus(request, RequestStatus.Approved);
        List<DeliveryProcessRepresentation> deliveryProcesses = new ArrayList<>();
        for(RequestType type: request.getRequestDetail().getRequestType()) {
            DeliveryProcess deliveryProcess = deliveryProcessService.start(user, type);
            request.addDeliveryProcess(deliveryProcess);
            publishDeliveryStatusUpdate(user, RequestStatus.Approved, request, deliveryProcess, null);
            deliveryProcesses.add(deliveryProcessMapper.deliveryProcessToDeliveryProcessRepresentation(deliveryProcess));
        }
        request.setStatus(RequestStatus.Delivery);
        request = requestRepository.save(request);
        requestService.publishStatusUpdate(user, RequestStatus.Approved, request, null);
        return deliveryProcesses;
    }

    /**
     *
     * @param user
     * @param requestUuid
     * @param deliveryProcessUuid
     * @param reference
     * @return
     * @throws ActionNotAllowedInStatus
     */
    public DeliveryProcessRepresentation release(AuthenticatedUser user, UUID requestUuid, UUID deliveryProcessUuid, DeliveryReferenceRepresentation reference) throws ActionNotAllowedInStatus {
        Request request = requestRepository.findOneByUuid(requestUuid);
        DeliveryProcess deliveryProcess = getDeliveryProcess(request, deliveryProcessUuid);
        checkDeliveryStatus(deliveryProcess, DeliveryStatus.Preparation);
        deliveryProcess = deliveryProcessService.release(user, deliveryProcess);
        deliveryProcess.setReference(reference.getReference());
        requestRepository.save(request);
        publishDeliveryStatusUpdate(user, DeliveryStatus.Preparation, request, deliveryProcess, null);
        return deliveryProcessMapper.deliveryProcessToDeliveryProcessRepresentation(deliveryProcess);
    }

    /**
     *
     * @param user
     * @param requestUuid
     * @param deliveryProcessUuid
     * @param message
     * @return
     * @throws ActionNotAllowedInStatus
     */
    public DeliveryProcessRepresentation reject(AuthenticatedUser user, UUID requestUuid, UUID deliveryProcessUuid, MessageRepresentation message) throws ActionNotAllowedInStatus {
        Request request = requestRepository.findOneByUuid(requestUuid);
        DeliveryProcess deliveryProcess = getDeliveryProcess(request, deliveryProcessUuid);
        checkDeliveryStatus(deliveryProcess, DeliveryStatus.Preparation);
        deliveryProcess = deliveryProcessService.reject(user, deliveryProcess);
        publishDeliveryStatusUpdate(user, DeliveryStatus.Preparation, request, deliveryProcess, message);
        return deliveryProcessMapper.deliveryProcessToDeliveryProcessRepresentation(deliveryProcess);
    }

    /**
     *
     * @param user
     * @param requestUuid
     * @param deliveryProcessUuid
     * @return
     * @throws ActionNotAllowedInStatus
     */
    public DeliveryProcessRepresentation received(AuthenticatedUser user, UUID requestUuid, UUID deliveryProcessUuid) throws ActionNotAllowedInStatus {
        Request request = requestRepository.findOneByUuid(requestUuid);
        DeliveryProcess deliveryProcess = getDeliveryProcess(request, deliveryProcessUuid);
        checkDeliveryStatus(deliveryProcess, DeliveryStatus.Released);
        deliveryProcess = deliveryProcessService.received(user, deliveryProcess);
        publishDeliveryStatusUpdate(user, DeliveryStatus.Released, request, deliveryProcess, null);
        return deliveryProcessMapper.deliveryProcessToDeliveryProcessRepresentation(deliveryProcess);
    }

    /**
     *
     * @param user
     * @param requestUuid
     * @param deliveryProcessUuid
     * @param message
     * @return
     * @throws ActionNotAllowedInStatus
     */
    public DeliveryProcessRepresentation cancel(AuthenticatedUser user, UUID requestUuid, UUID deliveryProcessUuid, MessageRepresentation message) throws ActionNotAllowedInStatus {
        Request request = requestRepository.findOneByUuid(requestUuid);
        DeliveryProcess deliveryProcess = getDeliveryProcess(request, deliveryProcessUuid);
        checkDeliveryStatus(deliveryProcess, DeliveryStatus.Released);
        deliveryProcess = deliveryProcessService.cancel(user, deliveryProcess);
        publishDeliveryStatusUpdate(user, DeliveryStatus.Released, request, deliveryProcess, message);
        return deliveryProcessMapper.deliveryProcessToDeliveryProcessRepresentation(deliveryProcess);
    }

}
