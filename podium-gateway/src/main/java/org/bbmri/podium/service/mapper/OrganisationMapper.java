package org.bbmri.podium.service.mapper;

import org.bbmri.podium.common.domain.Organisation;
import org.bbmri.podium.domain.*;
import org.bbmri.podium.service.dto.OrganisationDTO;

import org.mapstruct.*;
import java.util.List;

/**
 * Mapper for the entity Organisation and its DTO OrganisationDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface OrganisationMapper {

    OrganisationDTO organisationToOrganisationDTO(Organisation organisation);

    List<OrganisationDTO> organisationsToOrganisationDTOs(List<Organisation> organisations);

    Organisation organisationDTOToOrganisation(OrganisationDTO organisationDTO);

    List<Organisation> organisationDTOsToOrganisations(List<OrganisationDTO> organisationDTOs);
}
