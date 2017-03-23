/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.service.mapper;

import nl.thehyve.podium.domain.RequestDetail;
import nl.thehyve.podium.service.representation.RequestDetailRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = { PrincipalInvestigatorMapper.class })
public interface RequestDetailMapper {

    RequestDetailRepresentation requestDetailToRequestDetailRepresentation(RequestDetail requestDetail);

    RequestDetail requestDetailRepresentationToRequestDetail(RequestDetailRepresentation requestDetailRepresentation);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "principalInvestigator", target = "principalInvestigator", qualifiedByName = "clone")
    RequestDetail clone(RequestDetail requestDetail);
}
