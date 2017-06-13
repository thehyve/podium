/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.service;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.Sets;
import nl.thehyve.podium.client.OrganisationClient;
import nl.thehyve.podium.common.IdentifiableUser;
import nl.thehyve.podium.common.enumeration.*;
import nl.thehyve.podium.common.exceptions.AccessDenied;
import nl.thehyve.podium.common.exceptions.ActionNotAllowed;
import nl.thehyve.podium.common.exceptions.InvalidRequest;
import nl.thehyve.podium.common.exceptions.ResourceNotFound;
import nl.thehyve.podium.common.exceptions.ServiceNotAvailable;
import nl.thehyve.podium.common.security.AuthenticatedUser;
import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.common.service.dto.MessageRepresentation;
import nl.thehyve.podium.common.service.dto.OrganisationDTO;
import nl.thehyve.podium.common.service.dto.ReviewFeedbackRepresentation;
import nl.thehyve.podium.common.service.dto.ReviewRoundRepresentation;
import nl.thehyve.podium.common.service.dto.UserRepresentation;
import nl.thehyve.podium.domain.PodiumEvent;
import nl.thehyve.podium.domain.PrincipalInvestigator;
import nl.thehyve.podium.domain.Request;
import nl.thehyve.podium.domain.RequestDetail;
import nl.thehyve.podium.common.event.StatusUpdateEvent;
import nl.thehyve.podium.domain.ReviewFeedback;
import nl.thehyve.podium.domain.ReviewRound;
import nl.thehyve.podium.repository.RequestRepository;
import nl.thehyve.podium.repository.ReviewFeedbackRepository;
import nl.thehyve.podium.repository.ReviewRoundRepository;
import nl.thehyve.podium.repository.search.RequestSearchRepository;
import nl.thehyve.podium.repository.search.ReviewFeedbackSearchRepository;
import nl.thehyve.podium.service.mapper.RequestDetailMapper;
import nl.thehyve.podium.service.mapper.RequestMapper;
import nl.thehyve.podium.common.service.dto.RequestRepresentation;
import nl.thehyve.podium.service.mapper.ReviewFeedbackMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
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
@Timed
public class RequestService {

    private final Logger log = LoggerFactory.getLogger(RequestService.class);

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private RequestMapper requestMapper;

    @Autowired
    private RequestDetailMapper requestDetailMapper;

    @Autowired
    private ReviewFeedbackMapper reviewFeedbackMapper;

    @Autowired
    private RequestSearchRepository requestSearchRepository;

    @Autowired
    private ReviewFeedbackRepository reviewFeedbackRepository;

    @Autowired
    private ReviewFeedbackSearchRepository reviewFeedbackSearchRepository;

    @Autowired
    private RequestReviewProcessService requestReviewProcessService;

    @Autowired
    private ReviewRoundService reviewRoundService;

    @Autowired
    private OrganisationClientService organisationClientService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private EntityManager entityManager;


    @PostConstruct
    private void init() {
        notificationService.setRequestService(this);
    }


    @Transactional
    private void persistAndPublishEvent(Request request, StatusUpdateEvent event) {
        PodiumEvent historicEvent = new PodiumEvent(event);
        entityManager.persist(historicEvent);
        request.addHistoricEvent(historicEvent);
        entityManager.persist(request);
        log.info("About to publish event: {}", event);
        publisher.publishEvent(event);
    }

    protected void publishStatusUpdate(AuthenticatedUser user, RequestStatus sourceStatus, Request request, MessageRepresentation message) {
        StatusUpdateEvent event =
            new StatusUpdateEvent<>(user, sourceStatus, request.getStatus(), request.getUuid(), message);
        persistAndPublishEvent(request, event);
    }

