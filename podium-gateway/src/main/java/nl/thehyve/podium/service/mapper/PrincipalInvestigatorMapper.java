/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.service.mapper;

import nl.thehyve.podium.common.service.dto.*;
import nl.thehyve.podium.domain.PrincipalInvestigator;
import org.mapstruct.*;

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

    @Named("clonePrincipalInvestigator")
    @Mapping(target = "id", ignore = true)
    PrincipalInvestigator clonePrincipalInvestigator(PrincipalInvestigator principalInvestigator);

    @Named("safePrincipalInvestigatorDTOToPrincipalInvestigator")
    @Mapping(target = "id", ignore = true)
    PrincipalInvestigator safePrincipalInvestigatorDTOToPrincipalInvestigator(PrincipalInvestigatorRepresentation principalInvestigator);
}
