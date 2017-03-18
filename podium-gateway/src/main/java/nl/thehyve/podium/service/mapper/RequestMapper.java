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
import nl.thehyve.podium.common.service.dto.OrganisationDTO;
import nl.thehyve.podium.service.representation.RequestRepresentation;

import org.mapstruct.*;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapper for the entity Request and its DTO RequestDTO.
 */
@Mapper(componentModel = "spring", uses = { })
public interface RequestMapper {

    @Mapping(source = "parentRequest", target = "parentRequest")
    @Mapping(source = "requestDetail", target = "requestDetail")
    RequestRepresentation requestToRequestDTO(Request request);

    List<RequestRepresentation> requestsToRequestDTOs(List<Request> requests);

    @Mapping(source = "parentRequest", target = "parentRequest")
    @Mapping(source = "requestDetail", target = "requestDetail")
    Request requestDTOToRequest(RequestRepresentation requestDTO);

    List<Request> requestDTOsToRequests(List<RequestRepresentation> requestDTOs);

    default Set<UUID> uuidsFromOrganisations (List<OrganisationDTO> organisations) {
        return organisations.stream().map(OrganisationDTO::getUuid)
            .collect(Collectors.toSet());
    }

    default OrganisationDTO organisationFromUUID(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        OrganisationDTO organisation = new OrganisationDTO();
        organisation.setUuid(uuid);
        return organisation;
    }

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
