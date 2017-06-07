/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.web.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import nl.thehyve.podium.PodiumGatewayApp;
import nl.thehyve.podium.common.enumeration.RequestType;
import nl.thehyve.podium.common.enumeration.ReviewProcessOutcome;
import nl.thehyve.podium.common.enumeration.RequestReviewStatus;
import nl.thehyve.podium.common.enumeration.RequestStatus;
import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.common.security.SerialisedUser;
import nl.thehyve.podium.common.security.UserAuthenticationToken;
import nl.thehyve.podium.common.service.dto.MessageRepresentation;
import nl.thehyve.podium.common.service.dto.OrganisationDTO;
import nl.thehyve.podium.common.service.dto.UserRepresentation;
import nl.thehyve.podium.config.SecurityBeanOverrideConfiguration;
import nl.thehyve.podium.domain.Request;
import nl.thehyve.podium.repository.RequestRepository;
import nl.thehyve.podium.repository.search.RequestSearchRepository;
import nl.thehyve.podium.common.test.OAuth2TokenMockUtil;
import nl.thehyve.podium.service.*;
import nl.thehyve.podium.common.service.dto.PrincipalInvestigatorRepresentation;
import nl.thehyve.podium.common.service.dto.RequestDetailRepresentation;
import nl.thehyve.podium.common.service.dto.RequestRepresentation;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.collections.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.net.URISyntaxException;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for the RequestResource REST controller.
 *
 * @see RequestResource
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@SpringBootTest(classes = {PodiumGatewayApp.class, SecurityBeanOverrideConfiguration.class})
public class RequestResourceIntTest {

    private Logger log = LoggerFactory.getLogger(RequestResourceIntTest.class);

    @Autowired
    private TestService testService;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private RequestSearchRepository requestSearchRepository;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private OAuth2TokenMockUtil tokenUtil;

    @MockBean
    private OrganisationClientService organisationService;

    @MockBean
    private UserClientService userClientService;

    @MockBean
    private MailService mailService;

    @MockBean
    private AuditService auditService;

    private ObjectMapper mapper = new ObjectMapper();

    private TypeReference<List<RequestRepresentation>> listTypeReference =
        new TypeReference<List<RequestRepresentation>>(){};

    private MockMvc mockMvc;

    private UserAuthenticationToken requester;
    private UserAuthenticationToken coordinator1;
    private UserAuthenticationToken coordinator2;
    private UserAuthenticationToken reviewer1;
    private UserAuthenticationToken reviewer2;

    private static UUID organisationUuid1 = UUID.randomUUID();
    private static UUID organisationUuid2 = UUID.randomUUID();
    private static UUID coordinatorUuid1 = UUID.randomUUID();
    private static UUID coordinatorUuid2 = UUID.randomUUID();
    private static UUID reviewerUuid1 = UUID.randomUUID();
    private static UUID reviewerUuid2 = UUID.randomUUID();

    private static final String ACTION_VALIDATE = "validate";
    private static final String ACTION_APPROVE = "approve";
    private static final String ACTION_REQUEST_REVISION = "requestRevision";
    private static final String ACTION_REJECT = "reject";

    private static final String mockRequesterUsername = "requester";
    private static UUID mockRequesterUuid = UUID.randomUUID();

    private static Set<String> requesterAuthorities =
        Sets.newSet(AuthorityConstants.RESEARCHER);
    private static Set<String> coordinatorAuthorities =
        Sets.newSet(AuthorityConstants.ORGANISATION_COORDINATOR);
    private static Set<String> reviewerAuthorities =
        Sets.newSet(AuthorityConstants.REVIEWER);

    public static final String REQUESTS_ROUTE = "/api/requests";
    public static final String REQUESTS_SEARCH_ROUTE = "/api/_search/requests";

    private static OrganisationDTO createOrganisation(int i, UUID uuid) {
        OrganisationDTO organisation = new OrganisationDTO();
        organisation.setUuid(uuid);
        organisation.setName("Test organisation " + i);
        organisation.setShortName("Test" + i);
        organisation.setActivated(true);

        // The organisation accepts the Material and Data request types
        Set<RequestType> requestTypes = Sets.newSet(
            RequestType.Material,
            RequestType.Data
        );

        organisation.setRequestTypes(requestTypes);
        return organisation;
    }

