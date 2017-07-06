/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.repository;

import com.codahale.metrics.annotation.Timed;
import nl.thehyve.podium.common.enumeration.RequestOutcome;
import nl.thehyve.podium.common.enumeration.RequestReviewStatus;
import nl.thehyve.podium.common.enumeration.RequestStatus;
import nl.thehyve.podium.domain.Request;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Spring Data JPA repository for the Request entity.
 */
@SuppressWarnings("unused")
@Timed
public interface RequestRepository extends JpaRepository<Request,Long> {

    Request findOneByUuid(UUID requestUuid);

    @Query(value = "select r from Request r where r.id" +
        " in (select r.id from Request r join r.organisations o" +
        " where not r.status = nl.thehyve.podium.common.enumeration.RequestStatus.Draft" +
        " and o in :organisations)")
    Page<Request> findAllByOrganisations(
        @Param("organisations") Set<UUID> organisations,
        Pageable pageable);

    @Query("select distinct r from Request r" +
        " join r.organisations o" +
        " where not r.status = nl.thehyve.podium.common.enumeration.RequestStatus.Draft" +
        " and r.status = :status" +
        " and o in :organisations")
    Page<Request> findAllByOrganisationsAndStatus(
        @Param("organisations") Set<UUID> organisations,
        @Param("status") RequestStatus status,
        Pageable pageable);

    @Query(value = "select r from Request r where r.id" +
        " in (select r.id from Request r join r.organisations o" +
        " where r.status = nl.thehyve.podium.common.enumeration.RequestStatus.Review" +
        " and r.requestReviewProcess.status = :requestReviewStatus" +
        " and o in :organisations)")
    Page<Request> findAllByOrganisationsAndRequestReviewStatus(
        @Param("organisations") Set<UUID> organisations,
        @Param("requestReviewStatus") RequestReviewStatus requestReviewStatus,
        Pageable pageable);

    @Query("select distinct r from Request r" +
        " join r.organisations o" +
        " where r.status = nl.thehyve.podium.common.enumeration.RequestStatus.Closed" +
        " and r.outcome = :outcome" +
        " and o in :organisations")
    Page<Request> findAllByOrganisationsAndOutcome(
        @Param("organisations") Set<UUID> organisations,
        @Param("outcome") RequestOutcome outcome,
        Pageable pageable);

    @Query("select count(distinct r) from Request r" +
        " join r.organisations o" +
        " where not r.status = nl.thehyve.podium.common.enumeration.RequestStatus.Draft" +
        " and o in :organisations")
    long countByOrganisations(
        @Param("organisations") Set<UUID> organisations);

    @Query("select new nl.thehyve.podium.repository.SummaryEntry(r.status, count(distinct r))" +
        " from Request r" +
        " join r.organisations o" +
        " where not r.status = nl.thehyve.podium.common.enumeration.RequestStatus.Draft" +
        " and o in :organisations" +
        " group by r.status")
    List<SummaryEntry<RequestStatus>> countByOrganisationsPerStatus(
        @Param("organisations") Set<UUID> organisations);

    @Query("select new nl.thehyve.podium.repository.SummaryEntry(r.requestReviewProcess.status, count(distinct r))" +
        " from Request r" +
        " join r.organisations o" +
        " where r.status = nl.thehyve.podium.common.enumeration.RequestStatus.Review" +
        " and o in :organisations" +
        " group by r.requestReviewProcess.status")
    List<SummaryEntry<RequestReviewStatus>> countByOrganisationsPerRequestReviewStatus(
        @Param("organisations") Set<UUID> organisations);

    @Query("select count(distinct r)" +
        " from Request r" +
        " join r.organisations o" +
        " where r.status = nl.thehyve.podium.common.enumeration.RequestStatus.Review" +
        " and r.requestReviewProcess.status = :reviewStatus" +
        " and o in :organisations")
    long countByOrganisationsAndRequestReviewStatus(
        @Param("organisations") Set<UUID> organisations,
        @Param("reviewStatus") RequestReviewStatus reviewStatus);

    @Query("select new nl.thehyve.podium.repository.SummaryEntry(r.outcome, count(distinct r))" +
        " from Request r" +
        " join r.organisations o" +
        " where r.status = nl.thehyve.podium.common.enumeration.RequestStatus.Closed" +
        " and o in :organisations" +
        " group by r.outcome")
    List<SummaryEntry<RequestOutcome>> countByOrganisationsPerOutcome(
        @Param("organisations") Set<UUID> organisations);

    Page<Request> findAllByRequester(UUID requesterUuid, Pageable pageable);

    Page<Request> findAllByRequesterAndStatus(UUID requesterUuid, RequestStatus status, Pageable pageable);

    @Query("select distinct r from Request r" +
        " where r.requester = :requester" +
        " and r.status = nl.thehyve.podium.common.enumeration.RequestStatus.Review" +
        " and r.requestReviewProcess.status = :requestReviewStatus")
    Page<Request> findAllByRequesterAndRequestReviewStatus(
        @Param("requester") UUID requesterUuid,
        @Param("requestReviewStatus") RequestReviewStatus status,
        Pageable pageable);

    @Query("select distinct r from Request r" +
        " where r.requester = :requester" +
        " and r.status = nl.thehyve.podium.common.enumeration.RequestStatus.Closed" +
        " and r.outcome = :outcome")
    Page<Request> findAllByRequesterAndOutcome(
        @Param("requester") UUID requesterUuid,
        @Param("outcome") RequestOutcome outcome,
        Pageable pageable);

    long countByRequester(
        @Param("requester") UUID requesterUuid);

    @Query("select new nl.thehyve.podium.repository.SummaryEntry(r.status, count(distinct r))" +
        " from Request r" +
        " where r.requester = :requester" +
        " group by r.status")
    List<SummaryEntry<RequestStatus>> countByRequesterPerStatus(
        @Param("requester") UUID requesterUuid);

    @Query("select new nl.thehyve.podium.repository.SummaryEntry(r.requestReviewProcess.status, count(distinct r))" +
        " from Request r" +
        " where r.requester = :requester" +
        " and r.status = nl.thehyve.podium.common.enumeration.RequestStatus.Review" +
        " group by r.requestReviewProcess.status")
    List<SummaryEntry<RequestReviewStatus>> countByRequesterPerRequestReviewStatus(
        @Param("requester") UUID requesterUuid);

    @Query("select new nl.thehyve.podium.repository.SummaryEntry(r.outcome, count(distinct r))" +
        " from Request r" +
        " where r.requester = :requester" +
        " and r.status = nl.thehyve.podium.common.enumeration.RequestStatus.Closed" +
        " group by r.outcome")
    List<SummaryEntry<RequestOutcome>> countByRequesterPerOutcome(
        @Param("requester") UUID requesterUuid);

}