    protected void publishReviewStatusUpdate(AuthenticatedUser user, RequestReviewStatus sourceStatus, Request request, MessageRepresentation message) {
        StatusUpdateEvent event =
            new StatusUpdateEvent<>(user, sourceStatus, request.getRequestReviewProcess().getStatus(), request.getUuid(), message);
        persistAndPublishEvent(request, event);
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

    private Page<RequestRepresentation> findAllOrganisationRequestsInReviewStatusForRole(AuthenticatedUser user,
                                                                          RequestReviewStatus status,
                                                                          String authority,
                                                                          Pageable pageable) {
        Set<UUID> organisationUuids = user.getOrganisationAuthorities().entrySet().stream()
            .filter(entry -> entry.getValue().contains(authority))
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
        Page<Request> result = requestRepository.findAllByRequestReviewStatusAndOrganisations(status, organisationUuids, pageable);
        return result.map(requestMapper::extendedRequestToRequestDTO);
    }

    /**
     *  Get all the requests in review status 'Review' to organisations for which the current user is a reviewer.
     *
     *  @param user the current user (the coordinator)
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<RequestRepresentation> findAllForReviewer(AuthenticatedUser user, Pageable pageable) {
        log.debug("Request to get all organisation requests for a reviewer");
        return findAllOrganisationRequestsInReviewStatusForRole(user, RequestReviewStatus.Review, AuthorityConstants.REVIEWER, pageable);
    }

    private Page<RequestRepresentation> findAllOrganisationRequestsInStatusForRole(AuthenticatedUser user,
                                                                                   RequestStatus status,
                                                                                   String authority,
                                                                                   Pageable pageable) {
        Set<UUID> organisationUuids = user.getOrganisationAuthorities().entrySet().stream()
            .filter(entry -> entry.getValue().contains(authority))
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
        Page<Request> result = requestRepository.findAllByStatusAndOrganisations(status, organisationUuids, pageable);
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
    public Page<RequestRepresentation> findAllForCoordinatorInStatus(AuthenticatedUser user,
                                                                     RequestStatus status,
                                                                     Pageable pageable) {
        log.debug("Request to get all organisation requests for a coordinator");
        return findAllOrganisationRequestsInStatusForRole(user, status, AuthorityConstants.ORGANISATION_COORDINATOR, pageable);
    }

    /**
     * Checks if the user has the requested authority for at least one of the organisations with the specified
     * organisation uuids.
     * @param user the user object.
     * @param organisationUuids the collection of organisation uuids.
     * @param authority the requested authority.
     * @throws AccessDenied iff the user does not have the required access rights.
     */
    private void checkOrganisationAccess(AuthenticatedUser user, Collection<UUID> organisationUuids, String authority) {
        for (UUID organisationUuid: organisationUuids) {
            Collection<String> organisationAuthorities = user.getOrganisationAuthorities().get(organisationUuid);
            if (organisationAuthorities != null && organisationAuthorities.contains(authority)) {
                // the authority is present for one of the organisations
                return;
            }
        }
        throw new AccessDenied("Access denied for organisations " + Arrays.toString(organisationUuids.toArray()));
    }

    /**
     * Checks if the user has the requested authority for the organisation with the specified
     * organisation uuid.
     * @param user the user object.
     * @param organisationUuid the uuid of the organisation.
     * @param authority the requested authority.
     * @throws AccessDenied iff the user does not have the requested access.
     */
    private void checkOrganisationAccess(AuthenticatedUser user, UUID organisationUuid, String authority) {
        checkOrganisationAccess(user, Collections.singleton(organisationUuid), authority);
    }

    /**
     * Checks if the request has any of the allowed statuses
     * @param request the request object.
     * @param allowedStatuses the allowed statuses.
     * @throws ActionNotAllowed iff the request does not have any of the allowed statuses.
     */
    private static RequestStatus checkStatus(Request request, RequestStatus ... allowedStatuses) throws ActionNotAllowed {
        if (!Status.isCurrentStatusAllowed(request.getStatus(), allowedStatuses)) {
            throw ActionNotAllowed.forStatus(request.getStatus());
        }
        return request.getStatus();
    }

    /**
     * Checks if the request has one of the allowed review statuses.
     * @param request the request object.
     * @param allowedStatuses the allowed review statuses.
     * @throws ActionNotAllowed iff the request is not in a review status or does not have any of the
     * allowed review statuses.
     */
    private static RequestReviewStatus checkReviewStatus(Request request, RequestReviewStatus ... allowedStatuses) throws ActionNotAllowed {
        if (request.getStatus() != RequestStatus.Review) {
            throw ActionNotAllowed.forStatus(request.getStatus());
        }
        RequestReviewStatus currentReviewStatus = request.getRequestReviewProcess().getStatus();
        if (!Status.isCurrentStatusAllowed(currentReviewStatus, allowedStatuses)) {
            throw ActionNotAllowed.forStatus(currentReviewStatus);
        }
        return currentReviewStatus;
    }

    /**
     *  Get all the requests in review status 'Review' to the organisation for which the current user is a reviewer.
     *
     *  @param user the current user (the reviewer)
     *  @param organisationUuid the uuid of the organisation for which to fetch the requests
     *  @param pageable the pagination information
     *  @return the list of entities
     *  @throws AccessDenied iff the user is not a reviewer for the organisation with uuid organisationUuid.
     */
    @Transactional(readOnly = true)
    public Page<RequestRepresentation> findAllForReviewerByOrganisation(AuthenticatedUser user, UUID organisationUuid, Pageable pageable) {
        log.debug("Request to get all organisation requests for an organisation for a reviewer");
        checkOrganisationAccess(user, organisationUuid, AuthorityConstants.REVIEWER);
        Page<Request> result = requestRepository.findAllByRequestReviewStatusAndOrganisations(RequestReviewStatus.Review, Collections.singleton(organisationUuid), pageable);
        return result.map(requestMapper::extendedRequestToRequestDTO);
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
    public Page<RequestRepresentation> findAllForCoordinatorByOrganisationInStatus(AuthenticatedUser user,
                                                                                   RequestStatus status,
                                                                                   UUID organisationUuid,
                                                                                   Pageable pageable) {
        log.debug("Request to get all organisation requests for an organisation for a coordinator");
        checkOrganisationAccess(user, organisationUuid, AuthorityConstants.ORGANISATION_COORDINATOR);
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
     *  Get the request
     *
     *  @param requestUuid the uuid of the request
     *  @return the entity
     *  @throws ResourceNotFound when the requested request could not be found.
     */
    @Transactional(readOnly = true)
    public RequestRepresentation findRequestBasic(UUID requestUuid) {
        log.debug("Request to get Basic request with uuid {}", requestUuid);
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
     * @throws ActionNotAllowed if the request is not in status 'Draft'.
     */
    public RequestRepresentation updateDraft(IdentifiableUser user, RequestRepresentation body) throws ActionNotAllowed {
        Request request = requestRepository.findOneByUuid(body.getUuid());
        if (request.getStatus() != RequestStatus.Draft) {
            throw ActionNotAllowed.forStatus(request.getStatus());
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
     * @throws ActionNotAllowed if the request is not in review status 'Revision'.
     */
    public RequestRepresentation updateRequest(IdentifiableUser user, RequestRepresentation body) throws ActionNotAllowed {
        Request request = requestRepository.findOneByUuid(body.getUuid());

        checkReviewStatus(request, RequestReviewStatus.Revision);

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
     * @throws ActionNotAllowed if the request is not in status 'Revision'.
     */
    public RequestRepresentation submitRevision(AuthenticatedUser user, UUID uuid) throws ActionNotAllowed {
        Request request = requestRepository.findOneByUuid(uuid);

        checkReviewStatus(request, RequestReviewStatus.Revision);

        // Update the request details with the updated revision details
        request.setRequestDetail(request.getRevisionDetail());
        save(request);

        // Submit the request for validation by the organisation coordinator
        requestReviewProcessService.submitForValidation(user, request.getRequestReviewProcess());

        request = requestRepository.findOneByUuid(uuid);
        RequestRepresentation requestRepresentation = requestMapper.extendedRequestToRequestDTO(request);

        publishReviewStatusUpdate(user, RequestReviewStatus.Revision, request, null);

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
     *  @throws ActionNotAllowed if the request is not in status 'Draft'.
     */
    public void deleteDraft(IdentifiableUser user, UUID uuid) throws ActionNotAllowed {
        Request request = requestRepository.findOneByUuid(uuid);
        checkStatus(request, RequestStatus.Draft);
        if (!request.getRequester().equals(user.getUserUuid())) {
            throw new AccessDenied("Access denied to request " + uuid.toString());
        }
        log.debug("Request to delete Request : {}", uuid);
        deleteRequest(request.getId());
    }

    /**
     * Validating the request by uuid. If successful, the request will change to review status 'Review'.
     *
     * @param user the current user, validating the request
     * @param uuid the uuid of the request
     * @return the updated request
     * @throws ActionNotAllowed if the request is not in status 'Review' with review status 'Validation'.
     */
    public RequestRepresentation validateRequest(AuthenticatedUser user, UUID uuid) throws ActionNotAllowed {
        Request request = requestRepository.findOneByUuid(uuid);
        RequestReviewStatus sourceReviewStatus = checkReviewStatus(request, RequestReviewStatus.Validation);
        checkOrganisationAccess(user, request.getOrganisations(), AuthorityConstants.ORGANISATION_COORDINATOR);

        log.debug("Submitting request for review: {}", uuid);
        requestReviewProcessService.submitForReview(user, request.getRequestReviewProcess());

        request = requestRepository.findOneByUuid(uuid);

        // Once successfully started initiate review round and review feedback processes.
        ReviewRound reviewRound = reviewRoundService.createReviewRoundForRequest(request);
        request.getReviewRounds().add(reviewRound);

        requestRepository.save(request);

        publishReviewStatusUpdate(user, sourceReviewStatus, request, null);
        return requestMapper.extendedRequestToRequestDTO(request);
    }

    public RequestRepresentation rejectRequest(
        AuthenticatedUser user, UUID uuid, MessageRepresentation message
    ) throws ActionNotAllowed {
        Request request = requestRepository.findOneByUuid(uuid);

        RequestStatus sourceStatus = checkStatus(request, RequestStatus.Review);
        RequestReviewStatus sourceReviewStatus = checkReviewStatus(request, RequestReviewStatus.Validation, RequestReviewStatus.Review);
        checkOrganisationAccess(user, request.getOrganisations(), AuthorityConstants.ORGANISATION_COORDINATOR);

        // Reject the request
        requestReviewProcessService.reject(user, request.getRequestReviewProcess());

        // Finalize a potentially available review round
        reviewRoundService.finalizeReviewRoundForRequest(request);

        request = requestRepository.findOneByUuid(uuid);
        publishReviewStatusUpdate(user, sourceReviewStatus, request, message);

        if (request.getRequestReviewProcess().getStatus() == RequestReviewStatus.Closed &&
            request.getRequestReviewProcess().getDecision() == ReviewProcessOutcome.Rejected) {
            request.setStatus(RequestStatus.Closed);
            request.setOutcome(RequestOutcome.Rejected);
            request = save(request);
            publishStatusUpdate(user, sourceStatus, request, null);
        }

        return requestMapper.extendedRequestToRequestDTO(request);
    }

    public RequestRepresentation approveRequest(AuthenticatedUser user, UUID uuid) throws ActionNotAllowed {
        Request request = requestRepository.findOneByUuid(uuid);

        RequestStatus sourceStatus = checkStatus(request, RequestStatus.Review);
        RequestReviewStatus sourceReviewStatus = checkReviewStatus(request, RequestReviewStatus.Review);
        checkOrganisationAccess(user, request.getOrganisations(), AuthorityConstants.ORGANISATION_COORDINATOR);

        // Approve the request
        requestReviewProcessService.approve(user, request.getRequestReviewProcess());

        request = requestRepository.findOneByUuid(uuid);
        publishReviewStatusUpdate(user, sourceReviewStatus, request, null);

        if (request.getRequestReviewProcess().getStatus() == RequestReviewStatus.Closed &&
            request.getRequestReviewProcess().getDecision() == ReviewProcessOutcome.Approved) {
            request.setStatus(RequestStatus.Approved);
            request = save(request);
            publishStatusUpdate(user, sourceStatus, request, null);
        }

        // Finalize a potentially available review round
        reviewRoundService.finalizeReviewRoundForRequest(request);

        return requestMapper.extendedRequestToRequestDTO(request);
    }

    public RequestRepresentation requestRevision(
        AuthenticatedUser user, UUID uuid, MessageRepresentation message
    ) throws ActionNotAllowed {
        Request request = requestRepository.findOneByUuid(uuid);

        RequestReviewStatus sourceReviewStatus = checkReviewStatus(request, RequestReviewStatus.Validation, RequestReviewStatus.Review);
        checkOrganisationAccess(user, request.getOrganisations(), AuthorityConstants.ORGANISATION_COORDINATOR);

        // Request revision by the requester
        requestReviewProcessService.requestRevision(user, request.getRequestReviewProcess());

        request = requestRepository.findOneByUuid(uuid);

        // Finalize a potentially available review round
        reviewRoundService.finalizeReviewRoundForRequest(request);

        publishReviewStatusUpdate(user, sourceReviewStatus, request, message);
        return requestMapper.extendedRequestToRequestDTO(request);
    }

    /**
     * Submit the draft request by uuid.
     * Generates requests for the organisations specified in the draft.
     *
     * @param user the current user, submitting the request
     * @param uuid the uuid of the draft request
     * @return the list of generated requests to organisations.
     * @throws ActionNotAllowed if the request is not in status 'Draft'.
     */
    public List<RequestRepresentation> submitDraft(AuthenticatedUser user, UUID uuid) throws ActionNotAllowed {
        Request request = requestRepository.findOneByUuid(uuid);
        RequestStatus sourceStatus = checkStatus(request, RequestStatus.Draft);
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
            Request organisationRequest = requestMapper.clone(request);

            // Create organisation revision details
            RequestDetail revisionDetail = requestDetailMapper.clone(request.getRequestDetail());
            organisationRequest.setRevisionDetail(revisionDetail);

            try {
                Set<RequestType> selectedRequestTypes = organisationRequest.getRequestDetail().getRequestType();

                // Fetch the organisation object and filter the organisation supported request types.
                OrganisationDTO organisationDTO = organisationClientService.findOrganisationByUuid(organisationUuid);
                Set<RequestType> organisationRequestTypes = organisationDTO.getRequestTypes();

                Set<RequestType> organisationSupportedRequestTypes
                    = Sets.intersection(selectedRequestTypes, organisationRequestTypes);

                // Set the by the organisation supported request types for this request.
                organisationRequest.getRequestDetail().setRequestType(organisationSupportedRequestTypes);
                organisationRequest.getRevisionDetail().setRequestType(organisationSupportedRequestTypes);
            } catch (Exception e) {
                log.error("Error fetching organisation", e);
                throw new ServiceNotAvailable("Could not fetch organisation", e);
            }

            organisationRequest.setOrganisations(
                new HashSet<>(Collections.singleton(organisationUuid)));
            organisationRequest.setStatus(RequestStatus.Review);
            organisationRequest.setRequestReviewProcess(
                requestReviewProcessService.start(user));
            organisationRequest = save(organisationRequest);

            publishStatusUpdate(user, sourceStatus, organisationRequest, null);

            organisationRequests.add(organisationRequest);
            log.debug("Created new submitted request for organisation {}.", organisationUuid);
        }

        List<RequestRepresentation> result = requestMapper.extendedRequestsToRequestDTOs(organisationRequests);
        notificationService.submissionNotificationToRequester(user, result);

        log.debug("Deleting draft request.");
        deleteRequest(request.getId());
        return result;
    }

    /**
     * Close a request in status 'Approved' or 'Delivery'. Throws an exception if not all deliveries have been closed
     * in status 'Delivery'.
     * If in status 'Approved', the outcome is set to {@link RequestOutcome#Approved}.
     * If in status 'Delivery', if all deliveries were successful ('Received' or 'Returned'), the outcome is set to {@link RequestOutcome#Delivered};
     * else if some deliveries were successful, the outcome is set to {@link RequestOutcome#Partially_Delivered}.
     * @param user the current user.
     * @param uuid the uuid of the request.
     * @param message the (optional) message.
     * @return the updated request.
     * @throws ActionNotAllowed iff the request is not in status Approved or Delivery or the process is in status Delivery
     * and not all deliveries have been closed.
     */
    public RequestRepresentation closeRequest(
        AuthenticatedUser user, UUID uuid, MessageRepresentation message
    ) throws ActionNotAllowed {
        Request request = requestRepository.findOneByUuid(uuid);

        RequestStatus sourceStatus = request.getStatus();
        checkOrganisationAccess(user, request.getOrganisations(), AuthorityConstants.ORGANISATION_COORDINATOR);

        RequestOutcome outcome;
        switch (sourceStatus) {
            case Approved:
                outcome = RequestOutcome.Approved;
                break;
            case Delivery:
                if (request.getDeliveryProcesses().stream().anyMatch(deliveryProcess ->
                    deliveryProcess.getStatus() != DeliveryStatus.Closed)) {
                    throw new ActionNotAllowed("Not all delivery processes have been closed.");
                }
                if (request.getDeliveryProcesses().stream().allMatch(deliveryProcess ->
                    deliveryProcess.getOutcome() == DeliveryProcessOutcome.Received || deliveryProcess.getOutcome() == DeliveryProcessOutcome.Returned)) {
                    outcome = RequestOutcome.Delivered;
                } else if (request.getDeliveryProcesses().stream().anyMatch(deliveryProcess ->
                    deliveryProcess.getOutcome() == DeliveryProcessOutcome.Received || deliveryProcess.getOutcome() == DeliveryProcessOutcome.Returned)) {
                    outcome = RequestOutcome.Partially_Delivered;
                } else {
                    outcome = RequestOutcome.Cancelled;
                }
                break;
            default:
                throw ActionNotAllowed.forStatus(sourceStatus);
        }

        request.setStatus(RequestStatus.Closed);
        request.setOutcome(outcome);
        request = save(request);
        publishStatusUpdate(user, sourceStatus, request, message);

        return requestMapper.extendedRequestToRequestDTO(request);
    }


    /**
     * Save the review feedback for the current review round in a request.
     *
     * @param user the currently logged in user.
     * @param requestUuid the uuid of the request the review belongs to.
     * @param feedbackBody the feedback supplied by the reviewer.
     *
     * @return ReviewFeedback the updated review feedback.
     * @throws AccessDenied if the currently logged in user is not the owner of the feedback.
     * @throws ActionNotAllowed when the request is not in status 'Review', the feedback is not part of the request, or
     * the feedback has already been saved before.
     */
    @Timed
    public RequestRepresentation saveReviewFeedback(
        AuthenticatedUser user,
        UUID requestUuid,
        ReviewFeedbackRepresentation feedbackBody
    ) throws ActionNotAllowed {
        log.debug("Saving review feedback for {}", feedbackBody.getUuid());

        Request request = requestRepository.findOneByUuid(requestUuid);
        checkStatus(request, RequestStatus.Review);

        final UUID feedbackUuid = feedbackBody.getUuid();

        if (request.getReviewRounds() == null || request.getReviewRounds().isEmpty()) {
            throw new RuntimeException("No review rounds found for request " + requestUuid.toString());
        }
        ReviewRound currentReviewRound = request.getReviewRounds().get(request.getReviewRounds().size() - 1);

        // Check whether the feedback is part of the request.
        if (currentReviewRound.getReviewFeedback().stream().noneMatch(reviewFeedback ->
            reviewFeedback.getUuid().equals(feedbackUuid))) {
            throw new ActionNotAllowed(
                String.format("Review feedback (%s) is not part of the current review round of request (%s)",
                    feedbackUuid,
                    request.getUuid())
            );
        }

        ReviewFeedback feedback = reviewFeedbackRepository.findOneByUuid(feedbackUuid);
        if (feedback == null) {
            throw new ResourceNotFound("Review feedback could not be found for " + feedbackUuid.toString());
        }
        // Check if the current user is the owner of the review feedback
        if (!user.getUuid().equals(feedback.getReviewer())) {
            log.error("Current user ({}) is not the owner ({}) of the review feedback ({}).",
                user.getUuid(), feedback.getReviewer(), feedback.getUuid()
            );
            throw new AccessDenied("Current user is not the review feedback assignee.");
        }
        // Check if the review feedback has not already been saved before
        if (feedback.getAdvice() != ReviewProcessOutcome.None) {
            throw new ActionNotAllowed(
                String.format("Review feedback (%s) has already been saved.", feedbackUuid)
            );
        }

        feedback = reviewFeedbackMapper.safeUpdateReviewFeedbackFromDTO(feedbackBody, feedback);
        feedback = reviewFeedbackRepository.save(feedback);
        reviewFeedbackSearchRepository.save(feedback);
        entityManager.flush();
        entityManager.refresh(request);
        return requestMapper.extendedRequestToRequestDTO(request);
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

}
