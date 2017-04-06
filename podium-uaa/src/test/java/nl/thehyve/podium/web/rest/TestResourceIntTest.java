/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.thehyve.podium.PodiumUaaApp;
import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.common.service.dto.OrganisationDTO;
import nl.thehyve.podium.domain.Organisation;
import nl.thehyve.podium.domain.Role;
import nl.thehyve.podium.domain.User;
import nl.thehyve.podium.repository.OrganisationRepository;
import nl.thehyve.podium.repository.RoleRepository;
import nl.thehyve.podium.repository.UserRepository;
import nl.thehyve.podium.service.*;
import nl.thehyve.podium.service.representation.RoleRepresentation;
import nl.thehyve.podium.web.rest.vm.ManagedUserVM;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the TestResource REST controller.
 *
 * @see TestResource
 */
@ActiveProfiles({"test", "h2"})
@RunWith(SpringRunner.class)
@SpringBootTest(classes = PodiumUaaApp.class)
public class TestResourceIntTest {

    private final Logger log = LoggerFactory.getLogger(TestResourceIntTest.class);

    @Inject
    private UserService userService;

    @Inject
    private UserRepository userRepository;

    @Inject
    private OrganisationService organisationService;

    @Inject
    private OrganisationRepository organisationRepository;

    @Inject
    private RoleService roleService;

    @Inject
    private RoleRepository roleRepository;

    @Inject
    private TestService testService;

    private MockMvc mockMvc;

    private ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        TestResource testResource = new TestResource();
        ReflectionTestUtils.setField(testResource, "userService", userService);
        ReflectionTestUtils.setField(testResource, "organisationService", organisationService);
        ReflectionTestUtils.setField(testResource, "roleService", roleService);
        ReflectionTestUtils.setField(testResource, "testService", testService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(testResource).build();
    }

    private void createJoe() throws Exception {
        ManagedUserVM userData = new ManagedUserVM();
        AccountResourceIntTest.setMandatoryFields(userData);
        userData.setId(null);
        userData.setLogin("joe");
        userData.setPassword(AccountResourceIntTest.VALID_PASSWORD);
        userData.setFirstName("Joe");
        userData.setLastName("Shmoe");
        userData.setEmail("joe@example.com");
        userData.setLangKey("en");
        userData.setAdminVerified(true);
        userData.setEmailVerified(true);
        userData.setAuthorities(new HashSet<>(Arrays.asList(AuthorityConstants.RESEARCHER)));

        mockMvc.perform(post("/api/test/users")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(userData)))
            .andExpect(status().isCreated());
    }

    @Test
    @Transactional
    public void testCreateValidatedUser() throws Exception {
        createJoe();
        Optional<User> userOptional = userService.getUserWithAuthoritiesByLogin("joe");
        assertThat(userOptional.isPresent()).isTrue();
        User user = userOptional.get();
        assertThat(user.isAdminVerified());
        assertThat(user.isEmailVerified());
    }

    private OrganisationDTO createTestOrganisation() throws Exception {
        OrganisationDTO organisationData = new OrganisationDTO();
        organisationData.setName("Test organisation");
        organisationData.setShortName("Test");
        organisationData.setActivated(true);

        final OrganisationDTO[] organisation = new OrganisationDTO[1];

        mockMvc.perform(post("/api/test/organisations")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(organisationData)))
            .andExpect(status().isCreated())
            .andDo(result -> {
                log.info("Response: {}", result.getResponse().getContentAsString());
                OrganisationDTO data =
                    mapper.readValue(result.getResponse().getContentAsByteArray(), OrganisationDTO.class);
                organisation[0] = data;
            });
        return organisation[0];
    }

    @Test
    @Transactional
    public void testCreateActivatedOrganisation() throws Exception {
        OrganisationDTO newOrganisation = createTestOrganisation();
        Organisation organisation = organisationService.findByUuid(newOrganisation.getUuid());
        assertThat(organisation).isNotNull();
        assertThat(organisation.isActivated());
    }

    @Test
    @Transactional
    public void testAssignUserToRole() throws Exception {
        OrganisationDTO testOrganisation = createTestOrganisation();

        Organisation organisation = organisationService.findByUuid(testOrganisation.getUuid());
        Role role = roleService.findRoleByOrganisationAndAuthorityName(
            organisation,
            AuthorityConstants.ORGANISATION_COORDINATOR);
        assertThat(role.getUsers()).isEmpty();

        createJoe();
        User joe = userService.getUserWithAuthoritiesByLogin("joe").get();
        RoleRepresentation assignment = new RoleRepresentation();
        assignment.setAuthority(AuthorityConstants.ORGANISATION_COORDINATOR);
        assignment.setOrganisation(testOrganisation.getUuid());
        Set<UUID> users = new HashSet<>();
        users.add(joe.getUuid());
        assignment.setUsers(users);

        // Update role
        mockMvc.perform(post("/api/test/roles/assign")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(assignment)))
            .andExpect(status().isCreated());

        role = roleService.findRoleByOrganisationAndAuthorityName(
            organisation,
            AuthorityConstants.ORGANISATION_COORDINATOR);
        assertThat(role.getUsers()).isNotEmpty();
        assertThat(role.getUsers()).containsExactly(joe);
    }

    @Test
    @Transactional
    public void testClearDatabase() throws Exception {
        long organisationCount = organisationRepository.count();
        long userCount = userRepository.count();
        long roleCount = roleRepository.count();

        assertThat(organisationCount).isGreaterThan(0);
        assertThat(userCount).isGreaterThan(2);
        assertThat(roleCount).isGreaterThan(3);

        testService.clearDatabase();

        organisationCount = organisationRepository.count();
        userCount = userRepository.count();
        roleCount = roleRepository.count();

        assertThat(organisationCount).isEqualTo(0);
        assertThat(userCount).isEqualTo(2);
        assertThat(roleCount).isEqualTo(3);
    }

}
