/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.common.resource;

import nl.thehyve.podium.common.event.EventType;
import nl.thehyve.podium.common.service.dto.AuditEventRepresentation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RequestMapping("/internal")
public interface InternalAuditResource {

    /**
     * POST  /audit/events
     * Publishes an audit event to the event server.
     *
     * @param event the event data
     */
    @RequestMapping(value = "/audit/events", method = RequestMethod.POST)
    ResponseEntity add(@RequestBody AuditEventRepresentation event);

    /**
     *
     * @param principal
     * @param after
     * @param type
     * @return
     */
    @RequestMapping(value = "/audit/events/{principal}", method = RequestMethod.GET)
    ResponseEntity<List<AuditEventRepresentation>> find(
        @PathVariable("principal") String principal,
        @RequestParam("after") Date after,
        @RequestParam("type") EventType type);

}
