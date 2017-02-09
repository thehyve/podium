package org.bbmri.podium.service;

import org.bbmri.podium.domain.Authority;
import org.bbmri.podium.domain.Organisation;
import org.bbmri.podium.domain.Role;
import org.bbmri.podium.repository.RoleRepository;
import org.bbmri.podium.repository.search.RoleSearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * Service Implementation for managing Role.
 */
@Service
@Transactional
public class RoleService {

    private final Logger log = LoggerFactory.getLogger(RoleService.class);

    private final RoleRepository roleRepository;

    private final RoleSearchRepository roleSearchRepository;

    public RoleService(RoleRepository roleRepository, RoleSearchRepository roleSearchRepository) {
        this.roleRepository = roleRepository;
        this.roleSearchRepository = roleSearchRepository;
    }

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

    /**
     *  Get all the roles.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<Role> findAll(Pageable pageable) {
        log.debug("Request to get all Roles");
        return roleRepository.findAllWithUsers(pageable);
    }

    /**
     *  Get all the roles for an organisation.
     *
     *  @param organisation the organisation to fetch the roles for.
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<Role> findAllByOrganisation(Organisation organisation) {
        log.debug("Request to get all Roles for Organisation: {}", organisation.getUuid());
        return roleRepository.findAllByOrganisation(organisation);
    }

    /**
     *  Get one role by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public Role findOne(Long id) {
        log.debug("Request to get Role : {}", id);
        Role role = roleRepository.findOneWithUsers(id);
        return role;
    }

    private static final Set<String> globalRoleAuthorities = new HashSet<>(3);
    {
        globalRoleAuthorities.add(Authority.RESEARCHER);
        globalRoleAuthorities.add(Authority.PODIUM_ADMIN);
        globalRoleAuthorities.add(Authority.BBMRI_ADMIN);
    }

    /**
     *  Get the role for an authority.
     *  Only for global roles, not for organisation roles.
     *
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public Role findRoleByAuthorityName(String authorityName) {
        log.debug("Request to get role for an authority");
        if (!globalRoleAuthorities.contains(authorityName)) {
            return null;
        }
        Role role = roleRepository.findByAuthorityName(authorityName);
        return role;
    }

    /**
     *  Delete the  role by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Role : {}", id);
        roleRepository.delete(id);
        roleSearchRepository.delete(id);
    }

    /**
     * Search for the role corresponding to the query.
     *
     *  @param query the query of the search
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<Role> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Roles for query {}", query);
        Page<Role> result = roleSearchRepository.search(queryStringQuery(query), pageable);
        return result;
    }

}
