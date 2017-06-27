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

import nl.thehyve.podium.common.service.dto.ReviewFeedbackRepresentation;
import nl.thehyve.podium.domain.ReviewFeedback;
import nl.thehyve.podium.service.util.ExtendedUser;
import nl.thehyve.podium.service.util.UserMapperHelper;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;

/**
 * Mapper for the entity ReviewFeedback and its DTO ReviewFeedbackRepresentation.
 */
@Mapper(componentModel = "spring", uses = { UserMapperHelper.class })
public interface ReviewFeedbackMapper {

    @Mappings({
        @Mapping(source = "message.summary", target = "summary"),
        @Mapping(source = "message.description", target = "description"),
        @Mapping(target = "reviewer", ignore = true)
    })
    ReviewFeedback reviewFeedbackRepresentationToReviewFeedback(ReviewFeedbackRepresentation reviewFeedbackRepresentation);

    @Mappings({
        @Mapping(source = "summary", target = "message.summary"),
        @Mapping(source = "description", target = "message.description"),
        @Mapping(target = "reviewer", qualifiedBy = ExtendedUser.class)
    })
    ReviewFeedbackRepresentation reviewFeedbackToReviewFeedbackRepresentation(ReviewFeedback reviewFeedback);

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "uuid", ignore = true),
        @Mapping(target = "reviewer", ignore = true),
        @Mapping(target = "date", ignore = true),
        @Mapping(source = "message.summary", target = "summary"),
        @Mapping(source = "message.description", target = "description")
    })
    ReviewFeedback safeUpdateReviewFeedbackFromDTO(
        ReviewFeedbackRepresentation reviewFeedbackRepresentation,
        @MappingTarget ReviewFeedback reviewFeedback
    );
}
