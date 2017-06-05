/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

package nl.thehyve.podium.service.util;

import nl.thehyve.podium.common.service.dto.UserRepresentation;
import nl.thehyve.podium.service.UserClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserMapperHelper {

    @Autowired
    UserClientService userClientService;

    public UUID userRepresentationtoUuid(UserRepresentation userRepresentation) {
        return userRepresentation.getUuid();
    }

    @DefaultUser
    public UserRepresentation uuidToUserRepresentation(UUID uuid) {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUuid(uuid);
        return userRepresentation;
    }

    @ExtendedUser
    public UserRepresentation uuidToRemoteUserRepresentation(UUID uuid) {
        if (uuid == null) {
            return null;
        }

        UserRepresentation userRepresentation = new UserRepresentation();

        try {
            userRepresentation = userClientService.findUserByUuid(uuid);
        } catch (Exception ex) {
        }

        return userRepresentation;
    }

}
