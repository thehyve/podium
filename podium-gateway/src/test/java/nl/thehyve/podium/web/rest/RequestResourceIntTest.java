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
import nl.thehyve.podium.service.AuditService;
import nl.thehyve.podium.service.MailService;
import nl.thehyve.podium.service.OrganisationClientService;
import nl.thehyve.podium.service.UserClientService;
import nl.thehyve.podium.service.representation.PrincipalInvestigatorRepresentation;
import nl.thehyve.podium.service.representation.RequestDetailRepresentation;
import nl.thehyve.podium.service.representation.RequestRepresentation;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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

    private JsonParser parser = JsonParserFactory.getJsonParser();

    private TypeReference<List<RequestRepresentation>> listTypeReference =
        new TypeReference<List<RequestRepresentation>>(){};

    private MockMvc mockMvc;

    private UserAuthenticationToken requester;

    private static final String mockRequesterUsername = "test";
    private static final String mockRequesterPassword = "Password1!";
    private static UUID mockRequesterUuid = UUID.randomUUID();
    private static Set<String> mockRequesterAuthorities =
        Sets.newSet(AuthorityConstants.RESEARCHER);
    private static UUID organisationUuid = UUID.randomUUID();
    private static UUID coordinatorUuid = UUID.randomUUID();

    public static final String REQUESTS_ROUTE = "/api/requests";
    public static final String REQUESTS_SEARCH_ROUTE = "/api/_search/requests";

    private static OrganisationDTO createOrganisation() {
        OrganisationDTO organisation = new OrganisationDTO();
        organisation.setUuid(organisationUuid);
        organisation.setName("Test organisation");
        organisation.setShortName("Test");
        organisation.setActivated(true);
        return organisation;
    }

    private static UserRepresentation createCoordinator() {
        UserRepresentation coordinator = new UserRepresentation();
        coordinator.setUuid(coordinatorUuid);
        coordinator.setLogin("coordinator");
        coordinator.setFirstName("Co");
        coordinator.setLastName("Ordinator");
        coordinator.setEmail("coordinator@local");
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

        SerialisedUser mockRequester = new SerialisedUser(
            mockRequesterUuid, mockRequesterUsername, mockRequesterAuthorities, null);
        requester = new UserAuthenticationToken(mockRequester);
        requester.setAuthenticated(true);

        this.mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    private void initMocks() throws URISyntaxException {
        OrganisationDTO organisation = createOrganisation();
        given(this.organisationService.findOrganisationByUuid(any()))
            .willReturn(organisation);
        List<UserRepresentation> coordinators = new ArrayList<>();
        coordinators.add(createCoordinator());
        given(this.organisationService.findUsersByRole(any(), any()))
            .willReturn(coordinators);
        doNothing().when(this.mailService).sendSubmissionNotificationToCoordinators(any(), any(), anyListOf(UserRepresentation.class));

        UserRepresentation requesterRepresentation = createRequester();
        given(this.userClientService.findUserByUuid(any()))
            .willReturn(requesterRepresentation);

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
        .andDo(result -> {
            log.info("Result: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
            request[0] = mapper.readValue(result.getResponse().getContentAsByteArray(), RequestRepresentation.class);
        })
        .andExpect(status().isCreated());
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
        })
        .andExpect(status().isOk());
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
        .andDo(result -> {
            log.info("Result: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
        })
        .andExpect(status().isOk());

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
            .andDo(result -> {
                log.info("Result: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
                resultRequest[0] = mapper.readValue(result.getResponse().getContentAsByteArray(), RequestRepresentation.class);
            })
            .andExpect(status().isOk());
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
        UUID organisationUuid = UUID.randomUUID();
        organisation.setUuid(organisationUuid);
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
        .andDo(result -> {
            log.info("Submitted result: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
            List<RequestRepresentation> requests =
                mapper.readValue(result.getResponse().getContentAsByteArray(), listTypeReference);
            Assert.assertEquals(1, requests.size());
            for (RequestRepresentation req: requests) {
                Assert.assertEquals(RequestStatus.Review, req.getStatus());
                Assert.assertEquals(1, req.getOrganisations().size());
                Assert.assertEquals(organisationUuid, req.getOrganisations().get(0).getUuid());
            }
        })
        .andExpect(status().isOk());

        Thread.sleep(1);

        verify(this.mailService, times(1)).sendSubmissionNotificationToCoordinators(any(), any(), nonEmptyUserRepresentationList());
        verify(this.mailService, times(1)).sendSubmissionNotificationToRequester(any(), nonEmptyRequestList(), mapContainsKey(organisationUuid));
        verify(this.auditService, times(1)).publishEvent(any());

        // Fetch requests with status 'Review'
        mockMvc.perform(
            getRequest(HttpMethod.GET,
                REQUESTS_ROUTE + "/status/Review",
                null,
                Collections.emptyMap())
                .with(token(requester))
                .accept(MediaType.APPLICATION_JSON)
        )
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
        })
        .andExpect(status().isOk());
    }

    @Test
    @Transactional
    public void submitInvalidDraftRejected() throws Exception {
        RequestRepresentation request = newDraft(requester);

        setRequestData(request);

        List<OrganisationDTO> organisations = new ArrayList<>();
        OrganisationDTO organisation = new OrganisationDTO();
        organisation.setUuid(organisationUuid);
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
        .andDo(result -> {
            log.info("Submitted result: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
        })
        .andExpect(status().isBadRequest());
    }

}
