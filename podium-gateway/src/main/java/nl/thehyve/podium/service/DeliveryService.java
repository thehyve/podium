/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.service;

import com.codahale.metrics.annotation.Timed;
import nl.thehyve.podium.common.enumeration.DeliveryProcessOutcome;
import nl.thehyve.podium.common.enumeration.DeliveryStatus;
import nl.thehyve.podium.common.enumeration.RequestStatus;
import nl.thehyve.podium.common.enumeration.RequestType;
import nl.thehyve.podium.common.enumeration.Status;
import nl.thehyve.podium.common.event.StatusUpdateEvent;
import nl.thehyve.podium.common.exceptions.ActionNotAllowed;
import nl.thehyve.podium.common.exceptions.ResourceNotFound;
import nl.thehyve.podium.common.security.AuthenticatedUser;
import nl.thehyve.podium.common.service.dto.DeliveryProcessRepresentation;
import nl.thehyve.podium.common.service.dto.DeliveryReferenceRepresentation;
import nl.thehyve.podium.common.service.dto.MessageRepresentation;
import nl.thehyve.podium.domain.DeliveryProcess;
import nl.thehyve.podium.domain.PodiumEvent;
import nl.thehyve.podium.domain.Request;
import nl.thehyve.podium.repository.RequestRepository;
import nl.thehyve.podium.service.mapper.DeliveryProcessMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
    private NotificationService notificationService;

    @Autowired
    private DeliveryProcessMapper deliveryProcessMapper;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private EntityManager entityManager;


    @PostConstruct
    private void init() {
        notificationService.setDeliveryService(this);
    }


    @Transactional
    private void persistAndPublishDeliveryEvent(DeliveryProcess deliveryProcess, StatusUpdateEvent event) {
        PodiumEvent historicEvent = new PodiumEvent(event);
        entityManager.persist(historicEvent);
        deliveryProcess.addHistoricEvent(historicEvent);
        entityManager.persist(deliveryProcess);
        log.info("About to publish delivery event: {}", event);
        publisher.publishEvent(event);
    }

    private void publishDeliveryStatusUpdate(AuthenticatedUser user, DeliveryStatus sourceStatus, Request request, DeliveryProcess deliveryProcess, MessageRepresentation message) {
        StatusUpdateEvent event =
            new StatusUpdateEvent<>(user, sourceStatus, deliveryProcess.getStatus(), request.getUuid(), deliveryProcess.getUuid(), message);
        persistAndPublishDeliveryEvent(deliveryProcess, event);
    }

    /**
     * Checks if the request has any of the allowed statuses.
     * @param request the request object.
     * @param allowedStatuses the allowed statuses.
     * @return the current status iff the request has any of the allowed statuses.
     * @throws ActionNotAllowed iff the request does not have any of the allowed statuses.
     */
    private static RequestStatus checkStatus(Request request, RequestStatus ... allowedStatuses) throws ActionNotAllowed {
        if (!Status.isCurrentStatusAllowed(request.getStatus(), allowedStatuses)) {
            throw ActionNotAllowed.forStatus(request.getStatus());
        }
        return request.getStatus();
    }

    /**
     * Checks if the delivery process has any of the allowed statuses.
     * @param deliveryProcess the delivery process object.
     * @param allowedStatuses the allowed statuses.
     * @return the current status iff the delivery process has any of the allowed statuses.
     * @throws ActionNotAllowed iff the delivery process does not have any of the allowed statuses.
     */
    private static DeliveryStatus checkDeliveryStatus(DeliveryProcess deliveryProcess, DeliveryStatus ... allowedStatuses) throws ActionNotAllowed {
        if (!Status.isCurrentStatusAllowed(deliveryProcess.getStatus(), allowedStatuses)) {
            throw ActionNotAllowed.forStatus(deliveryProcess.getStatus());
        }
        return deliveryProcess.getStatus();
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
     * Gets all delivery processes for a request.
     * @param requestUuid the uuid of the request.
     * @return a list of representations of the delivery processes belonging to the request.
     */
    public List<DeliveryProcessRepresentation> getDeliveriesForRequest(UUID requestUuid) throws ActionNotAllowed {
        Request request = requestRepository.findOneByUuid(requestUuid);
        return request.getDeliveryProcesses().stream()
            .map(deliveryProcessMapper::deliveryProcessToDeliveryProcessRepresentation)
            .collect(Collectors.toList());
    }

    /**
     * Gets the delivery processes by uuid belonging to a request by uuid.
     * @param requestUuid the uuid of the request.
     * @param deliveryProcessUuid the uuid of the delivery process.
     * @return a representation of the delivery process.
     */
    public DeliveryProcessRepresentation getDeliveryForRequestByUuid(UUID requestUuid, UUID deliveryProcessUuid) {
        Request request = requestRepository.findOneByUuid(requestUuid);
        DeliveryProcess deliveryProcess = getDeliveryProcess(request, deliveryProcessUuid);
        return deliveryProcessMapper.deliveryProcessToDeliveryProcessRepresentation(deliveryProcess);
    }

    /**
     * Start delivery processes for each of the selected request types for a request.
     * Publishes a status update event for each of the started processes and one for the request.
     * @param user the current user.
     * @param uuid the uuid of the request.
     * @return the list of generated delivery process instances.
     * @throws ActionNotAllowed iff the request is not in status Approved.
     */
    public List<DeliveryProcessRepresentation> startDelivery(AuthenticatedUser user, UUID uuid) throws ActionNotAllowed {
        Request request = requestRepository.findOneByUuid(uuid);
        RequestStatus sourceStatus = checkStatus(request, RequestStatus.Approved);
        List<DeliveryProcessRepresentation> deliveryProcesses = new ArrayList<>();
        for(RequestType type: request.getRequestDetail().getRequestType()) {
            DeliveryProcess deliveryProcess = deliveryProcessService.start(user, type);
            request.addDeliveryProcess(deliveryProcess);
            publishDeliveryStatusUpdate(user, DeliveryStatus.None, request, deliveryProcess, null);
            deliveryProcesses.add(deliveryProcessMapper.deliveryProcessToDeliveryProcessRepresentation(deliveryProcess));
        }
        request.setStatus(RequestStatus.Delivery);
        request = requestRepository.save(request);
        requestService.publishStatusUpdate(user, sourceStatus, request, null);
        return deliveryProcesses;
    }

    /**
     * Marks the delivery process as released: the status of the process
     * is updated to {@link DeliveryStatus#Released}.
     * The delivery reference is stored in the delivery process object.
     * Publishes a status update event for the process.
     * @param user the current user.
     * @param requestUuid the uuid of the request the delivery process belongs to.
     * @param deliveryProcessUuid the uuid of the delivery process.
     * @param reference the delivery reference (e.g., URL or track and trace code).
     * @return the representation of the updated delivery process.
     * @throws ActionNotAllowed iff the delivery process is not in status {@link DeliveryStatus#Preparation}.
     */
    public DeliveryProcessRepresentation release(AuthenticatedUser user, UUID requestUuid, UUID deliveryProcessUuid, DeliveryReferenceRepresentation reference) throws ActionNotAllowed {
        Request request = requestRepository.findOneByUuid(requestUuid);
        DeliveryProcess deliveryProcess = getDeliveryProcess(request, deliveryProcessUuid);
        DeliveryStatus sourceStatus = checkDeliveryStatus(deliveryProcess, DeliveryStatus.Preparation);
        deliveryProcess = deliveryProcessService.release(user, deliveryProcess);
        deliveryProcess.setReference(reference.getReference());
        requestRepository.save(request);
        publishDeliveryStatusUpdate(user, sourceStatus, request, deliveryProcess, null);
        return deliveryProcessMapper.deliveryProcessToDeliveryProcessRepresentation(deliveryProcess);
    }

    /**
     * Marks the delivery process as received: the status of the process
     * is updated to {@link DeliveryStatus#Closed}, the outcome is set to {@link DeliveryProcessOutcome#Received}.
     * Publishes a status update event for the process.
     * @param user the current user.
     * @param requestUuid the uuid of the request the delivery process belongs to.
     * @param deliveryProcessUuid the uuid of the delivery process.
     * @return the representation of the updated delivery process.
     * @throws ActionNotAllowed iff the delivery process is not in status {@link DeliveryStatus#Released}.
     */
    public DeliveryProcessRepresentation received(AuthenticatedUser user, UUID requestUuid, UUID deliveryProcessUuid) throws ActionNotAllowed {
        Request request = requestRepository.findOneByUuid(requestUuid);
        DeliveryProcess deliveryProcess = getDeliveryProcess(request, deliveryProcessUuid);
        DeliveryStatus sourceStatus = checkDeliveryStatus(deliveryProcess, DeliveryStatus.Released);
        deliveryProcess = deliveryProcessService.received(user, deliveryProcess);
        publishDeliveryStatusUpdate(user, sourceStatus, request, deliveryProcess, null);
        return deliveryProcessMapper.deliveryProcessToDeliveryProcessRepresentation(deliveryProcess);
    }

    /**
     * Marks the delivery process as cancelled: the status of the process
     * is updated to {@link DeliveryStatus#Closed}, the outcome is set to {@link DeliveryProcessOutcome#Cancelled}.
     * Publishes a status update event for the process.
     * @param user the current user.
     * @param requestUuid the uuid of the request the delivery process belongs to.
     * @param deliveryProcessUuid the uuid of the delivery process.
     * @return the representation of the updated delivery process.
     * @throws ActionNotAllowed iff the delivery process is not in status {@link DeliveryStatus#Preparation} or {@link DeliveryStatus#Released}.
     */
    public DeliveryProcessRepresentation cancel(AuthenticatedUser user, UUID requestUuid, UUID deliveryProcessUuid, MessageRepresentation message) throws ActionNotAllowed {
        Request request = requestRepository.findOneByUuid(requestUuid);
        DeliveryProcess deliveryProcess = getDeliveryProcess(request, deliveryProcessUuid);
        DeliveryStatus sourceStatus = checkDeliveryStatus(deliveryProcess, DeliveryStatus.Preparation, DeliveryStatus.Released);
        deliveryProcess = deliveryProcessService.cancel(user, deliveryProcess);
        publishDeliveryStatusUpdate(user, sourceStatus, request, deliveryProcess, message);
        return deliveryProcessMapper.deliveryProcessToDeliveryProcessRepresentation(deliveryProcess);
    }

}
