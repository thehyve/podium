/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.service;

import feign.FeignException;
import nl.thehyve.podium.client.OrganisationClient;
import nl.thehyve.podium.common.service.dto.OrganisationDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

@Service
public class OrganisationService {

    private final Logger log = LoggerFactory.getLogger(OrganisationService.class);

    @Inject
    OrganisationClient organisationClient;

    public List<OrganisationDTO> findAllOrganisations() throws URISyntaxException, FeignException {
        return organisationClient.getAllOrganisations().getBody();
    }

    public OrganisationDTO findOrganisationByUuid(UUID organisationUuid) throws FeignException {
        return organisationClient.getOrganisation(organisationUuid).getBody();
    }

}
