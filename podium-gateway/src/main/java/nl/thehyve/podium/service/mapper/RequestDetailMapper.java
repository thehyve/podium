/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.service.mapper;

import nl.thehyve.podium.common.enumeration.RequestType;
import nl.thehyve.podium.domain.RequestDetail;
import nl.thehyve.podium.common.service.dto.RequestDetailRepresentation;
import nl.thehyve.podium.service.util.DefaultRequestDetail;
import nl.thehyve.podium.service.util.MinimalRequest;
import nl.thehyve.podium.service.util.SafeRequestDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;

import java.util.HashSet;

@Mapper(componentModel = "spring", uses = { PrincipalInvestigatorMapper.class })
public abstract class RequestDetailMapper {

    @DefaultRequestDetail
    public abstract RequestDetailRepresentation requestDetailToRequestDetailRepresentation(RequestDetail requestDetail);

    @MinimalRequest
    public RequestDetailRepresentation mapRequestTypeOnly(RequestDetail requestDetail) {
        RequestDetailRepresentation result = new RequestDetailRepresentation();
        if (requestDetail.getRequestType() != null) {
            result.setRequestType(new HashSet<>(requestDetail.getRequestType()));
        }
        return result;
    }

    @DefaultRequestDetail
    public abstract RequestDetail requestDetailRepresentationToRequestDetail(RequestDetailRepresentation requestDetailRepresentation);

    @DefaultRequestDetail
    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(source = "principalInvestigator", target = "principalInvestigator", qualifiedByName = "clone")
    })
    public abstract RequestDetail clone(RequestDetail requestDetail);

    /**
     * Safely transform requestDetail representation to a requestDetail entity
     * @param requestDetailRepresentation data to be mapped
     * @param requestDetail entity to map to
     * @return the mapping target
     */
    @SafeRequestDetail
    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "requestType", ignore = true),
        @Mapping(target = "combinedRequest", ignore = true),
        @Mapping(source = "principalInvestigator", target = "principalInvestigator", qualifiedByName = "clone")
    })
    public abstract RequestDetail processingRequestDetailDtoToRequestDetail(
        RequestDetailRepresentation requestDetailRepresentation, @MappingTarget RequestDetail requestDetail
    );
}
