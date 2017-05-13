/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.web.rest;

import com.codahale.metrics.annotation.Timed;
import nl.thehyve.podium.common.enumeration.RequestStatus;
import nl.thehyve.podium.common.enumeration.Status;
import nl.thehyve.podium.common.resource.InternalAuditResource;
import nl.thehyve.podium.common.security.annotations.Public;
import nl.thehyve.podium.common.security.annotations.RequestUuidParameter;
import nl.thehyve.podium.common.service.dto.AuditEventRepresentation;
import nl.thehyve.podium.service.AuditEventService;
import nl.thehyve.podium.service.mapper.AuditEventMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

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

    @Autowired
    private AuditEventMapper auditEventMapper;

    @Override
    @Timed
    public ResponseEntity add(@RequestBody AuditEventRepresentation event) {
        auditEventService.add(event.asAuditEvent());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    @Timed
    public ResponseEntity getLatestRequestStatusChangeEventForStatus(
        @RequestUuidParameter @PathVariable("uuid") UUID uuid,
        @PathVariable("status") RequestStatus status
    ) {
        log.debug("Internal audit server for request {} - status {} ", uuid, status);
        AuditEvent auditEvent = auditEventService.findByRequestUuidAndTargetStatus(uuid, status);
        AuditEventRepresentation auditEventRepresentation = auditEventMapper.auditEventToAuditEventRepresentation(auditEvent);
        return new ResponseEntity<>(auditEventRepresentation, HttpStatus.OK);
    }
}
