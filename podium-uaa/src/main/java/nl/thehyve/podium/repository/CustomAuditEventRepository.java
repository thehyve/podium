/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.repository;

import nl.thehyve.podium.common.event.EventType;
import nl.thehyve.podium.config.audit.AuditEventConverter;
import nl.thehyve.podium.domain.PersistentAuditEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * An implementation of Spring Boot's AuditEventRepository.
 */
@Repository
public class CustomAuditEventRepository implements AuditEventRepository {

    private static final String AUTHORIZATION_FAILURE = "AUTHORIZATION_FAILURE";

    private static final String ANONYMOUS_USER = "anonymoususer";

    @Autowired
    private PersistenceAuditEventRepository persistenceAuditEventRepository;

    @Autowired
    private AuditEventConverter auditEventConverter;

    @Override
    public List<AuditEvent> find(Date after) {
        Iterable<PersistentAuditEvent> persistentAuditEvents =
            persistenceAuditEventRepository.findByEventDateAfter(after);
        return auditEventConverter.convertToAuditEvent(persistentAuditEvents);
    }

    @Override
    public List<AuditEvent> find(String principal, Date after) {
        Iterable<PersistentAuditEvent> persistentAuditEvents;
        if (principal == null && after == null) {
            persistentAuditEvents = persistenceAuditEventRepository.findAll();
        } else if (after == null) {
            persistentAuditEvents = persistenceAuditEventRepository.findByPrincipal(principal);
        } else {
            persistentAuditEvents =
                persistenceAuditEventRepository.findByPrincipalAndEventDateAfter(principal, after);
        }
        return auditEventConverter.convertToAuditEvent(persistentAuditEvents);
    }

    @Override
    public List<AuditEvent> find(String principal, Date after, String type) {
        EventType eventType = type == null ? null : EventType.fromName(type);
        return findByEventType(principal, after, eventType);
    }

    public List<AuditEvent> findByEventType(String principal, Date after, EventType type) {
        Iterable<PersistentAuditEvent> persistentAuditEvents =
            persistenceAuditEventRepository.findByPrincipalAndEventDateAfterAndEventType(principal, after, type);
        return auditEventConverter.convertToAuditEvent(persistentAuditEvents);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void add(AuditEvent event) {
        if (!AUTHORIZATION_FAILURE.equals(event.getType()) &&
            !ANONYMOUS_USER.equals(event.getPrincipal())) {

            PersistentAuditEvent persistentAuditEvent = new PersistentAuditEvent();
            persistentAuditEvent.setPrincipal(event.getPrincipal());
            persistentAuditEvent.setEventType(EventType.fromName(event.getType()));
            persistentAuditEvent.setEventDate(event.getTimestamp());
            persistentAuditEvent.setData(auditEventConverter.convertDataToStrings(event.getData()));
            persistenceAuditEventRepository.save(persistentAuditEvent);
        }
    }
}
