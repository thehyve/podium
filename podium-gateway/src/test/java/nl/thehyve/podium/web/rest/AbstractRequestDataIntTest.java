package nl.thehyve.podium.web.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import nl.thehyve.podium.common.enumeration.*;
import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.common.security.SerialisedUser;
import nl.thehyve.podium.common.security.UserAuthenticationToken;
import nl.thehyve.podium.common.service.dto.*;
import nl.thehyve.podium.common.test.OAuth2TokenMockUtil;
import nl.thehyve.podium.repository.RequestRepository;
import nl.thehyve.podium.repository.search.RequestSearchRepository;
import nl.thehyve.podium.service.*;
import nl.thehyve.podium.common.service.dto.RequestFileRepresentation;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.collections.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.*;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.*;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class AbstractRequestDataIntTest {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    TestService testService;

    @Autowired
    RequestRepository requestRepository;

    @Autowired
    RequestSearchRepository requestSearchRepository;

    @Autowired
    WebApplicationContext context;

    @Autowired
    OAuth2TokenMockUtil tokenUtil;

    @MockBean
    OrganisationClientService organisationService;

    @MockBean
    UserClientService userClientService;

    @MockBean
    MailService mailService;

    @MockBean
    AuditService auditService;

    ObjectMapper mapper = new ObjectMapper();

    TypeReference<List<RequestRepresentation>> listTypeReference =
        new TypeReference<List<RequestRepresentation>>(){};

    TypeReference<List<DeliveryProcessRepresentation>> deliveryProcessListTypeReference =
        new TypeReference<List<DeliveryProcessRepresentation>>(){};

    TypeReference<Map<OverviewStatus, Long>> countsTypeReference =
        new TypeReference<Map<OverviewStatus, Long>>(){};

    TypeReference<List<RequestFileRepresentation>> requestFileListTypeReference =
            new TypeReference<List<RequestFileRepresentation>>(){};

    MockMvc mockMvc;

    UserAuthenticationToken requester;
    UserAuthenticationToken coordinator1;
    UserAuthenticationToken coordinator2;
    UserAuthenticationToken reviewer1;
    UserAuthenticationToken reviewer2;

    final UUID organisationUuid1 = UUID.randomUUID();
    final UUID organisationUuid2 = UUID.randomUUID();
    final UUID coordinatorUuid1 = UUID.randomUUID();
    final UUID coordinatorUuid2 = UUID.randomUUID();
    final UUID reviewerUuid1 = UUID.randomUUID();
    final UUID reviewerUuid2 = UUID.randomUUID();

    final String ACTION_VALIDATE = "validate";
    final String ACTION_APPROVE = "approve";
    final String ACTION_REQUEST_REVISION = "requestRevision";
    final String ACTION_REJECT = "reject";
    final String ACTION_CLOSE = "close";
    final String ACTION_START_DELIVERY = "startDelivery";
    final String ACTION_GET_DELIVERIES = "deliveries";
    final String DELIVERY_RELEASE = "release";
    final String DELIVERY_RECEIVED = "received";
    final String DELIVERY_CANCEL = "cancel";
    final String ACTION_SUBMIT_REVIEW_FEEDBACK = "review";
    final String ACTION_GET_FILES = "files";

    final String TEST_TWO_ORGANISATIONS_TITLE = "Test request to two organisations";

    static final String mockRequesterUsername = "requester";
    static UUID mockRequesterUuid = UUID.randomUUID();

    final Set<String> requesterAuthorities =
        Sets.newSet(AuthorityConstants.RESEARCHER);

    final String REQUESTS_ROUTE = "/api/requests";

    final Map<UUID, OrganisationRepresentation> organisations = new HashMap<>();
    final Map<UUID, UserRepresentation> users = new HashMap<>();

    OrganisationRepresentation createOrganisation(int i, UUID uuid) {
        OrganisationRepresentation organisation = new OrganisationRepresentation();
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

    UserRepresentation createCoordinator(int i, UUID uuid) {
        UserRepresentation coordinator = new UserRepresentation();
        coordinator.setUuid(uuid);
        coordinator.setLogin("coordinator" + i);
        coordinator.setFirstName("Co " + i);
        coordinator.setLastName("Ordinator");
        coordinator.setEmail("coordinator" + i + "@local");
        return coordinator;
    }

    UserRepresentation createReviewer(int i, UUID uuid) {
        UserRepresentation coordinator = new UserRepresentation();
        coordinator.setUuid(uuid);
        coordinator.setLogin("reviewer" + i);
        coordinator.setFirstName("Re " + i);
        coordinator.setLastName("Viewer");
        coordinator.setEmail("reviewer" + i + "@local");
        return coordinator;
    }

    UserRepresentation createRequester() {
        UserRepresentation requesterRepresentation = new UserRepresentation();
        requesterRepresentation.setUuid(mockRequesterUuid);
        requesterRepresentation.setLogin(mockRequesterUsername);
        requesterRepresentation.setFirstName("Re");
        requesterRepresentation.setLastName("Quester");
        requesterRepresentation.setEmail("requester@local");
        return requesterRepresentation;
    }

    Map<UUID, Collection<String>> createOrganisationRole(UUID organisationUuid, String authority) {
        Map<UUID, Collection<String>> roles = new HashMap<>();
        roles.put(organisationUuid, Sets.newSet(authority));
        return roles;
    }
    @Before
    public void setup() {
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

        // Reviewer 2 is reviewer of both mock organisations
        {
            Map<UUID, Collection<String>> roles = new HashMap<>();
            roles.put(organisationUuid1, Sets.newSet(AuthorityConstants.REVIEWER));
            roles.put(organisationUuid2, Sets.newSet(AuthorityConstants.REVIEWER));
            SerialisedUser mockReviewer = new SerialisedUser(
                reviewerUuid2, "reviewer2", requesterAuthorities,
                roles);
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

    void initMocks() {
        // Mock organisation service for fetching organisation info through Feign
        OrganisationRepresentation organisation1 = createOrganisation(1, organisationUuid1);
        organisations.put(organisationUuid1, organisation1);
        OrganisationRepresentation organisation2 = createOrganisation(2, organisationUuid2);
        organisations.put(organisationUuid2, organisation2);
        for(Map.Entry<UUID, OrganisationRepresentation> entry: organisations.entrySet()) {
            given(this.organisationService.findOrganisationByUuid(eq(entry.getKey())))
                .willReturn(entry.getValue());
            given(this.organisationService.findOrganisationByUuidCached(eq(entry.getKey())))
                .willReturn(entry.getValue());
        }

        // Mock organisations service for fetching coordinators
        UserRepresentation coordinatorRepresentation1 = createCoordinator(1, coordinatorUuid1);
        users.put(coordinatorUuid1, coordinatorRepresentation1);
        UserRepresentation coordinatorRepresentation2 = createCoordinator(2, coordinatorUuid2);
        users.put(coordinatorUuid2, coordinatorRepresentation2);
        List<UserRepresentation> coordinators1 = new ArrayList<>();
        coordinators1.add(coordinatorRepresentation1);
        coordinators1.add(coordinatorRepresentation2);
        given(this.organisationService.findUsersByRole(eq(organisationUuid1), eq(AuthorityConstants.ORGANISATION_COORDINATOR)))
            .willReturn(coordinators1);
        List<UserRepresentation> coordinators2 = new ArrayList<>();
        coordinators1.add(coordinatorRepresentation2);
        given(this.organisationService.findUsersByRole(eq(organisationUuid2), eq(AuthorityConstants.ORGANISATION_COORDINATOR)))
            .willReturn(coordinators2);

        // Mock organisations service for fetching reviewers
        UserRepresentation reviewerRepresentation1 = createReviewer(1, reviewerUuid1);
        users.put(reviewerUuid1, reviewerRepresentation1);
        UserRepresentation reviewerRepresentation2 = createReviewer(1, reviewerUuid2);
        users.put(reviewerUuid2, reviewerRepresentation2);
        List<UserRepresentation> reviewers1 = new ArrayList<>();
        reviewers1.add(reviewerRepresentation1);
        reviewers1.add(reviewerRepresentation2);
        given(this.organisationService.findUsersByRole(eq(organisationUuid1), eq(AuthorityConstants.REVIEWER)))
            .willReturn(reviewers1);
        List<UserRepresentation> reviewers2 = new ArrayList<>();
        reviewers2.add(reviewerRepresentation2);
        given(this.organisationService.findUsersByRole(eq(organisationUuid2), eq(AuthorityConstants.REVIEWER)))
            .willReturn(reviewers2);

        // Mock Feign client for fetching user information
        UserRepresentation requesterRepresentation = createRequester();
        users.put(mockRequesterUuid, requesterRepresentation);

        for(Map.Entry<UUID, UserRepresentation> entry: users.entrySet()) {
            given(this.userClientService.findUserByUuid(eq(entry.getKey())))
                .willReturn(entry.getValue());
            given(this.userClientService.findUserByUuidCached(eq(entry.getKey())))
                .willReturn(entry.getValue());
        }

        // Mock notification call
        doNothing().when(this.mailService).sendSubmissionNotificationToCoordinators(any(), any(), anyListOf(UserRepresentation.class));
        doNothing().when(this.mailService).sendRequestClosedNotificationToRequester(any(), any());
        doNothing().when(this.mailService).sendDeliveryReleasedNotificationToRequester(any(), any(), any());
        doNothing().when(this.mailService).sendDeliveryReceivedNotificationToCoordinators(any(), any(), any(), anyListOf(UserRepresentation.class));
        doNothing().when(this.mailService).sendDeliveryCancelledNotificationToRequester(any(), any(), any(UserRepresentation.class));

        // Mock audit service calls
        doNothing().when(this.auditService).publishEvent(any());
    }

    RequestPostProcessor token(UserAuthenticationToken user) {
        if (user == null) {
            return SecurityMockMvcRequestPostProcessors.anonymous();
        }
        log.info("Creating token for user: {} / {}", user.getName(), user.getUuid());
        return tokenUtil.oauth2Authentication(user);
    }

    MockHttpServletRequestBuilder getRequest(
        HttpMethod method,
        String url,
        Object body,
        Map<String, String> parameters) {
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

    MockMultipartHttpServletRequestBuilder getUploadRequest(
            String url,
            URL resource) {
        MockMultipartHttpServletRequestBuilder request = MockMvcRequestBuilders.fileUpload(url);
        try {
            String[] filenameParts = resource.getFile().split("/");
            String filename = filenameParts[filenameParts.length - 1];
            InputStream input = resource.openStream();
            MockMultipartFile file = new MockMultipartFile("file", filename, MediaType.APPLICATION_OCTET_STREAM_VALUE, input);
            log.info("Uploading file {}", file);
            return request.file(file);
        } catch (IOException e) {
            throw new RuntimeException("Error uploading file", e);
        }
    }

    RequestRepresentation newDraft(UserAuthenticationToken user) throws Exception {
        final RequestRepresentation[] request = new RequestRepresentation[1];

        mockMvc.perform(
            getRequest(HttpMethod.POST,
                REQUESTS_ROUTE + "/drafts",
                null,
                Collections.emptyMap())
                .with(token(user))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andDo(result -> {
                log.info("Result: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
                request[0] = mapper.readValue(result.getResponse().getContentAsByteArray(), RequestRepresentation.class);
            });

        Thread.sleep(100);

        return request[0];
    }

    RequestRepresentation updateDraft(UserAuthenticationToken user, RequestRepresentation request) throws Exception {
        final RequestRepresentation[] resultRequest = new RequestRepresentation[1];
        mockMvc.perform(
            getRequest(HttpMethod.PUT,
                REQUESTS_ROUTE + "/drafts",
                request,
                Collections.emptyMap())
                .with(token(user))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andDo(result -> {
                log.info("Result: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
                resultRequest[0] = mapper.readValue(result.getResponse().getContentAsByteArray(), RequestRepresentation.class);
            });
        return resultRequest[0];
    }

    void setRequestData(RequestRepresentation request) {
        RequestDetailRepresentation details = request.getRequestDetail();
        details.setTitle("Test title");
        details.setBackground("Background of the request");
        details.setResearchQuestion("Does it work?");
        details.setHypothesis("H0");
        details.setMethods("Testing");
        details.setSearchQuery("q");
        details.setRequestType(Sets.newSet(RequestType.Data, RequestType.Material));
        PrincipalInvestigatorRepresentation principalInvestigator = details.getPrincipalInvestigator();
        principalInvestigator.setName("Test Person");
        principalInvestigator.setEmail("pi@local");
        principalInvestigator.setJobTitle("Tester");
        principalInvestigator.setAffiliation("The Organisation");
    }

    void initFetchTests() throws Exception {
        // Initialize draft with two organisations
        RequestRepresentation requestTwoOrganisation = newDraft(requester);
        setRequestData(requestTwoOrganisation);
        requestTwoOrganisation.getRequestDetail().setTitle(TEST_TWO_ORGANISATIONS_TITLE);

        submitDraftToOrganisations(requestTwoOrganisation, Arrays.asList(organisationUuid1, organisationUuid2));

        // Initialize draft with one organisation
        RequestRepresentation requestOneOrganisation = newDraft(requester);
        setRequestData(requestOneOrganisation);

        submitDraftToOrganisations(requestOneOrganisation, Arrays.asList(organisationUuid2));
    }

    List<RequestRepresentation> fetchRequests(UserAuthenticationToken user, String query) throws Exception {
        final List<RequestRepresentation>[] res = new List[1];
        mockMvc.perform(
                getRequest(HttpMethod.GET,
                        REQUESTS_ROUTE + query,
                        null,
                        Collections.emptyMap())
                        .with(token(user))
                        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andDo(result -> {
            List<RequestRepresentation> requests =
                    mapper.readValue(result.getResponse().getContentAsByteArray(), listTypeReference);
            res[0] = requests;
        });
        return res[0];
    }

    RequestRepresentation fetchRequest(UserAuthenticationToken user, String query) throws Exception {
        final RequestRepresentation[] res = new RequestRepresentation[1];
        mockMvc.perform(
                getRequest(HttpMethod.GET,
                        REQUESTS_ROUTE + query,
                        null,
                        Collections.emptyMap())
                        .with(token(user))
                        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andDo(result -> {
            RequestRepresentation request =
                    mapper.readValue(result.getResponse().getContentAsByteArray(), RequestRepresentation.class);
            res[0] = request;
        });
        return res[0];
    }

    Map<OverviewStatus, Long> fetchCounts(UserAuthenticationToken user, String query) throws Exception {
        final Map<OverviewStatus, Long>[] res = new Map[1];
        mockMvc.perform(
                getRequest(HttpMethod.GET,
                        REQUESTS_ROUTE + query,
                        null,
                        Collections.emptyMap())
                        .with(token(user))
                        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andDo(result -> {
            log.info("Result: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
            Map<OverviewStatus, Long> counts =
                    mapper.readValue(result.getResponse().getContentAsByteArray(), countsTypeReference);
            res[0] = counts;
        });
        return res[0];
    }

    List<RequestRepresentation> fetchAllForRole(UserAuthenticationToken user, String authority) throws Exception {
        String role;
        switch(authority) {
            case AuthorityConstants.ORGANISATION_COORDINATOR:
                role = "coordinator";
                break;
            case AuthorityConstants.REVIEWER:
                role = "reviewer";
                break;
            case AuthorityConstants.RESEARCHER:
                role = "requester";
                break;
            default:
                throw new RuntimeException("Unsupported authority: " + authority);
        }
        return fetchRequests(user, "/status/Validation/" + role);
    }

    List<RequestRepresentation> fetchAllForCoordinator(UserAuthenticationToken user) throws Exception {
        return fetchAllForRole(user, AuthorityConstants.ORGANISATION_COORDINATOR);
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
    ResultActions performProcessAction(
        UserAuthenticationToken user, String action, UUID requestUuid, HttpMethod method, Object body
    ) throws Exception {
        return mockMvc.perform(
            getRequest(method,
                REQUESTS_ROUTE + "/" + requestUuid.toString() + "/" + action,
                body,
                Collections.emptyMap())
                .with(token(user))
                .accept(MediaType.APPLICATION_JSON));
    }

    List<RequestRepresentation> submitDraftToOrganisations(RequestRepresentation request, List<UUID> organisations) throws Exception {
        // Set organisations
        int i = 1;
        for (UUID uuid : organisations) {
            OrganisationRepresentation organisation = createOrganisation(i, uuid);
            request.getOrganisations().add(organisation);
            i++;
        }
        request = updateDraft(requester, request);
        Assert.assertEquals(organisations.size(), request.getOrganisations().size());

        // Submit the draft. One request should have been generated (and is returned).
        List<RequestRepresentation> requests = fetchRequests( requester,
                "/drafts/" + request.getUuid().toString() + "/submit");

        // Number of requests should equal the number of organisations it was submitted to
        Assert.assertEquals(organisations.size(), requests.size());
        for (RequestRepresentation req: requests) {
            Assert.assertEquals(OverviewStatus.Validation, req.getStatus());
        }
        return requests;
    }

    RequestRepresentation getSubmittedDraft() throws Exception {
        // Initialize draft
        RequestRepresentation request = newDraft(requester);
        setRequestData(request);

        // Setup submitted draft
        List<RequestRepresentation> result = submitDraftToOrganisations(request, Arrays.asList(organisationUuid1));
        Assert.assertEquals(1, result.size());

        return result.get(0);
    }

    RequestRepresentation validateRequest(RequestRepresentation request, UserAuthenticationToken coordinator) throws Exception {
        final RequestRepresentation[] res = new RequestRepresentation[1];
        // Send for review
        performProcessAction(coordinator, ACTION_VALIDATE, request.getUuid(), HttpMethod.GET, null)
            .andExpect(status().isOk())
            .andDo(result -> {
                log.info("Result validated request: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
                RequestRepresentation requestResult =
                    mapper.readValue(result.getResponse().getContentAsByteArray(), RequestRepresentation.class);
                Assert.assertEquals(OverviewStatus.Review, requestResult.getStatus());
                res[0] = requestResult;
            });
        return res[0];
    }

    RequestRepresentation approveRequest(RequestRepresentation request, UserAuthenticationToken coordinator) throws Exception {
        final RequestRepresentation[] res = new RequestRepresentation[1];
        // Approve the request.
        performProcessAction(coordinator, ACTION_APPROVE, request.getUuid(), HttpMethod.GET, null)
            .andExpect(status().isOk())
            .andDo(result -> {
                log.info("Result approved request: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
                RequestRepresentation requestResult =
                    mapper.readValue(result.getResponse().getContentAsByteArray(), RequestRepresentation.class);
                Assert.assertEquals(OverviewStatus.Approved, requestResult.getStatus());
                res[0] = requestResult;
            });
        return res[0];
    }

    RequestRepresentation createDeliveryProcesses(RequestRepresentation request) throws Exception {
        final RequestRepresentation[] res = new RequestRepresentation[1];
        // Start delivery.
        performProcessAction(coordinator1, ACTION_START_DELIVERY, request.getUuid(), HttpMethod.GET, null)
            .andExpect(status().isOk())
            .andDo(result -> {
                log.info("Result delivery request: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
                RequestRepresentation deliveryRequest =
                    mapper.readValue(result.getResponse().getContentAsByteArray(), RequestRepresentation.class);
                res[0] = deliveryRequest;
            });
        return res[0];
    }

    List<DeliveryProcessRepresentation> getDeliveryProcesses(RequestRepresentation request) throws Exception {
        final List<DeliveryProcessRepresentation> deliveryProcesses = new ArrayList<>();
        // Fetch delivery processes
        performProcessAction(coordinator1, ACTION_GET_DELIVERIES, request.getUuid(), HttpMethod.GET, null)
            .andExpect(status().isOk())
            .andDo(result -> {
                log.info("Result delivery processes: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
                deliveryProcesses.addAll(
                    mapper.readValue(result.getResponse().getContentAsByteArray(), deliveryProcessListTypeReference));
            });
        return deliveryProcesses;
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
    ResultActions performDeliveryAction(
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

    RequestRepresentation getApprovedRequest() throws Exception {
        RequestRepresentation request = getSubmittedDraft();

        // Send for review
        validateRequest(request, coordinator1);

        return approveRequest(request, coordinator1);
    }

    RequestFileRepresentation uploadRequestFile(UserAuthenticationToken user, RequestRepresentation request, String filename) throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource(filename);

        log.info("File: {} | {}", filename, resource);

        final RequestFileRepresentation[] resultRequestFile = new RequestFileRepresentation[1];
        mockMvc.perform(
                getUploadRequest(
                        REQUESTS_ROUTE + "/" + request.getUuid().toString() + "/files",
                        resource)
                        .with(token(user))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(result -> {
                    log.info("Result: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
                    resultRequestFile[0] = mapper.readValue(result.getResponse().getContentAsByteArray(), RequestFileRepresentation.class);
                });
        return resultRequestFile[0];
    }

    List<RequestFileRepresentation> getRequestFiles(UserAuthenticationToken user, RequestRepresentation request) throws Exception {
        final List<RequestFileRepresentation> files = new ArrayList<>();
        // Fetch delivery processes
        performProcessAction(user, ACTION_GET_FILES, request.getUuid(), HttpMethod.GET, null)
                .andExpect(status().isOk())
                .andDo(result -> {
                    log.info("Result files: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
                    files.addAll(
                            mapper.readValue(result.getResponse().getContentAsByteArray(), requestFileListTypeReference));
                });
        return files;
    }

    /**
     * Create a request template.
     * This endpoint uses Basic authentication.
     * @param requestTemplateRepresentation The request template data.
     * @param authentication A string of the format "username:password", which will be base64 encoded.
     * @return the view URL for the request template.
     */
    URI createRequestTemplate(RequestTemplateRepresentation requestTemplateRepresentation, String authentication) throws Exception {
        MockHttpServletRequestBuilder request = getRequest(HttpMethod.POST, "/api/public/requests/templates",
                requestTemplateRepresentation, Collections.emptyMap());

        HttpHeaders header = new HttpHeaders();
        String base64 = new String(Base64.encode(authentication.getBytes()), Charset.forName("UTF-8"));
        header.add("Authorization", String.format("Basic %s", base64));
        request.headers(header);

        final URI[] uri = new URI[1];
        ResultActions createRequestTemplateRequest = mockMvc.perform(request);
        createRequestTemplateRequest
                .andDo(result -> {
                    log.info("Result request template: {} ({})", result.getResponse().getStatus(),
                            result.getResponse().getContentAsString());
                    Assert.assertEquals(result.getResponse().getStatus(), HttpStatus.ACCEPTED.value());
                    uri[0] = new URI(result.getResponse().getContentAsString().replace("\"", ""));
                });
        return uri[0];
    }

    RequestTemplateRepresentation getRequestTemplate(UserAuthenticationToken user, UUID uuid) throws Exception {
        final RequestTemplateRepresentation[] template = new RequestTemplateRepresentation[1];
        // Fetch request template
        mockMvc.perform(
                getRequest(HttpMethod.GET,
                        REQUESTS_ROUTE + "/templates/" + uuid.toString(),
                        null,
                        Collections.emptyMap())
                        .with(token(user))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(result -> {
                    log.info("Result template: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
                    template[0] =
                            mapper.readValue(result.getResponse().getContentAsByteArray(), RequestTemplateRepresentation.class);
                });
        return template[0];
    }

    static List<UserRepresentation> nonEmptyUserRepresentationList() {
        return argThat(allOf(org.hamcrest.Matchers.isA(Collection.class), hasSize(greaterThan(0))));
    }

    static List<RequestRepresentation> nonEmptyRequestList() {
        return argThat(allOf(org.hamcrest.Matchers.isA(Collection.class), hasSize(greaterThan(0))));
    }

    static List<RequestFileRepresentation> nonEmptyRequestFileList() {
        return argThat(allOf(org.hamcrest.Matchers.isA(Collection.class), hasSize(greaterThan(0))));
    }

}
