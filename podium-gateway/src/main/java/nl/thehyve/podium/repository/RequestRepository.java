/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.repository;

import nl.thehyve.podium.domain.Request;

import nl.thehyve.podium.domain.enumeration.RequestStatus;
import org.springframework.data.jpa.repository.*;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for the Request entity.
 */
@SuppressWarnings("unused")
public interface RequestRepository extends JpaRepository<Request,Long> {
    List<Request> findAllByRequesterAndStatus(UUID requester, RequestStatus status);

}
