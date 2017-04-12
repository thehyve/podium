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

import nl.thehyve.podium.common.service.dto.OrganisationDTO;
import nl.thehyve.podium.domain.Organisation;
import nl.thehyve.podium.search.SearchOrganisation;
import nl.thehyve.podium.service.util.UuidMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;

import java.util.List;

/**
 * Mapper for the entity Organisation and its OrganisationDTO.
 */
@Mapper(componentModel = "spring", uses = { UuidMapper.class })
public interface OrganisationMapper {
    OrganisationDTO organisationToOrganisationDTO(Organisation organisation);

    List<OrganisationDTO> organisationsToOrganisationDTOs(List<Organisation> organisations);

    @Mappings({
        @Mapping(target = "uuid", ignore = true),
        @Mapping(target = "deleted", ignore = true),
        @Mapping(target = "activated", defaultValue = "false")
    })
    Organisation createOrganisationFromOrganisationDTO(OrganisationDTO organisationDTO);

    @Mappings({
        @Mapping(target = "uuid", ignore = true),
        @Mapping(target = "deleted", ignore = true),
        @Mapping(target = "activated", defaultValue = "false")
    })
    Organisation updateOrganisationFromOrganisationDTO(
        OrganisationDTO organisationDTO,
        @MappingTarget Organisation organisation
    );

    List<Organisation> createOrganisationsFromOrganisationDTOs(List<OrganisationDTO> organisationDTOs);

    SearchOrganisation organisationToSearchOrganisation(Organisation organisation);

    List<SearchOrganisation> organisationsToSearchOrganisations(List<Organisation> organisations);
}
