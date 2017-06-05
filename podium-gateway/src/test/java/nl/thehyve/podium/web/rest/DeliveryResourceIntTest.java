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
import nl.thehyve.podium.common.enumeration.DeliveryProcessOutcome;
import nl.thehyve.podium.common.enumeration.DeliveryStatus;
import nl.thehyve.podium.common.enumeration.RequestReviewStatus;
import nl.thehyve.podium.common.enumeration.RequestStatus;
import nl.thehyve.podium.common.enumeration.RequestType;
import nl.thehyve.podium.common.enumeration.ReviewProcessOutcome;
import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.common.security.SerialisedUser;
import nl.thehyve.podium.common.security.UserAuthenticationToken;
import nl.thehyve.podium.common.service.dto.DeliveryProcessRepresentation;
import nl.thehyve.podium.common.service.dto.DeliveryReferenceRepresentation;
import nl.thehyve.podium.common.service.dto.MessageRepresentation;
import nl.thehyve.podium.common.service.dto.OrganisationDTO;
import nl.thehyve.podium.common.service.dto.PodiumEventRepresentation;
import nl.thehyve.podium.common.service.dto.PrincipalInvestigatorRepresentation;
import nl.thehyve.podium.common.service.dto.RequestDetailRepresentation;
import nl.thehyve.podium.common.service.dto.RequestRepresentation;
import nl.thehyve.podium.common.service.dto.UserRepresentation;
import nl.thehyve.podium.common.test.OAuth2TokenMockUtil;
import nl.thehyve.podium.config.SecurityBeanOverrideConfiguration;
import nl.thehyve.podium.service.AuditService;
import nl.thehyve.podium.service.MailService;
import nl.thehyve.podium.service.OrganisationClientService;
import nl.thehyve.podium.service.TestService;
import nl.thehyve.podium.service.UserClientService;
import org.junit.After;
import org.junit.Assert;
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

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
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
public class DeliveryResourceIntTest {

    private Logger log = LoggerFactory.getLogger(DeliveryResourceIntTest.class);

    @Autowired
    private TestService testService;

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

    private TypeReference<List<RequestRepresentation>> requestListTypeReference =
        new TypeReference<List<RequestRepresentation>>(){};

    private TypeReference<List<DeliveryProcessRepresentation>> deliveryProcessListTypeReference =
        new TypeReference<List<DeliveryProcessRepresentation>>(){};

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
    private static final String ACTION_START_DELIVERY = "startDelivery";
    private static final String DELIVERY_RELEASE = "release";
    private static final String DELIVERY_RECEIVED = "received";
    private static final String DELIVERY_CANCEL = "cancel";

    private static final String mockRequesterUsername = "requester";
    private static UUID mockRequesterUuid = UUID.randomUUID();

    private static Set<String> requesterAuthorities =
        Sets.newSet(AuthorityConstants.RESEARCHER);
    private static Set<String> coordinatorAuthorities =
        Sets.newSet(AuthorityConstants.ORGANISATION_COORDINATOR);
    private static Set<String> reviewerAuthorities =
        Sets.newSet(AuthorityConstants.REVIEWER);

    public static final String REQUESTS_ROUTE = "/api/requests";

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

