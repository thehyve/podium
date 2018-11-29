/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.web.rest;

import nl.thehyve.podium.PodiumUaaApp;
import nl.thehyve.podium.common.security.AuthenticatedUser;
import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.common.test.AbstractAuthorisedUserIntTest;
import nl.thehyve.podium.domain.Organisation;
import nl.thehyve.podium.domain.Role;
import nl.thehyve.podium.domain.User;
import nl.thehyve.podium.exceptions.UserAccountException;
import nl.thehyve.podium.service.RoleService;
import nl.thehyve.podium.service.TestService;
import nl.thehyve.podium.service.mapper.RoleMapper;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.*;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

/**
 * Integration tests for access policies.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@SpringBootTest(classes = PodiumUaaApp.class)
public abstract class AbstractUaaAccessPolicyIntTest extends AbstractAuthorisedUserIntTest {

    @Autowired
    RoleService roleService;

    @Autowired
    TestService testService;

    @Autowired
    RoleMapper roleMapper;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    protected MockMvc getMockMvc() {
        return mockMvc;
    }

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    Organisation organisationA;
    Organisation organisationB;

    private void createOrganisations() {
        organisationA = testService.createOrganisation("A");
        organisationB = testService.createOrganisation("B");
    }

    User podiumAdmin;
    User bbmriAdmin;
    User adminOrganisationA;
    User adminOrganisationB;
    User adminOrganisationAandB;
    User coordinatorOrganisationA;
    User coordinatorOrganisationB;
    User coordinatorOrganisationAandB;
    User reviewerAandB;
    User reviewerA;
    User researcher;
    User testUser1;
    User testUser2;
    User anonymous;
    Set<AuthenticatedUser> allUsers = new LinkedHashSet<>();

    private void createUsers() throws UserAccountException {
        podiumAdmin = testService.createUser("podiumAdmin", AuthorityConstants.PODIUM_ADMIN);
        bbmriAdmin = testService.createUser("bbmriAdmin", AuthorityConstants.BBMRI_ADMIN);
        adminOrganisationA = testService.createUser("adminOrganisationA", AuthorityConstants.ORGANISATION_ADMIN, organisationA);
        adminOrganisationB = testService.createUser("adminOrganisationB", AuthorityConstants.ORGANISATION_ADMIN, organisationB);
        adminOrganisationAandB = testService.createUser("adminOrganisationAandB", AuthorityConstants.ORGANISATION_ADMIN, organisationA, organisationB);
        coordinatorOrganisationA = testService.createUser("coordinatorOrganisationA", AuthorityConstants.ORGANISATION_COORDINATOR, organisationA);
        coordinatorOrganisationB = testService.createUser("coordinatorOrganisationB", AuthorityConstants.ORGANISATION_COORDINATOR, organisationB);
        coordinatorOrganisationAandB= testService.createUser("coordinatorOrganisationAandB", AuthorityConstants.ORGANISATION_COORDINATOR, organisationA, organisationB);
        reviewerAandB = testService.createUser("reviewerAandB", AuthorityConstants.REVIEWER, organisationA, organisationB);
        reviewerA = testService.createUser("reviewerA", AuthorityConstants.REVIEWER, organisationA);
        researcher = testService.createUser("researcher", AuthorityConstants.RESEARCHER);
        testUser1 = testService.createUser("testUser1", AuthorityConstants.RESEARCHER);
        testUser2 = testService.createUser("testUser2", AuthorityConstants.RESEARCHER);
        anonymous = null;
        allUsers.addAll(Arrays.asList(
            podiumAdmin,
            bbmriAdmin,
            adminOrganisationA,
            adminOrganisationB,
            adminOrganisationAandB,
            coordinatorOrganisationA,
            coordinatorOrganisationB,
            coordinatorOrganisationAandB,
            reviewerAandB,
            reviewerA,
            researcher,
            testUser1,
            testUser2,
            anonymous
        ));
    }

    Role podiumAdminRole;
    Role bbmriAdminRole;
    Role researcherRole;
    Role orgAdminARole;
    Role orgAdminBRole;
    Role orgCoordinatorARole;
    Role orgCoordinatorBRole;
    Role reviewerARole;
    Role reviewerBRole;

    private void getRoles() {
        podiumAdminRole = roleService.findRoleByAuthorityName(AuthorityConstants.PODIUM_ADMIN);
        bbmriAdminRole = roleService.findRoleByAuthorityName(AuthorityConstants.BBMRI_ADMIN);
        researcherRole = roleService.findRoleByAuthorityName(AuthorityConstants.RESEARCHER);
        orgAdminARole = roleService.findRoleByOrganisationAndAuthorityName(organisationA, AuthorityConstants.ORGANISATION_ADMIN);
        orgAdminBRole = roleService.findRoleByOrganisationAndAuthorityName(organisationB, AuthorityConstants.ORGANISATION_ADMIN);
        orgCoordinatorARole = roleService.findRoleByOrganisationAndAuthorityName(organisationA, AuthorityConstants.ORGANISATION_COORDINATOR);
        orgCoordinatorBRole = roleService.findRoleByOrganisationAndAuthorityName(organisationB, AuthorityConstants.ORGANISATION_COORDINATOR);
        reviewerARole = roleService.findRoleByOrganisationAndAuthorityName(organisationA, AuthorityConstants.REVIEWER);
        reviewerBRole = roleService.findRoleByOrganisationAndAuthorityName(organisationB, AuthorityConstants.REVIEWER);
    }

    static final String ROLE_ROUTE = "/api/roles";
    static final String ROLE_SEARCH_ROUTE = "/api/_search/roles";
    static final String ORGANISATION_ROUTE = "/api/organisations";
    static final String USER_ROUTE = "/api/users";
    static final String ACCOUNT_ROUTE = "/api/account";

    void setupData() throws UserAccountException {
        createOrganisations();
        createUsers();
        getRoles();
    }

}
