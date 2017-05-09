/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.web.rest;

import com.codahale.metrics.annotation.Timed;
import nl.thehyve.podium.common.resource.InternalAuditResource;
import nl.thehyve.podium.common.security.annotations.Public;
import nl.thehyve.podium.common.service.dto.AuditEventRepresentation;
import nl.thehyve.podium.service.AuditEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for auditing.
 */
@RestController
@Public
public class InternalAuditServer implements InternalAuditResource {

    private final Logger log = LoggerFactory.getLogger(InternalAuditServer.class);

    private AuditEventService auditEventService;

    @Autowired
    public InternalAuditServer(AuditEventService auditEventService) {
        this.auditEventService = auditEventService;
    }

    @Override
    @Timed
    public ResponseEntity add(@RequestBody AuditEventRepresentation event) {
        auditEventService.add(event.asAuditEvent());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