        // Mock notification calls
        doNothing().when(this.mailService).sendSubmissionNotificationToCoordinators(any(), any(), anyListOf(UserRepresentation.class));
        doNothing().when(this.mailService).sendDeliveryReleasedNotificationToRequester(any(), any(), any());
        doNothing().when(this.mailService).sendDeliveryReceivedNotificationToCoordinators(any(), any(), any(), anyListOf(UserRepresentation.class));

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
        details.setRequestType(new HashSet<>(Arrays.asList(RequestType.Data, RequestType.Material)));
        PrincipalInvestigatorRepresentation principalInvestigator = details.getPrincipalInvestigator();
        principalInvestigator.setName("Test Person");
        principalInvestigator.setEmail("pi@local");
        principalInvestigator.setJobTitle("Tester");
        principalInvestigator.setAffiliation("The Organisation");
    }

    /**
     *
     * @param user The authenticated user performing the action
     * @param action The action to perform
     * @param requestUuid The UUID of the request to perform the action on
     * @param method The HttpMethod required to perform the action
     * @param message The message to pass as body of the request (null if not applicable)
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

    /**
     *
     * @param user The authenticated user performing the action
     * @param action The action to perform
     * @param requestUuid The UUID of the request to perform the action on
     * @param deliveryProcessUuid The UUID of the delivery process to perform the action on
     * @param body The object to pass as body of the request (null if not applicable)
     * @param method The HttpMethod required to perform the action
     * @return
     * @throws Exception
     */
    private ResultActions performDeliveryAction(
        UserAuthenticationToken user, String action, UUID requestUuid, UUID deliveryProcessUuid, HttpMethod method, Object body
    ) throws Exception {
        return mockMvc.perform(
            getRequest(method,
                REQUESTS_ROUTE + "/" + requestUuid.toString() + "/deliveries/" + deliveryProcessUuid.toString() + "/" + action,
                body,
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
        .andExpect(status().isOk())
        .andDo(result -> {
            log.info("Submitted result: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
            List<RequestRepresentation> requests =
                mapper.readValue(result.getResponse().getContentAsByteArray(), requestListTypeReference);

            // Number of requests should equal the number of organisations it was submitted to
            Assert.assertEquals(organisations.size(), requests.size());
            for (RequestRepresentation req: requests) {
                Assert.assertEquals(RequestStatus.Review, req.getStatus());
                Assert.assertEquals(RequestReviewStatus.Validation, req.getRequestReview().getStatus());
            }
            res[0] = requests;
        });

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

    private RequestRepresentation getApprovedRequest() throws Exception {
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

        final RequestRepresentation[] res = new RequestRepresentation[1];

        approvedRequest
            .andExpect(status().isOk())
            .andDo(result -> {
                log.info("Result approved request: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
                RequestRepresentation requestResult =
                    mapper.readValue(result.getResponse().getContentAsByteArray(), RequestRepresentation.class);
                res[0] = requestResult;
                Assert.assertEquals(ReviewProcessOutcome.Approved, requestResult.getRequestReview().getDecision());
            });
        return res[0];
    }

    private List<DeliveryProcessRepresentation> createDeliveryProcesses(RequestRepresentation request) throws Exception {
        final List<DeliveryProcessRepresentation>[] res = new List[1];
        // Start delivery.
        ResultActions startDeliveryResult
            = performProcessAction(coordinator1, ACTION_START_DELIVERY, request.getUuid(), HttpMethod.GET, null);

        startDeliveryResult
            .andExpect(status().isOk())
            .andDo(result -> {
                log.info("Result delivery processes: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
                List<DeliveryProcessRepresentation> deliveryProcesses =
                    mapper.readValue(result.getResponse().getContentAsByteArray(), deliveryProcessListTypeReference);
                res[0] = deliveryProcesses;
            });
        return res[0];
    }

    @Test
    public void startDeliveryProcesses() throws Exception {
        initMocks();
        RequestRepresentation request = getApprovedRequest();

        Thread.sleep(1000);
        reset(this.auditService);

        List<DeliveryProcessRepresentation> deliveryProcesses = createDeliveryProcesses(request);
        Assert.assertEquals(2, deliveryProcesses.size());

        // One request update, two delivery process updates
        verify(this.auditService, times(3)).publishEvent(any());
    }

    @Test
    public void releaseDelivery() throws Exception {
        initMocks();
        RequestRepresentation request = getApprovedRequest();
        List<DeliveryProcessRepresentation> deliveryProcesses = createDeliveryProcesses(request);
        DeliveryProcessRepresentation deliveryProcess = deliveryProcesses.get(0);

        Thread.sleep(1000);
        reset(this.auditService);

        // Release
        DeliveryReferenceRepresentation reference = new DeliveryReferenceRepresentation();
        String downloadUrl = "http://example.com/downloadData";
        reference.setReference(downloadUrl);
        ResultActions releaseDeliveryResult
            = performDeliveryAction(coordinator1, DELIVERY_RELEASE, request.getUuid(), deliveryProcess.getUuid(), HttpMethod.POST, reference);

        releaseDeliveryResult
            .andExpect(status().isOk())
            .andDo(result -> {
                log.info("Result delivery process: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
                DeliveryProcessRepresentation resultDeliveryProcess =
                    mapper.readValue(result.getResponse().getContentAsByteArray(), DeliveryProcessRepresentation.class);
                Assert.assertEquals(DeliveryStatus.Released, resultDeliveryProcess.getStatus());
                Assert.assertEquals(downloadUrl, resultDeliveryProcess.getReference());
            });

        Thread.sleep(1000);

        // Test if requester has been notified
        verify(this.mailService, times(1)).sendDeliveryReleasedNotificationToRequester(any(), any(), any());
        // Test status update event
        verify(this.auditService, times(1)).publishEvent(any());
    }

    @Test
    public void deliveryReceived() throws Exception {
        initMocks();
        RequestRepresentation request = getApprovedRequest();
        List<DeliveryProcessRepresentation> deliveryProcesses = createDeliveryProcesses(request);
        DeliveryProcessRepresentation deliveryProcess = deliveryProcesses.get(0);

        // Release
        DeliveryReferenceRepresentation reference = new DeliveryReferenceRepresentation();
        String downloadUrl = "http://example.com/downloadData";
        reference.setReference(downloadUrl);
        ResultActions releaseDeliveryResult
            = performDeliveryAction(coordinator1, DELIVERY_RELEASE, request.getUuid(), deliveryProcess.getUuid(), HttpMethod.POST, reference);

        releaseDeliveryResult
            .andExpect(status().isOk())
            .andDo(result -> {
                log.info("Result delivery process: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
                DeliveryProcessRepresentation resultDeliveryProcess =
                    mapper.readValue(result.getResponse().getContentAsByteArray(), DeliveryProcessRepresentation.class);
                Assert.assertEquals(DeliveryStatus.Released, resultDeliveryProcess.getStatus());
            });

        Thread.sleep(1000);
        reset(this.auditService);

        // Received
        ResultActions receivedDeliveryResult
            = performDeliveryAction(requester, DELIVERY_RECEIVED, request.getUuid(), deliveryProcess.getUuid(), HttpMethod.GET, reference);

        receivedDeliveryResult
            .andExpect(status().isOk())
            .andDo(result -> {
                log.info("Result delivery process: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
                DeliveryProcessRepresentation resultDeliveryProcess =
                    mapper.readValue(result.getResponse().getContentAsByteArray(), DeliveryProcessRepresentation.class);
                Assert.assertEquals(DeliveryStatus.Closed, resultDeliveryProcess.getStatus());
                Assert.assertEquals(DeliveryProcessOutcome.Received, resultDeliveryProcess.getOutcome());
                Assert.assertEquals(downloadUrl, resultDeliveryProcess.getReference());
            });

        Thread.sleep(1000);

        // Test if requester has been notified
        verify(this.mailService, times(1)).sendDeliveryReceivedNotificationToCoordinators(any(), any(), any(), anyListOf(UserRepresentation.class));
        // Test status update events
        verify(this.auditService, times(1)).publishEvent(any());
    }

    private void testCancel(RequestRepresentation request, DeliveryProcessRepresentation deliveryProcess) throws Exception {
        // Cancel
        MessageRepresentation message = new MessageRepresentation();
        String summary = "Delivery cancelled";
        message.setSummary(summary);
        message.setDescription("Cancelled because materials were damaged.");
        ResultActions rejectDeliveryResult
            = performDeliveryAction(coordinator1, DELIVERY_CANCEL, request.getUuid(), deliveryProcess.getUuid(), HttpMethod.POST, message);

        rejectDeliveryResult
            .andExpect(status().isOk())
            .andDo(result -> {
                log.info("Result delivery process: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
                DeliveryProcessRepresentation resultDeliveryProcess =
                    mapper.readValue(result.getResponse().getContentAsByteArray(), DeliveryProcessRepresentation.class);
                Assert.assertEquals(DeliveryStatus.Closed, resultDeliveryProcess.getStatus());
                Assert.assertEquals(DeliveryProcessOutcome.Cancelled, resultDeliveryProcess.getOutcome());
                List<PodiumEventRepresentation> events = resultDeliveryProcess.getHistoricEvents();
                Assert.assertNotEquals(0, events.size());
                events.forEach(event -> log.info("Event: {}", event));
                PodiumEventRepresentation latestEvent = events.get(events.size() - 1);
                Assert.assertEquals(summary, latestEvent.getData().get("messageSummary"));
            });
    }

    @Test
    public void cancelDeliveryAfterStart() throws Exception {
        initMocks();
        RequestRepresentation request = getApprovedRequest();
        List<DeliveryProcessRepresentation> deliveryProcesses = createDeliveryProcesses(request);
        DeliveryProcessRepresentation deliveryProcess = deliveryProcesses.get(0);

        Thread.sleep(1000);
        reset(this.auditService);

        testCancel(request, deliveryProcess);

        Thread.sleep(1000);

        // Test status update events
        verify(this.auditService, times(1)).publishEvent(any());
    }

    @Test
    public void cancelReleasedDelivery() throws Exception {
        initMocks();
        RequestRepresentation request = getApprovedRequest();
        List<DeliveryProcessRepresentation> deliveryProcesses = createDeliveryProcesses(request);
        DeliveryProcessRepresentation deliveryProcess = deliveryProcesses.get(0);

        Thread.sleep(1000);
        reset(this.auditService);

        // Release
        DeliveryReferenceRepresentation reference = new DeliveryReferenceRepresentation();
        ResultActions releaseDeliveryResult
            = performDeliveryAction(coordinator1, DELIVERY_RELEASE, request.getUuid(), deliveryProcess.getUuid(), HttpMethod.POST, reference);

        releaseDeliveryResult
            .andExpect(status().isOk())
            .andDo(result -> {
                log.info("Result delivery process: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
                DeliveryProcessRepresentation resultDeliveryProcess =
                    mapper.readValue(result.getResponse().getContentAsByteArray(), DeliveryProcessRepresentation.class);
                Assert.assertEquals(DeliveryStatus.Released, resultDeliveryProcess.getStatus());
            });

        testCancel(request, deliveryProcess);

        Thread.sleep(1000);

        // Test status update events
        verify(this.auditService, times(2)).publishEvent(any());
    }

}
