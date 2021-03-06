/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.common.service;

import nl.thehyve.podium.common.enumeration.OverviewStatus;
import nl.thehyve.podium.common.resource.InternalRequestResource;
import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.common.service.dto.OrganisationRepresentation;
import nl.thehyve.podium.common.service.dto.RequestRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URISyntaxException;
import java.util.UUID;

@Service
@Transactional
public class RequestSecurityService {

    private static final Logger log = LoggerFactory.getLogger(RequestSecurityService.class);

    @Autowired(required = false)
    private InternalRequestResource internalRequestResource;

    @Autowired
    private SecurityService securityService;

    /**
     * If the current user has the specified authority within one of the organisations
     * associated with the request with the specified uuid.
     * Access is denied to draft requests.
     * Access is denied to reviewers to requests that are not in a review status.
     * @param requestUuid the uuid of the request.
     * @return true if current user has the specified authority within one of the organisations
     * and the request is not a draft and the request is either in a review status or the authority is not reviewer;
     * false otherwise.
     */
    public boolean isCurrentUserInOrganisationRoleForRequest(UUID requestUuid, String authority) {
        log.debug("Checking access for request {}, role {}", requestUuid, authority);
        if (internalRequestResource == null) {
            log.error("No request resource available.");
            return false;
        }
        try {
            ResponseEntity<RequestRepresentation> response = internalRequestResource.getRequestBasic(requestUuid);
            if (response == null || response.getStatusCode() != HttpStatus.OK) {
                log.error("Could not fetch request with uuid {}. Status code: {}",
                    requestUuid, response == null ? null : response.getStatusCode());
                return false;
            }
            OverviewStatus status = response.getBody().getStatus();
            if (status == OverviewStatus.Draft) {
                // Organisation users do not have access to draft requests.
                return false;
            }
            if (authority.equals(AuthorityConstants.REVIEWER) && status != OverviewStatus.Review) {
                // Organisation reviewers only have access to requests in review status.
                return false;
            }
            for (OrganisationRepresentation organisation: response.getBody().getOrganisations()) {
                if (securityService.isCurrentUserInOrganisationRole(organisation.getUuid(), authority)) {
                    return true;
                }
            }
            return false;
        } catch (URISyntaxException e) {
            log.error("Could not fetch request with uuid {}", requestUuid);
            return false;
        }
    }

    /**
     * If the current user is the owner (requester) of the request with the specified uuid.
     * @param requestUuid the uuid of the request.
     * @return true if current user is the owner (requester) of the request; false otherwise.
     */
    public boolean isCurrentUserOwnerOfRequest(UUID requestUuid) {
        log.debug("Checking access for requester of request {}", requestUuid);
        if (internalRequestResource == null) {
            log.error("No request resource available.");
            return false;
        }
        try {
            log.debug("Fetching request {} ... ", requestUuid);
            ResponseEntity<RequestRepresentation> response = internalRequestResource.getRequestBasic(requestUuid);
            if (response == null || response.getStatusCode() != HttpStatus.OK) {
                log.error("Could not fetch request with uuid {}. Status code: {}",
                    requestUuid, response == null ? null : response.getStatusCode());
                return false;
            }

            if (response.getBody().getRequester() == null) {
                log.error("Requester not present in request representation. {}", response.getBody());
                return false;
            }

            log.debug("Request found. Owner: {}, current user: {}", response.getBody().getRequester().getUuid(),
                securityService.getCurrentUser().getUuid());
            return (securityService.getCurrentUser().getUuid().equals(response.getBody().getRequester().getUuid()));
        } catch (URISyntaxException e) {
            log.error("Could not fetch request with uuid {}", requestUuid);
            return false;
        }
    }

}
