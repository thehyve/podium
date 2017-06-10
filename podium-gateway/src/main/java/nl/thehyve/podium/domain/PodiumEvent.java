/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.domain;


import nl.thehyve.podium.common.domain.AbstractPodiumEvent;
import nl.thehyve.podium.common.event.EventType;
import nl.thehyve.podium.common.event.StatusUpdateEvent;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Application events to be stored in the database. E.g., status updates.
 */
@Entity
@Table(name = "podium_event")
public class PodiumEvent extends AbstractPodiumEvent {

    public PodiumEvent() {

    }

    public PodiumEvent(StatusUpdateEvent event) {
        this.setPrincipal(event.getUsername());
        this.setEventType(EventType.Status_Change);
        this.setEventDate(event.getEventDate());
        Map<String,String> data = new HashMap<>();
        data.put("requestUuid", event.getRequestUuid().toString());
        if (event.getDeliveryProcessUuid() != null) {
            data.put("deliveryProcessUuid", event.getDeliveryProcessUuid().toString());
        }
        data.put("sourceStatus", event.getSourceStatus().toString());
        data.put("targetStatus", event.getTargetStatus().toString());
        if (event.getMessage() != null) {
            data.put("messageSummary", event.getMessage().getSummary());
            data.put("messageDescription", event.getMessage().getDescription());
        }
        this.setData(data);
    }
}
