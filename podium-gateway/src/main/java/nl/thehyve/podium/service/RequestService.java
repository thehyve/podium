/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.service;

import com.codahale.metrics.annotation.Timed;
import nl.thehyve.podium.common.IdentifiableUser;
import nl.thehyve.podium.common.enumeration.RequestStatus;
import nl.thehyve.podium.common.event.EventType;
import nl.thehyve.podium.common.exceptions.AccessDenied;
import nl.thehyve.podium.common.exceptions.ActionNotAllowedInStatus;
import nl.thehyve.podium.common.exceptions.InvalidRequest;
import nl.thehyve.podium.common.exceptions.ResourceNotFound;
import nl.thehyve.podium.common.exceptions.ServiceNotAvailable;
import nl.thehyve.podium.common.security.AuthenticatedUser;
import nl.thehyve.podium.common.service.dto.OrganisationDTO;
import nl.thehyve.podium.domain.PodiumEvent;
import nl.thehyve.podium.domain.PrincipalInvestigator;
import nl.thehyve.podium.domain.Request;
import nl.thehyve.podium.domain.RequestDetail;
import nl.thehyve.podium.common.event.StatusUpdateEvent;
import nl.thehyve.podium.repository.RequestRepository;
import nl.thehyve.podium.repository.search.RequestSearchRepository;
import nl.thehyve.podium.service.mapper.RequestMapper;
import nl.thehyve.podium.service.representation.RequestRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.*;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

/**
 * Service Implementation for managing Request.
 */
@Service
@Transactional
public class RequestService {

    private final Logger log = LoggerFactory.getLogger(RequestService.class);

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private RequestMapper requestMapper;

    @Autowired
    private RequestSearchRepository requestSearchRepository;

    @Autowired
    private RequestReviewProcessService requestReviewProcessService;

    @Autowired
    private OrganisationClientService organisationClientService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private EntityManager entityManager;

    public RequestService() {}

    private PodiumEvent convert(StatusUpdateEvent event) {
        PodiumEvent podiumEvent = new PodiumEvent();
        podiumEvent.setPrincipal(event.getUsername());
        podiumEvent.setEventType(EventType.Status_Change);
        podiumEvent.setEventDate(event.getEventDate());
        Map<String,String> data = new HashMap<>();
        data.put("requestUuid", event.getRequestUuid().toString());
        data.put("sourceStatus", event.getSourceStatus().toString());
        data.put("targetStatus", event.getTargetStatus().toString());
        data.put("message", event.getMessage());
        podiumEvent.setData(data);
        return podiumEvent;
    }

    private void publishStatusUpdate(AuthenticatedUser user, RequestStatus sourceStatus, Request request, String message) {
        StatusUpdateEvent event =
            new StatusUpdateEvent(user, sourceStatus, request.getStatus(), request.getUuid(), message);
        PodiumEvent historicEvent = convert(event);
        entityManager.persist(historicEvent);
        request.addHistoricEvent(historicEvent);
        entityManager.persist(request);
        publisher.publishEvent(event);
    }

    /**
     * Save a request.
     *
     * @param request the entity to save
     * @return the persisted entity
     */
    @Transactional
    public Request save(Request request) {
        return requestRepository.save(request);
    }

    /**
     * Save request draft
     * @param requestRepresentation request representation
     * @return saved request representation
     */
    @Transactional
    public RequestRepresentation saveDraft(RequestRepresentation requestRepresentation) {
        log.debug("Save request draft with request id : {}", requestRepresentation.getId());
        Request request =  requestRepository.findOne(requestRepresentation.getId());
        Request updatedRequest = null;
        if (request != null) {
            updatedRequest = requestMapper.updateRequestDTOToRequest(requestRepresentation, request);
            save(updatedRequest);
        }
        return requestMapper.requestToRequestDTO(updatedRequest);
    }

