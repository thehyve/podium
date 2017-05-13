/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

package nl.thehyve.podium.service;

import nl.thehyve.podium.client.InternalAuditClient;
import nl.thehyve.podium.common.enumeration.RequestStatus;
import nl.thehyve.podium.common.service.dto.AuditEventRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PodiumEventService {
    private Logger log = LoggerFactory.getLogger(AuditService.class);

    @Autowired
    private InternalAuditClient internalAuditClient;

    public ResponseEntity<AuditEventRepresentation> findLatestEventForRequestByStatus(UUID uuid, RequestStatus status) {
        return internalAuditClient.getLatestRequestStatusChangeEventForStatus(uuid, status);
    }
}
