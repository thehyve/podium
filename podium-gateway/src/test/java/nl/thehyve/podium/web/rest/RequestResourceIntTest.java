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
import nl.thehyve.podium.common.enumeration.RequestStatus;
import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.common.security.SerialisedUser;
import nl.thehyve.podium.common.security.UserAuthenticationToken;
import nl.thehyve.podium.common.service.dto.OrganisationDTO;
import nl.thehyve.podium.common.service.dto.UserRepresentation;
import nl.thehyve.podium.config.SecurityBeanOverrideConfiguration;
import nl.thehyve.podium.domain.Request;
import nl.thehyve.podium.repository.RequestRepository;
import nl.thehyve.podium.repository.search.RequestSearchRepository;
import nl.thehyve.podium.security.OAuth2TokenMockUtil;
import nl.thehyve.podium.service.MailService;
import nl.thehyve.podium.service.OrganisationClientService;
import nl.thehyve.podium.service.UserClientService;
import nl.thehyve.podium.service.representation.PrincipalInvestigatorRepresentation;
import nl.thehyve.podium.service.representation.RequestDetailRepresentation;
import nl.thehyve.podium.service.representation.RequestRepresentation;
import org.assertj.core.util.Maps;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.collections.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
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

import java.util.*;
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
    private RequestRepository requestRepository;

    @Autowired
    private RequestSearchRepository requestSearchRepository;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private OAuth2TokenMockUtil tokenUtil;

    @MockBean
    OrganisationClientService organisationService;

    @MockBean
    UserClientService userClientService;

    @MockBean
    private MailService mailService;

    private ObjectMapper mapper = new ObjectMapper();

    private JsonParser parser = JsonParserFactory.getJsonParser();

    private TypeReference<List<RequestRepresentation>> listTypeReference =
        new TypeReference<List<RequestRepresentation>>(){};

    private MockMvc mockMvc;

    private UserAuthenticationToken requester;
    private UserAuthenticationToken coordinator1;
    private UserAuthenticationToken coordinator2;

    private static final String mockRequesterUsername = "test";
    private static final String mockRequesterPassword = "Password1!";
    private static UUID mockRequesterUuid = UUID.randomUUID();
    private static Set<String> mockRequesterAuthorities =
        Sets.newSet(AuthorityConstants.RESEARCHER);
    private static UUID organisationUuid1 = UUID.randomUUID();
    private static UUID organisationUuid2 = UUID.randomUUID();
    private static UUID coordinatorUuid1 = UUID.randomUUID();
    private static UUID coordinatorUuid2 = UUID.randomUUID();

    public static final String REQUESTS_ROUTE = "/api/requests";
    public static final String REQUESTS_SEARCH_ROUTE = "/api/_search/requests";

    private static OrganisationDTO createOrganisation(int i, UUID uuid) {
        OrganisationDTO organisation = new OrganisationDTO();
        organisation.setUuid(uuid);
        organisation.setName("Test organisation " + i);
        organisation.setShortName("Test" + i);
        organisation.setActivated(true);
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

    private static UserRepresentation createRequester() {
        UserRepresentation requesterRepresentation = new UserRepresentation();
        requesterRepresentation.setUuid(mockRequesterUuid);
        requesterRepresentation.setLogin(mockRequesterUsername);
        requesterRepresentation.setFirstName("Re");
        requesterRepresentation.setLastName("Quester");
        requesterRepresentation.setEmail("requester@local");
        return requesterRepresentation;
    }

    @Before
    public void setup() {
        requestSearchRepository.deleteAll();

        MockitoAnnotations.initMocks(this);

        // Mock authentication for the requester
        SerialisedUser mockRequester = new SerialisedUser(
            mockRequesterUuid, mockRequesterUsername, mockRequesterAuthorities, null);
        requester = new UserAuthenticationToken(mockRequester);
        requester.setAuthenticated(true);

        // Coordinator 1 is coordinator of mock organisation 1
        SerialisedUser mockCoordinator1 = new SerialisedUser(
            coordinatorUuid1, "coordinator1", mockRequesterAuthorities,
            Maps.newHashMap(organisationUuid1, Sets.newSet(AuthorityConstants.ORGANISATION_COORDINATOR)));
        coordinator1 = new UserAuthenticationToken(mockCoordinator1);
        coordinator1.setAuthenticated(true);

        // Coordinator 2 is coordinator of both mock organisations
        Map<UUID, Collection<String>> coordinator2Roles = new HashMap<>();
        coordinator2Roles.put(organisationUuid1, Sets.newSet(AuthorityConstants.ORGANISATION_COORDINATOR));
        coordinator2Roles.put(organisationUuid2, Sets.newSet(AuthorityConstants.ORGANISATION_COORDINATOR));
        SerialisedUser mockCoordinator2 = new SerialisedUser(
            coordinatorUuid2, "coordinator2", mockRequesterAuthorities, coordinator2Roles);
        coordinator2 = new UserAuthenticationToken(mockCoordinator2);
        coordinator2.setAuthenticated(true);

        this.mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    private void initMocks() throws URISyntaxException {
        // Mock organisation service
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
        // Mock notification call
        doNothing().when(this.mailService).sendSubmissionNotificationToCoordinators(any(), any(), anyListOf(UserRepresentation.class));

        UserRepresentation requesterRepresentation = createRequester();
        given(this.userClientService.findUserByUuid(any()))
            .willReturn(requesterRepresentation);
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

        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

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
        return request[0];
    }

    @Test
    @Transactional
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
    @Transactional
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
    @Transactional
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

    private static List<UserRepresentation> nonEmptyUserRepresentationList() {
        return argThat(allOf(org.hamcrest.Matchers.isA(Collection.class), hasSize(greaterThan(0))));
    }

    private static List<Request> nonEmptyRequestList() {
        return argThat(allOf(org.hamcrest.Matchers.isA(Collection.class), hasSize(greaterThan(0))));
    }

    private static Map<UUID, OrganisationDTO> mapContainsKey(Object key) {
        return argThat(allOf(org.hamcrest.Matchers.isA(Map.class), hasKey(key)));
    }

    @Test
    @Transactional
    public void submitDraft() throws Exception {
        initMocks();

        RequestRepresentation request = newDraft(requester);

        setRequestData(request);

        List<OrganisationDTO> organisations = new ArrayList<>();
        OrganisationDTO organisation = new OrganisationDTO();
        organisation.setUuid(organisationUuid1);
        organisations.add(organisation);
        request.setOrganisations(organisations);

        request = updateDraft(requester, request);
        Assert.assertEquals(1, request.getOrganisations().size());

        // Submit the draft. One request should have been generated (and is returned).
        mockMvc.perform(
            getRequest(HttpMethod.GET,
                REQUESTS_ROUTE + "/drafts/" + request.getUuid().toString() + "/submit",
                null,
                Collections.emptyMap())
                .with(token(requester))
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(result -> {
            log.info("Submitted result: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
            List<RequestRepresentation> requests =
                mapper.readValue(result.getResponse().getContentAsByteArray(), listTypeReference);
            Assert.assertEquals(1, requests.size());
            for (RequestRepresentation req: requests) {
                Assert.assertEquals(RequestStatus.Review, req.getStatus());
                Assert.assertEquals(1, req.getOrganisations().size());
                Assert.assertEquals(organisationUuid1, req.getOrganisations().get(0).getUuid());
            }
        });

        verify(this.mailService, times(1)).sendSubmissionNotificationToCoordinators(any(), any(), nonEmptyUserRepresentationList());
        verify(this.mailService, times(1)).sendSubmissionNotificationToRequester(any(), nonEmptyRequestList(), mapContainsKey(organisationUuid1));

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
            }
        });
    }

    private void createAndSubmitDraft(Set<UUID> organisationUuids) throws Exception {
        RequestRepresentation request = newDraft(requester);
        setRequestData(request);
        List<OrganisationDTO> organisations = new ArrayList<>();
        for(UUID uuid: organisationUuids) {
            OrganisationDTO organisation = new OrganisationDTO();
            organisation.setUuid(uuid);
            organisations.add(organisation);
        }
        request.setOrganisations(organisations);

        request = updateDraft(requester, request);
        Assert.assertEquals(organisationUuids.size(), request.getOrganisations().size());

        // Submit the draft. Requests should have been generated.
        mockMvc.perform(
            getRequest(HttpMethod.GET,
                REQUESTS_ROUTE + "/drafts/" + request.getUuid().toString() + "/submit",
                null,
                Collections.emptyMap())
                .with(token(requester))
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk());
    }

    private void initFetchTests() throws Exception {
        initMocks();

        createAndSubmitDraft(Sets.newSet(organisationUuid1, organisationUuid2));

        createAndSubmitDraft(Sets.newSet(organisationUuid2));
    }

    @Test
    @Transactional
    public void fetchRequesterRequests() throws Exception {
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
    @Transactional
    public void fetchCoordinatorRequests() throws Exception {
        initFetchTests();

        // Fetch requests with status 'Review' for coordinator 1: should return 1 request
        mockMvc.perform(
            getRequest(HttpMethod.GET,
                REQUESTS_ROUTE + "/status/Review/organisation",
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
                REQUESTS_ROUTE + "/status/Review/organisation",
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
                REQUESTS_ROUTE + "/status/Review/organisation/" + organisationUuid2.toString(),
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
    @Transactional
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

}
