/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.repository;


import com.codahale.metrics.annotation.Timed;
import nl.thehyve.podium.domain.Request;
import nl.thehyve.podium.domain.RequestFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for the RequestFile entity.
 */
@SuppressWarnings("unused")
@Timed
public interface RequestFileRepository extends JpaRepository<RequestFile, Long> {

    RequestFile findOneByUuid(UUID requestFileUuid);

    RequestFile findOneByUuidAndDeletedFalse(UUID requestFileUuid);

    List<RequestFile> findDistinctByRequestAndDeletedFalse(Request request);

    List<RequestFile> findDistinctByRequest(Request request);

}
