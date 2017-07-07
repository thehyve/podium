/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

package nl.thehyve.podium.repository;

import nl.thehyve.podium.domain.ReviewFeedback;
import nl.thehyve.podium.domain.ReviewRound;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for the ReviewFeedback entity.
 */
public interface ReviewFeedbackRepository extends JpaRepository<ReviewFeedback, Long> {
    ReviewFeedback findOneByUuid(UUID reviewFeedbackUuid);

    @Query("select distinct(rf) from ReviewRound rr join rr.reviewFeedback rf where rr.uuid = :reviewRoundUuid")
    List<ReviewFeedback> findAllByReviewRoundUuid(@Param("reviewRoundUuid") UUID reviewRoundUuid);

}
