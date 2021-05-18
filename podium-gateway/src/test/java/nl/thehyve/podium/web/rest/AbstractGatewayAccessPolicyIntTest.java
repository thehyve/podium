package nl.thehyve.podium.web.rest;

import nl.thehyve.podium.common.enumeration.RequestType;
import nl.thehyve.podium.common.resource.InternalRequestResource;
import nl.thehyve.podium.common.security.AuthenticatedUser;
import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.common.security.SerialisedUser;
import nl.thehyve.podium.common.security.UserAuthenticationToken;
import nl.thehyve.podium.common.service.dto.OrganisationRepresentation;
import nl.thehyve.podium.common.service.dto.RequestRepresentation;
import nl.thehyve.podium.common.service.dto.UserRepresentation;
import nl.thehyve.podium.service.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.collections.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.net.URISyntaxException;
import java.util.*;

import static nl.thehyve.podium.web.rest.RequestDataHelper.setRequestData;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

public abstract class AbstractGatewayAccessPolicyIntTest extends AbstractGatewayIntTest {

    static final String REQUEST_ROUTE = "/api/requests";

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    WebApplicationContext context;

    @Autowired
    TestService testService;

    @MockBean
    OrganisationClientService organisationService;

    @MockBean
    UserClientService userClientService;

    @MockBean
    AuditService auditService;

    @MockBean
    InternalRequestResource internalRequestResource;

    @MockBean
    MailService mailService;

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


    //region Organisations test data

    OrganisationRepresentation organisationA;
    OrganisationRepresentation organisationB;
    List<OrganisationRepresentation> organisations = new ArrayList<>();
    Map<UUID, Map<String, Set<UUID>>> organisationRoles = new HashMap<>();

    private static OrganisationRepresentation createOrganisation(String organisationName, UUID organisationUuid) {
        Set<RequestType> requestTypes = new HashSet<>();
        requestTypes.add(RequestType.Data);
        requestTypes.add(RequestType.Images);
        requestTypes.add(RequestType.Material);

        OrganisationRepresentation organisation = new OrganisationRepresentation();
        organisation.setUuid(organisationUuid);
        organisation.setName(organisationName);
        organisation.setShortName(organisationName);
        organisation.setRequestTypes(requestTypes);
        return organisation;
    }

    void createOrganisations() {
        organisationA = createOrganisation("A", UUID.randomUUID());
        organisationB = createOrganisation("B", UUID.randomUUID());
        organisations.addAll(Arrays.asList(organisationA, organisationB));
        for (OrganisationRepresentation organisation: organisations) {
            Map<String, Set<UUID>> roles = new HashMap<>();
            roles.put(AuthorityConstants.ORGANISATION_ADMIN, new HashSet<>());
            roles.put(AuthorityConstants.ORGANISATION_COORDINATOR, new HashSet<>());
            roles.put(AuthorityConstants.REVIEWER, new HashSet<>());
            organisationRoles.put(organisation.getUuid(), roles);
        }
    }

    //endregion


    //region Users test data

    AuthenticatedUser podiumAdmin;
    AuthenticatedUser bbmriAdmin;
    AuthenticatedUser adminOrganisationA;
    AuthenticatedUser adminOrganisationB;
    AuthenticatedUser adminOrganisationAandB;
    AuthenticatedUser coordinatorOrganisationA;
    AuthenticatedUser coordinatorOrganisationB;
    AuthenticatedUser coordinatorOrganisationAandB;
    AuthenticatedUser reviewerAandB;
    AuthenticatedUser reviewerA;
    AuthenticatedUser researcher;
    AuthenticatedUser testUser1;
    AuthenticatedUser testUser2;
    AuthenticatedUser anonymous;
    Set<AuthenticatedUser> allUsers = new LinkedHashSet<>();
    Map<String, SerialisedUser> userStore = new HashMap<>();
    Map<UUID, UserRepresentation> userInfo = new HashMap<>();

