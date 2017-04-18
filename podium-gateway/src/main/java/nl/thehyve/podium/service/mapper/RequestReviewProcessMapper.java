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

import nl.thehyve.podium.domain.RequestReviewProcess;
import nl.thehyve.podium.service.representation.RequestReviewRepresentation;
import org.mapstruct.Mapper;

/**
 * Created by bernd on 18/04/2017.
 */
@Mapper(componentModel = "spring")
public interface RequestReviewProcessMapper {
    RequestReviewRepresentation requestReviewProcessToRequestReviewProcessDTO(RequestReviewProcess requestReviewProcess);
}
