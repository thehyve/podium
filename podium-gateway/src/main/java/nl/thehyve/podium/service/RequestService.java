/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.service;

import com.codahale.metrics.annotation.Timed;
import nl.thehyve.podium.common.IdentifiableUser;
import nl.thehyve.podium.common.enumeration.RequestReviewStatus;
import nl.thehyve.podium.common.enumeration.RequestStatus;
import nl.thehyve.podium.common.event.EventType;
import nl.thehyve.podium.common.exceptions.AccessDenied;
import nl.thehyve.podium.common.exceptions.ActionNotAllowedInStatus;
import nl.thehyve.podium.common.exceptions.InvalidRequest;
import nl.thehyve.podium.common.exceptions.ResourceNotFound;
import nl.thehyve.podium.common.exceptions.ServiceNotAvailable;
import nl.thehyve.podium.common.security.AuthenticatedUser;
import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.common.service.dto.OrganisationDTO;
import nl.thehyve.podium.common.service.dto.UserRepresentation;
import nl.thehyve.podium.domain.PodiumEvent;
import nl.thehyve.podium.domain.PrincipalInvestigator;
import nl.thehyve.podium.domain.Request;
import nl.thehyve.podium.domain.RequestDetail;
import nl.thehyve.podium.common.event.StatusUpdateEvent;
import nl.thehyve.podium.repository.RequestRepository;
import nl.thehyve.podium.repository.search.RequestSearchRepository;
import nl.thehyve.podium.service.mapper.RequestDetailMapper;
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
import java.util.stream.Collectors;

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
    private RequestDetailMapper requestDetailMapper;

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
     *  Get all the requests to organisations for which the current user is a coordinator.
     *
     *  @param user the current user (the coordinator)
     *  @param status the status to filter on
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<RequestRepresentation> findAllCoordinatorRequestsInStatus(AuthenticatedUser user,
                                                             RequestStatus status,
                                                             Pageable pageable) {
        log.debug("Request to get all organisation requests for a coordinator");
        Set<UUID> organisationUuids = user.getOrganisationAuthorities().entrySet().stream()
            .filter(entry -> entry.getValue().contains(AuthorityConstants.ORGANISATION_COORDINATOR))
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
        Page<Request> result = requestRepository.findAllByStatusAndOrganisations(status, organisationUuids, pageable);
        return result.map(requestMapper::requestToRequestDTO);
    }

    /**
     *  Get all the requests to the organisation for which the current user is a coordinator.
     *
     *  @param user the current user (the coordinator)
     *  @param status the status to filter on
     *  @param organisationUuid the uuid of the organisation for which to fetch the requests
     *  @param pageable the pagination information
     *  @return the list of entities
     *  @throws AccessDenied iff the user is not a coordinator for the organisation with uuid organisationUuid.
     */
    @Transactional(readOnly = true)
    public Page<RequestRepresentation> findCoordinatorRequestsForOrganisationInStatus(AuthenticatedUser user,
                                                                       RequestStatus status,
                                                                       UUID organisationUuid,
                                                                       Pageable pageable) {
        log.debug("Request to get all organisation requests for an organisation for a coordinator");
        if (!user.getOrganisationAuthorities().containsKey(organisationUuid) ||
            !user.getOrganisationAuthorities().get(organisationUuid).contains(AuthorityConstants.ORGANISATION_COORDINATOR)) {
            throw new AccessDenied("Access denied to requests of organisation " + organisationUuid.toString());
        }
        Page<Request> result = requestRepository.findAllByStatusAndOrganisations(status, Collections.singleton(organisationUuid), pageable);
        return result.map(requestMapper::requestToRequestDTO);
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
     *  @throws ResourceNotFound when the requested request could not be found.
     */
    @Transactional(readOnly = true)
    public RequestRepresentation findRequest(UUID requestUuid) {
        log.debug("Request to get Request with uuid {}", requestUuid);
        Request request = requestRepository.findOneByUuid(requestUuid);
        if (request == null) {
            throw new ResourceNotFound("Request not found.");
        }
        return requestMapper.extendedRequestToRequestDTO(request);
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
     * The request to update is fetched based on the uuid in the body.
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

    /**
     * Updates the request with the properties in the body.
     * The request to update is fetched based on the uuid in the body.
     * Only allowed when a request has the review status Revision.
     *
     * @param user the current user
     * @param body the updated properties.
     * @return the updated request
     * @throws ActionNotAllowedInStatus if the request is not in review status 'Revision'.
     */
    @Transactional
    @Timed
    public RequestRepresentation updateRequest(IdentifiableUser user, RequestRepresentation body) throws ActionNotAllowedInStatus {
        Request request = requestRepository.findOneByUuid(body.getUuid());

        if (!this.isInRevision(request)) {
            throw ActionNotAllowedInStatus.forStatus(request.getStatus());
        }

        // FIXME: [AOP] Only requester should be able to perform an update to the request.
        if (!request.getRequester().equals(user.getUserUuid())) {
            throw new AccessDenied("Access denied to request " + request.getUuid().toString());
        }

        requestDetailMapper.processingRequestDetailDtoToRequestDetail(body.getRevisionDetail(), request.getRevisionDetail());

        request = save(request);
        return requestMapper.extendedRequestToRequestDTO(request);
    }

    /**
     * Submit the request by uuid.
     *
     * @param user the current user, submitting the request
     * @param uuid the uuid of the request
     * @return the updated request
     * @throws ActionNotAllowedInStatus if the request is not in status 'Revision'.
     */
    @Timed
    public RequestRepresentation submitRequest(AuthenticatedUser user, UUID uuid) throws ActionNotAllowedInStatus {
        Request request = requestRepository.findOneByUuid(uuid);

        // Is the request currently in Revision
        if (!this.isInRevision(request)) {
            throw ActionNotAllowedInStatus.forStatus(request.getStatus());
        }

        // Is the current user the owner of the request
        if (!request.getRequester().equals(user.getUserUuid())) {
            throw new AccessDenied("Access denied to request.");
        }

        // Update the request details with the updated revision details
        request.setRequestDetail(request.getRevisionDetail());
        save(request);

        // Submit the request for validation by the organisation coordinator
        requestReviewProcessService.submitForValidation(user, request.getRequestReviewProcess());

        RequestRepresentation requestRepresentation = requestMapper.extendedRequestToRequestDTO(request);

        // Send emails to all coordinators belonging to this organisation
        log.debug("Sending revision submission notification emails to all coordinators of {}",
            requestRepresentation.getOrganisations().get(0).getName());

        // Send notification to all coordinators
        for (UUID organisationUuid: request.getOrganisations()) {
            // Fetch organisation through Feign.
            OrganisationDTO organisation;

            try {
                organisation = organisationClientService.findOrganisationByUuid(organisationUuid);
            } catch (Exception e) {
                log.error("Error fetching organisation", e);
                throw new ServiceNotAvailable("Could not fetch organisation through feign", e);
            }

            notificationService.revisionNotificationToCoordinators(organisation, request);
        }

        return requestRepresentation;
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

    @Transactional
    @Timed
    public RequestRepresentation validateRequest(AuthenticatedUser user, UUID uuid) throws ActionNotAllowedInStatus {
        // TODO: Add NotificationEvent
        Request request = requestRepository.findOneByUuid(uuid);

        if(!hasAccessToRequestAsCoordinator(request, user)) {
            throw new AccessDenied("Access denied to request.");
        }

        // Move the request to Review by organisation reviewers
        requestReviewProcessService.submitForReview(user, request.getRequestReviewProcess());

        // Send notification to all reviewers of organisation
        for (UUID organisationUuid: request.getOrganisations()) {
            // Fetch organisation through Feign.
            OrganisationDTO organisation;

            // FIXME: Possibly do this using a mapstruct mapper
            try {
                organisation = organisationClientService.findOrganisationByUuid(organisationUuid);
            } catch (Exception e) {
                log.error("Error fetching organisation", e);
                throw new ServiceNotAvailable("Could not fetch organisation through feign", e);
            }

            notificationService.reviewNotificationToReviewers(organisation, request);
        }

        return requestMapper.requestToRequestDTO(request);
    }

    @Transactional
    @Timed
    public RequestRepresentation rejectRequest(AuthenticatedUser user, UUID uuid) throws ActionNotAllowedInStatus {
        // TODO: Add NotificationEvent
        Request request = requestRepository.findOneByUuid(uuid);

        if(!hasAccessToRequestAsCoordinator(request, user)) {
            throw new AccessDenied("Access denied to request.");
        }

        requestReviewProcessService.reject(user, request.getRequestReviewProcess());

        RequestRepresentation requestRepresentation = requestMapper.extendedRequestToRequestDTO(request);

        // Send rejection email
        notificationService.rejectionNotificationToRequester(user, requestRepresentation);
        return requestRepresentation;
    }

    @Transactional
    @Timed
    public RequestRepresentation approveRequest(AuthenticatedUser user, UUID uuid) throws ActionNotAllowedInStatus {
        // TODO: Add NotificationEvent
        Request request = requestRepository.findOneByUuid(uuid);

        if(!hasAccessToRequestAsCoordinator(request, user)) {
            throw new AccessDenied("Access denied to request.");
        }

        requestReviewProcessService.approve(user, request.getRequestReviewProcess());

        RequestRepresentation requestRepresentation = requestMapper.extendedRequestToRequestDTO(request);

        // Send approval email
        notificationService.approvalNotificationToRequester(user, requestRepresentation);
        return requestRepresentation;
    }

    @Transactional
    @Timed
    public RequestRepresentation reviseRequest(AuthenticatedUser user, UUID uuid) throws ActionNotAllowedInStatus {
        // TODO: Add NotificationEvent
        Request request = requestRepository.findOneByUuid(uuid);

        if(!hasAccessToRequestAsCoordinator(request, user)) {
            throw new AccessDenied("Access denied to request.");
        }

        requestReviewProcessService.requestRevision(user, request.getRequestReviewProcess());
        return requestMapper.requestToRequestDTO(request);
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
            // Fetch organisation through Feign.
            OrganisationDTO organisation;

            try {
                organisation = organisationClientService.findOrganisationByUuid(organisationUuid);
            } catch (Exception e) {
                log.error("Error fetching organisation", e);
                throw new ServiceNotAvailable("Could not fetch organisation through feign", e);
            }

            // TODO: validate request type of the request with the supported request types of the organisation.
            Request organisationRequest = requestMapper.clone(request);

            // Create organisation revision details
            RequestDetail revisionDetail = requestDetailMapper.clone(request.getRequestDetail());
            organisationRequest.setRevisionDetail(revisionDetail);

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
        return requestMapper.extendedRequestsToRequestDTOs(organisationRequests);
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
        return result.map(requestMapper::requestToRequestDTO);
    }

    public List<UserRepresentation> getCoordinatorsForRequest(Request request) {
        List<UserRepresentation> coordinators = new ArrayList<>();
        for (UUID organisationUuid: request.getOrganisations()) {
            // Fetch organisation coordinators through Feign.
            try {
                coordinators = organisationClientService.findUsersByRole(organisationUuid,
                    AuthorityConstants.ORGANISATION_COORDINATOR);
            } catch (Exception e) {
                log.error("Error fetching organisation coordinators", e);
                throw new ServiceNotAvailable("Could not fetch organisation coordinators through feign", e);
            }

        }
        return coordinators;
    }

    public boolean hasAccessToRequestAsCoordinator(Request request, AuthenticatedUser user) {
        // Fetch the coordinators of the request through Feign
        List<UserRepresentation> coordinators = getCoordinatorsForRequest(request);

        // Check whether the authenticated user is a coordinator of one of the associated organisations
        // If no coordinator is found, throw AccessDenied Exception
        Optional<UserRepresentation> coordinator = coordinators.stream()
            .filter(u -> u.getUuid().equals(user.getUuid()))
            .findAny();

        return coordinator.isPresent();
    }

    private boolean isInRevision(Request request) {
        RequestReviewStatus requestReviewStatus = request.getRequestReviewProcess().getStatus();

        if (request.getStatus() != RequestStatus.Review && requestReviewStatus != RequestReviewStatus.Revision) {
            log.debug("Not allowed to update request as it holds the wrong statuses {} - {}", request.getStatus(), requestReviewStatus);
            return false;
        }
        return true;
    }

}
