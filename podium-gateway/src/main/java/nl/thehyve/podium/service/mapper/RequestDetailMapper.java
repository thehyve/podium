package nl.thehyve.podium.service.mapper;

import nl.thehyve.podium.domain.RequestDetail;
import nl.thehyve.podium.service.representation.RequestDetailRepresentation;
import org.mapstruct.Mapper;

/**
 * Copyright (c) 2017 The Hyve B.V.
 * This code is licensed under the GNU General Public License,
 * version 3, or (at your option) any later version.
 */
@Mapper(componentModel = "spring", uses = { PrincipalInvestigatorMapper.class })
public interface RequestDetailMapper {

    RequestDetailRepresentation requestDetailToRequestDetailRepresentation(RequestDetail requestDetail);

    RequestDetail requestDetailRepresentationToRequestDetail(RequestDetailRepresentation requestDetailRepresentation);
}
