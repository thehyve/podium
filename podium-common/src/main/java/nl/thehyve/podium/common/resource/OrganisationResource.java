/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.common.resource;

import nl.thehyve.podium.common.security.annotations.OrganisationUuidParameter;
import nl.thehyve.podium.common.service.dto.OrganisationRepresentation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

@RequestMapping("/api")
public interface OrganisationResource {

    /**
     * GET  /organisations/all : get all the organisations.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of organisations in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @RequestMapping(value = "/organisations/all", method = RequestMethod.GET)
    ResponseEntity<List<OrganisationRepresentation>> getAllOrganisations() throws URISyntaxException;

    /**
     * GET  /organisations/uuid/:uuid : get the "uuid" organisation.
     *
     * @param uuid the uuid of the organisation to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the organisation, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/organisations/uuid/{uuid}", method = RequestMethod.GET)
    ResponseEntity<OrganisationRepresentation> getOrganisation(@OrganisationUuidParameter @PathVariable("uuid") UUID uuid);

}
