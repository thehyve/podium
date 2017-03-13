/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.service.mapper;

import nl.thehyve.podium.domain.PrincipalInvestigator;
import nl.thehyve.podium.service.representation.PrincipalInvestigatorRepresentation;

import org.mapstruct.*;
import java.util.List;

/**
 * Mapper for the entity PrincipalInvestigator and its DTO PrincipalInvestigatorDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface PrincipalInvestigatorMapper {

    PrincipalInvestigatorRepresentation principalInvestigatorToPrincipalInvestigatorDTO(PrincipalInvestigator principalInvestigator);

    List<PrincipalInvestigatorRepresentation> principalInvestigatorsToPrincipalInvestigatorDTOs(List<PrincipalInvestigator> principalInvestigators);

    @Mapping(target = "requestDetail", ignore = true)
    PrincipalInvestigator principalInvestigatorDTOToPrincipalInvestigator(PrincipalInvestigatorRepresentation principalInvestigatorDTO);

    List<PrincipalInvestigator> principalInvestigatorDTOsToPrincipalInvestigators(List<PrincipalInvestigatorRepresentation> principalInvestigatorDTOs);
}
