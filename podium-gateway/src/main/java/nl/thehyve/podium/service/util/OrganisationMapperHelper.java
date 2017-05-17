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

import nl.thehyve.podium.common.service.dto.OrganisationDTO;
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

    public UUID organisationDTOtoUUID(OrganisationDTO organisationDTO) {
        return organisationDTO.getUuid();
    }

    public List<UUID> organisationDTOsToUuids(List<OrganisationDTO> organisationDTOS) {
        return organisationDTOS.stream().map(this::organisationDTOtoUUID).collect(Collectors.toList());
    }

    @DefaultOrganisation
    @IterableMapping(qualifiedBy = DefaultOrganisation.class)
    public List<OrganisationDTO> uuidsToDefaultOrganisationDTOs(Set<UUID> uuids) {
        return uuids.stream().map(this::uuidToOrganisationDTO).collect(Collectors.toList());
    }

    @ExtendedOrganisation
    @IterableMapping(qualifiedBy = ExtendedOrganisation.class)
    public List<OrganisationDTO> uuidsToExtendedOrganisationDTOs(Set<UUID> uuids) {
        return uuids.stream().map(this::uuidToRemoteOrganisationDTO).collect(Collectors.toList());
    }

    @DefaultOrganisation
    OrganisationDTO uuidToOrganisationDTO(UUID uuid) {
        OrganisationDTO organisationRepresentation = new OrganisationDTO();
        organisationRepresentation.setUuid(uuid);
        return organisationRepresentation;
    }

    @ExtendedOrganisation
    OrganisationDTO uuidToRemoteOrganisationDTO(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        OrganisationDTO organisationRepresentation = new OrganisationDTO();
        organisationRepresentation.setUuid(uuid);

        try {
            OrganisationDTO remoteOrganisation = organisationClientService.findOrganisationByUuid(uuid);
            organisationRepresentation.setName(remoteOrganisation.getName());
        } catch(Exception ex) {}

        return organisationRepresentation;
    }

}
