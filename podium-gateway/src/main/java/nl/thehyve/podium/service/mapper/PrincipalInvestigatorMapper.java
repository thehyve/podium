/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.service.mapper;

import nl.thehyve.podium.common.service.dto.PrincipalInvestigatorRepresentation;
import nl.thehyve.podium.domain.PrincipalInvestigator;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * Mapper for the entity PrincipalInvestigator and its DTO PrincipalInvestigatorRepresentation.
 */
@Mapper(componentModel = "spring", uses = {})
public interface PrincipalInvestigatorMapper {

    PrincipalInvestigatorRepresentation principalInvestigatorToPrincipalInvestigatorDTO(PrincipalInvestigator principalInvestigator);

    List<PrincipalInvestigatorRepresentation> principalInvestigatorsToPrincipalInvestigatorDTOs(
        List<PrincipalInvestigator> principalInvestigators
    );

    PrincipalInvestigator principalInvestigatorDTOToPrincipalInvestigator(
        PrincipalInvestigatorRepresentation principalInvestigatorDTO
    );

    List<PrincipalInvestigator> principalInvestigatorDTOsToPrincipalInvestigators(
        List<PrincipalInvestigatorRepresentation> principalInvestigatorDTOs
    );

    PrincipalInvestigator updatePrincipalInvestigatorDTOToPrincipalInvestigator(
        PrincipalInvestigatorRepresentation principalInvestigatorRepresentation,
        @MappingTarget PrincipalInvestigator principalInvestigator
    );

    @Mapping(target = "id", ignore = true)
    PrincipalInvestigator clone(PrincipalInvestigator principalInvestigator);

}