    /**
     *  Get all the requests.
     *
     *  @param requester the current user (the requester)
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<RequestRepresentation> findAllForRequester(IdentifiableUser requester, Pageable pageable) {
        log.debug("Request to get all Requests");
        Page<Request> result = requestRepository.findAllByRequester(requester.getUserUuid(), pageable);
        return result.map(requestMapper::extendedRequestToRequestDTO);
    }

    /**
     *  Get the request for the requester
     *
     *  @param requester the current user (the requester)
     *  @param requestUuid the uuid of the request
     *  @return the entity
     *  @throws AccessDenied iff the user is not the requester of the request.
     */
    @Transactional(readOnly = true)
    public RequestRepresentation findRequestForRequester(IdentifiableUser requester, UUID requestUuid) {
        log.debug("Request to get Request with uuid {}", requestUuid);
        Request request = requestRepository.findOneByUuid(requestUuid);
        if (request == null) {
            throw new ResourceNotFound("Request not found.");
        }
        if (!request.getRequester().equals(requester.getUserUuid())) {
            throw new AccessDenied("Access denied to request " + request.getUuid().toString());
        }
        return requestMapper.requestToRequestDTO(request);
    }

    /**
     *  Get the request
     *
     *  @param requestUuid the uuid of the request
     *  @return the entity
     *  @throws AccessDenied iff the user is not the requester of the request.
     */
    @Transactional(readOnly = true)
    public RequestRepresentation findRequest(UUID requestUuid) {
        log.debug("Request to get Request with uuid {}", requestUuid);
        Request request = requestRepository.findOneByUuid(requestUuid);
        if (request == null) {
            throw new ResourceNotFound("Request not found.");
        }
        return requestMapper.requestToRequestDTO(request);
    }

    /**
     * Perform look up of request drafts by requester.
     *
     * @param requester The user to perform the lookup for
     * @param status the request status to filter on.
     * @param pageable the pagination object
     * @return the transformed DTO list of requests
     */
    @Transactional(readOnly = true)
    public Page<RequestRepresentation> findAllRequestsForRequesterByStatus(IdentifiableUser requester, RequestStatus status, Pageable pageable) {
        Page<Request> result = requestRepository.findAllByRequesterAndStatus(
            requester.getUserUuid(), status, pageable);
        return result.map(requestMapper::extendedRequestToRequestDTO);
    }

    /**
     * Get one request by id.
     *
     * @param id request id
     * @return request representation
     */
    @Transactional(readOnly = true)
    public RequestRepresentation findOne(Long id) {
        log.debug("Request to get a requestDetail : {}", id);
        Request request = requestRepository.findOne(id);
        return requestMapper.requestToRequestDTO(request);
    }

    /**
     * Create a new draft request.
     *
     * @param user the current user (the requester).
     * @return saved request representation
     */
    @Transactional
    public RequestRepresentation createDraft(IdentifiableUser user) {
        Request request = new Request();
        request.setStatus(RequestStatus.Draft);
        request.setRequester(user.getUserUuid());
        RequestDetail requestDetail = new RequestDetail();
        requestDetail.setPrincipalInvestigator(new PrincipalInvestigator());
        request.setRequestDetail(requestDetail);
        save(request);
        return requestMapper.requestToRequestDTO(request);
    }

    /**
     * Updates the draft request with the properties in the body.
     * The request to update is fetched based on the id in the body.
     *
     * @param user the current user
     * @param body the updated properties.
     * @return the updated draft request
     * @throws ActionNotAllowedInStatus if the request is not in status 'Draft'.
     */
    @Timed
    public RequestRepresentation updateDraft(IdentifiableUser user, RequestRepresentation body) throws ActionNotAllowedInStatus {
        Request request = requestRepository.findOneByUuid(body.getUuid());
        if (request.getStatus() != RequestStatus.Draft) {
            throw ActionNotAllowedInStatus.forStatus(request.getStatus());
        }
        if (!request.getRequester().equals(user.getUserUuid())) {
            throw new AccessDenied("Access denied to request " + request.getUuid().toString());
        }
        request = requestMapper.updateRequestDTOToRequest(body, request);
        save(request);
        return requestMapper.requestToRequestDTO(request);
    }

