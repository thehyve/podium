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

import nl.thehyve.podium.common.service.dto.OrganisationRepresentation;
import nl.thehyve.podium.domain.Organisation;
import nl.thehyve.podium.service.util.UuidMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;

import java.util.List;

/**
 * Mapper for the entity Organisation and its OrganisationRepresentation.
 */
@Mapper(componentModel = "spring", uses = { UuidMapper.class })
public interface OrganisationMapper {
    OrganisationRepresentation organisationToOrganisationDTO(Organisation organisation);

    @Mappings({
        @Mapping(target = "uuid", ignore = true),
        @Mapping(target = "deleted", ignore = true),
        @Mapping(target = "activated", defaultValue = "false")
    })
    Organisation createOrganisationFromOrganisationDTO(OrganisationRepresentation organisationRepresentation);

    @Mappings({
        @Mapping(target = "uuid", ignore = true),
        @Mapping(target = "deleted", ignore = true),
        @Mapping(target = "activated", defaultValue = "false")
    })
    Organisation updateOrganisationFromOrganisationDTO(
        OrganisationRepresentation organisationRepresentation,
        @MappingTarget Organisation organisation
    );
}
