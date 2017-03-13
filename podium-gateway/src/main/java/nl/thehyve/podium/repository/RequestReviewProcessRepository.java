/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.repository;

import nl.thehyve.podium.domain.RequestReviewProcess;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for the RequestReviewProcess entity.
 */
@SuppressWarnings("unused")
public interface RequestReviewProcessRepository extends JpaRepository<RequestReviewProcess,Long> {

}
