/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.common.service;

import nl.thehyve.podium.common.resource.InternalRequestResource;
import nl.thehyve.podium.common.service.dto.OrganisationDTO;
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

    @Autowired
    private InternalRequestResource internalRequestResource;

    @Autowired
    private SecurityService securityService;

    /**
     * If the current user has the specified authority within one of the organisations
     * associated with the request with the specified uuid.
     * @param requestUuid the uuid of the request.
     * @return true if current user has the specified authority within one of the organisations; false otherwise.
     */
    public boolean isCurrentUserInOrganisationRoleForRequest(UUID requestUuid, String authority) {
        log.info("Checking access for request {}, role {}", requestUuid, authority);
        if (internalRequestResource == null) {
            log.error("No request resource available.");
            return false;
        }
        try {
            ResponseEntity<RequestRepresentation> response = internalRequestResource.getRequest(requestUuid);
            if (response.getStatusCode() != HttpStatus.OK) {
                log.error("Could not fetch request with uuid {}. Status code: {}", requestUuid, response.getStatusCode());
            }
            for(OrganisationDTO organisation: response.getBody().getOrganisations()) {
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
        log.info("Checking access for requester of request {}", requestUuid);
        if (internalRequestResource == null) {
            log.error("No request resource available.");
            return false;
        }
        try {
            log.debug("Fetching request {} ... ", requestUuid);
            ResponseEntity<RequestRepresentation> response = internalRequestResource.getRequest(requestUuid);
            if (response.getStatusCode() != HttpStatus.OK) {
                log.error("Could not fetch request with uuid {}. Status code: {}", requestUuid, response.getStatusCode());
            }
            log.debug("Request found. Owner: {}, current user: {}", response.getBody().getRequester(),
                securityService.getCurrentUser().getUuid());
            return (securityService.getCurrentUser().getUuid().equals(response.getBody().getRequester()));
        } catch (URISyntaxException e) {
            log.error("Could not fetch request with uuid {}", requestUuid);
            return false;
        }
    }

}
