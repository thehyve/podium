/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.common.resource;

import nl.thehyve.podium.common.service.dto.AuditEventRepresentation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

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
}
