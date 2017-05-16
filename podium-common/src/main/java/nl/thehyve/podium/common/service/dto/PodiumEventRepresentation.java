/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

package nl.thehyve.podium.common.service.dto;

import nl.thehyve.podium.common.event.EventType;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

public class PodiumEventRepresentation implements Serializable {
    private Long id;

    private String principal;

    private Date eventDate;
    private EventType eventType;

    private Map<String, String> data;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PodiumEventRepresentation podiumEventRepresentation = (PodiumEventRepresentation) o;

        if ( ! Objects.equals(id, podiumEventRepresentation.id)) { return false; }

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "PodiumEventRepresentation{" +
            "id=" + id +
            ", principal='" + principal + '\'' +
            ", eventDate=" + eventDate +
            ", eventType=" + eventType +
            ", data=" + data +
            '}';
    }
}
