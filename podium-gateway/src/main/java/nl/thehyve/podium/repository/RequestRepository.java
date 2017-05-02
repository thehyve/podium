/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.repository;

import nl.thehyve.podium.common.enumeration.RequestReviewStatus;
import nl.thehyve.podium.common.enumeration.RequestStatus;
import nl.thehyve.podium.domain.Request;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;
import java.util.UUID;

/**
 * Spring Data JPA repository for the Request entity.
 */
@SuppressWarnings("unused")
public interface RequestRepository extends JpaRepository<Request,Long> {

    Request findOneByUuid(UUID requestUuid);

    Page<Request> findAllByRequesterAndStatus(UUID requesterUuid, RequestStatus status, Pageable pageable);

    @Query("select distinct r from Request r" +
        " join r.organisations o" +
        " where r.status = :status" +
        " and o in :organisations")
    Page<Request> findAllByStatusAndOrganisations(
        @Param("status") RequestStatus status,
        @Param("organisations") Set<UUID> organisations,
        Pageable pageable);

    @Query("select from Request r" +
        " join r.organisations o" +
        " where r.requestReviewProcess.status = :requestReviewStatus" +
        " and o in :organisations")
    Page<Request> findAllByRequestReviewStatusAndOrganisations(
        @Param("requestReviewStatus") RequestReviewStatus requestReviewStatus,
        @Param("organisations") Set<UUID> organisations,
        Pageable pageable);

    Page<Request> findAllByRequester(UUID requesterUuid, Pageable pageable);

}
