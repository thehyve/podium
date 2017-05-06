/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.service.mapper;

import nl.thehyve.podium.domain.Request;
import nl.thehyve.podium.service.representation.RequestRepresentation;
import nl.thehyve.podium.service.util.DefaultOrganisation;
import nl.thehyve.podium.service.util.DefaultRequest;
import nl.thehyve.podium.service.util.DefaultRequestDetail;
import nl.thehyve.podium.service.util.ExtendedOrganisation;
import nl.thehyve.podium.service.util.ExtendedRequest;
import nl.thehyve.podium.service.util.OrganisationMapperHelper;
import nl.thehyve.podium.service.util.SafeRequestDetail;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * Mapper for the entity Request and its DTO RequestDTO.
 */
@Mapper(componentModel = "spring", uses = {
    RequestDetailMapper.class,
    RequestReviewProcessMapper.class,
    OrganisationMapperHelper.class
})
public interface RequestMapper {

    @DefaultRequest
    @Mapping(source = "requestDetail", target = "requestDetail", qualifiedBy = DefaultRequestDetail.class)
    @Mapping(source = "requestReviewProcess", target = "requestReview")
    @Mapping(target = "organisations", qualifiedBy = DefaultOrganisation.class)
    RequestRepresentation requestToRequestDTO(Request request);

    @DefaultRequest
    @IterableMapping(qualifiedBy = DefaultRequest.class)
    List<RequestRepresentation> requestsToRequestDTOs(List<Request> requests);

    @DefaultRequest
    @Mapping(source = "requestDetail", target = "requestDetail", qualifiedBy = DefaultRequestDetail.class)
    Request requestDTOToRequest(RequestRepresentation requestDTO);

    @Mapping(source = "requestDetail", target = "requestDetail", qualifiedBy = DefaultRequestDetail.class)
    Request updateRequestDTOToRequest(RequestRepresentation requestDTO, @MappingTarget Request request);

    @Mapping(source = "requestDetail", target = "requestDetail", qualifiedByName = "clone")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    Request clone(Request request);

    @DefaultRequest
    @IterableMapping(qualifiedBy = DefaultRequest.class)
    List<Request> requestDTOsToRequests(List<RequestRepresentation> requestRepresentations);

    @ExtendedRequest
    @Mapping(source = "requestDetail", target = "requestDetail", qualifiedBy = DefaultRequestDetail.class)
    @Mapping(source = "requestReviewProcess", target = "requestReview")
    @Mapping(target = "organisations", qualifiedBy = ExtendedOrganisation.class)
    RequestRepresentation extendedRequestToRequestDTO(Request request);

    @IterableMapping(qualifiedBy = ExtendedRequest.class)
    List<RequestRepresentation> requestsToExtendedRequestDTOs(List<Request> requests);

    @Mapping(source = "requestDetail", target = "requestDetail", qualifiedBy = SafeRequestDetail.class)
    Request safeUpdateRequestRepresentationToRequest(
        RequestRepresentation requestRepresentation,
        @MappingTarget Request request
    );

}
