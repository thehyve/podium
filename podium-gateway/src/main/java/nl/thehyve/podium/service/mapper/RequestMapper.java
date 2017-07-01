/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.service.mapper;

import nl.thehyve.podium.domain.Request;
import nl.thehyve.podium.common.service.dto.RequestRepresentation;
import nl.thehyve.podium.service.util.*;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Mapper for the entity Request and its DTO RequestDTO.
 */
@Mapper(componentModel = "spring", uses = {
    RequestDetailMapper.class,
    RequestReviewProcessMapper.class,
    UserMapperHelper.class,
    OrganisationMapperHelper.class,
    PodiumEventMapper.class,
    ReviewRoundMapper.class
})
public abstract class RequestMapper {

    @Autowired
    private RequestDetailMapper requestDetailMapper;


    @Autowired
    private OrganisationMapperHelper organisationMapperHelper;

    @DefaultRequest
    @Mappings({
        @Mapping(source = "requestDetail", target = "requestDetail", qualifiedBy = DefaultRequestDetail.class),
        @Mapping(source = "revisionDetail", target = "revisionDetail", qualifiedBy = DefaultRequestDetail.class),
        @Mapping(source = "requestReviewProcess", target = "requestReview"),
        @Mapping(target = "requester", qualifiedBy = DefaultUser.class),
        @Mapping(target = "organisations", qualifiedBy = DefaultOrganisation.class),
        @Mapping(target = "relatedRequests", qualifiedBy = MinimalRequest.class)
    })
    public abstract RequestRepresentation requestToRequestDTO(Request request);

    @DefaultRequest
    @IterableMapping(qualifiedBy = DefaultRequest.class)
    public abstract List<RequestRepresentation> requestsToRequestDTOs(List<Request> requests);

    @DefaultRequest
    @Mappings({
        @Mapping(source = "requestDetail", target = "requestDetail", qualifiedBy = DefaultRequestDetail.class),
        @Mapping(source = "revisionDetail", target = "revisionDetail", qualifiedBy = DefaultRequestDetail.class),
        @Mapping(target = "historicEvents", ignore = true),
        @Mapping(target = "relatedRequests", ignore = true)
    })
    public abstract Request requestDTOToRequest(RequestRepresentation requestDTO);

    @Mappings({
        @Mapping(source = "requestDetail", target = "requestDetail", qualifiedBy = DefaultRequestDetail.class),
        @Mapping(source = "revisionDetail", target = "revisionDetail", qualifiedBy = DefaultRequestDetail.class),
        @Mapping(target = "historicEvents", ignore = true),
        @Mapping(target = "relatedRequests", ignore = true)
    })
    public abstract Request updateRequestDTOToRequest(RequestRepresentation requestDTO, @MappingTarget Request request);

    @Mappings({
        @Mapping(source = "requestDetail", target = "requestDetail", qualifiedByName = "clone"),
        @Mapping(source = "revisionDetail", target = "revisionDetail", qualifiedByName = "clone"),
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "uuid", ignore = true),
        @Mapping(target = "historicEvents", ignore = true),
        @Mapping(target = "relatedRequests", ignore = true)
    })
    public abstract Request clone(Request request);

    @DefaultRequest
    @IterableMapping(qualifiedBy = DefaultRequest.class)
    public abstract List<Request> requestDTOsToRequests(List<RequestRepresentation> requestRepresentations);

    @ExtendedRequest
    @Mappings({
        @Mapping(source = "requestDetail", target = "requestDetail", qualifiedBy = DefaultRequestDetail.class),
        @Mapping(source = "revisionDetail", target = "revisionDetail", qualifiedBy = DefaultRequestDetail.class),
        @Mapping(source = "requestReviewProcess", target = "requestReview"),
        @Mapping(target = "organisations", qualifiedBy = ExtendedOrganisation.class),
        @Mapping(target = "relatedRequests", qualifiedBy = MinimalRequest.class),
        @Mapping(target = "requester", qualifiedBy = ExtendedUser.class)
    })
    public abstract RequestRepresentation extendedRequestToRequestDTO(Request request);

    @ExtendedRequest
    @IterableMapping(qualifiedBy = ExtendedRequest.class)
    public abstract List<RequestRepresentation> extendedRequestsToRequestDTOs(List<Request> requests);

    @MinimalRequest
    public RequestRepresentation minimalRequestToRequestDTO(Request request) {
        if ( request == null ) {
            return null;
        }
        RequestRepresentation requestRepresentation = new RequestRepresentation();
        requestRepresentation.setId(request.getId());
        requestRepresentation.setUuid(request.getUuid());
        requestRepresentation.setRequestDetail(requestDetailMapper.mapRequestTypeOnly(request.getRequestDetail()));
        requestRepresentation.setOrganisations(organisationMapperHelper.uuidsToExtendedOrganisationDTOs(request.getOrganisations()));
        return requestRepresentation;
    }

    @MinimalRequest
    @IterableMapping(qualifiedBy = MinimalRequest.class)
    public abstract List<RequestRepresentation> minimalRequestsToRequestDTOs(List<Request> requests);

}
