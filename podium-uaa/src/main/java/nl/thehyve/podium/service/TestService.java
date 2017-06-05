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
import nl.thehyve.podium.search.SearchUser;
import nl.thehyve.podium.service.mapper.UserMapper;
import nl.thehyve.podium.service.representation.TestRoleRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service class for clearing database for testing purposes.
 */
@Profile({"dev", "test"})
@Service
@Transactional
public class TestService {

    private final Logger log = LoggerFactory.getLogger(TestService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSearchRepository userSearchRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RoleSearchRepository roleSearchRepository;

    @Autowired
    RoleService roleService;

    @Autowired
    private OrganisationRepository organisationRepository;

    @Autowired
    private OrganisationSearchRepository organisationSearchRepository;

    @Autowired
    OrganisationService organisationService;

    @Autowired
    EntityManager entityManager;

    @Autowired
    UserMapper userMapper;

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
        for (Role role : roleRepository.findAll()) {
            if (role.getOrganisation() != null) {
                roles.add(role);
            }
        }
        roleSearchRepository.delete(roles);
        roleRepository.delete(roles);

        // Delete all users except "admin" and "system"
        List<User> users = new ArrayList<>();
        for (User user : userRepository.findAll()) {
            if (!specialUsers.contains(user.getLogin())) {
                users.add(user);
                log.info("Scheduling user for deletion: {}", user.getLogin());
                // delete user from associated (non-organisational) roles
                if (user.getRoles() != null) {
                    for (Role role : user.getRoles()) {
                        log.info("Removing user {} from role {}", user.getLogin(), role.getAuthority());
                        role.getUsers().remove(user);
                        entityManager.persist(role);
                    }
                }
            }
        }

        List<SearchUser> searchUsers = userMapper.usersToSearchUsers(users);
        userSearchRepository.delete(searchUsers);
        userRepository.delete(users);

        // Delete all organisations
        organisationRepository.deleteAll();
        organisationSearchRepository.deleteAll();
    }

    /**
     * Assigns users to a role. The role is fetched based on the organisation UUID and the authority.
     *
     * @param roleData representation containing organisation UUID, authority and a set of user UUIDs.
     */
    @Profile({"dev", "test"})
    public void assignUsersToRole(TestRoleRepresentation roleData) {
        log.info("Assign to role: ({}, {})", roleData.getAuthority(), roleData.getOrganisation());
        log.info("Assign users  : {}", roleData.getUsers() == null ? null : Arrays.toString(roleData.getUsers().toArray()));
        Role role;
        if (AuthorityConstants.isOrganisationAuthority(roleData.getAuthority())) {
            Organisation organisation = organisationService.findByShortName(roleData.getOrganisation());
            if (organisation == null) {
                throw new ResourceNotFound("Organisation not found with short name " + roleData.getOrganisation());
            }
            role = roleService.findRoleByOrganisationAndAuthorityName(organisation, roleData.getAuthority());
        } else {
            role = roleService.findRoleByAuthorityName(roleData.getAuthority());
        }
        if (role == null) {
            String message = String.format("Role not found with authority %s and organisation %s",
                roleData.getAuthority(), roleData.getOrganisation());
            throw new ResourceNotFound(message);
        }
        Set<User> users = new HashSet<>();
        if (roleData.getUsers() != null) {
            for (String userLogin : roleData.getUsers()) {
                userService.getUserWithAuthoritiesByLogin(userLogin).ifPresent(users::add);
            }
        }
        role.setUsers(users);
        entityManager.persist(role);
        entityManager.flush();

        // Refresh associated users (because of caching)
        for (User user : role.getUsers()) {
            entityManager.refresh(user);
        }
    }

}
