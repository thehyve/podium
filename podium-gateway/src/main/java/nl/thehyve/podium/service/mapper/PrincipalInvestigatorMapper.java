/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.service.mapper;

import nl.thehyve.podium.domain.PrincipalInvestigator;
import nl.thehyve.podium.service.dto.PrincipalInvestigatorDTO;

import org.mapstruct.*;
import java.util.List;

/**
 * Mapper for the entity PrincipalInvestigator and its DTO PrincipalInvestigatorDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface PrincipalInvestigatorMapper {

    PrincipalInvestigatorDTO principalInvestigatorToPrincipalInvestigatorDTO(PrincipalInvestigator principalInvestigator);

    List<PrincipalInvestigatorDTO> principalInvestigatorsToPrincipalInvestigatorDTOs(List<PrincipalInvestigator> principalInvestigators);

    PrincipalInvestigator principalInvestigatorDTOToPrincipalInvestigator(PrincipalInvestigatorDTO principalInvestigatorDTO);

    List<PrincipalInvestigator> principalInvestigatorDTOsToPrincipalInvestigators(List<PrincipalInvestigatorDTO> principalInvestigatorDTOs);
}
