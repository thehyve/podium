/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.common.resource;

import nl.thehyve.podium.common.security.annotations.OrganisationUuidParameter;
import nl.thehyve.podium.common.service.dto.UserRepresentation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.UUID;

@RequestMapping("/internal")
public interface InternalRoleResource {

    /**
     * GET  /organisations/uuid/:uuid/roles/{authority}/users
     * Fetches the users in a particular role for a certain organisation.
     *
     * @param uuid the uuid of the organisation
     * @param authority the authority name of the role
     * @return the ResponseEntity with status 200 (OK) and with body the users, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/organisations/uuid/{uuid}/roles/{authority}/users", method = RequestMethod.GET)
    ResponseEntity<List<UserRepresentation>> getOrganisationRoleUsers(
        @OrganisationUuidParameter @PathVariable("uuid") UUID uuid,
        @PathVariable("authority") String authority);

}