    private void deleteRequest(Long id) {
        requestRepository.delete(id);
        requestSearchRepository.delete(id);
    }

    /**
     *  Delete the request by uuid.
     *
     *  @param user the current user
     *  @param uuid the uuid of the request
     *  @throws ActionNotAllowedInStatus if the request is not in status 'Draft'.
     */
    @Transactional
    @Timed
    public void deleteDraft(IdentifiableUser user, UUID uuid) throws ActionNotAllowedInStatus {
        Request request = requestRepository.findOneByUuid(uuid);
        if (request.getStatus() != RequestStatus.Draft) {
            throw ActionNotAllowedInStatus.forStatus(request.getStatus());
        }
        if (!request.getRequester().equals(user.getUserUuid())) {
            throw new AccessDenied("Access denied to request " + uuid.toString());
        }
        log.debug("Request to delete Request : {}", uuid);
        deleteRequest(request.getId());
    }

    /**
     * Submit the draft request by uuid.
     * Generates requests for the organisations specified in the draft.
     *
     * @param user the current user, submitting the request
     * @param uuid the uuid of the draft request
     * @return the list of generated requests to organisations.
     * @throws ActionNotAllowedInStatus if the request is not in status 'Draft'.
     */
    @Timed
    public List<RequestRepresentation> submitDraft(AuthenticatedUser user, UUID uuid) throws ActionNotAllowedInStatus {
        Request request = requestRepository.findOneByUuid(uuid);
        if (request.getStatus() != RequestStatus.Draft) {
            throw ActionNotAllowedInStatus.forStatus(request.getStatus());
        }
        if (!request.getRequester().equals(user.getUserUuid())) {
            throw new AccessDenied("Access denied to request.");
        }


        RequestRepresentation requestData = requestMapper.requestToRequestDTO(request);
        log.debug("Validating request data.");
        {
            ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
            Validator validator = factory.getValidator();

            Set<ConstraintViolation<RequestRepresentation>> requestConstraintViolations = validator.validate(requestData);
            if (!requestConstraintViolations.isEmpty()) {
                throw new InvalidRequest("Invalid request", requestConstraintViolations);
            }
        }

        log.debug("Submitting request : {}", uuid);

        List<Request> organisationRequests = new ArrayList<>();
        // TODO: Aggregate mails for multiple organisations per user.
        for (UUID organisationUuid: request.getOrganisations()) {
            // Fetch organisation and organisation coordinators through Feign.
            OrganisationDTO organisation;
            try {
                organisation = organisationClientService.findOrganisationByUuid(organisationUuid);
            } catch (Exception e) {
                log.error("Error fetching organisation and coordinators", e);
                throw new ServiceNotAvailable("Could not fetch organisation and coordinators", e);
            }

            // TODO: validate request type of the request with the supported request types of the organisation.

            Request organisationRequest = requestMapper.clone(request);
            organisationRequest.setOrganisations(
                new HashSet<>(Collections.singleton(organisationUuid)));
            organisationRequest.setStatus(RequestStatus.Review);
            organisationRequest.setRequestReviewProcess(
                requestReviewProcessService.start(user));
            organisationRequest = save(organisationRequest);

            notificationService.submissionNotificationToCoordinators(organisation, organisationRequest);

            publishStatusUpdate(user, RequestStatus.Draft, organisationRequest, null);

            organisationRequests.add(organisationRequest);
            log.debug("Created new submitted request for organisation {}.", organisationUuid);
        }

        notificationService.submissionNotificationToRequester(user, organisationRequests);

        log.debug("Deleting draft request.");
        deleteRequest(request.getId());
        return requestMapper.requestsToRequestDTOs(organisationRequests);
    }

    /**
     * Search for the request corresponding to the query.
     *
     *  @param query the query of the search
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<RequestRepresentation> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Requests for query {}", query);
        Page<Request> result = requestSearchRepository.search(queryStringQuery(query), pageable);
        return result.map(request -> requestMapper.requestToRequestDTO(request));
    }

}
