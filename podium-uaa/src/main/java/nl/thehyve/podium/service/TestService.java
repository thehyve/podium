/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.service;

import nl.thehyve.podium.common.exceptions.ResourceNotFound;
import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.domain.Organisation;
import nl.thehyve.podium.domain.Role;
import nl.thehyve.podium.domain.User;
import nl.thehyve.podium.repository.OrganisationRepository;
import nl.thehyve.podium.repository.RoleRepository;
import nl.thehyve.podium.repository.UserRepository;
import nl.thehyve.podium.repository.search.OrganisationSearchRepository;
import nl.thehyve.podium.repository.search.RoleSearchRepository;
import nl.thehyve.podium.repository.search.UserSearchRepository;
import nl.thehyve.podium.search.SearchOrganisation;
import nl.thehyve.podium.service.representation.RoleRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.*;

/**
 * Service class for managing users.
 */
@Profile({"dev", "test"})
@Service
@Transactional
public class TestService {

    private final Logger log = LoggerFactory.getLogger(TestService.class);

    @Inject
    private UserRepository userRepository;

    @Inject
    private UserSearchRepository userSearchRepository;

    @Inject
    private UserService userService;

    @Inject
    private RoleRepository roleRepository;

    @Inject
    private RoleSearchRepository roleSearchRepository;

    @Inject
    RoleService roleService;

    @Inject
    private OrganisationRepository organisationRepository;

    @Inject
    private OrganisationSearchRepository organisationSearchRepository;

    @Inject
    OrganisationService organisationService;

    private static final Set<String> specialUsers = new HashSet<>(Arrays.asList("admin", "system"));

    /**
     * Delete all:
     * - organisation roles;
     * - users, except "admin", "system";
     * - organisations.
     */
    public void clearDatabase() {
        // Delete all organisation roles
        Set<Role> roles = new HashSet<>();
        for (Role role: roleRepository.findAll()) {
            if (role.getOrganisation() != null) {
                roles.add(role);
            }
        }
        roleSearchRepository.delete(roles);
        roleRepository.delete(roles);

        // Delete all users except "admin" and "system"
        Set<User> users = new HashSet<>();
        for(User user: userRepository.findAll()) {
            if (!specialUsers.contains(user.getLogin())) {
                users.add(user);
            }
        }
        userSearchRepository.delete(users);
        userRepository.delete(users);

        // Delete all organisations
        organisationRepository.deleteAll();
        organisationSearchRepository.deleteAll();
    }

    /**
     * Assigns users to a role. The role is fetched based on the organisation UUID and the authority.
     * @param roleData representation containing organisation UUID, authority and a set of user UUIDs.
     */
    @Profile({"dev", "test"})
    public void assignUsersToRole(RoleRepresentation roleData) {
        log.info("Assign to role: ({}, {})", roleData.getAuthority(), roleData.getOrganisation());
        log.info("Assign users  : {}", roleData.getUsers() == null ? null : Arrays.toString(roleData.getUsers().toArray()));
        Role role;
        if (AuthorityConstants.isOrganisationAuthority(roleData.getAuthority())) {
            Organisation organisation = organisationService.findByUuid(roleData.getOrganisationUuid());
            if (organisation == null) {
                throw new ResourceNotFound("Organisation not found with uuid " + roleData.getOrganisationUuid().toString());
            }
            role = roleService.findRoleByOrganisationAndAuthorityName(organisation, roleData.getAuthority());
        } else {
            role = roleService.findRoleByAuthorityName(roleData.getAuthority());
        }
        if (role == null) {
            String message = String.format("Role not found with authority %s and organisation %s",
                roleData.getAuthority(), roleData.getOrganisationUuid());
            throw new ResourceNotFound(message);
        }
        Set<User> users = new HashSet<>();
        if (roleData.getUsers() != null) {
            for (UUID userUuid : roleData.getUsers()) {
                userService.getUserByUuid(userUuid).ifPresent(users::add);
            }
        }
        role.setUsers(users);
        roleService.save(role);
    }

}
