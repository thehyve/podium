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

import nl.thehyve.podium.common.service.dto.ReviewRoundRepresentation;
import nl.thehyve.podium.domain.ReviewRound;
import nl.thehyve.podium.service.util.DefaultRequestDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for the entity ReviewRound and its DTO ReviewRoundRepresentation.
 */
@Mapper(componentModel = "spring", uses = {
    RequestDetailMapper.class,
    ReviewFeedbackMapper.class
})
public interface ReviewRoundMapper {

    @Mapping(source = "requestDetail", target = "requestDetail", qualifiedBy = DefaultRequestDetail.class)
    ReviewRound reviewRoundRepresentationToReviewRound(ReviewRoundRepresentation reviewRoundRepresentation);

    @Mapping(source = "requestDetail", target = "requestDetail", qualifiedBy = DefaultRequestDetail.class)
    ReviewRoundRepresentation reviewRoundToReviewRoundRepresentation(ReviewRound reviewRound);
}
