/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.repository;

import nl.thehyve.podium.common.event.EventType;
import nl.thehyve.podium.domain.PersistentAuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

/**
 * Spring Data JPA repository for the PersistentAuditEvent entity.
 */
public interface PersistenceAuditEventRepository extends JpaRepository<PersistentAuditEvent, Long> {

    List<PersistentAuditEvent> findByPrincipal(String principal);

    List<PersistentAuditEvent> findByEventDateAfter(Date after);

    List<PersistentAuditEvent> findByPrincipalAndEventDateAfter(String principal, Date after);

    List<PersistentAuditEvent> findByPrincipalAndEventDateAfterAndEventType(String principle, Date after, EventType type);

    Page<PersistentAuditEvent> findAllByEventDateBetween(Date fromDate, Date toDate, Pageable pageable);

    @Query("select e from PersistentAuditEvent e" +
        " join e.data d" +
        " where e.eventType = nl.thehyve.podium.common.event.EventType.Status_Change" +
        " and (KEY(d) = 'requestUuid' and d = :uuid)" +
        " and (KEY(d) = 'targetStatus' and d = :status)" +
        " order by e.id DESC")
    List<PersistentAuditEvent> findOneLatestOfEventTypeByRequestUuidAndStatus(
        @Param("uuid") String uuid,
        @Param("status") String status,
        Pageable pageable);
}
