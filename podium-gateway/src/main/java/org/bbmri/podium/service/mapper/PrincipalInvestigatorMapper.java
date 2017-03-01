package org.bbmri.podium.service.mapper;

import org.bbmri.podium.domain.*;
import org.bbmri.podium.service.dto.PrincipalInvestigatorDTO;

import org.mapstruct.*;
import java.util.List;

/**
 * Mapper for the entity PrincipalInvestigator and its DTO PrincipalInvestigatorDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface PrincipalInvestigatorMapper {

    PrincipalInvestigatorDTO principalInvestigatorToPrincipalInvestigatorDTO(PrincipalInvestigator principalInvestigator);

    List<PrincipalInvestigatorDTO> principalInvestigatorsToPrincipalInvestigatorDTOs(List<PrincipalInvestigator> principalInvestigators);

    @Mapping(target = "requestDetail", ignore = true)
    PrincipalInvestigator principalInvestigatorDTOToPrincipalInvestigator(PrincipalInvestigatorDTO principalInvestigatorDTO);

    List<PrincipalInvestigator> principalInvestigatorDTOsToPrincipalInvestigators(List<PrincipalInvestigatorDTO> principalInvestigatorDTOs);
}
