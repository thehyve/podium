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

import nl.thehyve.podium.common.service.dto.RoleRepresentation;
import nl.thehyve.podium.domain.Authority;
import nl.thehyve.podium.domain.Organisation;
import nl.thehyve.podium.domain.Role;
import nl.thehyve.podium.domain.User;
import nl.thehyve.podium.service.util.UuidMapper;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapper for the entity User and its DTO UserDTO.
 */
@Mapper(componentModel = "spring", uses = { UuidMapper.class })
public interface RoleMapper {
    RoleRepresentation roleToRoleDTO(Role role);

    List<RoleRepresentation> rolesToRoleDTOs(List<Role> role);

    default Set<UUID> uuidsFromUsers (Set<User> users) {
        return users.stream()
            .filter(u -> !u.isDeleted())
            .map(User::getUuid)
            .collect(Collectors.toSet());
    }

    default UUID uuidFromOrganisation (Organisation organisation) {
        if (organisation == null) {
            return null;
        }
        return organisation.getUuid();
    }

    default String stringFromAuthority (Authority authority) {
        if (authority == null) {
            return null;
        }
        return authority.getName();
    }
}
