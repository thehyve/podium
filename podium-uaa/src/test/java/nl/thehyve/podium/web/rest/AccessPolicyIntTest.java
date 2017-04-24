/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.web.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.thehyve.podium.PodiumUaaApp;
import nl.thehyve.podium.common.enumeration.RequestType;
import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.common.security.UserAuthenticationToken;
import nl.thehyve.podium.common.service.dto.RoleRepresentation;
import nl.thehyve.podium.domain.Organisation;
import nl.thehyve.podium.domain.Role;
import nl.thehyve.podium.domain.User;
import nl.thehyve.podium.exceptions.UserAccountException;
import nl.thehyve.podium.security.OAuth2TokenMockUtil;
import nl.thehyve.podium.service.OrganisationService;
import nl.thehyve.podium.service.RoleService;
import nl.thehyve.podium.service.UserService;
import nl.thehyve.podium.web.rest.vm.ManagedUserVM;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test for the access policy on controller methods.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@SpringBootTest(classes = PodiumUaaApp.class)
public class AccessPolicyIntTest {

    Logger log = LoggerFactory.getLogger(AccessPolicyIntTest.class);

    @Autowired
    private UserService userService;

    @Autowired
    private OrganisationService organisationService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private OAuth2TokenMockUtil tokenUtil;

    @PersistenceContext
    EntityManager entityManager;

    private MockMvc mockMvc;

    private ObjectMapper mapper = new ObjectMapper();


    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    private Organisation organisationA;
    private Organisation organisationB;

    private Organisation createOrganisation(String organisationName) {
        Set<RequestType> requestTypes = new HashSet<>();
        requestTypes.add(RequestType.Data);

        Organisation organisation = new Organisation();
        organisation.setName(organisationName);
        organisation.setShortName(organisationName);
        organisation.setRequestTypes(requestTypes);
        organisation = organisationService.save(organisation);
        entityManager.persist(organisation);
        for(Role role: organisation.getRoles()) {
            entityManager.persist(role);
        }
        entityManager.flush();
        return organisation;
    }

    private void createOrganisations() {
        organisationA = createOrganisation("A");
        organisationB = createOrganisation("B");
    }

    private User podiumAdmin;
    private User bbmriAdmin;
    private User adminOrganisationA;
    private User adminOrganisationB;
    private User adminOrganisationAandB;
    private User coordinatorOrganisationA;
    private User coordinatorOrganisationB;
    private User coordinatorOrganisationAandB;
    private User reviewerAandB;
    private User reviewerA;
    private User researcher;
    private User testUser1;
    private User testUser2;
    private User anonymous;
    private Set<User> allUsers = new LinkedHashSet<>();

