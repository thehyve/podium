/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.common.resource;

import nl.thehyve.podium.common.security.AuthenticatedUser;
import nl.thehyve.podium.common.security.SerialisedUser;
import nl.thehyve.podium.common.security.annotations.UserUuidParameter;
import nl.thehyve.podium.common.service.dto.UserRepresentation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.UUID;

@RequestMapping("/internal")
public interface InternalUserResource {

    /**
     * GET  /users/uuid/:uuid
     * Fetches the user details for the user with the given uuid.
     *
     * @param uuid the uuid of the user
     * @return the ResponseEntity with status 200 (OK) and with body the user, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/users/uuid/{uuid}", method = RequestMethod.GET)
    ResponseEntity<UserRepresentation> getUser(
        @UserUuidParameter @PathVariable("uuid") UUID uuid);

    /**
     * GET  /users/login/:login
     * Fetches the authenticated user for the user with the given login.
     *
     * @param login the login of the user
     * @return the ResponseEntity with status 200 (OK) and with body the user, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/users/login/{login}", method = RequestMethod.GET)
    ResponseEntity<SerialisedUser> getAuthenticatedUserByLogin(
        @PathVariable("login") String login);

}
