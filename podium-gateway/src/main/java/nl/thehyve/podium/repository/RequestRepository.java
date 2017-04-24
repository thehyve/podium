/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.repository;

import nl.thehyve.podium.common.enumeration.RequestStatus;
import nl.thehyve.podium.domain.Request;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Spring Data JPA repository for the Request entity.
 */
@SuppressWarnings("unused")
public interface RequestRepository extends JpaRepository<Request,Long> {

    Request findOneByUuid(UUID requestUuid);

    Page<Request> findAllByRequesterAndStatus(UUID requesterUuid, RequestStatus status, Pageable pageable);

    Page<Request> findAllByRequester(UUID requesterUuid, Pageable pageable);

}
