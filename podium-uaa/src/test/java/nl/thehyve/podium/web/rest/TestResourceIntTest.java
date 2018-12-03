/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.thehyve.podium.PodiumUaaApp;
import nl.thehyve.podium.common.enumeration.RequestType;
import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.common.service.dto.OrganisationRepresentation;
import nl.thehyve.podium.common.test.web.rest.TestUtil;
import nl.thehyve.podium.domain.Organisation;
import nl.thehyve.podium.domain.Role;
import nl.thehyve.podium.domain.User;
import nl.thehyve.podium.repository.OrganisationRepository;
import nl.thehyve.podium.repository.RoleRepository;
import nl.thehyve.podium.repository.UserRepository;
import nl.thehyve.podium.service.OrganisationService;
import nl.thehyve.podium.service.RoleService;
import nl.thehyve.podium.service.TestService;
import nl.thehyve.podium.service.UserService;
import nl.thehyve.podium.service.dto.TestRoleRepresentation;
import nl.thehyve.podium.web.rest.dto.ManagedUserRepresentation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganisationService organisationService;

    @Autowired
    private OrganisationRepository organisationRepository;

    @Autowired
    private RoleService roleService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TestService testService;

    private MockMvc mockMvc;

    private ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        TestResource testResource = new TestResource();
        ReflectionTestUtils.setField(testResource, "userService", userService);
        ReflectionTestUtils.setField(testResource, "organisationService", organisationService);
        ReflectionTestUtils.setField(testResource, "testService", testService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(testResource).build();
    }

    private void createJoe() throws Exception {
        ManagedUserRepresentation userData = new ManagedUserRepresentation();
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

    private void createBbmriAdmin() throws Exception {
        ManagedUserRepresentation userData = new ManagedUserRepresentation();
        AccountResourceIntTest.setMandatoryFields(userData);
        userData.setId(null);
        userData.setLogin("bbmri_admin");
        userData.setPassword(AccountResourceIntTest.VALID_PASSWORD);
        userData.setFirstName("Bernard");
        userData.setLastName("Admin");
        userData.setEmail("bbmri_admin@example.com");
        userData.setLangKey("en");
        userData.setAdminVerified(true);
        userData.setEmailVerified(true);
        userData.setAuthorities(new HashSet<>(Arrays.asList(AuthorityConstants.BBMRI_ADMIN)));

        mockMvc.perform(post("/api/test/users")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(userData)))
            .andExpect(status().isCreated());

        Optional<ManagedUserRepresentation> userOptional = userService.getUserWithAuthoritiesByLogin("bbmri_admin");
        assertThat(userOptional.isPresent()).isTrue();
        ManagedUserRepresentation user = userOptional.get();
        assertThat(user.getAuthorities()).containsExactly(AuthorityConstants.BBMRI_ADMIN);
    }

    @Test
    @Transactional
    public void testCreateValidatedUser() throws Exception {
        createJoe();
        Optional<ManagedUserRepresentation> userOptional = userService.getUserWithAuthoritiesByLogin("joe");
        assertThat(userOptional.isPresent()).isTrue();
        ManagedUserRepresentation user = userOptional.get();
        assertThat(user.isAdminVerified());
        assertThat(user.isEmailVerified());
    }

    private OrganisationRepresentation createTestOrganisation() throws Exception {
        OrganisationRepresentation organisationData = new OrganisationRepresentation();
        organisationData.setName("Test organisation");
        organisationData.setShortName("Test");
        organisationData.setActivated(true);
        organisationData.setRequestTypes(new HashSet<>(Arrays.asList(RequestType.Data, RequestType.Images, RequestType.Material)));

        final OrganisationRepresentation[] organisation = new OrganisationRepresentation[1];

        mockMvc.perform(post("/api/test/organisations")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(organisationData)))
            .andExpect(status().isCreated())
            .andDo(result -> {
                log.info("Response: {}", result.getResponse().getContentAsString());
                OrganisationRepresentation data =
                    mapper.readValue(result.getResponse().getContentAsByteArray(), OrganisationRepresentation.class);
                organisation[0] = data;
            });
        return organisation[0];
    }

    @Test
    @Transactional
    public void testCreateActivatedOrganisation() throws Exception {
        OrganisationRepresentation newOrganisation = createTestOrganisation();
        Organisation organisation = organisationService.findByUuid(newOrganisation.getUuid());
        assertThat(organisation).isNotNull();
        assertThat(organisation.isActivated());
        assertThat(organisation.getRequestTypes().containsAll(Arrays.asList(RequestType.Data, RequestType.Images, RequestType.Material)));
    }

    @Test
    @Transactional
    public void testAssignUserToRole() throws Exception {
        OrganisationRepresentation testOrganisation = createTestOrganisation();

        Organisation organisation = organisationService.findByUuid(testOrganisation.getUuid());
        Role role = roleService.findRoleByOrganisationAndAuthorityName(
            organisation,
            AuthorityConstants.ORGANISATION_COORDINATOR);
        assertThat(role.getUsers()).isEmpty();

        createJoe();
        ManagedUserRepresentation joe = userService.getUserWithAuthoritiesByLogin("joe").get();
        TestRoleRepresentation assignment = new TestRoleRepresentation();
        assignment.setAuthority(AuthorityConstants.ORGANISATION_COORDINATOR);
        assignment.setOrganisation(testOrganisation.getShortName());
        Set<String> users = new HashSet<>();
        users.add(joe.getLogin());
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
        assertThat(role.getUsers().stream().map(User::getUuid)).containsExactly(joe.getUuid());
    }

    @Test
    @Transactional
    public void testClearDatabase() throws Exception {
        createBbmriAdmin();
        createJoe();
        createTestOrganisation();

        long organisationCount = organisationRepository.count();
        long userCount = userRepository.count();
        long roleCount = roleRepository.count();

        for(User user: userRepository.findAll()) {
            log.info(" - User: {}", user.getLogin());
        }

        assertThat(organisationCount).isEqualTo(1);
        assertThat(userCount).isEqualTo(2);
        assertThat(roleCount).isEqualTo(6);

        testService.clearDatabase();

        organisationCount = organisationRepository.count();
        userCount = userRepository.count();
        roleCount = roleRepository.count();

        assertThat(organisationCount).isEqualTo(0);
        assertThat(userCount).isEqualTo(0);
        assertThat(roleCount).isEqualTo(3);
    }

    @Test
    @Transactional
    public void testClearDatabaseAfterRoleAssignment() throws Exception {
        createBbmriAdmin();
        createJoe();
        createTestOrganisation();

        long organisationCount = organisationRepository.count();
        long userCount = userRepository.count();
        long roleCount = roleRepository.count();

        assertThat(organisationCount).isEqualTo(1);
        assertThat(userCount).isEqualTo(2);
        assertThat(roleCount).isEqualTo(6);

        testService.clearDatabase();

        // Create bbmri_admin user
        createBbmriAdmin();

        // Assign bbmri_admin user to bbrmi_admin role
        ManagedUserRepresentation bbmriAdmin = userService.getUserWithAuthoritiesByLogin("bbmri_admin").get();
        TestRoleRepresentation assignment = new TestRoleRepresentation();
        assignment.setAuthority(AuthorityConstants.BBMRI_ADMIN);
        Set<String> users = new HashSet<>();
        users.add(bbmriAdmin.getLogin());
        assignment.setUsers(users);
        mockMvc.perform(post("/api/test/roles/assign")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(assignment)))
            .andExpect(status().isCreated());

        // Test database cleanup
        testService.clearDatabase();

        organisationCount = organisationRepository.count();
        userCount = userRepository.count();
        roleCount = roleRepository.count();

        assertThat(organisationCount).isEqualTo(0);
        assertThat(userCount).isEqualTo(0);
        assertThat(roleCount).isEqualTo(3);
    }

}
