package org.bbmri.podium.web.rest;

import org.bbmri.podium.PodiumUaaApp;

import org.bbmri.podium.domain.Authority;
import org.bbmri.podium.domain.Role;
import org.bbmri.podium.domain.User;
import org.bbmri.podium.repository.AuthorityRepository;
import org.bbmri.podium.repository.RoleRepository;
import org.bbmri.podium.service.OrganisationService;
import org.bbmri.podium.service.RoleService;
import org.bbmri.podium.repository.search.RoleSearchRepository;

import org.bbmri.podium.service.UserService;
import org.bbmri.podium.service.representation.RoleRepresentation;
import org.bbmri.podium.web.rest.vm.ManagedUserVM;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the RoleResource REST controller.
 *
 * @see RoleResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = PodiumUaaApp.class)
public class RoleResourceIntTest {

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private OrganisationService organisationService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private RoleSearchRepository roleSearchRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private EntityManager em;

    private MockMvc restRoleMockMvc;

    private Role role;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        RoleResource roleResource = new RoleResource(roleService, userService, organisationService);
        this.restRoleMockMvc = MockMvcBuilders.standaloneSetup(roleResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public Role createEntity() {
        Authority authority = authorityRepository.findOne(Authority.REVIEWER);
        if (authority == null) {
            authority = new Authority(Authority.REVIEWER);
            authorityRepository.save(authority);
        }
        Role role = new Role(authority);
        User user;
        Optional<User> object = userService.getUserWithAuthoritiesByLogin("test");
        if (object.isPresent()) {
            user = object.get();
        } else {
            ManagedUserVM userVM = new ManagedUserVM();
            userVM.setLogin("test");
            userVM.setPassword("password");
            user = userService.createUser(userVM);
        }
        Set<User> users = new HashSet<>();
        users.add(user);
        role.setUsers(users);
        return role;
    }

    @Before
    public void initTest() {
        roleSearchRepository.deleteAll();
        role = createEntity();
    }

    @Test
    @Transactional
    public void createRoleNotAllowed() throws Exception {
        int databaseSizeBeforeCreate = roleRepository.findAll().size();

        // Create the Role

        restRoleMockMvc.perform(post("/api/roles")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(role)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Role is not in the database
        List<Role> roleList = roleRepository.findAll();
        assertThat(roleList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void getAllRoles() throws Exception {
        // Initialize the database
        roleRepository.saveAndFlush(role);

        // Get all the roleList
        restRoleMockMvc.perform(get("/api/roles?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(role.getId().intValue())));
    }

    @Test
    @Transactional
    public void getRole() throws Exception {
        // Initialize the database
        roleRepository.saveAndFlush(role);

        // Get the role
        restRoleMockMvc.perform(get("/api/roles/{id}", role.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(role.getId().intValue()));
    }

    @Test
    @Transactional
    public void getNonExistingRole() throws Exception {
        // Get the role
        restRoleMockMvc.perform(get("/api/roles/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateRole() throws Exception {
        // Initialize the database
        roleService.save(role);

        int databaseSizeBeforeUpdate = roleRepository.findAll().size();

        // Update the role
        Role updatedRole = roleRepository.findOne(role.getId());

        restRoleMockMvc.perform(put("/api/roles")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(new RoleRepresentation(updatedRole))))
            .andExpect(status().isOk());

        // Validate the Role in the database
        List<Role> roleList = roleRepository.findAll();
        assertThat(roleList).hasSize(databaseSizeBeforeUpdate);
        Role testRole = roleList.get(roleList.size() - 1);

        // Validate the Role in Elasticsearch
        Role roleEs = roleSearchRepository.findOne(testRole.getId());
        assertThat(roleEs).isEqualToComparingFieldByField(testRole);
    }

    @Test
    @Transactional
    public void updateNonExistingRole() throws Exception {
        int databaseSizeBeforeUpdate = roleRepository.findAll().size();

        // If the entity doesn't have an ID, status not found is returned
        restRoleMockMvc.perform(put("/api/roles")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(new RoleRepresentation(role))))
            .andExpect(status().isNotFound());

        // Validate the Role is not inserted in the database
        List<Role> roleList = roleRepository.findAll();
        assertThat(roleList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteRoleNotAllowed() throws Exception {
        // Initialize the database
        roleService.save(role);

        int databaseSizeBeforeDelete = roleRepository.findAll().size();

        // Get the role
        restRoleMockMvc.perform(delete("/api/roles/{id}", role.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isMethodNotAllowed());

        // Validate the database is not empty
        List<Role> roleList = roleRepository.findAll();
        assertThat(roleList).hasSize(databaseSizeBeforeDelete);
    }

    @Test
    @Transactional
    public void searchRole() throws Exception {
        // Initialize the database
        roleService.save(role);

        // Search the role
        restRoleMockMvc.perform(get("/api/_search/roles?query=id:" + role.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(role.getId().intValue())));
    }

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Role.class);
    }
}
