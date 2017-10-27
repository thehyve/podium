/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.domain;

import nl.thehyve.podium.common.domain.AbstractPodiumEvent;
import nl.thehyve.podium.common.event.AuthenticationEvent;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.HashMap;
import java.util.Map;

/**
 * Audit events to be stored in the database.
 */
@Entity
@Table(name = "podium_event")
@Document(indexName = "podium_event")
public class PersistentAuditEvent extends AbstractPodiumEvent {

    public PersistentAuditEvent(){}

    public PersistentAuditEvent(AuthenticationEvent event) {
        this.setPrincipal(event.getUsername());
        this.setEventType(event.getEventType());
        this.setEventDate(event.getEventDate());
        Map<String,String> data = new HashMap<>();
        data.put("userUuid", event.getUserUuid().toString());
        if (event.getHandlerUuid() != null) {
            data.put("handlerUuid", event.getHandlerUuid().toString());
        }

        if (event.getMessage() != null) {
            data.put("messageSummary", event.getMessage().getSummary());
            data.put("messageDescription", event.getMessage().getDescription());
        }
        this.setData(data);
    }
}
