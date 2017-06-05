/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.web.rest;

import nl.thehyve.podium.PodiumGatewayApp;
import nl.thehyve.podium.common.enumeration.RequestType;
import nl.thehyve.podium.common.resource.InternalRequestResource;
import nl.thehyve.podium.common.resource.InternalUserResource;
import nl.thehyve.podium.common.security.AuthenticatedUser;
import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.common.security.SerialisedUser;
import nl.thehyve.podium.common.security.UserAuthenticationToken;
import nl.thehyve.podium.common.service.dto.OrganisationDTO;
import nl.thehyve.podium.common.service.dto.RequestRepresentation;
import nl.thehyve.podium.common.service.dto.UserRepresentation;
import nl.thehyve.podium.common.test.AbstractAccessPolicyIntTest;
import nl.thehyve.podium.common.test.Action;
import nl.thehyve.podium.config.SecurityBeanOverrideConfiguration;
import nl.thehyve.podium.service.AuditService;
import nl.thehyve.podium.service.MailService;
import nl.thehyve.podium.service.OrganisationClientService;
import nl.thehyve.podium.service.RequestService;
import nl.thehyve.podium.service.TestService;
import nl.thehyve.podium.service.UserClientService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.collections.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static nl.thehyve.podium.common.test.Action.format;
import static nl.thehyve.podium.common.test.Action.newAction;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

