package org.bbmri.podium.service.mapper;

import org.bbmri.podium.common.domain.Organisation;
import org.bbmri.podium.domain.*;
import org.bbmri.podium.service.dto.RequestDTO;

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

    @Mapping(source = "parentRequest.id", target = "parentRequestId")
    @Mapping(source = "requestDetail.id", target = "requestDetailId")
    RequestDTO requestToRequestDTO(Request request);

    List<RequestDTO> requestsToRequestDTOs(List<Request> requests);

    @Mapping(source = "parentRequestId", target = "parentRequest")
    @Mapping(source = "requestDetailId", target = "requestDetail")
    Request requestDTOToRequest(RequestDTO requestDTO);

    List<Request> requestDTOsToRequests(List<RequestDTO> requestDTOs);

    default List<UUID> uuidsFromOrganisations (List<Organisation> organisations) {
        return organisations.stream().map(Organisation::getUuid)
            .collect(Collectors.toList());
    }

    default Organisation organisationFromUUID(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        Organisation organisation = new Organisation();
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
