/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.service;

import com.codahale.metrics.annotation.Timed;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import nl.thehyve.podium.common.IdentifiableUser;
import nl.thehyve.podium.common.config.FilterValues;
import nl.thehyve.podium.common.enumeration.*;
import nl.thehyve.podium.common.exceptions.AccessDenied;
import nl.thehyve.podium.common.exceptions.ActionNotAllowed;
import nl.thehyve.podium.common.exceptions.InvalidRequest;
import nl.thehyve.podium.common.exceptions.ResourceNotFound;
import nl.thehyve.podium.common.security.AccessCheckHelper;
import nl.thehyve.podium.common.security.AuthenticatedUser;
import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.common.service.dto.*;
import nl.thehyve.podium.domain.Request;
import nl.thehyve.podium.repository.RequestRepository;
import nl.thehyve.podium.repository.SummaryEntry;
import nl.thehyve.podium.repository.search.RequestSearchRepository;
import nl.thehyve.podium.security.RequestAccessCheckHelper;
import nl.thehyve.podium.service.mapper.RequestDetailMapper;
import nl.thehyve.podium.service.mapper.RequestMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
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
    private RequestSearchRepository requestSearchRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private StatusUpdateEventService statusUpdateEventService;

    @Autowired
    private RequestReviewProcessService requestReviewProcessService;

    @Autowired
    private OrganisationClientService organisationClientService;

    @PostConstruct
    private void init() {
        notificationService.setRequestService(this);
    }

    private static Map<OverviewStatus, Long> getOverviewCounts(
        long requestCount,
        Map<RequestStatus, Long> requestStatusCounts,
        Map<RequestReviewStatus, Long> requestReviewStatusCounts,
        Map<RequestOutcome, Long> requestOutcomeCounts
    ) {
        Map<OverviewStatus, Long> result = new HashMap<>();
        for(OverviewStatus status: OverviewStatus.values()) {
            if (status == OverviewStatus.None) {
                break;
            }
            FilterValues filterValues = FilterValues.forStatus(status);
            switch (filterValues.getRequestStatus()) {
                case None:
                    result.put(status, requestCount);
                    break;
                case Review: {
                    Long count = requestReviewStatusCounts.get(filterValues.getReviewStatus());
                    if (count != null) {
                        result.put(status, count);
                    }
                }
                break;
                case Closed: {
                    Long count = requestOutcomeCounts.get(filterValues.getRequestOutcome());
                    if (count != null) {
                        result.put(status, count);
                    }
                }
                break;
                default: {
                    Long count = requestStatusCounts.get(filterValues.getRequestStatus());
                    if (count != null) {
                        result.put(status, count);
                    }
                }
                break;
            }
        }
        return result;
    }

    public static void validateRequest(RequestDetailRepresentation requestDetail) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        Set<ConstraintViolation<RequestDetailRepresentation>> requestConstraintViolations = validator.validate(requestDetail);
        if (!requestConstraintViolations.isEmpty()) {
            throw new InvalidRequest("Invalid request", requestConstraintViolations);
        }
    }

    /**
     * Gets counts of requests per overview status for the specified requester.
     * @param requesterUuid the uuid of the requester.
     * @return a map from status to number of requests.
     */
    private Map<OverviewStatus, Long> getCountsForRequester(UUID requesterUuid) {
        long requestCount = requestRepository.countByRequester(requesterUuid);
        Map<RequestStatus, Long> requestStatusCounts = requestRepository.countByRequesterPerStatus(requesterUuid)
            .stream().collect(Collectors.toMap(SummaryEntry::getType, SummaryEntry::getCount));
        Map<RequestReviewStatus, Long> requestReviewStatusCounts = requestRepository.countByRequesterPerRequestReviewStatus(requesterUuid)
            .stream().collect(Collectors.toMap(SummaryEntry::getType, SummaryEntry::getCount));
        Map<RequestOutcome, Long> requestOutcomeCounts = requestRepository.countByRequesterPerOutcome(requesterUuid)
            .stream().collect(Collectors.toMap(SummaryEntry::getType, SummaryEntry::getCount));
        return getOverviewCounts(requestCount, requestStatusCounts, requestReviewStatusCounts, requestOutcomeCounts);
    }

    /**
     * Gets a page of requests in the specified overview status for the requester.
     * The filters for the overview status are configured in {@link FilterValues}.
     * @param requesterUuid the uuid of the requester to fetch the requests for.
     * @param status the overview status.
     * @param pageable pagination information.
     * @return the page with request representations.
     */
    private Page<RequestRepresentation> findRequesterRequestsInStatus(UUID requesterUuid,
                                                                      OverviewStatus status,
                                                                      Pageable pageable) {
        FilterValues filterValues = FilterValues.forStatus(status);
        Page<Request> result;
        switch (filterValues.getRequestStatus()) {
            case None:
                result = requestRepository.findAllByRequester(requesterUuid, pageable);
                break;
            case Review:
                result = requestRepository.findAllByRequesterAndRequestReviewStatus(requesterUuid, filterValues.getReviewStatus(), pageable);
                break;
            case Closed:
                result = requestRepository.findAllByRequesterAndOutcome(requesterUuid, filterValues.getRequestOutcome(), pageable);
                break;
            default:
                result = requestRepository.findAllByRequesterAndStatus(requesterUuid, filterValues.getRequestStatus(), pageable);
                break;
        }
        return result.map(requestMapper::detailedRequestToRequestDTO);
    }

    /**
     * Gets counts of requests per overview status for the specified organisations.
     * @param organisationUuids the uuids of the organisations.
     * @return a map from status to number of requests.
     */
    private Map<OverviewStatus, Long> getCountsForOrganisations(Set<UUID> organisationUuids) {
        long requestCount = requestRepository.countByOrganisations(organisationUuids);
        Map<RequestStatus, Long> requestStatusCounts = requestRepository.countByOrganisationsPerStatus(organisationUuids)
            .stream().collect(Collectors.toMap(SummaryEntry::getType, SummaryEntry::getCount));
        Map<RequestReviewStatus, Long> requestReviewStatusCounts = requestRepository.countByOrganisationsPerRequestReviewStatus(organisationUuids)
            .stream().collect(Collectors.toMap(SummaryEntry::getType, SummaryEntry::getCount));
        Map<RequestOutcome, Long> requestOutcomeCounts = requestRepository.countByOrganisationsPerOutcome(organisationUuids)
            .stream().collect(Collectors.toMap(SummaryEntry::getType, SummaryEntry::getCount));
        return getOverviewCounts(requestCount, requestStatusCounts, requestReviewStatusCounts, requestOutcomeCounts);
    }

    /**
     * Gets a page of requests in the specified overview status for the specified organisations.
     * The filters for the overview status are configured in {@link FilterValues}.
     * @param organisationUuids the uuids of the organisations to fetch the requests for.
     * @param status the overview status.
     * @param pageable pagination information.
     * @return the page with request representations.
     */
    private Page<RequestRepresentation> findOrganisationRequestsInStatus(Set<UUID> organisationUuids,
                                                                         OverviewStatus status,
                                                                         Pageable pageable) {
        FilterValues filterValues = FilterValues.forStatus(status);
        Page<Request> result;
        switch (filterValues.getRequestStatus()) {
            case None:
                result = requestRepository.findAllByOrganisations(organisationUuids, pageable);
                break;
            case Review:
                result = requestRepository.findAllByOrganisationsAndRequestReviewStatus(organisationUuids, filterValues.getReviewStatus(), pageable);
                break;
            case Closed:
                result = requestRepository.findAllByOrganisationsAndOutcome(organisationUuids, filterValues.getRequestOutcome(), pageable);
                break;
            default:
                result = requestRepository.findAllByOrganisationsAndStatus(organisationUuids, filterValues.getRequestStatus(), pageable);
                break;
        }
        return result.map(requestMapper::overviewRequestToRequestDTO);
    }

    /**
     * Gets the organisation uuids of the organisations for which the user has the specified role.
     * @param user the user
     * @param authority the role name
     */
    private static Set<UUID> getOrganisationsUuidsForRole(AuthenticatedUser user, String authority) {
        return user.getOrganisationAuthorities().entrySet().stream()
            .filter(entry -> entry.getValue().contains(authority))
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
    }

    private Page<RequestRepresentation> findAllOrganisationRequestsInStatusForRole(AuthenticatedUser user,
                                                                                   OverviewStatus status,
                                                                                   String authority,
                                                                                   Pageable pageable) {
        Set<UUID> organisationUuids = getOrganisationsUuidsForRole(user, authority);
        return findOrganisationRequestsInStatus(organisationUuids, status, pageable);
    }

    private Map<OverviewStatus, Long> countOrganisationRequestsForRole(AuthenticatedUser user,
                                                                       String authority) {
        Set<UUID> organisationUuids = getOrganisationsUuidsForRole(user, authority);
        return getCountsForOrganisations(organisationUuids);
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

        log.debug("Access and status checks...");
        RequestAccessCheckHelper.checkRequester(user, request);
        RequestAccessCheckHelper.checkReviewStatus(request, RequestReviewStatus.Revision);
        OverviewStatus sourceStatus = request.getOverviewStatus();

        log.debug("Validate new request data.");
        RequestDetailRepresentation requestData = requestDetailMapper.requestDetailToRequestDetailRepresentation(request.getRevisionDetail());
        validateRequest(requestData);

        request.setRequestDetail(request.getRevisionDetail());

        // Update the request details with the updated revision details
        requestRepository.save(request);

        // Submit the request for validation by the organisation coordinator
        requestReviewProcessService.submitForValidation(user, request.getRequestReviewProcess());

        request = requestRepository.findOneByUuid(uuid);
        RequestRepresentation requestRepresentation = requestMapper.detailedRequestToRequestDTO(request);

        statusUpdateEventService.publishStatusUpdate(user, sourceStatus, request, null);

        return requestRepresentation;
    }

    /**
     *  Get a request by UUID
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
        return requestMapper.detailedRequestToRequestDTO(request);
    }

    /**
     *  Get a request by UUID
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
        return requestMapper.overviewRequestToRequestDTO(request);
    }

    /**
     *  Count the requests for the requester per overview status.
     *
     *  @param requester the current user (the requester)
     *  @return the map from overview status to number of requests
     */
    @Transactional(readOnly = true)
    public Map<OverviewStatus, Long> countForRequester(IdentifiableUser requester) {
        log.debug("Request to count Requests");
        return getCountsForRequester(requester.getUserUuid());
    }

    /**
     *  Get all the requests for the requester.
     *
     *  @param requester the current user (the requester)
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<RequestRepresentation> findAllForRequester(IdentifiableUser requester, Pageable pageable) {
        log.debug("Request to get all Requests");
        Page<Request> result = requestRepository.findAllByRequester(requester.getUserUuid(), pageable);
        return result.map(requestMapper::detailedRequestToRequestDTO);
    }

    /**
     *  Get all the requests in the specified status for the requester.
     *
     *  @param requester the current user (the requester)
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<RequestRepresentation> findAllForRequesterInStatus(
        IdentifiableUser requester, OverviewStatus status, Pageable pageable) {
        log.debug("Request to get all Requests in status {}", status);
        return findRequesterRequestsInStatus(requester.getUserUuid(), status, pageable);
    }

    /**
     *  Count the requests for the reviewer for overview status 'Review'.
     *
     *  @param user the current user (the reviewer)
     *  @return the map from overview status to number of requests
     */
    @Transactional(readOnly = true)
    public Map<OverviewStatus, Long> countForReviewer(AuthenticatedUser user) {
        log.debug("Request to count Requests for reviewer");
        Set<UUID> organisationUuids = getOrganisationsUuidsForRole(user, AuthorityConstants.REVIEWER);
        Map<OverviewStatus, Long> result = new HashMap<>();
        Long reviewRequestCount = requestRepository.countByOrganisationsAndRequestReviewStatus(
            organisationUuids, RequestReviewStatus.Review
        );
        result.put(OverviewStatus.All, reviewRequestCount);
        result.put(OverviewStatus.Review, reviewRequestCount);
        return result;
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
        return findAllOrganisationRequestsInStatusForRole(user, OverviewStatus.Review, AuthorityConstants.REVIEWER, pageable);
    }

    /**
     *  Count the requests for the organisation coordinator per overview status.
     *
     *  @param user the current user (the organisation coordinator)
     *  @return the map from overview status to number of requests
     */
    @Transactional(readOnly = true)
    public Map<OverviewStatus, Long> countForCoordinator(AuthenticatedUser user) {
        log.debug("Request to count Requests for organisation coordinator");
        return countOrganisationRequestsForRole(user, AuthorityConstants.ORGANISATION_COORDINATOR);
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
                                                                     OverviewStatus status,
                                                                     Pageable pageable) {
        log.debug("Request to get all organisation requests for a coordinator");
        return findAllOrganisationRequestsInStatusForRole(user, status, AuthorityConstants.ORGANISATION_COORDINATOR, pageable);
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
        AccessCheckHelper.checkOrganisationAccess(user, organisationUuid, AuthorityConstants.REVIEWER);
        return findOrganisationRequestsInStatus(Collections.singleton(organisationUuid), OverviewStatus.Review, pageable);
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
                                                                                   OverviewStatus status,
                                                                                   UUID organisationUuid,
                                                                                   Pageable pageable) {
        log.debug("Request to get all organisation requests for an organisation for a coordinator");
        AccessCheckHelper.checkOrganisationAccess(user, organisationUuid, AuthorityConstants.ORGANISATION_COORDINATOR);
        return findOrganisationRequestsInStatus(Collections.singleton(organisationUuid), status, pageable);
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
        return requestMapper.overviewRequestToRequestDTO(request);
    }

    /**
     * Save a request.
     *
     * @param request the entity to save
     * @return the persisted entity
     */
    public Request save(Request request) {
        return requestRepository.save(request);
    }

    void deleteRequest(Long id) {
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
        RequestAccessCheckHelper.checkRequester(user, request);
        RequestAccessCheckHelper.checkStatus(request, RequestStatus.Draft);
        log.debug("Request to delete Request : {}", uuid);
        deleteRequest(request.getId());
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

        OverviewStatus sourceStatus = request.getOverviewStatus();
        AccessCheckHelper.checkOrganisationAccess(user, request.getOrganisations(), AuthorityConstants.ORGANISATION_COORDINATOR);

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
        statusUpdateEventService.publishStatusUpdate(user, sourceStatus, request, message);

        return requestMapper.detailedRequestToRequestDTO(request);
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
        return result.map(requestMapper::overviewRequestToRequestDTO);
    }

    /**
     * Handle external request data and create a new request draft
     *
     * @param newRequest a newly initiated Request
     * @param user Authenticated user making the request
     * @param externalRequestRepresentation the data given to us from the external party
     * @return Filled in RequestRepresentation
     */
     public Map<String, Object> createExternalRequest(RequestRepresentation newRequest, AuthenticatedUser user,
                                                      ExternalRequestRepresentation externalRequestRepresentation){
         RequestDetailRepresentation detail = newRequest.getRequestDetail();

         detail.setSearchQuery(externalRequestRepresentation.getHumanReadable());

         ArrayList<Map<String, String>> collections = externalRequestRepresentation.getCollections();

         // Get the String id's from the exteral request and turn them into a list of relevant organisations
         List<OrganisationRepresentation> organisations = new ArrayList<>();
         List<Map<String, String>> missingOrgUUIDS = new ArrayList<>();

         for (Map<String, String> collection : collections) {
             try {
                 UUID biobankID = UUID.fromString(collection.get("biobankID"));
                 log.debug("Checking for organization", biobankID);

                 OrganisationRepresentation organisationRepresentation =
                     organisationClientService.findOrganisationByUuidCached(biobankID);

                 organisations.add(organisationRepresentation);

             } catch (IllegalArgumentException e) {
                 Map<String, String> error = new HashMap<>();
                 error.put("organisationId", collection.get("biobankID"));
                 error.put("errorMessage", e.getMessage());
                 missingOrgUUIDS.add(error);

             } catch (HystrixRuntimeException e) {
                 Map<String, String> error = new HashMap<>();
                 error.put("organisationId", collection.get("biobankID"));
                 error.put("errorMessage", "Cannot find an organization for the given id: " +
                     collection.get("biobankID"));
                 missingOrgUUIDS.add(error);
             }
         }

         newRequest.setOrganisations(organisations);

         Set<RequestType> allTypes = new HashSet<>(Arrays.asList(RequestType.Data, RequestType.Images,
             RequestType.Material));
         detail.setRequestType(allTypes);

         newRequest.setRequestDetail(detail);

         Map<String, Object> returnObject = new HashMap<>();
         returnObject.put("draft", newRequest);
         returnObject.put("missingOrgUUIDs", missingOrgUUIDS);
         return returnObject;
     }
}
