/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.service;

import nl.thehyve.podium.common.IdentifiableUser;
import nl.thehyve.podium.common.exceptions.AccessDenied;
import nl.thehyve.podium.common.exceptions.ResourceNotFound;
import nl.thehyve.podium.common.security.AuthenticatedUser;
import nl.thehyve.podium.common.security.UserAuthenticationToken;
import nl.thehyve.podium.domain.PrincipalInvestigator;
import nl.thehyve.podium.domain.Request;
import nl.thehyve.podium.domain.RequestDetail;
import nl.thehyve.podium.domain.enumeration.RequestStatus;
import nl.thehyve.podium.common.exceptions.ActionNotAllowedInStatus;
import nl.thehyve.podium.repository.RequestRepository;
import nl.thehyve.podium.repository.search.RequestSearchRepository;
import nl.thehyve.podium.service.mapper.RequestMapper;
import nl.thehyve.podium.service.representation.RequestRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.*;

import static org.elasticsearch.index.query.QueryBuilders.*;

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


    public RequestService() {
    }

    private static ActionNotAllowedInStatus actionNotAllowedInStatus(RequestStatus status) {
        return new ActionNotAllowedInStatus("Action not allowed in status: " + status.name());
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
     *  Get all the requests.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<RequestRepresentation> findAllForRequester(IdentifiableUser requester, Pageable pageable) {
        log.debug("Request to get all Requests");
        Page<Request> result = requestRepository.findAllByRequester(requester.getUserUuid(), pageable);
        return result.map(requestMapper::requestToRequestDTO);
    }

    /**
     *  Get the request for the requester
     *
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
     * Perform look up of request drafts by requester.
     *
     * @param requester The user to perform the lookup for
     * @return the transformed DTO list of requests
     */
    @Transactional(readOnly = true)
    public Page<RequestRepresentation> findAllRequestsForRequesterByStatus(IdentifiableUser requester, RequestStatus status, Pageable pageable) {
        Page<Request> result = requestRepository.findAllByRequesterAndStatus(
            requester.getUserUuid(), status, pageable);
        return result.map(requestMapper::requestToRequestDTO);
    }

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


    public RequestRepresentation updateDraft(IdentifiableUser user, RequestRepresentation body) throws ActionNotAllowedInStatus {
        Request request = requestRepository.findOneByUuid(body.getUuid());
        if (request.getStatus() != RequestStatus.Draft) {
            throw actionNotAllowedInStatus(request.getStatus());
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
     *  @param uuid the uuid of the request
     */
    public void deleteDraft(IdentifiableUser user, UUID uuid) throws ActionNotAllowedInStatus {
        Request request = requestRepository.findOneByUuid(uuid);
        if (request.getStatus() != RequestStatus.Draft) {
            throw actionNotAllowedInStatus(request.getStatus());
        }
        if (!request.getRequester().equals(user.getUserUuid())) {
            throw new AccessDenied("Access denied to request " + uuid.toString());
        }
        log.debug("Request to delete Request : {}", uuid);
        deleteRequest(request.getId());
    }

    /**
     * FIXME: Do a deep clone of the request
     */
    private Request cloneRequest(Request source) {
        RequestRepresentation requestData = requestMapper.requestToRequestDTO(source);
        requestData.setUuid(null);
        requestData.setId(null);
        requestData.setRequestDetail(null);
        Request clone = requestMapper.requestDTOToRequest(requestData);
        RequestDetail details = new RequestDetail();
        details.setPrincipalInvestigator(new PrincipalInvestigator());
        clone.setRequestDetail(details);
        return clone;
    }

    /**
     * Submit the draft request by uuid.
     * Generates requests for the organisations specified in the draft.
     *
     * @param user the current user, submitting the request
     * @param uuid the uuid of the draft request
     * @return the list of generated requests to organisations.
     */
    public List<RequestRepresentation> submitDraft(AuthenticatedUser user, UUID uuid) throws ActionNotAllowedInStatus {
        Request request = requestRepository.findOneByUuid(uuid);
        if (request.getStatus() != RequestStatus.Draft) {
            throw actionNotAllowedInStatus(request.getStatus());
        }
        if (!request.getRequester().equals(user.getUserUuid())) {
            throw new AccessDenied("Access denied to request.");
        }
        log.debug("Submitting request : {}", uuid);

        List<Request> organisationRequests = new ArrayList<>();
        for (UUID organisationUuid: request.getOrganisations()) {
            // TODO: Fetch organisation DTO through Feign.
            // OrganisationDTO organisation;
            // OR: leave this to the (asynchronous) mail service call
            // to notify the organisations.
            Request organisationRequest = cloneRequest(request);
            organisationRequest.setOrganisations(
                new HashSet<>(Collections.singleton(organisationUuid)));
            organisationRequest.setStatus(RequestStatus.Review);
            organisationRequest.setRequestReviewProcess(
                requestReviewProcessService.start(user));
            organisationRequest = save(organisationRequest);
            organisationRequests.add(organisationRequest);
            log.debug("Created new submitted request for organisation {}.", organisationUuid);
        }
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