    private User createUser(String name, String authority, Organisation ... organisations) throws UserAccountException {
        log.info("Creating user {}", name);
        ManagedUserVM userVM = new ManagedUserVM();
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

    private void createUsers() throws UserAccountException {
        podiumAdmin = createUser("podiumAdmin", AuthorityConstants.PODIUM_ADMIN);
        bbmriAdmin = createUser("bbmriAdmin", AuthorityConstants.BBMRI_ADMIN);
        adminOrganisationA = createUser("adminOrganisationA", AuthorityConstants.ORGANISATION_ADMIN, organisationA);
        adminOrganisationB = createUser("adminOrganisationB", AuthorityConstants.ORGANISATION_ADMIN, organisationB);
        adminOrganisationAandB = createUser("adminOrganisationAandB", AuthorityConstants.ORGANISATION_ADMIN, organisationA, organisationB);
        coordinatorOrganisationA = createUser("coordinatorOrganisationA", AuthorityConstants.ORGANISATION_COORDINATOR, organisationA);
        coordinatorOrganisationB = createUser("coordinatorOrganisationB", AuthorityConstants.ORGANISATION_COORDINATOR, organisationB);
        coordinatorOrganisationAandB= createUser("coordinatorOrganisationAandB", AuthorityConstants.ORGANISATION_COORDINATOR, organisationA, organisationB);
        reviewerAandB = createUser("reviewerAandB", AuthorityConstants.REVIEWER, organisationA, organisationB);
        reviewerA = createUser("reviewerA", AuthorityConstants.REVIEWER, organisationA);
        researcher = createUser("researcher", AuthorityConstants.RESEARCHER);
        testUser1 = createUser("testUser1", AuthorityConstants.RESEARCHER);
        testUser2 = createUser("testUser2", AuthorityConstants.RESEARCHER);
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

    private Role podiumAdminRole;
    private Role bbmriAdminRole;
    private Role researcherRole;
    private Role orgAdminARole;
    private Role orgAdminBRole;
    private Role orgCoordinatorARole;
    private Role orgCoordinatorBRole;
    private Role reviewerARole;
    private Role reviewerBRole;

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

    static class Action {
        HttpMethod method = HttpMethod.GET;
        String url;
        Map<String, String> parameters = new HashMap<>();
        Object body;
        Collection<User> allowedUsers = new LinkedHashSet<>();
        HttpStatus expectedStatus;

        public Action setMethod(HttpMethod method) {
            this.method = method;
            return this;
        }

        public Action setUrl(String url) {
            this.url = url;
            return this;
        }

        public Action set(String param, Object value) {
            this.parameters.put(param, value.toString());
            return this;
        }

        public Action body(Object body) {
            this.body = body;
            return this;
        }

        public Action allow(User ... users) {
            for (User user: users) {
                this.allowedUsers.add(user);
            }
            return this;
        }

        public Action expect(HttpStatus status) {
            this.expectedStatus = status;
            return this;
        }

    }

    public static Action newAction() {
        return new Action();
    }


    public static final String ROLE_ROUTE = "/api/roles";
    public static final String ROLE_SEARCH_ROUTE = "/api/_search/roles";

    private String format(String url, String format, Object ... args) {
        return url + String.format(format, args);
    }

    private List<Action> actions = new ArrayList<>();

    private void createActions() {
        // Roles
        // POST /roles. Not allowed!
        actions.add(newAction()
            .setUrl(ROLE_ROUTE).setMethod(HttpMethod.POST)
            .expect(HttpStatus.METHOD_NOT_ALLOWED));
        // DELETE /roles/{id}. Not allowed!
        actions.add(newAction()
            .setUrl(format(ROLE_ROUTE, "/%d", researcherRole.getId()))
            .setMethod(HttpMethod.DELETE)
            .expect(HttpStatus.METHOD_NOT_ALLOWED));
        // PUT /roles (Role role).
        // Edit non-organisation specific role
        RoleRepresentation editedResearcherRole = researcherRole.toRepresentation();
        editedResearcherRole.getUsers().add(bbmriAdmin.getUuid());
        actions.add(newAction()
            .setUrl(ROLE_ROUTE)
            .setMethod(HttpMethod.PUT)
            .body(editedResearcherRole)
            .allow(podiumAdmin, bbmriAdmin));
        // Edit organisation specific role
        RoleRepresentation editedReviewerARole = reviewerARole.toRepresentation();
        editedReviewerARole.getUsers().add(coordinatorOrganisationA.getUuid());
        actions.add(newAction()
            .setUrl(ROLE_ROUTE)
            .setMethod(HttpMethod.PUT)
            .body(editedReviewerARole)
            .allow(podiumAdmin, bbmriAdmin, adminOrganisationA, adminOrganisationAandB));
        // GET /roles
        actions.add(newAction()
            .setUrl(ROLE_ROUTE)
            .allow(podiumAdmin, bbmriAdmin));
        // GET /roles/organisation/{uuid}
        actions.add(newAction()
            .setUrl(format(ROLE_ROUTE, "/organisation/%s", organisationA.getUuid()))
            .allow(podiumAdmin, bbmriAdmin,
                adminOrganisationA, adminOrganisationAandB,
                coordinatorOrganisationA, coordinatorOrganisationAandB,
                reviewerA, reviewerAandB));
        // GET /roles/{id}
        actions.add(newAction()
            .setUrl(format(ROLE_ROUTE, "/%d", reviewerBRole.getId()))
            .allow(podiumAdmin, bbmriAdmin));
        // GET /_search/roles
        actions.add(newAction()
            .setUrl(ROLE_SEARCH_ROUTE)
            .set("query", "admin")
            .allow(podiumAdmin, bbmriAdmin));
    }

    private void setupData() throws UserAccountException {
        createOrganisations();
        createUsers();
        getRoles();
        createActions();
    }

    private RequestPostProcessor token(User user) {
        if (user == null) {
            return SecurityMockMvcRequestPostProcessors.anonymous();
        }
        return tokenUtil.oauth2Authentication(user);
    }

    private MockHttpServletRequestBuilder getRequest(Action action) {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request(action.method, action.url);
        if (action.body != null) {
            try {
                request = request
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsBytes(action.body));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("JSON serialisation error", e);
            }
        }
        for(Map.Entry<String, String> entry: action.parameters.entrySet()) {
            request = request.param(entry.getKey(), entry.getValue());
        }
        return request;
    }

    private void expectSuccess(Action action, User user) throws Exception {
        mockMvc.perform(
            getRequest(action)
            .with(token(user))
            .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk());
    }

    private void expectFail(Action action, User user) throws Exception {
        mockMvc.perform(
            getRequest(action)
            .with(token(user))
            .accept(MediaType.APPLICATION_JSON)
        )
        .andDo(result -> log.info("Result: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString()))
        .andExpect(status().is4xxClientError());
    }

    private void expectStatus(Action action, User user) throws Exception {
        mockMvc.perform(
            getRequest(action)
                .with(token(user))
                .accept(MediaType.APPLICATION_JSON)
        )
        .andDo(result -> log.info("Result: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString()))
        .andExpect(status().is(action.expectedStatus.value()));
    }

    @Test
    @Transactional
    public void testAccessPolicy() throws Exception {
        setupData();

        for (Action action: actions) {
            for (User user: allUsers) {
                String login = user == null ? "anonymous" : user.getLogin();
                log.info("Testing action {} {} for user {}", action.method, action.url, login);
                if (user == null) {
                    log.info("Expect failure for anonymous...");
                    expectFail(action, user);
                } else if (action.expectedStatus != null) {
                    log.info("Expect {}...", action.expectedStatus);
                    expectStatus(action, user);
                } else {
                    if (action.allowedUsers.contains(user)) {
                        log.info("Expect success...");
                        expectSuccess(action, user);
                    } else {
                        log.info("Expect failure...");
                        expectFail(action, user);
                    }
                }
            }
        }
    }

}
