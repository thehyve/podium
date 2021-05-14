/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.service;

import com.google.common.collect.Sets;
import nl.thehyve.podium.common.exceptions.ResourceNotFound;
import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.common.service.dto.RoleRepresentation;
import nl.thehyve.podium.domain.Organisation;
import nl.thehyve.podium.domain.Role;
import nl.thehyve.podium.domain.User;
import nl.thehyve.podium.repository.OrganisationRepository;
import nl.thehyve.podium.repository.RoleRepository;
import nl.thehyve.podium.repository.search.RoleSearchRepository;
import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.common.service.dto.RoleRepresentation;
import nl.thehyve.podium.service.mapper.RoleMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

/**
 * Service Implementation for managing Role.
 */
@Service
@Transactional
public class RoleService {

    private final Logger log = LoggerFactory.getLogger(RoleService.class);

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private OrganisationRepository organisationRepository;

    @Autowired
    private RoleSearchRepository roleSearchRepository;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private UserService userService;


    /**
     * Save a role.
     *
     * @param role the entity to save
     * @return the persisted entity
     */
    public Role save(Role role) {
        log.debug("Request to save Role : {}", role);
        Role result = roleRepository.save(role);
        roleSearchRepository.save(result);
        return result;
    }

    private void copyProperties(RoleRepresentation source, Role target) {
        Set<UUID> currentUsers = target.getUsers().stream().map(User::getUuid).collect(Collectors.toSet());
        Set<UUID> desiredUsers = source.getUsers();
        Set<UUID> deleteUsers = Sets.difference(currentUsers, desiredUsers);
        Set<UUID> addUsers = Sets.difference(desiredUsers, currentUsers);
        Set<User> result = target.getUsers().stream().filter( u ->
            !deleteUsers.contains(u.getUuid())
        ).collect(Collectors.toSet());
        for (UUID userUuid: addUsers) {
            Optional<User> user = userService.getDomainUserByUuid(userUuid);
            if (user.isPresent()) {
                result.add(user.get());
            } else {
                throw new ResourceNotFound(String.format("Could not find user with uuid %s", userUuid));
            }
        }
        target.setUsers(result);
    }

    /**
     * Updates an existing role.
     *
     * @param roleRepresentation the role to update
     * @return the updated role
     */
    public RoleRepresentation updateRole(RoleRepresentation roleRepresentation) {
        Role role = findOneById(roleRepresentation.getId());
        if (role == null) {
            throw new ResourceNotFound(String.format("Role not found with id: %s.", roleRepresentation.getId()));
        }
        copyProperties(roleRepresentation, role);
        save(role);
        return roleMapper.roleToRoleDTO(role);
    }

    /**
     * Get all the roles.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<RoleRepresentation> findAll(Pageable pageable) {
        log.debug("Request to get all Roles");
        return roleRepository.findAllWithUsers(pageable).map(role -> roleMapper.roleToRoleDTO(role));
    }

    /**
     * Get all the roles for an organisation.
     *
     * @param uuid the organisation UUID to fetch the roles for.
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<RoleRepresentation> findAllByOrganisationUUID(UUID uuid) {
        log.debug("Request to get all Roles for Organisation UUID: {}", uuid);
        Organisation organisation = organisationRepository.findByUuidAndDeletedFalse(uuid);

        List<Role> roles = organisation.getRoles().stream().collect(Collectors.toList());

        return roleMapper.rolesToRoleDTOs(roles);
    }

    /**
     * Get one role by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public Role findOneById(Long id) {
        log.debug("Request to get Role : {}", id);
        return roleRepository.findOneWithUsers(id);
    }

    /**
     * Get one role by id.
     *
     * @param id the id of the entity
     * @return a representation of the entity
     */
    @Transactional(readOnly = true)
    public RoleRepresentation findOne(Long id) {
        log.debug("Request to get Role : {}", id);
        Role role = findOneById(id);
        if (role == null) {
            throw new ResourceNotFound(String.format("Role not found with id: %s.", id));
        }
        return roleMapper.roleToRoleDTO(role);
    }

    /**
     * Get the role for an authority.
     * Only for global roles, not for organisation roles.
     * Returns null when authorityName is a organisation authority.
     *
     * @param authorityName Authority name to fetch role for.
     * @return the role entity
     */
    @Transactional(readOnly = true)
    public Role findRoleByAuthorityName(String authorityName) {
        log.debug("Request to get role for an authority");
        if (AuthorityConstants.isOrganisationAuthority(authorityName)) {
            return null;
        }
        Role role = roleRepository.findByAuthorityName(authorityName);
        if (role != null) {
            role.getUsers().size();
        }
        return role;
    }

    /**
     * Get the role for an authority within an organisation.
     *
     * @param organisation The organisation to fetch the role for.
     * @param authorityName Authority name to fetch role for.
     * @return the entity
     */
    @Transactional(readOnly = true)
    public Role findRoleByOrganisationAndAuthorityName(Organisation organisation, String authorityName) {
        log.debug("Request to get role for an authority within an organisation");
        if (!AuthorityConstants.isOrganisationAuthority(authorityName)) {
            log.warn("{} is not an organisation authority!", authorityName);
            return null;
        }
        Role role = roleRepository.findByOrganisationAndAuthorityName(organisation, authorityName);
        if (role != null) {
            role.getUsers().size();
        }
        return role;
    }

    /**
     * Delete the  role by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Role : {}", id);
        roleRepository.deleteById(id);
        roleSearchRepository.deleteById(id);
    }

    /**
     * Search for the role corresponding to the query.
     *
     * @param query the query of the search
     * @param pageable Pagination object of the requested page
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<RoleRepresentation> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Roles for query {}", query);
        return roleSearchRepository.search(queryStringQuery(query), pageable).map(role -> roleMapper.roleToRoleDTO(role));
    }

    @Transactional(readOnly = true)
    public boolean organisationHasAnyRole(Organisation organisation) {
        return roleRepository.existsByOrganisation(organisation);
    }

}
