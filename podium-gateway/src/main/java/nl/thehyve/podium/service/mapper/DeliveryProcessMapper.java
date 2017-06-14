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

import nl.thehyve.podium.common.service.dto.DeliveryProcessRepresentation;
import nl.thehyve.podium.domain.DeliveryProcess;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {PodiumEventMapper.class})
public interface DeliveryProcessMapper {

    DeliveryProcessRepresentation deliveryProcessToDeliveryProcessRepresentation(DeliveryProcess deliveryProcess);

}
