package nl.thehyve.podium.web.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import nl.thehyve.podium.common.enumeration.OverviewStatus;
import nl.thehyve.podium.common.enumeration.RequestType;
import nl.thehyve.podium.common.security.AuthenticatedUser;
import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.common.service.dto.*;
import nl.thehyve.podium.common.test.AbstractAuthorisedUserIntTest;
import nl.thehyve.podium.repository.RequestRepository;
import nl.thehyve.podium.repository.search.RequestSearchRepository;
import nl.thehyve.podium.service.*;
import org.junit.Assert;
import org.mockito.internal.util.collections.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;

import static nl.thehyve.podium.web.rest.RequestDataHelper.setRequestData;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class AbstractGatewayIntTest extends AbstractAuthorisedUserIntTest {

    static List<UserRepresentation> nonEmptyUserRepresentationList() {
        return argThat(allOf(org.hamcrest.Matchers.isA(Collection.class), hasSize(greaterThan(0))));
    }

    static List<RequestRepresentation> nonEmptyRequestList() {
        return argThat(allOf(org.hamcrest.Matchers.isA(Collection.class), hasSize(greaterThan(0))));
    }

    static List<RequestFileRepresentation> nonEmptyRequestFileList() {
        return argThat(allOf(org.hamcrest.Matchers.isA(Collection.class), hasSize(greaterThan(0))));
    }

    @Autowired
    TestService testService;

    @Autowired
    RequestRepository requestRepository;

    @Autowired
    RequestSearchRepository requestSearchRepository;

    @Autowired
    WebApplicationContext context;

    TypeReference<List<RequestRepresentation>> listTypeReference =
        new TypeReference<List<RequestRepresentation>>(){};

    TypeReference<List<DeliveryProcessRepresentation>> deliveryProcessListTypeReference =
        new TypeReference<List<DeliveryProcessRepresentation>>(){};

    TypeReference<Map<OverviewStatus, Long>> countsTypeReference =
        new TypeReference<Map<OverviewStatus, Long>>(){};

    TypeReference<List<RequestFileRepresentation>> requestFileListTypeReference =
            new TypeReference<List<RequestFileRepresentation>>(){};

    MockMvc mockMvc;

    protected MockMvc getMockMvc() {
        return mockMvc;
    }

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

    UserRepresentation createRequester(UUID requesterUuid, String username) {
        UserRepresentation requesterRepresentation = new UserRepresentation();
        requesterRepresentation.setUuid(requesterUuid);
        requesterRepresentation.setLogin(username);
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

    RequestRepresentation newDraft(AuthenticatedUser user) throws Exception {
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

    RequestRepresentation updateDraft(AuthenticatedUser user, RequestRepresentation request) throws Exception {
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

    List<RequestRepresentation> fetchRequests(AuthenticatedUser user, String query) throws Exception {
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

    RequestRepresentation fetchRequest(AuthenticatedUser user, String query) throws Exception {
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

    Map<OverviewStatus, Long> fetchCounts(AuthenticatedUser user, String query) throws Exception {
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

    List<RequestRepresentation> fetchAllForRole(AuthenticatedUser user, String authority) throws Exception {
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

    List<RequestRepresentation> fetchAllForCoordinator(AuthenticatedUser user) throws Exception {
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
        AuthenticatedUser user, String action, UUID requestUuid, HttpMethod method, Object body
    ) throws Exception {
        return mockMvc.perform(
            getRequest(method,
                REQUESTS_ROUTE + "/" + requestUuid.toString() + "/" + action,
                body,
                Collections.emptyMap())
                .with(token(user))
                .accept(MediaType.APPLICATION_JSON));
    }

    List<RequestRepresentation> submitDraftToOrganisations(AuthenticatedUser requester, RequestRepresentation request, List<UUID> organisations) throws Exception {
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

    RequestRepresentation getSubmittedDraft(AuthenticatedUser requester, UUID organisationUuid) throws Exception {
        // Initialize draft
        RequestRepresentation request = newDraft(requester);
        setRequestData(request);

        // Setup submitted draft
        List<RequestRepresentation> result = submitDraftToOrganisations(requester, request, Arrays.asList(organisationUuid));
        Assert.assertEquals(1, result.size());

        return result.get(0);
    }

    RequestRepresentation validateRequest(RequestRepresentation request, AuthenticatedUser coordinator) throws Exception {
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

    RequestRepresentation approveRequest(RequestRepresentation request, AuthenticatedUser coordinator) throws Exception {
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

    RequestRepresentation createDeliveryProcesses(AuthenticatedUser coordinator, RequestRepresentation request) throws Exception {
        final RequestRepresentation[] res = new RequestRepresentation[1];
        // Start delivery.
        performProcessAction(coordinator, ACTION_START_DELIVERY, request.getUuid(), HttpMethod.GET, null)
            .andExpect(status().isOk())
            .andDo(result -> {
                log.info("Result delivery request: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
                RequestRepresentation deliveryRequest =
                    mapper.readValue(result.getResponse().getContentAsByteArray(), RequestRepresentation.class);
                res[0] = deliveryRequest;
            });
        return res[0];
    }

    List<DeliveryProcessRepresentation> getDeliveryProcesses(AuthenticatedUser coordinator, RequestRepresentation request) throws Exception {
        final List<DeliveryProcessRepresentation> deliveryProcesses = new ArrayList<>();
        // Fetch delivery processes
        performProcessAction(coordinator, ACTION_GET_DELIVERIES, request.getUuid(), HttpMethod.GET, null)
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
        AuthenticatedUser user, String action, UUID requestUuid, UUID deliveryProcessUuid, HttpMethod method, Object body
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

    DeliveryProcessRepresentation releaseDelivery(AuthenticatedUser coordinator, RequestRepresentation request, DeliveryProcessRepresentation deliveryProcess, DeliveryReferenceRepresentation reference) throws Exception {
        // Release
        ResultActions releaseDeliveryResult
            = performDeliveryAction(coordinator, DELIVERY_RELEASE, request.getUuid(), deliveryProcess.getUuid(), HttpMethod.POST, reference);

        final DeliveryProcessRepresentation[] res = new DeliveryProcessRepresentation[1];
        releaseDeliveryResult
            .andExpect(status().isOk())
            .andDo(result -> {
                log.info("Result delivery process: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
                DeliveryProcessRepresentation resultDeliveryProcess =
                    mapper.readValue(result.getResponse().getContentAsByteArray(), DeliveryProcessRepresentation.class);
                res[0] = resultDeliveryProcess;
            });
        return res[0];
    }

    RequestRepresentation getApprovedRequest(AuthenticatedUser requester, AuthenticatedUser coordinator, UUID organisationUuid) throws Exception {
        RequestRepresentation request = getSubmittedDraft(requester, organisationUuid);

        // Send for review
        validateRequest(request, coordinator);

        return approveRequest(request, coordinator);
    }

    RequestFileRepresentation uploadRequestFile(AuthenticatedUser user, RequestRepresentation request, String filename) throws Exception {
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

    List<RequestFileRepresentation> getRequestFiles(AuthenticatedUser user, RequestRepresentation request) throws Exception {
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
     * Perform a request template create action.
     * This endpoint uses Basic authentication.
     * @param requestTemplateRepresentation The request template data.
     * @param authentication A string of the format "username:password".
     * @param encodeAuthentication Whether to encode the authentication string with base64 encoding.
     * @return the result of the action.
     */
    ResultActions performCreateRequestTemplate(
        RequestTemplateRepresentation requestTemplateRepresentation,
        String authentication,
        boolean encodeAuthentication) throws Exception {
        MockHttpServletRequestBuilder request = getRequest(HttpMethod.POST, "/api/public/requests/templates",
            requestTemplateRepresentation, Collections.emptyMap());

        HttpHeaders header = new HttpHeaders();
        if (encodeAuthentication) {
            String base64 = new String(Base64.encode(authentication.getBytes()), Charset.forName("UTF-8"));
            header.add("Authorization", String.format("Basic %s", base64));
        } else {
            header.add("Authorization", String.format("Basic %s", authentication));
        }
        request.headers(header);

        return mockMvc.perform(request);
    }

    /**
     * Create a request template.
     * This endpoint uses Basic authentication.
     * @param requestTemplateRepresentation The request template data.
     * @param authentication A string of the format "username:password", which will be base64 encoded.
     * @return the view URL for the request template.
     */
    URI createRequestTemplate(RequestTemplateRepresentation requestTemplateRepresentation, String authentication) throws Exception {
        final URI[] uri = new URI[1];
        ResultActions createRequestTemplateRequest = performCreateRequestTemplate(
            requestTemplateRepresentation, authentication, true);
        createRequestTemplateRequest
                .andDo(result -> {
                    log.info("Result request template: {} ({})", result.getResponse().getStatus(),
                            result.getResponse().getContentAsString());
                    Assert.assertEquals(result.getResponse().getStatus(), HttpStatus.ACCEPTED.value());
                    uri[0] = new URI(result.getResponse().getHeader("Location"));
                });
        return uri[0];
    }

    RequestTemplateRepresentation getRequestTemplate(AuthenticatedUser user, UUID uuid) throws Exception {
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

}