/**
 * Integration test for the access policy on controller methods.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@SpringBootTest(classes = {PodiumGatewayApp.class, SecurityBeanOverrideConfiguration.class})
public class AccessPolicyIntTest extends AbstractAccessPolicyIntTest {

    public static final String REQUEST_ROUTE = "/api/requests";

    Logger log = LoggerFactory.getLogger(AccessPolicyIntTest.class);

    @Autowired
    private TestService testService;

    @Autowired
    private RequestService requestService;

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private OrganisationClientService organisationService;

    @MockBean
    private UserClientService userClientService;

    @MockBean
    private InternalUserResource internalUserResource;

    @MockBean
    private MailService mailService;

    @MockBean
    private AuditService auditService;

    @MockBean
    private InternalRequestResource internalRequestResource;

    private MockMvc mockMvc;

    private OrganisationDTO organisationA;

    private OrganisationDTO organisationB;

    private List<OrganisationDTO> organisations = new ArrayList<>();

    private Map<UUID, Map<String, Set<UUID>>> organisationRoles = new HashMap<>();

    private AuthenticatedUser podiumAdmin;

    private AuthenticatedUser bbmriAdmin;

    private AuthenticatedUser adminOrganisationA;

    private AuthenticatedUser adminOrganisationB;

    private AuthenticatedUser adminOrganisationAandB;

    private AuthenticatedUser coordinatorOrganisationA;

    private AuthenticatedUser coordinatorOrganisationB;

    private AuthenticatedUser coordinatorOrganisationAandB;

    private AuthenticatedUser reviewerAandB;

    private AuthenticatedUser reviewerA;

    private AuthenticatedUser researcher;

    private AuthenticatedUser testUser1;

    private AuthenticatedUser testUser2;

    private AuthenticatedUser anonymous;

    private Set<AuthenticatedUser> allUsers = new LinkedHashSet<>();

    private Map<String, SerialisedUser> userStore = new HashMap<>();

    private Map<UUID, UserRepresentation> userInfo = new HashMap<>();

    private RequestRepresentation draftRequest1;

    private List<Action> actions = new ArrayList<>();

    private static OrganisationDTO createOrganisation(String organisationName, UUID organisationUuid) {
        Set<RequestType> requestTypes = new HashSet<>();
        requestTypes.add(RequestType.Data);

        OrganisationDTO organisation = new OrganisationDTO();
        organisation.setUuid(organisationUuid);
        organisation.setName(organisationName);
        organisation.setShortName(organisationName);
        organisation.setRequestTypes(requestTypes);
        return organisation;
    }

    @Override
    protected MockMvc getMockMvc() {
        return mockMvc;
    }

    @Before
    public void setup() {
        log.info("Clearing database before test ...");
        testService.clearDatabase();

        MockitoAnnotations.initMocks(this);

        this.mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    @After
    public void clearDatabase() {
        log.info("Clearing database after test ...");
        testService.clearDatabase();
    }

    private void createOrganisations() {
        organisationA = createOrganisation("A", UUID.randomUUID());
        organisationB = createOrganisation("B", UUID.randomUUID());
        organisations.addAll(Arrays.asList(organisationA, organisationB));
        for (OrganisationDTO organisation : organisations) {
            Map<String, Set<UUID>> roles = new HashMap<>();
            roles.put(AuthorityConstants.ORGANISATION_ADMIN, new HashSet<>());
            roles.put(AuthorityConstants.ORGANISATION_COORDINATOR, new HashSet<>());
            roles.put(AuthorityConstants.REVIEWER, new HashSet<>());
            organisationRoles.put(organisation.getUuid(), roles);
        }
    }

    private AuthenticatedUser createUser(String name, String authority, OrganisationDTO... organisations) {
        log.info("Creating user {}", name);
        UUID userUuid = UUID.randomUUID();
        UserRepresentation userDetails = new UserRepresentation();
        userDetails.setLogin(name);
        userDetails.setUuid(userUuid);
        userDetails.setLogin("test_" + name);
        userDetails.setEmail("test_" + name + "@localhost");
        userDetails.setFirstName("test_firstname_" + name);
        userDetails.setLastName("test_lastname_" + name);
        Set<String> authorities = new HashSet<>();
        Map<UUID, Collection<String>> roles = new HashMap<>();
        if (organisations.length > 0) {
            for (OrganisationDTO organisation : organisations) {
                log.info("Assigning role {} for organisation {}", authority, organisation.getName());
                organisationRoles.get(organisation.getUuid()).get(authority).add(userUuid);
                roles.put(organisation.getUuid(), Sets.newSet(authority));
            }
        }
        if (authority != null) {
            log.info("Assigning role {}", authority);
            authorities.add(authority);
        }
        SerialisedUser user = new SerialisedUser(userUuid, name, authorities, roles);
        {
            log.info("Checking user {}", name);
            // some sanity checks
            if (authority != null) {
                assert (!user.getAuthorityNames().isEmpty());
                UserAuthenticationToken token = new UserAuthenticationToken(user);
                assert (!token.getAuthorities().isEmpty());
            }
        }
        userStore.put(user.getName(), user);
        userInfo.put(userUuid, userDetails);
        allUsers.add(user);
        return user;
    }

    private void createUsers() {
        podiumAdmin = createUser("podiumAdmin", AuthorityConstants.PODIUM_ADMIN);
        bbmriAdmin = createUser("bbmriAdmin", AuthorityConstants.BBMRI_ADMIN);
        adminOrganisationA = createUser("adminOrganisationA", AuthorityConstants.ORGANISATION_ADMIN, organisationA);
        adminOrganisationB = createUser("adminOrganisationB", AuthorityConstants.ORGANISATION_ADMIN, organisationB);
        adminOrganisationAandB = createUser("adminOrganisationAandB", AuthorityConstants.ORGANISATION_ADMIN, organisationA, organisationB);
        coordinatorOrganisationA = createUser("coordinatorOrganisationA", AuthorityConstants.ORGANISATION_COORDINATOR, organisationA);
        coordinatorOrganisationB = createUser("coordinatorOrganisationB", AuthorityConstants.ORGANISATION_COORDINATOR, organisationB);
        coordinatorOrganisationAandB = createUser("coordinatorOrganisationAandB", AuthorityConstants.ORGANISATION_COORDINATOR, organisationA, organisationB);
        reviewerAandB = createUser("reviewerAandB", AuthorityConstants.REVIEWER, organisationA, organisationB);
        reviewerA = createUser("reviewerA", AuthorityConstants.REVIEWER, organisationA);
        researcher = createUser("researcher", AuthorityConstants.RESEARCHER);
        testUser1 = createUser("testUser1", AuthorityConstants.RESEARCHER);
        testUser2 = createUser("testUser2", AuthorityConstants.RESEARCHER);
        anonymous = null;
        allUsers.add(anonymous);
    }

    private void createRequests() {
        draftRequest1 = requestService.createDraft(researcher);
    }

    private void createRequestActions() {
        // GET /requests/drafts
        actions.add(newAction()
            .setUrl(REQUEST_ROUTE + "/drafts")
            .allow(researcher, testUser1, testUser2));
        // POST /requests/drafts
        RequestRepresentation draft = new RequestRepresentation();
        actions.add(newAction()
            .setUrl(REQUEST_ROUTE + "/drafts")
            .setMethod(HttpMethod.POST)
            .body(draft)
            .successStatus(HttpStatus.CREATED)
            .allow(researcher, testUser1, testUser2));
        // GET /requests/drafts/{uuid}
        actions.add(newAction()
            .setUrl(format(REQUEST_ROUTE, "/drafts/%s", draftRequest1.getUuid()))
            .allow(researcher));
        // PUT /requests/drafts
        // POST /requests/drafts/validate
        // GET /requests/drafts/{uuid}/submit
        // GET /requests/requester
        actions.add(newAction()
            .setUrl(REQUEST_ROUTE + "/requester")
            .allow(researcher, testUser1, testUser2));
        // GET /requests/status/{status}/requester
        // PUT /requests
        // GET /requests/{uuid}/submit
        // GET /requests/reviewer
        // GET /requests/status/{status}/coordinator
        // GET /requests/organisation/{uuid}/reviewer
        // GET /requests/status/{status}/organisation/{uuid}/coordinator
        // GET /requests/{uuid}
        // DELETE /requests/drafts/{uuid}
        // GET /requests/{uuid}/validate
        // GET /requests/{uuid}/reject
        // GET /requests/{uuid}/approve
        // GET /requests/{uuid}/requestRevision
        // GET /_search/requests

    }

    private void initMocks() throws URISyntaxException {
        // Mock Feign client for organisations
        for (OrganisationDTO organisation : organisations) {
            given(this.organisationService.findOrganisationByUuid(eq(organisation.getUuid())))
                .willReturn(organisation);
        }

        // Don't return anything on findUsersByRole; only used for notification mails
        given(this.organisationService.findUsersByRole(any(), any()))
            .willReturn(Collections.emptyList());

        // Mock Feign client for fetching user information
        for (Map.Entry<UUID, UserRepresentation> userEntry : userInfo.entrySet()) {
            given(this.userClientService.findUserByUuid(eq(userEntry.getKey())))
                .willReturn(userEntry.getValue());
        }

        for (Map.Entry<String, SerialisedUser> userEntry : userStore.entrySet()) {
            given(this.internalUserResource.getAuthenticatedUserByLogin(eq(userEntry.getKey())))
                .willReturn(ResponseEntity.ok(userEntry.getValue()));
        }

        // Return draft request 1
        given(this.internalRequestResource.getDefaultRequest(eq(draftRequest1.getUuid())))
            .willReturn(ResponseEntity.ok(draftRequest1));

        // Mock mail sending
        //doNothing().when(this.mailService).sendEmail(any(), any(), any(), any(), any());

        // Mock audit service calls
        doNothing().when(this.auditService).publishEvent(any());

    }

    private void setupData() throws URISyntaxException {
        createOrganisations();
        createUsers();
        createRequests();
        createRequestActions();
        initMocks();
    }

    @Test
    public void testAccessPolicy() throws Exception {
        setupData();
        runAll(actions, allUsers);
    }

}
