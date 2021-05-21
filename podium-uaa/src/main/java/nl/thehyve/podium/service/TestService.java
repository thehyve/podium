/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.service;

import nl.thehyve.podium.common.enumeration.RequestType;
import nl.thehyve.podium.common.exceptions.ResourceNotFound;
import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.common.security.UserAuthenticationToken;
import nl.thehyve.podium.domain.Organisation;
import nl.thehyve.podium.domain.Role;
import nl.thehyve.podium.domain.User;
import nl.thehyve.podium.exceptions.UserAccountException;
import nl.thehyve.podium.repository.OrganisationRepository;
import nl.thehyve.podium.repository.RoleRepository;
import nl.thehyve.podium.repository.UserRepository;
import nl.thehyve.podium.repository.search.OrganisationSearchRepository;
import nl.thehyve.podium.repository.search.RoleSearchRepository;
import nl.thehyve.podium.repository.search.UserSearchRepository;
import nl.thehyve.podium.search.SearchUser;
import nl.thehyve.podium.service.mapper.UserMapper;
import nl.thehyve.podium.service.dto.TestRoleRepresentation;
import nl.thehyve.podium.web.rest.dto.ManagedUserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;

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
        for (Role role: roleRepository.findAll()) {
            if (role.getOrganisation() != null) {
                roles.add(role);
            }
        }
        roleSearchRepository.deleteAll(roles);
        roleRepository.deleteAll(roles);

        // Delete all users except "admin" and "system"
        List<User> users = new ArrayList<>();
        for(User user: userRepository.findAll()) {
            if (!specialUsers.contains(user.getLogin())) {
                users.add(user);
                log.info("Scheduling user for deletion: {}", user.getLogin());
                // delete user from associated (non-organisational) roles
                if (user.getRoles() != null) {
                    for(Role role: user.getRoles()) {
                        log.info("Removing user {} from role {}", user.getLogin(), role.getAuthority());
                        role.getUsers().remove(user);
                        entityManager.persist(role);
                    }
                }
            }
        }

        List<SearchUser> searchUsers = userMapper.usersToSearchUsers(users);
        userSearchRepository.deleteAll(searchUsers);
        userRepository.deleteAll(users);

        // Delete all organisations
        organisationRepository.deleteAll();
        organisationSearchRepository.deleteAll();
    }

    /**
     * Assigns users to a role. The role is fetched based on the organisation UUID and the authority.
     * @param roleData representation containing organisation UUID, authority and a set of user UUIDs.
     */
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
        entityManager.refresh(role);
        Set<User> users = new HashSet<>();
        if (roleData.getUsers() != null) {
            for (String userLogin : roleData.getUsers()) {
                userService.getDomainUserWithAuthoritiesByLogin(userLogin).ifPresent(users::add);
            }
        }
        role.setUsers(users);
        entityManager.persist(role);
        entityManager.flush();

        // Refresh associated users (because of caching)
        for(User user: role.getUsers()) {
            entityManager.refresh(user);
        }
    }

    /**
     * Create organisation for testing purposes with provided name.
     * @param organisationName the name of the organisation.
     * @return the created entity.
     */
    public Organisation createOrganisation(String organisationName) {
        Set<RequestType> requestTypes = new HashSet<>();
        requestTypes.add(RequestType.Data);

        Organisation organisation = new Organisation();
        organisation.setName(organisationName);
        organisation.setShortName(organisationName);
        organisation.setRequestTypes(requestTypes);
        organisation.setActivated(true);
        organisation = organisationService.save(organisation);
        entityManager.persist(organisation);
        for(Role role: organisation.getRoles()) {
            entityManager.persist(role);
        }
        entityManager.flush();
        return organisation;
    }

    /**
     * Create user for testing purposes with provided name, authority and list of organisations for which the
     * user should have that authority.
     * @param name the username.
     * @param authority the authority the user should have.
     * @param organisations the (possibly empty) list of organisations for which the user should have that authority.
     * @return the created entity.
     * @throws UserAccountException if login or email already in use.
     */
    public User createUser(String name, String authority, Organisation ... organisations) throws UserAccountException {
        log.info("Creating user {}", name);
        ManagedUserRepresentation userVM = new ManagedUserRepresentation();
        userVM.setLogin("test_" + name);
        userVM.setEmail("test_" + name + "@localhost");
        userVM.setFirstName("test_firstname_"+name);
        userVM.setLastName("test_lastname_"+name);
        userVM.setPassword("Password123!");
        User user = userService.createUser(userVM);
        if (organisations.length > 0) {
            for (Organisation organisation: organisations) {
                log.info("Assigning role {} for organisation {}", authority, organisation.getName());
                Role role = roleService.findRoleByOrganisationAndAuthorityName(organisation, authority);
                assert (role != null);
                user.getRoles().add(role);
                user = userService.save(user);
            }
        } else if (authority != null) {
            log.info("Assigning role {}", authority);
            Role role = roleService.findRoleByAuthorityName(authority);
            assert (role != null);
            user.getRoles().add(role);
            user = userService.save(user);
        }
        entityManager.persist(user);
        entityManager.flush();
        {
            log.info("Checking user {}", name);
            // some sanity checks
            User user1 = entityManager.find(User.class, user.getId());
            assert (user1 != null);
            if (authority != null) {
                assert (!user1.getAuthorities().isEmpty());
                UserAuthenticationToken token = new UserAuthenticationToken(user1);
                assert (!token.getAuthorities().isEmpty());
            }
        }
        return user;
    }


}
