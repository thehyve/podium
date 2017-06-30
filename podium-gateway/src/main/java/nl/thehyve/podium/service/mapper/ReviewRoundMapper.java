/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

package nl.thehyve.podium.service.mapper;

import nl.thehyve.podium.common.service.SecurityService;
import nl.thehyve.podium.common.service.dto.RequestDetailRepresentation;
import nl.thehyve.podium.common.service.dto.ReviewFeedbackRepresentation;
import nl.thehyve.podium.common.service.dto.ReviewRoundRepresentation;
import nl.thehyve.podium.domain.ReviewRound;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapper for the entity ReviewRound and its DTO ReviewRoundRepresentation.
 */
@Mapper(componentModel = "spring", uses = {
    RequestDetailMapper.class,
    ReviewFeedbackMapper.class
})
public abstract class ReviewRoundMapper {

    @Autowired
    private ReviewFeedbackMapper reviewFeedbackMapper;

    @Autowired
    private SecurityService securityService;

    @Mapping(target = "requestDetail", ignore = true)
    public abstract  ReviewRound reviewRoundRepresentationToReviewRound(ReviewRoundRepresentation reviewRoundRepresentation);

    @Mapping(target = "requestDetail", ignore = true)
    public abstract ReviewRoundRepresentation reviewRoundToReviewRoundRepresentation(ReviewRound reviewRound);

    public ReviewRoundRepresentation reviewerReviewRoundToReviewRoundRepresentation(ReviewRound reviewRound) {
        if (reviewRound == null) {
            return null;
        }
        ReviewRoundRepresentation reviewRoundRepresentation = new ReviewRoundRepresentation();
        reviewRoundRepresentation.setId(reviewRound.getId());
        reviewRoundRepresentation.setUuid(reviewRound.getUuid());
        reviewRoundRepresentation.setStartDate(reviewRound.getStartDate());
        reviewRoundRepresentation.setEndDate(reviewRound.getEndDate());
        reviewRoundRepresentation.setInitiatedBy(reviewRound.getInitiatedBy());
        if (reviewRound.getReviewFeedback() != null) {
            UUID reviewerUuid = securityService.getCurrentUserUuid();
            reviewRoundRepresentation.setReviewFeedback(
                reviewRound.getReviewFeedback().stream()
                    .filter(reviewFeedback -> reviewFeedback.getReviewer().equals(reviewerUuid))
                    .map(reviewFeedbackMapper::reviewFeedbackToReviewFeedbackRepresentation)
                    .collect(Collectors.toList())
            );
        }
        return reviewRoundRepresentation;
    }

}