    private static UserRepresentation createCoordinator(int i, UUID uuid) {
        UserRepresentation coordinator = new UserRepresentation();
        coordinator.setUuid(uuid);
        coordinator.setLogin("coordinator" + i);
        coordinator.setFirstName("Co " + i);
        coordinator.setLastName("Ordinator");
        coordinator.setEmail("coordinator" + i + "@local");
        return coordinator;
    }

    private static UserRepresentation createReviewer(int i, UUID uuid) {
        UserRepresentation coordinator = new UserRepresentation();
        coordinator.setUuid(uuid);
        coordinator.setLogin("reviewer" + i);
        coordinator.setFirstName("Re " + i);
        coordinator.setLastName("Viewer");
        coordinator.setEmail("reviewer" + i + "@local");
        return coordinator;
    }

    private static UserRepresentation createRequester() {
        UserRepresentation requesterRepresentation = new UserRepresentation();
        requesterRepresentation.setUuid(mockRequesterUuid);
        requesterRepresentation.setLogin(mockRequesterUsername);
        requesterRepresentation.setFirstName("Re");
        requesterRepresentation.setLastName("Quester");
        requesterRepresentation.setEmail("requester@local");
        return requesterRepresentation;
    }

    private static Map<UUID, Collection<String>> createOrganisationRole(UUID organisationUuid, String authority) {
        Map<UUID, Collection<String>> roles = new HashMap<>();
        roles.put(organisationUuid, Sets.newSet(authority));
        return roles;
    }

    @Before
    public void setup() throws URISyntaxException {
        log.info("Clearing database before test ...");
        testService.clearDatabase();

        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        MockitoAnnotations.initMocks(this);

        // Mock authentication for the requester
        {
            SerialisedUser mockRequester = new SerialisedUser(
                mockRequesterUuid, mockRequesterUsername, requesterAuthorities, null);
            requester = new UserAuthenticationToken(mockRequester);
            requester.setAuthenticated(true);
        }

        // Coordinator 1 is coordinator of mock organisation 1
        {
            SerialisedUser mockCoordinator = new SerialisedUser(
                coordinatorUuid1, "coordinator1", requesterAuthorities,
                createOrganisationRole(organisationUuid1, AuthorityConstants.ORGANISATION_COORDINATOR));
            coordinator1 = new UserAuthenticationToken(mockCoordinator);
            coordinator1.setAuthenticated(true);
        }

        // Coordinator 2 is coordinator of both mock organisations
        {
            Map<UUID, Collection<String>> roles = new HashMap<>();
            roles.put(organisationUuid1, Sets.newSet(AuthorityConstants.ORGANISATION_COORDINATOR));
            roles.put(organisationUuid2, Sets.newSet(AuthorityConstants.ORGANISATION_COORDINATOR));
            SerialisedUser mockCoordinator = new SerialisedUser(
                coordinatorUuid2, "coordinator2", requesterAuthorities, roles);
            coordinator2 = new UserAuthenticationToken(mockCoordinator);
            coordinator2.setAuthenticated(true);
        }

        // Reviewer 1 is reviewer of mock organisation 1
        {
            SerialisedUser mockReviewer = new SerialisedUser(
                reviewerUuid1, "reviewer1", requesterAuthorities,
                createOrganisationRole(organisationUuid1, AuthorityConstants.REVIEWER));
            reviewer1 = new UserAuthenticationToken(mockReviewer);
            reviewer1.setAuthenticated(true);
        }

        // Reviewer 2 is reviewer of mock organisation 1
        {
            SerialisedUser mockReviewer = new SerialisedUser(
                reviewerUuid2, "reviewer2", requesterAuthorities,
                createOrganisationRole(organisationUuid1, AuthorityConstants.REVIEWER));
            reviewer2 = new UserAuthenticationToken(mockReviewer);
            reviewer2.setAuthenticated(true);
        }

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

    private void initMocks() throws URISyntaxException {
        // Mock organisation service for fetching organisation info through Feign
        OrganisationDTO organisation1 = createOrganisation(1, organisationUuid1);
        given(this.organisationService.findOrganisationByUuid(eq(organisationUuid1)))
            .willReturn(organisation1);
        OrganisationDTO organisation2 = createOrganisation(2, organisationUuid2);
        given(this.organisationService.findOrganisationByUuid(eq(organisationUuid2)))
            .willReturn(organisation2);
        List<UserRepresentation> coordinators1 = new ArrayList<>();
        coordinators1.add(createCoordinator(1, coordinatorUuid1));
        coordinators1.add(createCoordinator(2, coordinatorUuid2));
        given(this.organisationService.findUsersByRole(eq(organisationUuid1), any()))
            .willReturn(coordinators1);
        List<UserRepresentation> coordinators2 = new ArrayList<>();
        coordinators1.add(createCoordinator(2, coordinatorUuid2));
        given(this.organisationService.findUsersByRole(eq(organisationUuid2), any()))
            .willReturn(coordinators2);

        // Mock Feign client for fetching user information
        UserRepresentation requesterRepresentation = createRequester();
        given(this.userClientService.findUserByUuid(any()))
            .willReturn(requesterRepresentation);

        // Mock notification call
        doNothing().when(this.mailService).sendSubmissionNotificationToCoordinators(any(), any(), anyListOf(UserRepresentation.class));

        // Mock audit service calls
        doNothing().when(this.auditService).publishEvent(any());
    }

    private RequestPostProcessor token(UserAuthenticationToken user) {
        if (user == null) {
            return SecurityMockMvcRequestPostProcessors.anonymous();
        }

        log.info("Creating token for user: {} / {}", user.getName(), user.getUuid());

        return tokenUtil.oauth2Authentication(user);
    }

    private MockHttpServletRequestBuilder getRequest(
        HttpMethod method,
        String url,
        Object body,
        Map<String, String> parameters
    ) {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request(method, url);
        if (body != null) {
            try {
                request = request
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsBytes(body));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("JSON serialisation error", e);
            }
        }
        for (Map.Entry<String, String> entry: parameters.entrySet()) {
            request = request.param(entry.getKey(), entry.getValue());
        }
        return request;
    }

