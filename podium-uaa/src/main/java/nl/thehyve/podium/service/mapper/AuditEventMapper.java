/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

package nl.thehyve.podium.service.mapper;

import nl.thehyve.podium.common.service.dto.AuditEventRepresentation;
import org.mapstruct.Mapper;
import org.springframework.boot.actuate.audit.AuditEvent;

import java.util.List;

@Mapper(componentModel = "spring", uses = { })
public interface AuditEventMapper {

    AuditEventRepresentation auditEventToAuditEventRepresentation(AuditEvent auditEvent);

    List<AuditEventRepresentation> auditEventsToAuditEventRepresentations(List<AuditEvent> auditEventList);
}
