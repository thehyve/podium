/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.service;

import feign.FeignException;
import nl.thehyve.podium.client.InternalRoleClient;
import nl.thehyve.podium.client.OrganisationClient;
import nl.thehyve.podium.common.exceptions.ResourceNotFound;
import nl.thehyve.podium.common.exceptions.ServiceNotAvailable;
import nl.thehyve.podium.common.service.dto.OrganisationRepresentation;
import nl.thehyve.podium.common.service.dto.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

@Service
public class OrganisationClientService {

    private final Logger log = LoggerFactory.getLogger(OrganisationClientService.class);

    @Autowired
    OrganisationClient organisationClient;

    @Autowired
    InternalRoleClient internalRoleClient;

    public List<OrganisationRepresentation> findAllOrganisations() throws URISyntaxException, FeignException {
        log.debug("Fetching all organisations through Feign ...");
        ResponseEntity<List<OrganisationRepresentation>> response = organisationClient.getAllOrganisations();
        switch (response.getStatusCode()) {
            case OK:
                return response.getBody();
            default:
                throw new ServiceNotAvailable("Error while fetching organisations.");
        }
    }

    public OrganisationRepresentation findOrganisationByUuid(UUID organisationUuid) throws FeignException {
        log.debug("Fetching organisation through Feign ...");
        ResponseEntity<OrganisationRepresentation> response = organisationClient.getOrganisation(organisationUuid);
        switch (response.getStatusCode()) {
            case OK:
                return response.getBody();
            case NOT_FOUND:
                throw new ResourceNotFound("Organisation not found.");
            default:
                throw new ServiceNotAvailable("Error while fetching the organisation.");
        }
    }

    @Cacheable("remoteOrganisations")
    public OrganisationRepresentation findOrganisationByUuidCached(UUID organisationUuid) throws FeignException {
        log.debug("Fetching organisation through Feign ...");
        return findOrganisationByUuid(organisationUuid);
    }

    public List<UserRepresentation> findUsersByRole(UUID organisationUuid, String authority) {
        log.debug("Fetching organisation users through Feign ...");
        ResponseEntity<List<UserRepresentation>> response = internalRoleClient.getOrganisationRoleUsers(organisationUuid, authority);
        switch (response.getStatusCode()) {
            case OK:
                return response.getBody();
            default:
                throw new ServiceNotAvailable("Error while fetching organisation users.");
        }
    }

}