    private RequestRepresentation newDraft(UserAuthenticationToken user) throws Exception {
        final RequestRepresentation[] request = new RequestRepresentation[1];

        mockMvc.perform(
            getRequest(HttpMethod.POST,
                REQUESTS_ROUTE + "/drafts",
                null,
                Collections.emptyMap())
                .with(token(user))
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isCreated())
        .andDo(result -> {
            log.info("Result: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
            request[0] = mapper.readValue(result.getResponse().getContentAsByteArray(), RequestRepresentation.class);
        });

        Thread.sleep(100);

        return request[0];
    }

    private RequestRepresentation updateDraft(UserAuthenticationToken user, RequestRepresentation request) throws Exception {
        final RequestRepresentation[] resultRequest = new RequestRepresentation[1];
        mockMvc.perform(
            getRequest(HttpMethod.PUT,
                REQUESTS_ROUTE + "/drafts",
                request,
                Collections.emptyMap())
                .with(token(user))
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(result -> {
            log.info("Result: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
            resultRequest[0] = mapper.readValue(result.getResponse().getContentAsByteArray(), RequestRepresentation.class);
        });
        return resultRequest[0];
    }

    private void setRequestData(RequestRepresentation request) {
        RequestDetailRepresentation details = request.getRequestDetail();
        details.setTitle("Test title");
        details.setBackground("Background of the request");
        details.setResearchQuestion("Does it work?");
        details.setHypothesis("H0");
        details.setMethods("Testing");
        details.setSearchQuery("q");
        PrincipalInvestigatorRepresentation principalInvestigator = details.getPrincipalInvestigator();
        principalInvestigator.setName("Test Person");
        principalInvestigator.setEmail("pi@local");
        principalInvestigator.setJobTitle("Tester");
        principalInvestigator.setAffiliation("The Organisation");
    }

    private void initFetchTests() throws Exception {
        // Initialize draft with two organisations
        RequestRepresentation requestTwoOrganisation = newDraft(requester);
        setRequestData(requestTwoOrganisation);

        submitDraftToOrganisations(requestTwoOrganisation, Arrays.asList(organisationUuid1, organisationUuid2));

        // Initialize draft with one organisation
        RequestRepresentation requestOneOrganisation = newDraft(requester);
        setRequestData(requestOneOrganisation);

        submitDraftToOrganisations(requestOneOrganisation, Arrays.asList(organisationUuid2));
    }

    private List<RequestRepresentation> fetchAllForCoordinator(UserAuthenticationToken user) throws Exception {
        final List<RequestRepresentation>[] res = new List[1];

        mockMvc.perform(
            getRequest(HttpMethod.GET,
                REQUESTS_ROUTE + "/status/Review/coordinator",
                null,
                Collections.emptyMap())
                .with(token(user))
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(result -> {
            List<RequestRepresentation> requests =
                mapper.readValue(result.getResponse().getContentAsByteArray(), listTypeReference);
            res[0] = requests;
        });
        return res[0];
    }

    /**
     *
     * @param user The authenticated user performing the action
     * @param action The action to perform
     * @param requestUuid The UUID of the request to perform the action on
     * @param method The HttpMethod required to perform the action
     * @return
     * @throws Exception
     */
    private ResultActions performProcessAction(
        UserAuthenticationToken user, String action, UUID requestUuid, HttpMethod method, MessageRepresentation message
    ) throws Exception {
        return mockMvc.perform(
            getRequest(method,
                REQUESTS_ROUTE + "/" + requestUuid.toString() + "/" + action,
                message,
                Collections.emptyMap())
                .with(token(user))
                .accept(MediaType.APPLICATION_JSON)
        );
    }

    private List<RequestRepresentation> submitDraftToOrganisations(RequestRepresentation request, List<UUID> organisations) throws Exception {
        // Set organisations
        int i = 1;
        for (UUID uuid : organisations) {
            OrganisationDTO organisation = createOrganisation(i, uuid);
            request.getOrganisations().add(organisation);
            i++;
        }
        request = updateDraft(requester, request);
        Assert.assertEquals(organisations.size(), request.getOrganisations().size());

        // Submit the draft. One request should have been generated (and is returned).
        final List<RequestRepresentation>[] res = new List[1];
        mockMvc.perform(
            getRequest(HttpMethod.GET,
                REQUESTS_ROUTE + "/drafts/" + request.getUuid().toString() + "/submit",
                null,
                Collections.emptyMap())
                .with(token(requester))
                .accept(MediaType.APPLICATION_JSON)
        )
        .andDo(result -> {
            log.info("Submitted result: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
            List<RequestRepresentation> requests =
                mapper.readValue(result.getResponse().getContentAsByteArray(), listTypeReference);

            // Number of requests should equal the number of organisations it was submitted to
            Assert.assertEquals(organisations.size(), requests.size());
            for (RequestRepresentation req: requests) {
                Assert.assertEquals(RequestStatus.Review, req.getStatus());
                Assert.assertEquals(RequestReviewStatus.Validation, req.getRequestReview().getStatus());
            }
            res[0] = requests;
        })
        .andExpect(status().isOk());

        return res[0];
    }

    private RequestRepresentation getSubmittedDraft() throws Exception {
        // Initialize draft
        RequestRepresentation request = newDraft(requester);
        setRequestData(request);

        // Setup submitted draft
        List<RequestRepresentation> result = submitDraftToOrganisations(request, Arrays.asList(organisationUuid1));
        Assert.assertEquals(1, result.size());

        return result.get(0);
    }

    private static List<UserRepresentation> nonEmptyUserRepresentationList() {
        return argThat(allOf(org.hamcrest.Matchers.isA(Collection.class), hasSize(greaterThan(0))));
    }

    private static List<RequestRepresentation> nonEmptyRequestList() {
        return argThat(allOf(org.hamcrest.Matchers.isA(Collection.class), hasSize(greaterThan(0))));
    }

    @Test
    public void createDraft() throws Exception {
        long databaseSizeBeforeCreate = requestRepository
                .findAllByRequesterAndStatus(mockRequesterUuid, RequestStatus.Draft, null).getTotalElements();

        RequestRepresentation request = newDraft(requester);

        Assert.assertNotNull(request.getUuid());

        long databaseSizeAfterCreate = requestRepository
                .findAllByRequesterAndStatus(mockRequesterUuid, RequestStatus.Draft, null).getTotalElements();
        Assert.assertEquals(databaseSizeBeforeCreate + 1, databaseSizeAfterCreate);
    }

    @Test
    public void fetchDrafts() throws Exception {
        RequestRepresentation request1 = newDraft(requester);
        RequestRepresentation request2 = newDraft(requester);
        Set<UUID> requestUuids = new TreeSet<>(Arrays.asList(request1.getUuid(), request2.getUuid()));

        mockMvc.perform(
            getRequest(HttpMethod.GET,
                REQUESTS_ROUTE + "/drafts",
                null,
                Collections.emptyMap())
            .with(token(requester))
            .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(result -> {
            log.info("Result: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
            List<RequestRepresentation> requests =
                mapper.readValue(result.getResponse().getContentAsByteArray(), listTypeReference);
            Assert.assertEquals(requestUuids.size(), requests.size());
            Set<UUID> resultUuids = new TreeSet<>();
            for(RequestRepresentation req: requests) {
                resultUuids.add(req.getUuid());
            }
            Assert.assertEquals(requestUuids, resultUuids);
        });
    }

    @Test
    public void deleteDraft() throws Exception {
        RequestRepresentation request = newDraft(requester);

        mockMvc.perform(
            getRequest(HttpMethod.DELETE,
                REQUESTS_ROUTE + "/drafts/" + request.getUuid().toString(),
                null,
                Collections.emptyMap())
                .with(token(requester))
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(result -> {
            log.info("Result: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
        });

        Request req = requestRepository.findOneByUuid(request.getUuid());
        Assert.assertNull(req);
    }

    @Test
    public void submitDraft() throws Exception {
        initMocks();
        // Setup a submitted draft
        getSubmittedDraft();

        Thread.sleep(1000);

        verify(this.mailService, times(1)).sendSubmissionNotificationToCoordinators(any(), any(), nonEmptyUserRepresentationList());
        verify(this.mailService, times(1)).sendSubmissionNotificationToRequester(any(), nonEmptyRequestList());
        verify(this.auditService, times(1)).publishEvent(any());

        // Fetch requests with status 'Review'
        mockMvc.perform(
            getRequest(HttpMethod.GET,
                REQUESTS_ROUTE + "/status/Review/requester",
                null,
                Collections.emptyMap())
                .with(token(requester))
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(result -> {
            log.info("Result: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
            List<RequestRepresentation> requests =
                mapper.readValue(result.getResponse().getContentAsByteArray(), listTypeReference);
            Assert.assertEquals(1, requests.size());
            for(RequestRepresentation req: requests) {
                Assert.assertEquals(RequestStatus.Review, req.getStatus());
                Request reqObj = requestRepository.findOneByUuid(req.getUuid());
                Assert.assertEquals(1, reqObj.getHistoricEvents().size());
            }
        });
    }

    @Test
    public void fetchRequesterRequests() throws Exception {
        initMocks();
        initFetchTests();

        // Fetch requests with status 'Review'
        mockMvc.perform(
            getRequest(HttpMethod.GET,
                REQUESTS_ROUTE + "/status/Review/requester",
                null,
                Collections.emptyMap())
                .with(token(requester))
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(result -> {
            log.info("Result: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
            List<RequestRepresentation> requests =
                mapper.readValue(result.getResponse().getContentAsByteArray(), listTypeReference);
            Assert.assertEquals(3, requests.size());
            for(RequestRepresentation req: requests) {
                Assert.assertEquals(RequestStatus.Review, req.getStatus());
            }
        });
    }

    @Test
    public void fetchCoordinatorRequests() throws Exception {
        initMocks();
        initFetchTests();

        // Fetch requests with status 'Review' for coordinator 1: should return 1 request
        mockMvc.perform(
            getRequest(HttpMethod.GET,
                REQUESTS_ROUTE + "/status/Review/coordinator",
                null,
                Collections.emptyMap())
                .with(token(coordinator1))
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(result -> {
            log.info("Result: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
            List<RequestRepresentation> requests =
                mapper.readValue(result.getResponse().getContentAsByteArray(), listTypeReference);
            Assert.assertEquals(1, requests.size());
            for(RequestRepresentation req: requests) {
                Assert.assertEquals(RequestStatus.Review, req.getStatus());
            }
        });

        // Fetch requests with status 'Review' for coordinator 2: should return 3 requests
        mockMvc.perform(
            getRequest(HttpMethod.GET,
                REQUESTS_ROUTE + "/status/Review/coordinator",
                null,
                Collections.emptyMap())
                .with(token(coordinator2))
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(result -> {
            log.info("Result: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
            List<RequestRepresentation> requests =
                mapper.readValue(result.getResponse().getContentAsByteArray(), listTypeReference);
            Assert.assertEquals(3, requests.size());
            for(RequestRepresentation req: requests) {
                Assert.assertEquals(RequestStatus.Review, req.getStatus());
            }
        });

        // Fetch requests with status 'Review' for coordinator 2, organisation 2: should return 2 requests
        mockMvc.perform(
            getRequest(HttpMethod.GET,
                REQUESTS_ROUTE + "/status/Review/organisation/" + organisationUuid2.toString() + "/coordinator",
                null,
                Collections.emptyMap())
                .with(token(coordinator2))
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(result -> {
            log.info("Result: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
            List<RequestRepresentation> requests =
                mapper.readValue(result.getResponse().getContentAsByteArray(), listTypeReference);
            Assert.assertEquals(2, requests.size());
            for(RequestRepresentation req: requests) {
                Assert.assertEquals(RequestStatus.Review, req.getStatus());
            }
        });
    }

    @Test
    public void fetchReviewerRequests() throws Exception {
        initMocks();
        initFetchTests();

        List<RequestRepresentation> coordinatorRequests = fetchAllForCoordinator(coordinator1);
        Assert.assertThat(coordinatorRequests.size(), greaterThan(0));

        UUID requestUuid = coordinatorRequests.get(0).getUuid();

        // Submit for review
        performProcessAction(coordinator1, ACTION_VALIDATE, requestUuid, HttpMethod.GET, null);

        // Fetch requests with status 'Review' for reviewer 1: should return 1 request
        mockMvc.perform(
            getRequest(HttpMethod.GET,
                REQUESTS_ROUTE + "/reviewer",
                null,
                Collections.emptyMap())
                .with(token(reviewer1))
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(result -> {
            log.info("Result: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
            List<RequestRepresentation> requests =
                mapper.readValue(result.getResponse().getContentAsByteArray(), listTypeReference);
            Assert.assertEquals(1, requests.size());
            for(RequestRepresentation req: requests) {
                Assert.assertEquals(requestUuid, req.getUuid());
                Assert.assertEquals(RequestStatus.Review, req.getStatus());
                Assert.assertEquals(RequestReviewStatus.Review, req.getRequestReview().getStatus());
            }
        });
    }

    @Test
    public void submitInvalidDraftRejected() throws Exception {
        RequestRepresentation request = newDraft(requester);

        setRequestData(request);

        List<OrganisationDTO> organisations = new ArrayList<>();
        OrganisationDTO organisation = new OrganisationDTO();
        organisation.setUuid(organisationUuid1);
        organisations.add(organisation);
        request.setOrganisations(organisations);

        request.getRequestDetail().setTitle(""); // invalid, should be rejected.

        request = updateDraft(requester, request);
        Assert.assertEquals(1, request.getOrganisations().size());
        Assert.assertEquals("", request.getRequestDetail().getTitle());

        // Submit the draft. One request should have been generated (and is returned).
        mockMvc.perform(
            getRequest(HttpMethod.GET,
                REQUESTS_ROUTE + "/drafts/" + request.getUuid().toString() + "/submit",
                null,
                Collections.emptyMap())
                .with(token(requester))
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isBadRequest())
        .andDo(result -> {
            log.info("Submitted result: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
        });
    }

    @Test
    public void rejectRequestFromValidation() throws Exception {
        initMocks();
        // Setup a submitted draft
        RequestRepresentation requestRepresentation = getSubmittedDraft();

        MessageRepresentation rejectionMessage = new MessageRepresentation();
        rejectionMessage.setSummary("Test rejection");

        // Reject the request.
        ResultActions rejectedRequest
            = performProcessAction(coordinator1, ACTION_REJECT, requestRepresentation.getUuid(), HttpMethod.POST, rejectionMessage);

        rejectedRequest
            .andDo(result -> {
                log.info("Result rejected request: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
                RequestRepresentation requestResult =
                    mapper.readValue(result.getResponse().getContentAsByteArray(), RequestRepresentation.class);

                Assert.assertEquals(ReviewProcessOutcome.Rejected, requestResult.getRequestReview().getDecision());
            })
            .andExpect(status().isOk());
    }

    @Test
    public void rejectRequestFromReview() throws Exception {
        initMocks();
        // Setup a submitted draft
        RequestRepresentation requestRepresentation = getSubmittedDraft();

        // Send for review
        ResultActions validatedRequest
            = performProcessAction(coordinator1, ACTION_VALIDATE, requestRepresentation.getUuid(), HttpMethod.GET, null);

        validatedRequest
            .andExpect(status().isOk())
            .andDo(result -> {
                log.info("Result validated request: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
                RequestRepresentation requestResult =
                    mapper.readValue(result.getResponse().getContentAsByteArray(), RequestRepresentation.class);
                Assert.assertEquals(RequestReviewStatus.Review, requestResult.getRequestReview().getStatus());
            });

        MessageRepresentation rejectionMessage = new MessageRepresentation();
        rejectionMessage.setSummary("Test rejection");

        // Reject the request.
        ResultActions rejectedRequest
            = performProcessAction(coordinator1, ACTION_REJECT, requestRepresentation.getUuid(), HttpMethod.POST, rejectionMessage);

        rejectedRequest
            .andExpect(status().isOk())
            .andDo(result -> {
                log.info("Result rejected request: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
                RequestRepresentation requestResult =
                    mapper.readValue(result.getResponse().getContentAsByteArray(), RequestRepresentation.class);

                Assert.assertEquals(ReviewProcessOutcome.Rejected, requestResult.getRequestReview().getDecision());
            });
    }

    @Test
    public void validateRequest() throws Exception {
        initMocks();
        // Setup a submitted draft
        RequestRepresentation requestRepresentation = getSubmittedDraft();

        // Send for review
        ResultActions validatedRequest
            = performProcessAction(coordinator1, ACTION_VALIDATE, requestRepresentation.getUuid(), HttpMethod.GET, null);

        validatedRequest
            .andExpect(status().isOk())
            .andDo(result -> {
                log.info("Result validated request: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
                RequestRepresentation requestResult =
                    mapper.readValue(result.getResponse().getContentAsByteArray(), RequestRepresentation.class);
                Assert.assertEquals(RequestReviewStatus.Review, requestResult.getRequestReview().getStatus());

                // Expect one review round to have been created
                Assert.assertEquals(requestResult.getReviewRounds().size(), 1);
                Assert.assertNull(requestResult.getReviewRounds().get(0).getEndDate());
            });
    }

    @Test
    public void approveReviewRequest() throws Exception {
        initMocks();
        RequestRepresentation requestRepresentation = getSubmittedDraft();

        // Send for review
        ResultActions validatedRequest
            = performProcessAction(coordinator1, ACTION_VALIDATE, requestRepresentation.getUuid(), HttpMethod.GET, null);

        validatedRequest
            .andExpect(status().isOk())
            .andDo(result -> {
                log.info("Result validated request: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
                RequestRepresentation requestResult =
                    mapper.readValue(result.getResponse().getContentAsByteArray(), RequestRepresentation.class);
                Assert.assertEquals(RequestReviewStatus.Review, requestResult.getRequestReview().getStatus());
            });

        // Approve the request.
        ResultActions approvedRequest
            = performProcessAction(coordinator1, ACTION_APPROVE, requestRepresentation.getUuid(), HttpMethod.GET, null);

        approvedRequest
            .andExpect(status().isOk())
            .andDo(result -> {
                log.info("Result approved request: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
                RequestRepresentation requestResult =
                    mapper.readValue(result.getResponse().getContentAsByteArray(), RequestRepresentation.class);
                Assert.assertEquals(ReviewProcessOutcome.Approved, requestResult.getRequestReview().getDecision());
            });
    }

    @Test
    public void sendRequestForRevisionFromValidation() throws Exception {
        initMocks();
        // Setup a submitted draft
        RequestRepresentation requestRepresentation = getSubmittedDraft();

        MessageRepresentation revisionMessage = new MessageRepresentation();
        revisionMessage.setSummary("Test revision. Please change fields xyz");

        // Send for revision
        ResultActions revisedRequest
            = performProcessAction(coordinator1, ACTION_REQUEST_REVISION, requestRepresentation.getUuid(), HttpMethod.POST, revisionMessage);

        revisedRequest
            .andExpect(status().isOk())
            .andDo(result -> {
                log.info("Result revised request: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
                RequestRepresentation requestResult =
                    mapper.readValue(result.getResponse().getContentAsByteArray(), RequestRepresentation.class);
                Assert.assertEquals(RequestReviewStatus.Revision, requestResult.getRequestReview().getStatus());
            });
    }

}
