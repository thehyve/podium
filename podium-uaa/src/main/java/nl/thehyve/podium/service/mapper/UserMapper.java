/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.service.mapper;

import nl.thehyve.podium.domain.Authority;
import nl.thehyve.podium.domain.User;
import nl.thehyve.podium.service.representation.UserRepresentation;
import org.mapstruct.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper for the entity User and its DTO UserDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface UserMapper {

    UserRepresentation userToUserDTO(User user);

    List<UserRepresentation> usersToUserDTOs(List<User> users);

    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activationKey", ignore = true)
    @Mapping(target = "resetKey", ignore = true)
    @Mapping(target = "resetDate", ignore = true)
    @Mapping(target = "password", ignore = true)
    User userDTOToUser(UserRepresentation userDTO);

    List<User> userDTOsToUsers(List<UserRepresentation> userDTOs);

    default User userFromId(Long id) {
        if (id == null) {
            return null;
        }
        User user = new User();
        user.setId(id);
        return user;
    }

    default Set<String> stringsFromAuthorities (Set<Authority> authorities) {
        return authorities.stream().map(Authority::getName)
            .collect(Collectors.toSet());
    }

    default Set<Authority> authoritiesFromStrings(Set<String> strings) {
        return strings.stream().map(string -> {
            Authority auth = new Authority(string);
            return auth;
        }).collect(Collectors.toSet());
    }

    default Set<String> stringsFromGrantedAuthorities (Set<GrantedAuthority> authorities) {
        return authorities.stream().map(GrantedAuthority::getAuthority)
            .collect(Collectors.toSet());
    }

    default Set<GrantedAuthority> grantedAuthoritiesFromStrings (Set<String> strings) {
        return strings.stream().map(SimpleGrantedAuthority::new)
            .collect(Collectors.toSet());
    }
}