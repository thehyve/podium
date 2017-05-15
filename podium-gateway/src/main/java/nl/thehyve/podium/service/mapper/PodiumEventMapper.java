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

import nl.thehyve.podium.common.service.dto.PodiumEventRepresentation;
import nl.thehyve.podium.domain.PodiumEvent;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = { })
public interface PodiumEventMapper {
    PodiumEventRepresentation podiumEventToPodiumEventRepresentation(PodiumEvent podiumEvent);
}
