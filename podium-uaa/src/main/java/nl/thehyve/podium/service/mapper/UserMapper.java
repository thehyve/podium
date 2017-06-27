/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.service.mapper;

import nl.thehyve.podium.common.service.dto.UserRepresentation;
import nl.thehyve.podium.domain.Authority;
import nl.thehyve.podium.domain.User;
import nl.thehyve.podium.search.SearchUser;
import nl.thehyve.podium.service.decorators.UserMapperDecorator;
import nl.thehyve.podium.service.util.UuidMapper;
import nl.thehyve.podium.web.rest.vm.ManagedUserVM;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.mapstruct.DecoratedWith;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper for the entity User and its DTO UserDTO.
 */
@Mapper(componentModel = "spring", uses = { UuidMapper.class })
@DecoratedWith(UserMapperDecorator.class)
public interface UserMapper {

    UserRepresentation userToUserDTO(User user);

    @Mapping(target = "password", ignore = true)
    ManagedUserVM userToManagedUserVM(User user);

    @IterableMapping(elementTargetType = ManagedUserVM.class)
    List<ManagedUserVM> usersToManagedUserVMs(List<User> user);

    @IterableMapping(elementTargetType = UserRepresentation.class)
    List<UserRepresentation> usersToUserDTOs(List<User> users);

    @Mappings({
        @Mapping(target = "createdBy", ignore = true),
        @Mapping(target = "createdDate", ignore = true),
        @Mapping(target = "lastModifiedBy", ignore = true),
        @Mapping(target = "lastModifiedDate", ignore = true),
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "activationKey", ignore = true),
        @Mapping(target = "resetKey", ignore = true),
        @Mapping(target = "resetDate", ignore = true),
        @Mapping(target = "password", ignore = true)
    })
    User userDTOToUser(UserRepresentation userDTO);

    List<User> userDTOsToUsers(List<UserRepresentation> userDTOs);

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "password", ignore = true),
        @Mapping(target = "email", ignore = true),
        @Mapping(target = "login", ignore = true),
        @Mapping(target = "deleted", ignore = true),
        @Mapping(target = "emailVerified", ignore = true),
        @Mapping(target = "adminVerified", ignore = true),
        @Mapping(target = "accountLocked", ignore = true),
        @Mapping(target = "authorities", ignore = true)
    })
    // Copy user properties, except login, password, email, activated.
    User safeUpdateUserWithUserDTO(UserRepresentation userDTO, @MappingTarget User user);

    // Decorated is used to generate the fullname for the searchuser
    SearchUser userToSearchUser(User user);

    List<SearchUser> usersToSearchUsers(List<User> users);

    SearchUser completionSuggestOptionToSearchUser(CompletionSuggestion.Entry.Option option);

    List<SearchUser> completionSuggestOptionsToSearchUsers(List<CompletionSuggestion.Entry.Option> options);

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
        return strings.stream().map(Authority::new).collect(Collectors.toSet());
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
