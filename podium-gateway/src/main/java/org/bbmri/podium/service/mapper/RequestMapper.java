package org.bbmri.podium.service.mapper;

import org.bbmri.podium.domain.*;
import org.bbmri.podium.common.service.dto.OrganisationDTO;
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

    @Mapping(source = "parentRequest", target = "parentRequest")
    @Mapping(source = "requestDetail", target = "requestDetail")
    RequestDTO requestToRequestDTO(Request request);

    List<RequestDTO> requestsToRequestDTOs(List<Request> requests);

    @Mapping(source = "parentRequest", target = "parentRequest")
    @Mapping(source = "requestDetail", target = "requestDetail")
    Request requestDTOToRequest(RequestDTO requestDTO);

    List<Request> requestDTOsToRequests(List<RequestDTO> requestDTOs);

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