    private AuthenticatedUser createUser(String name, String authority, OrganisationRepresentation... organisations) {
        log.info("Creating user {}", name);
        UUID userUuid = UUID.randomUUID();
        UserRepresentation userDetails = new UserRepresentation();
        userDetails.setLogin(name);
        userDetails.setUuid(userUuid);
        userDetails.setLogin("test_" + name);
        userDetails.setEmail("test_" + name + "@localhost");
        userDetails.setFirstName("test_firstname_"+name);
        userDetails.setLastName("test_lastname_"+name);
        userDetails.setLangKey("en");
        Set<String> authorities = new HashSet<>();
        Map<UUID, Collection<String>> roles = new HashMap<>();
        if (organisations.length > 0) {
            for (OrganisationRepresentation organisation: organisations) {
                log.debug("Assigning role {} for organisation {}", authority, organisation.getName());
                organisationRoles.get(organisation.getUuid()).get(authority).add(userUuid);
                roles.put(organisation.getUuid(), Sets.newSet(authority));
            }
        }
        if (authority != null) {
            log.debug("Assigning role {}", authority);
            authorities.add(authority);
        }
        SerialisedUser user = new SerialisedUser(userUuid, name, authorities, roles);
        {
            log.debug("Checking user {}", name);
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

    void createUsers() {
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
        allUsers.add(anonymous);
    }

    //endregion

    /**
     * Creates a map from user UUID to a url with a URL with a request UUID specific for the user
     * The query string should have a '%s' format specifier where the UUID should be placed.
     */
    Map<UUID, String> getUrlsForUsers(Map<UUID, ?> objectMap, String query) {
        return getUrlsForUsers(allUsers, REQUEST_ROUTE, query, objectMap);
    }

    private Collection<RequestRepresentation> allRequests = new ArrayList<>();

    RequestRepresentation createSubmittedRequest() throws Exception {
        RequestRepresentation request = newDraft(researcher);
        setRequestData(request);
        request.getRequestDetail().setRequestType(Sets.newSet(RequestType.Data, RequestType.Material, RequestType.Images));
        initRequestResourceMock(request);
        request = updateDraft(researcher, request);
        initRequestResourceMock(request);
        List<RequestRepresentation> submittedRequests = submitDraftToOrganisations(researcher, request, Arrays.asList(organisationA.getUuid()));
        Assert.assertEquals(submittedRequests.size(), 1);
        RequestRepresentation submittedRequest = submittedRequests.get(0);
        initRequestResourceMock(submittedRequest);
        return submittedRequest;
    }

    RequestRepresentation createValidatedRequest() throws Exception {
        RequestRepresentation request = createSubmittedRequest();
        request = validateRequest(request, coordinatorOrganisationA);
        return request;
    }

    RequestRepresentation createApprovedRequest() throws Exception {
        RequestRepresentation request = createValidatedRequest();
        request = approveRequest(request, coordinatorOrganisationA);
        return request;
    }

    abstract Collection<RequestRepresentation> createRequests() throws Exception;

    private void initMocks() {
        // Mock Feign client for organisations
        for(OrganisationRepresentation organisation: organisations) {
            log.info("Mocking organisation endpoint for {}", organisation.getUuid());
            given(this.organisationService.findOrganisationByUuid(eq(organisation.getUuid())))
                    .willReturn(organisation);
        }

        // Don't return anything on findUsersByRole; only used for notification mails
        given(this.organisationService.findUsersByRole(any(), any()))
                .willReturn(Collections.emptyList());
        // Return reviewers of organisation A
        List<UserRepresentation> reviewersA = Arrays.asList(
            userInfo.get(reviewerA.getUuid()),
            userInfo.get(reviewerAandB.getUuid()));
        given(this.organisationService.findUsersByRole(organisationA.getUuid(), AuthorityConstants.REVIEWER))
            .willReturn(reviewersA);

        // Mock Feign client for fetching user information
        for(Map.Entry<UUID, UserRepresentation> userEntry: userInfo.entrySet()) {
            given(this.userClientService.findUserByUuid(eq(userEntry.getKey())))
                    .willReturn(userEntry.getValue());
            given(this.userClientService.findUserByUuidCached(eq(userEntry.getKey())))
                .willReturn(userEntry.getValue());
        }

        // Mock mail sending
        doNothing().when(this.mailService).sendEmail(anyString(), anyString(), anyString(), anyBoolean(), anyBoolean());
        doNothing().when(this.mailService).sendSubmissionNotificationToRequester(any(), anyList());

        // Mock audit service calls
        doNothing().when(this.auditService).publishEvent(any());
    }

    void initRequestResourceMock(RequestRepresentation request) throws URISyntaxException {
        // Return request for the request security service
        given(this.internalRequestResource.getRequestBasic(eq(request.getUuid())))
            .willReturn(ResponseEntity.ok(request));
    }

    void setupData() throws Exception {
        createOrganisations();
        createUsers();
        initMocks();
        allRequests = createRequests();
        // Return requests for the request security service
        for(RequestRepresentation request: allRequests) {
            initRequestResourceMock(request);
        }
    }

}
