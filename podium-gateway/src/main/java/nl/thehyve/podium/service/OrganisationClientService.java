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
import nl.thehyve.podium.common.service.dto.OrganisationDTO;
import nl.thehyve.podium.common.service.dto.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    public List<OrganisationDTO> findAllOrganisations() throws URISyntaxException, FeignException {
        return organisationClient.getAllOrganisations().getBody();
    }

    public OrganisationDTO findOrganisationByUuid(UUID organisationUuid) throws FeignException {
        return organisationClient.getOrganisation(organisationUuid).getBody();
    }

    public List<UserRepresentation> findUsersByRole(UUID organisationUuid, String authority) {
        return internalRoleClient.getOrganisationRoleUsers(organisationUuid, authority).getBody();
    }

}
