/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

package nl.thehyve.podium.service.util;

import nl.thehyve.podium.common.service.dto.OrganisationRepresentation;
import nl.thehyve.podium.service.OrganisationClientService;
import org.mapstruct.IterableMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class OrganisationMapperHelper {

    @Autowired
    OrganisationClientService organisationClientService;

    public static UUID organisationDTOtoUUID(OrganisationRepresentation organisationRepresentation) {
        return organisationRepresentation.getUuid();
    }

    public static List<UUID> organisationDTOsToUuids(List<OrganisationRepresentation> organisationRepresentations) {
        return organisationRepresentations.stream().map(OrganisationMapperHelper::organisationDTOtoUUID).collect(Collectors.toList());
    }

    @MinimalMapper
    @IterableMapping(qualifiedBy = MinimalMapper.class)
    public List<OrganisationRepresentation> uuidsToDefaultOrganisationDTOs(Set<UUID> uuids) {
        return uuids.stream().map(this::uuidToOrganisationDTO).collect(Collectors.toList());
    }

    @DefaultMapper
    @IterableMapping(qualifiedBy = DefaultMapper.class)
    public List<OrganisationRepresentation> uuidsToExtendedOrganisationDTOs(Set<UUID> uuids) {
        return uuids.stream().map(this::uuidToRemoteOrganisationDTO).collect(Collectors.toList());
    }

    @MinimalMapper
    OrganisationRepresentation uuidToOrganisationDTO(UUID uuid) {
        OrganisationRepresentation organisationRepresentation = new OrganisationRepresentation();
        organisationRepresentation.setUuid(uuid);
        return organisationRepresentation;
    }

    @DefaultMapper
    OrganisationRepresentation uuidToRemoteOrganisationDTO(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        OrganisationRepresentation organisationRepresentation = new OrganisationRepresentation();
        organisationRepresentation.setUuid(uuid);

        try {
            OrganisationRepresentation remoteOrganisation = organisationClientService.findOrganisationByUuidCached(uuid);
            organisationRepresentation.setName(remoteOrganisation.getName());
            organisationRepresentation.setShortName(remoteOrganisation.getShortName());
        } catch(Exception ex) {}

        return organisationRepresentation;
    }

}
