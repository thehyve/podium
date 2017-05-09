/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.service.mapper;

import nl.thehyve.podium.domain.Attachment;
import nl.thehyve.podium.domain.Request;
import nl.thehyve.podium.domain.RequestDetail;
import nl.thehyve.podium.service.representation.RequestRepresentation;
import nl.thehyve.podium.service.util.DefaultOrganisation;
import nl.thehyve.podium.service.util.DefaultRequest;
import nl.thehyve.podium.service.util.ExtendedOrganisation;
import nl.thehyve.podium.service.util.ExtendedRequest;
import nl.thehyve.podium.service.util.OrganisationMapperHelper;
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
    @Mapping(source = "requestDetail", target = "requestDetail")
    @Mapping(source = "requestReviewProcess", target = "requestReview")
    @Mapping(target = "organisations", qualifiedBy = DefaultOrganisation.class)
    RequestRepresentation requestToRequestDTO(Request request);

    @IterableMapping(qualifiedBy = DefaultRequest.class)
    List<RequestRepresentation> requestsToRequestDTOs(List<Request> requests);

    @Mapping(source = "requestDetail", target = "requestDetail")
    @Mapping(target = "historicEvents", ignore = true)
    Request requestDTOToRequest(RequestRepresentation requestDTO);

    @Mapping(source = "requestDetail", target = "requestDetail")
    @Mapping(target = "historicEvents", ignore = true)
    Request updateRequestDTOToRequest(RequestRepresentation requestDTO, @MappingTarget Request request);

    @Mapping(source = "requestDetail", target = "requestDetail", qualifiedByName = "clone")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "historicEvents", ignore = true)
    Request clone(Request request);

    List<Request> requestDTOsToRequests(List<RequestRepresentation> requestRepresentations);

    @ExtendedRequest
    @Mapping(source = "requestDetail", target = "requestDetail")
    @Mapping(source = "requestReviewProcess", target = "requestReview")
    @Mapping(target = "organisations", qualifiedBy = ExtendedOrganisation.class)
    RequestRepresentation extendedRequestToRequestDTO(Request request);

    @IterableMapping(qualifiedBy = ExtendedRequest.class)
    List<RequestRepresentation> requestsToExtendedRequestDTOs(List<Request> requests);

    default Request requestFromId(Long id) {
        if (id == null) {
            return null;
        }
        Request request = new Request();
        request.setId(id);
        return request;
    }

    default RequestDetail requestDetailFromId(Long id) {
        if (id == null) {
            return null;
        }
        RequestDetail requestDetail = new RequestDetail();
        requestDetail.setId(id);
        return requestDetail;
    }

    default Attachment attachmentFromId(Long id) {
        if (id == null) {
            return null;
        }
        Attachment attachment = new Attachment();
        attachment.setId(id);
        return attachment;
    }
}
